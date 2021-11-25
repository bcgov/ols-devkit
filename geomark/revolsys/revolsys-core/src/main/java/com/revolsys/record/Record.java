package com.revolsys.record;

import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jeometry.common.compare.CompareUtil;
import org.jeometry.common.data.identifier.Identifiable;
import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.identifier.ListIdentifier;
import org.jeometry.common.data.identifier.TypedIdentifier;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonType;
import com.revolsys.record.io.format.json.Jsonable;
import com.revolsys.record.query.Value;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public interface Record extends MapEx, Comparable<Object>, Identifiable, RecordDefinitionProxy,
  BoundingBoxProxy, Jsonable {
  String EVENT_RECORD_CHANGED = "_recordChanged";

  String EXCLUDE_GEOMETRY = Record.class.getName() + ".excludeGeometry";

  String EXCLUDE_ID = Record.class.getName() + ".excludeId";

  Comparator<Record> IDENTIFIER_COMPARATOR = (final Record record1, final Record record2) -> {
    final Identifier identifier1 = record1.getIdentifier();
    final Identifier identifier2 = record2.getIdentifier();
    if (identifier1 == null) {
      if (identifier2 == null) {
        return 0;
      } else {
        return 1;
      }
    } else if (identifier2 == null) {
      return -1;
    } else {
      return identifier1.compareTo(identifier2);
    }
  };

  static boolean equalsNotNull(final List<Record> records1, List<Record> records2,
    final Collection<String> excludeFieldNames) {
    if (records1 == null) {
      return records2 == null;
    } else if (records2 == null) {
      return false;
    } else if (records1.size() == records2.size()) {
      records2 = new LinkedList<>(records2);
      for (final Record record1 : records1) {
        boolean recordEqual = false;
        for (final Record record2 : records2) {
          if (record1.equalValuesExclude(record2, excludeFieldNames)) {
            recordEqual = true;
            records2.remove(record2);
            break;
          }
        }
        if (!recordEqual) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  static boolean equalsNotNull(final Object object1, final Object object2) {
    return ((Record)object1).equalValuesAll((Map)object2);
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  static boolean equalsNotNull(final Object object1, final Object object2,
    final Collection<String> excludeFieldNames) {
    return ((Record)object1).equalValuesExclude((Map)object2, excludeFieldNames);
  }

  static Comparator<Record> newComparatorIdentifier(final String fieldName) {
    return (record1, record2) -> {
      final Identifier value1 = record1.getIdentifier(fieldName);
      final Identifier value2 = record2.getIdentifier(fieldName);
      if (value1 == null) {
        if (value2 == null) {
          return 0;
        } else {
          return 1;
        }
      } else if (value2 == null) {
        return -1;
      } else {
        final int compare = CompareUtil.compare(value1, value2);
        return compare;
      }
    };
  }

  static String toString(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final StringBuilder s = new StringBuilder();
    s.append(recordDefinition.getPath()).append("(\n");
    for (int i = 0; i < recordDefinition.getFieldCount(); i++) {
      final Object value = record.getValue(i);
      if (value != null) {
        final String fieldName = recordDefinition.getFieldName(i);
        s.append(fieldName).append('=').append(value).append('\n');
      }
    }
    s.append(')');
    return s.toString();
  }

  @Override
  default Record add(final String key, final Object value) {
    return (Record)MapEx.super.add(key, value);
  }

  @Override
  default Record addFieldValue(final String key, final Map<String, Object> source) {
    final Object value = source.get(key);
    return addValue(key, value);
  }

  @Override
  default Record addFieldValue(final String key, final Map<String, Object> source,
    final String sourceKey) {
    final Object value = source.get(sourceKey);
    return add(key, value);
  }

  @SuppressWarnings("unchecked")
  default <R extends Record> int addTo(final List<R> records) {
    if (!contains(records)) {
      final int index = records.size();
      records.add((R)this);
      return index;
    }
    return -1;
  }

  @Override
  default Record addValue(final String key, final Object value) {
    return (Record)MapEx.super.addValue(key, value);
  }

  default Record addValues(final MapEx values) {
    if (values instanceof Record) {
      final Record record = (Record)values;
      setValues(record);
    } else if (values != null) {
      setValues(values, new ArrayList<>(values.keySet()));
    }
    return this;
  }

  default <V> Record appendList(final String name, final V value) {
    List<V> collection = getValue(name);
    if (collection == null) {
      collection = new ArrayList<>();
    } else {
      collection = new ArrayList<>(collection);
    }
    collection.add(value);
    setValue(name, collection);
    return this;
  }

  @Override
  default JsonType asJson() {
    return JsonObject.hash(this);
  }

  @Override
  Record clone();

  @Override
  default int compareTo(final Object other) {
    if (other instanceof Record) {
      final Record record = (Record)other;
      return compareTo(record);
    } else {
      return -1;
    }
  }

  default int compareTo(final Record other) {
    if (other == null) {
      return -1;
    } else if (this == other) {
      return 0;
    } else {
      final int recordDefinitionCompare = getRecordDefinition()
        .compareTo(other.getRecordDefinition());
      if (recordDefinitionCompare == 0) {
        final Identifier id1 = getIdentifier();
        final Identifier id2 = other.getIdentifier();
        if (id1 == null) {
          if (id2 != null) {
            return -1;
          }
        } else {
          final int idCompare = id1.compareTo(id2);
          if (idCompare != 0) {
            return idCompare;
          }
        }
        final Geometry geometry1 = getGeometry();
        final Geometry geometry2 = other.getGeometry();
        if (geometry1 != null && geometry2 != null) {
          final int geometryComparison = geometry1.compareTo(geometry2);
          if (geometryComparison != 0) {
            return geometryComparison;
          }
        }
        final Integer hash1 = hashCode();
        final int hash2 = other.hashCode();
        final int hashCompare = hash1.compareTo(hash2);
        if (hashCompare != 0) {
          return hashCompare;
        }
        return -1;
      } else {
        return recordDefinitionCompare;
      }
    }
  }

  @Override
  default int compareValue(final CharSequence fieldName, final Object value) {
    final FieldDefinition fieldDefinition = getFieldDefinition(fieldName);
    if (fieldDefinition == null) {
      return -1;
    } else {
      final int fieldIndex = fieldDefinition.getIndex();
      final Object fieldValue = getValue(fieldIndex);
      return CompareUtil.compare(fieldValue, value);
    }
  }

  default int compareValue(final Map<String, Object> map, final CharSequence fieldName) {
    if (map != null) {
      final Object value = map.get(fieldName);
      return compareValue(fieldName, value);
    }
    return -1;
  }

  default int compareValue(final Record record, final CharSequence fieldName) {
    if (record != null) {
      final Object value = record.getValue(fieldName);
      return compareValue(fieldName, value);
    }
    return -1;
  }

  default int compareValue(final Record record, final CharSequence fieldName,
    final boolean nullsFirst) {
    final Comparable<Object> value1 = getValue(fieldName);
    Object value2;
    if (record == null) {
      value2 = null;
    } else {
      value2 = record.getValue(fieldName);
    }
    return CompareUtil.compare(value1, value2, nullsFirst);
  }

  default boolean contains(final Iterable<? extends Record> records) {
    for (final Record record : records) {
      if (isSame(record)) {
        return true;
      }
    }
    return false;
  }

  default boolean deleteRecord() {
    getRecordDefinition().deleteRecord(this);
    return true;
  }

  default double distance(final Geometry geometry) {
    final Geometry recordGeometry = getGeometry();
    if (Property.isEmpty(geometry) || Property.isEmpty(recordGeometry)) {
      return Double.NaN;
    } else {
      final double distance = recordGeometry.distanceGeometry(geometry);
      return distance;
    }
  }

  @Override
  default Set<Entry<String, Object>> entrySet() {
    return new RecordEntrySet(this);
  }

  default boolean equalPathValue(final CharSequence fieldPath, final Object value) {
    final Object fieldValue = getValueByPath(fieldPath);
    final boolean hasValue1 = Property.hasValue(value);
    final boolean hasValue2 = Property.hasValue(fieldValue);
    if (hasValue1) {
      if (hasValue2) {
        return DataType.equal(fieldValue, value);
      } else {
        return false;
      }
    } else {
      if (hasValue2) {
        return false;
      } else {
        return true;
      }
    }
  }

  @Override
  default boolean equalValue(final CharSequence fieldName, Object value) {
    final FieldDefinition fieldDefinition = getFieldDefinition(fieldName);
    if (fieldDefinition == null) {
      return false;
    } else {
      final int fieldIndex = fieldDefinition.getIndex();
      final Object fieldValue = getValue(fieldIndex);
      if (fieldValue != null) {
        final CodeTable codeTable = fieldDefinition.getCodeTable();
        if (codeTable != null) {
          final Object codeValue = codeTable.getIdentifier(value);
          if (codeValue != null) {
            value = codeValue;
          }
        }
      }
      return fieldDefinition.equals(fieldValue, value);
    }
  }

  default boolean equalValue(final Map<String, ? extends Object> map,
    final CharSequence fieldName) {
    if (map != null) {
      final Object value = map.get(fieldName);
      return equalValue(fieldName, value);
    }
    return false;
  }

  default boolean equalValueExclude(final CharSequence fieldName, final Object value,
    final CharSequence... excludeFieldNames) {
    final FieldDefinition fieldDefinition = getFieldDefinition(fieldName);
    if (fieldDefinition == null) {
      return false;
    } else {
      final int fieldIndex = fieldDefinition.getIndex();
      final Object fieldValue = getValue(fieldIndex);
      return fieldDefinition.equals(fieldValue, value, excludeFieldNames);
    }
  }

  default boolean equalValueExclude(final CharSequence fieldName, final Object value,
    final Collection<? extends CharSequence> excludeFieldNames) {
    final FieldDefinition fieldDefinition = getFieldDefinition(fieldName);
    if (fieldDefinition == null) {
      return false;
    } else {
      final int fieldIndex = fieldDefinition.getIndex();
      final Object fieldValue = getValue(fieldIndex);
      return fieldDefinition.equals(fieldValue, value, excludeFieldNames);
    }
  }

  default boolean equalValueExclude(final Map<String, ? extends Object> map,
    final CharSequence fieldName, final CharSequence... excludeFieldNames) {
    if (map != null) {
      final Object value = map.get(fieldName);
      return equalValueExclude(fieldName, value, excludeFieldNames);
    }
    return false;
  }

  default boolean equalValueExclude(final Map<String, ? extends Object> map,
    final CharSequence fieldName, final Collection<? extends CharSequence> excludeFieldNames) {
    if (isFieldExcluded(excludeFieldNames, fieldName)) {
      return true;
    } else {
      if (map != null) {
        final Object value = map.get(fieldName);
        return equalValueExclude(fieldName, value, excludeFieldNames);
      }
      return false;
    }
  }

  default boolean equalValueExclude(final Record otherRecord, final CharSequence fieldName,
    final CharSequence... excludeFieldNames) {
    if (otherRecord != null) {
      final Object value = otherRecord.getValue(fieldName);
      return equalValueExclude(fieldName, value, excludeFieldNames);
    }
    return false;
  }

  /**
   * Equal if the keys and values in the map are equal to those field values on the record.
   * @param map
   */
  default boolean equalValues(final Map<String, ? extends Object> map) {
    if (map != null) {
      for (final String fieldName : map.keySet()) {
        if (hasField(fieldName)) {
          if (!equalValue(map, fieldName)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  default boolean equalValues(final Map<String, ? extends Object> map, final String... fieldNames) {
    if (map == null) {
      return false;
    } else {
      for (final String fieldName : fieldNames) {
        if (!equalValue(map, fieldName)) {
          return false;
        }
      }
      return true;
    }
  }

  /**
   * Equal if the map has all the fields and values of this record
   * @param map
   */
  default boolean equalValuesAll(final Map<String, ? extends Object> map) {
    if (map == null) {
      return false;
    } else {
      if (map instanceof Record) {
        final Record record = (Record)map;
        if (!record.getPathName().equals(getPathName())) {
          return false;
        }
      }
      final List<String> fieldNames = getFieldNames();
      for (final String fieldName : fieldNames) {
        if (!equalValue(map, fieldName)) {
          return false;
        }
      }
      return true;
    }
  }

  /**
   * Equal if the map has all the fields and values of this record
   * @param map
   */
  default boolean equalValuesExclude(final Map<String, Object> map,
    final Collection<? extends CharSequence> excludeFieldNames) {
    final List<String> fieldNames = getFieldNames();
    for (final String fieldName : fieldNames) {
      if (!equalValueExclude(map, fieldName, excludeFieldNames)) {
        return false;
      }
    }
    return true;
  }

  @Override
  default Object get(final Object key) {
    if (key instanceof CharSequence) {
      final CharSequence name = (String)key;
      return getValue(name);
    } else {
      return null;
    }
  }

  @Override
  default BoundingBox getBoundingBox() {
    final Geometry geometry = getGeometry();
    if (geometry == null) {
      return null;
    } else {
      return geometry.getBoundingBox();
    }
  }

  @Override
  default Byte getByte(final CharSequence name) {
    final Object value = getValue(name);
    if (Property.hasValue(value)) {
      if (value instanceof Number) {
        final Number number = (Number)value;
        return number.byteValue();
      } else {
        return Byte.valueOf(value.toString());
      }
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  default <T> T getCodeValue(final CharSequence fieldName) {
    Object value = getValue(fieldName);
    if (Property.hasValue(value)) {
      final FieldDefinition fieldDefinition = getFieldDefinition(fieldName);
      value = fieldDefinition.getCodeValue(value);
    }
    return (T)value;
  }

  @SuppressWarnings("unchecked")
  default <T> T getCodeValue(final int fieldIndex) {
    Object value = getValue(fieldIndex);
    if (Property.hasValue(value)) {
      final FieldDefinition fieldDefinition = getFieldDefinition(fieldIndex);
      value = fieldDefinition.getCodeValue(value);
    }
    return (T)value;
  }

  /**
   * Return the list of field names that are different between this record and the other map.
   * Compares all the field names from this record.
   *
   * @param map The map to compare
   */
  default List<String> getDifferentFieldNames(final Map<String, Object> map) {
    final List<String> fieldNames = getFieldNames();
    return getDifferentFieldNames(map, fieldNames);
  }

  /**
   * Return the list of field names that are different between this record and the other map.
   *
   * @param map The map to compare
   * @param fieldNames The field names to compare
   */
  default List<String> getDifferentFieldNames(final Map<String, Object> map,
    final Collection<? extends CharSequence> fieldNames) {
    List<String> differentFieldNames = new ArrayList<>();
    for (final CharSequence fieldName : fieldNames) {
      if (!equalValue(map, fieldName)) {
        if (differentFieldNames == Collections.<String> emptyList()) {
          differentFieldNames = new ArrayList<>();
        }
        differentFieldNames.add(fieldName.toString());
      }
    }
    return differentFieldNames;
  }

  /**
   * Return the list of field names that are different between this record and the other map.
   *
   * @param map The map to compare
   * @param excludeFieldNames The field names to not compare
   */
  default List<String> getDifferentFieldNamesExclude(final Map<String, Object> map,
    final Collection<? extends CharSequence> excludeFieldNames) {
    List<String> differentFieldNames = new ArrayList<>();
    final List<String> fieldNames = getFieldNames();
    for (final CharSequence fieldName : fieldNames) {
      if (!excludeFieldNames.contains(fieldName) && !equalValue(map, fieldName)) {
        if (differentFieldNames == Collections.<String> emptyList()) {
          differentFieldNames = new ArrayList<>();
        }
        differentFieldNames.add(fieldName.toString());
      }
    }
    return differentFieldNames;
  }

  @Override
  default Double getDouble(final CharSequence name) {
    final Object value = getValue(name);
    if (Property.hasValue(value)) {
      if (value instanceof Number) {
        final Number number = (Number)value;
        return number.doubleValue();
      } else {
        return Double.valueOf(value.toString());
      }
    } else {
      return null;
    }
  }

  @Override
  default <E extends Enum<E>> E getEnum(final Class<E> enumType, final CharSequence fieldName) {
    final String value = getString(fieldName);
    if (Property.hasValue(value)) {
      return Enum.valueOf(enumType, value);
    } else {
      return null;
    }
  }

  default <E extends Enum<E>> E getEnum(final Class<E> enumType, final CharSequence fieldName,
    final E defaultValue) {
    final String value = getString(fieldName);
    if (Property.hasValue(value)) {
      return Enum.valueOf(enumType, value);
    } else {
      return defaultValue;
    }
  }

  @Override
  default Float getFloat(final CharSequence name) {
    final Object value = getValue(name);
    if (Property.hasValue(value)) {
      if (value instanceof Number) {
        final Number number = (Number)value;
        return number.floatValue();
      } else {
        return Float.valueOf(value.toString());
      }
    } else {
      return null;
    }
  }

  /**
   * Get the value of the primary geometry field.
   *
   * @return The primary geometry.
   */

  @SuppressWarnings("unchecked")
  default <T extends Geometry> T getGeometry() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      final int index = recordDefinition.getGeometryFieldIndex();
      return (T)getValue(index, GeometryDataTypes.GEOMETRY);
    }
  }

  @Override
  default GeometryFactory getGeometryFactory() {
    return RecordDefinitionProxy.super.getGeometryFactory();
  }

  @Override
  default Identifier getIdentifier() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final int idFieldCount = recordDefinition.getIdFieldCount();
    if (idFieldCount == 1) {
      final Integer idFieldIndex = recordDefinition.getIdFieldIndex();
      final Object idValue = getValue(idFieldIndex);
      if (idValue == null) {
        return null;
      } else {
        return Identifier.newIdentifier(idValue);
      }
    } else if (idFieldCount > 0) {
      boolean notNull = false;
      final List<Integer> idFieldIndexes = recordDefinition.getIdFieldIndexes();
      final Object[] idValues = new Object[idFieldCount];
      for (int i = 0; i < idValues.length; i++) {
        final Integer idFieldIndex = idFieldIndexes.get(i);
        final Object value = getValue(idFieldIndex);
        if (value != null) {
          notNull = true;
        }
        idValues[i] = value;
      }
      if (notNull) {
        return new ListIdentifier(idValues);
      }
    }
    return null;
  }

  @Override
  default Identifier getIdentifier(final CharSequence fieldName) {
    final Object value = getValue(fieldName);
    return Identifier.newIdentifier(value);
  }

  default Identifier getIdentifier(final CharSequence fieldName, final DataType dataType) {
    final Object value = getValue(fieldName, dataType);
    return Identifier.newIdentifier(value);
  }

  default Identifier getIdentifier(final int index) {
    final Object value = getValue(index);
    return Identifier.newIdentifier(value);
  }

  default Identifier getIdentifier(final List<? extends CharSequence> fieldNames) {
    final int idCount = fieldNames.size();
    if (idCount == 0) {
      return null;
    } else if (idCount == 1) {
      final CharSequence idFieldName = fieldNames.get(0);
      final Object idValue = getValue(idFieldName);
      if (idValue == null) {
        return null;
      } else {
        return Identifier.newIdentifier(idValue);
      }
    } else {
      boolean notNull = false;
      final Object[] idValues = new Object[idCount];
      for (int i = 0; i < idValues.length; i++) {
        final CharSequence idFieldName = fieldNames.get(i);
        final Object value = getValue(idFieldName);
        if (value != null) {
          notNull = true;
        }
        idValues[i] = value;
      }
      if (notNull) {
        return new ListIdentifier(idValues);
      } else {
        return null;
      }
    }
  }

  default TypedIdentifier getIdentifierTyped(final String type) {
    final Identifier identifier = getIdentifier();
    return TypedIdentifier.newIdentifier(type, identifier);
  }

  @Override
  default Integer getInteger(final CharSequence name) {
    final Object value = getValue(name);
    if (Property.hasValue(value)) {
      if (value instanceof Number) {
        final Number number = (Number)value;
        return number.intValue();
      } else {
        return Integer.valueOf(value.toString());
      }
    } else {
      return null;
    }
  }

  @Override
  default int getInteger(final CharSequence name, final int defaultValue) {
    final Integer value = getInteger(name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  @Override
  default Long getLong(final CharSequence name) {
    final Object value = getValue(name);
    if (Property.hasValue(value)) {
      if (value instanceof Number) {
        final Number number = (Number)value;
        return number.longValue();
      } else {
        return Long.valueOf(value.toString());
      }
    } else {
      return null;
    }
  }

  @Override
  RecordDefinition getRecordDefinition();

  @Override
  default Short getShort(final CharSequence name) {
    final Object value = getValue(name);
    if (Property.hasValue(value)) {
      if (value instanceof Number) {
        final Number number = (Number)value;
        return number.shortValue();
      } else {
        return Short.valueOf(value.toString());
      }
    } else {
      return null;
    }
  }

  default RecordState getState() {
    return RecordState.NEW;
  }

  @Override
  default String getString(final CharSequence fieldName) {
    final Object value = getValue(fieldName);
    if (value == null) {
      return null;
    } else if (value instanceof String) {
      return value.toString();
    } else if (value instanceof Clob) {
      final Clob clob = (Clob)value;
      try {
        return clob.getSubString(1, (int)clob.length());
      } catch (final SQLException e) {
        throw new RuntimeException("Unable to read clob", e);
      }
    } else {
      return getFieldDefinition(fieldName).toString(value);
    }
  }

  @Override
  default String getString(final CharSequence name, final String defaultValue) {
    final String value = getString(name);
    if (Property.hasValue(value)) {
      return value;
    } else {
      return defaultValue;
    }
  }

  default Identifier getTypedIdentifier(final String fieldName) {
    final Object identifier = getValue(fieldName);
    return TypedIdentifier.newIdentifier(identifier);
  }

  /**
   * Get the value of the field with the specified index.
   *
   * @param index The index of the field.
   * @return The field value.
   */
  default <T extends Object> T getValue(final int index) {
    final String fieldName = getFieldName(index);
    return Property.getSimple(this, fieldName);
  }

  default <T extends Object> T getValue(final int index, final DataType dataType) {
    final Object value = getValue(index);
    return dataType.toObject(value);
  }

  /**
   * Get the value of the field with the specified name.
   *
   * @param name The name of the field.
   * @return The field value.
   */

  @Override
  @SuppressWarnings("unchecked")
  default <T extends Object> T getValue(final String name) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    try {
      final int index = recordDefinition.getFieldIndex(name);
      return (T)getValue(index);
    } catch (final NullPointerException e) {
      if (recordDefinition == null) {
        Logs.warn(this, "record defintion not specified", e);
      } else {
        Logs.warn(this, "Field " + recordDefinition.getPath() + "." + name + " does not exist", e);
      }
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  default <T> T getValueByPath(final CharSequence path) {
    final int fieldIndex = getFieldIndex(path);
    if (fieldIndex == -1) {
      final RecordDefinition recordDefinition = getRecordDefinition();
      final String[] propertyPath = path.toString().split("\\.");
      Object propertyValue = this;
      for (int i = 0; i < propertyPath.length && propertyValue != null; i++) {
        final String propertyName = propertyPath[i];
        if (propertyValue instanceof Record) {
          final Record record = (Record)propertyValue;

          if (record.hasField(propertyName)) {
            propertyValue = record.getValue(propertyName);
            if (propertyValue == null) {
              return null;
            } else if (i + 1 < propertyPath.length) {
              final CodeTable codeTable = recordDefinition.getCodeTableByFieldName(propertyName);
              if (codeTable != null) {
                propertyValue = codeTable.getMap(propertyValue);
              }
            }
          } else {
            return null;
          }
        } else if (propertyValue instanceof Map) {
          final Map<String, Object> map = (Map<String, Object>)propertyValue;
          propertyValue = map.get(propertyName);
          if (propertyValue == null) {
            return null;
          } else if (i + 1 < propertyPath.length) {
            final CodeTable codeTable = recordDefinition.getCodeTableByFieldName(propertyName);
            if (codeTable != null) {
              propertyValue = codeTable.getMap(propertyValue);
            }
          }
        } else {
          try {
            final Object object = propertyValue;
            propertyValue = Property.getSimple(object, propertyName);
          } catch (final IllegalArgumentException e) {
            Logs.debug(this, "Path does not exist " + path, e);
            return null;
          }
        }
      }
      return (T)propertyValue;
    } else {
      return getValue(fieldIndex);
    }
  }

  default Map<String, Object> getValueMap(final Collection<? extends CharSequence> fieldNames) {
    final Map<String, Object> values = new HashMap<>();
    for (final CharSequence name : fieldNames) {
      final Object value = getValue(name);
      if (value != null) {
        values.put(name.toString(), value);
      }
    }
    return values;
  }

  default List<Object> getValues() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final List<Object> values = new ArrayList<>();
    for (int i = 0; i < recordDefinition.getFieldCount(); i++) {
      final Object value = getValue(i);
      values.add(value);
    }
    return values;
  }

  default List<Object> getValues(final Iterable<? extends CharSequence> fieldNames) {
    final List<Object> values = new ArrayList<>();
    for (final CharSequence fieldName : fieldNames) {
      final Object value = getValue(fieldName);
      values.add(value);
    }
    return values;
  }

  default boolean hasGeometry() {
    final Geometry geometry = getGeometry();
    return Property.hasValue(geometry);
  }

  @Override
  default boolean hasValue(final CharSequence name) {
    final Object value = getValue(name);
    return Property.hasValue(value);
  }

  default boolean hasValue(final int fieldIndex) {
    final Object value = getValue(fieldIndex);
    return Property.hasValue(value);
  }

  @Override
  default boolean hasValuesAll(final CharSequence... fieldNames) {
    for (final CharSequence fieldName : fieldNames) {
      if (!hasValue(fieldName)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check if any of the fields have a value.
   *
   * @param fieldNames
   * @return True if any of the fields have a value, false otherwise.
   */
  @Override
  default boolean hasValuesAny(final CharSequence... fieldNames) {
    for (final CharSequence fieldName : fieldNames) {
      if (hasValue(fieldName)) {
        return true;
      }
    }
    return false;
  }

  default int indexOf(final Iterable<? extends Record> records) {
    int index = 0;
    for (final Record record : records) {
      if (isSame(record)) {
        return index;
      }
      index++;
    }
    return -1;
  }

  default int indexOf(final List<? extends Record> records) {
    for (int index = 0; index < records.size(); index++) {
      try {
        final Record record = records.get(index);
        if (isSame(record)) {
          return index;
        }
      } catch (final IndexOutOfBoundsException e) {
        return -1;
      }
    }
    return -1;
  }

  default boolean isChanged() {
    final RecordState state = getState();
    if (state == RecordState.PERSISTED) {
      return false;
    } else {
      return true;
    }
  }

  default boolean isDeleted() {
    final RecordState state = getState();
    if (state == RecordState.DELETED) {
      return true;
    } else {
      return false;
    }
  }

  default boolean isFieldExcluded(final Collection<? extends CharSequence> excludeFieldNames,
    CharSequence fieldName) {
    fieldName = fieldName.toString();
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (excludeFieldNames.contains(fieldName)) {
      return true;
    } else if (excludeFieldNames.contains(Record.EXCLUDE_ID)
      && ("OBJECTID".equals(fieldName) || recordDefinition.isIdField(fieldName))) {
      return true;
    } else if (excludeFieldNames.contains(Record.EXCLUDE_GEOMETRY) && ("OBJECTID".equals(fieldName)
      || recordDefinition.getGeometryFieldNames().contains(fieldName))) {
      return true;
    } else {
      return false;
    }
  }

  default boolean isModified() {
    final RecordState state = getState();
    if (state == RecordState.NEW) {
      return true;
    } else if (state == RecordState.MODIFIED) {
      return true;
    } else {
      return false;
    }
  }

  default boolean isSame(final Record record) {
    if (record == null) {
      return false;
    } else {
      if (this == record) {
        return true;
      } else {
        synchronized (this) {
          if (record.getRecordDefinition() == getRecordDefinition()) {
            final Identifier id = getIdentifier();
            final Identifier otherId = record.getIdentifier();
            if (id == null || otherId == null) {
              return false;
            } else if (DataType.equal(id, otherId)) {
              return true;
            } else {
              return false;
            }
          } else {
            return false;
          }
        }
      }
    }
  }

  default boolean isState(final RecordState state) {
    return getState() == state;
  }

  default boolean isValid(final CharSequence fieldName) {
    return true;
  }

  default boolean isValid(final int index) {
    return true;
  }

  @Override
  default Set<String> keySet() {
    return getRecordDefinition().getFieldNamesSet();
  }

  default Record newRecordGeometry(final Geometry geometry) {
    final Record record = clone();
    record.setGeometryValue(geometry);
    return record;
  }

  @Override
  default Object put(final String key, final Object value) {
    final Object oldValue = getValue(key);
    setValue(key, value);
    return oldValue;
  }

  @Override
  default void putAll(final Map<? extends String, ? extends Object> values) {
    setValues(values);
  }

  @Override
  default Object remove(final Object key) {
    if (key instanceof CharSequence) {
      final CharSequence name = (CharSequence)key;
      final Object value = getValue(name);
      setValue(name, null);
      return value;
    }
    return null;
  }

  /**
   * Remove the first record in the collection of records that is {{@link #isSame(Record)}} as this record.
   *
   * @param records
   * @return The index of the removed record.
   */
  default int removeFrom(final Iterable<? extends Record> records) {
    int index = 0;
    for (final Iterator<? extends Record> iterator = records.iterator(); iterator.hasNext();) {
      final Record record = iterator.next();
      if (record.isSame(this)) {
        iterator.remove();
        return index;
      } else {
        index++;
      }
    }
    return -1;
  }

  @SuppressWarnings("unchecked")
  default <V> V setCodeValue(final String fieldName, final Record record,
    final String fromFieldName) {
    final Object value = record.getCodeValue(fromFieldName);
    setValue(fieldName, value);
    return (V)value;
  }

  /**
   * Set the value of the primary geometry field.
   *
   * @param geometry The primary geometry.
   * @return TODO
   */

  default Record setGeometryValue(final Geometry geometry) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final int index = recordDefinition.getGeometryFieldIndex();
    if (index > -1) {
      setValue(index, geometry);
    }
    return this;
  }

  default void setGeometryValue(final Record record) {
    if (record != null) {
      final Geometry geometry = record.getGeometry();
      if (geometry != null) {
        setGeometryValue(geometry);
      }
    }
  }

  default void setIdentifier(final Identifier identifier) {
    final RecordDefinition recordDefinition = getRecordDefinition();

    final RecordState state = getState();
    if (state == RecordState.NEW || state == RecordState.INITIALIZING) {
      final List<String> idFieldNames = recordDefinition.getIdFieldNames();
      Identifier.setIdentifier(this, idFieldNames, identifier);
    } else {
      final Identifier oldIdentifier = getIdentifier();
      if (!DataTypes.IDENTIFIER.equals(oldIdentifier, identifier)) {
        throw new IllegalStateException(
          "Cannot change the ID on a persisted record: " + identifier + "!=" + oldIdentifier);
      }
    }
  }

  default RecordState setState(final RecordState state) {
    return getState();
  }

  /**
   * Set the value of the field with the specified name.
   *
   * @param name The name of the field.
   * @param value The new value.
   */

  default boolean setValue(final CharSequence name, final Object value) {
    final boolean updated = false;
    final RecordDefinition recordDefinition = getRecordDefinition();
    final int index = recordDefinition.getFieldIndex(name);
    if (index != -1) {
      return setValue(index, value);
    } else {
      if (Strings.contains(name, '.')) {
        throw new IllegalArgumentException("name cannot contain a '.' " + name + "=" + value);
      }
    }
    return updated;
  }

  default <T> T setValue(final CharSequence fieldName, final Record source,
    final String sourceFieldName) {
    @SuppressWarnings("unchecked")
    final T value = (T)source.getValue(sourceFieldName);
    setValueByPath(fieldName, value);
    return value;
  }

  /**
   * Set the value of the field with the specified name.
   *
   * @param index The index of the field.
   * @param value The new value;
   */
  default boolean setValue(final int index, final Object value) {
    final String fieldName = getFieldName(index);
    final Object oldValue = getValue(index);
    Property.set(this, fieldName, value);
    return DataType.equal(oldValue, value);
  }

  default <T> T setValue(final Record source, final CharSequence fieldName) {
    @SuppressWarnings("unchecked")
    final T value = (T)source.getValue(fieldName);
    setValue(fieldName, value);
    return value;
  }

  @SuppressWarnings("rawtypes")
  default boolean setValueByPath(final CharSequence path, final Object value) {
    boolean updated = false;
    final String name = path.toString();
    final int dotIndex = name.indexOf(".");
    String codeTableFieldName;
    String codeTableValueName = null;
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (dotIndex == -1) {
      if (recordDefinition.isIdField(name)) {
        codeTableFieldName = null;
      } else {
        codeTableFieldName = name;
      }
    } else {
      codeTableFieldName = name.substring(0, dotIndex);
      codeTableValueName = name.substring(dotIndex + 1);
    }
    final CodeTable codeTable = recordDefinition.getCodeTableByFieldName(codeTableFieldName);
    if (codeTable == null) {
      if (dotIndex != -1) {
        Logs.debug(this, "Cannot get code table for " + recordDefinition.getPath() + "." + name);
        return false;
      }
      updated = setValue(name, value);
    } else if (!Property.hasValue(value)) {
      updated = setValue(codeTableFieldName, null);
    } else {
      Object targetValue;
      if (codeTableValueName == null) {
        Identifier id;
        if (value instanceof List) {
          final List list = (List)value;
          id = codeTable.getIdentifier(list.toArray());
        } else {
          if (getState().isInitializing()) {
            id = null;
          } else {
            id = codeTable.getIdentifier(value);
          }
        }
        if (id == null) {
          targetValue = value;
        } else {
          targetValue = Value.getValue(id);
        }
      } else {
        targetValue = codeTable.getIdentifier(Collections.singletonMap(codeTableValueName, value));
      }
      if (targetValue == null) {
        targetValue = value;
      }
      updated = setValue(codeTableFieldName, targetValue);
    }
    return updated;
  }

  default <T> T setValueByPath(final CharSequence fieldPath, final Record source,
    final String sourceFieldPath) {
    @SuppressWarnings("unchecked")
    final T value = (T)source.getValueByPath(sourceFieldPath);
    setValueByPath(fieldPath, value);
    return value;
  }

  default void setValues(final Iterable<? extends Object> values) {
    int fieldIndex = 0;
    for (final Object value : values) {
      setValue(fieldIndex, value);
      fieldIndex++;
    }
  }

  default void setValues(final Map<? extends CharSequence, ? extends Object> values) {
    if (values instanceof Record) {
      final Record record = (Record)values;
      setValues(record);
    } else if (values != null) {
      setValues(values, new ArrayList<>(values.keySet()));
    }
  }

  default void setValues(final Map<? extends CharSequence, ? extends Object> values,
    final Collection<? extends CharSequence> fieldNames) {
    for (final CharSequence fieldName : fieldNames) {
      final Object newValue = values.get(fieldName);
      final FieldDefinition fieldDefinition = getFieldDefinition(fieldName);
      if (fieldDefinition != null) {
        fieldDefinition.setValue(this, newValue);
      }
    }
  }

  default void setValues(final Map<? extends String, ? extends Object> values,
    final String... fieldNames) {
    setValues(values, Arrays.asList(fieldNames));
  }

  default Record setValues(final Object... values) {
    for (int fieldIndex = 0; fieldIndex < values.length; fieldIndex++) {
      final Object value = values[fieldIndex];
      setValue(fieldIndex, value);
    }
    return this;
  }

  default void setValues(final Record record) {
    if (record != null) {
      final List<FieldDefinition> fields = getFieldDefinitions();
      for (final FieldDefinition fieldDefintion : fields) {
        if (!isIdField(fieldDefintion)) {
          final String name = fieldDefintion.getName();
          if (record.hasField(name)) {
            final Object value = record.getValue(name);
            fieldDefintion.setValue(this, value);
          }
        }
      }
    }
  }

  default void setValuesAll(final Record record) {
    if (record != null) {
      final List<FieldDefinition> fields = getFieldDefinitions();
      for (final FieldDefinition fieldDefintion : fields) {
        final String name = fieldDefintion.getName();
        final Object value = record.getValue(name);
        fieldDefintion.setValue(this, value);
      }
      final Geometry geometry = record.getGeometry();
      if (geometry != null) {
        setGeometryValue(geometry);
      }
    }
  }

  default void setValuesByPath(final Map<? extends CharSequence, ? extends Object> values) {
    if (values != null) {
      for (final Entry<? extends CharSequence, ? extends Object> defaultValue : new ArrayList<>(
        values.entrySet())) {
        final CharSequence name = defaultValue.getKey();
        final Object value = defaultValue.getValue();
        setValueByPath(name, value);
      }
    }
  }

  default void setValuesClone(final Record record) {
    final List<FieldDefinition> fields = getFieldDefinitions();
    for (final FieldDefinition fieldDefintion : fields) {
      final String name = fieldDefintion.getName();
      final Object value = record.getValue(name);
      fieldDefintion.setValueClone(this, value);
    }
  }

  default void setValuesNotNull(final Record record) {
    if (record != null) {
      final List<FieldDefinition> idFields = getRecordDefinition().getIdFields();
      final List<FieldDefinition> fields = getFieldDefinitions();
      for (final FieldDefinition fieldDefintion : fields) {
        if (!idFields.contains(fieldDefintion)) {
          final String name = fieldDefintion.getName();
          if (record.hasField(name)) {
            final Object value = record.getValue(name);
            if (value != null) {
              fieldDefintion.setValue(this, value);
            }
          }
        }
      }
    }
  }

  @Override
  default int size() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getFieldCount();
  }

  @Override
  default JsonObject toJson() {
    final JsonObject json = JsonObject.hash();
    for (int i = 0; i < getFieldCount(); i++) {
      final Object value = getValue(i);
      if (value != null) {
        final String fieldName = getFieldName(i);
        json.addValueClone(fieldName, value);
      }
    }
    return json;
  }

  @Override
  default String toJsonString() {
    return toJson().toJsonString();
  }

  default void validateField(final FieldDefinition field) {
    if (field != null) {
      final int index = field.getIndex();
      final Object value = getValue(index);
      field.validate(this, value);
    }
  }

  default void validateField(final int fieldIndex) {
    final FieldDefinition field = getFieldDefinition(fieldIndex);
    if (field != null) {
      final Object value = getValue(fieldIndex);
      field.validate(this, value);
    }
  }
}
