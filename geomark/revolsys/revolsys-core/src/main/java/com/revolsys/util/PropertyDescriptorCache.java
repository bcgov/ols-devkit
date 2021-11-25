package com.revolsys.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import com.revolsys.collection.map.Maps;

public class PropertyDescriptorCache {
  private static Map<Class<?>, Map<String, PropertyDescriptor>> propertyDescriptorByClassAndName = new WeakHashMap<>();

  private static Map<Class<?>, Map<String, Method>> propertyWriteMethodByClassAndName = new WeakHashMap<>();

  public static void clearCache() {
    synchronized (propertyDescriptorByClassAndName) {
      propertyDescriptorByClassAndName.clear();
      propertyWriteMethodByClassAndName.clear();
    }
  }

  public static PropertyDescriptor getPropertyDescriptor(final Class<?> clazz,
    final String propertyName) {
    final Map<String, PropertyDescriptor> propertyDescriptors = getPropertyDescriptors(clazz);
    return propertyDescriptors.get(propertyName);
  }

  public static PropertyDescriptor getPropertyDescriptor(final Object object,
    final String propertyName) {
    if (object == null) {
      return null;
    } else {
      final Class<? extends Object> clazz = object.getClass();
      return getPropertyDescriptor(clazz, propertyName);
    }
  }

  protected static Map<String, PropertyDescriptor> getPropertyDescriptors(final Class<?> clazz) {
    synchronized (propertyDescriptorByClassAndName) {
      Map<String, PropertyDescriptor> propertyDescriptors = propertyDescriptorByClassAndName
        .get(clazz);
      if (propertyDescriptors == null) {
        propertyDescriptors = new HashMap<>();
        try {
          final BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
          for (final PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
            final String propertyName = propertyDescriptor.getName();
            propertyDescriptors.put(propertyName, propertyDescriptor);
            Method writeMethod = propertyDescriptor.getWriteMethod();
            if (writeMethod == null) {
              final String setMethodName = "set" + Character.toUpperCase(propertyName.charAt(0))
                + propertyName.substring(1);
              try {
                final Class<?> propertyType = propertyDescriptor.getPropertyType();
                writeMethod = clazz.getMethod(setMethodName, propertyType);
                propertyDescriptor.setWriteMethod(writeMethod);
              } catch (NoSuchMethodException | SecurityException e) {
              }
            }
            Maps.put(propertyWriteMethodByClassAndName, clazz, propertyName, writeMethod);
          }
        } catch (final IntrospectionException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        propertyDescriptorByClassAndName.put(clazz, propertyDescriptors);
      }
      return propertyDescriptors;
    }
  }

  protected static Map<String, Method> getWriteMethods(final Class<?> clazz) {
    synchronized (propertyDescriptorByClassAndName) {
      Map<String, Method> writeMethods = propertyWriteMethodByClassAndName.get(clazz);
      if (writeMethods == null) {
        getPropertyDescriptors(clazz);
        writeMethods = propertyWriteMethodByClassAndName.get(clazz);
      }
      return writeMethods;
    }
  }

}
