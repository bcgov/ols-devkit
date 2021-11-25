package com.revolsys.record.property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jeometry.common.data.type.DataType;

import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.model.End;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.record.FieldValueInvalidException;
import com.revolsys.record.Record;
import com.revolsys.record.Records;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;

public class DirectionalFields extends AbstractRecordDefinitionProperty {
  public static final String PROPERTY_NAME = DirectionalFields.class.getName() + ".propertyName";

  public static boolean canMergeRecords(final Point point, final Record record1,
    final Record record2) {
    final Set<String> excludes = Collections.emptySet();
    final DirectionalFields property = getProperty(record1);
    return property.canMerge(point, record1, record2, excludes);
  }

  public static boolean canMergeRecords(final Point point, final Record record1,
    final Record record2, final Set<String> equalExcludeFieldNames) {
    final DirectionalFields property = getProperty(record1);
    return property.canMerge(point, record1, record2, equalExcludeFieldNames);
  }

  public static void edgeSplitFieldValues(final LineString line, final Point point,
    final List<Edge<Record>> edges) {
    if (!edges.isEmpty()) {
      final Edge<Record> firstEdge = edges.get(0);
      final Record record = firstEdge.getObject();
      final DirectionalFields property = getProperty(record);
      property.setEdgeSplitFieldNames(line, point, edges);
    }
  }

  public static boolean equalsRecords(final Record record1, final Record record2) {
    final Set<String> excludes = Collections.emptySet();
    return equalsRecords(record1, record2, excludes);
  }

  public static boolean equalsRecords(final Record record1, final Record record2,
    final Collection<String> equalExcludeFieldNames) {
    final DirectionalFields property = getProperty(record1);
    return property.equals(record1, record2, equalExcludeFieldNames);
  }

  public static Set<String> getCantMergeFieldNamesRecords(final Point point, final Record record1,
    final Record record2, final Set<String> equalExcludeFieldNames) {
    final DirectionalFields property = getProperty(record1);
    return property.getCantMergeFieldNames(point, record1, record2, equalExcludeFieldNames);
  }

  public static DirectionalFields getProperty(final RecordDefinition recordDefinition) {
    DirectionalFields property = recordDefinition.getProperty(PROPERTY_NAME);
    if (property == null) {
      property = new DirectionalFields();
      property.setRecordDefinition(recordDefinition);
    }
    return property;
  }

  public static DirectionalFields getProperty(final RecordDefinitionProxy proxy) {
    final RecordDefinition recordDefinition = proxy.getRecordDefinition();
    return getProperty(recordDefinition);
  }

  public static Record getReverseRecord(final Record record) {
    final DirectionalFields property = getProperty(record);
    final Record reverse = property.getReverse(record);
    return reverse;
  }

  public static boolean hasProperty(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    return recordDefinition.getProperty(PROPERTY_NAME) != null;
  }

  public static Record merge(final Point point, final Record record1, final Record record2) {
    final DirectionalFields property = getProperty(record1);
    return property.getMergedRecord(point, record1, record2);
  }

  public static Record merge(final Record record1, final Record record2) {
    final DirectionalFields property = getProperty(record1);
    return property.getMergedRecord(record1, record2);
  }

  public static Record mergeLongest(final Point point, final Record record1, final Record record2) {
    final DirectionalFields property = getProperty(record1);
    return property.getMergedRecordReverseLongest(point, record1, record2);
  }

  public static Record mergeLongest(final Record record1, final Record record2) {
    final DirectionalFields property = getProperty(record1);
    return property.getMergedRecordReverseLongest(record1, record2);
  }

  public static void reverseFieldValuesRecord(final Record record) {
    final DirectionalFields property = getProperty(record);
    final Map<String, Object> map = record;
    property.reverseFieldValues(map);
  }

  public static void reverseGeometryRecord(final Record record) {
    final DirectionalFields property = getProperty(record);
    final Map<String, Object> map = record;
    property.reverseGeometry(map);
  }

  public static void reverseRecord(final Record record) {
    final DirectionalFields property = getProperty(record);
    property.reverseFieldValuesAndGeometry(record);
  }

