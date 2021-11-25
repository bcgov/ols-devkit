package com.revolsys.parallel;

public class ThreadInterruptedException extends RuntimeException {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public ThreadInterruptedException() {
    super("Thread was interrupted");
  }

  public ThreadInterruptedException(final InterruptedException e) {
    super("Thread was interrupted", e);
    Thread.currentThread().interrupt();
  }
}
