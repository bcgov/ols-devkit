package com.revolsys.collection.map;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

public class ThreadSharedProperties {
  private static Map<Object, Object> defaultProperties = new WeakHashMap<>();

  private static ThreadLocal<Map<Object, Object>> threadProperties = new ThreadLocal<>();

  private static Map<ThreadGroup, Map<Object, Object>> threadGroupProperties = new WeakHashMap<>();

  public static void clearProperties() {
    final Map<Object, Object> properties = getLocalProperties();
    synchronized (properties) {
      properties.clear();
    }
  }

  public static void clearThreadGroup(final ThreadGroup threadGroup) {
    synchronized (threadGroupProperties) {
      threadGroupProperties.remove(threadGroup);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T getDefaultProperty(final Object name) {
    synchronized (defaultProperties) {
      final T value = (T)defaultProperties.get(name);
      return value;
    }
  }

  private static Map<Object, Object> getLocalProperties() {
    Map<Object, Object> properties = threadProperties.get();
    if (properties == null) {
      properties = getThreadGroupProperties();
      threadProperties.set(properties);
    }
    return properties;
  }

  public static Map<String, Object> getProperties() {
    final Map<Object, Object> properties = getLocalProperties();
    synchronized (properties) {
      final HashMap<String, Object> map = new HashMap<>();
      for (final Entry<Object, Object> entry : properties.entrySet()) {
        final Object key = entry.getKey();
        if (key instanceof String) {
          final String name = (String)key;
          final Object value = entry.getValue();
          map.put(name, value);
        }
      }
      return map;
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T getProperty(final Object name) {
    final Map<Object, Object> properties = getLocalProperties();
    synchronized (properties) {
      return (T)properties.get(name);
    }
  }

  public static Map<Object, Object> getThreadGroupProperties() {
    synchronized (threadGroupProperties) {
      Map<Object, Object> properties = null;
      final Thread thread = Thread.currentThread();
      final ThreadGroup threadGroup = thread.getThreadGroup();
      if (threadGroup != null) {
        properties = threadGroupProperties.get(threadGroup);
      }
      if (properties == null) {
        properties = new HashMap<>(defaultProperties);
      }
      return properties;
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T getThreadGroupProperty(final Object name) {
    final Map<Object, Object> properties = getThreadGroupProperties();
    synchronized (properties) {
      final T value = (T)properties.get(name);
      if (value == null) {
        return (T)getDefaultProperty(name);
      }
      return value;
    }
  }

  public static void initialiseThreadGroup(final ThreadGroup threadGroup) {
    final Map<Object, Object> properties = getLocalProperties();
    synchronized (threadGroupProperties) {
      threadGroupProperties.put(threadGroup, properties);
    }
  }

  public static void setDefaultProperties(final Map<? extends Object, Object> values) {
    synchronized (defaultProperties) {
      defaultProperties.putAll(values);
    }
  }

  public static void setDefaultProperty(final Object name, final Object value) {
    synchronized (defaultProperties) {
      defaultProperties.put(name, value);
    }
  }

  public static void setProperties(final Map<? extends Object, Object> values) {
    final Map<Object, Object> properties = getLocalProperties();
    synchronized (properties) {
      properties.putAll(values);
    }
  }

  public static void setProperty(final Object name, final Object value) {
    final Map<Object, Object> properties = getLocalProperties();
    synchronized (properties) {
      properties.put(name, value);
    }
  }
}
