package com.revolsys.record;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataType;

import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;

public class ArrayChangeTrackRecord extends ArrayRecord implements ChangeTrackRecord {
  public static final RecordFactory<ArrayChangeTrackRecord> FACTORY = ArrayChangeTrackRecord::newRecord;

  private static final Map<String, Object> EMPTY_ORIGINAL_VALUES = Collections.emptyMap();

  public static ArrayChangeTrackRecord newRecord(final RecordDefinition recordDefinition) {
    return new ArrayChangeTrackRecord(recordDefinition);
  }

  private Identifier identifier;

  protected Map<String, Object> originalValues = EMPTY_ORIGINAL_VALUES;

  public ArrayChangeTrackRecord(final Record record) {
    super(record);
  }

  public ArrayChangeTrackRecord(final RecordDefinition recordDefinition) {
    super(recordDefinition);
  }

  public ArrayChangeTrackRecord(final RecordDefinition recordDefinition,
    final Iterable<? extends Object> values) {
    super(recordDefinition, values);
  }

  public ArrayChangeTrackRecord(final RecordDefinition recordDefinition,
    final Map<String, ? extends Object> values) {
    super(recordDefinition, values);
  }

  public ArrayChangeTrackRecord(final RecordDefinition recordDefinition, final Object[] values) {
    super(recordDefinition, values);
  }

  @Override
  public Identifier getIdentifier() {
    if (this.identifier == null) {
      this.identifier = super.getIdentifier();
    }
    return this.identifier;
  }

  @Override
  public <T> T getOriginalValue(final int fieldIndex) {
    final String fieldName = getFieldName(fieldIndex);
    return getOriginalValue(fieldName);
  }

  @Override
  @SuppressWarnings("unchecked")
  public synchronized <T> T getOriginalValue(final String name) {
    final Map<String, Object> originalValues = this.originalValues;
    if (originalValues.containsKey(name)) {
      return (T)originalValues.get(name);
    }
    return (T)getValue(name);
  }

  @Override
  public boolean isModified(final int fieldIndex) {
    final String fieldName = getFieldName(fieldIndex);
    return isModified(fieldName);
  }

  @Override
  public boolean isModified(final String fieldName) {
    final Map<String, Object> originalValues = this.originalValues;
    if (originalValues == null) {
      return false;
    } else {
      return originalValues.containsKey(fieldName);
    }
  }

  @Override
  public Record newRecord() {
    return new ArrayRecord(this);
  }

  protected Map<String, Object> removeOriginalValue(final Map<String, Object> originalValues,
    final String fieldName) {
    if (!originalValues.isEmpty()) {
      originalValues.remove(fieldName);
      if (originalValues.isEmpty()) {
        this.originalValues = EMPTY_ORIGINAL_VALUES;
      }
    }
    return this.originalValues;
  }

  @Override
  protected synchronized boolean setValue(final FieldDefinition field, final Object value) {
    boolean updated = false;
    final int fieldIndex = field.getIndex();
    final String fieldName = field.getName();

    final Object newValue = field.toFieldValue(value);
    final Object oldValue = getValue(fieldIndex);
    RecordState newState = null;
    if (!DataType.equal(oldValue, newValue)) {
      final RecordState state = getState();
      switch (state) {
        case INITIALIZING:
        // Allow modification on initialization
        break;
        case NEW:
        break;
        case DELETED:
        break;
        case PERSISTED:
        case MODIFIED:
          final Object originalValue = getOriginalValue(fieldName);
          Map<String, Object> originalValues = this.originalValues;
          if (field.equals(originalValue, newValue)) {
            if (originalValues != EMPTY_ORIGINAL_VALUES) {
              originalValues = removeOriginalValue(originalValues, fieldName);
              if (originalValues.isEmpty()) {
                newState = RecordState.PERSISTED;
              }
            }
          } else {
            if (originalValues == EMPTY_ORIGINAL_VALUES) {
              originalValues = new HashMap<>();
            }
            originalValues.put(fieldName, originalValue);
            if (RecordState.INITIALIZING != state) {
              newState = RecordState.MODIFIED;
            }
          }
          this.originalValues = originalValues;
        break;
      }
      updated |= super.setValue(field, newValue);
      if (newState != null) {
        setState(newState);
      }

    }
    return updated;
  }
}
