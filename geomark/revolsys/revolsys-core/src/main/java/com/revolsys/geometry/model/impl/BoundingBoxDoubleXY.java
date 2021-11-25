/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.geometry.model.impl;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.GeometryFactory;

public class BoundingBoxDoubleXY extends BaseBoundingBox {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public static BoundingBox newBoundingBoxDoubleXY(double minX, double minY, double maxX,
    double maxY) {
    if (minX > maxX) {
      final double t = minX;
      minX = maxX;
      maxX = t;
    }
    if (minY > maxY) {
      final double t = minY;
      minY = maxY;
      maxY = t;
    }
    return new BoundingBoxDoubleXY(minX, minY, maxX, maxY);
  }

  protected double maxX;

  protected double maxY;

  protected double minX;

  protected double minY;

  protected BoundingBoxDoubleXY() {
    this.minX = Double.POSITIVE_INFINITY;
    this.minY = Double.POSITIVE_INFINITY;
    this.maxX = Double.NEGATIVE_INFINITY;
    this.maxY = Double.NEGATIVE_INFINITY;
  }

  public BoundingBoxDoubleXY(final BoundingBox boundingBox) {
    this.minX = boundingBox.getMinX();
    this.minY = boundingBox.getMinY();
    this.maxX = boundingBox.getMaxX();
    this.maxY = boundingBox.getMaxY();
  }

  public BoundingBoxDoubleXY(final double x1, final double y1, final double x2, final double y2) {
    if (Double.isFinite(x1)) {
      if (Double.isFinite(x2)) {
        if (x1 <= x2) {
          this.minX = x1;
          this.maxX = x2;
        } else {
          this.minX = x2;
          this.maxX = x1;
        }
      } else {
        this.minX = x1;
        this.maxX = x1;
      }
    } else if (Double.isFinite(x2)) {
      this.minX = x2;
      this.maxX = x2;
    } else {
      this.minX = Double.POSITIVE_INFINITY;
      this.maxX = Double.NEGATIVE_INFINITY;
    }
    if (Double.isFinite(y1)) {
      if (Double.isFinite(y2)) {
        if (y1 <= y2) {
          this.minY = y1;
          this.maxY = y2;
        } else {
          this.minY = y2;
          this.maxY = y1;
        }
      } else {
        this.minY = y1;
        this.maxY = y1;
      }
    } else if (Double.isFinite(y2)) {
      this.minY = y2;
      this.maxY = y2;
    } else {
      this.minY = Double.POSITIVE_INFINITY;
      this.maxY = Double.NEGATIVE_INFINITY;
    }
  }

  @Override
  public boolean bboxCovers(final double x, final double y) {
    return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY;
  }

  @Override
  public boolean bboxCovers(final double minX, final double minY, final double maxX,
    final double maxY) {
    return this.minX <= minX && maxX <= this.maxX && this.minY <= minY && maxY <= this.maxY;
  }

  @Override
  public boolean bboxIntersects(final double x, final double y) {
    return !(x > this.maxX || x < this.minX || y > this.maxY || y < this.minY);
  }

  @Override
  public boolean bboxIntersects(double x1, double y1, double x2, double y2) {
    if (x1 > x2) {
      final double t = x1;
      x1 = x2;
      x2 = t;
    }
    if (y1 > y2) {
      final double t = y1;
      y1 = y2;
      y2 = t;
    }
    return !(x1 > this.maxX || x2 < this.minX || y1 > this.maxY || y2 < this.minY);
  }

