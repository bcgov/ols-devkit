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

package com.revolsys.geometry.algorithm.distance;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.util.Points;

/**
 * Contains a pair of points and the distance between them.
 * Provides methods to update with a new point pair with
 * either maximum or minimum distance.
 */
public class PointPairDistance {
  private double distance = Double.POSITIVE_INFINITY;

  private boolean isNull = true;

  private double x1 = Double.NaN;

  private double y1 = Double.NaN;

  private double x2 = Double.NaN;

  private double y2 = Double.NaN;

  public PointPairDistance() {
  }

  public double getDistance() {
    return this.distance;
  }

  public Point getPoint(final int i) {
    if (i == 0) {
      return new PointDoubleXY(this.x1, this.y1);
    } else if (i == 1) {
      return new PointDoubleXY(this.x2, this.y2);
    } else {
      throw new ArrayIndexOutOfBoundsException(i);
    }
  }

  public Point[] getPoints() {
    return new Point[] {
      new PointDoubleXY(this.x1, this.y1), new PointDoubleXY(this.x2, this.y2)
    };
  }

  public void initialize() {
    this.distance = Double.POSITIVE_INFINITY;
    this.isNull = true;
    this.x1 = Double.NaN;
    this.y1 = Double.NaN;
    this.x2 = Double.NaN;
    this.y2 = Double.NaN;
  }

  /**
   * Initializes the points, avoiding recomputing the distance.
   * @param p0
   * @param p1
   * @param distance the distance between p0 and p1
   */
  private void initialize(final double x1, final double y1, final double x2, final double y2,
    final double distance) {
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
    this.distance = distance;
    this.isNull = false;
  }

  public void setMaximum(final double x1, final double y1, final double x2, final double y2) {
    final double distance = Points.distance(x1, y1, x2, y2);
    if (this.isNull || distance > this.distance) {
      initialize(x1, y1, x2, y2, distance);
    }
  }

  public void setMaximum(final Point p0, final Point p1) {
    final double x1 = p0.getX();
    final double y1 = p0.getY();
    final double x2 = p1.getX();
    final double y2 = p1.getY();
    setMaximum(x1, y1, x2, y2);
  }

  public void setMaximum(final PointPairDistance pointPairDistance) {
    if (!pointPairDistance.isNull) {
      final double x1 = pointPairDistance.x1;
      final double y1 = pointPairDistance.y1;
      final double x2 = pointPairDistance.x2;
      final double y2 = pointPairDistance.y2;
      setMaximum(x1, y1, x2, y2);
    }
  }

  public void setMinimum(final double x1, final double y1, final double x2, final double y2) {
    final double distance = Points.distance(x1, y1, x2, y2);
    if (distance < this.distance) {
      initialize(x1, y1, x2, y2, distance);
    }
  }

  public void setMinimum(final Geometry geometry, final double x, final double y) {
    if (geometry.isGeometryCollection()) {
      for (final Geometry part : geometry.geometries()) {
        setMinimum(part, x, y);
      }
    } else if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      final double closestX = point.getX();
      final double closestY = point.getY();
      setMinimum(closestX, closestY, x, y);
    } else {
      for (final Segment segment : geometry.segments()) {
        final Point closestPoint = segment.closestPoint(x, y);
        final double closestX = closestPoint.getX();
        final double closestY = closestPoint.getY();
        setMinimum(closestX, closestY, x, y);
      }
    }
  }

  public void setMinimum(final Point p0, final Point p1) {
    final double x1 = p0.getX();
    final double y1 = p0.getY();
    final double x2 = p1.getX();
    final double y2 = p1.getY();
    setMinimum(x1, y1, x2, y2);
  }

  public void setMinimum(final PointPairDistance pointPairDistance) {
    if (!pointPairDistance.isNull) {
      final double x1 = pointPairDistance.x1;
      final double y1 = pointPairDistance.y1;
      final double x2 = pointPairDistance.x2;
      final double y2 = pointPairDistance.y2;
      setMinimum(x1, y1, x2, y2);
    }
  }

  @Override
  public String toString() {
    return "LINESTRING(" + this.x1 + ' ' + this.y1 + ',' + this.x2 + ' ' + this.y2 + ')';
  }
}
