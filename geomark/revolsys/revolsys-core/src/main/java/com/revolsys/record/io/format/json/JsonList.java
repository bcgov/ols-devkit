package com.revolsys.record.io.format.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;

public interface JsonList extends List<Object>, JsonType {

  JsonList EMPTY = new JsonList() {

    @Override
    public void add(final int index, final Object element) {
    }

    @Override
    public boolean add(final Object e) {
      return false;
    }

    @Override
    public boolean addAll(final Collection<? extends Object> c) {
      return false;
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends Object> c) {
      return false;
    }

    @Override
    public void clear() {
    }

    @Override
    public JsonList clone() {
      return this;
    }

    @Override
    public boolean contains(final Object o) {
      return false;
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
      return false;
    }

    @Override
    public boolean equals(final Object object,
      final Collection<? extends CharSequence> excludeFieldNames) {
      return false;
    }

    @Override
    public Object get(final int index) {
      return null;
    }

    @Override
    public int indexOf(final Object o) {
      return -1;
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public Iterator<Object> iterator() {
      return Collections.emptyIterator();
    }

    @Override
    public int lastIndexOf(final Object o) {
      return -1;
    }

    @Override
    public ListIterator<Object> listIterator() {
      return Collections.emptyListIterator();
    }

    @Override
    public ListIterator<Object> listIterator(final int index) {
      return Collections.emptyListIterator();
    }

    @Override
    public Object remove(final int index) {
      return null;
    }

    @Override
    public boolean remove(final Object o) {
      return false;
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
      return false;
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
      return false;
    }

    @Override
    public Object set(final int index, final Object element) {
      return null;
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public List<Object> subList(final int fromIndex, final int toIndex) {
      return this;
    }

    @Override
    public Object[] toArray() {
      return new Object[0];
    }

    @Override
    public <T> T[] toArray(final T[] a) {
      return a;
    }

  };

  static JsonList array() {
    return new JsonListArray();
  }

  static JsonList array(final Collection<?> collection) {
    return new JsonListArray(collection);
  }

  static JsonList array(final Object value) {
    return new JsonListArray(value);
  }

  static JsonList array(final Object... values) {
    return new JsonListArray(values);
  }

  default boolean addIfNotContains(final Object value) {
    final boolean notContains = !contains(value);
    if (notContains) {
      add(value);
    }
    return notContains;
  }

  default boolean addIfNotContains(final Object value,
    final Collection<? extends CharSequence> excludeFieldNames) {
    final boolean notContains = !contains(value, excludeFieldNames);
    if (notContains) {
      add(value);
    }
    return notContains;
  }

  default JsonList addValuesClone(final Collection<?> values) {
    for (Object value : values) {
      if (value != null) {
        value = JsonType.toJsonClone(value);
      }
      add(value);
    }
    return this;
  }

  @Override
  default Appendable appendJson(final Appendable appendable) {
    try {
      appendable.append('[');
      boolean first = true;
      for (final Object value : this) {
        if (first) {
          first = false;
        } else {
          appendable.append(',');
        }
        JsonWriterUtil.appendValue(appendable, value);
      }
      appendable.append(']');
      return appendable;
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  default JsonList asJson() {
    return (JsonList)JsonType.super.asJson();
  }

  default boolean contains(final Object value,
    final Collection<? extends CharSequence> excludeFieldNames) {
    final int size = size();
    for (int i = 0; i < size; i++) {
      final Object listValue = get(i);
      if (DataType.equal(value, listValue, excludeFieldNames)) {
        return true;
      }
    }
    return false;
  }

  default boolean equals(final Object value1, final Object value2,
    final Collection<? extends CharSequence> excludeFieldNames) {
    final List<?> list1 = (List<?>)value1;
    final List<?> list2 = (List<?>)value2;
    if (list1.size() != list2.size()) {
      return false;
    } else {
      for (int i = 0; i < list1.size(); i++) {
        final Object value11 = list1.get(i);
        final Object value21 = list2.get(i);
        if (!DataType.equal(value11, value21, excludeFieldNames)) {
          return false;
        }
      }
    }
    return true;
  }

  default <V> void forEachType(final Consumer<V> action) {
    List.super.forEach(value -> {
      action.accept((V)value);
    });
  }

  default Integer getInteger(final int index) {
    return getValue(index, DataTypes.INT);
  }

  default int getInteger(final int index, final int defaultValue) {
    final Integer value = getInteger(index);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  default String getString(final int index) {
    return getValue(index, DataTypes.STRING);
  }

  default String getString(final int index, final String defaultValue) {
    final String value = getString(index);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  @SuppressWarnings("unchecked")
  default <V> V getValue(final int index) {
    return (V)get(index);
  }

  default <T extends Object> T getValue(final int index, final DataType dataType) {
    final Object value = get(index);
    return dataType.toObject(value);
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  default Iterable<JsonObject> jsonObjects() {
    return (Iterable)this;
  }

  default <V> List<V> mapTo(final Function<JsonObject, V> mapper) {
    final List<V> objects = new ArrayList<>();
    forEachType((final JsonObject record) -> {
      final V object = mapper.apply(record);
      objects.add(object);
    });
    return objects;
  }

  @Override
  default JsonList toJson() {
    return (JsonList)JsonType.super.toJson();
  }

  @Override
  default String toJsonString(final boolean indent) {
    return Json.toString(this, indent);
  }
}
