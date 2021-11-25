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
package com.revolsys.geometry.model.segment;

import org.jeometry.common.function.BiConsumerDouble;

import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDouble;

public class LineSegmentDoubleXY extends AbstractLineSegment {
  private static final long serialVersionUID = 1L;

  protected final double x1;

  protected final double y1;

  protected final double x2;

  protected final double y2;

  public LineSegmentDoubleXY() {
    this(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
  }

  public LineSegmentDoubleXY(final double x1, final double y1, final double x2, final double y2) {
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
  }

  @Override
  public LineSegmentDoubleXY clone() {
    return (LineSegmentDoubleXY)super.clone();
  }

  @Override
  public void forEachVertex(final BiConsumerDouble action) {
    if (!isEmpty()) {
      action.accept(this.x1, this.y1);
      action.accept(this.x2, this.y2);
    }
  }

  @Override
  public int getAxisCount() {
    return 2;
  }

  @Override
  public double getCoordinate(final int vertexIndex, final int axisIndex) {
    if (axisIndex == 0) {
      if (vertexIndex == 0) {
        return this.x1;
      } else if (vertexIndex == 1) {
        return this.x2;
      } else {
        return Double.NaN;
      }
    } else if (axisIndex == 1) {
      if (vertexIndex == 0) {
        return this.y1;
      } else if (vertexIndex == 1) {
        return this.y2;
      } else {
        return Double.NaN;
      }
    } else {
      return Double.NaN;
    }
  }

  @Override
  public double[] getCoordinates() {
    return new double[] {
      this.x1, this.y1, this.x2, this.y2
    };
  }

  @Override
  public double getX(final int vertexIndex) {
    if (vertexIndex == 0) {
      return this.x1;
    } else if (vertexIndex == 1) {
      return this.x2;
    } else {
      return Double.NaN;
    }
  }

  public double getX1() {
    return this.x1;
  }

  public double getX2() {
    return this.x2;
  }

  @Override
  public double getY(final int vertexIndex) {
    if (vertexIndex == 0) {
      return this.y1;
    } else if (vertexIndex == 1) {
      return this.y2;
    } else {
      return Double.NaN;
    }
  }

  public double getY1() {
    return this.y1;
  }

  public double getY2() {
    return this.y2;
  }

  @Override
  public double getZ(final int vertexIndex) {
    return Double.NaN;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public LineSegment newLineSegment(final int axisCount, final double... coordinates) {
    return new LineSegmentDoubleXY(coordinates[0], coordinates[1], coordinates[axisCount],
      coordinates[axisCount + 1]);
  }

  @Override
  public Point newPoint(final double... coordinates) {
    return new PointDouble(coordinates);
  }

}
