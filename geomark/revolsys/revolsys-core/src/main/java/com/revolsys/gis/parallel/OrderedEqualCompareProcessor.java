package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.MultiInputSelector;
import com.revolsys.parallel.channel.store.Buffer;
import com.revolsys.parallel.process.AbstractInProcess;
import com.revolsys.record.Record;
import com.revolsys.record.RecordLog;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public class OrderedEqualCompareProcessor extends AbstractInProcess<Record> {

  private List<String> equalExclude = new ArrayList<>();

  private String fieldName;

  private final Set<String> fieldNames = new TreeSet<>();

  private Channel<Record> otherIn;

  private int otherInBufferSize = 0;

  private String otherName = "Other";

  private RecordDefinition recordDefinition1;

  private RecordDefinition recordDefinition2;

  private boolean running;

  private String sourceName = "Source";

  private boolean equals(final Geometry geometry1, final Geometry geometry2) {
    if (geometry1 == null) {
      return geometry2 == null;
    } else if (geometry2 == null) {
      return false;
    } else if (geometry1.getClass() == geometry2.getClass()) {
      if (geometry1.isGeometryCollection()) {
        if (geometry1.getGeometryCount() == geometry2.getGeometryCount()) {
          for (int i = 0; i < geometry1.getGeometryCount(); i++) {
            final Geometry subGeometry1 = geometry1.getGeometry(i);
            final Geometry subGeometry2 = geometry2.getGeometry(i);
            if (!equals(subGeometry1, subGeometry2)) {
              return false;
            }
          }
          return true;
        } else {
          return false;
        }
      } else {
        return geometry1.equals(geometry1.getAxisCount(), geometry2);
      }
    } else {
      return false;
    }
  }

  protected boolean geometryEquals(final Record object1, final Record object2) {
    final Geometry geometry1 = object1.getGeometry();
    final Geometry geometry2 = object2.getGeometry();

    return equals(geometry1, geometry2);
  }

  public List<String> getEqualExclude() {
    return this.equalExclude;
  }

  public String getFieldName() {
    return this.fieldName;
  }

  protected Set<String> getNotEqualFieldNames(final Record object1, final Record object2) {
    final Set<String> notEqualFieldNames = new LinkedHashSet<>();
    final String geometryFieldName1 = this.recordDefinition1.getGeometryFieldName();
    final String geometryFieldName2 = this.recordDefinition2.getGeometryFieldName();
    for (final String fieldName : this.fieldNames) {
      if (!this.equalExclude.contains(fieldName) && !fieldName.equals(geometryFieldName1)
        && !fieldName.equals(geometryFieldName2)) {
        final Object value1 = object1.getValue(fieldName);
        final Object value2 = object2.getValue(fieldName);
        if (!valueEquals(value1, value2)) {
          notEqualFieldNames.add(fieldName);
        }
      }
    }
    return notEqualFieldNames;
  }

  /**
   * @return the in
   */
  public Channel<Record> getOtherIn() {
    if (this.otherIn == null) {
      if (this.otherInBufferSize < 1) {
        setOtherIn(new Channel<Record>());
      } else {
        final Buffer<Record> buffer = new Buffer<>(this.otherInBufferSize);
        setOtherIn(new Channel<>(buffer));
      }
    }
    return this.otherIn;
  }

  public int getOtherInBufferSize() {
    return this.otherInBufferSize;
  }

  public String getOtherName() {
    return this.otherName;
  }

  public String getSourceName() {
    return this.sourceName;
  }

  private void initAttributes() {
    final List<String> fieldNames1 = new ArrayList<>(this.recordDefinition1.getFieldNames());
    final List<String> fieldNames2 = new ArrayList<>(this.recordDefinition2.getFieldNames());
    this.fieldNames.addAll(fieldNames1);
    this.fieldNames.retainAll(fieldNames2);
    fieldNames1.removeAll(this.fieldNames);
    fieldNames1.remove(this.recordDefinition1.getGeometryFieldName());
    if (!fieldNames1.isEmpty()) {
      Logs.error(this, "Extra columns in file 1: " + fieldNames1);
    }
    fieldNames2.removeAll(this.fieldNames);
    fieldNames2.remove(this.recordDefinition2.getGeometryFieldName());
    if (!fieldNames2.isEmpty()) {
      Logs.error(this, "Extra columns in file 2: " + fieldNames2);
    }
  }

  protected void logNoMatch(final Record record, final boolean other) {
    if (other) {
      RecordLog.error(getClass(), this.otherName + " has no match in " + this.sourceName, record);
    } else {
      RecordLog.error(getClass(), this.sourceName + " has no match in " + this.otherName, record);
    }
  }

  private void logNoMatch(final Record[] objects, final Channel<Record> channel,
    final boolean other) {
    if (objects[0] != null) {
      logNoMatch(objects[0], false);
    }
    if (objects[1] != null) {
      logNoMatch(objects[1], true);
    }
    while (this.running) {
      final Record object = readObject(channel);
      logNoMatch(object, other);
    }
  }

  protected void logNotEqual(final Record sourceRecord, final Record otherRecord,
    final Set<String> notEqualFieldNames, final boolean geometryEquals) {
    final String fieldNames = Strings.toString(",", notEqualFieldNames);
    RecordLog.error(getClass(), this.sourceName + " " + fieldNames, sourceRecord);
    RecordLog.error(getClass(), this.otherName + " " + fieldNames, otherRecord);
  }

  protected Record readObject(final Channel<Record> channel) {
    return channel.read();
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  @Override
  protected void run(final Channel<Record> in) {
    this.running = true;
    final Channel<Record>[] channels = new Channel[] {
      in, this.otherIn
    };
    Record previousEqualObject = null;

    final Record[] objects = new Record[2];
    final boolean[] guard = new boolean[] {
      true, true
    };
    final MultiInputSelector alt = new MultiInputSelector();
    while (this.running) {
      final int index = alt.select(channels, guard);
      if (index == -1) {
        if (in.isClosed()) {
          logNoMatch(objects, this.otherIn, true);
          return;
        } else if (this.otherIn.isClosed()) {
          logNoMatch(objects, in, false);
          return;
        } else {
        }
      } else {
        final Channel<Record> channel = channels[index];
        final Record readObject = readObject(channel);
        if (index == 0 && this.recordDefinition1 == null) {
          setRecordDefinition1(readObject.getRecordDefinition());
        } else if (index == 1 && this.recordDefinition2 == null) {
          setRecordDefinition2(readObject.getRecordDefinition());
        }

        if (readObject != null) {
          if (previousEqualObject != null && DataType.equal(previousEqualObject, readObject)) {
            if (index == 0) {
              RecordLog.error(getClass(), "Duplicate in " + this.sourceName, readObject);
            } else {
              RecordLog.error(getClass(), "Duplicate in " + this.otherName, readObject);
            }
          } else {
            Record sourceObject;
            Record otherObject;
            final int oppositeIndex = (index + 1) % 2;
            if (index == 0) {
              sourceObject = readObject;
              otherObject = objects[oppositeIndex];
            } else {
              sourceObject = objects[oppositeIndex];
              otherObject = readObject;
            }
            final Object value = readObject.getValue(this.fieldName);
            if (value == null) {
              RecordLog.error(getClass(), "Missing key value for " + this.fieldName, readObject);
            } else if (objects[oppositeIndex] == null) {
              objects[index] = readObject;
              guard[index] = false;
              guard[oppositeIndex] = true;
            } else {
              final Object sourceValue = sourceObject.getValue(this.fieldName);
              final Comparable<Object> sourceComparator;
              if (sourceValue instanceof Number) {
                final Number number = (Number)sourceValue;
                final Double doubleValue = number.doubleValue();
                sourceComparator = (Comparable)doubleValue;
              } else {
                sourceComparator = (Comparable<Object>)sourceValue;
              }
              Object otherValue = otherObject.getValue(this.fieldName);
              if (otherValue instanceof Number) {
                final Number number = (Number)otherValue;
                otherValue = number.doubleValue();
              }
              // TODO duplicates
              final int compare = sourceComparator.compareTo(otherValue);
              if (compare == 0) {
                final Set<String> notEqualFieldNames = getNotEqualFieldNames(sourceObject,
                  otherObject);

                final boolean geometryEquals = geometryEquals(sourceObject, otherObject);
                if (!geometryEquals) {
                  final String geometryFieldName = sourceObject.getRecordDefinition()
                    .getGeometryFieldName();
                  notEqualFieldNames.add(geometryFieldName);
                }
                if (!notEqualFieldNames.isEmpty()) {
                  logNotEqual(sourceObject, otherObject, notEqualFieldNames, geometryEquals);
                }
                objects[0] = null;
                objects[1] = null;
                guard[0] = true;
                guard[1] = true;
                previousEqualObject = sourceObject;
              } else if (compare < 0) { // other object is bigger, keep other
                // object
                logNoMatch(sourceObject, false);
                objects[0] = null;
                objects[1] = otherObject;
                guard[0] = true;
                guard[1] = false;

              } else { // source is bigger, keep source object
                logNoMatch(otherObject, true);
                objects[0] = sourceObject;
                objects[1] = null;
                guard[0] = false;
                guard[1] = true;
              }
            }
          }
        }
      }
    }
  }

  public void setEqualExclude(final List<String> equalExclude) {
    this.equalExclude = equalExclude;
  }

  public void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }

  /**
   * @param in the in to set
   */
  public void setOtherIn(final Channel<Record> in) {
    this.otherIn = in;
    in.readConnect();
  }

  public void setOtherInBufferSize(final int otherInBufferSize) {
    this.otherInBufferSize = otherInBufferSize;
  }

  public void setOtherName(final String otherName) {
    this.otherName = otherName;
  }

  public void setRecordDefinition1(final RecordDefinition recordDefinition1) {
    this.recordDefinition1 = recordDefinition1;
    if (this.recordDefinition2 != null) {
      initAttributes();
    }
  }

  public void setRecordDefinition2(final RecordDefinition recordDefinition2) {
    this.recordDefinition2 = recordDefinition2;
    if (this.recordDefinition1 != null) {
      initAttributes();
    }
  }

  public void setSourceName(final String sourceName) {
    this.sourceName = sourceName;
  }

  protected boolean valueEquals(final Object value1, final Object value2) {
    if (value1 == null) {
      if (value2 == null) {
        return true;
      } else if (value2 instanceof String) {
        final String string2 = (String)value2;
        return !Property.hasValue(string2);
      }
    } else if (value2 == null) {
      if (value1 instanceof String) {
        final String string1 = (String)value1;
        return !Property.hasValue(string1);
      } else {
        return false;
      }
    } else if (value1 instanceof String && value2 instanceof String) {
      if (!Property.hasValue((String)value1) && !Property.hasValue((String)value2)) {
        return true;
      }
    }
    return DataType.equal(value1, value2);
  }
}
