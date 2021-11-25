package com.revolsys.record;

public interface Available {
  default boolean isAvailable() {
    return true;
  }
}
