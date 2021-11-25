package com.revolsys.collection.list;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.record.io.format.json.JsonParser;
import com.revolsys.util.Cancellable;
import com.revolsys.util.Property;

public interface Lists {
  Supplier<List<?>> FACTORY_ARRAY = () -> {
    return new ArrayList<>();
  };

  static <V> void addAll(final List<V> list, final Iterable<? extends V> values) {
    if (values != null) {
      for (final V value : values) {
        list.add(value);
      }
    }
  }

  static <V> void addAll(final List<V> list, final Stream<? extends V> values) {
    if (values != null) {
      values.forEach(list::add);
    }
  }

  static <V> void addAll(final List<V> list, @SuppressWarnings("unchecked") final V... values) {
    if (values != null) {
      for (final V value : values) {
        list.add(value);
      }
    }
  }

  static <V> void addListsAll(final List<V> list, final Iterable<? extends Iterable<V>> lists) {
    if (lists != null) {
      for (final Iterable<V> values : lists) {
        for (final V value : values) {
          list.add(value);
        }
      }
    }
  }

  /**
  * Add the value to the list if it is not empty and not already in the list.
  * @param list
  * @param value
  * @return
  */
  static <V> boolean addNotContains(final List<V> list, final int index, final V value) {
    if (Property.hasValue(value)) {
      if (!list.contains(value)) {
        list.add(index, value);
        return true;
      }
    }
    return false;
  }

  /**
   * Add the value to the list if it is not empty and not already in the list.
   * @param list
   * @param value
   * @return
   */
  static <V> boolean addNotContains(final List<V> list, final V value) {
    if (Property.hasValue(value)) {
      if (!list.contains(value)) {
        return list.add(value);
      }
    }
    return false;
  }

  /**
   * Add the value to the list if it is not empty and not already in the list.
   * @param list
   * @param value
   * @return
   */
  static <V> boolean addNotContainsLast(final List<V> list, final V value) {
    if (Property.hasValue(value)) {
      if (list.isEmpty() || !list.get(list.size() - 1).equals(value)) {
        list.add(value);
        return true;
      }
    }
    return false;
  }