  private final Map<String, Map<Object, Object>> directionalFieldValues = new HashMap<>();

  private final List<List<String>> endAndSideFieldNamePairs = new ArrayList<>();

  private final Map<String, String> endFieldNamePairs = new HashMap<>();

  private final Set<String> fromFieldNames = new HashSet<>();

  private final Map<String, String> reverseFieldNameMap = new HashMap<>();

  private final Map<String, String> sideFieldNamePairs = new HashMap<>();

  private final Set<String> sideFieldNames = new HashSet<>();

  private final Set<String> toFieldNames = new HashSet<>();

  public DirectionalFields() {
  }

  public void addDirectionalFieldValues(final String fieldName,
    final Map<? extends Object, ? extends Object> values) {
    final Map<Object, Object> newValues = new LinkedHashMap<>();
    for (final Entry<? extends Object, ? extends Object> entry : values.entrySet()) {
      final Object value1 = entry.getKey();
      final Object value2 = entry.getValue();
      addValue(newValues, value1, value2);
      addValue(newValues, value2, value1);
    }
    this.directionalFieldValues.put(fieldName, newValues);
  }

  public void addEndAndSideFieldNamePairs(final String startLeftFieldName,
    final String startRightFieldName, final String endLeftFieldName,
    final String endRightFieldName) {
    this.endAndSideFieldNamePairs.add(
      Arrays.asList(startLeftFieldName, startRightFieldName, endLeftFieldName, endRightFieldName));
    addEndFieldNamePairInternal(startLeftFieldName, endLeftFieldName);
    addEndFieldNamePairInternal(startRightFieldName, endRightFieldName);
    addFieldNamePair(this.reverseFieldNameMap, startLeftFieldName, endRightFieldName);
    addFieldNamePair(this.reverseFieldNameMap, endLeftFieldName, startRightFieldName);
  }

  public void addEndFieldNamePair(final String startFieldName, final String endFieldName) {
    addEndFieldNamePairInternal(startFieldName, endFieldName);
    addFieldNamePair(this.reverseFieldNameMap, startFieldName, endFieldName);
  }

  private void addEndFieldNamePairInternal(final String startFieldName, final String endFieldName) {
    addFieldNamePair(this.endFieldNamePairs, startFieldName, endFieldName);
    this.fromFieldNames.add(startFieldName);
    this.toFieldNames.add(endFieldName);
  }

  /**
   * Add a mapping from the fromFieldName to the toFieldName and an
   * inverse mapping to the namePairs map.
   *
   * @param namePairs The name pair mapping.
   * @param fromFieldName The from attribute name.
   * @param toFieldName The to attribute name.
   */
  private void addFieldNamePair(final Map<String, String> namePairs, final String fromFieldName,
    final String toFieldName) {
    final String fromPair = namePairs.get(fromFieldName);
    if (fromPair == null) {
      final String toPair = namePairs.get(toFieldName);
      if (toPair == null) {
        namePairs.put(fromFieldName, toFieldName);
        namePairs.put(toFieldName, fromFieldName);
      } else if (toPair.equals(fromFieldName)) {
        throw new IllegalArgumentException(
          "Cannot override mapping " + toFieldName + "=" + toPair + " to " + fromFieldName);
      }
    } else if (fromPair.equals(toFieldName)) {
      throw new IllegalArgumentException(
        "Cannot override mapping " + fromFieldName + "=" + fromPair + " to " + toFieldName);
    }
  }

  public void addSideFieldNamePair(final String leftFieldName, final String rightFieldName) {
    addFieldNamePair(this.sideFieldNamePairs, leftFieldName, rightFieldName);
    this.sideFieldNames.add(leftFieldName);
    this.sideFieldNames.add(rightFieldName);
    addFieldNamePair(this.reverseFieldNameMap, leftFieldName, rightFieldName);
  }

  protected void addValue(final Map<Object, Object> map, final Object key, final Object value) {
    final Object oldValue = map.get(key);
    if (oldValue != null && !oldValue.equals(value)) {
      throw new IllegalArgumentException(
        "Cannot override mapping " + key + "=" + oldValue + " with " + value);
    }
    map.put(key, value);
  }

