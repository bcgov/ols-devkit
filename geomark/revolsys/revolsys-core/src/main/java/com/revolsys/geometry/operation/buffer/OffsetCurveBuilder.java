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
package com.revolsys.geometry.operation.buffer;

import com.revolsys.geometry.geomgraph.Position;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;

/**
 * Computes the raw offset curve for a
 * single {@link Geometry} component (ring, line or point).
 * A raw offset curve line is not noded -
 * it may contain self-intersections (and usually will).
 * The final buffer polygon is computed by forming a topological graph
 * of all the noded raw curves and tracing outside contours.
 * The points in the raw curve are rounded
 * to a given scale.
 *
 * @version 1.7
 */
public class OffsetCurveBuilder {
  /**
   * Use a value which results in a potential distance error which is
   * significantly less than the error due to
   * the quadrant segment discretization.
   * For QS = 8 a value of 100 is reasonable.
   * This should produce a maximum of 1% distance error.
   */
  private static final double SIMPLIFY_FACTOR = 100.0;

  /**
   * Computes the distance tolerance to use during input
   * line simplification.
   *
   * @param distance the buffer distance
   * @return the simplification tolerance
   */
  private static double simplifyTolerance(final double bufDistance) {
    return bufDistance / SIMPLIFY_FACTOR;
  }

  private final BufferParameters bufParams;

  private double distance = 0.0;

  private final GeometryFactory precisionModel;

  public OffsetCurveBuilder(final GeometryFactory precisionModel,
    final BufferParameters bufParams) {
    this.precisionModel = precisionModel;
    this.bufParams = bufParams;
  }

  private void computeLineBufferCurve(final LineString points,
    final OffsetSegmentGenerator segGen) {
    final double distTol = simplifyTolerance(this.distance);

    // --------- compute points for left side of line
    // Simplify the appropriate side of the line before generating
    final LineString simp1 = BufferInputLineSimplifier.simplify(points, distTol);
    // MD - used for testing only (to eliminate simplification)
    // Point[] simp1 = inputPts;

    final int n1 = simp1.getVertexCount() - 1;
    final double x1 = simp1.getX(0);
    final double y1 = simp1.getY(0);
    final double x2 = simp1.getX(1);
    final double y2 = simp1.getY(1);
    segGen.initSideSegments(x1, y1, x2, y2, Position.LEFT);
    for (int i = 2; i <= n1; i++) {
      final double x = simp1.getX(i);
      final double y = simp1.getY(i);
      segGen.addNextSegment(x, y, true);
    }
    segGen.addLastSegment();
    // add line cap for end of line
    segGen.addLineEndCap(simp1.getX(n1 - 1), simp1.getY(n1 - 1), simp1.getX(n1), simp1.getY(n1));

    // ---------- compute points for right side of line
    // Simplify the appropriate side of the line before generating
    final LineString simp2 = BufferInputLineSimplifier.simplify(points, -distTol);
    // MD - used for testing only (to eliminate simplification)
    // Point[] simp2 = inputPts;
    final int n2 = simp2.getVertexCount() - 1;

    // since we are traversing line in opposite order, offset position is still
    // LEFT
    segGen.initSideSegments(simp2.getX(n2), simp2.getY(n2), simp2.getX(n2 - 1), simp2.getY(n2 - 1),
      Position.LEFT);
    for (int i = n2 - 2; i >= 0; i--) {
      final double x = simp2.getX(i);
      final double y = simp2.getY(i);
      segGen.addNextSegment(x, y, true);
    }
    segGen.addLastSegment();
    // add line cap for start of line
    segGen.addLineEndCap(simp2.getX(1), simp2.getY(1), simp2.getX(0), simp2.getY(0));

    segGen.closeRing();
  }

