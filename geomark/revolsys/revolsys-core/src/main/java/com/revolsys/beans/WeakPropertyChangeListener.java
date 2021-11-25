package com.revolsys.beans;

import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public class WeakPropertyChangeListener extends BaseProxyPropertyChangeListener {
  private final Reference<PropertyChangeListener> listenerReference;

  public WeakPropertyChangeListener(final PropertyChangeListener listener) {
    this.listenerReference = new WeakReference<>(listener);
  }

  @Override
  public PropertyChangeListener getPropertyChangeListener() {
    return this.listenerReference.get();
  }
}
