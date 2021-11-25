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

import org.jeometry.common.math.Angle;

import com.revolsys.geometry.algorithm.CGAlgorithms;
import com.revolsys.geometry.algorithm.CGAlgorithmsDD;
import com.revolsys.geometry.algorithm.HCoordinate;
import com.revolsys.geometry.algorithm.LineIntersector;
import com.revolsys.geometry.algorithm.NotRepresentableException;
import com.revolsys.geometry.algorithm.RobustLineIntersector;
import com.revolsys.geometry.geomgraph.Position;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineJoin;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDouble;
import com.revolsys.geometry.util.Points;

/**
 * Generates segments which form an offset curve.
 * Supports all end cap and join options
 * provided for buffering.
 * This algorithm implements various heuristics to
 * produce smoother, simpler curves which are
 * still within a reasonable tolerance of the
 * true curve.
 *
 * @author Martin Davis
 *
 */
class OffsetSegmentGenerator {

  /**
   * Factor which controls how close curve vertices can be to be snapped
   */
  private static final double CURVE_VERTEX_SNAP_DISTANCE_FACTOR = 1.0E-6;

  /**
   * Factor which controls how close curve vertices on inside turns can be to be snapped
   */
  private static final double INSIDE_TURN_VERTEX_SNAP_DISTANCE_FACTOR = 1.0E-3;

  /**
   * Factor which determines how short closing segs can be for round buffers
   */
  private static final int MAX_CLOSING_SEG_LEN_FACTOR = 80;

  /**
   * Factor which controls how close offset segments can be to
   * skip adding a filler or mitre.
   */
  private static final double OFFSET_SEGMENT_SEPARATION_FACTOR = 1.0E-3;

  private final BufferParameters bufParams;

  /**
   * The Closing Segment Length Factor controls how long
   * "closing segments" are.  Closing segments are added
   * at the middle of inside corners to ensure a smoother
   * boundary for the buffer offset curve.
   * In some cases (particularly for round joins with default-or-better
   * quantization) the closing segments can be made quite short.
   * This substantially improves performance (due to fewer intersections being created).
   *
   * A closingSegFactor of 0 results in lines to the corner vertex
   * A closingSegFactor of 1 results in lines halfway to the corner vertex
   * A closingSegFactor of 80 results in lines 1/81 of the way to the corner vertex
   * (this option is reasonable for the very common default situation of round joins
   * and quadrantSegs >= 8)
   */
  private int closingSegLengthFactor = 1;

  private double distance = 0.0;

  /**
   * The angle quantum with which to approximate a fillet curve
   * (based on the input # of quadrant segments)
   */
  private final double filletAngleQuantum;

  private boolean hasNarrowConcaveAngle = false;

  private final LineIntersector li;

  private LineSegment offset0;

  private LineSegment offset1;

  private final GeometryFactory geometryFactory;

  private double s0X;

  private double s0Y;

  private double s1X;

  private double s1Y;

  private double s2X;

  private double s2Y;

  private final OffsetSegmentString segList;

  private int side = 0;

  public OffsetSegmentGenerator(final GeometryFactory geometryFactory,
    final BufferParameters bufParams, final double distance) {
    this.geometryFactory = geometryFactory;
    this.bufParams = bufParams;

    // compute intersections in full precision, to provide accuracy
    // the points are rounded as they are inserted into the curve line
    this.li = new RobustLineIntersector();
    this.filletAngleQuantum = Math.PI / 2.0 / bufParams.getQuadrantSegments();

    /**
     * Non-round joins cause issues with short closing segments, so don't use
     * them. In any case, non-round joins only really make sense for relatively
     * small buffer distances.
     */
    if (bufParams.getQuadrantSegments() >= 8 && bufParams.getJoinStyle() == LineJoin.ROUND) {
      this.closingSegLengthFactor = MAX_CLOSING_SEG_LEN_FACTOR;
    }
    this.distance = distance;
    // Choose the min vertex separation as a small fraction of the offset
    // distance.
    final double minimimVertexDistance = distance * CURVE_VERTEX_SNAP_DISTANCE_FACTOR;
    this.segList = new OffsetSegmentString(this.geometryFactory, minimimVertexDistance);
  }

