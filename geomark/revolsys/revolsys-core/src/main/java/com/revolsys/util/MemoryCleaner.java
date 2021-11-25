package com.revolsys.util;

import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;

public class MemoryCleaner {

  private static Cleaner cleaner;

  public static synchronized Cleaner getCleaner() {
    if (cleaner == null) {
      cleaner = Cleaner.create();
    }
    return cleaner;
  }

  public static Cleanable register(final Object object, final Runnable action) {
    final Cleaner cleaner = getCleaner();
    return cleaner.register(object, action);
  }

}
