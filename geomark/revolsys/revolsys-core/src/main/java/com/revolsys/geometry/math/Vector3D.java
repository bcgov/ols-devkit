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

package com.revolsys.geometry.math;

import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXYZ;

/**
 * Represents a vector in 3-dimensional Cartesian space.
 *
 * @author mdavis
 *
 */
public class Vector3D {

  /**
   * Computes the 3D dot-product of two {@link Coordinates}s.
   *
   * @param v1 the first vector
   * @param v2 the second vector
   * @return the dot product of the vectors
   */
  public static double dot(final Point v1, final Point v2) {
    return v1.getX() * v2.getX() + v1.getY() * v2.getY() + v1.getZ() * v2.getZ();
  }

  /**
   * Computes the dot product of the 3D vectors AB and CD.
   *
   * @param A
   * @param B
   * @param C
   * @param D
   * @return the dot product
   */
  public static double dot(final Point A, final Point B, final Point C, final Point D) {
    final double ABx = B.getX() - A.getX();
    final double ABy = B.getY() - A.getY();
    final double ABz = B.getZ() - A.getZ();
    final double CDx = D.getX() - C.getX();
    final double CDy = D.getY() - C.getY();
    final double CDz = D.getZ() - C.getZ();
    return ABx * CDx + ABy * CDy + ABz * CDz;
  }

  public static double length(final Point v) {
    return Math.sqrt(v.getX() * v.getX() + v.getY() * v.getY() + v.getZ() * v.getZ());
  }

  /**
   * Creates a new vector with given X and Y components.
   *
   * @param x
   *            the x component
   * @param y
   *            the y component
   * @param z
   *            the z component
   * @return a new vector
   */
  public static Vector3D newVector(final double x, final double y, final double z) {
    return new Vector3D(x, y, z);
  }

  /**
   * Creates a vector from a {@link Coordinates}.
   *
   * @param coord
   *            the Point to copy
   * @return a new vector
   */
  public static Vector3D newVector(final Point coord) {
    return new Vector3D(coord);
  }

  public static Point normalize(final Point point) {
    final double len = length(point);
    return new PointDoubleXYZ(point.getX() / len, point.getY() / len, point.getZ() / len);
  }

  private final double x;

  private final double y;

  private final double z;

  public Vector3D(final double x, final double y, final double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Vector3D(final Point v) {
    this.x = v.getX();
    this.y = v.getY();
    this.z = v.getZ();
  }

  public Vector3D(final Point from, final Point to) {
    this.x = to.getX() - from.getX();
    this.y = to.getY() - from.getY();
    this.z = to.getZ() - from.getZ();
  }

  private Vector3D divide(final double d) {
    return newVector(this.x / d, this.y / d, this.z / d);
  }

  /**
   * Computes the dot-product of two vectors
   *
   * @param v
   *            a vector
   * @return the dot product of the vectors
   */
  public double dot(final Vector3D v) {
    return this.x * v.x + this.y * v.y + this.z * v.z;
  }

  public double getX() {
    return this.x;
  }

  public double getY() {
    return this.y;
  }

  public double getZ() {
    return this.z;
  }

  public double length() {
    return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
  }

  public Vector3D normalize() {
    final double length = length();
    if (length > 0.0) {
      return divide(length());
    }
    return newVector(0.0, 0.0, 0.0);
  }

  /**
   * Gets a string representation of this vector
   *
   * @return a string representing this vector
   */
  @Override
  public String toString() {
    return "[" + this.x + ", " + this.y + ", " + this.z + "]";
  }

}
