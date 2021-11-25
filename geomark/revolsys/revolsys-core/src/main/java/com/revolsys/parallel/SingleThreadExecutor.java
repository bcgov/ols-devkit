package com.revolsys.parallel;

import java.util.concurrent.Callable;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.BaseCloseable;

public class SingleThreadExecutor implements BaseCloseable {
  private static ThreadLocal<Boolean> IS_THREAD = new ThreadLocal<>();

  private final Object callSync = new Object();

  private Throwable exception;

  private final Object handleSync = new Object();

  private boolean hasTask;

  private final Runnable preRun;

  private Object result;

  private Runnable runnable;

  private boolean running = false;

  private final Object runningSync = new Object();

  private boolean stopped = false;

  private Callable<? extends Object> task;

  private final Thread thread;

  private final String threadName;

  public SingleThreadExecutor(final String threadName) {
    this(threadName, null);
  }

  public SingleThreadExecutor(final String threadName, final Runnable preRun) {
    this.threadName = threadName;
    this.preRun = preRun;
    this.thread = new Thread(this::taskHandler, threadName);
    this.thread.setDaemon(true);
    this.thread.start();
  }

  public <V> V call(final Callable<V> task) {
    final Runnable runnable = null;
    return sendTask(task, runnable);
  }

  @Override
  public void close() {
    synchronized (this.runningSync) {
      if (this.running) {
        this.running = false;
        this.runningSync.notifyAll();
        this.thread.interrupt();
      }
    }
  }

  public boolean isRunning() {
    return this.running && !this.stopped;
  }

  public boolean isStopped() {
    return this.stopped;
  }

  public boolean isThread() {
    return IS_THREAD.get() == Boolean.TRUE;
  }

  public void run(final Runnable runnable) {
    sendTask(null, runnable);
  }

  @SuppressWarnings("unchecked")
  private <V> V sendTask(final Callable<V> task, final Runnable runnable) throws Error {
    if (IS_THREAD.get() == Boolean.TRUE) {
      try {
        if (task != null) {
          return task.call();
        } else if (runnable != null) {
          runnable.run();
        }
      } catch (final Exception e) {
        Exceptions.throwUncheckedException(e);
      }
    } else if (task != null || runnable != null) {
      synchronized (this.callSync) {
        try {
          synchronized (this.handleSync) {
            this.task = task;
            this.runnable = runnable;
            this.hasTask = true;
            this.handleSync.notifyAll();

            this.handleSync.wait();
          }
          if (this.exception instanceof Error) {
            throw (Error)this.exception;
          } else if (this.exception instanceof RuntimeException) {
            throw (RuntimeException)this.exception;
          } else if (this.exception != null) {
            throw Exceptions.wrap(this.threadName + ": error running task", this.exception);
          }
          return (V)this.result;
        } catch (final InterruptedException e) {
          // Ignore
        } finally {
          this.result = null;
          this.exception = null;
        }
      }
    }
    return null;
  }

  private void taskHandler() {
    try {
      IS_THREAD.set(Boolean.TRUE);
      if (this.preRun != null && !this.stopped) {
        this.preRun.run();
      }
      synchronized (this.runningSync) {
        if (!this.stopped) {
          this.running = true;
          this.runningSync.notifyAll();
        }
      }
      while (this.running) {
        synchronized (this.handleSync) {
          while (!this.hasTask && this.running) {
            try {
              this.handleSync.wait();
            } catch (final InterruptedException e) {
              if (!this.running) {
                synchronized (this.handleSync) {
                  this.handleSync.notifyAll();
                }
                return;
              }
            }
          }
          try {
            if (this.task != null) {
              this.result = this.task.call();
            } else if (this.runnable != null) {
              this.runnable.run();
            }
          } catch (final Throwable e) {
            this.exception = e;
          } finally {
            this.runnable = null;
            this.task = null;
            this.hasTask = false;
            this.handleSync.notifyAll();
          }
        }
      }
    } finally {
      this.running = false;
      this.stopped = true;
    }
  }

  @Override
  public String toString() {
    return this.threadName;
  }

  public void waitForRunning() {
    if (this.stopped) {
      throw new IllegalStateException("Not running");
    } else {
      synchronized (this.runningSync) {
        if (!this.running) {
          try {
            this.runningSync.wait();
          } catch (final InterruptedException e) {
          }
        }
      }
    }
  }
}
