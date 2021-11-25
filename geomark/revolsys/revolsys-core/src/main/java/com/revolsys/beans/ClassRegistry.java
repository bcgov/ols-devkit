package com.revolsys.beans;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class ClassRegistry<T> {
  /** The cache for super class matches. */
  private final Map<Class<?>, T> findCache = new WeakHashMap<>();

  /** The registry of classes to values. */
  private final Map<Class<?>, T> registry = new HashMap<>();

  /**
   * Clear the cache used by the {@link ClassRegistry#find(Class)} method.
   */
  private void clearFindCache() {
    this.findCache.clear();
  }

  /**
   * Find the value by class. If no direct match was found, a match for the
   * super class will be found until a match is found. Returns null if no match
   * was found on any super class.
   *
   * @param clazz The class.
   * @return The class if a match was found for this class or one of the super
   *         classes or null if no match was found.
   */
  public T find(final Class<?> clazz) {
    T value = this.findCache.get(clazz);
    if (value == null) {
      value = findDo(clazz);
      if (value == null) {
        for (final Class<?> interfaceClass : Classes.getInterfaces(clazz)) {
          value = find(interfaceClass);
          if (value != null) {
            return value;
          }
        }
      }
      if (value != null) {
        this.findCache.put(clazz, value);
      }
    }
    return value;
  }

  public T find(final Object object) {
    if (object == null) {
      return null;
    } else {
      final Class<?> clazz = object.getClass();
      return find(clazz);
    }
  }

  protected T findDo(final Class<?> clazz) {
    if (clazz == null) {
      return null;
    } else {
      T value = get(clazz);
      if (value == null) {
        value = this.findCache.get(clazz);
        if (value == null) {
          final Class<?> superClass = clazz.getSuperclass();
          value = find(superClass);
          if (value != null) {
            this.findCache.put(clazz, value);
          }
        }
      }
      return value;
    }
  }

  /**
   * Get the value from the registry using the key. Returns null if an exact
   * match by class is not found.
   *
   * @param clazz The class.
   * @return The value, or null if no value has been registered for this class.
   */
  public T get(final Class<?> clazz) {
    return this.registry.get(clazz);
  }

  /**
   * Register the value for the specified class.
   *
   * @param clazz The class.
   * @param value The value.
   */
  public void put(final Class<?> clazz, final T value) {
    if (get(clazz) != value) {
      this.registry.put(clazz, value);
      clearFindCache();
    }
  }
}
