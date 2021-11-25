package com.revolsys.util;

import java.util.Set;
import java.util.function.Supplier;

import com.revolsys.collection.set.Sets;

public class Locals {

  public static <V> V get(final ThreadLocal<V> local, final Supplier<V> supplier) {
    V value = local.get();
    if (value == null) {
      value = supplier.get();
      local.set(value);
    }
    return value;
  }

  public static <V> boolean setAdd(final ThreadLocal<Set<V>> local, final V value) {
    final Set<V> collection = get(local, Sets.hashFactory());
    return collection.add(value);
  }

  public static <V> boolean setContains(final ThreadLocal<Set<V>> local, final V value) {
    final Set<V> collection = local.get();
    if (collection == null) {
      return false;
    } else {
      return collection.contains(value);
    }
  }

  public static <V> boolean setRemove(final ThreadLocal<Set<V>> local, final V value) {
    final Set<V> collection = get(local, Sets.hashFactory());
    final boolean removed = collection.remove(value);
    if (removed) {
      if (collection.isEmpty()) {
        local.set(null);
      }
    }
    return removed;
  }
}
