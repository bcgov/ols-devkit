package com.revolsys.net.http.exception;

public class SecurityException extends RuntimeException {

  public SecurityException(final String message) {
    super(message);
  }

  public SecurityException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public SecurityException(final Throwable exception) {
    super(exception);
  }

}
