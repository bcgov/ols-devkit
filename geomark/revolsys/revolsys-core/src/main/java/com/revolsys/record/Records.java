package com.revolsys.record;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.jeometry.common.compare.CompareUtil;
import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.list.Lists;
import com.revolsys.collection.map.MapEx;
import com.revolsys.comparator.StringNumberComparator;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.TopologyException;
import com.revolsys.geometry.model.editor.BoundingBoxEditor;
import com.revolsys.predicate.Predicates;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.query.ColumnReference;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.BaseCloneable;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public interface Records {
  static BoundingBox bboxAddRecords(final BoundingBoxEditor boundingBox,
    final Iterable<? extends Record> records) {
    for (final Record record : records) {
      final Geometry geometry = record.getGeometry();
      boundingBox.addGeometry(geometry);
    }
    return boundingBox.newBoundingBox();
  }

  static BoundingBox boundingBox(final GeometryFactory geometryFactory,
    final Iterable<? extends Record> records) {
    final BoundingBoxEditor boundingBox = geometryFactory.bboxEditor();
    for (final Record record : records) {
      final Geometry geometry = record.getGeometry();
      boundingBox.addGeometry(geometry);
    }
    return boundingBox.newBoundingBox();
  }

  static BoundingBox boundingBox(final Iterable<? extends Record> records) {
    BoundingBox boundingBox = BoundingBox.empty();
    for (final Record record : records) {
      boundingBox = boundingBox.bboxEdit(editor -> editor.addBbox(boundingBox(record)));
    }
    return boundingBox;
  }

  static BoundingBox boundingBox(final Record record) {
    if (record != null) {
      final Geometry geometry = record.getGeometry();
      if (geometry != null) {
        return geometry.getBoundingBox();
      }
    }
    return BoundingBox.empty();
  }

  static int compareNullFirst(final Record record1, final Record record2, final String fieldName) {
    final Object value1 = getValue(record1, fieldName);
    final Object value2 = getValue(record2, fieldName);
    if (value1 == value2) {
      return 0;
    } else {
      if (value1 == null) {
        return -1;
      } else if (value2 == null) {
        return 1;
      } else {
        return CompareUtil.compare(value1, value2);
      }
    }
  }

  static int compareNullFirst(final Record record1, final Record record2,
    final String... fieldNames) {
    for (final String fieldName : fieldNames) {
      final Object value1 = getValue(record1, fieldName);
      final Object value2 = getValue(record2, fieldName);
      if (value1 != value2) {
        if (value1 == null) {
          return -1;
        } else if (value2 == null) {
          return 1;
        } else {
          final int compare = CompareUtil.compare(value1, value2);
          if (compare != 0) {
            return compare;
          }
        }
      }
    }
    return 0;
  }

  static int compareNullLast(final Record record1, final Record record2, final String fieldName) {
    final Object value1 = getValue(record1, fieldName);
    final Object value2 = getValue(record2, fieldName);
    if (value1 == value2) {
      return 0;
    } else {
      if (value1 == null) {
        return 1;
      } else if (value2 == null) {
        return -1;
      } else {
        return CompareUtil.compare(value1, value2);
      }
    }
  }

  static int compareNullLast(final Record record1, final Record record2,
    final String... fieldNames) {
    for (final String fieldName : fieldNames) {
      final Object value1 = getValue(record1, fieldName);
      final Object value2 = getValue(record2, fieldName);
      if (value1 != value2) {
        if (value1 == null) {
          return 1;
        } else if (value2 == null) {
          return -1;
        } else {
          final int compare = CompareUtil.compare(value1, value2);
          if (compare != 0) {
            return compare;
          }
        }
      }
    }
    return 0;
  }

  static Record copy(final RecordDefinition recordDefinition, final Record record) {
    final Record copy = new ArrayRecord(recordDefinition);
    copy.setValuesClone(record);
    return copy;
  }

  /**
   * Construct a new copy of the data record replacing the geometry with the new
   * geometry. If the existing geometry on the record has user data it will be
   * cloned to the new geometry.
   *
   * @param record The record to copy.
   * @param geometry The new geometry.
   * @return The copied record.
   */
  @SuppressWarnings("unchecked")
  static <T extends Record> T copy(final T record, final Geometry geometry) {
    final T newObject = (T)record.clone();
    newObject.setGeometryValue(geometry);
    return newObject;
  }

  static void copyRecords(final RecordStore sourceRecordStore, final String sourceTableName,
    final RecordStore targetRecordStore, final String targetTableName) {
    final Query query = new Query(sourceTableName);
    try (
      RecordReader reader = sourceRecordStore.getRecords(query);
      RecordWriter writer = targetRecordStore.newRecordWriter();) {
      final RecordDefinition recordDefinition = targetRecordStore
        .getRecordDefinition(targetTableName);
      for (final Record record : reader) {
        final Record newRecord = recordDefinition.newRecord();
        newRecord.setValuesAll(record);
        writer.write(newRecord);
      }
    }
  }

  static void copyRecords(final RecordStore sourceRecordStore, final String sourceTableName,
    final RecordStore targetRecordStore, final String targetTableName,
    final BiConsumer<Record, Record> recordEditor) {
    final Query query = new Query(sourceTableName);
    try (
      RecordReader reader = sourceRecordStore.getRecords(query);
      RecordWriter writer = targetRecordStore.newRecordWriter();) {
      final RecordDefinition recordDefinition = targetRecordStore
        .getRecordDefinition(targetTableName);
      for (final Record record : reader) {
        final Record newRecord = recordDefinition.newRecord();
        newRecord.setValuesAll(record);
        recordEditor.accept(record, newRecord);
        writer.write(newRecord);
      }
    }
  }

  static double distance(final Record record1, final Record record2) {
    if (record1 == null || record2 == null) {
      return Double.MAX_VALUE;
    } else {
      final Geometry geometry1 = record1.getGeometry();
      final Geometry geometry2 = record2.getGeometry();
      if (geometry1 == null || geometry2 == null) {
        return Double.MAX_VALUE;
      } else {
        return geometry1.distanceGeometry(geometry2);
      }
    }
  }

  static <D extends Record> List<D> filter(final Collection<D> records, final Geometry geometry,
    final double maxDistance) {
    final List<D> results = new ArrayList<>();
    for (final D record : records) {
      final Geometry recordGeometry = record.getGeometry();
      final double distance = recordGeometry.distanceGeometry(geometry);
      if (distance < maxDistance) {
        results.add(record);
      }
    }
    return results;
  }

  /**
   * Filter and sort the records. The list will be overwritten by the result.
   *
   * @param records
   * @param filter
   * @param orderBy
   */
  static <V extends Record> void filterAndSort(final List<V> records,
    final Predicate<? super V> filter, final Map<QueryValue, Boolean> orderBy) {
    // Filter records
    if (!Property.isEmpty(filter)) {
      Predicates.retain(records, filter);
    }

    // Sort records
    if (Property.hasValue(orderBy)) {
      final Comparator<Record> comparator = newComparatorOrderBy(orderBy);
      Collections.sort(records, comparator);
    }
  }

  static boolean getBoolean(final Record record, final String fieldName) {
    if (record == null) {
      return false;
    } else {
      final Object value = getValue(record, fieldName);
      if (value == null) {
        return false;
      } else if (value instanceof Boolean) {
        final Boolean booleanValue = (Boolean)value;
        return booleanValue;
      } else if (value instanceof Number) {
        final Number number = (Number)value;
        return number.intValue() == 1;
      } else {
        final String stringValue = value.toString();
        if (stringValue.equals("Y") || stringValue.equals("1")
          || Boolean.parseBoolean(stringValue)) {
          return true;
        } else {
          return false;
        }
      }
    }
  }

  static Double getDouble(final Record record, final int fieldIndex) {
    final Number value = record.getValue(fieldIndex);
    if (value == null) {
      return null;
    } else if (value instanceof Double) {
      return (Double)value;
    } else {
      return value.doubleValue();
    }
  }

  static Double getDouble(final Record record, final String fieldName) {
    final Number value = record.getValue(fieldName);
    if (value == null) {
      return null;
    } else if (value instanceof Double) {
      return (Double)value;
    } else {
      return value.doubleValue();
    }
  }

  @SuppressWarnings("unchecked")
  static <T> T getFieldByPath(final MapEx map, final String path) {
    if (map instanceof Record) {
      final Record record = (Record)map;
      return getFieldByPath(record, path);
    } else if (path == null) {
      return null;
    } else {

      final String[] propertyPath = path.split("\\.");
      Object propertyValue = map;
      for (int i = 0; i < propertyPath.length && propertyValue != null; i++) {
        final String propertyName = propertyPath[i];
        if (propertyValue instanceof Record) {
          final Record recordValue = (Record)propertyValue;

          if (recordValue.hasField(propertyName)) {
            propertyValue = getValue(recordValue, propertyName);
            if (propertyValue == null) {
              return null;
            }
          } else {
            return null;
          }
        } else if (propertyValue instanceof Map) {
          final Map<String, Object> map2 = (Map<String, Object>)propertyValue;
          propertyValue = map2.get(propertyName);
          if (propertyValue == null) {
            return null;
          }
        } else {
          try {
            final Object object = propertyValue;
            propertyValue = Property.getSimple(object, propertyName);
          } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("Path does not exist " + path, e);
          }
        }
      }
      return (T)propertyValue;
    }
  }

  @SuppressWarnings("unchecked")
  static <T> T getFieldByPath(final Record record, final String path) {
    if (path == null) {
      return null;
    } else {
      final RecordDefinition recordDefinition = record.getRecordDefinition();

      final String[] propertyPath = path.split("\\.");
      Object propertyValue = record;
      for (int i = 0; i < propertyPath.length && propertyValue != null; i++) {
        final String propertyName = propertyPath[i];
        if (propertyValue instanceof Record) {
          final Record recordValue = (Record)propertyValue;

          if (recordValue.hasField(propertyName)) {
            propertyValue = getValue(recordValue, propertyName);
            if (propertyValue == null) {
              return null;
            } else if (i + 1 < propertyPath.length) {
              final CodeTable codeTable = recordDefinition.getCodeTableByFieldName(propertyName);
              if (codeTable != null) {
                propertyValue = codeTable.getMap(Identifier.newIdentifier(propertyValue));
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
              propertyValue = codeTable.getMap(Identifier.newIdentifier(propertyValue));
            }
          }
        } else {
          try {
            final Object object = propertyValue;
            propertyValue = Property.getSimple(object, propertyName);
          } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("Path does not exist " + path, e);
          }
        }
      }
      return (T)propertyValue;
    }
  }

  static List<Geometry> getGeometries(final Collection<?> records) {
    final List<Geometry> geometries = new ArrayList<>();
    for (final Object record : records) {
      final Geometry geometry = unionGeometry(record);
      if (geometry != null) {
        geometries.add(geometry);
      }
    }
    return geometries;
  }

  static Geometry getGeometry(final Collection<?> records) {
    final List<Geometry> geometries = getGeometries(records);
    if (geometries.isEmpty()) {
      return GeometryFactory.DEFAULT_3D.geometry();
    } else {
      final GeometryFactory geometryFactory = geometries.get(0).getGeometryFactory();
      return geometryFactory.geometry(geometries);
    }
  }

  static <G extends Geometry> G getGeometry(final Record record) {
    if (record == null) {
      return null;
    } else {
      return record.getGeometry();
    }
  }

  static Set<Identifier> getIdentifiers(final Collection<? extends Record> records) {
    final Set<Identifier> identifiers = Identifier.newTreeSet();
    for (final Record record : records) {
      final Identifier identifier = record.getIdentifier();
      if (identifier != null) {
        identifiers.add(identifier);
      }
    }
    return identifiers;
  }

  static List<Identifier> getIdentifiers(final Record record, final Collection<String> fieldNames) {
    final List<Identifier> identifiers = new ArrayList<>();
    for (final String fieldName : fieldNames) {
      final Identifier identifier = record.getIdentifier(fieldName);
      if (Property.hasValue(identifier)) {
        identifiers.add(identifier);
      }
    }
    return identifiers;
  }

  static List<Identifier> getIdentifiers(final Record record, final String... fieldNames) {
    return getIdentifiers(record, Arrays.asList(fieldNames));
  }

  static Integer getInteger(final Record record, final String fieldName,
    final Integer defaultValue) {
    if (record == null) {
      return null;
    } else {
      final Number value = record.getValue(fieldName);
      if (value == null) {
        return defaultValue;
      } else if (value instanceof Integer) {
        return (Integer)value;
      } else {
        return value.intValue();
      }
    }
  }

  static Long getLong(final Record record, final String fieldName) {
    final Number value = record.getValue(fieldName);
    if (value == null) {
      return null;
    } else if (value instanceof Long) {
      return (Long)value;
    } else {
      return value.longValue();
    }
  }

  static int getMax(final int max, final Record record, final String fieldName) {
    if (record != null) {
      final Integer value = record.getInteger(fieldName);
      if (value != null) {
        if (value > max) {
          return value;
        }
      }
    }
    return max;
  }

  static int getMin(final int min, final Record record, final String fieldName) {
    if (record != null) {
      final Integer value = record.getInteger(fieldName);
      if (value != null) {
        if (value < min) {
          return value;
        }
      }
    }
    return min;
  }

  static Object getValue(final Record record, final String fieldName) {
    if (record == null || !Property.hasValue(fieldName)) {
      return null;
    } else {
      return record.getValue(fieldName);
    }
  }

  static void mergeStringListValue(final Map<String, Object> record, final Record record1,
    final Record record2, final String fieldName, final String separator) {
    final String value1 = record1.getString(fieldName);
    final String value2 = record2.getString(fieldName);
    mergeStringListValue(record, fieldName, value1, value2);
  }

  static void mergeStringListValue(final Map<String, Object> record, final String fieldName,
    final String value1, final String value2) {
    Object value;
    if (!Property.hasValue(value1)) {
      value = value2;
    } else if (!Property.hasValue(value2)) {
      value = value1;
    } else if (DataType.equal(value1, value2)) {
      value = value1;
    } else {
      final Set<String> values = new TreeSet<>(new StringNumberComparator());
      values.addAll(Lists.split(value1, ","));
      values.addAll(Lists.split(value2, ","));
      value = Strings.toString(values);
    }
    record.put(fieldName, value);
  }

  static void mergeValue(final Map<String, Object> record, final Record record1,
    final Record record2, final String fieldName, final String separator) {
    final String value1 = record1.getString(fieldName);
    final String value2 = record2.getString(fieldName);
    Object value;
    if (!Property.hasValue(value1)) {
      value = value2;
    } else if (!Property.hasValue(value2)) {
      value = value1;
    } else if (DataType.equal(value1, value2)) {
      value = value1;
    } else {
      value = value1 + separator + value2;
    }
    record.put(fieldName, value);
  }

  static Comparator<Record> newComparatorDistance(final Geometry geometry) {
    return (record1, record2) -> {
      if (record1 == record2) {
        return 0;
      } else {
        final double distance1 = record1.distance(geometry);
        final double distance2 = record2.distance(geometry);
        int compare = Double.compare(distance1, distance2);
        if (compare == 0) {
          compare = record1.compareTo(record2);
        }
        return compare;
      }
    };
  }

  static <R extends MapEx> Comparator<R> newComparatorOrderBy(
    final Map<QueryValue, Boolean> orderBy) {
    return (record1, record2) -> {
      if (record1 == record2) {
        return 0;
      } else {
        if (Property.hasValue(orderBy)) {
          for (final Entry<QueryValue, Boolean> entry : orderBy.entrySet()) {
            final QueryValue field = entry.getKey();
            if (field instanceof ColumnReference) {
              final ColumnReference column = (ColumnReference)field;
              final String fieldName = column.getAliasName();
              final Boolean ascending = entry.getValue();
              final Object value1 = record1.getValue(fieldName);
              final Object value2 = record2.getValue(fieldName);
              final int compare = CompareUtil.compare(value1, value2);
              if (compare != 0) {
                if (ascending) {
                  return compare;
                } else {
                  return -compare;
                }
              }
            }

          }
          return 0;
        } else {
          return 0;
        }
      }
    };
  }

  static <R extends Record> Comparator<R> newComparatorOrderByIdentifier(
    final Map<? extends CharSequence, Boolean> orderBy) {
    return (record1, record2) -> {
      if (record1 == record2) {
        return 0;
      } else {
        if (Property.hasValue(orderBy)) {
          for (final Entry<? extends CharSequence, Boolean> entry : orderBy.entrySet()) {
            final CharSequence fieldName = entry.getKey();
            final Boolean ascending = entry.getValue();
            final Object value1 = record1.getValue(fieldName);
            final Object value2 = record2.getValue(fieldName);
            final int compare = CompareUtil.compare(value1, value2);
            if (compare != 0) {
              if (ascending) {
                return compare;
              } else {
                return -compare;
              }
            }
          }
          final Identifier identifier1 = record1.getIdentifier();
          final Identifier identifier2 = record2.getIdentifier();
          return CompareUtil.compare(identifier1, identifier2);
        } else {
          return -1;
        }
      }
    };
  }

  static <V extends Record> Predicate<V> newFilter(final BoundingBox boundingBox) {
    return record -> {
      if (record != null) {
        try {
          final Geometry geometry = record.getGeometry();
          if (geometry != null) {
            return geometry.intersectsBbox(boundingBox);
          }
        } catch (final Throwable t) {
          Logs.debug(Records.class, "Invalid Geometry", t);
        }
      }
      return false;
    };
  }

  static <V extends Record> Predicate<V> newFilter(final Geometry geometry,
    final double maxDistance) {
    return record -> {
      if (record != null) {
        final Geometry recordGeometry = record.getGeometry();
        if (recordGeometry != null) {
          final double distance = recordGeometry.distanceGeometry(geometry, maxDistance);
          if (distance <= maxDistance) {
            return true;
          }
        }
      }
      return false;
    };
  }

  static <V extends Record> Predicate<V> newFilter(final String fieldName, final Object value) {
    return record -> {
      if (record != null) {
        final Object fieldValue = record.getValue(fieldName);
        return DataType.equal(fieldValue, value);
      }
      return false;
    };
  }

  static <R extends Record> Predicate<R> newFilterGeometryIntersects(final Geometry geometry) {
    if (Property.hasValue(geometry)) {
      final GeometryFactory geometryFactory = geometry.getGeometryFactory();
      return record -> {
        if (record != null) {
          try {
            final Geometry geometry2 = record.getGeometry();
            final Geometry convertedGeometry2 = geometry2.convertGeometry(geometryFactory);
            if (convertedGeometry2 != null) {
              try {
                return geometry.intersects(convertedGeometry2);
              } catch (final TopologyException e) {
                return true;
              }
            }
          } catch (final Throwable t) {
          }
        }
        return false;
      };
    } else {
      return Predicates.none();
    }
  }

  static RecordDefinition newGeometryRecordDefinition() {
    final FieldDefinition geometryField = new FieldDefinition("geometry",
      GeometryDataTypes.GEOMETRY, true);
    return new RecordDefinitionImpl(PathName.newPathName("/Feature"), geometryField);
  }

  static Record newRecord(final RecordDefinition recordDefinition,
    final Map<String, Object> values) {
    return new ArrayRecord(recordDefinition, values);
  }

  static List<Record> newRecords(final RecordDefinition recordDefinition,
    final Collection<? extends Map<String, Object>> list) {
    final List<Record> records = new ArrayList<>();
    for (final Map<String, Object> map : list) {
      final Record record = newRecord(recordDefinition, map);
      records.add(record);
    }
    return records;
  }

  static void removeDeleted(final Collection<? extends Record> records) {
    for (final Iterator<? extends Record> iterator = records.iterator(); iterator.hasNext();) {
      final Record record = iterator.next();
      if (record == null || record.getState() == RecordState.DELETED) {
        iterator.remove();
      }
    }
  }

  static void setValues(final Record target, final Record source,
    final Collection<String> fieldNames, final Collection<String> ignoreFieldNames) {
    for (final String fieldName : fieldNames) {
      if (!ignoreFieldNames.contains(fieldName)) {
        final Object oldValue = getValue(target, fieldName);
        Object newValue = getValue(source, fieldName);
        if (!DataType.equal(oldValue, newValue)) {
          newValue = BaseCloneable.clone(newValue);
          target.setValue(fieldName, newValue);
        }
      }
    }
  }

  static void toUpperCase(final MapEx record, final String fieldName) {
    if (record != null) {
      String value = record.getString(fieldName);
      if (value != null) {
        value = value.toUpperCase();
        record.addValue(fieldName, value);
      }
    }
  }

  static Geometry unionGeometry(final Collection<?> records) {
    final Geometry geometry = getGeometry(records);
    return geometry.union();
  }

  static Geometry unionGeometry(final Map<?, ?> map) {
    Geometry union = null;
    for (final Entry<?, ?> entry : map.entrySet()) {
      final Object key = entry.getKey();
      final Geometry keyGeometry = unionGeometry(key);
      if (keyGeometry != null) {
        union = keyGeometry.union(union);
      }
      final Object value = entry.getValue();
      final Geometry valueGeometry = unionGeometry(value);
      if (valueGeometry != null) {
        union = valueGeometry.union(union);
      }
    }
    return union;
  }

  static Geometry unionGeometry(final Object object) {
    if (object instanceof Geometry) {
      final Geometry geometry = (Geometry)object;
      return geometry;
    } else if (object instanceof Record) {
      final Record record = (Record)object;
      return record.getGeometry();
    } else if (object instanceof Collection) {
      final Collection<?> objects = (Collection<?>)object;
      return unionGeometry(objects);
    } else if (object instanceof Map) {
      final Map<?, ?> map = (Map<?, ?>)object;
      return unionGeometry(map);
    } else {
      return null;
    }
  }
}
