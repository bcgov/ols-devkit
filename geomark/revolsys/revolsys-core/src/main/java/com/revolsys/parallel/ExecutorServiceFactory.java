package com.revolsys.parallel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.revolsys.collection.map.ThreadSharedProperties;

public class ExecutorServiceFactory {
  private static final String KEY = ExecutorServiceFactory.class.getName() + ".key";

  private static final Object SYNC = new Object();

  public static ExecutorService getExecutorService() {
    synchronized (SYNC) {
      ExecutorService executorService = ThreadSharedProperties.getProperty(KEY);
      if (executorService == null) {
        executorService = Executors.newCachedThreadPool();
        ThreadSharedProperties.setDefaultProperty(KEY, executorService);
      }
      return executorService;

    }
  }

  public static void setDefaultExecutorService(final ExecutorService executorService) {
    ThreadSharedProperties.setDefaultProperty(KEY, executorService);
  }

  public static void setThreadExecutorService(final ExecutorService executorService) {
    ThreadSharedProperties.setProperty(KEY, executorService);
  }
}