  private void computeOffsetCurve(final LineString points, final boolean isRightSide,
    final OffsetSegmentGenerator segGen) {
    final double distTol = simplifyTolerance(this.distance);

    if (isRightSide) {
      // ---------- compute points for right side of line
      // Simplify the appropriate side of the line before generating
      final LineString simp2 = BufferInputLineSimplifier.simplify(points, -distTol);
      // MD - used for testing only (to eliminate simplification)
      // Point[] simp2 = inputPts;
      final int n2 = simp2.getVertexCount() - 1;

      // since we are traversing line in opposite order, offset position is
      // still LEFT
      segGen.initSideSegments(simp2.getX(n2), simp2.getY(n2), simp2.getX(n2 - 1),
        simp2.getY(n2 - 1), Position.LEFT);
      segGen.addFirstSegment();
      for (int i = n2 - 2; i >= 0; i--) {
        final double x = simp2.getX(i);
        final double y = simp2.getY(i);
        segGen.addNextSegment(x, y, true);
      }
    } else {
      // --------- compute points for left side of line
      // Simplify the appropriate side of the line before generating
      final LineString simp1 = BufferInputLineSimplifier.simplify(points, distTol);
      // MD - used for testing only (to eliminate simplification)
      // Point[] simp1 = inputPts;

      final int n1 = simp1.getVertexCount() - 1;
      segGen.initSideSegments(simp1.getX(0), simp1.getY(0), simp1.getX(1), simp1.getY(1),
        Position.LEFT);
      segGen.addFirstSegment();
      for (int i = 2; i <= n1; i++) {
        final double x = simp1.getX(i);
        final double y = simp1.getY(i);
        segGen.addNextSegment(x, y, true);
      }
    }
    segGen.addLastSegment();
  }

  private void computePointCurve(final double x, final double y,
    final OffsetSegmentGenerator segGen) {
    switch (this.bufParams.getEndCapStyle()) {
      case ROUND:
        segGen.newCircle(x, y);
      break;
      case SQUARE:
        segGen.newSquare(x, y);
      break;
      case BUTT:
      break;
      // otherwise curve is empty (e.g. for a butt cap);
    }
  }

  private void computeRingBufferCurve(final LineString inputLine, final int side,
    final OffsetSegmentGenerator segGen) {
    // simplify input line to improve performance
    double distTol = simplifyTolerance(this.distance);
    // ensure that correct side is simplified
    if (side == Position.RIGHT) {
      distTol = -distTol;
    }
    final LineString simplifiedLine = BufferInputLineSimplifier.simplify(inputLine, distTol);

    final int n = simplifiedLine.getVertexCount() - 1;
    segGen.initSideSegments(simplifiedLine.getX(n - 1), simplifiedLine.getY(n - 1),
      simplifiedLine.getX(0), simplifiedLine.getY(0), side);
    boolean addStartPoint = false;
    for (int vertexIndex = 1; vertexIndex <= n; vertexIndex++) {
      final double x = simplifiedLine.getX(vertexIndex);
      final double y = simplifiedLine.getY(vertexIndex);
      segGen.addNextSegment(x, y, addStartPoint);
      addStartPoint = true;
    }
    segGen.closeRing();
  }

  private void computeSingleSidedBufferCurve(final LineString inputPts, final boolean isRightSide,
    final OffsetSegmentGenerator segGen) {
    final double distTol = simplifyTolerance(this.distance);

    if (isRightSide) {
      // add original line
      segGen.addSegments(inputPts, true);

      // ---------- compute points for right side of line
      // Simplify the appropriate side of the line before generating
      final LineString simp2 = BufferInputLineSimplifier.simplify(inputPts, -distTol);
      // MD - used for testing only (to eliminate simplification)
      // Point[] simp2 = inputPts;
      final int n2 = simp2.getVertexCount() - 1;

      // since we are traversing line in opposite order, offset position is
      // still LEFT
      segGen.initSideSegments(simp2.getX(n2), simp2.getY(n2), simp2.getX(n2 - 1),
        simp2.getY(n2 - 1), Position.LEFT);
      segGen.addFirstSegment();
      for (int i = n2 - 2; i >= 0; i--) {
        final double x = simp2.getX(i);
        final double y = simp2.getY(i);
        segGen.addNextSegment(x, y, true);
      }
    } else {
      // add original line
      segGen.addSegments(inputPts, false);

      // --------- compute points for left side of line
      // Simplify the appropriate side of the line before generating
      final LineString simp1 = BufferInputLineSimplifier.simplify(inputPts, distTol);
      // MD - used for testing only (to eliminate simplification)
      // Point[] simp1 = inputPts;

      final int n1 = simp1.getVertexCount() - 1;
      segGen.initSideSegments(simp1.getX(0), simp1.getY(0), simp1.getX(1), simp1.getY(1),
        Position.LEFT);
      segGen.addFirstSegment();
      for (int i = 2; i <= n1; i++) {
        final double x = simp1.getX(i);
        final double y = simp1.getY(i);
        segGen.addNextSegment(x, y, true);
      }
    }
    segGen.addLastSegment();
    segGen.closeRing();
  }

