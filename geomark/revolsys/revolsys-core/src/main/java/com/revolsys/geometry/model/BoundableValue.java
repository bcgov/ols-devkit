package com.revolsys.geometry.model;

public interface BoundableValue<T> extends BoundingBoxProxy {

  T getBoundableValue();
}
