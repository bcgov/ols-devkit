package com.revolsys.parallel;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.jeometry.common.logging.LoggingRunnable;

public class NamedThreadFactory implements ThreadFactory {
  private static final AtomicInteger poolNumber = new AtomicInteger(1);

  private ThreadGroup group;

  private String namePrefix;

  private ThreadGroup parentGroup;

  private int priority;

  private String threadNamePrefix;

  private final AtomicInteger threadNumber = new AtomicInteger(1);

  public NamedThreadFactory() {
    this(Thread.NORM_PRIORITY);
  }

  public NamedThreadFactory(final int priority) {
    this.priority = priority;
    final SecurityManager securityManager = System.getSecurityManager();
    if (securityManager == null) {
      final Thread currentThread = Thread.currentThread();
      this.parentGroup = currentThread.getThreadGroup();
    } else {
      this.parentGroup = securityManager.getThreadGroup();
    }
    this.namePrefix = "pool-" + poolNumber.getAndIncrement();
  }

  public String getNamePrefix() {
    return this.namePrefix;
  }

  public ThreadGroup getParentGroup() {
    return this.parentGroup;
  }

  public int getPriority() {
    return this.priority;
  }

  @Override
  public Thread newThread(final Runnable runnable) {
    synchronized (this.threadNumber) {
      if (this.group == null) {
        this.threadNamePrefix = this.namePrefix + "-thread-";
        this.group = new ThreadGroup(this.parentGroup, this.namePrefix);
      }
    }

    final String threadName = this.threadNamePrefix + this.threadNumber.getAndIncrement();
    final LoggingRunnable loggingRunnable = new LoggingRunnable(runnable);
    final Thread thread = new Thread(this.group, loggingRunnable, threadName, 0);
    if (thread.isDaemon()) {
      thread.setDaemon(false);
    }
    thread.setPriority(this.priority);
    return thread;
  }

  public NamedThreadFactory setNamePrefix(final String namePrefix) {
    this.namePrefix = namePrefix;
    return this;
  }

  public NamedThreadFactory setParentGroup(final ThreadGroup parentGroup) {
    this.parentGroup = parentGroup;
    return this;
  }

  public NamedThreadFactory setPriority(final int priority) {
    this.priority = priority;
    return this;
  }

}