  public boolean canMerge(final Point point, final Record record1, final Record record2,
    final Collection<String> equalExcludeFieldNames) {
    final End[] lineEnds = getLineEndsAtPoint(point, record1, record2);

    if (lineEnds != null) {
      final RecordDefinition recordDefinition = getRecordDefinition();
      final EqualIgnoreFieldNames equalIgnore = EqualIgnoreFieldNames.getProperty(recordDefinition);
      for (final String fieldName : recordDefinition.getFieldNames()) {
        if (!record1.isFieldExcluded(equalExcludeFieldNames, fieldName)
          && !equalIgnore.isFieldIgnored(fieldName)) {
          if (!canMerge(fieldName, point, record1, record2, equalExcludeFieldNames, lineEnds)) {
            return false;
          }
        }
      }
      return true;
    } else {
      return false;
    }
  }

  public boolean canMerge(final String fieldName, final Point point, final Record record1,
    final Record record2, final Collection<String> equalExcludeFieldNames, final End[] lineEnds) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (fieldName.equals(recordDefinition.getGeometryFieldName())) {
      final LineString line1 = record1.getGeometry();
      final LineString line2 = record2.getGeometry();
      return !line1.equals(line2);
    }
    if (lineEnds == null) {
      return false;
    } else {
      final End line1End = lineEnds[0];
      final End line2End = lineEnds[1];
      if (hasDirectionalFieldValues(fieldName)) {
        if (line1End != line2End) {
          final Object value1 = record1.getValue(fieldName);
          final Object value2 = getDirectionalFieldValue(record2, fieldName);
          if (DataType.equal(value1, value2, equalExcludeFieldNames)) {
            return true;
          } else {
            return false;
          }
        }
      } else if (isFromField(fieldName)) {
        return canMergeFromField(fieldName, record1, line1End, record2, line2End,
          equalExcludeFieldNames);
      } else if (isToField(fieldName)) {
        return canMergeToField(fieldName, record1, line1End, record2, line2End,
          equalExcludeFieldNames);
      } else if (isSideField(fieldName)) {
        if (line1End == line2End) {
          final String oppositeFieldName = getSideFieldName(fieldName);
          if (oppositeFieldName == null) { // only check the pair once
            return true;
          } else {
            return equals(record1, fieldName, record2, oppositeFieldName, equalExcludeFieldNames);
          }
        }
      }
      return equals(record1, fieldName, record2, fieldName, equalExcludeFieldNames);
    }
  }

  protected boolean canMergeFromField(final String startFieldName, final Record record1,
    final End line1End, final Record record2, final End line2End,
    final Collection<String> equalExcludeFieldNames) {
    final String endFieldName = this.endFieldNamePairs.get(startFieldName);
    if (line1End.isTo()) {
      if (line2End.isFrom()) {
        // -->*-->
        return isNull(record1, endFieldName, record2, startFieldName, equalExcludeFieldNames);
      } else {
        // -->*<--
        return true;
      }
    } else {
      if (line2End.isFrom()) {
        // <--*-->
        return isNull(record1, startFieldName, record2, startFieldName, equalExcludeFieldNames);
      } else {
        // <--*<--
        return isNull(record1, startFieldName, record2, endFieldName, equalExcludeFieldNames);
      }
    }
  }

  protected boolean canMergeToField(final String endFieldName, final Record record1,
    final End line1End, final Record record2, final End line2End,
    final Collection<String> equalExcludeFieldNames) {
    final String startFieldName = this.endFieldNamePairs.get(endFieldName);
    if (line1End.isTo()) {
      if (line2End.isFrom()) {
        // -->*-->
        return isNull(record1, endFieldName, record2, startFieldName, equalExcludeFieldNames);
      } else {
        // -->*<--
        return isNull(record1, endFieldName, record2, endFieldName, equalExcludeFieldNames);
      }
    } else {
      if (line2End.isFrom()) {
        // <--*-->
        return true;
      } else {
        // <--*<--
        return isNull(record1, startFieldName, record2, endFieldName, equalExcludeFieldNames);
      }
    }
  }

  public void clearFromFields(final Map<String, Object> record) {
    for (final String fieldName : this.fromFieldNames) {
      record.put(fieldName, null);
    }
  }

  public void clearToFields(final Map<String, Object> record) {
    for (final String fieldName : this.toFieldNames) {
      record.put(fieldName, null);
    }
  }

  public boolean equals(final Record record1, final Record record2,
    final Collection<String> equalExcludeFieldNames) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final EqualIgnoreFieldNames equalIgnore = EqualIgnoreFieldNames.getProperty(recordDefinition);
    for (final String fieldName : recordDefinition.getFieldNames()) {
      if (!equalExcludeFieldNames.contains(fieldName) && !equalIgnore.isFieldIgnored(fieldName)) {
        if (!equals(fieldName, record1, record2, equalExcludeFieldNames)) {
          return false;
        }
      }
    }
    return true;
  }

  protected boolean equals(final Record record1, final String name1, final Record record2,
    final String name2, final Collection<String> equalExcludeFieldNames) {
    final Object value1 = record1.getValue(name1);
    final Object value2 = record2.getValue(name2);
    if (DataType.equal(value1, value2, equalExcludeFieldNames)) {
      return true;
    } else {
      return false;
    }
  }

  protected boolean equals(final String fieldName, final Record record1, final Record record2,
    final Collection<String> equalExcludeFieldNames) {
    final LineString line1 = record1.getGeometry();
    final LineString line2 = record2.getGeometry();
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (fieldName.equals(recordDefinition.getGeometryFieldName())) {
      return line1.equals(line2);
    }

    boolean reverseEquals;
    if (line1.equalsVertex(2, 0, line2, 0)) {
      if (line1.isClosed()) {
        // TODO handle loops
        throw new IllegalArgumentException("Cannot handle loops");
      }
      reverseEquals = false;
    } else {
      reverseEquals = true;
    }
    if (reverseEquals) {
      return equalsReverse(fieldName, record1, record2, equalExcludeFieldNames);
    } else {
      return equals(record1, fieldName, record2, fieldName, equalExcludeFieldNames);
    }
  }

  private boolean equalsReverse(final String fieldName, final Record record1, final Record record2,
    final Collection<String> equalExcludeFieldNames) {
    if (hasDirectionalFieldValues(fieldName)) {
      final Object value1 = record1.getValue(fieldName);
      final Object value2 = getDirectionalFieldValue(record2, fieldName);
      if (DataType.equal(value1, value2, equalExcludeFieldNames)) {
        return true;
      } else {
        return false;
      }
    } else {
      final String reverseFieldName = getReverseFieldName(fieldName);
      if (reverseFieldName == null) {
        return equals(record1, fieldName, record2, fieldName, equalExcludeFieldNames);
      } else {
        return equals(record1, fieldName, record2, reverseFieldName, equalExcludeFieldNames);
      }
    }
  }

  public Set<String> getCantMergeFieldNames(final Point point, final Record record1,
    final Record record2, final Collection<String> equalExcludeFieldNames) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final End[] lineEnds = getLineEndsAtPoint(point, record1, record2);
    if (lineEnds != null) {
      final Set<String> fieldNames = new LinkedHashSet<>();
      final EqualIgnoreFieldNames equalIgnore = EqualIgnoreFieldNames.getProperty(recordDefinition);
      for (final String fieldName : recordDefinition.getFieldNames()) {
        if (!equalExcludeFieldNames.contains(fieldName) && !equalIgnore.isFieldIgnored(fieldName)) {
          if (!canMerge(fieldName, point, record1, record2, equalExcludeFieldNames, lineEnds)) {
            fieldNames.add(fieldName);
          }
        }
      }
      return fieldNames;
    } else {
      final String geometryFieldName = recordDefinition.getGeometryFieldName();
      return Collections.singleton(geometryFieldName);
    }
  }

  protected Object getDirectionalFieldValue(final Map<String, ? extends Object> record,
    final String fieldName) {
    final Object value = record.get(fieldName);

    final Map<Object, Object> valueMap = this.directionalFieldValues.get(fieldName);
    if (valueMap != null) {
      if (valueMap.containsKey(value)) {
        final Object directionalValue = valueMap.get(value);
        return directionalValue;
      }
    }
    return value;
  }

  public Map<String, Map<Object, Object>> getDirectionalFieldValues() {
    return this.directionalFieldValues;
  }

  public List<List<String>> getEndAndSideFieldNamePairs() {
    return this.endAndSideFieldNamePairs;
  }

  public Map<String, String> getEndFieldNamePairs() {
    return this.endFieldNamePairs;
  }

  public Set<String> getFromFieldNames() {
    return this.fromFieldNames;
  }

  protected End getLineEnd(final Record record, final Point point) {
    final LineString line = record.getGeometry();
    return line.getEnd(point);
  }

  protected End[] getLineEndsAtPoint(final Point point, final Record record1,
    final Record record2) {
    final LineString line1 = record1.getGeometry();
    final LineString line2 = record2.getGeometry();

    final End end1 = line1.getEnd(point);
    final End end2 = line2.getEnd(point);
    if (end1 != End.NONE && end2 != End.NONE) {
      return new End[] {
        end1, end2
      };
    }
    return null;
  }

  public Map<String, Object> getMergedMap(final Point point, final Record record1, Record record2) {
    final LineString line1 = record1.getGeometry();
    LineString line2 = record2.getGeometry();

    Record fromRecord;
    Record toRecord;

    LineString newLine;

    final Vertex line1From = line1.getVertex(0);
    final Vertex line2From = line2.getVertex(0);
    if (line1From.equals(2, line2From) && line1From.equals(2, point)) {
      record2 = getReverse(record2);
      line2 = record2.getGeometry();
      fromRecord = record2;
      toRecord = record1;
      newLine = line1.merge(point, line2);
    } else {
      final Vertex line1To = line1.getToVertex(0);
      final Vertex line2To = line2.getToVertex(0);
      if (line1To.equals(2, line2To) && line1To.equals(2, point)) {
        record2 = getReverse(record2);
        line2 = record2.getGeometry();
        fromRecord = record1;
        toRecord = record2;
        newLine = line1.merge(point, line2);
      } else if (line1To.equals(2, line2From) && line1To.equals(2, point)) {
        fromRecord = record1;
        toRecord = record2;
        newLine = line1.merge(point, line2);
      } else if (line1From.equals(2, line2To) && line1From.equals(2, point)) {
        fromRecord = record2;
        toRecord = record1;
        newLine = line2.merge(point, line1);
      } else {
        throw new IllegalArgumentException("Lines for records don't touch");
      }
    }

    final Map<String, Object> newValues = new LinkedHashMap<>(record1);
    setFromFieldValues(fromRecord, toRecord, newValues);
    setToFieldValues(toRecord, fromRecord, newValues);
    final RecordDefinition recordDefinition = record1.getRecordDefinition();
    final String geometryFieldName = recordDefinition.getGeometryFieldName();
    newValues.put(geometryFieldName, newLine);
    return newValues;
  }

  /**
   * Get a new record that is the result of merging the two records. The
   * attributes will be taken from the record with the longest length. If one
   * line needs to be reversed then the second record will be reversed.
   *
   * @param record1
   * @param record2
   * @return
   */
  public Record getMergedRecord(final Point point, final Record record1, Record record2) {
    final LineString line1 = record1.getGeometry();
    LineString line2 = record2.getGeometry();

    Record fromRecord;
    Record toRecord;

    final boolean line1Longer = line1.getLength() > line2.getLength();
    LineString newLine;
    final int lastPoint1 = line1.getVertexCount() - 1;
    final int lastPoint2 = line2.getVertexCount() - 1;

    if (line1.equalsVertex(2, 0, line2, 0) && line1.equalsVertex(2, 0, point)) {
      record2 = getReverse(record2);
      line2 = record2.getGeometry();
      fromRecord = record2;
      toRecord = record1;
      newLine = line1.merge(point, line2);
    } else if (line1.equalsVertex(2, lastPoint1, line2, lastPoint2)
      && line1.equalsVertex(2, lastPoint1, point)) {
      record2 = getReverse(record2);
      line2 = record2.getGeometry();
      fromRecord = record1;
      toRecord = record2;
      newLine = line1.merge(point, line2);
    } else if (line1.equalsVertex(2, lastPoint1, line2, 0)
      && line1.equalsVertex(2, lastPoint1, point)) {
      fromRecord = record1;
      toRecord = record2;
      newLine = line1.merge(point, line2);
    } else if (line1.equalsVertex(2, 0, line2, lastPoint2) && line1.equalsVertex(2, 0, point)) {
      fromRecord = record2;
      toRecord = record1;
      newLine = line2.merge(point, line1);
    } else {
      throw new IllegalArgumentException("Lines for records don't touch");
    }

    Record newRecord;
    if (line1Longer) {
      newRecord = Records.copy(record1, newLine);
    } else {
      newRecord = Records.copy(record2, newLine);
    }
    setFromFieldValues(fromRecord, toRecord, newRecord);
    setToFieldValues(toRecord, fromRecord, newRecord);
    LengthFieldName.setRecordLength(newRecord);
    return newRecord;
  }

  /**
   * Get a new record that is the result of merging the two records. The
   * attributes will be taken from the record with the longest length. If one
   * line needs to be reversed then the second record will be reversed.
   *
   * @param record1
   * @param record2
   * @return
   */
  public Record getMergedRecord(final Record record1, Record record2) {
    final LineString line1 = record1.getGeometry();
    final int vertexCount1 = line1.getVertexCount();
    LineString line2 = record2.getGeometry();
    final int vertexCount2 = line2.getVertexCount();

    Record fromRecord;
    Record toRecord;

    final boolean line1Longer = line1.getLength() > line2.getLength();
    LineString newLine;

    if (line1.equalsVertex(2, 0, line2, 0)) {
      record2 = getReverse(record2);
      line2 = record2.getGeometry();
      fromRecord = record2;
      toRecord = record1;
      newLine = line1.merge(line2);
    } else if (line1.equalsVertex(2, vertexCount1 - 1, line2, vertexCount2 - 1)) {
      record2 = getReverse(record2);
      line2 = record2.getGeometry();
      fromRecord = record1;
      toRecord = record2;
      newLine = line1.merge(line2);
    } else if (line1.equalsVertex(2, vertexCount1 - 1, line2, 0)) {
      fromRecord = record1;
      toRecord = record2;
      newLine = line1.merge(line2);
    } else if (line1.equalsVertex(2, 0, line2, vertexCount2 - 1)) {
      fromRecord = record2;
      toRecord = record1;
      newLine = line2.merge(line1);
    } else {
      throw new IllegalArgumentException("Lines for records don't touch");
    }

    Record newRecord;
    if (line1Longer) {
      newRecord = Records.copy(record1, newLine);
    } else {
      newRecord = Records.copy(record2, newLine);
    }
    setFromFieldValues(fromRecord, toRecord, newRecord);
    setToFieldValues(toRecord, fromRecord, newRecord);
    LengthFieldName.setRecordLength(newRecord);
    return newRecord;
  }

  public Record getMergedRecordReverseLongest(final Point point, final Record record1,
    final Record record2) {
    final LineString line1 = record1.getGeometry();
    final LineString line2 = record2.getGeometry();
    if (line1.getLength() >= line2.getLength()) {
      return getMergedRecord(point, record1, record2);
    } else {
      return getMergedRecord(point, record2, record1);
    }
  }

  /**
   * Get a new record that is the result of merging the two records. The
   * attributes will be taken from the record with the longest length. If one
   * line needs to be reversed then the longest will be reversed.
   *
   * @param record1
   * @param record2
   * @return
   */
  public Record getMergedRecordReverseLongest(final Record record1, final Record record2) {
    final LineString line1 = record1.getGeometry();
    final LineString line2 = record2.getGeometry();
    if (line1.getLength() >= line2.getLength()) {
      return getMergedRecord(record1, record2);
    } else {
      return getMergedRecord(record2, record1);
    }
  }

  @Override
  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  public Record getReverse(final Record record) {
    final Record reverse = record.clone();
    reverseFieldValuesAndGeometry(reverse);
    return reverse;
  }

  public String getReverseFieldName(final String fieldName) {
    return this.reverseFieldNameMap.get(fieldName);
  }

  public Map<String, Object> getReverseFieldValues(final Map<String, Object> record) {
    final Map<String, Object> reverse = new LinkedHashMap<>(record);
    for (final Entry<String, String> pair : this.reverseFieldNameMap.entrySet()) {
      final String fromFieldName = pair.getKey();
      final String toFieldName = pair.getValue();
      final Object toValue = record.get(toFieldName);
      reverse.put(fromFieldName, toValue);
    }
    for (final String fieldName : this.directionalFieldValues.keySet()) {
      final Object value = getDirectionalFieldValue(record, fieldName);
      reverse.put(fieldName, value);
    }
    return reverse;
  }

  public Map<String, Object> getReverseFieldValuesAndGeometry(final Map<String, Object> record) {
    final Map<String, Object> reverse = getReverseFieldValues(record);
    final String geometryFieldName = getRecordDefinition().getGeometryFieldName();
    if (geometryFieldName != null) {
      final Geometry geometry = getReverseLine(record);
      reverse.put(geometryFieldName, geometry);
    }
    return reverse;
  }

  public Map<String, Object> getReverseGeometry(final Map<String, Object> record) {
    final Map<String, Object> reverse = new LinkedHashMap<>(record);
    final String geometryFieldName = getRecordDefinition().getGeometryFieldName();
    if (geometryFieldName != null) {
      final Geometry geometry = getReverseLine(record);
      reverse.put(geometryFieldName, geometry);
    }
    return reverse;
  }

  protected Geometry getReverseLine(final Map<String, Object> record) {
    final String geometryFieldName = getRecordDefinition().getGeometryFieldName();
    final LineString line = (LineString)record.get(geometryFieldName);
    if (line == null) {
      return null;
    } else {
      final LineString reverseLine = line.reverse();
      return reverseLine;
    }
  }

  protected String getSideFieldName(final String fieldName) {
    return this.sideFieldNamePairs.get(fieldName);
  }

  public Map<String, String> getSideFieldNamePairs() {
    return this.sideFieldNamePairs;
  }

  public Set<String> getToFieldNames() {
    return this.toFieldNames;
  }

  public boolean hasDirectionalFields() {
    return !this.directionalFieldValues.isEmpty() || !this.reverseFieldNameMap.isEmpty();
  }

  public boolean hasDirectionalFieldValues(final String fieldName) {
    return this.directionalFieldValues.containsKey(fieldName);
  }

  public boolean isFromField(final String fieldName) {
    return this.fromFieldNames.contains(fieldName);
  }

  protected boolean isNull(final Record record1, final String name1, final Record record2,
    final String name2, final Collection<String> equalExcludeFieldNames) {
    final Object value1 = record1.getValue(name1);
    final Object value2 = record2.getValue(name2);
    if (value1 == null && value2 == null) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isSideField(final String fieldName) {
    return this.sideFieldNames.contains(fieldName);
  }

  public boolean isToField(final String fieldName) {
    return this.toFieldNames.contains(fieldName);
  }

  public Map<String, Object> newSplitValues(final Record oldRecord, final LineString oldLine,
    final Point splitPoint, final LineString newLine) {
    final Map<String, Object> newValues = Maps.newLinkedHash(oldRecord);
    final String geometryFieldName = oldRecord.getGeometryFieldName();
    newValues.put(geometryFieldName, newLine);
    setSplitFieldValues(oldLine, splitPoint, newValues, newLine);
    return newValues;
  }

  public void reverseFieldValues(final Map<String, Object> record) {
    final Map<String, Object> reverseFieldValues = getReverseFieldValues(record);
    record.putAll(reverseFieldValues);
  }

  public void reverseFieldValuesAndGeometry(final Map<String, Object> record) {
    final Map<String, Object> reverseFieldValues = getReverseFieldValuesAndGeometry(record);
    record.putAll(reverseFieldValues);
  }

  public void reverseGeometry(final Map<String, Object> record) {
    final Map<String, Object> reverseFieldValues = getReverseGeometry(record);
    record.putAll(reverseFieldValues);

  }

  public void setDirectionalFieldValues(
    final Map<String, Map<Object, Object>> directionalFieldValuesValues) {
    for (final Entry<String, Map<Object, Object>> entry : directionalFieldValuesValues.entrySet()) {
      final String fieldName = entry.getKey();
      final Map<Object, Object> values = entry.getValue();
      addDirectionalFieldValues(fieldName, values);
    }
  }

  public void setEdgeSplitFieldNames(final LineString line, final Point point,
    final List<Edge<Record>> edges) {
    for (final Edge<Record> edge : edges) {
      final Record record = edge.getObject();
      setSplitFieldValues(record, line, point);
    }
  }

  public void setEndAndSideFieldNamePairs(final List<List<String>> endAndSideFieldNamePairs) {
    for (final List<String> endAndSideFieldNamePair : endAndSideFieldNamePairs) {
      final String startLeftFieldName = endAndSideFieldNamePair.get(0);
      final String startRightFieldName = endAndSideFieldNamePair.get(1);
      final String endLeftFieldName = endAndSideFieldNamePair.get(2);
      final String endRightFieldName = endAndSideFieldNamePair.get(3);
      addEndAndSideFieldNamePairs(startLeftFieldName, startRightFieldName, endLeftFieldName,
        endRightFieldName);
    }
  }

  public void setEndFieldNamePairs(final Map<String, String> fieldNamePairs) {
    this.endFieldNamePairs.clear();
    this.toFieldNames.clear();
    this.fromFieldNames.clear();
    for (final Entry<String, String> pair : fieldNamePairs.entrySet()) {
      final String from = pair.getKey();
      final String to = pair.getValue();
      addEndFieldNamePair(from, to);
    }
  }

  public void setFromFieldValues(final Record source1, final Record source2,
    final Map<String, Object> newRecord) {
    for (final String fieldName : this.fromFieldNames) {
      Object value = source1.getValue(fieldName);
      if (value == null) {
        value = source2.getValue(fieldName);
      }
      newRecord.put(fieldName, value);
    }
  }

  public void setSideFieldNamePairs(final Map<String, String> fieldNamePairs) {
    this.sideFieldNamePairs.clear();
    for (final Entry<String, String> pair : fieldNamePairs.entrySet()) {
      final String from = pair.getKey();
      final String to = pair.getValue();
      addSideFieldNamePair(from, to);
    }
  }

  public void setSplitFieldValues(final LineString oldLine, final Point splitPoint,
    final Map<String, Object> newRecord, final LineString newLine) {
    final boolean firstPoint = newLine.equalsVertex(2, 0, splitPoint);
    final boolean toPoint = newLine.equalsVertex(2, newLine.getLastVertexIndex(), splitPoint);
    if (firstPoint) {
      if (!toPoint) {
        clearFromFields(newRecord);
      }
    } else if (toPoint) {
      clearToFields(newRecord);
    }
  }

  public void setSplitFieldValues(final Record newRecord, final LineString oldLine,
    final Point splitPoint) {
    final LineString newLine = newRecord.getGeometry();
    if (newLine != null) {
      setSplitFieldValues(oldLine, splitPoint, newRecord, newLine);
    }
  }

  public void setToFieldValues(final Record toRecord, final Record fromRecord,
    final Map<String, Object> newValues) {
    for (final String fieldName : this.toFieldNames) {
      Object value = toRecord.getValue(fieldName);
      if (value == null) {
        value = fromRecord.getValue(fieldName);
      }
      newValues.put(fieldName, value);
    }
  }

  @Override
  public String toString() {
    return "DirectionalFields";
  }

  public void validateFieldAtMergeEnd(final Map<String, Object> mergedValues,
    final String fieldName, final Object value) {
    if (isFromField(fieldName) || isToField(fieldName)) {
      if (value != null) {
        throw new FieldValueInvalidException(fieldName, value, value + " != null");
      }
    }
  }
}
