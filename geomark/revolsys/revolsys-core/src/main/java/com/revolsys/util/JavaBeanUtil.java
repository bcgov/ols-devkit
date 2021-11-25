/*
 * Copyright 2004-2005 Revolution Systems Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.beanutils.MethodUtils;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.logging.Logs;

/**
 * The JavaBeanUtil is a utility class that provides methods to set/get
 * properties from an object with no checked exceptions thrown.
 *
 * @author Paul Austin
 */
public final class JavaBeanUtil {
  public static final Class<?>[] ARRAY_CLASS_0 = new Class[0];

  public static final Object[] ARRAY_OBJECT_0 = new Object[0];

  @SuppressWarnings("unchecked")
  public static <V> V createInstance(final String className) {
    try {
      final Class<?> clazz = Class.forName(className);
      return (V)clazz.newInstance();
    } catch (final InstantiationException e) {
      return (V)Exceptions.throwCauseException(e);
    } catch (final Throwable e) {
      return (V)Exceptions.throwUncheckedException(e);
    }
  }

  public static boolean getBooleanValue(final Object object, final String fieldName) {
    if (object == null) {
      return false;
    } else {
      final Object value = Property.get(object, fieldName);
      if (value == null) {
        return false;
      } else if (value instanceof Boolean) {
        final Boolean booleanValue = (Boolean)value;
        return booleanValue;
      } else if (value instanceof Number) {
        final Number number = (Number)value;
        return number.intValue() == 1;
      } else {
        final String stringValue = value.toString();
        if (stringValue.equals("1") || Boolean.parseBoolean(stringValue)) {
          return true;
        } else {
          return false;
        }
      }
    }
  }

  public static <T> Constructor<T> getConstructor(final Class<T> clazz,
    final Class<?>... parameterClasses) {
    try {
      return clazz.getConstructor(parameterClasses);
    } catch (final NoSuchMethodException e) {
      return null;
    } catch (final Throwable e) {
      return Exceptions.throwUncheckedException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> Constructor<T> getConstructor(final String className,
    final Class<?>... parameterClasses) {
    try {
      final Class<T> clazz = (Class<T>)Class.forName(className);
      return clazz.getConstructor(parameterClasses);
    } catch (final NoSuchMethodException e) {
      return null;
    } catch (final Throwable e) {
      return Exceptions.throwUncheckedException(e);
    }
  }

  public static Method getMethod(final Class<?> clazz, final String name,
    final Class<?>... parameterTypes) {
    try {
      final Method method = clazz.getMethod(name, parameterTypes);
      return method;
    } catch (final NoSuchMethodException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static List<Method> getMethods(final Class<?> clazz) {
    final Method[] methods = clazz.getMethods();
    Arrays.sort(methods, new Comparator<Method>() {
      @Override
      public int compare(final Method method1, final Method method2) {
        final String name1 = method1.getName().replaceAll("^(set|get|is)", "").toLowerCase();
        final String name2 = method2.getName().replaceAll("^(set|get|is)", "").toLowerCase();
        final int nameCompare = name1.compareTo(name2);
        return nameCompare;
      }
    });
    return Arrays.asList(methods);
  }

  public static String getPropertyName(final String methodName) {
    String propertyName;
    if (methodName.startsWith("is")) {
      propertyName = methodName.substring(2, 3).toLowerCase() + methodName.substring(3);
    } else {
      propertyName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
    }
    return propertyName;
  }

  public static Class<?> getTypeParameterClass(final Method method,
    final Class<?> expectedRawClass) {
    final Type resultListReturnType = method.getGenericReturnType();
    if (resultListReturnType instanceof ParameterizedType) {
      final ParameterizedType parameterizedType = (ParameterizedType)resultListReturnType;
      final Type rawType = parameterizedType.getRawType();
      if (rawType == expectedRawClass) {
        final Type[] typeArguments = parameterizedType.getActualTypeArguments();
        if (typeArguments.length == 1) {
          final Type resultType = typeArguments[0];
          if (resultType instanceof Class<?>) {
            final Class<?> resultClass = (Class<?>)resultType;
            return resultClass;
          } else {
            throw new IllegalArgumentException(method.getName() + " must return "
              + expectedRawClass.getName() + " with 1 generic type parameter that is a class");
          }
        }
      }
    }
    throw new IllegalArgumentException(method.getName() + " must return "
      + expectedRawClass.getName() + " with 1 generic class parameter");
  }

  public static void initialize(final Class<?> clazz) {
    if (clazz != null) {
      try {
        Class.forName(clazz.getName(), true, clazz.getClassLoader());
      } catch (final ClassNotFoundException e) {
        Logs.error(clazz, "Unable to iniaitlize", e);
      }
    }
  }

  public static <T> T invokeConstructor(final Constructor<? extends T> constructor,
    final Object... args) {
    try {
      final T object = constructor.newInstance(args);
      return object;
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Error e) {
      throw e;
    } catch (final InvocationTargetException e) {
      final Throwable t = e.getTargetException();
      if (t instanceof RuntimeException) {
        throw (RuntimeException)t;
      } else if (t instanceof Error) {
        throw (Error)t;
      } else {
        throw new RuntimeException(t.getMessage(), t);
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static boolean isAssignableFrom(final Collection<Class<?>> classes,
    final Class<?> objectClass) {
    for (final Class<?> allowedClass : classes) {
      if (allowedClass != null) {
        if (allowedClass.isAssignableFrom(objectClass)) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean isAssignableFrom(final Collection<Class<?>> classes, final Object object) {
    Class<?> objectClass;
    if (object == null) {
      return false;
    } else if (object instanceof Class<?>) {
      objectClass = (Class<?>)object;
    } else {
      objectClass = object.getClass();
    }
    return isAssignableFrom(classes, objectClass);
  }

  public static boolean isDefinedInClassLoader(final ClassLoader classLoader,
    final URL resourceUrl) {
    if (classLoader instanceof URLClassLoader) {
      final String resourceUrlString = resourceUrl.toString();
      final URLClassLoader urlClassLoader = (URLClassLoader)classLoader;
      for (final URL url : urlClassLoader.getURLs()) {
        if (resourceUrlString.contains(url.toString())) {
          return true;
        }
      }
      return false;
    } else {
      return true;
    }
  }

  public static <T> T method(final Method method, final Object object, final Object... args) {
    try {
      @SuppressWarnings("unchecked")
      final T result = (T)method.invoke(object, args);
      return result;
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Error e) {
      throw e;
    } catch (final InvocationTargetException e) {
      final Throwable t = e.getTargetException();
      if (t instanceof RuntimeException) {
        throw (RuntimeException)t;
      } else if (t instanceof Error) {
        throw (Error)t;
      } else {
        throw Exceptions.wrap(t);
      }
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T method(final Object object, final String methodName, final Object... args) {
    try {
      return (T)MethodUtils.invokeMethod(object, methodName, args);
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Error e) {
      throw e;
    } catch (final InvocationTargetException e) {
      final Throwable t = e.getTargetException();
      if (t instanceof RuntimeException) {
        throw (RuntimeException)t;
      } else if (t instanceof Error) {
        throw (Error)t;
      } else {
        throw new RuntimeException(t.getMessage(), t);
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Construct a new JavaBeanUtil.
   */
  private JavaBeanUtil() {
  }
}
