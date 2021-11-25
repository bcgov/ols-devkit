package org.jeometry.common.logging;

import java.lang.Thread.State;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public class DeadlockLogger implements Runnable {
  private static final int WAIT_TIME = 60000;

  private static Thread thread;

  public static synchronized void initialize() {
    if (thread == null || !thread.isAlive()) {
      final DeadlockLogger deadlockLogger = new DeadlockLogger();
      final Thread thread = new Thread(deadlockLogger, "Deadlock-detection");
      thread.setDaemon(true);
      thread.start();
      DeadlockLogger.thread = thread;
    }
  }

  @Override
  public void run() {
    final ThreadMXBean mbean = ManagementFactory.getThreadMXBean();

    while (true) {
      synchronized (this) {
        try {
          wait(WAIT_TIME);
        } catch (final InterruptedException e) {
          return;
        }
        final long[] deadlockedThreadIds = mbean.findDeadlockedThreads();

        if (deadlockedThreadIds != null) {
          final ThreadInfo[] threadInfos1 = mbean.getThreadInfo(deadlockedThreadIds, true, true);
          final ThreadInfo[] threadInfos2 = mbean.getThreadInfo(deadlockedThreadIds, 500);
          final StringBuilder message = new StringBuilder("Deadlock detected\n\n");
          for (int i = 0; i < threadInfos1.length; i++) {
            final ThreadInfo threadInfo1 = threadInfos1[i];
            final ThreadInfo threadInfo2 = threadInfos2[i];
            message.append(toString(threadInfo1));
            message.append(toString(threadInfo2));
          }
          Logs.error(this, message.toString());
          // Quit thread now point running if there is a deadlock
          return;
        }

      }
    }
  }

  private Object toString(final ThreadInfo threadInfo) {
    final String threadName = threadInfo.getThreadName();
    final long threadId = threadInfo.getThreadId();
    final State threadState = threadInfo.getThreadState();
    final StringBuilder sb = new StringBuilder(
      "\"" + threadName + "\"" + " Id=" + threadId + " " + threadState);
    final String lockName = threadInfo.getLockName();
    if (lockName != null) {
      sb.append(" on " + lockName);
    }
    final String lockOwnerName = threadInfo.getLockOwnerName();
    if (lockOwnerName != null) {
      final long lockOwnerId = threadInfo.getLockOwnerId();
      sb.append(" owned by \"" + lockOwnerName + "\" Id=" + lockOwnerId);
    }
    if (threadInfo.isSuspended()) {
      sb.append(" (suspended)");
    }
    if (threadInfo.isInNative()) {
      sb.append(" (in native)");
    }
    sb.append('\n');
    final StackTraceElement[] stackTrace = threadInfo.getStackTrace();
    int i = 0;
    for (; i < stackTrace.length && i < 500; i++) {
      final StackTraceElement ste = stackTrace[i];
      sb.append("\tat " + ste.toString());
      sb.append('\n');
      final LockInfo lockInfo = threadInfo.getLockInfo();
      if (i == 0 && lockInfo != null) {
        final Thread.State ts = threadState;
        switch (ts) {
          case BLOCKED:
            sb.append("\t-  blocked on " + lockInfo);
            sb.append('\n');
          break;
          case WAITING:
            sb.append("\t-  waiting on " + lockInfo);
            sb.append('\n');
          break;
          case TIMED_WAITING:
            sb.append("\t-  waiting on " + lockInfo);
            sb.append('\n');
          break;
          default:
        }
      }

      final MonitorInfo[] lockedMonitors = threadInfo.getLockedMonitors();
      for (final MonitorInfo mi : lockedMonitors) {
        if (mi.getLockedStackDepth() == i) {
          sb.append("\t-  locked " + mi);
          sb.append('\n');
        }
      }
    }
    if (i < stackTrace.length) {
      sb.append("\t...");
      sb.append('\n');
    }

    final LockInfo[] locks = threadInfo.getLockedSynchronizers();
    if (locks.length > 0) {
      sb.append("\n\tNumber of locked synchronizers = " + locks.length);
      sb.append('\n');
      for (final LockInfo li : locks) {
        sb.append("\t- " + li);
        sb.append('\n');
      }
    }
    sb.append('\n');
    return sb.toString();
  }
}
