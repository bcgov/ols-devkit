package com.revolsys.record.code;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.identifier.ListIdentifier;
import org.jeometry.common.data.identifier.SingleIdentifier;
import org.jeometry.common.date.Dates;
import org.jeometry.common.io.PathName;

import com.revolsys.collection.list.Lists;
import com.revolsys.record.Record;
import com.revolsys.record.comparator.RecordFieldComparator;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.query.And;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.Property;
import com.revolsys.util.count.CategoryLabelCountMap;

public class SingleValueRecordStoreCodeTable extends AbstractSingleValueCodeTable
  implements RecordDefinitionProxy {

  private static final String DEFAULT_FIELD_NAME = "VALUE";

  private boolean createMissingCodes = true;

  private String creationTimestampFieldName;

  private List<String> fieldNameAliases = new ArrayList<>();

  private String idFieldName;

  private boolean loadAll = true;

  private boolean loaded = false;

  private boolean loading = false;

  private boolean loadMissingCodes = true;

  private String modificationTimestampFieldName;

  private String orderBy = DEFAULT_FIELD_NAME;

  private RecordDefinition recordDefinition;

  private RecordStore recordStore;

  private final ThreadLocal<Boolean> threadLoading = new ThreadLocal<>();

  private PathName typePath;

  private String valueFieldName = DEFAULT_FIELD_NAME;

  private boolean allowNullValues = false;

  public SingleValueRecordStoreCodeTable() {
  }

  public SingleValueRecordStoreCodeTable(final Map<String, ? extends Object> config) {
    setProperties(config);
  }

  public void addFieldAlias(final String columnName) {
    this.fieldNameAliases.add(columnName);
  }

  @Override
  public void addValue(final Record code) {
    final String idFieldName = getIdFieldName();
    final Identifier id = code.getIdentifier(idFieldName);
    if (id == null) {
      throw new NullPointerException(idFieldName + "=null for " + code);
    } else {
      Object value = code.getValue(this.valueFieldName);
      if (value instanceof SingleIdentifier) {
        final SingleIdentifier identifier = (SingleIdentifier)value;
        value = identifier.getValue(0);
      }
      if (value == null) {
        if (!this.allowNullValues) {
          throw new NullPointerException(this.valueFieldName + "=null for " + code);
        }
      }
      addValue(id, value);
    }
  }

  protected void addValues(final Iterable<Record> allCodes) {
    for (final Record code : allCodes) {
      addValue(code);
    }
  }

  @Override
  protected int calculateValueFieldLength() {
    return this.recordDefinition.getFieldLength(this.valueFieldName);
  }

  @Override
  public SingleValueRecordStoreCodeTable clone() {
    final SingleValueRecordStoreCodeTable clone = (SingleValueRecordStoreCodeTable)super.clone();
    clone.recordDefinition = null;
    clone.fieldNameAliases = new ArrayList<>(this.fieldNameAliases);
    return clone;
  }

  @SuppressWarnings("unchecked")
  public <C extends CodeTable> C getCodeTable() {
    return (C)this;
  }

  public String getCreationTimestampFieldName() {
    return this.creationTimestampFieldName;
  }

  @Override
  public List<String> getFieldNameAliases() {
    return this.fieldNameAliases;
  }

  @Override
  public String getIdFieldName() {
    if (this.idFieldName != null) {
      return this.idFieldName;
    } else if (this.recordDefinition == null) {
      return "";
    } else {
      final String idFieldName = this.recordDefinition.getIdFieldName();
      if (idFieldName == null) {
        return this.recordDefinition.getFieldName(0);
      } else {
        return idFieldName;
      }
    }
  }

  @Override
  public Map<String, ? extends Object> getMap(final Identifier id) {
    final Object value = getValue(id);
    if (value == null) {
      return Collections.emptyMap();
    } else {
      return Collections.singletonMap(this.valueFieldName, value);
    }
  }

  public String getModificationTimestampFieldName() {
    return this.modificationTimestampFieldName;
  }

  @Override
  public Record getRecord(final Identifier id) {
    return this.recordStore.getRecord(this.typePath, id);
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends RecordStore> R getRecordStore() {
    return (R)this.recordStore;
  }

  public String getTypeName() {
    return this.typePath.getPath();
  }

  public PathName getTypePath() {
    return this.typePath;
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  @Override
  public <V> V getValue(final Object id) {
    if (id instanceof Identifier) {
      return getValue((Identifier)id);
    } else if (id instanceof List) {
      final List list = (List)id;
      return getValue(new ListIdentifier(list));
    } else {
      return getValue(Identifier.newIdentifier(id));
    }
  }

  public String getValueFieldName() {
    return this.valueFieldName;
  }

  @Override
  public List<String> getValueFieldNames() {
    return Collections.singletonList(this.valueFieldName);
  }

  public boolean isAllowNullValues() {
    return this.allowNullValues;
  }

  public boolean isCreateMissingCodes() {
    return this.createMissingCodes;
  }

  @Override
  public boolean isLoadAll() {
    return this.loadAll;
  }

  @Override
  public boolean isLoaded() {
    return this.loaded;
  }

  @Override
  public boolean isLoading() {
    return this.loading;
  }

  @Override
  public boolean isLoadMissingCodes() {
    return this.loadMissingCodes;
  }

  public synchronized void loadAll() {
    final long time = System.currentTimeMillis();
    if (this.threadLoading.get() != Boolean.TRUE) {
      if (this.loading) {
        while (this.loading) {
          try {
            wait(1000);
          } catch (final InterruptedException e) {
          }
        }
        return;
      } else {
        this.threadLoading.set(Boolean.TRUE);
        this.loading = true;
        try {
          if (this.recordStore != null) {
            final RecordDefinition recordDefinition = this.recordStore
              .getRecordDefinition(this.typePath);
            final Query query = new Query(recordDefinition);
            query.addOrderBy(this.orderBy);
            try (
              RecordReader reader = this.recordStore.getRecords(query)) {
              final List<Record> codes = reader.toList();
              final CategoryLabelCountMap statistics = this.recordStore.getStatistics();
              if (statistics != null) {
                statistics.getLabelCountMap("query").addCount(this.typePath, -codes.size());
              }
              Collections.sort(codes, new RecordFieldComparator(this.orderBy));
              addValues(codes);
            }
          }
        } finally {
          this.loading = false;
          this.loaded = true;
          this.threadLoading.set(null);
          this.notifyAll();
        }
        Property.firePropertyChange(this, "valuesChanged", false, true);
      }
    }
    Dates.debugEllapsedTime(this, "Load All: " + getTypePath(), time);
  }

  @Override
  protected synchronized Identifier loadId(final Object value, final boolean createId) {
    if (this.loadAll && !this.loadMissingCodes && !isEmpty()) {
      return null;
    }
    Identifier id = null;
    if (createId && this.loadAll && !isLoaded()) {
      loadAll();
      id = getIdentifier(value, false);
    } else {
      final Query query = new Query(this.typePath);
      final And and = new And();
      if (value == null) {
        and.and(Q.isNull(this.valueFieldName));
      } else {
        final FieldDefinition fieldDefinition = this.recordDefinition.getField(this.valueFieldName);
        and.and(Q.equal(fieldDefinition, value));
      }
      query.setWhereCondition(and);
      final RecordReader reader = this.recordStore.getRecords(query);
      try {
        final List<Record> codes = reader.toList();
        if (codes.size() > 0) {
          final CategoryLabelCountMap statistics = this.recordStore.getStatistics();
          if (statistics != null) {
            statistics.getLabelCountMap("query").addCount(this.typePath, -codes.size());
          }

          addValues(codes);
        }
        id = getIdByValue(value);
        Property.firePropertyChange(this, "valuesChanged", false, true);
      } finally {
        reader.close();
      }
    }
    if (createId && id == null) {
      return newIdentifier(value);
    } else {
      return id;
    }
  }

  @Override
  protected Object loadValues(final Object id) {
    if (this.loadAll && !isLoaded()) {
      loadAll();
    } else if (!this.loadAll || this.loadMissingCodes) {
      try {
        final Record code;
        if (id instanceof Identifier) {
          final Identifier identifier = (Identifier)id;
          code = this.recordStore.getRecord(this.typePath, identifier);
        } else {
          code = this.recordStore.getRecord(this.typePath, id);
        }
        if (code != null) {
          addValue(code);
        }
      } catch (final Throwable e) {
        return null;
      }
    }
    return getValueById(id);
  }

  protected synchronized Identifier newIdentifier(final Object value) {
    if (this.createMissingCodes) {
      // TODO prevent duplicates from other threads/processes
      final Record code = this.recordStore.newRecord(this.typePath);
      final RecordDefinition recordDefinition = code.getRecordDefinition();
      Identifier id = this.recordStore.newPrimaryIdentifier(this.typePath);
      if (id == null) {
        final FieldDefinition idField = recordDefinition.getIdField();
        if (idField != null) {
          if (Number.class.isAssignableFrom(idField.getDataType().getJavaClass())) {
            id = Identifier.newIdentifier(getNextId());
          } else {
            id = Identifier.newIdentifier(UUID.randomUUID().toString());
          }
        }
      }
      code.setIdentifier(id);
      code.setValue(this.valueFieldName, value);

      final Timestamp now = new Timestamp(System.currentTimeMillis());
      if (this.creationTimestampFieldName != null) {
        code.setValue(this.creationTimestampFieldName, now);
      }
      if (this.modificationTimestampFieldName != null) {
        code.setValue(this.modificationTimestampFieldName, now);
      }

      this.recordStore.insertRecord(code);
      return code.getIdentifier();
    } else {
      return null;
    }
  }

  @Override
  public synchronized void refresh() {
    super.refresh();
    if (isLoadAll()) {
      this.loaded = false;
      loadAll();
    }
  }

  @Override
  public void refreshIfNeeded() {
    if (this.loadAll && !this.loading) {
      super.refreshIfNeeded();
    }
  }

  public SingleValueRecordStoreCodeTable setAllowNullValues(final boolean allowNullValues) {
    this.allowNullValues = allowNullValues;
    return this;
  }

  public SingleValueRecordStoreCodeTable setCreateMissingCodes(final boolean createMissingCodes) {
    this.createMissingCodes = createMissingCodes;
    return this;
  }

  public SingleValueRecordStoreCodeTable setCreationTimestampFieldName(
    final String creationTimestampFieldName) {
    this.creationTimestampFieldName = creationTimestampFieldName;
    return this;
  }

  public SingleValueRecordStoreCodeTable setFieldAliases(final String... fieldNameAliases) {
    setFieldNameAliases(Lists.newArray(fieldNameAliases));
    return this;
  }

  public SingleValueRecordStoreCodeTable setFieldNameAliases(final List<String> fieldNameAliases) {
    this.fieldNameAliases = new ArrayList<>(fieldNameAliases);
    return this;
  }

  public SingleValueRecordStoreCodeTable setIdFieldName(final String idFieldName) {
    this.idFieldName = idFieldName;
    return this;
  }

  public SingleValueRecordStoreCodeTable setLoadAll(final boolean loadAll) {
    this.loadAll = loadAll;
    return this;
  }

  @Override
  public SingleValueRecordStoreCodeTable setLoadMissingCodes(final boolean loadMissingCodes) {
    this.loadMissingCodes = loadMissingCodes;
    return this;
  }

  public SingleValueRecordStoreCodeTable setModificationTimestampFieldName(
    final String modificationTimestampFieldName) {
    this.modificationTimestampFieldName = modificationTimestampFieldName;
    return this;
  }

  public SingleValueRecordStoreCodeTable setOrderByFieldName(final String orderByFieldName) {
    this.orderBy = orderByFieldName;
    return this;
  }

  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    if (this.recordDefinition != recordDefinition) {
      if (this.recordDefinition != null) {
        setRecordDefinitionBefore(this.recordDefinition);
      }
      this.recordDefinition = recordDefinition;
      if (recordDefinition == null) {
        this.recordStore = null;
        this.typePath = null;
      } else {
        this.typePath = recordDefinition.getPathName();
        final String name = this.typePath.getName();
        setName(name);
        if (this.idFieldName == null) {
          this.idFieldName = recordDefinition.getIdFieldName();
        }
        this.recordStore = this.recordDefinition.getRecordStore();
        setValueFieldDefinition(recordDefinition.getField(this.valueFieldName));
        setRecordDefinitionAfter(recordDefinition);
      }
    }
  }

  protected void setRecordDefinitionAfter(final RecordDefinition recordDefinition) {
  }

  protected void setRecordDefinitionBefore(final RecordDefinition oldRecordDefinition) {
  }

  public SingleValueRecordStoreCodeTable setValueFieldName(final String valueFieldName) {
    this.valueFieldName = valueFieldName;
    if (this.orderBy == DEFAULT_FIELD_NAME) {
      setOrderByFieldName(valueFieldName);
    }
    return this;
  }

  @Override
  public String toString() {
    return this.typePath + " " + getIdFieldName() + " " + this.valueFieldName;

  }

  public String toString(final List<String> values) {
    final StringBuilder string = new StringBuilder(values.get(0));
    for (int i = 1; i < values.size(); i++) {
      final String value = values.get(i);
      string.append(",");
      string.append(value);
    }
    return string.toString();
  }
}
