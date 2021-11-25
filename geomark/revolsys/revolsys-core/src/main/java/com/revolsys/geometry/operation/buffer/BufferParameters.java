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

import com.revolsys.geometry.model.LineCap;
import com.revolsys.geometry.model.LineJoin;

/**
 * A value class containing the parameters which
 * specify how a buffer should be constructed.
 * <p>
 * The parameters allow control over:
 * <ul>
 * <li>Quadrant segments (accuracy of approximation for circular arcs)
 * <li>End Cap style
 * <li>Join style
 * <li>Mitre limit
 * <li>whether the buffer is single-sided
 * </ul>
 *
 * @author Martin Davis
 *
 */
public class BufferParameters {
  /**
   * The default mitre limit
   * Allows fairly pointy mitres.
   */
  public static final double DEFAULT_MITRE_LIMIT = 5.0;

  /**
   * The default number of facets into which to divide a fillet of 90 degrees.
   * A value of 8 gives less than 2% max error in the buffer distance.
   * For a max error of < 1%, use QS = 12.
   * For a max error of < 0.1%, use QS = 18.
   */
  public static final int DEFAULT_QUADRANT_SEGMENTS = 8;

  /**
   * Computes the maximum distance error due to a given level
   * of approximation to a true arc.
   *
   * @param quadSegs the number of segments used to approximate a quarter-circle
   * @return the error of approximation
   */
  public static double bufferDistanceError(final int quadSegs) {
    final double alpha = Math.PI / 2.0 / quadSegs;
    return 1 - Math.cos(alpha / 2.0);
  }

  private LineCap endCapStyle = LineCap.ROUND;

  private boolean isSingleSided = false;

  private LineJoin joinStyle = LineJoin.ROUND;

  private double mitreLimit = DEFAULT_MITRE_LIMIT;

  private int quadrantSegments = DEFAULT_QUADRANT_SEGMENTS;

  /**
   * Creates a default set of parameters
   *
   */
  public BufferParameters() {
  }

  /**
   * Creates a set of parameters with the
   * given quadrantSegments value.
   *
   * @param quadrantSegments the number of quadrant segments to use
   */
  public BufferParameters(final int quadrantSegments) {
    setQuadrantSegments(quadrantSegments);
  }

  /**
   * Creates a set of parameters with the
   * given quadrantSegments and endCapStyle values.
   *
   * @param quadrantSegments the number of quadrant segments to use
   * @param endCapStyle the end cap style to use
   */
  public BufferParameters(final int quadrantSegments, final LineCap endCapStyle) {
    setQuadrantSegments(quadrantSegments);
    setEndCapStyle(endCapStyle);
  }

  /**
   * Creates a set of parameters with the
   * given parameter values.
   *
   * @param quadrantSegments the number of quadrant segments to use
   * @param endCapStyle the end cap style to use
   * @param joinStyle the join style to use
   * @param mitreLimit the mitre limit to use
   */
  public BufferParameters(final int quadrantSegments, final LineCap endCapStyle,
    final LineJoin joinStyle, final double mitreLimit) {
    setQuadrantSegments(quadrantSegments);
    setEndCapStyle(endCapStyle);
    setJoinStyle(joinStyle);
    setMitreLimit(mitreLimit);
  }

  /**
   * Gets the end cap style.
   *
   * @return the end cap style
   */
  public LineCap getEndCapStyle() {
    return this.endCapStyle;
  }

  /**
   * Gets the join style
   *
   * @return the join style code
   */
  public LineJoin getJoinStyle() {
    return this.joinStyle;
  }

  /**
   * Gets the mitre ratio limit.
   *
   * @return the limit value
   */
  public double getMitreLimit() {
    return this.mitreLimit;
  }

  /**
   * Gets the number of quadrant segments which will be used
   *
   * @return the number of quadrant segments
   */
  public int getQuadrantSegments() {
    return this.quadrantSegments;
  }

  /**
   * Tests whether the buffer is to be generated on a single side only.
   *
   * @return true if the generated buffer is to be single-sided
   */
  public boolean isSingleSided() {
    return this.isSingleSided;
  }