  /**
   * Add the value to the list if it is not empty.
   * @param list
   * @param value
   * @return
   */
  static <V> boolean addNotEmpty(final List<V> list, final int index, final V value) {
    if (Property.hasValue(value)) {
      list.add(index, value);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Add the value to the list if it is not empty.
   * @param list
   * @param value
   * @return
   */
  static <V> boolean addNotEmpty(final List<V> list, final V value) {
    if (list != null && Property.hasValue(value)) {
      return list.add(value);
    } else {
      return false;
    }
  }

  static List<? extends Object> arrayToList(final Object value) {
    final List<Object> list = new ArrayList<>();
    if (value instanceof boolean[]) {
      for (final Object item : (boolean[])value) {
        list.add(item);
      }
    } else if (value instanceof Object[]) {
      for (final Object item : (Object[])value) {
        list.add(item);
      }
    } else if (value instanceof byte[]) {
      for (final Object item : (byte[])value) {
        list.add(item);
      }
    } else if (value instanceof short[]) {
      for (final Object item : (short[])value) {
        list.add(item);
      }
    } else if (value instanceof int[]) {
      for (final Object item : (int[])value) {
        list.add(item);
      }
    } else if (value instanceof long[]) {
      for (final Object item : (long[])value) {
        list.add(item);
      }
    } else if (value instanceof float[]) {
      for (final Object item : (float[])value) {
        list.add(item);
      }
    } else if (value instanceof double[]) {
      for (final Object item : (double[])value) {
        list.add(item);
      }
    } else {
      list.add(value);
    }
    return list;
  }

  static <V> ListBuilder<V> buildArray() {
    return new ListBuilder<>(new ArrayList<>());
  }

  static <T> boolean containsReference(final List<WeakReference<T>> list, final T object) {
    for (int i = 0; i < list.size(); i++) {
      final WeakReference<T> reference = list.get(i);
      final T value = reference.get();
      if (value == null) {
        list.remove(i);
      } else if (value == object) {
        return true;
      }
    }
    return false;
  }

  static boolean equalsNotNull(final List<?> list1, final List<?> list2) {
    if (list1.size() != list2.size()) {
      return false;
    } else {
      for (int i = 0; i < list1.size(); i++) {
        final Object value1 = list1.get(i);
        final Object value2 = list2.get(i);
        if (!DataType.equal(value1, value2)) {
          return false;
        }
      }
    }
    return true;
  }

  static boolean equalsNotNull(final List<?> list1, final List<?> list2,
    final Collection<? extends CharSequence> exclude) {
    if (list1.size() != list2.size()) {
      return false;
    } else {
      for (int i = 0; i < list1.size(); i++) {
        final Object value1 = list1.get(i);
        final Object value2 = list2.get(i);
        if (!DataType.equal(value1, value2, exclude)) {
          return false;
        }
      }
    }
    return true;
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  static <V> Supplier<List<V>> factoryArray() {
    return (Supplier)FACTORY_ARRAY;
  }

  static <V> Supplier<List<V>> factoryLinked() {
    return () -> {
      return new LinkedList<>();
    };
  }

  static <V> List<V> filter(final Cancellable cancellable, final List<V> list,
    final Predicate<? super V> filter) {
    if (list != null && !list.isEmpty()) {
      List<V> newList = null;
      int i = 0;
      for (final V value : list) {
        if (cancellable.isCancelled()) {
          return Collections.emptyList();
        }
        if (filter.test(value)) {
          if (newList != null) {
            newList.add(value);
          }
        } else if (newList == null) {
          newList = new ArrayList<>(list.size() - i);
          for (int j = 0; j < i; j++) {
            newList.add(list.get(j));
          }
        }
        i++;
      }
      if (newList == null) {
        return list;
      } else {
        return newList;
      }
    }
    return Collections.emptyList();
  }

  static <V> List<V> filter(final Iterable<V> list, final Predicate<? super V> filter) {
    if (list == null) {
      return Collections.emptyList();
    } else {
      final List<V> newList = new ArrayList<>();
      for (final V value : list) {
        if (filter.test(value)) {
          newList.add(value);
        }
      }
      return newList;
    }
  }

  static int getClassCount(final List<?> list, final Class<?> clazz) {
    int count = 0;
    for (int i = 0; i < list.size(); i++) {
      final Object value = list.get(i);
      if (value == null) {
        list.remove(i);
      } else if (clazz.isAssignableFrom(value.getClass())) {
        count++;
      }
    }
    return count++;
  }

  static <T> int getReferenceClassCount(final List<WeakReference<T>> list, final Class<?> clazz) {
    int count = 0;
    for (int i = 0; i < list.size(); i++) {
      final WeakReference<?> reference = list.get(i);
      final Object value = reference.get();
      if (value == null) {
        list.remove(i);
      } else if (clazz.isAssignableFrom(value.getClass())) {
        count++;
      }
    }
    return count++;
  }

  static <T> List<T> getReferences(final List<WeakReference<T>> list) {
    final List<T> values = new ArrayList<>();
    for (int i = 0; i < list.size(); i++) {
      final WeakReference<T> reference = list.get(i);
      final T value = reference.get();
      if (value == null) {
        list.remove(i);
      } else {
        values.add(value);
      }
    }
    return values;
  }

  static <V> LinkedList<V> linked(@SuppressWarnings("unchecked") final V... values) {
    final LinkedList<V> list = new LinkedList<>();
    addAll(list, values);
    return list;
  }

  @SafeVarargs
  static <IN, OUT> List<OUT> map(final Function<? super IN, OUT> converter, final IN... list) {
    if (list == null) {
      return Collections.emptyList();
    } else {
      final List<OUT> newList = new ArrayList<>();
      for (final IN value : list) {
        final OUT newValue = converter.apply(value);
        newList.add(newValue);
      }
      return newList;
    }
  }

  static <IN, OUT> List<OUT> map(final Iterable<IN> list,
    final Function<? super IN, OUT> converter) {
    if (list == null) {
      return Collections.emptyList();
    } else {
      final List<OUT> newList = new ArrayList<>();
      for (final IN value : list) {
        final OUT newValue = converter.apply(value);
        newList.add(newValue);
      }
      return newList;
    }
  }

  static <V> List<V> newArray(final BiConsumer<Consumer<V>, Predicate<V>> forEachFunction,
    final Predicate<V> filter) {
    final List<V> values = new ArrayList<>();
    forEachFunction.accept(values::add, filter);
    return values;
  }

  static <V> ArrayList<V> newArray(final Consumer<Consumer<V>> action) {
    final ArrayList<V> list = new ArrayList<>();
    action.accept(list::add);
    return list;
  }

  static List<Double> newArray(final double... values) {
    if (values == null) {
      return Collections.emptyList();
    } else {
      final List<Double> list = new ArrayList<>();
      for (final double value : values) {
        list.add(value);
      }
      return list;
    }
  }

  static List<Integer> newArray(final int... values) {
    if (values == null) {
      return Collections.emptyList();
    } else {
      final List<Integer> list = new ArrayList<>();
      for (final int value : values) {
        list.add(value);
      }
      return list;
    }
  }

  static <V> ArrayList<V> newArray(@SuppressWarnings("unchecked") final V... values) {
    final ArrayList<V> list = new ArrayList<>();
    addAll(list, values);
    return list;
  }

  static List<Double> newArrayDouble(final double... values) {
    if (values == null) {
      return Collections.emptyList();
    } else {
      final List<Double> list = new ArrayList<>();
      for (final double value : values) {
        list.add(value);
      }
      return list;
    }
  }

  static List<Integer> newArrayInt(final int... values) {
    if (values == null) {
      return Collections.emptyList();
    } else {
      final List<Integer> list = new ArrayList<>();
      for (final int value : values) {
        list.add(value);
      }
      return list;
    }
  }

  static <V> List<V> newArraySorted(final BiConsumer<Consumer<V>, Predicate<V>> forEachFunction,
    final Predicate<V> filter, final Comparator<V> comparator) {
    final List<V> values = new ArrayList<>();
    forEachFunction.accept(values::add, filter);
    values.sort(comparator);
    return values;
  }

  static <T> void removeReference(final List<WeakReference<T>> list, final T object) {
    for (int i = 0; i < list.size(); i++) {
      final WeakReference<T> reference = list.get(i);
      final T value = reference.get();
      if (value == null) {
        list.remove(i);
      } else if (value == object) {
        list.remove(i);
      }
    }
  }

  static List<String> split(final String text, final String regex) {
    if (Property.hasValue(text)) {
      return Arrays.asList(text.split(regex));
    } else {
      return Collections.emptyList();
    }
  }

  static <V> List<V> to(final Supplier<List<V>> factory, final Iterable<? extends V> values) {
    final List<V> list = factory.get();
    addAll(list, values);
    return list;
  }

  static <V> List<V> toArray(final Iterable<? extends V> values) {
    final List<V> list = new ArrayList<>();
    addAll(list, values);
    return list;
  }

  static <T> List<T> toArray(final Iterable<T> iterable, final int size) {
    final List<T> list = new ArrayList<>(size);
    int i = 0;
    for (final T value : iterable) {
      if (i < size) {
        list.add(value);
        i++;
      } else {
        return list;
      }
    }
    return list;
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  static <V> List<V> toArray(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof List) {
      return (List)value;
    } else if (value instanceof Iterable) {
      final Iterable<Object> iterable = (Iterable)value;
      return (List<V>)toArray(iterable);
    } else if (value instanceof Number) {
      final List<V> list = new ArrayList<>();
      list.add((V)value);
      return list;
    } else {
      final String string = DataTypes.toString(value);
      return toArray(string);
    }
  }

  static <V> List<V> toArray(final Stream<? extends V> values) {
    final List<V> list = new ArrayList<>();
    addAll(list, values);
    return list;
  }

  @SuppressWarnings("unchecked")
  static <V> List<V> toArray(final String string) {
    final Object value = JsonParser.read(string);
    if (value instanceof List) {
      return (List<V>)value;
    } else {
      throw new IllegalArgumentException("Value must be a JSON list " + string);
    }
  }

  static <V> List<V> toArrayThreadSafe(final List<? extends V> values) {
    final List<V> list = new ArrayList<>(values.size());
    for (int i = 0; i < values.size(); i++) {
      try {
        final V value = values.get(i);
        list.add(value);
      } catch (final IndexOutOfBoundsException e) {
      }
    }
    return list;
  }

  static <V> List<V> toList(final Supplier<List<V>> constructor,
    final Iterable<? extends V> values) {
    final List<V> list = constructor.get();
    addAll(list, values);
    return list;
  }

  static String toString(final Object value) {
    final Collection<?> collection;
    if (value instanceof Collection) {
      collection = (Collection<?>)value;
    } else {
      collection = toArray(value);
    }
    if (value == null) {
      return null;
    } else {

      final StringBuilder string = new StringBuilder("[");
      for (final Iterator<?> iterator = collection.iterator(); iterator.hasNext();) {
        final Object object = iterator.next();
        final String stringValue = DataTypes.toString(object);
        string.append(stringValue);
        if (iterator.hasNext()) {
          string.append(", ");
        }
      }
      string.append("]");
      return string.toString();
    }

  }

  static <V> List<V> unmodifiable(final Iterable<? extends V> values) {
    return new UnmodifiableArrayList<>(values);
  }

  static <V> List<V> unmodifiable(@SuppressWarnings("unchecked") final V... values) {
    return new UnmodifiableArrayList<>(values);
  }
}
