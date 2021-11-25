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

package com.revolsys.geometry.operation.distance3d;

import com.revolsys.geometry.algorithm.RayCrossingCounter;
import com.revolsys.geometry.math.Plane3D;
import com.revolsys.geometry.math.Vector3D;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.impl.PointDoubleXYZ;

/**
 * Models a polygon lying in a plane in 3-dimensional Cartesian space.
 * The polyogn representation is supplied
 * by a {@link Polygon},
 * containing coordinates with XYZ ordinates.
 * 3D polygons are assumed to lie in a single plane.
 * The plane best fitting the polygon coordinates is
 * computed and is represented by a {@link Plane3D}.
 *
 * @author mdavis
 *
 */
public class PlanarPolygon3D {

  private static LineString project(final LineString seq, final int facingPlane) {
    switch (facingPlane) {
      case Plane3D.XY_PLANE:
        return AxisPlaneCoordinateSequence.projectToXY(seq);
      case Plane3D.XZ_PLANE:
        return AxisPlaneCoordinateSequence.projectToXZ(seq);
      default:
        return AxisPlaneCoordinateSequence.projectToYZ(seq);
    }
  }

  private static Point project(final Point p, final int facingPlane) {
    switch (facingPlane) {
      case Plane3D.XY_PLANE:
        return new PointDoubleXY(p.getX(), p.getY());
      case Plane3D.XZ_PLANE:
        return new PointDoubleXY(p.getX(), p.getZ());
      // Plane3D.YZ
      default:
        return new PointDoubleXY(p.getY(), p.getZ());
    }
  }

  private int facingPlane = -1;

  private final Plane3D plane;

  private final Polygon poly;

  public PlanarPolygon3D(final Polygon poly) {
    this.poly = poly;
    this.plane = findBestFitPlane(poly);
    this.facingPlane = this.plane.closestAxisPlane();
  }

  /**
   * Computes an average normal vector from a list of polygon coordinates.
   * Uses Newell's method, which is based
   * on the fact that the vector with components
   * equal to the areas of the projection of the polygon onto
   * the Cartesian axis planes is normal.
   *
   * @param line the sequence of coordinates for the polygon
   * @return a normal vector
   */
  private Vector3D averageNormal(final LineString line) {
    final int vertexCount = line.getVertexCount();
    double sumX = 0;
    double sumY = 0;
    double sumZ = 0;
    for (int i = 0; i < vertexCount - 1; i++) {
      final double x1 = line.getX(0);
      final double y1 = line.getY(0);
      final double z1 = line.getZ(0);
      final double x2 = line.getX(1);
      final double y2 = line.getY(1);
      final double z2 = line.getZ(1);
      sumX += (y1 - y2) * (z1 + z2);
      sumY += (z1 - z2) * (x1 + x2);
      sumZ += (x1 - x2) * (y1 + y2);
    }
    final double x = sumX / vertexCount;
    final double y = sumY / vertexCount;
    final double z = sumZ / vertexCount;
    final Vector3D norm = Vector3D.newVector(x, y, z).normalize();
    return norm;
  }

  /**
   * Computes a point which is the average of all coordinates
   * in a sequence.
   * If the sequence lies in a single plane,
   * the computed point also lies in the plane.
   *
   * @param seq a coordinate sequence
   * @return a Point with averaged ordinates
   */
  private Point averagePoint(final LineString line) {
    final int vertexCount = line.getVertexCount();
    double sumX = 0;
    double sumY = 0;
    double sumZ = 0;
    for (int i = 0; i < vertexCount; i++) {
      sumX += line.getCoordinate(i, Geometry.X);
      sumY += line.getCoordinate(i, Geometry.Y);
      sumZ += line.getCoordinate(i, Geometry.Z);
    }
    final double x = sumX / vertexCount;
    final double y = sumY / vertexCount;
    final double z = sumZ / vertexCount;
    return new PointDoubleXYZ(x, y, z);
  }

  /**
   * Finds a best-fit plane for the polygon,
   * by sampling a few points from the exterior ring.
   * <p>
   * The algorithm used is Newell's algorithm:
   * - a base point for the plane is determined from the average of all vertices
   * - the normal vector is determined by
   *   computing the area of the projections on each of the axis planes
   *
   * @param poly the polygon to determine the plane for
   * @return the best-fit plane
   */
  private Plane3D findBestFitPlane(final Polygon poly) {
    final LineString seq = poly.getShell();
    final Point basePt = averagePoint(seq);
    final Vector3D normal = averageNormal(seq);
    return new Plane3D(normal, basePt);
  }

  public Plane3D getPlane() {
    return this.plane;
  }

  public Polygon getPolygon() {
    return this.poly;
  }

  public boolean intersects(final Point intPt) {
    if (Location.EXTERIOR == locate(intPt, this.poly.getShell())) {
      return false;
    }

    for (int i = 0; i < this.poly.getHoleCount(); i++) {
      if (Location.INTERIOR == locate(intPt, this.poly.getHole(i))) {
        return false;
      }
    }
    return true;
  }

  public boolean intersects(final Point pt, final LineString ring) {
    final LineString seq = ring;
    final LineString seqProj = project(seq, this.facingPlane);
    final Point ptProj = project(pt, this.facingPlane);
    return Location.EXTERIOR != RayCrossingCounter.locatePointInRing(ptProj, seqProj);
  }

  private Location locate(final Point pt, final LineString ring) {
    final LineString seq = ring;
    final LineString seqProj = project(seq, this.facingPlane);
    final Point ptProj = project(pt, this.facingPlane);
    return RayCrossingCounter.locatePointInRing(ptProj, seqProj);
  }

}