  /**
   * Adds a bevel join connecting the two offset segments
   * around a reflex corner.
   *
   * @param offset0 the first offset segment
   * @param offset1 the second offset segment
   */
  private void addBevelJoin(final LineSegment offset0, final LineSegment offset1) {
    final double x01 = offset0.getX(1);
    final double y01 = offset0.getY(1);
    this.segList.addPoint(x01, y01);
    final double x10 = offset1.getX(0);
    final double y10 = offset1.getY(0);
    this.segList.addPoint(x10, y10);
  }

  private void addCollinear(final boolean addStartPoint) {
    /**
     * This test could probably be done more efficiently,
     * but the situation of exact collinearity should be fairly rare.
     */

    this.li.computeIntersectionLine(this.s0X, this.s0Y, this.s1X, this.s1Y, this.s1X, this.s1Y,
      this.s2X, this.s2Y);
    final int intersectionCount = this.li.getIntersectionCount();
    /**
     * if numInt is < 2, the lines are parallel and in the same direction. In
     * this case the point can be ignored, since the offset lines will also be
     * parallel.
     */
    if (intersectionCount >= 2) {
      final double x01 = this.offset0.getX(1);
      final double y01 = this.offset0.getY(1);
      final double x10 = this.offset1.getX(0);
      final double y10 = this.offset1.getY(0);
      /**
       * segments are collinear but reversing.
       * Add an "end-cap" fillet
       * all the way around to other direction This case should ONLY happen
       * for LineStrings, so the orientation is always CW. (Polygons can never
       * have two consecutive segments which are parallel but reversed,
       * because that would be a self intersection.
       *
       */
      if (this.bufParams.getJoinStyle() == LineJoin.BEVEL
        || this.bufParams.getJoinStyle() == LineJoin.MITER) {
        if (addStartPoint) {
          this.segList.addPoint(x01, y01);
        }
        this.segList.addPoint(x10, y10);
      } else {
        addFillet(this.s1X, this.s1Y, x01, y01, x10, y10, CGAlgorithms.CLOCKWISE, this.distance);
      }
    }
  }

  private void addFillet(final double x, final double y, final double x1, final double y1,
    final double x2, final double y2, final int direction, final double radius) {
    final double dx0 = x1 - x;
    final double dy0 = y1 - y;
    double startAngle = Math.atan2(dy0, dx0);
    final double dx1 = x2 - x;
    final double dy1 = y2 - y;
    final double endAngle = Math.atan2(dy1, dx1);

    if (direction == CGAlgorithms.CLOCKWISE) {
      if (startAngle <= endAngle) {
        startAngle += 2.0 * Math.PI;
      }
    } else { // direction == COUNTERCLOCKWISE
      if (startAngle >= endAngle) {
        startAngle -= 2.0 * Math.PI;
      }
    }
    this.segList.addPoint(x1, y1);
    addFillet(x, y, startAngle, endAngle, direction, radius);
    this.segList.addPoint(x2, y2);
  }

  /**
   * Adds points for a circular fillet arc
   * between two specified angles.
   * The start and end point for the fillet are not added -
   * the caller must add them if required.
   *
   * @param direction is -1 for a CW angle, 1 for a CCW angle
   * @param radius the radius of the fillet
   */
  private void addFillet(final double x, final double y, final double startAngle,
    final double endAngle, final int direction, final double radius) {
    final int directionFactor = direction == CGAlgorithms.CLOCKWISE ? -1 : 1;

    final double totalAngle = Math.abs(startAngle - endAngle);
    final int segmentCount = (int)(totalAngle / this.filletAngleQuantum + 0.5);

    if (segmentCount > 0) {
      // choose angle increment so that each segment has equal length
      final double initAngle = 0.0;
      final double currAngleInc = totalAngle / segmentCount;

      double currAngle = initAngle;
      while (currAngle < totalAngle) {
        final double angle = startAngle + directionFactor * currAngle;
        final double newX = x + radius * Math.cos(angle);
        final double newY = y + radius * Math.sin(angle);
        this.segList.addPoint(newX, newY);
        currAngle += currAngleInc;
      }
    }
  }

