package com.revolsys.collection;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class ArrayUtil {
  public static <T> void fill(final T[] array, final Supplier<T> supplier) {
    if (array != null) {
      for (int i = 0; i < array.length; i++) {
        array[i] = supplier.get();
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T[] newArray(final Class<T> clazz, final int size) {
    return (T[])Array.newInstance(clazz, size);
  }

  public static <T> T[] newArray(final Collection<T> list) {
    if (list == null) {
      return null;
    } else {
      @SuppressWarnings("unchecked")
      final T[] array = (T[])new Object[list.size()];
      return list.toArray(array);
    }
  }

  @SafeVarargs
  public static <T> T[] newArray(final T... o) {
    return o;
  }

  public static int[] newDoubleArray(final List<Integer> list) {
    if (list == null) {
      return null;
    } else {
      final int[] array = new int[list.size()];
      for (int i = 0; i < array.length; i++) {
        array[i] = list.get(i);
      }
      return array;
    }
  }

  public static int[] newIntArray(final List<Integer> list) {
    if (list == null) {
      return null;
    } else {
      final int[] array = new int[list.size()];
      for (int i = 0; i < array.length; i++) {
        array[i] = list.get(i);
      }
      return array;
    }
  }
}
