package com.revolsys.record;

import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.schema.FieldDefinition;

public class ChangeTrackRecordImpl extends BaseRecord implements ChangeTrackRecord {

  public static ChangeTrackRecord newRecord(final Record originalRecord,
    final JsonObject changedValues) {
    if (originalRecord == null) {
      throw new IllegalArgumentException("Original record cannot be null");
    } else {
      final ChangeTrackRecordImpl changeTrackRecord = new ChangeTrackRecordImpl(originalRecord);
      changeTrackRecord.setState(RecordState.INITIALIZING);
      changeTrackRecord.setValues(changedValues);
      changeTrackRecord.setState(RecordState.PERSISTED);
      return changeTrackRecord;
    }
  }

  private final boolean[] changedFlags;

  private final Object[] changedValues;

  private final Record originalRecord;

  protected ChangeTrackRecordImpl(final Record originalRecord) {
    super(originalRecord.getRecordDefinition());
    this.originalRecord = originalRecord;
    final int fieldCount = getFieldCount();
    this.changedFlags = new boolean[fieldCount];
    this.changedValues = new Object[fieldCount];
  }

  @Override
  public ChangeTrackRecordImpl clone() {
    return (ChangeTrackRecordImpl)super.clone();
  }

  @Override
  public <T> T getOriginalValue(final int fieldIndex) {
    return this.originalRecord.getValue(fieldIndex);
  }

  @Override
  public <T> T getOriginalValue(final String fieldName) {
    final String fieldName1 = fieldName;
    final int fieldIndex = getFieldIndex(fieldName1);
    return getOriginalValue(fieldIndex);
  }

  @SuppressWarnings("unchecked")
  @Override
  public synchronized <T> T getValue(final int fieldIndex) {
    if (fieldIndex >= 0) {
      if (this.changedFlags[fieldIndex]) {
        return (T)this.changedValues[fieldIndex];
      }
      return getOriginalValue(fieldIndex);
    } else {
      return null;
    }
  }

  @Override
  public <T> T getValue(final String name) {
    final int fieldIndex = getFieldIndex(name);
    return getValue(fieldIndex);
  }

  public boolean isHasChangedValue() {
    for (final boolean changedFlag : this.changedFlags) {
      if (changedFlag) {
        return true;
      }
    }
    return false;
  }

  @Override
  public synchronized boolean isModified(final int fieldIndex) {
    if (this.changedFlags[fieldIndex]) {
      final FieldDefinition field = getRecordDefinition().getField(fieldIndex);
      final Object changedValue = this.changedValues[fieldIndex];
      final Object originalValue = getOriginalValue(fieldIndex);
      return !field.equals(changedValue, originalValue);
    }
    return false;
  }

  @Override
  public Record newRecord() {
    return new ArrayRecord(this);
  }

  @Override
  protected synchronized boolean setValue(final FieldDefinition field, final Object value) {
    final RecordState state = getState();
    if (state.isDeleted()) {
      throw new IllegalStateException("Cannot modify a deleted record\n" + toString());
    } else {
      final boolean updated = false;
      final int fieldIndex = field.getIndex();

      final Object newValue = field.toFieldValue(value);
      final Object oldValue = getValue(fieldIndex);
      if (field.equals(oldValue, newValue)) {

      } else {
        final Object originalValue = getOriginalValue(fieldIndex);
        final boolean valueEqual = field.equals(originalValue, newValue);
        this.changedFlags[fieldIndex] = !valueEqual;
        if (valueEqual) {
          this.changedValues[fieldIndex] = null;
        } else {
          this.changedValues[fieldIndex] = newValue;
        }
        if (getState() != RecordState.INITIALIZING) {
          setState(RecordState.MODIFIED);
        }
      }
      return updated;
    }
  }
}