  public void addFirstSegment() {
    final double x10 = this.offset1.getX(0);
    final double y10 = this.offset1.getY(0);
    this.segList.addPoint(x10, y10);
  }

  /**
   * Adds the offset points for an inside (concave) turn.
   *
   * @param orientation
   * @param addStartPoint
   */
  private void addInsideTurn(final int orientation, final boolean addStartPoint) {
    final double x00 = this.offset0.getX(0);
    final double y00 = this.offset0.getY(0);
    final double x01 = this.offset0.getX(1);
    final double y01 = this.offset0.getY(1);
    final double x10 = this.offset1.getX(0);
    final double y10 = this.offset1.getY(0);
    final double x11 = this.offset1.getX(1);
    final double y11 = this.offset1.getY(1);
    /**
     * add intersection point of offset segments (if any)
     */
    this.li.computeIntersectionLine(x00, y00, x01, y01, x10, y10, x11, y11);
    if (this.li.hasIntersection()) {
      final Point intersection = this.li.getIntersection(0);
      final double intersectionX = intersection.getX();
      final double intersectionY = intersection.getY();
      this.segList.addPoint(intersectionX, intersectionY);
    } else {
      /**
       * If no intersection is detected,
       * it means the angle is so small and/or the offset so
       * large that the offsets segments don't intersect.
       * In this case we must
       * add a "closing segment" to make sure the buffer curve is continuous,
       * fairly smooth (e.g. no sharp reversals in direction)
       * and tracks the buffer correctly around the corner. The curve connects
       * the endpoints of the segment offsets to points
       * which lie toward the centre point of the corner.
       * The joining curve will not appear in the final buffer outline, since it
       * is completely internal to the buffer polygon.
       *
       * In complex buffer cases the closing segment may cut across many other
       * segments in the generated offset curve.  In order to improve the
       * performance of the noding, the closing segment should be kept as short as possible.
       * (But not too short, since that would defeat its purpose).
       * This is the purpose of the closingSegFactor heuristic value.
       */

      /**
       * The intersection test above is vulnerable to robustness errors; i.e. it
       * may be that the offsets should intersect very close to their endpoints,
       * but aren't reported as such due to rounding. To handle this situation
       * appropriately, we use the following test: If the offset points are very
       * close, don't add closing segments but simply use one of the offset
       * points
       */
      this.hasNarrowConcaveAngle = true;
      if (Points.distance(x01, y01, x10, y10) < this.distance
        * INSIDE_TURN_VERTEX_SNAP_DISTANCE_FACTOR) {
        this.segList.addPoint(x01, y01);
      } else {
        // add endpoint of this segment offset
        this.segList.addPoint(x01, y01);

        /**
         * Add "closing segment" of required length.
         */
        if (this.closingSegLengthFactor > 0) {
          final double midX0 = (this.closingSegLengthFactor * x01 + this.s1X)
            / (this.closingSegLengthFactor + 1);
          final double midY0 = (this.closingSegLengthFactor * y01 + this.s1Y)
            / (this.closingSegLengthFactor + 1);
          this.segList.addPoint(midX0, midY0);
          final double midX1 = (this.closingSegLengthFactor * x10 + this.s1X)
            / (this.closingSegLengthFactor + 1);
          final double midY1 = (this.closingSegLengthFactor * y10 + this.s1Y)
            / (this.closingSegLengthFactor + 1);
          this.segList.addPoint(midX1, midY1);
        } else {
          /**
           * This branch is not expected to be used except for testing purposes.
           * It is equivalent to the JTS 1.9 logic for closing segments
           * (which results in very poor performance for large buffer distances)
           */
          this.segList.addPoint(this.s1X, this.s1Y);
        }

        // */
        // add start point of next segment offset
        this.segList.addPoint(x10, y10);
      }
    }
  }

  /**
   * Add last offset point
   */
  public void addLastSegment() {
    final double x11 = this.offset1.getX(1);
    final double y11 = this.offset1.getY(1);
    this.segList.addPoint(x11, y11);
  }

