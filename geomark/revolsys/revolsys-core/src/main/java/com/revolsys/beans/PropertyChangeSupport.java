/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map.Entry;

import org.jeometry.common.data.type.DataType;

/**
 * This is a utility class that can be used by beans that support bound
 * properties.  It manages a list of listeners and dispatches
 * {@link PropertyChangeEvent}s to them.  You can use an instance of this class
 * as a member field of your bean and delegate these types of work to it.
 * The {@link PropertyChangeListener} can be registered for all properties
 * or for a property specified by name.
 * <p>
 * Here is an example of {@code PropertyChangeSupport} usage that follows
 * the rules and recommendations laid out in the JavaBeans&trade; specification:
 * <pre>
 * public class MyBean {
 *     private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 *
 *     public void addPropertyChangeListener(PropertyChangeListener listener) {
 *         this.pcs.addPropertyChangeListener(listener);
 *     }
 *
 *     public void removePropertyChangeListener(PropertyChangeListener listener) {
 *         this.pcs.removePropertyChangeListener(listener);
 *     }
 *
 *     private String value;
 *
 *     public String getValue() {
 *         return this.value;
 *     }
 *
 *     public void setValue(String newValue) {
 *         String oldValue = this.value;
 *         this.value = newValue;
 *         this.pcs.firePropertyChange("value", oldValue, newValue);
 *     }
 *
 *     [...]
 * }
 * </pre>
 * <p>
 * A {@code PropertyChangeSupport} instance is thread-safe.
 * <p>
 * This class is serializable.  When it is serialized it will save
 * (and restore) any listeners that are themselves serializable.  Any
 * non-serializable listeners will be skipped during serialization.
 *
 * @see VetoableChangeSupport
 */
public class PropertyChangeSupport implements Serializable {
  /**
   * This is a {@link ChangeListenerMap ChangeListenerMap} implementation
   * that works with {@link PropertyChangeListener PropertyChangeListener} objects.
   */
  private static final class PropertyChangeListenerMap
    extends ChangeListenerMap<PropertyChangeListener> {
    private static final PropertyChangeListener[] EMPTY = {};

    /**
     * {@inheritDoc}
     */
    @Override
    public final PropertyChangeListener extract(PropertyChangeListener listener) {
      while (listener instanceof PropertyChangeListenerProxy) {
        listener = ((PropertyChangeListenerProxy)listener).getListener();
      }
      return listener;
    }

    /**
     * Creates an array of {@link PropertyChangeListener PropertyChangeListener} objects.
     * This method uses the same instance of the empty array
     * when {@code length} equals {@code 0}.
     *
     * @param length  the array length
     * @return        an array with specified length
     */
    @Override
    protected PropertyChangeListener[] newArray(final int length) {
      return 0 < length ? new PropertyChangeListener[length] : EMPTY;
    }

    /**
     * Creates a {@link PropertyChangeListenerProxy PropertyChangeListenerProxy}
     * object for the specified property.
     *
     * @param name      the name of the property to listen on
     * @param listener  the listener to process events
     * @return          a {@code PropertyChangeListenerProxy} object
     */
    @Override
    protected PropertyChangeListener newProxy(final String name,
      final PropertyChangeListener listener) {
      return new PropertyChangeListenerProxy(name, listener);
    }
  }

  /**
   * @serialField children                                   Hashtable
   * @serialField source                                     Object
   * @serialField propertyChangeSupportSerializedDataVersion int
   */
  private static final ObjectStreamField[] serialPersistentFields = {
    new ObjectStreamField("children", Hashtable.class),
    new ObjectStreamField("source", Object.class),
    new ObjectStreamField("propertyChangeSupportSerializedDataVersion", Integer.TYPE)
  };

  /**
   * Serialization version ID, so we're compatible with JDK 1.1
   */
  static final long serialVersionUID = 6401253773779951803L;

  private static void fire(final PropertyChangeListener[] listeners,
    final PropertyChangeEvent event) {
    if (listeners != null) {
      for (final PropertyChangeListener listener : listeners) {
        listener.propertyChange(event);
      }
    }
  }

  private PropertyChangeListenerMap map = new PropertyChangeListenerMap();

  /**
   * The object to be provided as the "source" for any generated events.
   */
  private Object source;

  /**
   * Constructs a <code>PropertyChangeSupport</code> object.
   *
   * @param sourceBean  The bean to be given as the source for any events.
   */
  public PropertyChangeSupport(final Object sourceBean) {
    if (sourceBean == null) {
      throw new NullPointerException();
    }
    this.source = sourceBean;
  }