  protected BoundingBoxDoubleXY clear() {
    this.minX = Double.POSITIVE_INFINITY;
    this.maxX = Double.NEGATIVE_INFINITY;
    this.maxY = Double.NEGATIVE_INFINITY;
    this.minY = Double.POSITIVE_INFINITY;
    return this;
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

  protected void expandBbox(final BoundingBoxProxy boundingBoxProxy) {
    if (boundingBoxProxy != null) {
      final BoundingBox boundingBox = boundingBoxProxy.getBoundingBox();
      if (boundingBox != null && !boundingBox.isEmpty()) {
        final double minX = boundingBox.getMinX();
        final double minY = boundingBox.getMinY();
        final double maxX = boundingBox.getMaxX();
        final double maxY = boundingBox.getMaxY();
        expandBbox(minX, minY, maxX, maxY);
      }
    }
  }

  protected void expandBbox(final double x, final double y) {
    if (x < this.minX) {
      this.minX = x;
    }
    if (y < this.minY) {
      this.minY = y;
    }
    if (x > this.maxX) {
      this.maxX = x;
    }
    if (y > this.maxY) {
      this.maxY = y;
    }
  }

  /**
   * minX must be <= maxX, minY must be <= maxY
   *
   * @param minX
   * @param minY
   * @param maxX
   * @param maxY
   */
  protected void expandBbox(final double minX, final double minY, final double maxX,
    final double maxY) {
    if (minX < this.minX) {
      this.minX = minX;
    }
    if (minY < this.minY) {
      this.minY = minY;
    }
    if (maxX > this.maxX) {
      this.maxX = maxX;
    }
    if (maxY > this.maxY) {
      this.maxY = maxY;
    }
  }

  @Override
  public double getArea() {
    final double width = this.maxX - this.minX;
    final double height = this.maxY - this.minY;
    final double area = width * height;
    if (Double.isFinite(area)) {
      return area;
    } else {
      return 0;
    }
  }

  @Override
  public int getAxisCount() {
    return 2;
  }

  @Override
  public double getCentreX() {
    return (this.minX + this.maxX) / 2;
  }

  @Override
  public double getCentreY() {
    return (this.minY + this.maxY) / 2;
  }

  @Override
  public double getMax(final int i) {
    if (i == 0) {
      return this.maxX;
    } else if (i == 1) {
      return this.maxY;
    } else {
      return Double.NEGATIVE_INFINITY;
    }
  }

  @Override
  public double getMaxX() {
    return this.maxX;
  }

  @Override
  public double getMaxY() {
    return this.maxY;
  }

  @Override
  public double getMin(final int i) {
    if (i == 0) {
      return this.minX;
    } else if (i == 1) {
      return this.minY;
    } else {
      return Double.POSITIVE_INFINITY;
    }
  }

  @Override
  public double[] getMinMaxValues() {
    if (isEmpty()) {
      return null;
    } else {
      return new double[] {
        this.minX, this.minY, this.maxX, this.maxY
      };
    }
  }

  @Override
  public double getMinX() {
    return this.minX;
  }

  @Override
  public double getMinY() {
    return this.minY;
  }

  @Override
  public boolean isEmpty() {
    if (Double.isFinite(this.minX)) {
      return false;
    } else if (Double.isFinite(this.maxX)) {
      return false;
    } else if (Double.isFinite(this.minY)) {
      return false;
    } else if (Double.isFinite(this.maxY)) {
      return false;
    } else {
      return true;
    }
  }

  protected void setBoundingBox(final BoundingBox boundingBox) {
    this.minX = boundingBox.getMinX();
    this.minY = boundingBox.getMinY();
    this.maxX = boundingBox.getMaxX();
    this.maxY = boundingBox.getMaxY();
  }

  protected void setMaxX(final double maxX) {
    this.maxX = maxX;
  }

  protected void setMaxY(final double maxY) {
    this.maxY = maxY;
  }

  protected void setMinX(final double minX) {
    this.minX = minX;
  }

  protected void setMinY(final double minY) {
    this.minY = minY;
  }

  @Override
  public RectangleXY toRectangle() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.newRectangleCorners(this.minX, this.minY, this.maxX, this.maxY);
  }

  @Override
  public String toString() {
    return BoundingBox.toString(this);
  }
}
