package com.revolsys.beans;

/**
 * An exception that wraps an object and property that caused the exception.
 */
public class ObjectPropertyException extends ObjectException {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final String propertyName;

  public ObjectPropertyException(final Object object, final String propertyName) {
    super(object);
    this.propertyName = propertyName;
  }

  public ObjectPropertyException(final Object object, final String propertyName,
    final String message) {
    super(object, message);
    this.propertyName = propertyName;
  }

  public ObjectPropertyException(final Object object, final String propertyName,
    final String message, final Throwable cause) {
    super(object, message, cause);
    this.propertyName = propertyName;
  }

  public ObjectPropertyException(final Object object, final String propertyName,
    final Throwable cause) {
    super(object, cause);
    this.propertyName = propertyName;
  }

  public String getPropertyName() {
    return this.propertyName;
  }

  @Override
  public String toString() {
    return this.propertyName + ": " + super.toString();
  }
}
