package com.revolsys.record.io.format.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jeometry.common.data.type.DataType;

public class JsonListArray extends ArrayList<Object> implements JsonList {
  private static final long serialVersionUID = 1L;

  JsonListArray() {
  }

  JsonListArray(final Collection<? extends Object> c) {
    super(c);
  }

  JsonListArray(final int initialCapacity) {
    super(initialCapacity);
  }

  JsonListArray(final Object value) {
    add(value);
  }

  JsonListArray(final Object... values) {
    for (final Object value : values) {
      add(value);
    }
  }

  @Override
  public JsonList clone() {
    return new JsonListArray()//
      .addValuesClone(this);
  }

  @Override
  public boolean equals(final Object value2) {
    if (value2 instanceof List<?>) {
      final List<?> list2 = (List<?>)value2;
      if (size() != list2.size()) {
        return false;
      } else {
        for (int i = 0; i < size(); i++) {
          final Object value11 = get(i);
          final Object value21 = list2.get(i);
          if (!DataType.equal(value11, value21)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  @Override
  public boolean equals(final Object object,
    final Collection<? extends CharSequence> excludeFieldNames) {
    if (object instanceof List<?>) {
      final List<?> list2 = (List<?>)object;
      if (size() == list2.size()) {
        for (int i = 0; i < size(); i++) {
          final Object value11 = get(i);
          final Object value21 = list2.get(i);
          if (!DataType.equal(value11, value21, excludeFieldNames)) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return Json.toString(this, false);
  }
}
