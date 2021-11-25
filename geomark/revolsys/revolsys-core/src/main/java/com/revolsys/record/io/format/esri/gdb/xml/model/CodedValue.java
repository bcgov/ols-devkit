package com.revolsys.record.io.format.esri.gdb.xml.model;

public class CodedValue implements Cloneable {
  private Object code;

  private String name;

  public CodedValue() {
  }

  public CodedValue(final Object code, final String name) {
    this.code = code;
    this.name = name;
  }

  @Override
  protected CodedValue clone() throws CloneNotSupportedException {
    return (CodedValue)super.clone();
  }

  public Object getCode() {
    return this.code;
  }

  public String getName() {
    return this.name;
  }

  public void setCode(final Object code) {
    this.code = code;
  }

  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return this.code + "=" + this.name;
  }
}