  /**
   * Specifies the end cap style of the generated buffer.
   * The styles supported are {@link #CAP_ROUND}, {@link #CAP_FLAT}, and {@link #CAP_SQUARE}.
   * The default is CAP_ROUND.
   *
   * @param endCapStyle the end cap style to specify
   */
  public void setEndCapStyle(final LineCap endCapStyle) {
    this.endCapStyle = endCapStyle;
  }

  /**
   * Sets the join style for outside (reflex) corners between line segments.
   * Allowable values are {@link #JOIN_ROUND} (which is the default),
   * {@link #JOIN_MITRE} and {link JOIN_BEVEL}.
   *
   * @param joinStyle the code for the join style
   */
  public void setJoinStyle(final LineJoin joinStyle) {
    this.joinStyle = joinStyle;
  }

  /**
   * Sets the limit on the mitre ratio used for very sharp corners.
   * The mitre ratio is the ratio of the distance from the corner
   * to the end of the mitred offset corner.
   * When two line segments meet at a sharp angle,
   * a miter join will extend far beyond the original geometry.
   * (and in the extreme case will be infinitely far.)
   * To prevent unreasonable geometry, the mitre limit
   * allows controlling the maximum length of the join corner.
   * Corners with a ratio which exceed the limit will be beveled.
   *
   * @param mitreLimit the mitre ratio limit
   */
  public void setMitreLimit(final double mitreLimit) {
    this.mitreLimit = mitreLimit;
  }

  /**
   * Sets the number of line segments used to approximate an angle fillet.
   * <ul>
   * <li>If <tt>quadSegs</tt> >= 1, joins are round, and <tt>quadSegs</tt> indicates the number of
   * segments to use to approximate a quarter-circle.
   * <li>If <tt>quadSegs</tt> = 0, joins are bevelled (flat)
   * <li>If <tt>quadSegs</tt> < 0, joins are mitred, and the value of qs
   * indicates the mitre ration limit as
   * <pre>
   * mitreLimit = |<tt>quadSegs</tt>|
   * </pre>
   * </ul>
   * For round joins, <tt>quadSegs</tt> determines the maximum
   * error in the approximation to the true buffer curve.
   * The default value of 8 gives less than 2% max error in the buffer distance.
   * For a max error of < 1%, use QS = 12.
   * For a max error of < 0.1%, use QS = 18.
   * The error is always less than the buffer distance
   * (in other words, the computed buffer curve is always inside the true
   * curve).
   *
   * @param quadSegs the number of segments in a fillet for a quadrant
   */
  public void setQuadrantSegments(final int quadSegs) {
    this.quadrantSegments = quadSegs;

    /**
     * Indicates how to construct fillets.
     * If qs >= 1, fillet is round, and qs indicates number of
     * segments to use to approximate a quarter-circle.
     * If qs = 0, fillet is bevelled flat (i.e. no filleting is performed)
     * If qs < 0, fillet is mitred, and absolute value of qs
     * indicates maximum length of mitre according to
     *
     * mitreLimit = |qs|
     */
    if (this.quadrantSegments == 0) {
      this.joinStyle = LineJoin.BEVEL;
    }
    if (this.quadrantSegments < 0) {
      this.joinStyle = LineJoin.MITER;
      this.mitreLimit = Math.abs(this.quadrantSegments);
    }

    if (quadSegs <= 0) {
      this.quadrantSegments = 1;
    }

    /**
     * If join style was set by the quadSegs value,
     * use the default for the actual quadrantSegments value.
     */
    if (this.joinStyle != LineJoin.ROUND) {
      this.quadrantSegments = DEFAULT_QUADRANT_SEGMENTS;
    }
  }

  /**
   * Sets whether the computed buffer should be single-sided.
   * A single-sided buffer is constructed on only one side of each input line.
   * <p>
   * The side used is determined by the sign of the buffer distance:
   * <ul>
   * <li>a positive distance indicates the left-hand side
   * <li>a negative distance indicates the right-hand side
   * </ul>
   * The single-sided buffer of point geometries is
   * the same as the regular buffer.
   * <p>
   * The End Cap Style for single-sided buffers is
   * always ignored,
   * and forced to the equivalent of <tt>CAP_FLAT</tt>.
   *
   * @param isSingleSided true if a single-sided buffer should be constructed
   */
  public void setSingleSided(final boolean isSingleSided) {
    this.isSingleSided = isSingleSided;
  }
}
