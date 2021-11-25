package com.revolsys.record.schema;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.jdbc.JdbcConnection;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.record.ArrayChangeTrackRecord;
import com.revolsys.record.ChangeTrackRecord;
import com.revolsys.record.ODataParser;
import com.revolsys.record.Record;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.format.json.JsonList;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.query.Cast;
import com.revolsys.record.query.ColumnReference;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Or;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.TableReference;
import com.revolsys.transaction.Transaction;
import com.revolsys.transaction.TransactionOptions;
import com.revolsys.transaction.TransactionRecordReader;
import com.revolsys.util.Property;

public class AbstractTableRecordStore {

  public static JsonObject schemaToJson(final RecordDefinition recordDefinition) {
    final JsonList jsonFields = JsonList.array();
    final String idFieldName = recordDefinition.getIdFieldName();
    final JsonObject jsonSchema = JsonObject.hash()
      .addValue("typeName", recordDefinition.getPathName())
      .addValue("title", recordDefinition.getTitle())
      .addValue("idFieldName", idFieldName)
      .addValue("geometryFieldName", recordDefinition.getGeometryFieldName())
      .addValue("fields", jsonFields);
    final GeometryFactory geometryFactory = recordDefinition.getGeometryFactory();
    if (geometryFactory != null) {
      final int coordinateSystemId = geometryFactory.getHorizontalCoordinateSystemId();
      if (coordinateSystemId > 0) {
        jsonSchema.addValue("srid", coordinateSystemId);
      }
    }
    for (final FieldDefinition field : recordDefinition.getFields()) {
      final String fieldName = field.getName();
      final DataType dataType = field.getDataType();
      String dataTypeString = dataType.toString();
      if (dataTypeString.startsWith("List")) {
        dataTypeString = dataTypeString.substring(4) + "[]";
      }
      final JsonObject jsonField = JsonObject.hash()
        .addValue("name", fieldName)
        .addNotEmpty("title", field.getTitle().replace(" Ind", ""))
        .addNotEmpty("description", field.getDescription())
        .addValue("dataType", dataTypeString)
        .addValue("required", field.isRequired());
      final int length = field.getLength();
      if (length > 0) {
        jsonField.addValue("length", length);
      }
      final int scale = field.getScale();
      if (scale > 0) {
        jsonField.addValue("scale", scale);
      }
      jsonField//
        .addNotEmpty("default", field.getDefaultValue())
        .addNotEmpty("allowedValues", field.getAllowedValues())
        .addNotEmpty("min", field.getMinValue())
        .addNotEmpty("max", field.getMaxValue());
      final CodeTable codeTable = field.getCodeTable();
      if (codeTable != null) {
        jsonField.addNotEmpty("codeTable", codeTable.getName());
      }
      jsonFields.add(jsonField);
    }
    return jsonSchema;
  }

  private JdbcRecordStore recordStore;

  private final PathName tablePath;

  private final String typeName;

  private RecordDefinition recordDefinition;

  protected Map<QueryValue, Boolean> defaultSortOrder = new LinkedHashMap<>();

  private final Set<String> searchFieldNames = new LinkedHashSet<>();

  public AbstractTableRecordStore(final PathName typePath) {
    this.tablePath = typePath;
    this.typeName = typePath.getName();
  }

  public AbstractTableRecordStore(final PathName typePath, final JdbcRecordStore recordStore) {
    this(typePath);
    setRecordStore(recordStore);
  }

  protected void addDefaultSortOrder(final String fieldName) {
    addDefaultSortOrder(fieldName, true);
  }