  /**
   * Adds a limited mitre join connecting the two reflex offset segments.
   * A limited mitre is a mitre which is beveled at the distance
   * determined by the mitre ratio limit.
   *
   * @param offset0 the first offset segment
   * @param offset1 the second offset segment
   * @param distance the offset distance
   * @param mitreLimit the mitre limit ratio
   */
  private void addLimitedMitreJoin(final LineSegment offset0, final LineSegment offset1,
    final double distance, final double mitreLimit) {
    final double basePtX = this.s1X;
    final double basePtY = this.s1Y;

    final double ang0 = Angle.angle2d(basePtX, basePtY, this.s0X, this.s0Y);
    final double ang2 = Angle.angle2d(basePtX, basePtY, this.s2X, this.s2Y);

    // oriented angle between segments
    final double angDiff = Angle.angleBetweenOriented(ang0, ang2);
    // half of the interior angle
    final double angDiffHalf = angDiff / 2;

    // angle for bisector of the interior angle between the segments
    final double midAng = Angle.normalize(ang0 + angDiffHalf);
    // rotating this by PI gives the bisector of the reflex angle
    final double mitreMidAng = Angle.normalize(midAng + Math.PI);

    // the miterLimit determines the distance to the mitre bevel
    final double mitreDist = mitreLimit * distance;
    // the bevel delta is the difference between the buffer distance
    // and half of the length of the bevel segment
    final double bevelDelta = mitreDist * Math.abs(Math.sin(angDiffHalf));
    final double bevelHalfLen = distance - bevelDelta;

    // compute the midpoint of the bevel segment
    final double bevelMidX = basePtX + mitreDist * Math.cos(mitreMidAng);
    final double bevelMidY = basePtY + mitreDist * Math.sin(mitreMidAng);

    // compute the mitre midline segment from the corner point to the bevel
    // segment midpoint
    final LineSegment mitreMidLine = new LineSegmentDouble(2, basePtX, basePtY, bevelMidX,
      bevelMidY);

    // finally the bevel segment endpoints are computed as offsets from
    // the mitre midline
    final Point bevelEndLeft = mitreMidLine.pointAlongOffset(1.0, bevelHalfLen);
    final Point bevelEndRight = mitreMidLine.pointAlongOffset(1.0, -bevelHalfLen);

    if (this.side == Position.LEFT) {
      this.segList.addPoint(bevelEndLeft);
      this.segList.addPoint(bevelEndRight);
    } else {
      this.segList.addPoint(bevelEndRight);
      this.segList.addPoint(bevelEndLeft);
    }
  }

  /**
  * Add an end cap around point p1, terminating a line segment coming from p0
  */
  public void addLineEndCap(final double x1, final double y1, final double x2, final double y2) {
    final LineSegment offsetL = newOffsetSegment(x1, y1, x2, y2, Position.LEFT, this.distance);
    final LineSegment offsetR = newOffsetSegment(x1, y1, x2, y2, Position.RIGHT, this.distance);

    final double dx = x2 - x1;
    final double dy = y2 - y1;
    final double angle = Math.atan2(dy, dx);

    final double leftX2 = offsetL.getX(1);
    final double leftY2 = offsetL.getY(1);
    final double rightX2 = offsetR.getX(1);
    final double rightY2 = offsetR.getY(1);
    switch (this.bufParams.getEndCapStyle()) {
      case ROUND:
        // add offset seg points with a fillet between them
        this.segList.addPoint(leftX2, leftY2);
        addFillet(x2, y2, angle + Math.PI / 2, angle - Math.PI / 2, CGAlgorithms.CLOCKWISE,
          this.distance);
        this.segList.addPoint(rightX2, rightY2);
      break;
      case BUTT:
        // only offset segment points are added
        this.segList.addPoint(leftX2, leftY2);
        this.segList.addPoint(rightX2, rightY2);
      break;
      case SQUARE:
        final double absDistance = Math.abs(this.distance);
        // add a square defined by extensions of the offset segment endpoints
        final double squareCapSideOffsetX = absDistance * Math.cos(angle);
        final double squareCapSideOffsetY = absDistance * Math.sin(angle);

        final double lx = leftX2 + squareCapSideOffsetX;
        final double ly = leftY2 + squareCapSideOffsetY;
        this.segList.addPoint(lx, ly);

        final double rx = rightX2 + squareCapSideOffsetX;
        final double ry = rightY2 + squareCapSideOffsetY;
        this.segList.addPoint(rx, ry);
      break;

    }
  }

