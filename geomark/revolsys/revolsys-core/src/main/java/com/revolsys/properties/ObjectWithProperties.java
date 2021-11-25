package com.revolsys.properties;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PreDestroy;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.ThreadSharedProperties;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.util.Property;

public interface ObjectWithProperties {

  @SuppressWarnings("unchecked")
  static <C> C getProperty(final ObjectWithProperties object, final Map<String, Object> properties,
    final String name) {
    if (properties == null) {
      return null;
    } else {
      Object value = properties.get(name);
      if (value instanceof Reference) {
        final Reference<C> reference = (Reference<C>)value;
        if (reference.isEnqueued()) {
          value = null;
        } else {
          value = reference.get();
        }
        if (value == null) {
          properties.remove(name);
        }
      }
      if (value instanceof ObjectPropertyProxy) {
        final ObjectPropertyProxy<C, Object> proxy = (ObjectPropertyProxy<C, Object>)value;
        value = proxy.getValue(object);
      }
      return (C)value;
    }
  }

  static void setProperties(final Object object, final Map<String, ? extends Object> properties) {
    if (properties != null) {
      if (object instanceof ObjectWithProperties) {
        final ObjectWithProperties objectWithProperties = (ObjectWithProperties)object;
        objectWithProperties.setProperties(properties);
      } else if (object != null) {
        for (final Entry<String, ? extends Object> entry : properties.entrySet()) {
          final String name = entry.getKey();
          final Object value = entry.getValue();
          setProperty(object, name, value);
        }
      }
    }
  }

  static void setProperty(final Object object, final String name, final Object value) {
    try {
      Property.setSimple(object, name, value);
    } catch (final Throwable e) {
    }
  }

  @SuppressWarnings("unchecked")
  default <V extends ObjectWithProperties> V addProperty(final String name, final Object value) {
    setProperty(name, value);
    return (V)this;
  }

  default void clearProperties() {
    final Map<String, Object> properties = getProperties();
    properties.clear();
  }

  @PreDestroy
  default void close() {
    clearProperties();
  }

  MapEx getProperties();

  default <C> C getProperty(final String name) {
    C value = Property.getSimple(this, name);
    if (value == null) {
      final Map<String, Object> properties = getProperties();
      value = getProperty(this, properties, name);
    }
    return value;
  }

  @SuppressWarnings("unchecked")
  default <C> C getProperty(final String name, final C defaultValue) {
    final C value = (C)getProperty(name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  default Map<String, Object> getThreadProperties() {
    Map<String, Object> properties = ThreadSharedProperties.getProperty(this);
    if (properties == null) {
      properties = new HashMap<>();
      ThreadSharedProperties.setProperty(this, properties);
    }
    return properties;
  }

  @SuppressWarnings("unchecked")
  default <T> T getThreadProperty(final String name) {
    final Map<String, Object> properties = getThreadProperties();
    final T value = (T)properties.get(name);
    return value;
  }

  default boolean hasProperty(final String name) {
    final Object value = getProperty(name);
    return Property.hasValue(value);
  }

  default boolean isPropertyEqual(final String name, final Object value) {
    final Object propertyValue = getProperty(name);
    return DataType.equal(value, propertyValue);
  }

  default void removeProperty(final String propertyName) {
    final Map<String, Object> properties = getProperties();
    properties.remove(propertyName);
  }

  default void setProperties(final Map<String, ? extends Object> properties) {
    if (properties != null) {
      for (final Entry<String, ? extends Object> entry : properties.entrySet()) {
        final String name = entry.getKey();
        final Object value = entry.getValue();
        setProperty(name, value);
      }
    }
  }

  default void setProperty(final String name, final Object value) {
    try {
      if (!Property.setSimple(this, name, value)) {
        final Map<String, Object> properties = getProperties();
        if (!MapObjectFactory.TYPE.equals(name)) {
          properties.put(name, value);
        }
      }
    } catch (final Throwable e) {
      setPropertyError(name, value, e);
    }
  }

  default void setPropertyError(final String name, final Object value, final Throwable e) {
    Logs.debug(this, "Error setting " + name + '=' + value, e);
  }

  default void setPropertySoft(final String name, final Object value) {
    setProperty(name, new SoftReference<>(value));
  }

  default void setPropertyWeak(final String name, final Object value) {
    setProperty(name, new WeakReference<>(value));
  }

  default void setThreadProperty(final String name, final Object value) {
    final Map<String, Object> properties = getThreadProperties();
    properties.put(name, value);
  }
}
