package com.revolsys.record.schema;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.CollectionUtil;
import com.revolsys.collection.list.Lists;
import com.revolsys.collection.map.Maps;
import com.revolsys.collection.set.Sets;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.ClockDirection;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonObjectHash;
import com.revolsys.record.property.RecordDefinitionProperty;
import com.revolsys.record.property.ValueRecordDefinitionProperty;
import com.revolsys.record.query.ColumnReference;

public class RecordDefinitionImpl extends AbstractRecordStoreSchemaElement
  implements RecordDefinition {

  public static void destroy(final RecordDefinitionImpl... recordDefinitionList) {
    for (final RecordDefinitionImpl recordDefinition : recordDefinitionList) {
      recordDefinition.destroy();
    }
  }

  public static RecordDefinitionImpl newRecordDefinition(
    final Map<String, ? extends Object> properties) {
    return new RecordDefinitionImpl(properties);
  }

  private ClockDirection polygonRingDirection = ClockDirection.OGC_SFS_COUNTER_CLOCKWISE;

  private Map<String, CodeTable> codeTableByFieldNameMap = new HashMap<>();

  private String tableAlias;

  private Map<String, Object> defaultValues = new HashMap<>();

  private BoundingBox boundingBox = BoundingBox.empty();

  private CodeTable codeTable;

  private String description;

  private final Map<String, Integer> fieldIdMap = new HashMap<>();

  private final Map<String, FieldDefinition> fieldMap = new HashMap<>();

  private List<String> fieldNames = Collections.emptyList();

  private Set<String> fieldNamesSet = Collections.emptySet();

  private List<FieldDefinition> fields = Collections.emptyList();

  /** The index of the primary geometry field. */
  private int geometryFieldDefinitionIndex = -1;

  private final List<Integer> geometryFieldDefinitionIndexes = new ArrayList<>();

  private final List<Integer> geometryFieldDefinitionIndexesUnmod = Collections
    .unmodifiableList(this.geometryFieldDefinitionIndexes);

  private final List<String> geometryFieldDefinitionNames = new ArrayList<>();

  private final List<String> geometryFieldDefinitionNamesUnmod = Collections
    .unmodifiableList(this.geometryFieldDefinitionNames);

  /** The index of the ID field. */
  private int idFieldDefinitionIndex = -1;

  private final List<Integer> idFieldDefinitionIndexes = new ArrayList<>();

  private final List<Integer> idFieldDefinitionIndexesUnmod = Collections
    .unmodifiableList(this.idFieldDefinitionIndexes);

  private final List<String> idFieldDefinitionNames = new ArrayList<>();

  private final List<FieldDefinition> idFieldDefinitions = new ArrayList<>();

  private final List<FieldDefinition> idFieldDefinitionsUnmod = Collections
    .unmodifiableList(this.idFieldDefinitions);

  private final List<String> internalFieldNames = new ArrayList<>();

  private final List<String> idFieldDefinitionNamesUnmod = Collections
    .unmodifiableList(this.idFieldDefinitionNames);

  private final List<FieldDefinition> internalFields = new ArrayList<>();

  private RecordDefinitionFactory recordDefinitionFactory;

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  private RecordFactory<Record> recordFactory = (RecordFactory)ArrayRecord.FACTORY;

  private final Map<String, Collection<Object>> restrictions = new HashMap<>();

  private final List<RecordDefinition> superClasses = new ArrayList<>();

  private GeometryFactory geometryFactory;

  public RecordDefinitionImpl() {
    super(null, (PathName)null);
  }

  @SuppressWarnings("unchecked")
  public RecordDefinitionImpl(final Map<String, ? extends Object> properties) {
    this(PathName.newPathName(Maps.getString(properties, "path")));
    final List<Object> fields = (List<Object>)properties.get("fields");
    for (final Object object : fields) {
      if (object instanceof FieldDefinition) {
        final FieldDefinition field = (FieldDefinition)object;
        addField(field.clone());
      } else if (object instanceof Map) {
        final Map<String, Object> fieldProperties = (Map<String, Object>)object;
        final FieldDefinition field = FieldDefinition.newFieldDefinition(fieldProperties);
        addField(field);
      }
    }
    final Object geometryFactoryProperty = properties.get("geometryFactory");
    if (geometryFactoryProperty instanceof Map) {
      final Map<String, Object> geometryFactoryDef = (Map<String, Object>)geometryFactoryProperty;
      if (geometryFactoryDef != null) {
        final GeometryFactory geometryFactory = MapObjectFactory.toObject(geometryFactoryDef);
        setGeometryFactory(geometryFactory);
      }
    } else if (geometryFactoryProperty instanceof GeometryFactory) {
      final GeometryFactory geometryFactory = (GeometryFactory)geometryFactoryProperty;
      setGeometryFactory(geometryFactory);
    }
  }

  public RecordDefinitionImpl(final PathName path) {
    super(path);
  }

  public RecordDefinitionImpl(final PathName path, final FieldDefinition... fields) {
    this(path, null, fields);
  }

  public RecordDefinitionImpl(final PathName path, final List<FieldDefinition> fields) {
    this(path, null, fields);
  }

  public RecordDefinitionImpl(final PathName path, final Map<String, Object> properties,
    final FieldDefinition... fields) {
    this(path, properties, Arrays.asList(fields));
  }

  public RecordDefinitionImpl(final PathName path, final Map<String, Object> properties,
    final List<FieldDefinition> fields) {
    super(path);
    for (final FieldDefinition field : fields) {
      addField(field.clone());
    }
    cloneProperties(properties);
  }

  public RecordDefinitionImpl(final RecordDefinition recordDefinition) {
    this(recordDefinition.getPathName(), recordDefinition.getProperties(),
      recordDefinition.getFields());
    setPolygonRingDirection(recordDefinition.getPolygonRingDirection());
    setIdFieldIndex(recordDefinition.getIdFieldIndex());
    this.codeTable = recordDefinition.getCodeTable();
  }

  public RecordDefinitionImpl(final RecordStoreSchema schema) {
    super(schema);
    final RecordStore recordStore = getRecordStore();
    if (recordStore != null) {
      this.recordFactory = recordStore.getRecordFactory();
    }
  }

  public RecordDefinitionImpl(final RecordStoreSchema schema, final PathName pathName) {
    super(schema, pathName);
    final RecordStore recordStore = getRecordStore();
    if (recordStore != null) {
      this.recordFactory = recordStore.getRecordFactory();
    }
  }

  public RecordDefinitionImpl(final RecordStoreSchema schema, final PathName path,
    final Map<String, Object> properties, final List<FieldDefinition> fields) {
    this(schema, path);
    for (final FieldDefinition field : fields) {
      addField(field.clone());
    }
    cloneProperties(properties);
  }

  public RecordDefinitionImpl(final RecordStoreSchema schema,
    final RecordDefinition recordDefinition) {
    this(schema, recordDefinition.getPathName());
    for (final FieldDefinition field : recordDefinition.getFields()) {
      addField(field.clone());
    }
    cloneProperties(recordDefinition.getProperties());
  }

  public RecordDefinitionImpl(final String pathName) {
    this(PathName.newPathName(pathName));
  }

  @Override
  public RecordDefinitionImpl addDefaultValue(final String fieldName, final Object defaultValue) {
    this.defaultValues.put(fieldName, defaultValue);
    final FieldDefinition fieldDefinition = getFieldDefinition(fieldName);
    if (fieldDefinition != null) {
      fieldDefinition.setDefaultValue(defaultValue);
    }
    return this;
  }

  public synchronized void addField(final FieldDefinition field) {
    final int index = this.fieldNames.size();
    final String name = field.getName();
    String lowerName;
    if (name == null) {
      lowerName = null;
    } else {
      lowerName = name.toLowerCase();
    }

    final int fieldIndex = this.internalFields.size();
    this.internalFieldNames.add(name);
    this.fieldNames = Lists.unmodifiable(this.internalFieldNames);
    this.fieldNamesSet = Sets.unmodifiableLinked(this.internalFieldNames);
    this.internalFields.add(field);
    this.fields = Lists.unmodifiable(this.internalFields);
    this.fieldMap.put(name, field);
    this.fieldMap.put(lowerName, field);
    this.fieldIdMap.put(name, fieldIndex);
    this.fieldIdMap.put(lowerName, fieldIndex);
    final DataType dataType = field.getDataType();
    if (dataType == null) {
      Logs.debug(this, field.toString());
    } else {
      final Class<?> dataClass = dataType.getJavaClass();
      if (Geometry.class.isAssignableFrom(dataClass)) {
        this.geometryFieldDefinitionIndexes.add(index);
        this.geometryFieldDefinitionNames.add(name);
        if (this.geometryFieldDefinitionIndex == -1) {
          this.geometryFieldDefinitionIndex = index;
          final GeometryFactory geometryFactory = field.getGeometryFactory();
          if (geometryFactory == null && this.geometryFactory != null) {
            field.setGeometryFactory(this.geometryFactory);
          }
        }
      }
    }
    field.setIndex(index);
    field.setRecordDefinition(this);
    final CodeTable codeTable = field.getCodeTable();
    addFieldCodeTable(name, codeTable);
  }

  /**
   * Adds an field with the given case-sensitive name.
   *
   */
  public FieldDefinition addField(final String fieldName, final DataType type) {
    return addField(fieldName, type, false);
  }

  public FieldDefinition addField(final String name, final DataType type, final boolean required) {
    final FieldDefinition field = new FieldDefinition(name, type, required);
    addField(field);
    return field;
  }

  public FieldDefinition addField(final String name, final DataType type, final int length,
    final boolean required) {
    final FieldDefinition field = new FieldDefinition(name, type, length, required);
    addField(field);
    return field;
  }

  public FieldDefinition addField(final String fieldName, final DataType type, final int length,
    final int scale) {
    final FieldDefinition field = new FieldDefinition(fieldName, type, length, scale, false);
    addField(field);
    return field;
  }

  public FieldDefinition addField(final String name, final DataType type, final int length,
    final int scale, final boolean required) {
    final FieldDefinition field = new FieldDefinition(name, type, length, scale, required);
    addField(field);
    return field;
  }

  public void addFieldCodeTable(final String fieldName, final CodeTable codeTable) {
    if (codeTable != null && fieldName != null) {
      this.codeTableByFieldNameMap.put(fieldName.toUpperCase(), codeTable);
    }
  }

  protected void addIdField(final FieldDefinition fieldDefinition) {
    fieldDefinition.setIdField(true);
    final int index = fieldDefinition.getIndex();
    final String fieldName = fieldDefinition.getName();
    this.idFieldDefinitionIndexes.add(index);
    this.idFieldDefinitionNames.add(fieldName);
    this.idFieldDefinitions.add(fieldDefinition);
  }

  @Override
  public void addProperty(final RecordDefinitionProperty property) {
    final String name = property.getPropertyName();
    addProperty(name, property);
  }

  public void addRestriction(final String fieldPath, final Collection<Object> values) {
    this.restrictions.put(fieldPath, values);
  }

  public void addSuperClass(final RecordDefinition superClass) {
    if (!this.superClasses.contains(superClass)) {
      this.superClasses.add(superClass);
    }
  }

  private void clearIdFields() {
    for (final FieldDefinition fieldDefinition : this.fields) {
      fieldDefinition.setIdField(false);
    }
    this.idFieldDefinitionIndex = -1;
    this.idFieldDefinitionIndexes.clear();
    this.idFieldDefinitionNames.clear();
    this.idFieldDefinitions.clear();
  }

  public void cloneProperties(final Map<String, Object> properties) {
    if (properties != null) {
      for (final Entry<String, Object> property : properties.entrySet()) {
        final String propertyName = property.getKey();
        final Object value = property.getValue();
        if (value instanceof RecordDefinitionProperty) {
          RecordDefinitionProperty recordDefinitionProperty = (RecordDefinitionProperty)value;
          recordDefinitionProperty = recordDefinitionProperty.clone();
          recordDefinitionProperty.setRecordDefinition(this);
          setProperty(propertyName, recordDefinitionProperty);
        } else {
          setProperty(propertyName, value);
        }
      }
    }
  }

  @Override
  public void deleteRecord(final Record record) {
    final RecordStore recordStore = getRecordStore();
    if (recordStore == null) {
      throw new UnsupportedOperationException();
    } else {
      recordStore.deleteRecord(record);
    }
  }

  @Override
  @PreDestroy
  public void destroy() {
    super.close();
    this.fieldIdMap.clear();
    this.fieldMap.clear();
    this.internalFieldNames.clear();
    this.fields = Collections.emptyList();
    this.internalFields.clear();
    this.fieldNames = Collections.emptyList();
    this.fieldNamesSet = Collections.emptySet();
    this.codeTableByFieldNameMap.clear();
    this.recordFactory = null;
    this.recordDefinitionFactory = new RecordDefinitionFactoryImpl();
    this.defaultValues.clear();
    this.description = "";
    this.geometryFieldDefinitionIndex = -1;
    this.geometryFieldDefinitionIndexes.clear();
    this.geometryFieldDefinitionNames.clear();
    this.restrictions.clear();
    this.superClasses.clear();
  }

  @Override
  public BoundingBox getBoundingBox() {
    if (this.boundingBox.isEmpty() && this.geometryFactory != null) {
      return this.geometryFactory.getAreaBoundingBox();
    } else {
      return this.boundingBox;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <CT extends CodeTable> CT getCodeTable() {
    return (CT)this.codeTable;
  }

  @Override
  public CodeTable getCodeTableByFieldName(final CharSequence fieldName) {
    if (fieldName == null) {
      return null;
    } else {
      final RecordStore recordStore = getRecordStore();
      CodeTable codeTable;
      final FieldDefinition fieldDefinition = getField(fieldName);
      if (fieldDefinition != null) {
        codeTable = fieldDefinition.getCodeTable();
        if (codeTable != null) {
          return codeTable;
        }
      }
      codeTable = this.codeTableByFieldNameMap.get(fieldName.toString().toUpperCase());
      if (codeTable == null && recordStore != null) {
        codeTable = recordStore.getCodeTableByFieldName(fieldName);
      }
      if (codeTable instanceof RecordDefinitionProxy) {
        final RecordDefinitionProxy proxy = (RecordDefinitionProxy)codeTable;
        if (proxy.getRecordDefinition() == this) {
          return null;
        }
      }
      if (fieldDefinition != null && codeTable != null) {
        fieldDefinition.setCodeTable(codeTable);
      }
      return codeTable;
    }
  }

  @Override
  public ColumnReference getColumn(final CharSequence name) {
    if (name == null) {
      throw new IllegalArgumentException("Column name must not be null");
    } else {
      final String nameString = name.toString();
      FieldDefinition field = this.fieldMap.get(nameString);
      if (field == null) {
        final String lowerName = nameString.toLowerCase();
        field = this.fieldMap.get(lowerName);
        if (field == null) {
          throw new IllegalArgumentException("Column not found: " + getName() + "." + nameString);
        }
      }
      return field;
    }
  }

  @Override
  public String getDbTableName() {
    return getName();
  }

  @Override
  public Object getDefaultValue(final String fieldName) {
    return this.defaultValues.get(fieldName);
  }

  @Override
  public Map<String, Object> getDefaultValues() {
    return this.defaultValues;
  }

  public String getDescription() {
    return this.description;
  }

  @Override
  public FieldDefinition getField(final CharSequence name) {
    if (name == null) {
      return null;
    } else {
      final String nameString = name.toString();
      FieldDefinition field = this.fieldMap.get(nameString);
      if (field == null) {
        final String lowerName = nameString.toLowerCase();
        field = this.fieldMap.get(lowerName);
      }
      return field;
    }
  }

  @Override
  public FieldDefinition getField(final int i) {
    if (i >= 0 && i < this.fields.size()) {
      return this.fields.get(i);
    } else {
      return null;
    }
  }

  @Override
  public Class<?> getFieldClass(final CharSequence name) {
    final DataType dataType = getFieldType(name);
    if (dataType == null) {
      return Object.class;
    } else {
      return dataType.getJavaClass();
    }
  }

  @Override
  public Class<?> getFieldClass(final int i) {
    final DataType dataType = getFieldType(i);
    if (dataType == null) {
      return Object.class;
    } else {
      return dataType.getJavaClass();
    }
  }

  @Override
  public int getFieldCount() {
    return this.fields.size();
  }

  @Override
  public int getFieldIndex(final String name) {
    if (name == null) {
      return -1;
    } else {
      final Integer fieldId = this.fieldIdMap.get(name);
      if (fieldId == null) {
        final String lowerName = name.toLowerCase();
        return this.fieldIdMap.getOrDefault(lowerName, -1);
      }
      return fieldId;
    }
  }

  @Override
  public int getFieldLength(final int i) {
    try {
      final FieldDefinition field = this.fields.get(i);
      return field.getLength();
    } catch (final ArrayIndexOutOfBoundsException e) {
      throw e;
    }
  }

  @Override
  public int getFieldLength(final String name) {
    final FieldDefinition field = getField(name);
    if (field == null) {
      return 0;
    } else {
      return field.getLength();
    }
  }

  @Override
  public String getFieldName(final int i) {
    if (this.fields != null && i >= 0 && i < this.fields.size()) {
      final FieldDefinition field = this.fields.get(i);
      return field.getName();
    }
    return null;
  }

  @Override
  public List<String> getFieldNames() {
    return this.fieldNames;
  }

  @Override
  public Set<String> getFieldNamesSet() {
    return this.fieldNamesSet;
  }

  @Override
  public List<FieldDefinition> getFields() {
    return this.fields;
  }

  @Override
  public int getFieldScale(final int i) {
    final FieldDefinition field = this.fields.get(i);
    return field.getScale();
  }

  @Override
  public List<String> getFieldTitles() {
    final List<String> titles = new ArrayList<>();
    for (final FieldDefinition field : getFields()) {
      titles.add(field.getTitle());
    }
    return titles;
  }

  @Override
  public DataType getFieldType(final CharSequence name) {
    final int index = getFieldIndex(name);
    if (index == -1) {
      return null;
    } else {
      return getFieldType(index);
    }
  }

  @Override
  public DataType getFieldType(final int i) {
    final FieldDefinition field = this.fields.get(i);
    return field.getDataType();
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    final FieldDefinition geometryFieldDefinition = getGeometryField();
    if (geometryFieldDefinition == null) {
      return null;
    } else {
      final GeometryFactory geometryFactory = geometryFieldDefinition.getGeometryFactory();
      if (geometryFactory == null) {
        return this.geometryFactory;
      }
      return geometryFactory;
    }
  }

  @Override
  public FieldDefinition getGeometryField() {
    if (this.geometryFieldDefinitionIndex == -1
      && this.geometryFieldDefinitionIndex < this.fields.size()) {
      return null;
    } else {
      return this.fields.get(this.geometryFieldDefinitionIndex);
    }
  }

  @Override
  public int getGeometryFieldIndex() {
    return this.geometryFieldDefinitionIndex;
  }

  @Override
  public List<Integer> getGeometryFieldIndexes() {
    return this.geometryFieldDefinitionIndexesUnmod;
  }

  @Override
  public String getGeometryFieldName() {
    return getFieldName(this.geometryFieldDefinitionIndex);
  }

  @Override
  public List<String> getGeometryFieldNames() {
    return this.geometryFieldDefinitionNamesUnmod;
  }

  @Override
  public String getIconName() {
    final FieldDefinition geometryField = getGeometryField();
    if (geometryField == null) {
      return "table";
    } else {
      final DataType dataType = geometryField.getDataType();
      if (dataType.equals(GeometryDataTypes.GEOMETRY_COLLECTION)) {
        return "table_geometry";
      } else {
        return "table_" + dataType.toString().toLowerCase();
      }
    }
  }

  @Override
  public FieldDefinition getIdField() {
    if (this.idFieldDefinitionIndex >= 0) {
      return this.fields.get(this.idFieldDefinitionIndex);
    } else {
      return null;
    }
  }

  @Override
  public int getIdFieldCount() {
    return getIdFieldIndexes().size();
  }

  @Override
  public int getIdFieldIndex() {
    return this.idFieldDefinitionIndex;
  }

  @Override
  public List<Integer> getIdFieldIndexes() {
    return this.idFieldDefinitionIndexesUnmod;
  }

  @Override
  public String getIdFieldName() {
    return getFieldName(this.idFieldDefinitionIndex);
  }

  @Override
  public List<String> getIdFieldNames() {
    return this.idFieldDefinitionNamesUnmod;
  }

  @Override
  public List<FieldDefinition> getIdFields() {
    return this.idFieldDefinitionsUnmod;
  }

  @Override
  public ClockDirection getPolygonRingDirection() {
    return this.polygonRingDirection;
  }

  @Override
  public RecordDefinitionFactory getRecordDefinitionFactory() {
    if (this.recordDefinitionFactory == null) {
      final RecordStore recordStore = getRecordStore();
      return recordStore;
    } else {
      return this.recordDefinitionFactory;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends Record> RecordFactory<R> getRecordFactory() {
    return (RecordFactory<R>)this.recordFactory;
  }

  public Map<String, Collection<Object>> getRestrictions() {
    return this.restrictions;
  }

  @Override
  public String getTableAlias() {
    return this.tableAlias;
  }

  @Override
  public PathName getTablePath() {
    return getPathName();
  }

  @Override
  public boolean hasColumn(final CharSequence name) {
    return hasField(name);
  }

  @Override
  public boolean hasField(final CharSequence name) {
    if (name == null) {
      return false;
    } else {
      final String nameString = name.toString();
      final boolean hasField = this.fieldMap.containsKey(nameString);
      if (hasField) {
        return true;
      } else {
        final String lowerName = nameString.toLowerCase();
        return this.fieldMap.containsKey(lowerName);
      }
    }
  }

  @Override
  public boolean hasGeometryField() {
    return this.geometryFieldDefinitionIndex != -1;
  }

  @Override
  public boolean hasIdField() {
    return !this.idFieldDefinitions.isEmpty();
  }

  @Override
  public boolean isFieldRequired(final CharSequence name) {
    final FieldDefinition field = getField(name);
    if (field == null) {
      return false;
    } else {
      return field.isRequired();
    }
  }

  @Override
  public boolean isFieldRequired(final int i) {
    final FieldDefinition field = getField(i);
    return field.isRequired();
  }

  @Override
  public boolean isIdField(final int fieldIndex) {
    return this.idFieldDefinitionIndexes.contains(fieldIndex);
  }

  @Override
  public boolean isIdField(final String fieldName) {
    return this.idFieldDefinitionNames.contains(fieldName);
  }

  @Override
  public boolean isInstanceOf(final RecordDefinition classDefinition) {
    if (classDefinition == null) {
      return false;
    }
    if (equals(classDefinition)) {
      return true;
    }
    for (final RecordDefinition superClass : this.superClasses) {
      if (superClass.isInstanceOf(classDefinition)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Record newRecord() {
    final RecordFactory<Record> recordFactory = this.recordFactory;
    if (recordFactory == null) {
      return new ArrayRecord(this);
    } else {
      return recordFactory.newRecord(this);
    }
  }

  private void readObject(final ObjectInputStream ois) throws ClassNotFoundException, IOException {
    ois.defaultReadObject();
  }

  public RecordDefinitionImpl rename(final String path) {
    final RecordDefinitionImpl clone = new RecordDefinitionImpl(PathName.newPathName(path),
      getProperties(), this.fields);
    clone.setIdFieldIndex(this.idFieldDefinitionIndex);
    clone.setProperties(getProperties());
    return clone;
  }

  public void replaceField(final FieldDefinition field, final FieldDefinition newFieldDefinition) {
    final String name = field.getName();
    final String lowerName = name.toLowerCase();
    final String newName = newFieldDefinition.getName();
    if (this.fields.contains(field) && name.equals(newName)) {
      final int index = field.getIndex();
      this.internalFields.set(index, newFieldDefinition);
      this.fields = Lists.unmodifiable(this.internalFields);
      this.fieldMap.put(name, newFieldDefinition);
      this.fieldMap.put(lowerName, newFieldDefinition);
      newFieldDefinition.setIndex(index);
    } else {
      addField(newFieldDefinition);
    }
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  public void setCodeTable(final CodeTable codeTable) {
    this.codeTable = codeTable;
  }

  public void setCodeTableByFieldNameMap(final Map<String, CodeTable> codeTableByFieldNameMap) {
    this.codeTableByFieldNameMap = codeTableByFieldNameMap;
  }

  @Override
  public void setDefaultValues(final Map<String, ? extends Object> defaultValues) {
    this.defaultValues = Maps.newHash(defaultValues);
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  @Override
  public void setGeometryFactory(GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      geometryFactory = GeometryFactory.DEFAULT_2D;
    }
    final FieldDefinition geometryFieldDefinition = getGeometryField();
    if (geometryFieldDefinition == null) {
      this.geometryFactory = geometryFactory;
    } else {
      geometryFieldDefinition.setGeometryFactory(geometryFactory);
    }
  }

  /**
   * @param geometryFieldDefinitionIndex the geometryFieldDefinitionIndex to set
   */
  public void setGeometryFieldIndex(final int geometryFieldDefinitionIndex) {
    this.geometryFieldDefinitionIndex = geometryFieldDefinitionIndex;
  }

  public void setGeometryFieldName(final String name) {
    final int id = getFieldIndex(name);
    setGeometryFieldIndex(id);
  }

  /**
   * @param idFieldDefinitionIndex the idFieldDefinitionIndex to set
   */
  public void setIdFieldIndex(final int idFieldDefinitionIndex) {
    clearIdFields();
    if (idFieldDefinitionIndex != -1) {
      final FieldDefinition fieldDefinition = getField(idFieldDefinitionIndex);
      if (fieldDefinition == null) {
        throw new ArrayIndexOutOfBoundsException(
          "Cannot set ID " + getPath() + "[" + idFieldDefinitionIndex + "] does not exist");
      } else {
        this.idFieldDefinitionIndex = idFieldDefinitionIndex;
        addIdField(fieldDefinition);
      }
    }
  }

  public void setIdFieldName(final String name) {
    final int id = getFieldIndex(name);
    setIdFieldIndex(id);
  }

  public void setIdFieldNames(final Collection<String> names) {
    clearIdFields();
    if (names != null) {
      if (names.size() == 1) {
        final String name = CollectionUtil.get(names, 0);
        setIdFieldName(name);
      } else {
        for (final String name : names) {
          final FieldDefinition fieldDefinition = getField(name);
          if (fieldDefinition == null) {
            throw new IllegalArgumentException(
              "Cannot set ID " + getPath() + "." + name + " does not exist");
          } else {
            addIdField(fieldDefinition);
          }
        }
      }
    }
  }

  public void setIdFieldNames(final String... names) {
    setIdFieldNames(Arrays.asList(names));
  }

  public void setPolygonRingDirection(final ClockDirection polygonRingDirection) {
    this.polygonRingDirection = polygonRingDirection;
  }

  @Override
  public void setProperties(final Map<String, ? extends Object> properties) {
    if (properties != null) {
      for (final Entry<String, ? extends Object> entry : properties.entrySet()) {
        final String key = entry.getKey();
        final Object value = entry.getValue();
        if (value instanceof ValueRecordDefinitionProperty) {
          final ValueRecordDefinitionProperty valueProperty = (ValueRecordDefinitionProperty)value;
          final String propertyName = valueProperty.getPropertyName();
          final Object propertyValue = valueProperty.getValue();
          setProperty(propertyName, propertyValue);
        }
        if (value instanceof RecordDefinitionProperty) {
          final RecordDefinitionProperty property = (RecordDefinitionProperty)value;
          final RecordDefinitionProperty clonedProperty = property.clone();
          clonedProperty.setRecordDefinition(this);
        } else {
          setProperty(key, value);
        }
      }
    }

  }

  @Override
  public void setProperty(final String name, Object value) {
    if (value instanceof ValueRecordDefinitionProperty) {
      final ValueRecordDefinitionProperty valueHolder = (ValueRecordDefinitionProperty)value;
      value = valueHolder.getValue();
    }
    super.setProperty(name, value);
  }

  public void setRecordDefinitionFactory(final RecordDefinitionFactory recordDefinitionFactory) {
    this.recordDefinitionFactory = recordDefinitionFactory;
  }

  @Override
  public void setTableAlias(final String tableAlias) {
    this.tableAlias = tableAlias;
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = new JsonObjectHash();
    addTypeToMap(map, "recordDefinition");
    final String path = getPath();
    map.put("path", path);
    final ClockDirection polygonRingDirection = getPolygonRingDirection();
    addToMap(map, "polygonRingDirection", polygonRingDirection, null);
    final GeometryFactory geometryFactory = getGeometryFactory();
    addToMap(map, "geometryFactory", geometryFactory, null);
    final List<FieldDefinition> fields = getFields();
    addToMap(map, "fields", fields);
    return map;
  }

}
