package com.revolsys.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.revolsys.util.Property;

public interface PropertyChangeSupportProxy {
  default void addPropertyChangeListener(final PropertyChangeListener listener) {
    final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  default void addPropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener) {
    final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
    propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  default void firePropertyChange(final Object source, final String propertyName,
    final boolean oldValue, final boolean newValue) {
    final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
    if (propertyChangeSupport != null) {
      propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
  }

  default void firePropertyChange(final Object source, final String propertyName,
    final int oldValue, final int newValue) {
    final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
    if (propertyChangeSupport != null) {
      propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
  }

  default void firePropertyChange(final Object source, final String propertyName,
    final Object oldValue, final Object newValue) {
    final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
    if (propertyChangeSupport != null) {
      propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
  }

  default void firePropertyChange(final PropertyChangeEvent event) {
    final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
    if (propertyChangeSupport != null) {
      propertyChangeSupport.firePropertyChange(event);
    }
  }

  default void firePropertyChange(final String propertyName, final int index,
    final boolean oldValue, final boolean newValue) {
    final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
    if (propertyChangeSupport != null) {
      propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }
  }

  default void firePropertyChange(final String propertyName, final int index, final Object oldValue,
    final Object newValue) {
    final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
    if (propertyChangeSupport != null) {
      propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }
  }

  default void firePropertyChange(final String propertyName, final Object oldValue,
    final Object newValue) {
    final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
    if (propertyChangeSupport != null) {
      propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
  }

  PropertyChangeSupport getPropertyChangeSupport();

  default void removePropertyChangeListener(final PropertyChangeListener listener) {
    Property.removeListener(this, listener);
  }

  default void removePropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener) {
    Property.removeListener(this, propertyName, listener);
  }
}
