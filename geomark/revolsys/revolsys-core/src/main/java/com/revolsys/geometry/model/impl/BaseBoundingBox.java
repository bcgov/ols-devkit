package com.revolsys.geometry.model.impl;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

public abstract class BaseBoundingBox implements BoundingBox {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public BaseBoundingBox() {
  }

  @Override
  public BoundingBox clone() {
    try {
      return (BoundingBox)super.clone();
    } catch (final CloneNotSupportedException e) {
      return null;
    }
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof BoundingBox) {
      final BoundingBox boundingBox = (BoundingBox)other;
      return equals(boundingBox);
    } else {
      return false;
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return GeometryFactory.DEFAULT_2D;
  }

  @Override
  public int hashCode() {
    return BoundingBox.hashCode(this);
  }

  @Override
  public String toString() {
    return bboxToEWkt();
  }
}
