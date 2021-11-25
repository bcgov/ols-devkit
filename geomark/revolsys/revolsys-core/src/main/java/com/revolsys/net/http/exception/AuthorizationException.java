package com.revolsys.net.http.exception;

public class AuthorizationException extends SecurityException {

  public AuthorizationException(final String message) {
    super(message);
  }

  public AuthorizationException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public AuthorizationException(final Throwable exception) {
    super(exception);
  }

}
