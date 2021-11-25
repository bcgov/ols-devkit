package com.revolsys.collection.range;

public class RangeInvalidException extends IllegalArgumentException {
  private static final long serialVersionUID = 1L;

  public RangeInvalidException() {
    super();
  }

  public RangeInvalidException(final String s) {
    super(s);
  }

  public RangeInvalidException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public RangeInvalidException(final Throwable cause) {
    super(cause);
  }

}
