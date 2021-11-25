package com.revolsys.geometry.model;

@FunctionalInterface
public interface BoundingBoxPointFunction<R> {
  R accept(BoundingBox boundingBox, double x, double y);

}
