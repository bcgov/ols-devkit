package com.revolsys.record.property;

public class SimpleProperty extends AbstractRecordDefinitionProperty {
  private String propertyName;

  private Object value;

  public SimpleProperty() {
  }

  public SimpleProperty(final String propertyName, final Object value) {
    this.propertyName = propertyName;
    this.value = value;
  }

  @Override
  public SimpleProperty clone() {
    return new SimpleProperty(this.propertyName, this.value);
  }

  @Override
  public String getPropertyName() {
    return this.propertyName;
  }

  public <T> T getValue() {
    return (T)this.value;
  }

  public void setPropertyName(final String propertyName) {
    this.propertyName = propertyName;
  }

  public void setValue(final Object value) {
    this.value = value;
  }
}
