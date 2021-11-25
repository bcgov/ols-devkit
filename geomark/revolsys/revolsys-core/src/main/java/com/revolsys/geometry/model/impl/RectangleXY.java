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

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.jeometry.common.function.BiConsumerDouble;
import org.jeometry.common.function.BiFunctionDouble;
import org.jeometry.common.function.Consumer3Double;
import org.jeometry.common.function.Consumer4Double;
import org.jeometry.common.function.Function4Double;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.ClockDirection;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.util.OutCode;

public class RectangleXY extends AbstractPolygon implements BoundingBox {

  private static final long serialVersionUID = 1L;

  public static void notNullSameCs(final GeometryFactoryProxy geometryFactory,
    final BoundingBox rectangle) {
    if (rectangle == null) {
      throw new NullPointerException("Argument rectangle cannot be null");
    } else if (!geometryFactory.isSameCoordinateSystem(rectangle)) {
      throw new IllegalArgumentException(
        "Rectangle operations require the same coordinate system this != rectangle\n  "
          + geometryFactory.getHorizontalCoordinateSystemName() + "\n  "
          + rectangle.getHorizontalCoordinateSystemName());
    }
  }

  private final GeometryFactory geometryFactory;

  protected double maxX;

  protected double maxY;

  protected double minX;

  protected double minY;

  private final LinearRing ring = new LinearRing() {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public LinearRing clone() {
      return this;
    }

    @Override
    public boolean equals(final Object other) {
      if (other instanceof Geometry) {
        final Geometry geometry = (Geometry)other;
        return equals(2, geometry);
      } else {
        return false;
      }
    }

    @Override
    public <R> R findSegment(final Function4Double<R> action) {
      final double minX = RectangleXY.this.minX;
      final double minY = RectangleXY.this.minY;
      final double maxX = RectangleXY.this.maxX;
      final double maxY = RectangleXY.this.maxY;
      R result = action.accept(minX, minY, maxX, minY);
      if (result != null) {
        return result;
      }
      result = action.accept(maxX, minY, maxX, maxY);
      if (result != null) {
        return result;
      }
      result = action.accept(maxX, maxY, minX, maxY);
      if (result != null) {
        return result;
      }
      result = action.accept(minX, maxY, minX, minY);
      return result;
    }

    @Override
    public <R> R findVertex(final BiFunctionDouble<R> action) {
      final double minX = RectangleXY.this.minX;
      final double minY = RectangleXY.this.minY;
      final double maxX = RectangleXY.this.maxX;
      final double maxY = RectangleXY.this.maxY;
      R result = action.accept(minX, minY);
      if (result != null) {
        return result;
      }
      result = action.accept(maxX, minY);
      if (result != null) {
        return result;
      }
      result = action.accept(maxX, maxY);
      if (result != null) {
        return result;
      }
      result = action.accept(minX, maxY);
      if (result != null) {
        return result;
      }
      result = action.accept(minX, minY);
      return result;
    }

    @Override
    public void forEachSegment(final Consumer4Double action) {
      final double minX = RectangleXY.this.minX;
      final double minY = RectangleXY.this.minY;
      final double maxX = RectangleXY.this.maxX;
      final double maxY = RectangleXY.this.maxY;
      action.accept(minX, minY, maxX, minY);
      action.accept(maxX, minY, maxX, maxY);
      action.accept(maxX, maxY, minX, maxY);
      action.accept(minX, maxY, minX, minY);
    }

    @Override
    public void forEachVertex(final BiConsumerDouble action) {
      final double minX = RectangleXY.this.minX;
      final double minY = RectangleXY.this.minY;
      final double maxX = RectangleXY.this.maxX;
      final double maxY = RectangleXY.this.maxY;
      action.accept(minX, minY);
      action.accept(maxX, minY);
      action.accept(maxX, maxY);
      action.accept(minX, maxY);
      action.accept(minX, minY);
    }

    @Override
    public void forEachVertex(final Consumer3Double action) {
      final double minX = RectangleXY.this.minX;
      final double minY = RectangleXY.this.minY;
      final double maxX = RectangleXY.this.maxX;
      final double maxY = RectangleXY.this.maxY;
      action.accept(minX, minY, Double.NaN);
      action.accept(maxX, minY, Double.NaN);
      action.accept(maxX, maxY, Double.NaN);
      action.accept(minX, maxY, Double.NaN);
      action.accept(minX, minY, Double.NaN);
    }

    @Override
    public int getAxisCount() {
      return 2;
    }

    @Override
    public BoundingBox getBoundingBox() {
      return RectangleXY.this.getBoundingBox();
    }

    @Override
    public ClockDirection getClockDirection() {
      return ClockDirection.COUNTER_CLOCKWISE;
    }

    @Override
    public double getCoordinate(final int vertexIndex, final int axisIndex) {
      if (axisIndex == 0) {
        return getX(vertexIndex);
      } else if (axisIndex == 1) {
        return getY(vertexIndex);
      } else {
        return Double.NaN;
      }
    }

    @Override
    public double[] getCoordinates() {
      final double minX = RectangleXY.this.minX;
      final double minY = RectangleXY.this.minY;
      final double maxX = RectangleXY.this.maxX;
      final double maxY = RectangleXY.this.maxY;
      return new double[] {
        minX, minY, //
        maxX, minY, //
        maxX, maxY, //
        minX, maxY, //
        minX, minY //
      };
    }

    @Override
    public GeometryFactory getGeometryFactory() {
      return RectangleXY.this.geometryFactory;
    }

    @Override
    public double getLength() {
      return 2 * (getWidth() + getHeight());
    }

    @Override
    public int getVertexCount() {
      return 5;
    }

    @Override
    public double getX(final int vertexIndex) {
      switch (vertexIndex) {
        case 0:
        case 3:
        case 4:
          return RectangleXY.this.minX;
        case 1:
        case 2:
          return RectangleXY.this.maxX;
        default:
          return Double.NaN;
      }
    }

    @Override
    public double getY(final int vertexIndex) {
      switch (vertexIndex) {
        case 0:
        case 1:
        case 4:
          return RectangleXY.this.minY;
        case 2:
        case 3:
          return RectangleXY.this.maxY;
        default:
          return Double.NaN;
      }
    }

    /**
     * Gets a hash code for the Geometry.
     *
     * @return an integer value suitable for use as a hashcode
     */

    @Override
    public int hashCode() {
      return getBoundingBox().hashCode();
    }

    @Override
    public String toString() {
      return toEwkt();
    }
  };

