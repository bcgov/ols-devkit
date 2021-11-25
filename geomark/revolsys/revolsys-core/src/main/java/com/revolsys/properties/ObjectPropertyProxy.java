package com.revolsys.properties;

public interface ObjectPropertyProxy<T, O> {
  void clearValue();

  T getValue(final O object);
}
