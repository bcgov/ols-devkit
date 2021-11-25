package com.revolsys.record;

public class NoSuchRecordException extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public NoSuchRecordException() {
    super();
  }

  public NoSuchRecordException(final String message) {
    super(message);
  }

  public NoSuchRecordException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public NoSuchRecordException(final Throwable cause) {
    super(cause);
  }

}
