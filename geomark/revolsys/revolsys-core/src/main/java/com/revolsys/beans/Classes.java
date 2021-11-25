package com.revolsys.beans;

import java.util.LinkedHashSet;
import java.util.Set;

public class Classes {
  public static void addInterfaces(final Set<Class<? extends Object>> classes,
    final Class<? extends Object> clazz) {
    if (clazz != null) {
      final Class<?>[] interfaceClasses = clazz.getInterfaces();
      for (final Class<?> interfaceClass : interfaceClasses) {
        addSuperClassesAndInterfaces(classes, interfaceClass);
      }
    }
  }

  public static void addSuperClasses(final Set<Class<? extends Object>> classes,
    final Class<? extends Object> clazz) {
    if (clazz != null) {
      classes.add(clazz);
      final Class<?> superclass = clazz.getSuperclass();
      addSuperClasses(classes, superclass);
    }
  }

  public static void addSuperClassesAndInterfaces(final Set<Class<? extends Object>> classes,
    final Class<? extends Object> clazz) {
    if (clazz != null) {
      classes.add(clazz);
      final Class<?>[] interfaceClasses = clazz.getInterfaces();
      for (final Class<?> interfaceClass : interfaceClasses) {
        addSuperClassesAndInterfaces(classes, interfaceClass);
      }
      final Class<?> superClass = clazz.getSuperclass();
      addSuperClassesAndInterfaces(classes, superClass);
    }
  }

  public static String className(final Object value) {
    if (value == null) {
      return null;
    }
    Class<?> clazz;
    if (value instanceof Class) {
      clazz = (Class<?>)value;
    } else {
      clazz = value.getClass();
    }
    return clazz.getSimpleName();
  }

  public static Set<Class<? extends Object>> getInterfaces(final Class<? extends Object> clazz) {
    final Set<Class<? extends Object>> classes = new LinkedHashSet<>();
    addInterfaces(classes, clazz);
    return classes;
  }

  public static Set<Class<? extends Object>> getSuperClasses(final Class<? extends Object> clazz) {
    final Set<Class<? extends Object>> classes = new LinkedHashSet<>();
    addSuperClasses(classes, clazz);
    return classes;
  }

  public static Set<Class<? extends Object>> getSuperClassesAndInterfaces(
    final Class<? extends Object> clazz) {
    final Set<Class<? extends Object>> classes = new LinkedHashSet<>();
    addSuperClassesAndInterfaces(classes, clazz);
    return classes;
  }

  public static String packageName(final Class<?> classDef) {
    if (classDef != null) {
      final Package packageDef = classDef.getPackage();
      if (packageDef != null) {
        final String packageName = packageDef.getName();
        return packageName;
      }
    }
    return "";
  }

  public static String packagePath(final Class<?> classDef) {
    final String packageName = packageName(classDef);
    return "/" + packageName.replaceAll("\\.", "/");
  }
}
