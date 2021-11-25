package com.revolsys.beans;

/**
 * An exception that wraps an object that caused the exception.
 */
public class ObjectException extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final Object object;

  public ObjectException(final Object object) {
    this.object = object;
  }

  public ObjectException(final Object object, final String message) {
    super(message);
    this.object = object;
  }

  public ObjectException(final Object object, final String message, final Throwable cause) {
    super(message, cause);
    this.object = object;
  }

  public ObjectException(final Object object, final Throwable cause) {
    super(cause.getMessage(), cause);
    this.object = object;
  }

  @SuppressWarnings("unchecked")
  public <V> V getObject() {
    return (V)this.object;
  }
}
