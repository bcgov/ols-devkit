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

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.jeometry.common.number.Doubles;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.util.RectangleUtil;

/**
 *  Defines a rectangular region of the 2D coordinate plane.
 *  It is often used to represent the bounding box of a {@link Geometry},
 *  e.g. the minimum and maximum x and y values of the {@link Coordinates}s.
 *  <p>
 *  Note that Envelopes support infinite or half-infinite regions, by using the values of
 *  <code>Double.POSITIVE_INFINITY</code> and <code>Double.NEGATIVE_INFINITY</code>.
 *  <p>
 *  When BoundingBox objects are created or initialized,
 *  the supplies extent values are automatically sorted into the correct order.
 *
 *@version 1.7
 */
public class BoundingBoxDoubleGeometryFactory extends BaseBoundingBox {
  private static final long serialVersionUID = 1L;

  static {
    ConvertUtils.register(new Converter() {

      @Override
      public Object convert(@SuppressWarnings("rawtypes") final Class paramClass,
        final Object paramObject) {
        if (paramObject == null) {
          return null;
        } else if (BoundingBox.class.isAssignableFrom(paramClass)) {
          if (paramObject instanceof BoundingBox) {
            return paramObject;
          } else {
            return BoundingBox.bboxNew(paramObject.toString());
          }
        }
        return null;
      }
    }, BoundingBox.class);
  }

  private final double[] bounds;

  private final GeometryFactory geometryFactory;

  public BoundingBoxDoubleGeometryFactory(final GeometryFactory geometryFactory,
    final int axisCount, final double... bounds) {
    this.geometryFactory = geometryFactory;
    if (bounds == null || bounds.length == 0 || axisCount < 1) {
      this.bounds = null;
    } else if (bounds.length % axisCount == 0) {
      this.bounds = RectangleUtil.newBounds(axisCount);
      RectangleUtil.expand(geometryFactory, this.bounds, bounds);
    } else {
      throw new IllegalArgumentException(
        "Expecting a multiple of " + axisCount + " not " + bounds.length);
    }
  }

  /**
   * <p>Bounding boxes are immutable so clone returns this.</p>
   *
   * @return this
   */
  @Override
  public BoundingBox clone() {
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

  @Override
  public int getAxisCount() {
    if (this.bounds == null) {
      return 0;
    } else {
      return this.bounds.length / 2;
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    if (this.geometryFactory == null) {
      return GeometryFactory.DEFAULT_3D;
    }
    return this.geometryFactory;
  }

  @Override
  public double getMax(final int axisIndex) {
    if (this.bounds == null || axisIndex >= getAxisCount()) {
      return Double.NEGATIVE_INFINITY;
    } else {
      return RectangleUtil.getMax(this.bounds, axisIndex);
    }
  }

  @Override
  public double getMin(final int axisIndex) {
    if (this.bounds == null) {
      return Double.POSITIVE_INFINITY;
    } else {
      return RectangleUtil.getMin(this.bounds, axisIndex);
    }
  }

  @Override
  public double[] getMinMaxValues() {
    if (this.bounds == null) {
      return null;
    } else {
      return this.bounds.clone();
    }
  }

  @Override
  public int hashCode() {
    if (isEmpty()) {
      return 0;
    } else {
      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();
      int result = 17;
      result = 37 * result + Doubles.hashCode(minX);
      result = 37 * result + Doubles.hashCode(maxX);
      result = 37 * result + Doubles.hashCode(minY);
      result = 37 * result + Doubles.hashCode(maxY);
      return result;
    }
  }

  @Override
  public boolean isEmpty() {
    if (this.bounds != null) {
      for (final double value : this.bounds) {
        if (Double.isFinite(value)) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public String toString() {
    return BoundingBox.toString(this);
  }
}
