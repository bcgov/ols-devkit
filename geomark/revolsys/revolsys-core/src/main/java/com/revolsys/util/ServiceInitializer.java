package com.revolsys.util;

public interface ServiceInitializer {

  static void initializeServices() {
    ServiceInitializerLoader.initializeServices();
  }

  void initializeService();

  int priority();
}
