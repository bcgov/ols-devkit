package com.revolsys.beans;

import java.beans.PropertyChangeEvent;

/**
 * <p>
 * An KeyedPropertyChangeEvent gets delivered whenever a component that conforms
 * to the JavaBeans&trade; specification (a "bean") changes a bound keyed property.
 * This class is an extension of <code>PropertyChangeEvent</code> but contains
 * the key of the property that has changed.
 * </p>
 * <p>
 * Null values may be provided for the old and the new values if their true
 * values are not known.
 * </p>
 * <p>
 * An event source may send a null object as the name to indicate that an
 * arbitrary set of if its properties have changed. In this case the old and new
 * values should also be null.
 * </p>
 */
public class KeyedPropertyChangeEvent extends PropertyChangeEvent {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /** The key. */
  private final Object key;

  /**
   * Construct a new KeyedPropertyChangeEvent.
   *
   * @param source The source object.
   * @param propertyName The property name.
   * @param oldValue The old value.
   * @param newValue The new value.
   * @param key The key.
   */
  public KeyedPropertyChangeEvent(final Object source, final String propertyName,
    final Object oldValue, final Object newValue, final Object key) {
    super(source, propertyName, oldValue, newValue);
    this.key = key;
  }

  /**
   * Get the key.
   *
   * @return The key.
   */
  public Object getKey() {
    return this.key;
  }
}
