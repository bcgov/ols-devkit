package com.revolsys.properties;

import javax.annotation.PreDestroy;

import com.revolsys.collection.map.MapEx;

public class DelegatingObjectWithProperties extends BaseObjectWithProperties {

  private ObjectWithProperties object;

  public DelegatingObjectWithProperties() {
  }

  public DelegatingObjectWithProperties(final Object object) {
    if (object instanceof ObjectWithProperties) {
      this.object = (ObjectWithProperties)object;
    }
  }

  @Override
  public void clearProperties() {
    final ObjectWithProperties object = getObject();
    if (object == null) {
      super.clearProperties();
    } else {
      object.clearProperties();
    }
  }

  @Override
  @PreDestroy
  public void close() {
    super.close();
    this.object = null;
  }

  @SuppressWarnings("unchecked")
  public <V extends ObjectWithProperties> V getObject() {
    return (V)this.object;
  }

  @Override
  public final MapEx getProperties() {
    final ObjectWithProperties object = getObject();
    if (object == null) {
      return super.getProperties();
    } else {
      return object.getProperties();
    }
  }

  public void setObject(final ObjectWithProperties object) {
    this.object = object;
  }

  @Override
  public String toString() {
    if (this.object == null) {
      return super.toString();
    } else {
      return this.object.toString();
    }
  }
}
