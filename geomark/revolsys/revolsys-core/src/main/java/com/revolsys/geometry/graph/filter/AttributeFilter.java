package com.revolsys.geometry.graph.filter;

import java.util.function.Predicate;

import org.jeometry.common.data.type.DataType;

import com.revolsys.properties.ObjectWithProperties;

public class AttributeFilter<T extends ObjectWithProperties> implements Predicate<T> {
  private final String fieldName;

  private final boolean inverse;

  private final Object value;

  public AttributeFilter(final String fieldName, final Object value) {
    this.fieldName = fieldName;
    this.value = value;
    this.inverse = false;
  }

  public AttributeFilter(final String fieldName, final Object value, final boolean inverse) {
    this.fieldName = fieldName;
    this.value = value;
    this.inverse = inverse;
  }

  @Override
  public boolean test(final T object) {
    final Object value = object.getProperty(this.fieldName);
    final boolean equal = DataType.equal(this.value, value);
    if (this.inverse) {
      return !equal;
    } else {
      return equal;
    }
  }
}
