package com.revolsys.gis.converter;

import org.springframework.core.convert.converter.Converter;

public class Constant<T> implements Converter<T, Object> {
  private Object value;

  public Constant() {
  }

  public Constant(final Object value) {
    this.value = value;
  }

  @Override
  public Object convert(final T source) {
    return this.value;
  }
}
