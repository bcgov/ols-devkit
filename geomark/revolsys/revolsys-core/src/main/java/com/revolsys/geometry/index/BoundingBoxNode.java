package com.revolsys.geometry.index;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.util.RectangleUtil;
import com.revolsys.util.Property;

public class BoundingBoxNode {
  private double maxX;

  private double maxY;

  private double minX;

  private double minY;

  public BoundingBoxNode() {
    this.minX = Double.NaN;
    this.minY = Double.NaN;
    this.maxX = Double.NaN;
    this.maxY = Double.NaN;
  }

  public BoundingBoxNode(final double... bounds) {
    this.minX = bounds[0];
    this.minY = bounds[1];
    this.maxX = bounds[2];
    this.maxY = bounds[3];
  }

  public BoundingBoxNode(final double minX, final double minY, final double maxX,
    final double maxY) {
    this.minX = minX;
    this.minY = minY;
    this.maxX = maxX;
    this.maxY = maxY;
  }

  public boolean covers(final BoundingBox boundingBox) {
    return RectangleUtil.covers(this.minX, this.minY, this.maxX, this.maxY, boundingBox.getMinX(),
      boundingBox.getMinY(), boundingBox.getMaxX(), boundingBox.getMaxY());
  }

  public boolean covers(final double minX, final double minY, final double maxX,
    final double maxY) {
    return this.minX <= minX && maxX <= this.maxX && this.minY <= minY && maxY <= this.maxY;
  }

  protected void expandBoundingBox(final BoundingBox boundingBox) {
    final double minX = boundingBox.getMinX();
    final double minY = boundingBox.getMinY();
    final double maxX = boundingBox.getMaxX();
    final double maxY = boundingBox.getMaxY();
    expandBoundingBox(minX, minY, maxX, maxY);
  }

  protected void expandBoundingBox(final BoundingBoxNode node) {
    final double minX = node.getMinX();
    final double minY = node.getMinY();
    final double maxX = node.getMaxX();
    final double maxY = node.getMaxY();
    expandBoundingBox(minX, minY, maxX, maxY);
  }

  protected void expandBoundingBox(final double... bounds) {
    final double minX = bounds[0];
    final double minY = bounds[1];
    final double maxX = bounds[2];
    final double maxY = bounds[3];
    expandBoundingBox(minX, minY, maxX, maxY);
  }

  protected void expandBoundingBox(final double minX, final double minY, final double maxX,
    final double maxY) {
    if (minX < this.minX || Double.isNaN(this.minX)) {
      this.minX = minX;
    }
    if (minY < this.minY || Double.isNaN(this.minY)) {
      this.minY = minY;
    }

    if (maxX > this.maxX || Double.isNaN(this.maxX)) {
      this.maxX = maxX;
    }
    if (maxY > this.maxY || Double.isNaN(this.maxY)) {
      this.maxY = maxY;
    }
  }

  public double getArea() {
    return (this.maxX - this.minX) * (this.maxY - this.minY);
  }

  public double getCentreX() {
    return (this.minX + this.maxX) / 2;
  }

  public double getCentreY() {
    return (this.minY + this.maxY) / 2;
  }

  public double getMaxX() {
    return this.maxX;
  }

  public double getMaxY() {
    return this.maxY;
  }

  public double getMinX() {
    return this.minX;
  }

  public double getMinY() {
    return this.minY;
  }

  public boolean intersectsBoundingBox(final BoundingBox boundingBox) {
    if (Property.isEmpty(boundingBox)) {
      return false;
    } else {
      final double minX = boundingBox.getMinX();
      final double minY = boundingBox.getMinY();
      final double maxX = boundingBox.getMaxX();
      final double maxY = boundingBox.getMaxY();
      return intersectsBoundingBox(minX, minY, maxX, maxY);
    }
  }

  public boolean intersectsBoundingBox(final double... bounds) {
    final double minX = bounds[0];
    final double minY = bounds[1];
    final double maxX = bounds[2];
    final double maxY = bounds[3];
    return intersectsBoundingBox(minX, minY, maxX, maxY);
  }

  public boolean intersectsBoundingBox(final double x, final double y) {
    if (Double.isNaN(this.minX) || Double.isNaN(this.minY)) {
      return false;
    } else {
      return !(x > this.maxX || x < this.minX || y > this.maxY || y < this.minY);
    }
  }

  public boolean intersectsBoundingBox(final double minX, final double minY, final double maxX,
    final double maxY) {
    if (Double.isNaN(minX) || Double.isNaN(minY)) {
      return false;
    } else {
      return !(minX > this.maxX || maxX < this.minX || minY > this.maxY || maxY < this.minY);
    }
  }

  protected void setBoundingBox(final double minX, final double minY, final double maxX,
    final double maxY) {
    this.minX = minX;
    this.minY = minY;
    this.maxX = maxX;
    this.maxY = maxY;
  }

  @Override
  public String toString() {
    return BoundingBox.bboxToWkt(this.minX, this.minY, this.maxX, this.maxY);
  }
}
