package com.revolsys.util;

import java.util.ArrayList;
import java.util.List;

public class RunnableInitializers {
  private final List<Runnable> initializers = new ArrayList<>();

  private boolean initialized;

  public synchronized void addInitializer(final Runnable initializer) {
    if (this.initialized) {
      initializer.run();
    } else {
      this.initializers.add(initializer);
    }
  }

  public synchronized void initialize() {
    if (!this.initialized) {
      for (final Runnable initializer : this.initializers) {
        initializer.run();
      }
      this.initializers.clear();
      this.initialized = true;
    }
  }
}