  /**
   * Gets the buffer parameters being used to generate the curve.
   *
   * @return the buffer parameters being used
   */
  public BufferParameters getBufferParameters() {
    return this.bufParams;
  }

  /**
   * This method handles single points as well as LineStrings.
   * LineStrings are assumed <b>not</b> to be closed (the function will not
   * fail for closed lines, but will generate superfluous line caps).
   *
   * @param inputPts the vertices of the line to offset
   * @param distance the offset distance
   *
   * @return a Point array representing the curve
   * or null if the curve is empty
   */
  public LineString getLineCurve(final LineString inputPts, final double distance) {
    this.distance = distance;

    // a zero or negative width buffer of a line/point is empty
    if (distance < 0.0 && !this.bufParams.isSingleSided()) {
      return null;
    }
    if (distance == 0.0) {
      return null;
    }

    final double posDistance = Math.abs(distance);
    final OffsetSegmentGenerator segGen = getSegGen(posDistance);
    final int vertexCount = inputPts.getVertexCount();
    if (vertexCount == 0) {
    } else if (vertexCount == 1) {
      final double x = inputPts.getX(0);
      final double y = inputPts.getY(0);
      computePointCurve(x, y, segGen);
    } else {
      if (this.bufParams.isSingleSided()) {
        final boolean isRightSide = distance < 0.0;
        computeSingleSidedBufferCurve(inputPts, isRightSide, segGen);
      } else {
        computeLineBufferCurve(inputPts, segGen);
      }
    }

    return segGen.getPoints();
  }

  public LineString getOffsetCurve(final LineString inputPts, final double distance) {
    this.distance = distance;

    // a zero width offset curve is empty
    if (distance == 0.0) {
      return null;
    }

    final boolean isRightSide = distance < 0.0;
    final double posDistance = Math.abs(distance);
    final OffsetSegmentGenerator segGen = getSegGen(posDistance);
    final int vertexCount = inputPts.getVertexCount();
    if (vertexCount == 0) {
    } else if (vertexCount == 1) {
      final double x = inputPts.getX(0);
      final double y = inputPts.getY(0);
      computePointCurve(x, y, segGen);
    } else {
      computeOffsetCurve(inputPts, isRightSide, segGen);
    }
    final LineString curvePts = segGen.getPoints();
    // for right side line is traversed in reverse direction, so have to reverse
    // generated line
    if (isRightSide) {
      curvePts.reverse();
    }
    return curvePts;
  }

  public LineString getPointCurve(final Point point, final double distance) {
    this.distance = distance;
    // a zero or negative width buffer of a line/point is empty
    if (distance < 0.0 && !this.bufParams.isSingleSided()) {
      return null;
    } else if (distance == 0.0) {
      return null;
    } else {
      final double posDistance = Math.abs(distance);
      final OffsetSegmentGenerator segGen = getSegGen(posDistance);
      final double x = point.getX();
      final double y = point.getY();
      computePointCurve(x, y, segGen);
      return segGen.getPoints();
    }
  }

  /**
   * This method handles the degenerate cases of single points and lines,
   * as well as rings.
   *
   * @return a Point array representing the curve
   * or null if the curve is empty
   */
  public LineString getRingCurve(final LineString points, final int side, final double distance) {
    this.distance = distance;
    if (points.getVertexCount() <= 2) {
      return getLineCurve(points, distance);
    }

    // optimize creating ring for for zero distance
    if (distance == 0.0) {
      return points.clone();
    } else {
      final OffsetSegmentGenerator segGen = getSegGen(distance);
      computeRingBufferCurve(points, side, segGen);
      return segGen.getPoints();
    }
  }

  private OffsetSegmentGenerator getSegGen(final double distance) {
    return new OffsetSegmentGenerator(this.precisionModel, this.bufParams, distance);
  }

}
