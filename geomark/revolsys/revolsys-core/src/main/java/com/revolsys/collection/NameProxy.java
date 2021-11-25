package com.revolsys.collection;

import java.util.function.Consumer;
import java.util.function.Function;

public interface NameProxy {
  static boolean acceptName(final Object object, final Consumer<String> consumer) {
    final String name = getName(object);
    if (name != null) {
      consumer.accept(name);
      return true;
    }
    return false;
  }

  static <V> V applyName(final Object object, final Function<String, V> function) {
    return applyName(object, function, null);
  }

  static <V> V applyName(final Object object, final Function<String, V> function,
    final V defaultValue) {
    final String name = getName(object);
    if (name != null) {
      return function.apply(name);
    }
    return defaultValue;
  }

  static boolean equalsName(final Object object, final String otherName) {
    final String name = getName(object);
    if (name == null) {
      return otherName == null;
    } else {
      return name.equals(otherName);
    }
  }

  static String getName(final Object object) {
    if (object instanceof NameProxy) {
      final NameProxy proxy = (NameProxy)object;
      return proxy.getName();
    }
    return null;
  }

  String getName();
}
