package com.revolsys.logging;

import java.lang.Thread.UncaughtExceptionHandler;

import org.jeometry.common.logging.Logs;

public class Slf4jUncaughtExceptionHandler implements UncaughtExceptionHandler {
  static {
    Thread.setDefaultUncaughtExceptionHandler(new Slf4jUncaughtExceptionHandler());
  }

  public static void init() {
  }

  @Override
  public void uncaughtException(final Thread t, final Throwable e) {
    Logs.error(this, e.getMessage(), e);
  }
}
