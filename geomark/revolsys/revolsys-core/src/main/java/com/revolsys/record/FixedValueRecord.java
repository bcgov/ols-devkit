package com.revolsys.record;

import java.util.Arrays;
import java.util.List;

import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;

public class FixedValueRecord extends BaseRecord {
  private static final RecordDefinition RECORD_DEFINITION = new RecordDefinitionImpl();

  private final Object value;

  public FixedValueRecord(final Object value) {
    this(RECORD_DEFINITION, value);
  }

  public FixedValueRecord(final RecordDefinition recordDefinition, final Object value) {
    super(recordDefinition);
    this.value = value;
  }

  @Override
  public FixedValueRecord clone() {
    final FixedValueRecord clone = (FixedValueRecord)super.clone();
    return clone;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getValue(final CharSequence name) {
    return (T)this.value;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Object> T getValue(final int index) {
    if (index < 0) {
      return null;
    } else {
      return (T)this.value;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getValueByPath(final CharSequence path) {
    return (T)this.value;
  }

  @Override
  public List<Object> getValues() {
    return Arrays.asList(this.value);
  }

  @Override
  public int hashCode() {
    return this.value.hashCode();
  }

  @Override
  protected boolean setValue(final FieldDefinition fieldDefinition, final Object value) {
    return false;
  }
}
