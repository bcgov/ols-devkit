package org.jeometry.common.data.type;

import java.util.Collection;

public abstract class AbstractDataType implements DataType {

  private final Class<?> javaClass;

  private final String name;

  private final boolean requiresQuotes;

  public AbstractDataType(final String name, final Class<?> javaClass,
    final boolean requiresQuotes) {
    this.name = name;
    this.javaClass = javaClass;
    this.requiresQuotes = requiresQuotes;
    DataTypes.register(this);
  }

  @Override
  public boolean equals(final Object value1, final Object value2) {
    if (value1 == value2) {
      return true;
    } else if (value1 == null) {
      return value2 == null;
    } else if (value2 == null) {
      return false;
    } else {
      try {
        final Object convertedValue1 = toObject(value1);
        final Object convertedValue2 = toObject(value2);
        final boolean equal = equalsNotNull(convertedValue1, convertedValue2);
        return equal;
      } catch (final Throwable e) {
        return false;
      }
    }
  }

  @Override
  public boolean equals(final Object value1, final Object value2,
    final Collection<? extends CharSequence> excludeFieldNames) {
    if (value1 == value2) {
      return true;
    } else if (value1 == null) {
      return value2 == null;
    } else if (value2 == null) {
      return false;
    } else {
      try {
        final Object convertedValue1 = toObject(value1);
        final Object convertedValue2 = toObject(value2);
        final boolean equal = equalsNotNull(convertedValue1, convertedValue2, excludeFieldNames);
        return equal;
      } catch (final Throwable e) {
        return false;
      }
    }
  }

  protected boolean equalsNotNull(final Object value1, final Object value2) {
    return value1.equals(value2);
  }

  protected boolean equalsNotNull(final Object value1, final Object value2,
    final Collection<? extends CharSequence> excludeFieldNames) {
    return value1.equals(value2);
  }

  @Override
  public Class<?> getJavaClass() {
    return this.javaClass;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getValidationName() {
    if (Number.class.isAssignableFrom(this.javaClass)) {
      return "number (" + getName() + ")";
    } else {
      return getName();
    }
  }

  @Override
  public int hashCode() {
    return this.name.hashCode();
  }

  @Override
  public boolean isAssignableFrom(final Class<?> valueClass) {
    return this.javaClass.isAssignableFrom(valueClass);
  }

  @Override
  public boolean isAssignableTo(final Class<?> valueClass) {
    return valueClass.isAssignableFrom(this.javaClass);
  }

  @Override
  public boolean isRequiresQuotes() {
    return this.requiresQuotes;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V toObject(final Object value) {
    if (value == null) {
      return null;
    } else {
      final Class<?> javaClass = this.javaClass;
      final Class<?> valueClass = value.getClass();
      if (javaClass == valueClass || javaClass.isAssignableFrom(valueClass)) {
        return (V)value;
      } else {
        return (V)toObjectDo(value);
      }
    }
  }

  protected Object toObjectDo(final Object value) {
    throw new IllegalArgumentException(value + " is not a valid " + getValidationName());
  }

  @Override
  public String toString() {
    return this.name.toString();
  }

  @Override
  public final String toString(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof CharSequence) {
      return value.toString();
    } else {
      try {
        final Object convertedValue = toObject(value);
        return toStringDo(convertedValue);
      } catch (final Throwable e) {
        return value.toString();
      }
    }
  }

  protected String toStringDo(final Object value) {
    return value.toString();
  }
}
