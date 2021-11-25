package org.jeometry.common.exception;

public final class WrappedException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public WrappedException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public WrappedException(final Throwable cause) {
    super(cause);
  }

  public boolean isException(final Class<? extends Throwable> clazz) {
    final Throwable cause = getCause();
    if (cause == null) {
      return false;
    } else if (cause instanceof WrappedException) {
      return ((WrappedException)cause).isException(clazz);
    } else if (clazz.isAssignableFrom(cause.getClass())) {
      return true;
    } else {
      return false;
    }
  }
}