  protected void addDefaultSortOrder(final String fieldName, final boolean ascending) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition != null) {
      final FieldDefinition field = recordDefinition.getFieldDefinition(fieldName);
      if (field != null) {
        this.defaultSortOrder.put(field, ascending);
      }
    }
  }

  public void addQueryOrderBy(final Query query, final String orderBy) {
    if (Property.hasValue(orderBy)) {
      for (String orderClause : orderBy.split(",")) {
        orderClause = orderClause.trim();
        String fieldName;
        boolean ascending = true;
        final int spaceIndex = orderClause.indexOf(' ');
        if (spaceIndex == -1) {
          fieldName = orderClause;
        } else {
          fieldName = orderClause.substring(0, spaceIndex);
          if ("desc".equalsIgnoreCase(orderClause.substring(spaceIndex + 1))) {
            ascending = false;
          }
        }
        query.addOrderBy(fieldName, ascending);
      }
    }
    applyDefaultSortOrder(query);
  }

  protected void addSearchConditions(final Query query, final Or or, String search) {
    search = '%' + search.trim().toLowerCase() + '%';
    for (final String fieldName : this.searchFieldNames) {
      final ColumnReference column = query.getTable().getColumn(fieldName);
      QueryValue left = column;
      if (column.getDataType() != DataTypes.STRING) {
        left = new Cast(left, "text");
      }
      final Condition condition = query.newCondition(left, Q.ILIKE, search);
      or.addCondition(condition);
    }
  }

  protected Condition alterCondition(final HttpServletRequest request,
    final TableRecordStoreConnection connection, final Query query, final Condition condition) {
    return condition;
  }

  public void applyDefaultSortOrder(final Query query) {
    query.addOrderBy(this.defaultSortOrder);
  }

  public Query applySearchCondition(final Query query, final String search) {
    if (search != null && search.trim().length() > 0) {
      final Or or = new Or();
      addSearchConditions(query, or, search);
      if (!or.isEmpty()) {
        query.and(or);
      }
    }
    return query;
  }

  public boolean canEditField(final String fieldName) {
    if (!this.recordDefinition.hasField(fieldName)) {
      return false;
    }
    if (this.recordDefinition.isIdField(fieldName)) {
      return false;
    }
    return true;
  }

  protected int deleteRecords(final TableRecordStoreConnection connection, final Query query) {
    try (
      Transaction transaction = connection.newTransaction(TransactionOptions.REQUIRED)) {
      return this.recordStore.deleteRecords(query);
    }
  }

  protected void executeUpdate(final TableRecordStoreConnection connection, final String sql,
    final Object... parameters) {
    try (
      Transaction transaction = connection.newTransaction()) {
      this.recordStore.executeUpdate(sql, parameters);
    }
  }

  public Map<QueryValue, Boolean> getDefaultSortOrder() {
    return this.defaultSortOrder;
  }

  protected JdbcConnection getJdbcConnection() {
    return this.recordStore.getJdbcConnection();
  }

  @SuppressWarnings("unchecked")
  protected <R extends Record> R getRecord(final TableRecordStoreConnection connection,
    final Query query) {
    try (
      Transaction transaction = connection.newTransaction(TransactionOptions.REQUIRED)) {
      return (R)this.recordStore.getRecord(query);
    }
  }

  public Record getRecord(final TableRecordStoreConnection connection, final String fieldName,
    final Object value) {
    return newQuery(connection).and(fieldName, value).getRecord();
  }

  public Record getRecordById(final TableRecordStoreConnection connection, final Object id) {
    return getRecord(connection, "id", id);
  }

  public Record getRecordById(final TableRecordStoreConnection connection, final UUID id) {
    return getRecord(connection, "id", id);
  }

  protected long getRecordCount(final TableRecordStoreConnection connection, final Query query) {
    try (
      Transaction transaction = connection.newTransaction(TransactionOptions.REQUIRED)) {
      return this.recordStore.getRecordCount(query);
    }
  }

  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  protected RecordReader getRecordReader(final TableRecordStoreConnection connection,
    final Query query) {
    final Transaction transaction = connection.newTransaction(TransactionOptions.REQUIRED);
    final RecordReader reader = this.recordStore.getRecords(query);
    return new TransactionRecordReader(reader, transaction);
  }

  protected RecordReader getRecordReader(final TableRecordStoreConnection connection,
    final Query query, final Transaction transaction) {
    if (transaction == null) {
      return getRecordReader(connection, query);
    } else {
      return this.recordStore.getRecords(query);
    }
  }

  @SuppressWarnings("unchecked")
  public <R extends RecordStore> R getRecordStore() {
    return (R)this.recordStore;
  }

  public TableReference getTable() {
    return getRecordDefinition();
  }

  public PathName getTablePath() {
    return this.tablePath;
  }

  protected String getTypeName() {
    return this.typeName;
  }

  public boolean hasRecord(final TableRecordStoreConnection connection, final Query query) {
    return query.getRecordCount() > 0;
  }

  public boolean hasRecord(final TableRecordStoreConnection connection, final String fieldName,
    final Object value) {
    final Query query = newQuery(connection).and(fieldName, value);
    return hasRecord(connection, query);
  }

  protected Record insertOrUpdateRecord(final TableRecordStoreConnection connection,
    final Query query, final Supplier<Record> newRecordSupplier,
    final Consumer<Record> updateAction) {
    query.setRecordFactory(ArrayChangeTrackRecord.FACTORY);

    try (
      Transaction transaction = connection.newTransaction(TransactionOptions.REQUIRED)) {
      final ChangeTrackRecord changeTrackRecord = query.getRecord();
      if (changeTrackRecord == null) {
        final Record newRecord = newRecordSupplier.get();
        if (newRecord == null) {
          return null;
        } else {
          return insertRecord(connection, newRecord);
        }
      } else {
        updateAction.accept(changeTrackRecord);
        updateRecordDo(connection, changeTrackRecord);
        return changeTrackRecord.newRecord();
      }
    }
  }

  protected Record insertRecord(final TableRecordStoreConnection connection, final Query query,
    final Supplier<Record> newRecordSupplier) {
    query.setRecordFactory(ArrayChangeTrackRecord.FACTORY);
    final ChangeTrackRecord changeTrackRecord = query.getRecord();
    if (changeTrackRecord == null) {
      final Record newRecord = newRecordSupplier.get();
      if (newRecord == null) {
        return null;
      } else {
        return insertRecord(connection, newRecord);
      }
    } else {
      return changeTrackRecord.newRecord();
    }
  }

  public Record insertRecord(final TableRecordStoreConnection connection, final Record record) {
    try (
      Transaction transaction = connection.newTransaction(TransactionOptions.REQUIRED)) {
      insertRecordBefore(connection, record);
      validateRecord(record);
      this.recordStore.insertRecord(record);
      insertRecordAfter(connection, record);
    }
    return record;
  }

  protected void insertRecordAfter(final TableRecordStoreConnection connection,
    final Record record) {
  }

  protected void insertRecordBefore(final TableRecordStoreConnection connection,
    final Record record) {
  }

  protected boolean isFieldReadonly(final String fieldName) {
    return this.recordDefinition.isIdField(fieldName);
  }

  public void lockTable() {
    this.recordStore.lockTable(this.tablePath);
  }

  public Condition newODataFilter(final String filter) {
    if (Property.hasValue(filter)) {
      final TableReference table = getTable();
      return (Condition)ODataParser.parseFilter(table, filter);
    } else {
      return null;
    }
  }

  public Query newQuery(final TableRecordStoreConnection connection) {
    return new TableRecordStoreQuery(this, connection);
  }

  public Query newQuery(final TableRecordStoreConnection connection,
    final HttpServletRequest request, final int maxSize) {
    final String select = request.getParameter("$select");
    final String filter = request.getParameter("$filter");
    final String search = request.getParameter("$search");
    final String orderBy = request.getParameter("$orderby");
    int skip = 0;
    try {
      final String value = request.getParameter("$skip");
      skip = Integer.parseInt(value);
    } catch (final Exception e) {
    }
    int top = maxSize;
    try {
      final String value = request.getParameter("$top");
      top = Math.min(Integer.parseInt(value), maxSize);
      if (top <= 0) {
        throw new IllegalArgumentException("$top must be > 1: " + top);
      }
    } catch (final Exception e) {
    }

    final Query query = newQuery(connection).selectCsv(select).setOffset(skip).setLimit(top);
    Condition filterCondition = newODataFilter(filter);
    if (filterCondition != null) {
      filterCondition = alterCondition(request, connection, query, filterCondition);
      query.and(filterCondition.clone(null, query.getTable()));
    }
    applySearchCondition(query, search);
    addQueryOrderBy(query, orderBy);
    return query;
  }

  protected void newQueryFilterConditionSearch(final Query query, final String search) {
    applySearchCondition(query, search);
  }

  public Record newRecord() {
    return getRecordDefinition().newRecord();
  }

  public Record newRecord(final MapEx values) {
    if (values == null) {
      return null;
    } else {
      final Record record = newRecord();
      for (final String fieldName : values.keySet()) {
        final Object value = values.getValue(fieldName);
        if (Property.hasValue(value)) {
          record.setValue(fieldName, value);
        }
      }
      return record;
    }
  }

  public Transaction newTransaction() {
    return this.recordStore.newTransaction();
  }

  public UUID newUUID() {
    return UUID.randomUUID();
  }

  public JsonObject schemaToJson() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final JsonList jsonFields = JsonList.array();
    final String idFieldName = recordDefinition.getIdFieldName();
    final JsonObject jsonSchema = JsonObject.hash()
      .addValue("typeName", recordDefinition.getPathName())
      .addValue("title", recordDefinition.getTitle())
      .addValue("idFieldName", idFieldName)
      .addValue("geometryFieldName", recordDefinition.getGeometryFieldName())
      .addValue("fields", jsonFields);
    final GeometryFactory geometryFactory = recordDefinition.getGeometryFactory();
    if (geometryFactory != null) {
      final int coordinateSystemId = geometryFactory.getHorizontalCoordinateSystemId();
      if (coordinateSystemId > 0) {
        jsonSchema.addValue("srid", coordinateSystemId);
      }
    }
    for (final FieldDefinition field : recordDefinition.getFields()) {
      final String fieldName = field.getName();
      final DataType dataType = field.getDataType();
      String dataTypeString = dataType.toString();
      if (dataTypeString.startsWith("List")) {
        dataTypeString = dataTypeString.substring(4) + "[]";
      }
      final JsonObject jsonField = JsonObject.hash()
        .addValue("name", fieldName)
        .addNotEmpty("title", field.getTitle().replace(" Ind", "").replace(" Code", ""))
        .addNotEmpty("description", field.getDescription())
        .addValue("dataType", dataTypeString)
        .addValue("required", field.isRequired());
      if (isFieldReadonly(fieldName)) {
        jsonField.addValue("readonly", true);
      }
      final int length = field.getLength();
      if (length > 0) {
        jsonField.addValue("length", length);
      }
      final int scale = field.getScale();
      if (scale > 0) {
        jsonField.addValue("scale", scale);
      }
      jsonField//
        .addNotEmpty("default", field.getDefaultValue())
        .addNotEmpty("allowedValues", field.getAllowedValues())
        .addNotEmpty("min", field.getMinValue())
        .addNotEmpty("max", field.getMaxValue());
      final CodeTable codeTable = field.getCodeTable();
      if (codeTable != null) {
        jsonField.addNotEmpty("codeTable", codeTable.getName());
      }
      jsonFields.add(jsonField);
    }
    return jsonSchema;
  }

  protected void setDefaultSortOrder(final Collection<String> fieldNames) {
    this.defaultSortOrder.clear();
    for (final String fieldName : fieldNames) {
      addDefaultSortOrder(fieldName);
    }

  }

  public void setDefaultSortOrder(final String... fieldNames) {
    this.defaultSortOrder.clear();
    for (final String fieldName : fieldNames) {
      addDefaultSortOrder(fieldName);
    }
  }

  protected void setDefaultSortOrder(final String fieldName, final boolean ascending) {
    this.defaultSortOrder.clear();
    addDefaultSortOrder(fieldName, ascending);
  }

  protected void setGeneratedFields(final String... fieldNames) {
    if (getRecordDefinition() != null) {
      for (final String fieldName : fieldNames) {
        final JdbcFieldDefinition field = (JdbcFieldDefinition)getRecordDefinition()
          .getField(fieldName);
        if (field != null) {
          field.setGenerated(true);
        }
      }
    }
  }

  protected void setRecordDefinition(final RecordDefinition recordDefinition) {
    this.recordDefinition = recordDefinition;
    if (recordDefinition == null) {
      Logs.error(this, "Table doesn't exist\t" + getTypeName());
    } else {
      setRecordDefinitionPost(recordDefinition);
    }
  }

  protected void setRecordDefinitionPost(final RecordDefinition recordDefinition) {
  }

  protected void setRecordStore(final JdbcRecordStore recordStore) {
    this.recordStore = recordStore;
    final RecordDefinition recordDefinition = this.recordStore.getRecordDefinition(this.tablePath);
    setRecordDefinition(recordDefinition);
  }

  public AbstractTableRecordStore setSearchFieldNames(final Collection<String> searchFieldNames) {
    this.searchFieldNames.addAll(searchFieldNames);
    return this;
  }

  public AbstractTableRecordStore setSearchFieldNames(final String... searchFieldNames) {
    for (final String searchFieldName : searchFieldNames) {
      this.searchFieldNames.add(searchFieldName);
    }
    return this;
  }

  public Record updateRecord(final TableRecordStoreConnection connection, final Identifier id,
    final Consumer<Record> updateAction) {
    final Query query = newQuery(connection)
      .and(Q.equalId(getRecordDefinition().getIdFieldNames(), id));
    return query.updateRecord(updateAction);
  }

  public Record updateRecord(final TableRecordStoreConnection connection, final Identifier id,
    final JsonObject values) {
    return updateRecord(connection, id, record -> record.setValues(values));
  }

  public Record updateRecord(final TableRecordStoreConnection connection, final Object id,
    final Consumer<Record> updateAction) {
    final Query query = newQuery(connection);
    query.and(getRecordDefinition().getIdFieldName(), id);
    return query.updateRecord(updateAction);
  }

  protected Record updateRecord(final TableRecordStoreConnection connection, final Query query,
    final Consumer<Record> updateAction) {
    try (
      Transaction transaction = connection.newTransaction(TransactionOptions.REQUIRED)) {
      query.setRecordFactory(ArrayChangeTrackRecord.FACTORY);
      final ChangeTrackRecord record = query.getRecord();
      if (record == null) {
        return null;
      } else {
        updateAction.accept(record);
        updateRecordDo(connection, record);
        return record.newRecord();
      }
    }
  }

  protected void updateRecordAfter(final TableRecordStoreConnection connection,
    final ChangeTrackRecord record) {
  }

  protected void updateRecordBefore(final TableRecordStoreConnection connection,
    final ChangeTrackRecord record) {
  }

  private void updateRecordDo(final TableRecordStoreConnection connection,
    final ChangeTrackRecord record) {
    if (record.isModified()) {
      updateRecordBefore(connection, record);
      this.recordStore.updateRecord(record);
      updateRecordAfter(connection, record);
    }
  }

  public int updateRecords(final TableRecordStoreConnection connection, final Query query,
    final Consumer<? super ChangeTrackRecord> updateAction) {
    int i = 0;
    final RecordDefinition recordDefinition = getRecordDefinition();
    final RecordStore recordStore = this.recordStore;
    query.setRecordFactory(ArrayChangeTrackRecord.FACTORY);
    try (
      RecordReader reader = getRecordReader(connection, query);
      RecordWriter writer = recordStore.newRecordWriter(recordDefinition)) {
      for (final Record queryRecord : reader) {
        final ChangeTrackRecord record = (ChangeTrackRecord)queryRecord;
        updateAction.accept(record);
        if (record.isModified()) {
          updateRecordBefore(connection, record);
          writer.write(record);
          updateRecordAfter(connection, record);
          i++;
        }
      }
    }
    return i;
  }

  public void validateRecord(final MapEx record) {
    getRecordDefinition().validateRecord(record);
  }

}
