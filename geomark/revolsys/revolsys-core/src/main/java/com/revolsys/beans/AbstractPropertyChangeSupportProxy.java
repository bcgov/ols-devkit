package com.revolsys.beans;

import org.jeometry.common.exception.Exceptions;

public abstract class AbstractPropertyChangeSupportProxy
  implements PropertyChangeSupportProxy, Cloneable {
  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  @Override
  protected AbstractPropertyChangeSupportProxy clone() {
    try {
      final AbstractPropertyChangeSupportProxy clone = (AbstractPropertyChangeSupportProxy)super.clone();
      clone.propertyChangeSupport = new PropertyChangeSupport(clone);
      return clone;
    } catch (final CloneNotSupportedException e) {
      return Exceptions.throwUncheckedException(e);
    }
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return this.propertyChangeSupport;
  }
}
