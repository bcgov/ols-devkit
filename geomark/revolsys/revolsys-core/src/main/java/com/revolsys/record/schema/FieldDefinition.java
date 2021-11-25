package com.revolsys.record.schema;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jeometry.common.compare.NumericComparator;
import org.jeometry.common.data.identifier.Code;
import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypeProxy;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.beans.ObjectPropertyException;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.operation.valid.IsValidOp;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonObjectHash;
import com.revolsys.record.query.ColumnReference;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.TableReference;
import com.revolsys.util.BaseCloneable;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

/**
 * The FieldDefinition class defines the name, type and other properties about each
 * field on a {@link Record} in the {@link RecordDefinition}.
 *
 * @see Record
 * @see RecordDefinition
 */
public class FieldDefinition extends BaseObjectWithProperties implements CharSequence, Cloneable,
  MapSerializer, RecordDefinitionProxy, DataTypeProxy, ColumnReference {
  public static FieldDefinition newFieldDefinition(final Map<String, ? extends Object> config) {
    return new FieldDefinition(config);
  }

  private final Map<Object, Object> allowedValues = new LinkedHashMap<>();

  private CodeTable codeTable;

  private Object defaultValue;

  private GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;

  /** The description of the field. */
  private String description;

  private int index;

  private boolean idField;

  /** The maximum length of an field value. */
  private int length;

  private Object maxValue;

  private Object minValue;

  /** The name of the field. */
  private String name;

  private Reference<RecordDefinition> recordDefinition;

  /** The flag indicating if a value is required for the field. */
  private boolean required;

  /** The maximum number of decimal places. */
  private int scale;

  private String title;

  /** The data type of the field value. */
  private DataType type = DataTypes.STRING;

  public FieldDefinition() {
  }

  public FieldDefinition(final FieldDefinition field) {
    this.allowedValues.putAll(field.getAllowedValues());
    this.defaultValue = field.getDefaultValue();
    this.description = field.getDescription();
    setLength(field.getLength());
    this.maxValue = field.getMaxValue();
    this.minValue = field.getMinValue();
    this.name = field.getName();
    this.required = field.isRequired();
    this.scale = field.getScale();
    setTitle(field.getTitle());
    this.type = field.getDataType();
    this.codeTable = field.getCodeTable();
    this.geometryFactory = field.getGeometryFactory();
    this.recordDefinition = field.recordDefinition;

    final Map<String, Object> properties = field.getProperties();
    setProperties(properties);
  }

  public FieldDefinition(final int index) {
    this.index = index;
  }

  public FieldDefinition(final Map<String, ? extends Object> properties) {
    this.name = Maps.getString(properties, "name");
    final String title = Maps.getString(properties, "title");
    setTitle(title);
    this.description = Maps.getString(properties, "description");
    this.type = DataTypes.getDataType(Maps.getString(properties, "dataType"));
    this.required = Maps.getBool(properties, "required");
    setLength(Maps.getInteger(properties, "length", 0));
    this.scale = Maps.getInteger(properties, "scale", 0);
    this.minValue = properties.get("minValue");
    final DataType dataType = this.type;
    if (this.minValue == null) {
      this.minValue = dataType.getMinValue();
    } else {
      this.minValue = dataType.toObject(this.minValue);
    }
    if (this.maxValue == null) {
      this.maxValue = dataType.getMaxValue();
    } else {
      this.maxValue = dataType.toObject(this.maxValue);
    }
  }

  /**
   * Construct a new field.
   *
   * @param name The name of the field.
   * @param type The data type of the field value.
   * @param required The flag indicating if a value is required for the
   *          field.
   */
  public FieldDefinition(final String name, final DataType type, final boolean required) {
    this(name, type, 0, 0, required, null, null);
  }

  /**
   * Construct a new field.
   *
   * @param name The name of the field.
   * @param type The data type of the field value.
   * @param required The flag indicating if a value is required for the
   *          field.
   * @param properties The meta data properties about the field.
   */
  public FieldDefinition(final String name, final DataType type, final boolean required,
    final Map<String, Object> properties) {
    this(name, type, 0, 0, required, properties);
  }

  public FieldDefinition(final String name, final DataType dataType, final boolean required,
    final String description) {
    this(name, dataType, 0, 0, required, description, null);
  }

  /**
   * Construct a new field.
   *
   * @param name The name of the field.
   * @param type The data type of the field value.
   * @param length The maximum length of an field value, 0 for no maximum.
   * @param required The flag indicating if a value is required for the
   *          field.
   */
  public FieldDefinition(final String name, final DataType type, final int length,
    final boolean required) {
    this(name, type, length, 0, required, null, null);
  }

  /**
   * Construct a new field.
   *
   * @param name The name of the field.
   * @param type The data type of the field value.
   * @param length The maximum length of an field value, 0 for no maximum.
   * @param scale The maximum number of decimal places.
   * @param required The flag indicating if a value is required for the
   *          field.
   * @param properties The meta data properties about the field.
   */
  public FieldDefinition(final String name, final DataType type, final int length,
    final boolean required, final Map<String, Object> properties) {
    this(name, type, length, 0, required, properties);
  }

  public FieldDefinition(final String name, final DataType type, final int length,
    final boolean required, final String description) {
    this(name, type, length, 0, required, description, null);
  }

  /**
   * Construct a new field.
   *
   * @param name The name of the field.
   * @param type The data type of the field value.
   * @param required The flag indicating if a value is required for the
   *          field.
   * @param properties The meta data properties about the field.
   */
  public FieldDefinition(final String name, final DataType type, final Integer length,
    final Integer scale, final Boolean required) {
    this(name, type, length, scale, required, "");
  }

  /**
   * Construct a new field.
   *
   * @param name The name of the field.
   * @param type The data type of the field value.
   * @param length The maximum length of an field value, 0 for no maximum.
   * @param scale The maximum number of decimal places.
   * @param required The flag indicating if a value is required for the
   *          field.
   * @param properties The meta data properties about the field.
   */
  public FieldDefinition(final String name, final DataType type, final Integer length,
    final Integer scale, final Boolean required, final Map<String, Object> properties) {
    this(name, type, length, scale, required, null, properties);

  }

  /**
   * Construct a new field.
   *
   * @param name The name of the field.
   * @param type The data type of the field value.
   * @param length The maximum length of an field value, 0 for no maximum.
   * @param scale The maximum number of decimal places.
   * @param required The flag indicating if a value is required for the
   *          field.
   * @param properties The meta data properties about the field.
   */
  public FieldDefinition(final String name, final DataType type, final Integer length,
    final Integer scale, final Boolean required, final String description) {
    setName(name);
    this.description = description;
    this.type = type;
    if (required != null) {
      this.required = required;
    }
    if (length != null) {
      setLength(length);
    }
    if (scale != null) {
      this.scale = scale;
    }
    this.description = description;
    this.minValue = type.getMinValue();
    this.maxValue = type.getMaxValue();
  }

  /**
   * Construct a new field.
   *
   * @param name The name of the field.
   * @param type The data type of the field value.
   * @param length The maximum length of an field value, 0 for no maximum.
   * @param scale The maximum number of decimal places.
   * @param required The flag indicating if a value is required for the
   *          field.
   * @param properties The meta data properties about the field.
   */
  public FieldDefinition(final String name, DataType type, final Integer length,
    final Integer scale, final Boolean required, final String description,
    final Map<String, Object> properties) {
    setName(name);
    if (type == null) {
      type = DataTypes.OBJECT;
    }
    this.type = type;
    if (required != null) {
      this.required = required;
    }
    if (length != null) {
      setLength(length);
    }
    if (scale != null) {
      this.scale = scale;
    }
    this.description = description;
    this.minValue = type.getMinValue();
    this.maxValue = type.getMaxValue();
    setProperties(properties);
  }

  public void addAllowedValue(final Object value, final Object text) {
    this.allowedValues.put(value, text);
  }

  public void appendColumnName(final Appendable sql) {
    try {
      sql.append(this.name);
    } catch (final IOException e) {
      Exceptions.throwUncheckedException(e);
    }
  }

  public void appendColumnName(final Appendable sql, final boolean quoteName) {
    try {
      if (quoteName) {
        sql.append('"');
      }
      sql.append(this.name);
      if (quoteName) {
        sql.append('"');
      }
    } catch (final IOException e) {
      Exceptions.throwUncheckedException(e);
    }
  }

  public void appendColumnName(final Appendable sql, final String tablePrefix) {
    try {
      if (tablePrefix != null) {
        sql.append(tablePrefix);
        sql.append(".");
      }
      appendColumnName(sql);
    } catch (final IOException e) {
      Exceptions.throwUncheckedException(e);
    }
  }

  @Override
  public void appendDefaultSelect(final Query query, final RecordStore recordStore,
    final Appendable sql) {
    appendName(sql);
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder sql) {
    appendName(sql);
  }

  @Override
  public void appendName(final Appendable sql) {
    final String tableAlias = getTableAlias();
    appendColumnName(sql, tableAlias);
  }

  public void appendType(final StringBuilder string) {
    string.append(this.type);
    if (this.length > 0) {
      string.append('(');
      string.append(this.length);
      if (this.scale > 0) {
        string.append(',');
        string.append(this.scale);
      }
      string.append(')');
    }
  }

  @Override
  public char charAt(final int index) {
    return this.name.charAt(index);
  }

  @Override
  public FieldDefinition clone() {
    return new FieldDefinition(this);
  }

  @Override
  public ColumnReference clone(final TableReference oldTable, final TableReference newTable) {
    if (oldTable != newTable) {
      if (oldTable == getRecordDefinition()) {
        return newTable.getColumn(this.name);
      }
    }
    return this;
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof FieldDefinition) {
      final FieldDefinition fieldDefinition = (FieldDefinition)object;
      final String name2 = fieldDefinition.getName();
      return this.name.equals(name2);
    } else if (object instanceof String) {
      final String name2 = (String)object;
      return this.name.equals(name2);
    } else {
      return false;
    }
  }

  public Map<Object, Object> getAllowedValues() {
    return this.allowedValues;
  }

  public CodeTable getCodeTable() {
    return this.codeTable;
  }

  @SuppressWarnings("unchecked")
  public <V> V getCodeValue(final Object value) {
    if (value != null && this.codeTable != null) {
      final V codeValue = this.codeTable.getValue(value);
      if (codeValue != null) {
        return codeValue;
      }
    }
    return (V)value;
  }

  /**
   * Get the data type of the field value.
   *
   * @return The data type of the field value.
   */
  @Override
  public DataType getDataType() {
    return this.type;
  }

  @SuppressWarnings("unchecked")
  public <T> T getDefaultValue() {
    return (T)this.defaultValue;
  }

  public String getDescription() {
    return this.description;
  }

  @Override
  public FieldDefinition getFieldDefinition() {
    return this;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public int getIndex() {
    return this.index;
  }

  /**
   * Get the maximum length of the field value. The length 0 should be used
   * if there is no maximum.
   *
   * @return The maximum length of an field value.
   */
  public int getLength() {
    return this.length;
  }

  public int getMaxStringLength() {
    int length = this.length;
    if (this.scale > 0) {
      length += 1;
      length += this.scale;
    }

    if (Number.class.isAssignableFrom(this.type.getJavaClass())) {
      length += 1;
    } else if (DataTypes.SQL_DATE.equals(this.type)) {
      return 10;
    }
    return length;
  }

  @SuppressWarnings("unchecked")
  public <V> V getMaxValue() {
    return (V)this.maxValue;
  }

  @SuppressWarnings("unchecked")
  public <V> V getMinValue() {
    return (V)this.minValue;
  }

  /**
   * Get the name of the field.
   *
   * @return The name of the field.
   */
  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    if (this.recordDefinition == null) {
      return null;
    } else {
      return this.recordDefinition.get();
    }
  }

  /**
   * Get the maximum number of decimal places of the field value.
   *
   * @return The maximum number of decimal places.
   */
  public int getScale() {
    return this.scale;
  }

  public String getSimpleType() {
    final StringBuilder string = new StringBuilder();
    String typeName;
    if (Number.class.isAssignableFrom(getTypeClass())) {
      typeName = "NUMBER";
    } else if (CharSequence.class.isAssignableFrom(getTypeClass())) {
      typeName = "CHARACTER";
    } else {
      typeName = this.type.getName().toUpperCase();
    }
    string.append(typeName);
    if (this.length > 0) {
      string.append('(');
      string.append(this.length);
      if (this.scale > 0) {
        string.append(',');
        string.append(this.scale);
      }
      string.append(')');
    }
    return string.toString();
  }

  @Override
  public String getStringValue(final MapEx record) {
    final Object value = getValue(record);
    return toString(value);
  }

  @Override
  public RecordDefinition getTable() {
    return getRecordDefinition();
  }

  public String getTableAlias() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return "t";
    } else {
      return recordDefinition.getTableAlias();
    }
  }

  public String getTitle() {
    return this.title;
  }

  /**
   * Get the data type class of the field value.
   *
   * @return The data type of the field value.
   */
  public Class<?> getTypeClass() {
    if (this.type == null) {
      return Object.class;
    } else {
      return this.type.getJavaClass();
    }
  }

  /**
   * Get the data type of the field value.
   *
   * @return The data type of the field value.
   */
  public String getTypeDescription() {
    final StringBuilder typeDescription = new StringBuilder();
    appendType(typeDescription);
    return typeDescription.toString();
  }

  public boolean hasCodeTable() {
    return this.codeTable != null;
  }

  /**
   * Return the hash code of the field.
   *
   * @return The hash code.
   */
  @Override
  public int hashCode() {
    return this.name.hashCode();
  }

  public boolean isCodeTableReady() {
    if (this.codeTable == null) {
      return true;
    } else {
      return !this.codeTable.isLoadAll() || this.codeTable.isLoaded();
    }
  }

  public boolean isGenerated() {
    return false;
  }

  public boolean isIdField() {
    return this.idField;
  }

  /**
   * Get the flag indicating if a value is required for the field.
   *
   * @return True if a value is required, false otherwise.
   */
  public boolean isRequired() {
    return this.required;
  }

  public boolean isSortable() {
    return true;
  }

  public boolean isValid(final Object value) {
    try {
      validate(value);
      return true;
    } catch (final Throwable e) {
      return false;
    }

  }

  @Override
  public int length() {
    return this.name.length();
  }

  public FieldDefinition setAllowedValues(final Collection<?> allowedValues) {
    for (final Object allowedValue : allowedValues) {
      final Object fieldValue = toFieldValue(allowedValue);
      this.allowedValues.put(fieldValue, fieldValue);
    }
    return this;
  }

  public FieldDefinition setAllowedValues(final Map<?, ?> allowedValues) {
    for (final Entry<?, ?> entry : allowedValues.entrySet()) {
      final Object allowedValue = entry.getKey();
      final Object description = entry.getValue();
      final Object fieldValue = toFieldValue(allowedValue);
      this.allowedValues.put(fieldValue, description);
    }
    return this;
  }

  public FieldDefinition setCodeTable(final CodeTable codeTable) {
    this.codeTable = codeTable;
    return this;
  }

  public FieldDefinition setDefaultValue(final Object defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  public FieldDefinition setDescription(final String description) {
    this.description = description;
    return this;
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public void setIdField(final boolean idField) {
    this.idField = idField;
  }

  void setIndex(final int index) {
    this.index = index;
  }

  public FieldDefinition setLength(final int length) {
    if (length == 0 || length == Integer.MAX_VALUE) {
      this.length = 0;
    } else {
      this.length = length;
    }
    return this;
  }

  public FieldDefinition setMaxValue(final Object maxValue) {
    this.maxValue = maxValue;
    return this;
  }

  public FieldDefinition setMinValue(final Object minValue) {
    this.minValue = minValue;
    return this;
  }

  public FieldDefinition setName(final String name) {
    this.name = name;
    String title = getTitle();
    if (!Property.hasValue(title)) {
      title = CaseConverter.toCapitalizedWords(name);
      setTitle(title);
    }
    return this;
  }

  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    this.recordDefinition = new WeakReference<>(recordDefinition);
  }

  public FieldDefinition setRequired(final boolean required) {
    this.required = required;
    return this;
  }

  public FieldDefinition setScale(final int scale) {
    this.scale = scale;
    return this;
  }

  public FieldDefinition setTitle(final String title) {
    if (Property.hasValue(title)) {
      this.title = title;
    } else {
      final String name = getName();
      this.title = CaseConverter.toCapitalizedWords(name);
    }
    return this;
  }

  public FieldDefinition setType(final DataType type) {
    this.type = type;
    return this;
  }

  public void setValue(final Record record, Object value) {
    if (record != null) {
      final int index = getIndex();
      value = toFieldValue(value);
      record.setValue(index, value);
    }
  }

  public void setValueClone(final Record record, Object value) {
    if (record != null) {
      final int index = getIndex();
      value = toFieldValue(value);
      value = BaseCloneable.clone(value);
      record.setValue(index, value);
    }
  }

  @Override
  public CharSequence subSequence(final int beginIndex, final int endIndex) {
    return this.name.subSequence(beginIndex, endIndex);
  }

  public String toCodeString(final Object value) {
    if (value == null) {
      return null;
    } else if (this.codeTable == null) {
      if (value instanceof String) {
        final String string = (String)value;
        if (!Property.hasValue(string)) {
          return null;
        }
      }
      return this.type.toString(value);
    } else {
      final List<Object> values = this.codeTable.getValues(Identifier.newIdentifier(value));
      if (values == null || values.isEmpty()) {
        return this.type.toString(value);
      } else if (values.size() == 1) {
        final Object codeValue = values.get(0);
        if (codeValue instanceof Code) {
          return ((Code)codeValue).getDescription();
        } else if (codeValue instanceof String) {
          final String string = (String)codeValue;
          if (Property.hasValue(string)) {
            return string;
          } else {
            return null;
          }
        } else {
          return DataTypes.toString(codeValue);
        }
      } else {
        return Strings.toString(values);
      }
    }
  }

  @Override
  public <V> V toColumnTypeException(final Object value) {
    if (value == null) {
      return null;
    } else {
      try {
        if (value instanceof String) {
          final String string = (String)value;
          if (!Property.hasValue(string)) {
            return null;
          }
        }
        final V fieldValue = this.type.toObject(value);
        return fieldValue;
      } catch (final IllegalArgumentException e) {
        throw e;
      } catch (final Throwable e) {
        throw new IllegalArgumentException(
          getName() + "='" + value + "' is not a valid " + getDataType().getValidationName(), e);
      }
    }
  }

  /**
   * Convert the object to a value that is valid for the field. If the value can't be converted then
   * the original value will be returned. This can result in invalid values in the record but those can be picked up
   * with validation. Otherwise invalid values would silently be removed.
   *
   * @param value
   * @return
   */
  @Override
  @SuppressWarnings("unchecked")
  public <V> V toFieldValue(final Object value) {
    try {
      return toFieldValueException(value);
    } catch (final Throwable e) {
      return (V)value;
    }
  }

  /**
   * Convert the object to a value that is valid for the field. If the value can't be converted then
   * the original value will be returned. This can result in invalid values in the record but those can be picked up
   * with validation. Otherwise invalid values would silently be removed.
   *
   * @param value
   * @return
   */
  @SuppressWarnings("unchecked")
  public <V> V toFieldValue(final RecordState state, final Object value) {
    try {
      return toFieldValueException(state, value);
    } catch (final Throwable e) {
      return (V)value;
    }
  }

  @Override
  public <V> V toFieldValueException(final Object value) {
    if (value == null) {
      return null;
    } else {
      try {
        if (value instanceof String) {
          final String string = (String)value;
          if (!Property.hasValue(string)) {
            return null;
          }
        }
        if (this.codeTable != null) {
          final Identifier identifier = this.codeTable.getIdentifier(value);
          if (identifier == null) {
            throw new IllegalArgumentException(getName() + "='" + value
              + "' cannot be found in code table " + this.codeTable.getName());
          } else {
            return identifier.toSingleValue();
          }
        }

        final V fieldValue = this.type.toObject(value);
        return fieldValue;
      } catch (final IllegalArgumentException e) {
        throw e;
      } catch (final Throwable e) {
        throw new IllegalArgumentException(
          getName() + "='" + value + "' is not a valid " + getDataType().getValidationName(), e);
      }
    }
  }

  public <V> V toFieldValueException(final RecordState state, final Object value) {
    if (value == null) {
      return null;
    } else {
      try {
        if (value instanceof String) {
          final String string = (String)value;
          if (!Property.hasValue(string)) {
            return null;
          }
        }
        if (this.codeTable != null && !state.isInitializing()) {
          final Identifier identifier = this.codeTable.getIdentifier(value);
          if (identifier == null) {
            throw new IllegalArgumentException(getName() + "='" + value
              + "' cannot be found in code table " + this.codeTable.getName());
          } else {
            return identifier.toSingleValue();
          }
        }

        final V fieldValue = this.type.toObject(value);
        return fieldValue;
      } catch (final IllegalArgumentException e) {
        throw e;
      } catch (final Throwable e) {
        throw new IllegalArgumentException(
          getName() + "='" + value + "' is not a valid " + getDataType().getValidationName(), e);
      }
    }
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = new JsonObjectHash();
    addTypeToMap(map, "field");
    map.put("name", getName());
    map.put("title", getTitle());
    addToMap(map, "description", getDescription(), "");
    map.put("dataType", getDataType().getName());
    map.put("length", getLength());
    map.put("scale", getScale());
    map.put("required", isRequired());
    addToMap(map, "minValue", getMinValue(), null);
    addToMap(map, "maxValue", getMaxValue(), null);
    addToMap(map, "defaultValue", getDefaultValue(), null);
    addToMap(map, "allowedValues", getAllowedValues(), Collections.emptyMap());
    return map;
  }

  @Override
  public String toString() {
    return this.name;
  }

  @Override
  public String toString(final Object value) {
    if (value == null) {
      return null;
    } else {
      if (value instanceof String) {
        final String string = (String)value;
        if (!Property.hasValue(string)) {
          return null;
        }
      }
      return this.type.toString(value);
    }
  }

  public Object validate(final MapEx record) {
    final String fieldName = getName();
    try {
      final Object value = record.getValue(fieldName);
      return validate(value);
    } catch (final Throwable e) {
      throw new ObjectPropertyException(record, fieldName, e.getMessage(), e);
    }
  }

  public Object validate(Object value) {
    final String fieldName = getName();
    value = toFieldValueException(value);
    if (value == null) {
      if (isRequired() && !isGenerated() && !isIdField()) {
        throw new IllegalArgumentException(fieldName + " is required");
      }
    } else {
      final RecordDefinition recordDefinition = getRecordDefinition();
      CodeTable codeTable = this.codeTable;
      if (codeTable == null && recordDefinition != null) {
        codeTable = recordDefinition.getCodeTableByFieldName(fieldName);
      }
      if (codeTable == null) {
        final int maxLength = getLength();
        if (value instanceof Number) {
          final Number number = (Number)value;
          final BigDecimal bigNumber = new BigDecimal(number.toString());
          final int length = bigNumber.precision();
          if (maxLength > 0) {
            if (length > maxLength) {
              throw new IllegalArgumentException(
                fieldName + "=" + value + " length " + length + " > " + maxLength);
            }
          }

          final int scale = bigNumber.scale();
          final int maxScale = getScale();
          if (maxScale > 0) {
            if (scale > maxScale) {
              throw new IllegalArgumentException(
                fieldName + "=" + value + " scale " + scale + " > " + maxScale);
            }
          }
          final Number minValue = getMinValue();
          if (minValue != null) {
            if (NumericComparator.numericCompare(number, minValue) < 0) {
              throw new IllegalArgumentException(fieldName + "=" + value + " > " + minValue);
            }
          }
          final Number maxValue = getMaxValue();
          if (maxValue != null) {
            if (NumericComparator.numericCompare(number, maxValue) > 0) {
              throw new IllegalArgumentException(fieldName + "=" + value + " < " + maxValue);
            }
          }
        } else if (value instanceof String) {
          final String string = (String)value;
          final int length = string.length();
          if (maxLength > 0) {
            if (length > maxLength) {
              throw new IllegalArgumentException(
                fieldName + "=" + value + " length " + length + " > " + maxLength);
            }
          }
        } else if (value instanceof Geometry) {
          final Geometry geometry = (Geometry)value;
          final IsValidOp validOp = new IsValidOp(geometry, false);
          if (!validOp.isValid()) {
            final String errors = Strings.toString(validOp.getErrors());
            throw new IllegalArgumentException("Geometry not valid: " + errors);
          }
        }
        if (!this.allowedValues.isEmpty()) {
          if (!this.allowedValues.containsKey(value)) {
            throw new IllegalArgumentException(fieldName + "=" + value + " not in ("
              + Strings.toString(",", this.allowedValues) + ")");
          }
        }
      } else {
        final Identifier id = codeTable.getIdentifier(value);
        if (id == null) {
          throw new IllegalArgumentException(
            "Unable to find code for '" + value + "' in " + codeTable.getName());
        }
      }
    }
    return value;
  }

  public Object validate(final Record record, final Object value) {
    final String fieldName = getName();
    try {
      return validate(value);
    } catch (final Throwable e) {
      throw new ObjectPropertyException(record, fieldName, e.getMessage(), e);
    }
  }
}
