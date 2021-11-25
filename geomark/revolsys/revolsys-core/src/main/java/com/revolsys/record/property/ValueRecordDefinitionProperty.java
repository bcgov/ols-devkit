package com.revolsys.record.property;

import com.revolsys.record.schema.RecordDefinition;

public class ValueRecordDefinitionProperty extends AbstractRecordDefinitionProperty {
  public static void setProperty(final RecordDefinition recordDefinition, final String propertyName,
    final Object value) {
    final ValueRecordDefinitionProperty valueRecordDefinitionProperty = new ValueRecordDefinitionProperty(
      propertyName, value);
    valueRecordDefinitionProperty.setRecordDefinition(recordDefinition);
  }

  private String propertyName;

  private Object value;

  public ValueRecordDefinitionProperty() {
  }

  public ValueRecordDefinitionProperty(final String propertyName, final Object value) {
    this.propertyName = propertyName;
    this.value = value;
  }

  @Override
  public String getPropertyName() {
    return this.propertyName;
  }

  public Object getValue() {
    return this.value;
  }

  public void setPropertyName(final String propertyName) {
    this.propertyName = propertyName;
  }

  public void setValue(final Object value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return this.propertyName + "=" + this.value;
  }
}
