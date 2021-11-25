package com.revolsys.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public interface ProxyPropertyChangeListener extends PropertyChangeListener {
  PropertyChangeListener getPropertyChangeListener();

  @Override
  default void propertyChange(final PropertyChangeEvent event) {
    final PropertyChangeListener listener = getPropertyChangeListener();
    if (listener != null) {
      listener.propertyChange(event);
    }
  }
}
