package com.revolsys.util;

import java.util.List;
import java.util.ServiceLoader;

import org.jeometry.common.date.Dates;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.list.Lists;

public class ServiceInitializerLoader {
  static {
    try {
      final ServiceLoader<ServiceInitializer> serviceLoader = ServiceLoader
        .load(ServiceInitializer.class);
      final List<ServiceInitializer> services = Lists.toArray(serviceLoader);
      services.sort((a, b) -> Integer.compare(a.priority(), b.priority()));
      for (final ServiceInitializer serviceInitializer : services) {
        final long startTime = System.currentTimeMillis();
        try {
          serviceInitializer.initializeService();
        } catch (final Throwable e) {
          Logs.error(serviceInitializer, "Unable to initialize", e);
        }
        Dates.debugEllapsedTime(ServiceInitializer.class, "init\t" + serviceInitializer.getClass(),
          startTime);
      }
    } catch (final Error e) {
      Logs.error(ServiceInitializerLoader.class, "Unable to initialize services", e);
      throw e;
    } catch (final Exception e) {
      Logs.error(ServiceInitializerLoader.class, "Unable to initialize services", e);
    }
  }

  static void initializeServices() {
  }
}
