package com.revolsys.collection.map;

public class ObjectKey {
  private Object value;

  public ObjectKey(final Object value) {
    if (value == null) {
      throw new IllegalArgumentException("Object cannot be null");
    } else {
      this.value = value;
    }
  }

  @Override
  public boolean equals(final Object object) {

    return this.value == object;
  }

  public Object getValue() {
    return this.value;
  }

  @Override
  public int hashCode() {
    return this.value.hashCode();
  }

  @Override
  public String toString() {
    return this.value.toString();
  }
}
