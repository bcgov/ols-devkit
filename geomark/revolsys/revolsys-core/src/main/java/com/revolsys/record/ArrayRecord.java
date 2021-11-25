package com.revolsys.record;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jeometry.common.data.identifier.SingleIdentifier;

import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Property;

/**
 * The ArrayRecord is an implementation of {@link Record} which uses an array of
 * Objects as the storage for the field values.
 */
public class ArrayRecord extends BaseRecord {
  public static final RecordFactory<ArrayRecord> FACTORY = ArrayRecord::newRecord;

  /**
   * Construct a new ArrayRecord using the record definition
   *
   * @param recordDefinition The record definition used to create the instance.
   * @return The Record instance.
   */
  public static ArrayRecord newRecord(final RecordDefinition recordDefinition) {
    return new ArrayRecord(recordDefinition);
  }

  /** The object's field values. */
  private Object[] values;

  /**
   * Construct a new ArrayRecord as a deep clone of the field values. Objects can
   * only be cloned if they have a publicly accessible {@link #clone()} method.
   *
   * @param record The object to clone.
   */
  public ArrayRecord(final Record record) {
    this(record.getRecordDefinition(), record);
  }

  /**
   * Construct a new empty ArrayRecord using the recordDefinition.
   *
   * @param recordDefinition The recordDefinition defining the object type.
   */
  public ArrayRecord(final RecordDefinition recordDefinition) {
    super(recordDefinition);
    setState(RecordState.INITIALIZING);
    initDefaultValues(recordDefinition);
    setState(RecordState.NEW);
  }

  public ArrayRecord(final RecordDefinition recordDefinition,
    final Iterable<? extends Object> values) {
    this(recordDefinition);
    setState(RecordState.INITIALIZING);
    initDefaultValues(recordDefinition);
    setValues(values);
    setState(RecordState.NEW);
  }

  public ArrayRecord(final RecordDefinition recordDefinition,
    final Map<String, ? extends Object> values) {
    super(recordDefinition);
    setState(RecordState.INITIALIZING);
    initDefaultValues(recordDefinition);
    initValues(values);
    setState(RecordState.NEW);
  }

  public ArrayRecord(final RecordDefinition recordDefinition, final Object[] values) {
    this(recordDefinition);
    setState(RecordState.INITIALIZING);
    initDefaultValues(recordDefinition);
    setValues(values);
    setState(RecordState.NEW);
  }

  /**
   * Construct a new clone of the record.
   *
   * @return The cloned record.
   */
  @Override
  public ArrayRecord clone() {
    final ArrayRecord clone = (ArrayRecord)super.clone();
    clone.values = this.values.clone();
    return clone;
  }

  /**
   * Get the value of the field with the specified index.
   *
   * @param index The index of the field.
   * @return The field value.
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T extends Object> T getValue(final int index) {
    if (index < 0) {
      return null;
    } else {
      try {
        return (T)getValueInternal(index);
      } catch (final ArrayIndexOutOfBoundsException e) {
        return null;
      }
    }
  }

  protected Object getValueInternal(final int index) {
    return this.values[index];
  }

  /**
   * Get the values of all values.
   *
   * @return The field value.
   */
  @Override
  public List<Object> getValues() {
    return Arrays.asList(this.values);
  }

  @Override
  public int hashCode() {
    return this.values.hashCode();
  }

  protected void initDefaultValues(final RecordDefinition recordDefinition) {
    if (recordDefinition == null) {
      this.values = new Object[0];
    } else {
      final int fieldCount = recordDefinition.getFieldCount();
      this.values = new Object[fieldCount];
      final Map<String, Object> defaultValues = recordDefinition.getDefaultValues();
      setValuesByPath(defaultValues);
    }
  }

  /**
   * Set the value of the field with the specified name.
   *
   * @param index The index of the field.
   * @param value The new value.
   */
  @Override
  protected boolean setValue(final FieldDefinition fieldDefinition, Object value) {
    boolean updated = false;
    if (value instanceof String) {
      final String string = (String)value;
      if (!Property.hasValue(string)) {
        value = null;
      }
    }
    if (value instanceof SingleIdentifier) {
      final SingleIdentifier identifier = (SingleIdentifier)value;
      value = identifier.getValue(0);
    }
    final Object newValue = fieldDefinition.toFieldValue(getState(), value);
    final int index = fieldDefinition.getIndex();
    final Object oldValue = getValueInternal(index);
    if (!isInitializing() && !fieldDefinition.equals(oldValue, newValue)) {
      updated = true;
      updateState();
    }
    setValueInternal(index, newValue);
    return updated;
  }

  protected void setValueInternal(final int index, final Object newValue) {
    this.values[index] = newValue;
  }

}
