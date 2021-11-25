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
package com.revolsys.geometry.operation.buffer.validate;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.algorithm.distance.DiscreteHausdorffDistance;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.operation.distance.DistanceWithPoints;
import com.revolsys.record.io.format.wkt.EWktWriter;

/**
 * Validates that a given buffer curve lies an appropriate distance
 * from the input generating it.
 * Useful only for round buffers (cap and join).
 * Can be used for either positive or negative distances.
 * <p>
 * This is a heuristic test, and may return false positive results
 * (I.e. it may fail to detect an invalid result.)
 * It should never return a false negative result, however
 * (I.e. it should never report a valid result as invalid.)
 *
 * @author mbdavis
 *
 */
public class BufferDistanceValidator {
  /**
   * Maximum allowable fraction of buffer distance the
   * actual distance can differ by.
   * 1% sometimes causes an error - 1.2% should be safe.
   */
  private static final double MAX_DISTANCE_DIFF_FRAC = .012;

  private static boolean VERBOSE = false;

  private final double bufDistance;

  private String errMsg = null;

  private Geometry errorIndicator = null;

  private Point errorLocation = null;

  private final Geometry input;

  private boolean isValid = true;

  private double maxDistanceFound;

  private double maxValidDistance;

  private double minDistanceFound;

  private double minValidDistance;

  private final Geometry result;

  public BufferDistanceValidator(final Geometry input, final double bufDistance,
    final Geometry result) {
    this.input = input;
    this.bufDistance = bufDistance;
    this.result = result;
  }

  /**
   * Checks that the furthest distance from the buffer curve to the input
   * is less than the given maximum distance.
   * This uses the Oriented Hausdorff distance metric.
   * It corresponds to finding
   * the point on the buffer curve which is furthest from <i>some</i> point on the input.
   *
   * @param input a geometry
   * @param bufCurve a geometry
   * @param maxDist the maximum distance that a buffer result can be from the input
   */
  private void checkMaximumDistance(final Geometry input, final Geometry bufCurve,
    final double maxDist) {
    // BufferCurveMaximumDistanceFinder maxDistFinder = new
    // BufferCurveMaximumDistanceFinder(input);
    // maxDistanceFound = maxDistFinder.findDistance(bufCurve);

    final DiscreteHausdorffDistance haus = new DiscreteHausdorffDistance(bufCurve, input);
    haus.setDensifyFraction(0.25);
    this.maxDistanceFound = haus.orientedDistance();

    if (this.maxDistanceFound > maxDist) {
      this.isValid = false;
      final Point[] pts = haus.getCoordinates();
      this.errorLocation = pts[1];
      this.errorIndicator = input.getGeometryFactory().lineString(pts);
      this.errMsg = "Distance between buffer curve and input is too large " + "("
        + this.maxDistanceFound + " at " + EWktWriter.lineString(pts[0], pts[1]) + ")";
    }
  }

  /**
   * Checks that two geometries are at least a minumum distance apart.
   *
   * @param g1 a geometry
   * @param g2 a geometry
   * @param minDist the minimum distance the geometries should be separated by
   */
  private void checkMinimumDistance(final Geometry g1, final Geometry g2, final double minDist) {
    final DistanceWithPoints distOp = new DistanceWithPoints(g1, g2, minDist);
    this.minDistanceFound = distOp.distance();

    if (this.minDistanceFound < minDist) {
      this.isValid = false;
      final List<Point> pts = distOp.nearestPoints();
      this.errorLocation = pts.get(1);
      this.errorIndicator = g1.getGeometryFactory().lineString(pts);
      this.errMsg = "Distance between buffer curve and input is too small " + "("
        + this.minDistanceFound + " at " + EWktWriter.lineString(pts.get(0), this.errorLocation)
        + " )";
    }
  }

  private void checkNegativeValid() {
    // Assert: only polygonal inputs can be checked for negative buffers

    // MD - could generalize this to handle GCs too
    if (!(this.input instanceof Polygonal || this.input.isGeometryCollection())) {
      return;
    }
    final Geometry inputCurve = getPolygonLines(this.input);
    checkMinimumDistance(inputCurve, this.result, this.minValidDistance);
    if (!this.isValid) {
      return;
    }

    checkMaximumDistance(inputCurve, this.result, this.maxValidDistance);
  }

  private void checkPositiveValid() {
    final Geometry bufCurve = this.result.getBoundary();
    checkMinimumDistance(this.input, bufCurve, this.minValidDistance);
    if (!this.isValid) {
      return;
    }

    checkMaximumDistance(this.input, bufCurve, this.maxValidDistance);
  }

  /**
   * Gets a geometry which indicates the location and nature of a validation failure.
   * <p>
   * The indicator is a line segment showing the location and size
   * of the distance discrepancy.
   *
   * @return a geometric error indicator
   * or null if no error was found
   */
  public Geometry getErrorIndicator() {
    return this.errorIndicator;
  }

  public Point getErrorLocation() {
    return this.errorLocation;
  }

  public String getErrorMessage() {
    return this.errMsg;
  }

  private Geometry getPolygonLines(final Geometry geometry) {
    final List<LineString> lines = new ArrayList<>();
    for (final Polygon polygon : geometry.getGeometries(Polygon.class)) {
      lines.addAll(polygon.getRings());
    }
    final GeometryFactory geometryFactory = geometry.getGeometryFactory();
    return geometryFactory.geometry(lines);
  }

  public boolean isValid() {
    final double posDistance = Math.abs(this.bufDistance);
    final double distDelta = MAX_DISTANCE_DIFF_FRAC * posDistance;
    this.minValidDistance = posDistance - distDelta;
    this.maxValidDistance = posDistance + distDelta;

    // can't use this test if either is empty
    if (this.input.isEmpty() || this.result.isEmpty()) {
      return true;
    }

    if (this.bufDistance > 0.0) {
      checkPositiveValid();
    } else {
      checkNegativeValid();
    }
    if (VERBOSE) {
      System.out.println("Min Dist= " + this.minDistanceFound + "  err= "
        + (1.0 - this.minDistanceFound / this.bufDistance) + "  Max Dist= " + this.maxDistanceFound
        + "  err= " + (this.maxDistanceFound / this.bufDistance - 1.0));
    }
    return this.isValid;
  }

  /*
   * private void OLDcheckMaximumDistance(Geometry input, Geometry bufCurve,
   * double maxDist) { BufferCurveMaximumDistanceFinder maxDistFinder = new
   * BufferCurveMaximumDistanceFinder(input); maxDistanceFound =
   * maxDistFinder.findDistance(bufCurve); if (maxDistanceFound > maxDist) {
   * isValid = false; PointPairDistance ptPairDist =
   * maxDistFinder.getDistancePoints(); errorLocation =
   * ptPairDist.getCoordinate(1); errMsg =
   * "Distance between buffer curve and input is too large " + "(" +
   * ptPairDist.getDistance() + " at " + ptPairDist.toString() +")"; } }
   */

}
