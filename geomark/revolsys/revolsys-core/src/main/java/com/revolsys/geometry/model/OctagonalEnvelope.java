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

import com.revolsys.geometry.model.impl.PointDoubleXY;

/**
 * A Bounding Container which is in the shape of an octagon.
 * The OctagonalEnvelope of a geometric object
 * is tight along the four extremal rectilineal parallels
 * and along the four extremal diagonal parallels.
 * Depending on the shape of the contained
 * geometry, the octagon may be degenerate to any extreme
 * (e.g. it may be a rectangle, a line, or a point).
 */
public class OctagonalEnvelope {

  private static double SQRT2 = Math.sqrt(2.0);

  private static double computeA(final double x, final double y) {
    return x + y;
  }

  private static double computeB(final double x, final double y) {
    return x - y;
  }

  private double maxA;

  private double maxB;

  private double maxX;

  private double maxY;

  private double minA;

  private double minB;

  // initialize in the null state
  private double minX = Double.NaN;

  private double minY;

  /**
   * Creates a new null bounding octagon
   */
  public OctagonalEnvelope() {
  }

  /**
   * Creates a new null bounding octagon bounding an {@link BoundingBox}
   */
  public OctagonalEnvelope(final BoundingBox env) {
    expandToInclude(env);
  }

  /**
   * Creates a new null bounding octagon bounding a {@link Geometry}
   */
  public OctagonalEnvelope(final Geometry geom) {
    expandToInclude(geom);
  }

  /**
   * Creates a new null bounding octagon bounding an {@link OctagonalEnvelope}
   * (the copy constructor).
   */
  public OctagonalEnvelope(final OctagonalEnvelope oct) {
    expandToInclude(oct);
  }

  /**
   * Creates a new null bounding octagon bounding a {@link Coordinates}
   */
  public OctagonalEnvelope(final Point p) {
    expandToInclude(p);
  }

  /**
   * Creates a new null bounding octagon bounding a pair of {@link Coordinates}s
   */
  public OctagonalEnvelope(final Point p0, final Point p1) {
    expandToInclude(p0);
    expandToInclude(p1);
  }

  public boolean contains(final OctagonalEnvelope other) {
    if (isNull() || other.isNull()) {
      return false;
    }

    return other.minX >= this.minX && other.maxX <= this.maxX && other.minY >= this.minY
      && other.maxY <= this.maxY && other.minA >= this.minA && other.maxA <= this.maxA
      && other.minB >= this.minB && other.maxB <= this.maxB;
  }

  public void expandBy(final double distance) {
    if (isNull()) {
      return;
    }

    final double diagonalDistance = SQRT2 * distance;

    this.minX -= distance;
    this.maxX += distance;
    this.minY -= distance;
    this.maxY += distance;
    this.minA -= diagonalDistance;
    this.maxA += diagonalDistance;
    this.minB -= diagonalDistance;
    this.maxB += diagonalDistance;

    if (!isValid()) {
      setToNull();
    }
  }

  public OctagonalEnvelope expandToInclude(final BoundingBox env) {
    expandToInclude(env.getMinX(), env.getMinY());
    expandToInclude(env.getMinX(), env.getMaxY());
    expandToInclude(env.getMaxX(), env.getMinY());
    expandToInclude(env.getMaxX(), env.getMaxY());
    return this;
  }

  public OctagonalEnvelope expandToInclude(final double x, final double y) {
    final double A = computeA(x, y);
    final double B = computeB(x, y);

    if (isNull()) {
      this.minX = x;
      this.maxX = x;
      this.minY = y;
      this.maxY = y;
      this.minA = A;
      this.maxA = A;
      this.minB = B;
      this.maxB = B;
    } else {
      if (x < this.minX) {
        this.minX = x;
      }
      if (x > this.maxX) {
        this.maxX = x;
      }
      if (y < this.minY) {
        this.minY = y;
      }
      if (y > this.maxY) {
        this.maxY = y;
      }
      if (A < this.minA) {
        this.minA = A;
      }
      if (A > this.maxA) {
        this.maxA = A;
      }
      if (B < this.minB) {
        this.minB = B;
      }
      if (B > this.maxB) {
        this.maxB = B;
      }
    }
    return this;
  }

  public void expandToInclude(final Geometry geometry) {
    for (final Point point : geometry.getGeometries(Point.class)) {
      expandToInclude(point);
    }
    for (final LineString line : geometry.getGeometryComponents(LineString.class)) {
      expandToInclude(line);
    }
  }

  public OctagonalEnvelope expandToInclude(final LineString seq) {
    for (int i = 0; i < seq.getVertexCount(); i++) {
      final double x = seq.getX(i);
      final double y = seq.getY(i);
      expandToInclude(x, y);
    }
    return this;
  }

