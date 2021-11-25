package com.revolsys.parallel;

public class ThreadUtil {

  public static boolean isInterrupted() {
    return Thread.currentThread().isInterrupted();
  }

  public static void pause(final long milliSeconds) {
    pause(new Object(), milliSeconds);
  }

  public static void pause(final Object object) {
    synchronized (object) {
      try {
        object.wait();
      } catch (final InterruptedException e) {
        throw new ThreadInterruptedException(e);
      }
    }
  }

  public static void pause(final Object object, final long milliSeconds) {
    synchronized (object) {
      try {
        object.wait(milliSeconds);
      } catch (final InterruptedException e) {
        throw new ThreadInterruptedException(e);
      }
    }
  }

  public static void pause(final Object object, final long milliSeconds, final int nanoSeconds) {
    synchronized (object) {
      try {
        object.wait(milliSeconds, nanoSeconds);
      } catch (final InterruptedException e) {
        throw new ThreadInterruptedException(e);
      }
    }
  }
}
