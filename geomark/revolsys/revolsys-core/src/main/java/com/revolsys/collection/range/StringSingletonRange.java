package com.revolsys.collection.range;

import com.revolsys.util.Property;

/**
 *
 * Ranges are immutable
 */
public class StringSingletonRange extends AbstractRange<String> {
  private final String value;

  public StringSingletonRange(final char character) {
    this(Character.toString(character));
  }

  public StringSingletonRange(final String value) {
    if (!Property.hasValue(value)) {
      throw new IllegalArgumentException("Value must not be empty for " + getClass());
    }
    this.value = value;
  }

  @Override
  public AbstractRange<?> expand(final AbstractRange<?> range) {
    return null;
  }

  @Override
  public AbstractRange<?> expand(final Object value) {
    if (this.value.equals(value)) {
      return this;
    } else {
      return null;
    }
  }

  @Override
  public String getFrom() {
    return this.value;
  }

  @Override
  public String getTo() {
    return this.value;
  }
}