  /**
   * Adds a mitre join connecting the two reflex offset segments.
   * The mitre will be beveled if it exceeds the mitre ratio limit.
   *
   * @param offset0 the first offset segment
   * @param offset1 the second offset segment
   * @param distance the offset distance
   */
  private void addMitreJoin(final double x, final double y, final LineSegment offset0,
    final LineSegment offset1, final double distance) {
    boolean isMitreWithinLimit = true;
    double intPtX = 0;
    double intPtY = 0;

    /**
     * This computation is unstable if the offset segments are nearly collinear.
     * Howver, this situation should have been eliminated earlier by the check for
     * whether the offset segment endpoints are almost coincident
     */
    try {
      final double line1x1 = offset0.getX(0);
      final double line1y1 = offset0.getY(0);
      final double line1x2 = offset0.getX(1);
      final double line1y2 = offset0.getY(1);
      final double line2x1 = offset1.getX(0);
      final double line2y1 = offset1.getY(0);
      final double line2x2 = offset1.getX(1);
      final double line2y2 = offset1.getY(1);
      final Point intersection = HCoordinate.intersection(line1x1, line1y1, line1x2, line1y2,
        line2x1, line2y1, line2x2, line2y2);
      intPtX = intersection.getX();
      intPtY = intersection.getY();

      final double mitreRatio;
      if (distance <= 0.0) {
        mitreRatio = 1;
      } else {
        mitreRatio = Points.distance(intPtX, intPtY, x, y) / Math.abs(distance);
      }
      if (mitreRatio > this.bufParams.getMitreLimit()) {
        isMitreWithinLimit = false;
      }
    } catch (final NotRepresentableException ex) {
      isMitreWithinLimit = false;
    }

    if (isMitreWithinLimit) {
      this.segList.addPoint(intPtX, intPtY);
    } else {
      addLimitedMitreJoin(offset0, offset1, distance, this.bufParams.getMitreLimit());
    }
  }

  public void addNextSegment(final double x, final double y, final boolean addStartPoint) {
    // s0-s1-s2 are the coordinates of the previous segment and the current one
    this.s0X = this.s1X;
    this.s0Y = this.s1Y;
    this.s1X = this.s2X;
    this.s1Y = this.s2Y;
    this.s2X = x;
    this.s2Y = y;
    this.offset0 = newOffsetSegment(this.s0X, this.s0Y, this.s1X, this.s1Y, this.side,
      this.distance);
    this.offset1 = newOffsetSegment(this.s1X, this.s1Y, this.s2X, this.s2Y, this.side,
      this.distance);

    // do nothing if points are equal
    if (this.s1X != this.s2X || this.s1Y != this.s2Y) {
      final int orientation = CGAlgorithmsDD.orientationIndex(this.s0X, this.s0Y, this.s1X,
        this.s1Y, this.s2X, this.s2Y);
      final boolean outsideTurn = orientation == CGAlgorithms.CLOCKWISE
        && this.side == Position.LEFT
        || orientation == CGAlgorithms.COUNTERCLOCKWISE && this.side == Position.RIGHT;

      if (orientation == 0) { // lines are collinear
        addCollinear(addStartPoint);
      } else if (outsideTurn) {
        addOutsideTurn(orientation, addStartPoint);
      } else { // inside turn
        addInsideTurn(orientation, addStartPoint);
      }
    }
  }

