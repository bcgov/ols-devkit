package com.revolsys.collection.list;

import java.util.ArrayList;
import java.util.Collection;

public class NoRepeatArrayList extends ArrayList {
  /**
   *
   */
  private static final long serialVersionUID = -8395301622287658179L;

  @Override
  public void add(final int index, final Object object) {
    if (isEmpty()) {
      super.add(index, object);
    } else if (index > 0 && get(index - 1).equals(object)) {
      return;
    } else if (index < size() && get(index).equals(object)) {
      return;
    } else {
      super.add(index, object);
    }
  }

  @Override
  public boolean add(final Object object) {
    if (isEmpty()) {
      return super.add(object);
    } else if (!get(size() - 1).equals(object)) {
      return super.add(object);
    } else {
      return false;
    }
  }

  @Override
  public boolean addAll(final Collection values) {
    ensureCapacity(size() + values.size());
    boolean modified = false;
    for (final Object value : values) {
      if (add(value)) {
        modified = true;
      }
    }
    return modified;
  }

  @Override
  public boolean addAll(int index, final Collection values) {
    ensureCapacity(size() + values.size());
    for (final Object value : values) {
      add(index++, value);
    }
    return true;
  }
}
