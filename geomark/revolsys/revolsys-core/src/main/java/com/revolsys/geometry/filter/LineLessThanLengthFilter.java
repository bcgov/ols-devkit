package com.revolsys.geometry.filter;

import java.util.function.Predicate;

import com.revolsys.geometry.model.Geometry;

public class LineLessThanLengthFilter implements Predicate<Geometry> {
  private double length;

  public LineLessThanLengthFilter() {
  }

  public LineLessThanLengthFilter(final double length) {
    this.length = length;
  }

  public double getLength() {
    return this.length;
  }

  public void setLength(final double length) {
    this.length = length;
  }

  @Override
  public boolean test(final Geometry geometry) {
    return geometry.getLength() < this.length;
  }
}