  /**
   * Adds the offset points for an outside (convex) turn
   *
   * @param orientation
   * @param addStartPoint
   */
  private void addOutsideTurn(final int orientation, final boolean addStartPoint) {
    final double x01 = this.offset0.getX(1);
    final double y01 = this.offset0.getY(1);
    final double x10 = this.offset1.getX(0);
    final double y10 = this.offset1.getY(0);
    /**
     * Heuristic: If offset endpoints are very close together,
     * just use one of them as the corner vertex.
     * This avoids problems with computing mitre corners in the case
     * where the two segments are almost parallel
     * (which is hard to compute a robust intersection for).
     */
    if (Points.distance(x01, y01, x10, y10) < this.distance * OFFSET_SEGMENT_SEPARATION_FACTOR) {
      this.segList.addPoint(x01, y01);
      return;
    }

    if (this.bufParams.getJoinStyle() == LineJoin.MITER) {
      addMitreJoin(this.s1X, this.s1Y, this.offset0, this.offset1, this.distance);
    } else if (this.bufParams.getJoinStyle() == LineJoin.BEVEL) {
      addBevelJoin(this.offset0, this.offset1);
    } else {
      // add a circular fillet connecting the endpoints of the offset segments
      if (addStartPoint) {
        this.segList.addPoint(x01, y01);
      }
      // TESTING - comment out to produce beveled joins
      addFillet(this.s1X, this.s1Y, x01, y01, x10, y10, orientation, this.distance);
      this.segList.addPoint(x10, y10);
    }
  }

  public void addSegments(final LineString points, final boolean isForward) {
    this.segList.addPoints(points, isForward);
  }

  public void closeRing() {
    this.segList.closeRing();
  }

  public LineString getPoints() {
    return this.segList.getPoints();
  }

  /**
   * Tests whether the input has a narrow concave angle
   * (relative to the offset distance).
   * In this case the generated offset curve will contain self-intersections
   * and heuristic closing segments.
   * This is expected behaviour in the case of Buffer curves.
   * For pure Offset Curves,
   * the output needs to be further treated
   * before it can be used.
   *
   * @return true if the input has a narrow concave angle
   */
  public boolean hasNarrowConcaveAngle() {
    return this.hasNarrowConcaveAngle;
  }

  public void initSideSegments(final double s1X, final double s1Y, final double s2X,
    final double s2Y, final int side) {
    this.s1X = s1X;
    this.s1Y = s1Y;
    this.s2X = s2X;
    this.s2Y = s2Y;
    this.side = side;
    this.offset1 = newOffsetSegment(this.s1X, this.s1Y, this.s2X, this.s2Y, side, this.distance);
  }

  /**
   * Creates a CW circle around a point
   */
  public void newCircle(final double x, final double y) {
    // add start point
    final double newX = x + this.distance;
    this.segList.addPoint(newX, y);
    addFillet(x, y, 0.0, 2.0 * Math.PI, -1, this.distance);
    this.segList.closeRing();
  }

  /**
   * Compute an offset segment for an input segment on a given side and at a given distance.
   * The offset points are computed in full double precision, for accuracy.
   *
   * @param side the side of the segment ({@link Position}) the offset lies on
   * @param distance the offset distance
   * @param offset the points computed for the offset segment
   */
  public LineSegment newOffsetSegment(final double x1, final double y1, final double x2,
    final double y2, final int side, final double distance) {
    final int sideSign;
    if (side == Position.LEFT) {
      sideSign = 1;
    } else {
      sideSign = -1;
    }
    final double dx = x2 - x1;
    final double dy = y2 - y1;
    final double len = Math.sqrt(dx * dx + dy * dy);
    // u is the vector that is the length of the offset, in the direction of the
    // segment
    final double ux = sideSign * distance * dx / len;
    final double uy = sideSign * distance * dy / len;
    final double newX1 = this.geometryFactory.makePrecise(0, x1 - uy);
    final double newY1 = this.geometryFactory.makePrecise(1, y1 + ux);
    final double newX2 = this.geometryFactory.makePrecise(0, x2 - uy);
    final double newY2 = this.geometryFactory.makePrecise(1, y2 + ux);
    return new LineSegmentDouble(2, newX1, newY1, newX2, newY2);
  }

  /**
   * Creates a CW square around a point
   */
  public void newSquare(final double x, final double y) {
    this.segList.addPoint(x + this.distance, y + this.distance);
    this.segList.addPoint(x + this.distance, y - this.distance);
    this.segList.addPoint(x - this.distance, y - this.distance);
    this.segList.addPoint(x - this.distance, y + this.distance);
    this.segList.closeRing();
  }

  @Override
  public String toString() {
    return this.segList.toString();
  }
}
