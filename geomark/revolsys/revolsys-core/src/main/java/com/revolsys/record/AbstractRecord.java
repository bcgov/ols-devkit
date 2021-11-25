package com.revolsys.record;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Property;

public abstract class AbstractRecord implements Record, Cloneable {
  @Override
  public Record clone() {
    try {
      final AbstractRecord record = (AbstractRecord)super.clone();
      record.setState(RecordState.NEW);
      return record;
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException("Unable to clone", e);
    }
  }

  @Override
  public boolean equals(final Object o) {
    return this == o;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  protected void initValues(final Map<String, ? extends Object> record) {
    if (record != null) {
      final List<FieldDefinition> fields = getFieldDefinitions();
      for (final FieldDefinition fieldDefintion : fields) {
        final String name = fieldDefintion.getName();
        final Object value = record.get(name);
        fieldDefintion.setValue(this, value);
      }
    }
  }

  protected boolean setValue(final FieldDefinition fieldDefinition, Object value) {
    final String propertyName = fieldDefinition.getName();
    value = fieldDefinition.toFieldValueException(getState(), value);
    Property.setSimple(this, propertyName, value);
    return true;
  }

  @Override
  public final boolean setValue(final int fieldIndex, final Object value) {
    final FieldDefinition fieldDefinition = getFieldDefinition(fieldIndex);
    if (fieldDefinition == null) {
      return false;
    } else {
      return setValue(fieldDefinition, value);
    }
  }

  @Override
  public void setValues(final Iterable<? extends Object> values) {
    if (values != null) {
      final Iterator<? extends Object> iterator = values.iterator();
      final RecordDefinition recordDefinition = getRecordDefinition();
      for (final FieldDefinition fieldDefinition : recordDefinition.getFields()) {
        if (iterator.hasNext()) {
          final Object value = iterator.next();
          setValue(fieldDefinition, value);
        } else {
          return;
        }
      }
    }
  }

  @Override
  public AbstractRecord setValues(final Object... values) {
    if (values != null) {
      int i = 0;
      final RecordDefinition recordDefinition = getRecordDefinition();
      for (final FieldDefinition fieldDefinition : recordDefinition.getFields()) {
        if (i < values.length) {
          final Object value = values[i];
          setValue(fieldDefinition, value);
          i++;
        } else {
          return this;
        }
      }
    }
    return this;
  }

  @Override
  public JsonObject toJson() {
    return Record.super.toJson();
  }

  /**
   * Return a String representation of the record. There is no guarantee as to
   * the format of this string.
   *
   * @return The string value.
   */
  @Override
  public String toString() {
    final Record record = this;
    return Record.toString(record);
  }

  @SuppressWarnings("incomplete-switch")
  protected void updateState() {
    switch (getState()) {
      case PERSISTED:
        setState(RecordState.MODIFIED);
      break;
      case DELETED:
        throw new IllegalStateException("Cannot modify an object which has been deleted");
    }
  }

}
