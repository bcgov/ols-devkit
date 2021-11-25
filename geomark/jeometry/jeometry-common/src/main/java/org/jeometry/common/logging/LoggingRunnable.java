package org.jeometry.common.logging;

public class LoggingRunnable implements Runnable {
  private final Runnable runnable;

  public LoggingRunnable(final Runnable runnable) {
    this.runnable = runnable;
  }

  @Override
  public void run() {
    Class<? extends Runnable> logClass;
    if (this.runnable == null) {
      logClass = getClass();
    } else {
      logClass = this.runnable.getClass();
    }
    Logs.setUncaughtExceptionHandler(logClass);
    try {
      this.runnable.run();
    } catch (final Throwable e) {

      Logs.error(logClass, e.getMessage(), e);
    }

  }
}
