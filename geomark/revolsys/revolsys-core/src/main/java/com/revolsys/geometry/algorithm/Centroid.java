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
package com.revolsys.geometry.algorithm;

import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.util.Points;
import com.revolsys.geometry.util.Triangles;

/**
 * Computes the centroid of a {@link Geometry} of any dimension.
 * If the geometry is nominally of higher dimension,
 * but has lower <i>effective</i> dimension
 * (i.e. contains only components
 * having zero length or area),
 * the centroid will be computed as for the equivalent lower-dimension geometry.
 * If the input geometry is empty, a
 * <code>null</code> Point is returned.
 *
 * <h2>Algorithm</h2>
 * <ul>
 * <li><b>Dimension 2</b> - the centroid is computed
 * as the weighted sum of the centroids
 * of a decomposition of the area into (possibly overlapping) triangles.
 * Holes and multipolygons are handled correctly.
 * See <code>http://www.faqs.org/faqs/graphics/algorithms-faq/</code>
 * for further details of the basic approach.
 *
 * <li><b>Dimension 1</b> - Computes the average of the midpoints
 * of all line segments weighted by the segment length.
 * Zero-length lines are treated as points.
 *
 * <li><b>Dimension 0</b> - Compute the average coordinate for all points.
 * Repeated points are all included in the average.
 * </ul>
 *
 * @version 1.7
 */
public class Centroid {

  /**
   * Computes the centroid point of a geometry.
   *
   * @param geom the geometry to use
   * @return the centroid point, or null if the geometry is empty
   */
  public static Point getCentroid(final Geometry geom) {
    final Centroid cent = new Centroid(geom);
    return cent.getCentroid();
  }

  public static Point getCentroid(final List<? extends Point> points) {
    return new Centroid(points).getCentroid();
  }

  private double areaBasePtX = Double.NaN;

  private double areaBasePtY = Double.NaN;

  /* Partial area sum */
  private double areaSum2 = 0;

  private double cg3X;

  private double cg3Y;

  private final GeometryFactory geometryFactory;

  // data for linear centroid computation, if needed
  private double lineCenterX = 0;

  private double lineCenterY = 0;

  private double lineTotalLength = 0.0;

  private int pointCount = 0;

  private double pointSumX = 0;

  private double pointSumY = 0;

  /**
   * Creates a new instance for computing the centroid of a geometry
   */
  public Centroid(final Geometry geometry) {
    this.geometryFactory = geometry.getGeometryFactory();
    add(geometry);
  }

  public Centroid(final List<? extends Point> points) {
    if (points.isEmpty()) {
      this.geometryFactory = GeometryFactory.DEFAULT_2D;
    } else {
      this.geometryFactory = points.get(0).getGeometryFactory();
      for (final Point point : points) {
        addPoint(point);
      }
    }
  }

