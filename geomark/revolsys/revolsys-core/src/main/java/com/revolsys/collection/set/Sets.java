package com.revolsys.collection.set;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

public class Sets {
  @SafeVarargs
  public static <V> void addAll(final Set<V> set, final Collection<? extends V>... collections) {
    for (final Collection<? extends V> collection : collections) {
      if (collection != null) {
        set.addAll(collection);
      }
    }
  }

  public static <V> void addAll(final Set<V> set, final Iterable<? extends V> values) {
    if (set != null && values != null) {
      for (final V value : values) {
        set.add(value);
      }
    }
  }

  @SafeVarargs
  public static <V> Set<V> all(final Supplier<Set<V>> factory,
    final Collection<? extends V>... collections) {
    final Set<V> set = factory.get();
    addAll(set, collections);
    return set;
  }

  public static <V> Supplier<Set<V>> hashFactory() {
    return () -> {
      return new HashSet<>();
    };
  }

  public static <V> Supplier<Set<V>> linkedHashFactory() {
    return () -> {
      return new LinkedHashSet<>();
    };
  }

  public static <V> HashSet<V> newHash() {
    return new HashSet<>();
  }

  public static <V> HashSet<V> newHash(final Iterable<? extends V> values) {
    final HashSet<V> set = new HashSet<>();
    if (values != null) {
      for (final V value : values) {
        set.add(value);
      }
    }
    return set;
  }

  public static <V> HashSet<V> newHash(@SuppressWarnings("unchecked") final V... values) {
    final HashSet<V> set = newHash();
    if (values != null) {
      for (final V value : values) {
        set.add(value);
      }
    }
    return set;
  }

  public static <V> LinkedHashSet<V> newLinkedHash() {
    return new LinkedHashSet<>();
  }

  public static <V> LinkedHashSet<V> newLinkedHash(final Iterable<? extends V> values) {
    final LinkedHashSet<V> set = newLinkedHash();
    addAll(set, values);
    return set;
  }

  public static <V> LinkedHashSet<V> newLinkedHash(
    @SuppressWarnings("unchecked") final V... values) {
    final LinkedHashSet<V> set = new LinkedHashSet<>();
    if (values != null) {
      for (final V value : values) {
        set.add(value);
      }
    }
    return set;
  }

  public static <V> LinkedHashSet<V> newLinkedHash(final V value) {
    final LinkedHashSet<V> set = new LinkedHashSet<>();
    if (value != null) {
      set.add(value);
    }
    return set;
  }

  public static <V> TreeSet<V> newTree(final Comparator<V> comparator, final Iterable<V> values) {
    final TreeSet<V> set = new TreeSet<>(comparator);
    addAll(set, values);
    return set;
  }

  public static <V> TreeSet<V> newTree(final Iterable<? extends V> values) {
    final TreeSet<V> set = new TreeSet<>();
    if (values != null) {
      for (final V value : values) {
        set.add(value);
      }
    }
    return set;
  }

  public static <V> TreeSet<V> newTree(final V value) {
    final TreeSet<V> set = new TreeSet<>();
    if (value != null) {
      set.add(value);
    }
    return set;
  }

  @SafeVarargs
  public static <V> TreeSet<V> treeAll(final Collection<? extends V>... collections) {
    final TreeSet<V> set = new TreeSet<>();
    addAll(set, collections);
    return set;
  }

  public static <V> Supplier<Set<V>> treeFactory() {
    return () -> {
      return new TreeSet<>();
    };
  }

  public static <V> Supplier<Set<V>> treeFactory(final Comparator<V> comparator) {
    return () -> {
      return new TreeSet<>(comparator);
    };
  }

  public static <V> Set<V> unmodifiableLinked(final Iterable<V> values) {
    if (values == null) {
      return Collections.emptySet();
    } else {
      final Set<V> set = newLinkedHash(values);
      return Collections.unmodifiableSet(set);
    }
  }
}