  /**
   * Add a PropertyChangeListener to the listener list.
   * The listener is registered for all properties.
   * The same listener object may be added more than once, and will be called
   * as many times as it is added.
   * If <code>listener</code> is null, no exception is thrown and no action
   * is taken.
   *
   * @param listener  The PropertyChangeListener to be added
   */
  public void addPropertyChangeListener(final PropertyChangeListener listener) {
    if (listener != null) {
      if (listener instanceof PropertyChangeListenerProxy) {
        final PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy)listener;
        // Call two argument add method.
        addPropertyChangeListener(proxy.getPropertyName(), proxy.getListener());
      } else {
        this.map.add(null, listener);
      }
    }
  }

  /**
   * Add a PropertyChangeListener for a specific property.  The listener
   * will be invoked only when a call on firePropertyChange names that
   * specific property.
   * The same listener object may be added more than once.  For each
   * property,  the listener will be invoked the number of times it was added
   * for that property.
   * If <code>propertyName</code> or <code>listener</code> is null, no
   * exception is thrown and no action is taken.
   *
   * @param propertyName  The name of the property to listen on.
   * @param listener  The PropertyChangeListener to be added
   */
  public void addPropertyChangeListener(final String propertyName,
    PropertyChangeListener listener) {
    if (listener != null && propertyName != null) {
      listener = this.map.extract(listener);
      if (listener != null) {
        this.map.add(propertyName, listener);
      }
    }
  }

  /**
   * Reports a boolean bound indexed property update to listeners
   * that have been registered to track updates of
   * all properties or a property with the specified name.
   * <p>
   * No event is fired if old and new values are equal.
   * <p>
   * This is merely a convenience wrapper around the more general
   * {@link #fireIndexedPropertyChange(String, int, Object, Object)} method.
   *
   * @param propertyName  the programmatic name of the property that was changed
   * @param index         the index of the property element that was changed
   * @param oldValue      the old value of the property
   * @param newValue      the new value of the property
   * @since 1.5
   */
  public void fireIndexedPropertyChange(final String propertyName, final int index,
    final boolean oldValue, final boolean newValue) {
    if (oldValue != newValue) {
      final IndexedPropertyChangeEvent event = new IndexedPropertyChangeEvent(this.source,
        propertyName, oldValue, newValue, index);
      firePropertyChangeDo(event);
    }
  }

  /**
   * Reports an integer bound indexed property update to listeners
   * that have been registered to track updates of
   * all properties or a property with the specified name.
   * <p>
   * No event is fired if old and new values are equal.
   * <p>
   * This is merely a convenience wrapper around the more general
   * {@link #fireIndexedPropertyChange(String, int, Object, Object)} method.
   *
   * @param propertyName  the programmatic name of the property that was changed
   * @param index         the index of the property element that was changed
   * @param oldValue      the old value of the property
   * @param newValue      the new value of the property
   * @since 1.5
   */
  public void fireIndexedPropertyChange(final String propertyName, final int index,
    final int oldValue, final int newValue) {
    if (oldValue != newValue) {
      final IndexedPropertyChangeEvent event = new IndexedPropertyChangeEvent(this.source,
        propertyName, oldValue, newValue, index);
      firePropertyChangeDo(event);
    }
  }

  /**
   * Reports a bound indexed property update to listeners
   * that have been registered to track updates of
   * all properties or a property with the specified name.
   * <p>
   * No event is fired if old and new values are equal and non-null.
   * <p>
   * This is merely a convenience wrapper around the more general
   * {@link #firePropertyChange(PropertyChangeEvent)} method.
   *
   * @param propertyName  the programmatic name of the property that was changed
   * @param index         the index of the property element that was changed
   * @param oldValue      the old value of the property
   * @param newValue      the new value of the property
   * @since 1.5
   */
  public void fireIndexedPropertyChange(final String propertyName, final int index,
    final Object oldValue, final Object newValue) {
    if (!DataType.equal(oldValue, newValue)) {
      final IndexedPropertyChangeEvent event = new IndexedPropertyChangeEvent(this.source,
        propertyName, oldValue, newValue, index);
      firePropertyChangeDo(event);
    }
  }

  /**
   * Fires a property change event to listeners
   * that have been registered to track updates of
   * all properties or a property with the specified name.
   * <p>
   * No event is fired if the given event's old and new values are equal and non-null.
   *
   * @param event  the {@code PropertyChangeEvent} to be fired
   */
  public void firePropertyChange(final PropertyChangeEvent event) {
    final Object oldValue = event.getOldValue();
    final Object newValue = event.getNewValue();
    if (!DataType.equal(oldValue, newValue)) {
      firePropertyChangeDo(event);
    }
  }

  /**
   * Reports a boolean bound property update to listeners
   * that have been registered to track updates of
   * all properties or a property with the specified name.
   * <p>
   * No event is fired if old and new values are equal.
   * <p>
   * This is merely a convenience wrapper around the more general
   * {@link #firePropertyChange(String, Object, Object)}  method.
   *
   * @param propertyName  the programmatic name of the property that was changed
   * @param oldValue      the old value of the property
   * @param newValue      the new value of the property
   */
  public void firePropertyChange(final String propertyName, final boolean oldValue,
    final boolean newValue) {
    if (oldValue != newValue) {
      final PropertyChangeEvent event = new PropertyChangeEvent(this.source, propertyName, oldValue,
        newValue);
      firePropertyChangeDo(event);
    }
  }

  /**
   * Reports an integer bound property update to listeners
   * that have been registered to track updates of
   * all properties or a property with the specified name.
   * <p>
   * No event is fired if old and new values are equal.
   * <p>
   * This is merely a convenience wrapper around the more general
   * {@link #firePropertyChange(String, Object, Object)}  method.
   *
   * @param propertyName  the programmatic name of the property that was changed
   * @param oldValue      the old value of the property
   * @param newValue      the new value of the property
   */
  public void firePropertyChange(final String propertyName, final int oldValue,
    final int newValue) {
    if (oldValue != newValue) {
      final PropertyChangeEvent event = new PropertyChangeEvent(this.source, propertyName, oldValue,
        newValue);
      firePropertyChangeDo(event);
    }
  }

  /**
   * Reports a bound property update to listeners
   * that have been registered to track updates of
   * all properties or a property with the specified name.
   * <p>
   * No event is fired if old and new values are equal and non-null.
   * <p>
   * This is merely a convenience wrapper around the more general
   * {@link #firePropertyChange(PropertyChangeEvent)} method.
   *
   * @param propertyName  the programmatic name of the property that was changed
   * @param oldValue      the old value of the property
   * @param newValue      the new value of the property
   */
  public void firePropertyChange(final String propertyName, final Object oldValue,
    final Object newValue) {
    if (oldValue == null) {
      if (newValue == null) {
        return;
      }
    } else if (newValue == null) {
    } else if (oldValue.equals(newValue)) {
      return;
    }
    final PropertyChangeEvent event = new PropertyChangeEvent(this.source, propertyName, oldValue,
      newValue);
    firePropertyChangeDo(event);
  }

  private void firePropertyChangeDo(final PropertyChangeEvent event) {
    final String name = event.getPropertyName();

    final PropertyChangeListener[] common = this.map.get(null);
    fire(common, event);
    if (name != null) {
      final PropertyChangeListener[] named = this.map.get(name);
      fire(named, event);
    }
  }

  /**
   * Returns an array of all the listeners that were added to the
   * PropertyChangeSupport object with addPropertyChangeListener().
   * <p>
   * If some listeners have been added with a named property, then
   * the returned array will be a mixture of PropertyChangeListeners
   * and <code>PropertyChangeListenerProxy</code>s. If the calling
   * method is interested in distinguishing the listeners then it must
   * test each element to see if it's a
   * <code>PropertyChangeListenerProxy</code>, perform the cast, and examine
   * the parameter.
   *
   * <pre>{@code
   * PropertyChangeListener[] listeners = bean.getPropertyChangeListeners();
   * for (int i = 0; i < listeners.length; i++) {
   *   if (listeners[i] instanceof PropertyChangeListenerProxy) {
   *     PropertyChangeListenerProxy proxy =
   *                    (PropertyChangeListenerProxy)listeners[i];
   *     if (proxy.getPropertyName().equals("foo")) {
   *       // proxy is a PropertyChangeListener which was associated
   *       // with the property named "foo"
   *     }
   *   }
   * }
   * }</pre>
   *
   * @see PropertyChangeListenerProxy
   * @return all of the <code>PropertyChangeListeners</code> added or an
   *         empty array if no listeners have been added
   * @since 1.4
   */
  public PropertyChangeListener[] getPropertyChangeListeners() {
    return this.map.getListeners();
  }

  /**
   * Returns an array of all the listeners which have been associated
   * with the named property.
   *
   * @param propertyName  The name of the property being listened to
   * @return all of the <code>PropertyChangeListeners</code> associated with
   *         the named property.  If no such listeners have been added,
   *         or if <code>propertyName</code> is null, an empty array is
   *         returned.
   * @since 1.4
   */
  public PropertyChangeListener[] getPropertyChangeListeners(final String propertyName) {
    return this.map.getListeners(propertyName);
  }

  /**
   * Check if there are any listeners for a specific property, including
   * those registered on all properties.  If <code>propertyName</code>
   * is null, only check for listeners registered on all properties.
   *
   * @param propertyName  the property name.
   * @return true if there are one or more listeners for the given property
   */
  public boolean hasListeners(final String propertyName) {
    return this.map.hasListeners(propertyName);
  }

  private void readObject(final ObjectInputStream s) throws ClassNotFoundException, IOException {
    this.map = new PropertyChangeListenerMap();

    final ObjectInputStream.GetField fields = s.readFields();

    @SuppressWarnings("unchecked")
    final Hashtable<String, PropertyChangeSupport> children = (Hashtable<String, PropertyChangeSupport>)fields
      .get("children", null);
    this.source = fields.get("source", null);
    fields.get("propertyChangeSupportSerializedDataVersion", 2);

    Object listenerOrNull;
    while (null != (listenerOrNull = s.readObject())) {
      this.map.add(null, (PropertyChangeListener)listenerOrNull);
    }
    if (children != null) {
      for (final Entry<String, PropertyChangeSupport> entry : children.entrySet()) {
        for (final PropertyChangeListener listener : entry.getValue()
          .getPropertyChangeListeners()) {
          this.map.add(entry.getKey(), listener);
        }
      }
    }
  }

  /**
   * Remove a PropertyChangeListener from the listener list.
   * This removes a PropertyChangeListener that was registered
   * for all properties.
   * If <code>listener</code> was added more than once to the same event
   * source, it will be notified one less time after being removed.
   * If <code>listener</code> is null, or was never added, no exception is
   * thrown and no action is taken.
   *
   * @param listener  The PropertyChangeListener to be removed
   */
  public void removePropertyChangeListener(final PropertyChangeListener listener) {
    if (listener == null) {
      return;
    }
    if (listener instanceof PropertyChangeListenerProxy) {
      final PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy)listener;
      // Call two argument remove method.
      removePropertyChangeListener(proxy.getPropertyName(), proxy.getListener());
    } else {
      this.map.remove(null, listener);
    }
  }

  /**
   * Remove a PropertyChangeListener for a specific property.
   * If <code>listener</code> was added more than once to the same event
   * source for the specified property, it will be notified one less time
   * after being removed.
   * If <code>propertyName</code> is null,  no exception is thrown and no
   * action is taken.
   * If <code>listener</code> is null, or was never added for the specified
   * property, no exception is thrown and no action is taken.
   *
   * @param propertyName  The name of the property that was listened on.
   * @param listener  The PropertyChangeListener to be removed
   */
  public void removePropertyChangeListener(final String propertyName,
    PropertyChangeListener listener) {
    if (listener == null || propertyName == null) {
      return;
    }
    listener = this.map.extract(listener);
    if (listener != null) {
      this.map.remove(propertyName, listener);
    }
  }

  /**
   * @serialData Null terminated list of <code>PropertyChangeListeners</code>.
   * <p>
   * At serialization time we skip non-serializable listeners and
   * only serialize the serializable listeners.
   */
  private void writeObject(final ObjectOutputStream s) throws IOException {
    Hashtable<String, PropertyChangeSupport> children = null;
    PropertyChangeListener[] listeners = null;
    synchronized (this.map) {
      for (final Entry<String, PropertyChangeListener[]> entry : this.map.getEntries()) {
        final String property = entry.getKey();
        if (property == null) {
          listeners = entry.getValue();
        } else {
          if (children == null) {
            children = new Hashtable<>();
          }
          final PropertyChangeSupport pcs = new PropertyChangeSupport(this.source);
          pcs.map.set(null, entry.getValue());
          children.put(property, pcs);
        }
      }
    }
    final ObjectOutputStream.PutField fields = s.putFields();
    fields.put("children", children);
    fields.put("source", this.source);
    fields.put("propertyChangeSupportSerializedDataVersion", 2);
    s.writeFields();

    if (listeners != null) {
      for (final PropertyChangeListener l : listeners) {
        if (l instanceof Serializable) {
          s.writeObject(l);
        }
      }
    }
    s.writeObject(null);
  }
}