  /**
   * Adds a Geometry to the centroid total.
   *
   * @param geometry the geometry to add
   */
  private void add(final Geometry geometry) {
    if (geometry.isEmpty()) {
      return;
    } else if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      addPoint(point);
    } else if (geometry instanceof LineString) {
      addLine((LineString)geometry);
    } else if (geometry instanceof Polygon) {
      final Polygon poly = (Polygon)geometry;
      add(poly);
    } else if (geometry.isGeometryCollection()) {
      geometry.forEachGeometry(this::add);
    }
  }

  private void add(final Polygon poly) {
    addShell(poly.getShell());
    for (int i = 0; i < poly.getHoleCount(); i++) {
      addHole(poly.getHole(i));
    }
  }

  private void addHole(final LineString line) {
    final int vertexCount = line.getVertexCount();
    if (vertexCount > 0) {
      final double x = this.areaBasePtX;
      final double y = this.areaBasePtY;
      final boolean isPositiveArea = line.isCounterClockwise();

      double lineLength = 0.0;
      final double x0 = line.getX(0);
      final double y0 = line.getY(0);
      double x1 = x0;
      double y1 = y0;
      for (int vertexIndex = 1; vertexIndex < vertexCount; vertexIndex++) {
        final double x2 = line.getX(vertexIndex);
        final double y2 = line.getY(vertexIndex);
        addTriangle(x, y, x1, y1, x2, y2, isPositiveArea);
        final double segmentLength = Points.distance(x1, y1, x2, y2);
        if (segmentLength > 0.0) {
          lineLength += segmentLength;

          final double midx = (x1 + x2) / 2;
          final double midy = (y1 + y2) / 2;
          this.lineCenterX += segmentLength * midx;
          this.lineCenterY += segmentLength * midy;
        }
        x1 = x2;
        y1 = y2;
      }
      this.lineTotalLength += lineLength;
      if (lineLength == 0.0) {
        addPoint(x1, y1);
      }
    }
  }

  /**
   * Adds the line segments defined by an array of coordinates
   * to the linear centroid accumulators.
   *
   * @param pts an array of {@link Coordinates}s
   */
  private void addLine(final LineString line) {
    final int vertexCount = line.getVertexCount();
    double lineLength = 0.0;
    if (vertexCount > 0) {
      final double x0 = line.getX(0);
      double x1 = x0;
      final double y0 = line.getY(0);
      double y1 = y0;
      for (int vertexIndex = 1; vertexIndex < vertexCount; vertexIndex++) {
        final double x2 = line.getX(vertexIndex);
        final double y2 = line.getY(vertexIndex);
        final double segmentLength = Points.distance(x1, y1, x2, y2);
        if (segmentLength > 0.0) {
          lineLength += segmentLength;

          final double midx = (x1 + x2) / 2;
          final double midy = (y1 + y2) / 2;
          this.lineCenterX += segmentLength * midx;
          this.lineCenterY += segmentLength * midy;
        }
        x1 = x2;
        y1 = y2;
      }
      this.lineTotalLength += lineLength;
      if (lineLength == 0.0) {
        addPoint(x1, y1);
      }
    }
  }

  /**
   * Adds a point to the point centroid accumulator.
   * @param pt a {@link Coordinates}
   */
  private void addPoint(final double x, final double y) {
    this.pointCount += 1;
    this.pointSumX += x;
    this.pointSumY += y;
  }

  private void addPoint(final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    addPoint(x, y);
  }

  private void addShell(final LineString line) {
    final int vertexCount = line.getVertexCount();
    if (vertexCount > 0) {
      double lineLength = 0.0;
      double x1 = line.getX(0);
      double y1 = line.getY(0);
      if (Double.isNaN(this.areaBasePtX)) {
        this.areaBasePtX = x1;
        this.areaBasePtY = y1;
      }
      final double x = this.areaBasePtX;
      final double y = this.areaBasePtY;

      final boolean isPositiveArea = line.isClockwise();
      for (int vertexIndex = 1; vertexIndex < vertexCount; vertexIndex++) {
        final double x2 = line.getX(vertexIndex);
        final double y2 = line.getY(vertexIndex);
        addTriangle(x, y, x1, y1, x2, y2, isPositiveArea);
        final double segmentLength = Points.distance(x1, y1, x2, y2);
        if (segmentLength > 0.0) {
          lineLength += segmentLength;

          final double midx = (x1 + x2) / 2;
          final double midy = (y1 + y2) / 2;
          this.lineCenterX += segmentLength * midx;
          this.lineCenterY += segmentLength * midy;
        }
        x1 = x2;
        y1 = y2;
      }
      this.lineTotalLength += lineLength;
      if (lineLength == 0.0) {
        addPoint(x1, y1);
      }
    }
  }

  private void addTriangle(final double x1, final double y1, final double x2, final double y2,
    final double x3, final double y3, final boolean isPositiveArea) {
    final double sign = isPositiveArea ? 1.0 : -1.0;
    final double triangleCent3X = x1 + x2 + x3;
    final double triangleCent3Y = y1 + y2 + y3;

    final double area2 = Triangles.area2(x1, y1, x2, y2, x3, y3);
    this.cg3X += sign * area2 * triangleCent3X;
    this.cg3Y += sign * area2 * triangleCent3Y;
    this.areaSum2 += sign * area2;
  }

  /**
   * Gets the computed centroid.
   *
   * @return the computed centroid, or null if the input is empty
   */
  public Point getCentroid() {
    /**
     * The centroid is computed from the highest dimension components present in the input.
     * I.e. areas dominate lineal geometry, which dominates points.
     * Degenerate geometry are computed using their effective dimension
     * (e.g. areas may degenerate to lines or points)
     */
    if (Math.abs(this.areaSum2) > 0.0) {
      /**
       * Input contains areal geometry
       */
      final double x = this.cg3X / 3 / this.areaSum2;
      final double y = this.cg3Y / 3 / this.areaSum2;
      return this.geometryFactory.point(x, y);
    } else if (this.lineTotalLength > 0.0) {
      /**
       * Input contains lineal geometry
       */
      final double x = this.lineCenterX / this.lineTotalLength;
      final double y = this.lineCenterY / this.lineTotalLength;
      return this.geometryFactory.point(x, y);
    } else if (this.pointCount > 0) {
      /**
       * Input contains puntal geometry only
       */
      final double x = this.pointSumX / this.pointCount;
      final double y = this.pointSumY / this.pointCount;
      return this.geometryFactory.point(x, y);
    } else {
      return null;
    }
  }

}
