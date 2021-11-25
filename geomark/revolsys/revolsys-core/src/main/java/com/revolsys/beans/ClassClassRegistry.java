package com.revolsys.beans;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class ClassClassRegistry<T> {
  /** The cache for super class matches. */
  private final Map<Class<?>, Class<?>> findCache = new HashMap<>();

  /** The registry of classes to values. */
  private final Map<Class<?>, Class<?>> registry = new HashMap<>();

  /**
   * Clear the cache used by the {@link ClassClassRegistry#findClass(Class)}
   * method.
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
  public Class<?> findClass(final Class<?> clazz) {
    if (clazz == null) {
      return null;
    } else {
      Class<?> value = getClass(clazz);
      if (value == null) {
        value = this.findCache.get(clazz);
        if (value == null) {
          final Class<?> superClass = clazz.getSuperclass();
          value = findClass(superClass);
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
  public Class<?> getClass(final Class<?> clazz) {
    return this.registry.get(clazz);
  }

  public T newInstance(final Class<?> clazz, final Object... parameters) {
    final Class<?> instanceClazz = findClass(clazz);
    if (instanceClazz == null) {
      return null;
    } else {
      final Class<?>[] parameterClasses = new Class<?>[parameters.length];
      for (int i = 0; i < parameters.length; i++) {
        final Object parameter = parameters[i];
        final Class<?> paramaterClass = parameter.getClass();
        parameterClasses[i] = paramaterClass;
      }
      try {
        final Constructor<?> constructor = instanceClazz.getConstructor(parameterClasses);
        return (T)constructor.newInstance(parameters);
      } catch (final Throwable t) {
        t.printStackTrace();
        return null;
      }
    }

  }

  /**
   * Register the value for the specified class.
   *
   * @param clazz The class.
   * @param value The value.
   */
  public void put(final Class<?> clazz, final Class<?> value) {
    if (getClass(clazz) != value) {
      this.registry.put(clazz, value);
      clearFindCache();
    }
  }
}
