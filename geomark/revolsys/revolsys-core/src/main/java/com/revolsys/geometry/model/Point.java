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
package com.revolsys.geometry.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.function.BiConsumerDouble;
import org.jeometry.common.function.BiFunctionDouble;
import org.jeometry.common.function.Consumer3Double;
import org.jeometry.common.function.Consumer4Double;
import org.jeometry.common.math.Angle;
import org.jeometry.common.number.Doubles;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

import com.revolsys.geometry.model.editor.AbstractGeometryCollectionEditor;
import com.revolsys.geometry.model.editor.AbstractGeometryEditor;
import com.revolsys.geometry.model.editor.PointEditor;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.PointVertex;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.util.NumberUtil;
import com.revolsys.geometry.util.Points;
import com.revolsys.util.Pair;
import com.revolsys.util.Property;

/**
 * Represents a single point.
 *
 * A <code>Point</code> is topologically valid if and only if:
 * <ul>
 * <li>the coordinate which defines it (if any) is a valid coordinate
 * (i.e does not have an <code>NaN</code> X or Y ordinate)
 * </ul>
 *
 *@version 1.7
 */
public interface Point extends Punctual, Serializable, BoundingBox {
  /**
   * Returns the un-oriented smallest angle between two vectors.
   * The computed angle will be in the range 0->Pi.
   *
   * @param p1 the tip of one vector
   * @param p2 the tail of each vector
   * @param p3 the tip of the other vector
   * @return the angle between tail-tip1 and tail-tip2
   */
  static double angleBetween(final Point p1, final Point p2, final Point p3) {
    final double x1 = p1.getX();
    final double y1 = p1.getY();
    final double x2 = p2.getX();
    final double y2 = p2.getY();
    final double x3 = p3.getX();
    final double y3 = p3.getY();
    return Angle.angleBetween(x1, y1, x2, y2, x3, y3);
  }

  /**
   * Returns the oriented smallest angle between two vectors.
   * The computed angle will be in the range (-Pi, Pi].
   * A positive result corresponds to a counterclockwise
   * (CCW) rotation
   * from v1 to v2;
   * a negative result corresponds to a clockwise (CW) rotation;
   * a zero result corresponds to no rotation.
   *
   * @param tip1 the tip of v1
   * @param tail the tail of each vector
   * @param tip2 the tip of v2
   * @return the angle between v1 and v2, relative to v1
   */
  static double angleBetweenOriented(final Point tip1, final Point tail, final Point tip2) {
    final double x1 = tip1.getX();
    final double y1 = tip1.getY();
    final double x = tail.getX();
    final double y = tail.getY();
    final double x2 = tip2.getX();
    final double y2 = tip2.getY();
    return Angle.angleBetweenOriented(x1, y1, x, y, x2, y2);
  }

  static Point empty() {
    return GeometryFactory.DEFAULT_2D.point();
  }