  public OctagonalEnvelope expandToInclude(final OctagonalEnvelope oct) {
    if (oct.isNull()) {
      return this;
    }

    if (isNull()) {
      this.minX = oct.minX;
      this.maxX = oct.maxX;
      this.minY = oct.minY;
      this.maxY = oct.maxY;
      this.minA = oct.minA;
      this.maxA = oct.maxA;
      this.minB = oct.minB;
      this.maxB = oct.maxB;
      return this;
    }
    if (oct.minX < this.minX) {
      this.minX = oct.minX;
    }
    if (oct.maxX > this.maxX) {
      this.maxX = oct.maxX;
    }
    if (oct.minY < this.minY) {
      this.minY = oct.minY;
    }
    if (oct.maxY > this.maxY) {
      this.maxY = oct.maxY;
    }
    if (oct.minA < this.minA) {
      this.minA = oct.minA;
    }
    if (oct.maxA > this.maxA) {
      this.maxA = oct.maxA;
    }
    if (oct.minB < this.minB) {
      this.minB = oct.minB;
    }
    if (oct.maxB > this.maxB) {
      this.maxB = oct.maxB;
    }
    return this;
  }

  public OctagonalEnvelope expandToInclude(final Point p) {
    expandToInclude(p.getX(), p.getY());
    return this;
  }

  public double getMaxA() {
    return this.maxA;
  }

  public double getMaxB() {
    return this.maxB;
  }

  public double getMaxX() {
    return this.maxX;
  }

  public double getMaxY() {
    return this.maxY;
  }

  public double getMinA() {
    return this.minA;
  }

  public double getMinB() {
    return this.minB;
  }

  public double getMinX() {
    return this.minX;
  }

  public double getMinY() {
    return this.minY;
  }

  public boolean intersects(final OctagonalEnvelope other) {
    if (isNull() || other.isNull()) {
      return false;
    }

    if (this.minX > other.maxX) {
      return false;
    }
    if (this.maxX < other.minX) {
      return false;
    }
    if (this.minY > other.maxY) {
      return false;
    }
    if (this.maxY < other.minY) {
      return false;
    }
    if (this.minA > other.maxA) {
      return false;
    }
    if (this.maxA < other.minA) {
      return false;
    }
    if (this.minB > other.maxB) {
      return false;
    }
    if (this.maxB < other.minB) {
      return false;
    }
    return true;
  }

  public boolean intersects(final Point p) {
    if (this.minX > p.getX()) {
      return false;
    }
    if (this.maxX < p.getX()) {
      return false;
    }
    if (this.minY > p.getY()) {
      return false;
    }
    if (this.maxY < p.getY()) {
      return false;
    }

    final double A = computeA(p.getX(), p.getY());
    final double B = computeB(p.getX(), p.getY());
    if (this.minA > A) {
      return false;
    }
    if (this.maxA < A) {
      return false;
    }
    if (this.minB > B) {
      return false;
    }
    if (this.maxB < B) {
      return false;
    }
    return true;
  }

  public boolean isNull() {
    return Double.isNaN(this.minX);
  }

  /**
   * Tests if the extremal values for this octagon are valid.
   *
   * @return <code>true</code> if this object has valid values
   */
  private boolean isValid() {
    if (isNull()) {
      return true;
    }
    return this.minX <= this.maxX && this.minY <= this.maxY && this.minA <= this.maxA
      && this.minB <= this.maxB;
  }

  /**
   *  Sets the value of this object to the null value
   */
  public void setToNull() {
    this.minX = Double.NaN;
  }

  public Geometry toGeometry(final GeometryFactory geometryFactory) {
    if (isNull()) {
      return geometryFactory.point();
    }

    final Point px00 = new PointDoubleXY(geometryFactory.makePrecise(0, this.minX),
      geometryFactory.makePrecise(1, this.minA - this.minX));
    final Point px01 = new PointDoubleXY(geometryFactory.makePrecise(0, this.minX),
      geometryFactory.makePrecise(1, this.minX - this.minB));

    final Point px10 = new PointDoubleXY(geometryFactory.makePrecise(0, this.maxX),
      geometryFactory.makePrecise(1, this.maxX - this.maxB));
    final Point px11 = new PointDoubleXY(geometryFactory.makePrecise(0, this.maxX),
      geometryFactory.makePrecise(1, this.maxA - this.maxX));

    final Point py00 = new PointDoubleXY(geometryFactory.makePrecise(0, this.minA - this.minY),
      geometryFactory.makePrecise(1, this.minY));
    final Point py01 = new PointDoubleXY(geometryFactory.makePrecise(0, this.minY + this.maxB),
      geometryFactory.makePrecise(1, this.minY));

    final Point py10 = new PointDoubleXY(geometryFactory.makePrecise(0, this.maxY + this.minB),
      geometryFactory.makePrecise(1, this.maxY));
    final Point py11 = new PointDoubleXY(geometryFactory.makePrecise(0, this.maxA - this.maxY),
      geometryFactory.makePrecise(1, this.maxY));

    final PointList coordList = new PointList();
    coordList.add(px00, false);
    coordList.add(px01, false);
    coordList.add(py10, false);
    coordList.add(py11, false);
    coordList.add(px11, false);
    coordList.add(px10, false);
    coordList.add(py01, false);
    coordList.add(py00, false);

    if (coordList.size() == 1) {
      return geometryFactory.point(px00);
    }
    if (coordList.size() == 2) {
      return geometryFactory.lineString(coordList);
    }
    // must be a polygon, so add closing point
    coordList.add(px00, false);
    return geometryFactory.polygon(geometryFactory.linearRing(coordList));
  }
}
