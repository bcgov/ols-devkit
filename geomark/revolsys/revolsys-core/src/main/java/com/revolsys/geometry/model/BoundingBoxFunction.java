package com.revolsys.geometry.model;

@FunctionalInterface
public interface BoundingBoxFunction<R> {
  R accept(BoundingBox boundingBox, double minX, double minY, double maxX, double maxY);

}
