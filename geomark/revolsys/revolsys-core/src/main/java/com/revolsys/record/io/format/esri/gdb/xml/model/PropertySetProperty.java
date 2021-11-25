package com.revolsys.record.io.format.esri.gdb.xml.model;

public class PropertySetProperty {
  private String key;

  private Object value;

  public String getKey() {
    return this.key;
  }

  public Object getValue() {
    return this.value;
  }

  public void setKey(final String key) {
    this.key = key;
  }

  public void setValue(final Object value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return this.key + "=" + this.value;
  }
}