  public RectangleXY(final GeometryFactory geometryFactory, final double x, final double y,
    final double width, final double height) {
    this.geometryFactory = geometryFactory;
    if (width < 0) {
      throw new IllegalArgumentException("width " + width + " must be > 0");
    }
    if (height < 0) {
      throw new IllegalArgumentException("height " + height + " must be > 0");
    }
    this.minX = x;
    this.minY = y;
    this.maxX = x + width;
    this.maxY = y + height;
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

  /**
   * Creates and returns a full copy of this {@link Polygon} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public RectangleXY clone() {
    return (RectangleXY)super.clone();
  }

  @Override
  public boolean contains(final double x, final double y) {
    return !(x <= this.minX || this.maxX <= x || y <= this.minY || this.maxY <= y);
  }

  @Override
  public <R> R findSegment(final Function4Double<R> action) {
    R result = action.accept(this.minX, this.minY, this.maxX, this.minY);
    if (result != null) {
      return result;
    }
    result = action.accept(this.maxX, this.minY, this.maxX, this.maxY);
    if (result != null) {
      return result;
    }
    result = action.accept(this.maxX, this.maxY, this.minX, this.maxY);
    if (result != null) {
      return result;
    }
    result = action.accept(this.minX, this.maxY, this.minX, this.minY);
    return result;
  }

  @Override
  public <R> R findVertex(final BiFunctionDouble<R> action) {
    R result = action.accept(this.minX, this.minY);
    if (result != null) {
      return result;
    }
    result = action.accept(this.maxX, this.minY);
    if (result != null) {
      return result;
    }
    result = action.accept(this.maxX, this.maxY);
    if (result != null) {
      return result;
    }
    result = action.accept(this.minX, this.maxY);
    if (result != null) {
      return result;
    }
    result = action.accept(this.minX, this.minY);
    return result;
  }

  @Override
  public void forEachGeometry(final Consumer<Geometry> action) {
    final LinearRing ring = getRing(0);
    ring.forEachGeometry(action);
  }

  @Override
  public void forEachSegment(final Consumer4Double action) {
    action.accept(this.minX, this.minY, this.maxX, this.minY);
    action.accept(this.maxX, this.minY, this.maxX, this.maxY);
    action.accept(this.maxX, this.maxY, this.minX, this.maxY);
    action.accept(this.minX, this.maxY, this.minX, this.minY);
  }

  @Override
  public void forEachVertex(final BiConsumerDouble action) {
    action.accept(this.minX, this.minY);
    action.accept(this.maxX, this.minY);
    action.accept(this.maxX, this.maxY);
    action.accept(this.minX, this.maxY);
    action.accept(this.minX, this.minY);
  }

  @Override
  public void forEachVertex(final Consumer3Double action) {
    action.accept(this.minX, this.minY, Double.NaN);
    action.accept(this.maxX, this.minY, Double.NaN);
    action.accept(this.maxX, this.maxY, Double.NaN);
    action.accept(this.minX, this.maxY, Double.NaN);
    action.accept(this.minX, this.minY, Double.NaN);
  }

  @Override
  public void forEachVertex(final CoordinatesOperation coordinatesOperation,
    final CoordinatesOperationPoint point, final Consumer<CoordinatesOperationPoint> action) {
    this.ring.forEachVertex(coordinatesOperation, point, action);
  }

  @Override
  public void forEachVertex(final CoordinatesOperationPoint coordinates,
    final Consumer<CoordinatesOperationPoint> action) {
    this.ring.forEachVertex(coordinates, action);
  }

  @Override
  public double getArea() {
    return getWidth() * getHeight();
  }

  @Override
  public int getAxisCount() {
    return this.geometryFactory.getAxisCount();
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this;
  }

  @Override
  public double getCoordinate(final int partIndex, final int ringIndex, final int vertexIndex,
    final int axisIndex) {
    if (partIndex == 0) {
      return getCoordinate(ringIndex, vertexIndex, axisIndex);
    } else {
      return Double.NaN;
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public double getHeight() {
    return this.maxY - this.minY;
  }

  @Override
  public LinearRing getHole(final int ringIndex) {
    return null;
  }

  @Override
  public int getHoleCount() {
    return 0;
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
  public double getMinX() {
    return this.minX;
  }

  @Override
  public double getMinY() {
    return this.minY;
  }

  @Override
  public LinearRing getRing(final int ringIndex) {
    if (ringIndex == 0) {
      return this.ring;
    } else {
      return null;
    }
  }

  @Override
  public int getRingCount() {
    return 1;
  }

  @Override
  public List<LinearRing> getRings() {
    return Lists.newArray(getRing(0));
  }

  @Override
  public LinearRing getShell() {
    return this.ring;
  }

  @Override
  public double getWidth() {
    return this.maxX - this.minX;
  }

  @Override
  public int hashCode() {
    return BoundingBox.hashCode(this);
  }

  @Override
  public Iterable<LinearRing> holes() {
    return Collections.emptyList();
  }

  @Override
  public Geometry intersection(final Geometry geometry) {
    final Geometry convertedGeometry = geometry.convertGeometry(this.geometryFactory);
    return convertedGeometry.intersectionBbox(this);
  }

  @Override
  public Geometry intersectionBbox(final BoundingBox boundingBox) {
    notNullSameCs(boundingBox);
    if (bboxCoveredBy(boundingBox)) {
      return this;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final double minX = boundingBox.getMinX();
      final double minY = boundingBox.getMinY();
      final double maxX = boundingBox.getMaxX();
      final double maxY = boundingBox.getMaxY();
      if (bboxIntersects(minX, minY, maxX, maxY)) {
        final double intMinX = Math.max(this.minX, minX);
        final double intMinY = Math.max(this.minY, minY);
        final double intMaxX = Math.min(this.maxX, maxX);
        final double intMaxY = Math.min(this.maxY, maxY);
        return geometryFactory.newRectangleCorners(intMinX, intMinY, intMaxX, intMaxY);
      } else {
        return geometryFactory.polygon();
      }
    }
  }

  @Override
  public boolean intersects(final double x, final double y) {
    if (x < this.minX || this.maxX < x || y < this.minY || this.maxY < y) {
      return false;
    } else {
      return true;
    }
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean isRectangle() {
    return true;
  }

  @Override
  public boolean isWithinDistance(Geometry geometry, final double distance) {
    geometry = geometry.as2d(this);
    final BoundingBox boundingBox2 = geometry.getBoundingBox();
    final double bboxDistance = boundingBox2.bboxDistance(this.minX, this.minY, this.maxX,
      this.maxY);
    if (bboxDistance > distance) {
      return false;
    } else {
      final double geometryDistance = this.distanceGeometry(geometry);
      return geometryDistance <= distance;

    }
  }

  @Override
  public Location locate(final double x, final double y) {
    if (x < this.minX || this.maxX < x || y < this.minY || this.maxY < y) {
      return Location.EXTERIOR;
    } else if (x == this.minX || x == this.maxX || y == this.minY || y == this.maxY) {
      return Location.BOUNDARY;
    } else {
      return Location.INTERIOR;
    }
  }

  @Override
  public BoundingBox newBoundingBox() {
    throw new UnsupportedOperationException();
  }

  @Override
  public OutCode outcode(final double x, final double y) {
    if (x < this.minX) {
      if (y < this.minY) {
        return OutCode.LEFT_BOTTOM;
      } else if (y > this.maxY) {
        return OutCode.LEFT_TOP;
      } else {
        return OutCode.LEFT;
      }
    } else if (x > this.maxX) {
      if (y < this.minY) {
        return OutCode.RIGHT_BOTTOM;
      } else if (y > this.maxY) {
        return OutCode.RIGHT_TOP;
      } else {
        return OutCode.RIGHT;
      }

    } else {
      if (y < this.minY) {
        return OutCode.BOTTOM;
      } else if (y > this.maxY) {
        return OutCode.TOP;
      } else {
        return OutCode.INSIDE;
      }

    }
  }

  @Override
  public RectangleXY toRectangle() {
    return this;
  }
}