  static int hashCode(final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(x);
    result = prime * result + (int)(temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(y);
    result = prime * result + (int)(temp ^ temp >>> 32);
    return result;
  }

  /**
   * Computes the interior angle between two segments of a ring. The ring is
   * assumed to be oriented in a clockwise direction. The computed angle will be
   * in the range [0, 2Pi]
   *
   * @param p0
   *          a point of the ring
   * @param p1
   *          the next point of the ring
   * @param p2
   *          the next point of the ring
   * @return the interior angle based at <code>p1</code>
   */
  static double interiorAngle(final Point p0, final Point p1, final Point p2) {
    final double anglePrev = p1.angle2d(p0);
    final double angleNext = p1.angle2d(p2);
    return Math.abs(angleNext - anglePrev);
  }

  /**
   * Tests whether the angle between p0-p1-p2 is acute.
   * An angle is acute if it is less than 90 degrees.
   * <p>
   * Note: this implementation is not precise (determistic) for angles very close to 90 degrees.
   *
   * @param p0 an endpoint of the angle
   * @param p1 the base of the angle
   * @param p2 the other endpoint of the angle
   */
  static boolean isAcute(final Point p0, final Point p1, final Point p2) {
    // relies on fact that A dot B is positive iff A ang B is acute
    final double dx0 = p0.getX() - p1.getX();
    final double dy0 = p0.getY() - p1.getY();
    final double dx1 = p2.getX() - p1.getX();
    final double dy1 = p2.getY() - p1.getY();
    final double dotprod = dx0 * dx1 + dy0 * dy1;
    return dotprod > 0;
  }

  /**
   * Tests whether the angle between p0-p1-p2 is obtuse.
   * An angle is obtuse if it is greater than 90 degrees.
   * <p>
   * Note: this implementation is not precise (determistic) for angles very close to 90 degrees.
   *
   * @param p0 an endpoint of the angle
   * @param p1 the base of the angle
   * @param p2 the other endpoint of the angle
   */
  static boolean isObtuse(final Point p0, final Point p1, final Point p2) {
    // relies on fact that A dot B is negative iff A ang B is obtuse
    final double dx0 = p0.getX() - p1.getX();
    final double dy0 = p0.getY() - p1.getY();
    final double dx1 = p2.getX() - p1.getX();
    final double dy1 = p2.getY() - p1.getY();
    final double dotprod = dx0 * dx1 + dy0 * dy1;
    return dotprod < 0;
  }

  @SuppressWarnings("unchecked")
  static <G extends Geometry> G newPoint(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Point) {
      return (G)value;
    } else if (value instanceof Geometry) {
      throw new IllegalArgumentException(
        ((Geometry)value).getGeometryType() + " cannot be converted to a Point");
    } else {
      final String string = DataTypes.toString(value);
      return (G)GeometryFactory.DEFAULT_3D.geometry(string, false);
    }
  }

  default Point add(final double deltaX, final double deltaY) {
    final double x1 = getX();
    final double y1 = getY();
    return newPoint(x1 + deltaX, y1 + deltaY);
  }

  default Point add(final Point point) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final Point convertedPoint = point.convertPoint2d(geometryFactory);
    final double x2 = convertedPoint.getX();
    final double y2 = convertedPoint.getY();

