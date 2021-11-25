package com.revolsys.beans;

import java.beans.PropertyChangeListener;

public abstract class BaseProxyPropertyChangeListener implements ProxyPropertyChangeListener {
  @Override
  public boolean equals(final Object other) {
    if (other instanceof PropertyChangeListener) {
      final PropertyChangeListener otherListener = (PropertyChangeListener)other;
      for (PropertyChangeListener listener = otherListener; listener != null;) {
        if (listener == this) {
          return true;
        } else if (listener == getPropertyChangeListener()) {
          return true;
        } else {
          if (listener instanceof ProxyPropertyChangeListener) {
            final ProxyPropertyChangeListener proxyListener = (ProxyPropertyChangeListener)listener;
            listener = proxyListener.getPropertyChangeListener();
          } else {
            listener = null;
          }
        }
      }
      final PropertyChangeListener proxiedListener = getPropertyChangeListener();
      if (proxiedListener instanceof ProxyPropertyChangeListener) {
        final ProxyPropertyChangeListener proxyListener = (ProxyPropertyChangeListener)proxiedListener;
        return proxyListener.equals(otherListener);
      }
    }
    return false;
  }

  @Override
  public String toString() {
    final PropertyChangeListener listener = getPropertyChangeListener();
    if (listener == null) {
      return null;
    } else {
      return listener.toString();
    }
  }
}
