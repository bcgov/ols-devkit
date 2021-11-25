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

package com.revolsys.geometry.operation.overlay.validate;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;

/**
 * Generates points offset by a given distance
 * from both sides of the midpoint of
 * all segments in a {@link Geometry}.
 * Can be used to generate probe points for
 * determining whether a polygonal overlay result
 * is incorrect.
 * The input geometry may have any orientation for its rings,
 * but {@link #setSidesToGenerate(boolean, boolean)} is
 * only meaningful if the orientation is known.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class OffsetPointGenerator {
  private boolean doLeft = true;

  private boolean doRight = true;

  private final Geometry g;

  public OffsetPointGenerator(final Geometry g) {
    this.g = g;
  }

  /**
   * Generates the two points which are offset from the
   * midpoint of the segment <tt>(p0, p1)</tt> by the
   * <tt>offsetDistance</tt>.
   *
   * @param p0 the first point of the segment to offset from
   * @param p1 the second point of the segment to offset from
   */
  private void computeOffsetPoints(final double x1, final double y1, final double x2,
    final double y2, final double offsetDistance, final List<Point> offsetPts) {
    final double dx = x2 - x1;
    final double dy = y2 - y1;
    final double len = Math.sqrt(dx * dx + dy * dy);
    // u is the vector that is the length of the offset, in the direction of the
    // segment
    final double ux = offsetDistance * dx / len;
    final double uy = offsetDistance * dy / len;

    final double midX = (x2 + x1) / 2;
    final double midY = (y2 + y1) / 2;

    if (this.doLeft) {
      final Point offsetLeft = new PointDoubleXY(midX - uy, midY + ux);
      offsetPts.add(offsetLeft);
    }

    if (this.doRight) {
      final Point offsetRight = new PointDoubleXY(midX + uy, midY - ux);
      offsetPts.add(offsetRight);
    }
  }

  private void extractPoints(final LineString line, final double offsetDistance,
    final List<Point> offsetPts) {
    final int vertexCount = line.getVertexCount();
    double x1 = line.getX(0);
    double y1 = line.getY(0);
    for (int vertexIndex = 1; vertexIndex < vertexCount; vertexIndex++) {
      final double x2 = line.getX(vertexIndex);
      final double y2 = line.getY(vertexIndex);
      computeOffsetPoints(x1, y1, x2, y2, offsetDistance, offsetPts);
      x1 = x2;
      y1 = y2;
    }
  }

  /**
   * Gets the computed offset points.
   *
   * @return List<Point>
   */
  public List<Point> getPoints(final double offsetDistance) {
    final List<Point> offsetPts = new ArrayList<>();
    final List<LineString> lines = this.g.getGeometryComponents(LineString.class);
    for (final LineString line : lines) {
      extractPoints(line, offsetDistance, offsetPts);
    }
    return offsetPts;
  }

  /**
   * Set the sides on which to generate offset points.
   *
   * @param doLeft
   * @param doRight
   */
  public void setSidesToGenerate(final boolean doLeft, final boolean doRight) {
    this.doLeft = doLeft;
    this.doRight = doRight;
  }

}
