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

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.PointDoubleXY;

/**
 * Computes a point in the interior of an areal geometry.
 *
 * <h2>Algorithm</h2>
 * <ul>
 *   <li>Find a Y value which is close to the centre of
 *       the geometry's vertical extent but is different
 *       to any of it's Y ordinates.
 *   <li>Construct a new horizontal bisector line using the Y value
 *       and the geometry's horizontal extent
 *   <li>Find the intersection between the geometry
 *       and the horizontal bisector line.
 *       The intersection is a collection of lines and points.
 *   <li>Pick the midpoint of the largest intersection geometry
 * </ul>
 *
 * <h3>KNOWN BUGS</h3>
 * <ul>
 * <li>If a fixed precision model is used,
 * in some cases this method may return a point
 * which does not lie in the interior.
 * </ul>
 *
 * @version 1.7
 */
public class InteriorPointArea {

  /**
   * Finds a safe bisector Y ordinate
   * by projecting to the Y axis
   * and finding the Y-ordinate interval
   * which contains the centre of the Y extent.
   * The centre of this interval is returned as the bisector Y-ordinate.
   *
   * @author mdavis
   *
   */
  private static class SafeBisectorFinder {
    public static double getBisectorY(final Polygon poly) {
      final SafeBisectorFinder finder = new SafeBisectorFinder(poly);
      return finder.getBisectorY();
    }

    private final double centreY;

    private double hiY = Double.MAX_VALUE;

    private double loY = -Double.MAX_VALUE;

    private final Polygon poly;

    public SafeBisectorFinder(final Polygon poly) {
      this.poly = poly;

      // initialize using extremal values
      this.hiY = poly.getBoundingBox().getMaxY();
      this.loY = poly.getBoundingBox().getMinY();
      this.centreY = avg(this.loY, this.hiY);
    }

    public double getBisectorY() {
      process(this.poly.getShell());
      for (int i = 0; i < this.poly.getHoleCount(); i++) {
        process(this.poly.getHole(i));
      }
      final double bisectY = avg(this.hiY, this.loY);
      return bisectY;
    }

    private void process(final LineString line) {
      final LineString seq = line;
      for (int i = 0; i < seq.getVertexCount(); i++) {
        final double y = seq.getY(i);
        updateInterval(y);
      }
    }

    private void updateInterval(final double y) {
      if (y <= this.centreY) {
        if (y > this.loY) {
          this.loY = y;
        }
      } else if (y > this.centreY) {
        if (y < this.hiY) {
          this.hiY = y;
        }
      }
    }
  }

  private static double avg(final double a, final double b) {
    return (a + b) / 2.0;
  }

  /**
   * Returns the centre point of the envelope.
   * @param envelope the envelope to analyze
   * @return the centre of the envelope
   */
  public static Point centre(final BoundingBox envelope) {
    return new PointDoubleXY(avg(envelope.getMinX(), envelope.getMaxX()),
      avg(envelope.getMinY(), envelope.getMaxY()));
  }

  private final GeometryFactory factory;

  private Point interiorPoint = null;

  private double maxWidth = 0.0;

  /**
   * Creates a new interior point finder
   * for an areal geometry.
   *
   * @param g an areal geometry
   */
  public InteriorPointArea(final Geometry g) {
    this.factory = g.getGeometryFactory();
    add(g);
  }

  /**
   * Tests the interior vertices (if any)
   * defined by an areal Geometry for the best inside point.
   * If a component Geometry is not of dimension 2 it is not tested.
   *
   * @param geometry the geometry to add
   */
  private void add(final Geometry geometry) {
    if (geometry instanceof Polygon) {
      addPolygon(geometry);
    } else if (geometry.isGeometryCollection()) {
      geometry.forEachGeometry(this::add);
    }
  }

  /**
   * Finds an interior point of a Polygon.
   * @param geometry the geometry to analyze
   */
  private void addPolygon(final Geometry geometry) {
    if (geometry.isEmpty()) {
      return;
    }

    Point intPt;
    double width = 0;

    final LineString bisector = horizontalBisector(geometry);
    if (bisector.getLength() == 0.0) {
      width = 0;
      intPt = bisector.getPoint();
    } else {
      final Geometry intersections = bisector.intersection(geometry);
      final Geometry widestIntersection = widestGeometry(intersections);
      width = widestIntersection.getBoundingBox().getWidth();
      intPt = centre(widestIntersection.getBoundingBox());
    }
    if (this.interiorPoint == null || width > this.maxWidth) {
      this.interiorPoint = intPt;
      this.maxWidth = width;
    }
  }

  /**
   * Gets the computed interior point.
   *
   * @return the coordinate of an interior point
   */
  public Point getInteriorPoint() {
    return this.interiorPoint;
  }

  protected LineString horizontalBisector(final Geometry geometry) {
    final BoundingBox envelope = geometry.getBoundingBox();

    /**
     * Original algorithm.  Fails when geometry contains a horizontal
     * segment at the Y midpoint.
     */
    // Assert: for areas, minx <> maxx
    // double avgY = avg(envelope.getMinY(), envelope.getMaxY());

    final double bisectY = SafeBisectorFinder.getBisectorY((Polygon)geometry);
    return this.factory.lineString(2, envelope.getMinX(), bisectY, envelope.getMaxX(), bisectY);
  }

  // @return if geometry is a collection, the widest sub-geometry; otherwise,
  // the geometry itself
  private Geometry widestGeometry(final Geometry geometry) {
    if (geometry.isGeometryCollection()) {
      if (geometry.isEmpty()) {
        return geometry;
      } else {
        double widestWidth = 0;
        Geometry widestGeometry = null;
        // scan remaining geom components to see if any are wider
        for (final Geometry part : geometry.geometries()) {
          final double width = part.getBoundingBox().getWidth();
          if (widestGeometry == null || width > widestWidth) {
            widestGeometry = part;
            widestWidth = width;
          }
        }
        return widestGeometry;
      }
    } else {
      return geometry;
    }
  }
}