    return add(x2, y2);
  }

  /**
   * Returns the angle that the vector from (0,0) to p,
   * relative to the positive X-axis.
   * The angle is normalized to be in the range ( -Pi, Pi ].
   *
   * @return the normalized angle (in radians) that p makes with the positive x-axis.
   */
  default double angle() {
    final double x = getX();
    final double y = getY();
    return Angle.angle(x, y);
  }

  /**
   * Calculate the counter clockwise angle in radians of the vector from this
   * point to another point. The angle is relative to the positive x-axis
   * relative to the positive X-axis. The angle will be in the range -PI -> PI
   * where negative values have a clockwise orientation.
   *
   * @return The angle in radians.
   */
  default double angle2d(final Point other) {
    final double x1 = this.getX();
    final double y1 = this.getY();
    final double x2 = other.getX();
    final double y2 = other.getY();
    return Angle.angle2d(x1, y1, x2, y2);
  }

  @Override
  Point clone();

  default int compareTo(final double x, final double y) {
    final double x1 = getX();
    if (x1 < x) {
      return -1;
    } else if (x1 > x) {
      return 1;
    } else {
      final double y1 = getY();
      if (y1 < y) {
        return -1;
      } else if (y1 > y) {
        return 1;
      } else {
        return 0;
      }
    }
  }

  @Override
  default int compareTo(final Object other) {
    if (other instanceof Point) {
      final Point point = (Point)other;
      return compareTo(point);
    } else if (other instanceof Geometry) {
      final Geometry geometry = (Geometry)other;
      return compareTo(geometry);
    } else {
      return -1;
    }
  }

  /**
   *  Compares this {@link Coordinates} with the specified {@link Coordinates} for order.
   *  This method ignores the z value when making the comparison.
   *  Returns:
   *  <UL>
   *    <LI> -1 : this.x < other.x || ((this.x == other.x) && (this.y <
   *    other.y))
   *    <LI> 0 : this.x == other.x && this.y = other.y
   *    <LI> 1 : this.x > other.x || ((this.x == other.x) && (this.y > other.y))
   *
   *  </UL>
   *  Note: This method assumes that ordinate values
   * are valid numbers.  NaN values are not handled correctly.
   *
   *@param  o  the <code>Coordinate</code> with which this <code>Coordinate</code>
   *      is being compared
   *@return    -1, zero, or 1 as this <code>Coordinate</code>
   *      is less than, equal to, or greater than the specified <code>Coordinate</code>
   */
  default int compareTo(final Point point) {
    final boolean otherEmpty = point.isEmpty();
    if (isEmpty()) {
      if (otherEmpty) {
        return 0;
      } else {
        return -1;
      }
    } else if (otherEmpty) {
      return 1;
    } else {
      final Point convertedPoint;
      if (isSameCoordinateSystem(point)) {
        convertedPoint = point;
      } else {
        final GeometryFactory geometryFactory = getGeometryFactory();
        convertedPoint = point.convertPoint2d(geometryFactory);
      }
      final double x1 = getX();
      final double x2 = convertedPoint.getX();
      if (x1 < x2) {
        return -1;
      } else if (x1 > x2) {
        return 1;
      } else {
        final double y1 = getY();
        final double y2 = convertedPoint.getY();
        if (y1 < y2) {
          return -1;
        } else if (y1 > y2) {
          return 1;
        } else {
          return 0;
        }
      }
    }
  }

  @Override
  default int compareToSameClass(final Geometry other) {
    final Point point = (Point)other;
    return getPoint().compareTo(point.getPoint());
  }

  @Override
  default boolean contains(final double x, final double y) {
    return equalsVertex(x, y);
  }

  default double[] convertCoordinates(GeometryFactory geometryFactory) {
    final GeometryFactory sourceGeometryFactory = getGeometryFactory();
    final int targetAxisCount = geometryFactory.getAxisCount();
    final double[] targetCoordinates = getCoordinates(targetAxisCount);
    if (isEmpty()) {
      return targetCoordinates;
    } else {
      geometryFactory = getNonZeroGeometryFactory(geometryFactory);
      final CoordinatesOperation coordinatesOperation = sourceGeometryFactory
        .getCoordinatesOperation(geometryFactory);
      if (coordinatesOperation == null) {
        return targetCoordinates;
      } else {
        final CoordinatesOperationPoint point = newCoordinatesOperationPoint();
        coordinatesOperation.perform(point);
        point.copyCoordinatesTo(targetCoordinates);
        return targetCoordinates;
      }
    }
  }

  default Point convertPoint2d(final GeometryFactoryProxy geometryFactory) {
    if (isEmpty()) {
      return this;
    } else {
      final CoordinatesOperation coordinatesOperation = getGeometryFactory()
        .getCoordinatesOperation(geometryFactory);
      if (coordinatesOperation == null) {
        return this;
      } else {
        final CoordinatesOperationPoint point = newCoordinatesOperationPoint();
        coordinatesOperation.perform(point);
        return new PointDoubleXY(point.x, point.y);
      }
    }
  }

  default void copyCoordinates(final double[] coordinates) {
    for (int i = 0; i < coordinates.length; i++) {
      final double value = getCoordinate(i);
      coordinates[i] = value;
    }
  }

  /**
   * Copy the coordinates in this point to the coordinates array parameter and convert them to the geometry factory.
   *
   * @param geometryFactory
   * @param coordinates
   */
  @Deprecated
  default void copyCoordinates(GeometryFactory geometryFactory, final double[] coordinates) {
    final GeometryFactory sourceGeometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
    } else if (isEmpty()) {
      Arrays.fill(coordinates, Double.NaN);
    } else {
      copyCoordinates(coordinates);
      if (geometryFactory != null) {
        geometryFactory = getNonZeroGeometryFactory(geometryFactory);
        final CoordinatesOperation coordinatesOperation = sourceGeometryFactory
          .getCoordinatesOperation(geometryFactory);
        if (coordinatesOperation != null) {
          final CoordinatesOperationPoint point = newCoordinatesOperationPoint();
          coordinatesOperation.perform(point);
          point.copyCoordinatesTo(coordinates);
        }
      }
    }
  }

  default int copyCoordinates(final int axisCount, final double nanValue,
    final double[] destCoordinates, int destOffset) {
    if (isEmpty()) {
      return destOffset;
    } else {
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        double coordinate = getCoordinate(axisIndex);
        if (Double.isNaN(coordinate)) {
          coordinate = nanValue;
        }
        destCoordinates[destOffset++] = coordinate;
      }
      return destOffset;
    }
  }

  default double cross(final Point point) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final Point convertedPoint = point.convertPoint2d(geometryFactory);
    final double x2 = convertedPoint.getX();
    final double y2 = convertedPoint.getY();
    return getX() * y2 - getY() * x2;
  }

  /**
   * Computes the 3-dimensional Euclidean distance to another location.
   *
   * @param c a coordinate
   * @return the 3-dimensional Euclidean distance between the locations
   */
  default double distance3d(final Point point) {
    final double x = getX();
    final double y = getY();
    final double z = getZ();
    final GeometryFactory geometryFactory = getGeometryFactory();
    final Point convertedPoint = point.convertPoint2d(geometryFactory);
    final double x2 = convertedPoint.getX();
    final double y2 = convertedPoint.getY();
    final double z2 = point.getZ();
    final double dx = x - x2;
    final double dy = y - y2;
    final double dz = z - z2;
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  @Override
  default double distanceGeometry(final Geometry geometry, final double terminateDistance) {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      return distancePoint(point);
    } else if (isEmpty()) {
      return Double.POSITIVE_INFINITY;
    } else if (Property.isEmpty(geometry)) {
      return Double.POSITIVE_INFINITY;
    } else {
      return geometry.distancePoint(this, terminateDistance);
    }
  }

  default double distanceOrigin() {
    final double distanceOriginSquared = distanceOriginSquared();
    return Math.sqrt(distanceOriginSquared);
  }

  default double distanceOriginSquared() {
    return dot(this);
  }

  @Override
  default double distancePoint(final double x, final double y) {
    final double x1 = this.getX();
    final double y1 = this.getY();
    return Points.distance(x1, y1, x, y);
  }

  @Override
  default double distancePoint(final double x, final double y, final double terminateDistance) {
    final double x1 = this.getX();
    final double y1 = this.getY();
    return Points.distance(x1, y1, x, y);
  }

  @Override
  default double distancePoint(Point point) {
    if (isEmpty()) {
      return Double.POSITIVE_INFINITY;
    } else if (Property.isEmpty(point)) {
      return Double.POSITIVE_INFINITY;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      point = point.convertPoint2d(geometryFactory);
      final double x = point.getX();
      final double y = point.getY();
      final double x1 = this.getX();
      final double y1 = this.getY();
      return Points.distance(x1, y1, x, y);
    }
  }

  default double distanceSquared(final double x, final double y) {
    final double x1 = this.getX();
    final double y1 = this.getY();
    final double dx = x - x1;
    final double dy = y - y1;
    final double distanceSquared = dx * dx + dy * dy;
    return distanceSquared;
  }

  default double dot(final Point point) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final Point convertedPoint = point.convertPoint2d(geometryFactory);
    final double x2 = convertedPoint.getX();
    final double y2 = convertedPoint.getY();

    final double x1 = getX();
    final double y1 = getY();
    return x1 * x2 + y1 * y2;
  }

  @Override
  default boolean equals(final int axisCount, final Geometry geometry) {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      return equals(axisCount, point);
    } else {
      return false;
    }
  }

  default boolean equals(final int axisCount, final Point point) {
    if (point == this) {
      return true;
    } else if (point == null) {
      return false;
    } else if (axisCount < 2) {
      throw new IllegalArgumentException("Axis Count must be >=2");
    } else {
      if (isEmpty()) {
        return point.isEmpty();
      } else if (point.isEmpty()) {
        return false;
      } else if (equals(point)) {
        for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
          final double value = getCoordinate(axisIndex);
          final double value2 = point.getCoordinate(axisIndex);
          if (!Doubles.equal(value, value2)) {
            return false;
          }
        }
        return true;
      } else {
        return false;
      }
    }
  }

  default boolean equals(final Point point) {
    if (point == null) {
      return false;
    } else if (isEmpty()) {
      return point.isEmpty();
    } else if (point.isEmpty()) {
      return false;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Point convertedPoint = point.convertPoint2d(geometryFactory);
      final double x2 = convertedPoint.getX();
      final double y2 = convertedPoint.getY();
      return equalsVertex(x2, y2);
    }
  }

  @Override
  default boolean equalsExact(final Geometry other, final double tolerance) {
    if (other instanceof Point) {
      final Point point = (Point)other;
      if (isEmpty() && other.isEmpty()) {
        return true;
      } else if (isEmpty() != other.isEmpty()) {
        return false;
      } else {
        return equal(point, getPoint(), tolerance);
      }
    } else {
      return false;
    }
  }

  default boolean equalsVertex(final double... coordinates) {
    for (int i = 0; i < coordinates.length; i++) {
      final double coordinate2 = coordinates[i];
      final double coordinate = getCoordinate(i);
      if (!Doubles.equal(coordinate, coordinate2)) {
        return false;
      }
    }
    return true;
  }

  default boolean equalsVertex(final double x, final double y) {
    if (Double.compare(x, getX()) == 0) {
      if (Double.compare(y, getY()) == 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tests if another coordinate has the same values for the X and Y ordinates.
   * The Z ordinate is ignored.
   *
   *@param other a <code>Coordinate</code> with which to do the 2D comparison.
   *@return true if <code>other</code> is a <code>Coordinate</code>
   *      with the same values for X and Y.
   */
  default boolean equalsVertex2d(final Point point, final double tolerance) {
    if (point == null) {
      return false;
    } else if (isEmpty()) {
      return point.isEmpty();
    } else if (point.isEmpty()) {
      return false;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Point convertedPoint = point.convertPoint2d(geometryFactory);
      final double x2 = convertedPoint.getX();
      final double y2 = convertedPoint.getY();
      final double x = getX();
      final double y = getY();
      if (NumberUtil.equalsWithTolerance(x, x2, tolerance)) {
        if (NumberUtil.equalsWithTolerance(y, y2, tolerance)) {
          return true;
        }
      }
      return false;
    }
  }

  /**
   * First the vertex or segment that is within the specified maxDistance. If a vertex is within the
   * distance, then it is returned, even if a segment is closer.
   * @param x
   * @param y
   * @param maxDistance
   * @return
   */
  @Override
  default Pair<GeometryComponent, Double> findGeometryComponentWithinDistance(final double x,
    final double y, final double maxDistance) {
    if (!isEmpty()) {
      final double distance = distancePoint(x, y);
      if (distance < maxDistance) {
        return new Pair<>(vertices(), distance);
      } else {

      }
    }
    return new Pair<>();
  }

  @Override
  default <R> R findVertex(final BiFunctionDouble<R> action) {
    if (!isEmpty()) {
      final double x = getX();
      final double y = getY();
      return action.accept(x, y);
    }
    return null;
  }

  @Override
  default void forEachVertex(final BiConsumerDouble action) {
    if (!isEmpty()) {
      final double x = getX();
      final double y = getY();
      action.accept(x, y);
    }
  }

  @Override
  default void forEachVertex(final Consumer3Double action) {
    if (!isEmpty()) {
      final double x = getX();
      final double y = getY();
      final double z = getZ();
      action.accept(x, y, z);
    }
  }

  default void forEachVertex(final Consumer4Double action) {
    if (!isEmpty()) {
      final double x = getX();
      final double y = getY();
      final double z = getZ();
      final double m = getM();
      action.accept(x, y, z, m);
    }
  }

  @Override
  default void forEachVertex(final CoordinatesOperation coordinatesOperation,
    final CoordinatesOperationPoint point, final Consumer<CoordinatesOperationPoint> action) {
    if (!isEmpty()) {
      point.x = getX();
      point.y = getY();
      point.z = getZ();
      coordinatesOperation.perform(point);
      action.accept(point);
    }
  }

  @Override
  default void forEachVertex(final CoordinatesOperationPoint point,
    final Consumer<CoordinatesOperationPoint> action) {
    if (!isEmpty()) {
      point.x = getX();
      point.y = getY();
      point.z = getZ();
      action.accept(point);
    }
  }

  @Override
  default double getArea() {
    return 0.0;
  }

  /**
   * Gets the boundary of this geometry.
   * Zero-dimensional geometries have no boundary by definition,
   * so an empty GeometryCollection is returned.
   *
   * @return an empty GeometryCollection
   * @see Geometry#getBoundary
   */
  @Override
  default Geometry getBoundary() {
    return getGeometryFactory().geometryCollection();
  }

  @Override
  default Dimension getBoundaryDimension() {
    return Dimension.FALSE;
  }

  @Override
  default BoundingBox getBoundingBox() {
    return this;
  }

  @Override
  default Point getCentroid() {
    return newGeometry(2);
  }

  /**
   * Gets the ordinate value for the given index.
   * The supported values for the index are
   * {@link #X}, {@link #Y}, and {@link #Z}.
   *
   * @param axisIndex the ordinate index
   * @return the value of the ordinate
   * @throws IllegalArgumentException if the index is not valid
   */
  double getCoordinate(int axisIndex);

  @Override
  default double getCoordinate(final int partIndex, final int axisIndex) {
    if (partIndex == 0) {
      return getCoordinate(axisIndex);
    } else {
      return Double.NaN;
    }
  }

  default double[] getCoordinates() {
    final int axisCount = getAxisCount();
    return getCoordinates(axisCount);
  }

  default double[] getCoordinates(final int axisCount) {
    final double[] coordinates = new double[axisCount];
    for (int i = 0; i < axisCount; i++) {
      coordinates[i] = getCoordinate(i);
    }
    return coordinates;
  }

  @Override
  default GeometryDataType<Point, PointEditor> getDataType() {
    return GeometryDataTypes.POINT;
  }

  @Override
  default Dimension getDimension() {
    return Dimension.P;
  }

  @Override
  default Point getInteriorPoint() {
    return this;
  }

  default double getM() {
    return getCoordinate(3);
  }

  @Override
  default double getMax(final int axisIndex) {
    final double coordinate = getCoordinate(axisIndex);
    if (Double.isFinite(coordinate)) {
      return coordinate;
    } else {
      return Double.NEGATIVE_INFINITY;
    }
  }

  @Override
  default double getMin(final int axisIndex) {
    final double coordinate = getCoordinate(axisIndex);
    if (Double.isFinite(coordinate)) {
      return coordinate;
    } else {
      return Double.POSITIVE_INFINITY;
    }
  }

  @Override
  default Point getPoint() {
    if (isEmpty()) {
      return null;
    } else {
      return this;
    }
  }

  @Override
  default Point getPoint(final int i) {
    if (i == 0) {
      return this;
    } else {
      return null;
    }
  }

  @Override
  default Point getPointWithin() {
    return newPoint2D();
  }

  @Override
  default Segment getSegment(final int... segmentId) {
    return null;
  }

  default long getTime() {
    return (long)getM();
  }

  @Override
  default Vertex getToVertex(final int... vertexId) {
    return getVertex(vertexId);
  }

  @Override
  default Vertex getVertex(final int... vertexId) {
    if (isEmpty()) {
      return null;
    } else {
      return new PointVertex(this);
    }
  }

  @Override
  default int getVertexCount() {
    return isEmpty() ? 0 : 1;
  }

  default double getX() {
    if (isEmpty()) {
      return Double.NaN;
    } else {
      return getCoordinate(0);
    }
  }

  default double getY() {
    if (isEmpty()) {
      return Double.NaN;
    } else {
      return getCoordinate(1);
    }
  }

  default double getZ() {
    return getCoordinate(2);
  }

  @Override
  default boolean hasInvalidXyCoordinates() {
    final double x = getX();
    if (!Double.isFinite(x)) {
      return true;
    }
    final double y = getY();
    if (!Double.isFinite(y)) {
      return true;
    }
    return false;
  }

  @Override
  default boolean hasPoint(final double x, final double y) {
    return equalsVertex(x, y);
  }

  @Override
  default Geometry intersectionBbox(final BoundingBox boundingBox) {
    notNullSameCs(boundingBox);
    if (bboxCoveredBy(boundingBox)) {
      return this;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      return geometryFactory.point();
    }
  }

  @Override
  default boolean intersects(final Geometry geometry) {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      return intersects(point);
    } else {
      return geometry.intersects(this);
    }
  }

  @Override
  default boolean intersects(final Point point) {
    if (isEmpty()) {
      return false;
    } else {
      return equals(point);
    }
  }

  @Override
  default boolean intersectsBbox(final BoundingBox boundingBox) {
    if (isEmpty() || boundingBox.isEmpty()) {
      return false;
    } else {
      final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
      final Point convertedPoint = convertPoint2d(geometryFactory);
      final double x = convertedPoint.getX();
      final double y = convertedPoint.getY();

      return boundingBox.bboxIntersects(x, y);
    }
  }

  /**
   * Tests if a point is contained in the boundary of the target rectangle.
   *
     * contains = false iff the point is properly contained in the rectangle.
     *
     * This code assumes that the point lies in the rectangle envelope
  * @param point the point to test
   * @return true if the point is contained in the boundary
   */
  @Override
  default boolean isContainedInBoundary(final BoundingBox boundingBox) {
    final double x = getX();
    final double y = getY();
    final double minX = boundingBox.getMinX();
    final double minY = boundingBox.getMinY();
    final double maxX = boundingBox.getMaxX();
    final double maxY = boundingBox.getMaxY();
    return x == minX || x == maxX || y == minY || y == maxY;
  }

  @Override
  default boolean isEquivalentClass(final Geometry other) {
    return other instanceof Point;
  }

  @Override
  default boolean isSimple() {
    return true;
  }

  @Override
  default boolean isValid() {
    if (isEmpty()) {
      return true;
    } else {
      final double x = getX();
      final double y = getY();
      if (Double.isFinite(x) && Double.isFinite(y)) {
        return true;
      } else {
        return false;
      }
    }
  }

  @Override
  default Location locate(final double x, final double y) {
    if (equalsVertex(x, y)) {
      return Location.INTERIOR;
    } else {
      return Location.EXTERIOR;
    }
  }

  @Override
  default Location locate(final Point point) {
    if (equals(2, point)) {
      return Location.INTERIOR;
    } else {
      return Location.EXTERIOR;
    }
  }

  default Point multiply(final double s) {
    final double x = getX();
    final double y = getY();
    return newPoint(x * s, y * s);
  }

  @Override
  default BoundingBox newBoundingBox() {
    return this;
  }

  default CoordinatesOperationPoint newCoordinatesOperationPoint() {
    final double x = getX();
    final double y = getY();
    final double z = getZ();
    final double m = getM();
    return new CoordinatesOperationPoint(x, y, z, m);
  }

  @Override
  default Point newGeometry(GeometryFactory geometryFactory) {
    final GeometryFactory sourceGeometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      return this.clone();
    } else if (isEmpty()) {
      return geometryFactory.point();
    } else {
      geometryFactory = getNonZeroGeometryFactory(geometryFactory);
      final CoordinatesOperation coordinatesOperation = sourceGeometryFactory
        .getCoordinatesOperation(geometryFactory);
      if (coordinatesOperation == null) {
        final double[] coordinates = getCoordinates();
        return geometryFactory.point(coordinates);
      } else {
        final CoordinatesOperationPoint point = newCoordinatesOperationPoint();
        coordinatesOperation.perform(point);
        final int targetAxisCount = geometryFactory.getAxisCount();
        final double[] targetCoordinates = new double[targetAxisCount];
        point.copyCoordinatesTo(targetCoordinates);
        return geometryFactory.point(targetCoordinates);
      }
    }
  }

  @Override
  default PointEditor newGeometryEditor() {
    return new PointEditor(this);
  }

  @Override
  default PointEditor newGeometryEditor(final AbstractGeometryEditor<?> parentEditor) {
    return new PointEditor((AbstractGeometryCollectionEditor<?, ?, ?>)parentEditor, this);
  }

  @Override
  default PointEditor newGeometryEditor(final int axisCount) {
    final PointEditor geometryEditor = newGeometryEditor();
    geometryEditor.setAxisCount(axisCount);
    return geometryEditor;
  }

  default Point newPoint() {
    GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      geometryFactory = GeometryFactory.DEFAULT_3D;
    }
    return geometryFactory.point(this);
  }

  default Point newPoint(final double x, final double y) {
    return new PointDoubleXY(x, y);
  }

  default Point newPoint(final GeometryFactory geometryFactory, final double... coordinates) {
    return geometryFactory.point(coordinates);
  }

  default Point newPoint2D() {
    final double x = getX();
    final double y = getY();
    return newPoint(x, y);
  }

  default Point newPointCoordinate(final int axisIndex, final double coordinate) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final int axisCount = geometryFactory.getAxisCount();
    if (axisIndex < axisCount) {
      final double[] coordinates = getCoordinates(axisCount);
      coordinates[axisIndex] = coordinate;
      return newPoint(geometryFactory, coordinates);
    }
    return this;
  }

  default Point newPointZ(final double z) {
    return newPointCoordinate(2, z);
  }

  @SuppressWarnings("unchecked")
  @Override
  default <G> G newUsingGeometryFactory(final GeometryFactory factory) {
    if (factory == getGeometryFactory()) {
      return (G)this;
    } else if (isEmpty()) {
      return (G)factory.point();
    } else {
      final double[] coordinates = getCoordinates();
      return (G)factory.point(coordinates);
    }
  }

  @Override
  default Point normalize() {
    return this;
  }

  /*
   * Does this vector lie on the left or right of ab? -1 = left 0 = on 1 = right
   */
  default int orientation(final Point point1, final Point point2) {
    final double x = getX();
    final double y = getY();

    final GeometryFactory geometryFactory = getGeometryFactory();
    final Point convertedPoint1 = point1.convertPoint2d(geometryFactory);
    final double x1 = convertedPoint1.getX();
    final double y1 = convertedPoint1.getY();

    final Point convertedPoint2 = point2.convertPoint2d(geometryFactory);
    final double x2 = convertedPoint2.getX();
    final double y2 = convertedPoint2.getY();

    final double det = (x1 - x) * (y2 - y) - (x2 - x) * (y1 - y);
    return Double.compare(det, 0.0);
  }

  @Override
  default List<Vertex> pointVertices() {
    if (isEmpty()) {
      return Collections.emptyList();
    } else {
      return Collections.singletonList(new PointVertex(this, 0));
    }
  }

  @Override
  default Point prepare() {
    return this;
  }

  @Override
  default Point removeDuplicatePoints() {
    return this;
  }

  @Override
  default Point reverse() {
    return this;
  }

  default double setCoordinate(final int axisIndex, final double coordinate) {
    throw new UnsupportedOperationException();
  }

  @Override
  default double setCoordinate(final int partIndex, final int axisIndex, final double coordinate) {
    if (partIndex == 0) {
      return setCoordinate(axisIndex, coordinate);
    } else {
      throw new ArrayIndexOutOfBoundsException(partIndex);
    }
  }

  default double setM(final double m) {
    return setCoordinate(M, m);
  }

  default double setX(final double x) {
    return setCoordinate(X, x);
  }

  default double setY(final double y) {
    return setCoordinate(Y, y);
  }

  default double setZ(final double z) {
    return setCoordinate(Z, z);
  }

  default Point subtract(final Point point) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final Point convertedPoint = point.convertPoint2d(geometryFactory);
    final double x2 = convertedPoint.getX();
    final double y2 = convertedPoint.getY();

    final double x1 = getX();
    final double y1 = getY();
    return newPoint(x1 - x2, y1 - y2);
  }

  @Override
  default Point union() {
    return this;
  }

  @Override
  default PointVertex vertices() {
    return new PointVertex(this, -1);
  }
}
