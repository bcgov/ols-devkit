package com.revolsys.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jeometry.common.exception.Exceptions;

public interface BaseCloneable extends Cloneable {

  /**
   * Clone the value if it has a clone method.
   *
   * @param value The value to clone.
   * @return The cloned value.
   */
  @SuppressWarnings("unchecked")
  static <V> V clone(final V value) {
    if (value == null) {
      return null;
    } else if (value instanceof BaseCloneable) {
      return (V)((BaseCloneable)value).clone();
    } else if (value instanceof Map) {
      final Map<Object, Object> sourceMap = (Map<Object, Object>)value;
      final Map<Object, Object> map = new LinkedHashMap<>(sourceMap);
      for (final Entry<Object, Object> entry : sourceMap.entrySet()) {
        final Object key = entry.getKey();
        final Object mapValue = entry.getValue();
        final Object clonedMapValue = clone(mapValue);
        map.put(key, clonedMapValue);
      }
      return (V)map;
    } else if (value instanceof List) {
      final List<?> list = (List<?>)value;
      final List<Object> cloneList = new ArrayList<>();
      for (final Object object : list) {
        final Object clonedObject = clone(object);
        cloneList.add(clonedObject);
      }
      return (V)cloneList;
    } else if (value instanceof Cloneable) {
      try {
        final Class<? extends Object> valueClass = value.getClass();
        final Method method = valueClass.getMethod("clone", JavaBeanUtil.ARRAY_CLASS_0);
        if (method != null) {
          return (V)method.invoke(value, JavaBeanUtil.ARRAY_OBJECT_0);
        }
      } catch (final Throwable e) {
        return Exceptions.throwUncheckedException(e);
      }
    }
    return value;
  }

  public Object clone();
}
