package com.revolsys.geometry.graph.attribute;

import java.util.function.Function;

import com.revolsys.properties.AbstractObjectPropertyProxy;

public class FunctionObjectPropertyProxy<O, T> extends AbstractObjectPropertyProxy<T, O> {
  private final Function<O, T> function;

  public FunctionObjectPropertyProxy(final Function<O, T> function) {
    this.function = function;
  }

  @Override
  public T newValue(final O value) {
    if (value != null && this.function != null) {
      return this.function.apply(value);
    }
    return null;
  }
}
