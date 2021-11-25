package com.revolsys.io;

public class EndOfFileException extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public EndOfFileException() {
  }

  public EndOfFileException(final String message) {
    super(message);
  }

  public EndOfFileException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public EndOfFileException(final Throwable cause) {
    super(cause);
  }

}
