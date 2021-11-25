package com.revolsys.properties;

import javax.annotation.PreDestroy;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;

public class BaseObjectWithProperties implements ObjectWithProperties {
  private MapEx properties = new LinkedHashMapEx();

  private boolean cancelled = false;

  public BaseObjectWithProperties() {
  }

  public void cancel() {
    this.cancelled = true;
  }

  @Override
  protected BaseObjectWithProperties clone() {
    try {
      final BaseObjectWithProperties clone = (BaseObjectWithProperties)super.clone();
      clone.properties = Maps.newLinkedHashEx(this.properties);
      return clone;
    } catch (final CloneNotSupportedException e) {
      return Exceptions.throwUncheckedException(e);
    }
  }

  @Override
  @PreDestroy
  public void close() {
    clearProperties();
  }

  @Override
  public MapEx getProperties() {
    return this.properties;
  }

  public boolean isCancelled() {
    return this.cancelled;
  }

}
