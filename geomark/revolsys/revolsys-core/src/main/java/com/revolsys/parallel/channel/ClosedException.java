package com.revolsys.parallel.channel;

@SuppressWarnings("serial")
public class ClosedException extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public ClosedException() {
    super();
  }

  public ClosedException(final String message) {
    super(message);
  }

  public ClosedException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public ClosedException(final Throwable cause) {
    super(cause);
  }

}
