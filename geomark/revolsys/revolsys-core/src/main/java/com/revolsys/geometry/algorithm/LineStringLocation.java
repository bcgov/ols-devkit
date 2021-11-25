package com.revolsys.geometry.algorithm;

import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;

/**
 * Represents a location along a {@link LineString}.
 */
public class LineStringLocation implements Comparable<LineStringLocation> {

  private final LineString line;

  private final double segmentFraction;

  private final int segmentIndex;

  private final double distance;

  public LineStringLocation(final LineString line, final int segmentIndex,
    final double segmentFraction, final double distance) {
    this.line = line;
    final int vertexCount = line.getVertexCount();
    if (segmentIndex < 0) {
      this.segmentIndex = 0;
      this.segmentFraction = 0.0;
    } else if (segmentIndex >= vertexCount) {
      this.segmentIndex = vertexCount - 1;
      this.segmentFraction = 1.0;
    } else {
      this.segmentIndex = segmentIndex;
      if (segmentFraction < 0.0) {
        this.segmentFraction = 0.0;
      } else if (segmentFraction > 1.0) {
        this.segmentFraction = 1.0;
      } else {
        this.segmentFraction = segmentFraction;
      }
    }
    this.distance = distance;
  }

  /**
   * Compares this object with the specified object for order.
   *
   * @param o the <code>LineStringLocation</code> with which this
   *          <code>Coordinate</code> is being compared
   * @return a negative integer, zero, or a positive integer as this
   *         <code>LineStringLocation</code> is less than, equal to, or greater
   *         than the specified <code>LineStringLocation</code>
   */
  @Override
  public int compareTo(final LineStringLocation other) {
    if (this.segmentIndex < other.segmentIndex) {
      return -1;
    } else if (this.segmentIndex > other.segmentIndex) {
      return 1;
    } else if (this.segmentFraction < other.segmentFraction) {
      return -1;
    } else if (this.segmentFraction > other.segmentFraction) {
      return 1;
    } else {
      return 0;
    }
  }

  public double getDistance() {
    return this.distance;
  }

  public LineString getLine() {
    return this.line;
  }

  public Point getPoint() {
    final double x1 = this.line.getX(this.segmentIndex);
    final double y1 = this.line.getY(this.segmentIndex);
    if (this.segmentFraction == 0) {
      return this.line.getPoint(this.segmentIndex);
    } else {
      final double x2 = this.line.getX(this.segmentIndex + 1);
      final double y2 = this.line.getY(this.segmentIndex + 1);
      if (this.segmentFraction == 0) {
        return this.line.getPoint(this.segmentIndex + 1);
      } else {
        final double x = (x2 - x1) * this.segmentFraction + x1;
        final double y = (y2 - y1) * this.segmentFraction + y1;
        return new PointDoubleXY(this.line.getGeometryFactory(), x, y);
      }
    }
  }

  public Point getPoint2d() {
    final double x1 = this.line.getX(this.segmentIndex);
    final double y1 = this.line.getY(this.segmentIndex);
    if (this.segmentFraction == 0) {
      return new PointDoubleXY(x1, y1);
    } else {
      final double x2 = this.line.getX(this.segmentIndex + 1);
      final double y2 = this.line.getY(this.segmentIndex + 1);
      if (this.segmentFraction == 0) {
        return new PointDoubleXY(x2, y2);
      } else {
        final double x = (x2 - x1) * this.segmentFraction + x1;
        final double y = (y2 - y1) * this.segmentFraction + y1;
        return new PointDoubleXY(this.line.getGeometryFactory(), x, y);
      }
    }
  }

  public double getSegmentFraction() {
    return this.segmentFraction;
  }

  public int getSegmentIndex() {
    return this.segmentIndex;
  }

  public int getVertexIndex() {
    if (this.segmentFraction == 1) {
      return this.segmentIndex + 1;
    } else {
      return this.segmentIndex;
    }
  }

  public boolean isFromVertex() {
    return this.segmentIndex == 0 && this.segmentFraction == 0.0;
  }

  public boolean isToVertex() {
    final int lastSegmentIndex = this.line.getVertexCount() - 2;
    return this.segmentIndex == lastSegmentIndex && this.segmentFraction == 1.0;
  }

  public boolean isVertex() {
    return this.segmentFraction == 0.0 || this.segmentFraction == 1.0;
  }

  @Override
  public String toString() {
    return getPoint() + " i=" + this.segmentIndex + " %=" + this.segmentFraction * 100 + " d="
      + this.distance;
  }
}
