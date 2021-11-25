/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package com.revolsys.beans;

import java.beans.VetoableChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This is an abstract class that provides base functionality
 * for the {@link PropertyChangeSupport PropertyChangeSupport} class
 * and the {@link VetoableChangeSupport VetoableChangeSupport} class.
 *
 * @see PropertyChangeListenerMap
 * @see VetoableChangeListenerMap
 *
 * @author Sergey A. Malenkov
 */
abstract class ChangeListenerMap<L extends EventListener> {
  private Map<String, L[]> map;

  /**
   * Adds a listener to the list of listeners for the specified property.
   * This listener is called as many times as it was added.
   *
   * @param name      the name of the property to listen on
   * @param listener  the listener to process events
   */
  public final synchronized void add(final String name, final L listener) {
    if (this.map == null) {
      this.map = new HashMap<>();
    }
    final L[] array = this.map.get(name);
    final int size = array != null ? array.length : 0;

    final L[] clone = newArray(size + 1);
    clone[size] = listener;
    if (array != null) {
      System.arraycopy(array, 0, clone, 0, size);
    }
    this.map.put(name, clone);
  }

  /**
   * Extracts a real listener from the proxy listener.
   * It is necessary because default proxy class is not serializable.
   *
   * @return a real listener
   */
  public abstract L extract(L listener);

  /**
   * Returns the list of listeners for the specified property.
   *
   * @param name  the name of the property
   * @return      the corresponding list of listeners
   */
  public final synchronized L[] get(final String name) {
    return this.map != null ? this.map.get(name) : null;
  }

  /**
   * Returns a set of entries from the map.
   * Each entry is a pair consisted of the property name
   * and the corresponding list of listeners.
   *
   * @return a set of entries from the map
   */
  public final Set<Entry<String, L[]>> getEntries() {
    return this.map != null ? this.map.entrySet() : Collections.<Entry<String, L[]>> emptySet();
  }

  /**
   * Returns all listeners in the map.
   *
   * @return an array of all listeners
   */
  public final synchronized L[] getListeners() {
    if (this.map == null) {
      return newArray(0);
    }
    final List<L> list = new ArrayList<>();

    final L[] listeners = this.map.get(null);
    if (listeners != null) {
      for (final L listener : listeners) {
        list.add(listener);
      }
    }
    for (final Entry<String, L[]> entry : this.map.entrySet()) {
      final String name = entry.getKey();
      if (name != null) {
        for (final L listener : entry.getValue()) {
          list.add(newProxy(name, listener));
        }
      }
    }
    return list.toArray(newArray(list.size()));
  }

  /**
   * Returns listeners that have been associated with the named property.
   *
   * @param name  the name of the property
   * @return an array of listeners for the named property
   */
  public final L[] getListeners(final String name) {
    if (name != null) {
      final L[] listeners = get(name);
      if (listeners != null) {
        return listeners.clone();
      }
    }
    return newArray(0);
  }

  /**
   * Indicates whether the map contains
   * at least one listener to be notified.
   *
   * @param name  the name of the property
   * @return      {@code true} if at least one listener exists or
   *              {@code false} otherwise
   */
  public final synchronized boolean hasListeners(final String name) {
    if (this.map == null) {
      return false;
    }
    final L[] array = this.map.get(null);
    return array != null || name != null && null != this.map.get(name);
  }

  /**
   * Creates an array of listeners.
   * This method can be optimized by using
   * the same instance of the empty array
   * when {@code length} is equal to {@code 0}.
   *
   * @param length  the array length
   * @return        an array with specified length
   */
  protected abstract L[] newArray(int length);

  /**
   * Creates a proxy listener for the specified property.
   *
   * @param name      the name of the property to listen on
   * @param listener  the listener to process events
   * @return          a proxy listener
   */
  protected abstract L newProxy(String name, L listener);

  /**
   * Removes a listener from the list of listeners for the specified property.
   * If the listener was added more than once to the same event source,
   * this listener will be notified one less time after being removed.
   *
   * @param name      the name of the property to listen on
   * @param listener  the listener to process events
   */
  public final synchronized void remove(final String name, final L listener) {
    if (this.map != null) {
      final L[] array = this.map.get(name);
      if (array != null) {
        for (int i = 0; i < array.length; i++) {
          if (listener.equals(array[i])) {
            final int size = array.length - 1;
            if (size > 0) {
              final L[] clone = newArray(size);
              System.arraycopy(array, 0, clone, 0, i);
              System.arraycopy(array, i + 1, clone, i, size - i);
              this.map.put(name, clone);
            } else {
              this.map.remove(name);
              if (this.map.isEmpty()) {
                this.map = null;
              }
            }
            break;
          }
        }
      }
    }
  }

  /**
   * Sets new list of listeners for the specified property.
   *
   * @param name       the name of the property
   * @param listeners  new list of listeners
   */
  public final void set(final String name, final L[] listeners) {
    if (listeners != null) {
      if (this.map == null) {
        this.map = new HashMap<>();
      }
      this.map.put(name, listeners);
    } else if (this.map != null) {
      this.map.remove(name);
      if (this.map.isEmpty()) {
        this.map = null;
      }
    }
  }
}
