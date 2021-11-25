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
package com.revolsys.geometry.precision;

import com.revolsys.geometry.index.strtree.ItemDistance;
import com.revolsys.geometry.index.strtree.StrTree;
import com.revolsys.geometry.index.strtree.StrTreeLeaf;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.operation.distance.FacetSequence;
import com.revolsys.geometry.operation.distance.FacetSequenceTreeBuilder;
import com.revolsys.util.Pair;

/**
 * Computes the Minimum Clearance of a {@link Geometry}.
 * <p>
 * The <b>Minimum Clearance</b> is a measure of
 * what magnitude of perturbation of
 * the vertices of a geometry can be tolerated
 * before the geometry becomes topologically invalid.
 * The smaller the Minimum Clearance distance,
 * the less vertex pertubation the geometry can tolerate
 * before becoming invalid.
 * <p>
 * The concept was introduced by Thompson and Van Oosterom
 * [TV06], based on earlier work by Milenkovic [Mi88].
 * <p>
 * The Minimum Clearance of a geometry G
 * is defined to be the value <i>r</i>
 * such that "the movement of all points by a distance
 * of <i>r</i> in any direction will
 * guarantee to leave the geometry valid" [TV06].
 * An equivalent constructive definition [Mi88] is that
 * <i>r</i> is the largest value such:
 * <ol>
 * <li>No two distinct vertices of G are closer than <i>r</i>
 * <li>No vertex of G is closer than <i>r</i> to an edge of G
 * of which the vertex is not an endpoint
 * </ol>
 * The following image shows an example of the Minimum Clearance
 * of a simple polygon.
 * <p>
 * <center><img src='doc-files/minClearance.png'></center>
 * <p>
 * If G has only a single vertex (i.e. is a
 * {@link Point}), the value of the minimum clearance
 * is {@link Double#MAX_VALUE}.
 * <p>
 * If G is a {@link Punctual} or {@link Lineal} geometry,
 * then in fact no amount of perturbation
 * will render the geometry invalid.
 * In this case a Minimum Clearance is still computed
 * based on the vertex and segment distances
 * according to the constructive definition.
 * <p>
 * It is possible for no Minimum Clearance to exist.
 * For instance, a {@link Punctual} with all members identical
 * has no Minimum Clearance
 * (i.e. no amount of perturbation will cause
 * the member points to become non-identical).
 * Empty geometries also have no such distance.
 * The lack of a meaningful MinimumClearance distance is detected
 * and suitable values are returned by
 * {@link #getDistance()} and {@link #getLine()}.
 * <p>
 * The computation of Minimum Clearance utilizes
 * the {@link StrTree#nearestNeighbour(ItemDistance)}
 * method to provide good performance even for
 * large inputs.
 * <p>
 * An interesting note is that for the case of multi part {@link Punctual}s,
 * the computed Minimum Clearance line
 * effectively determines the Nearest Neighbours in the collection.
 *
 * <h3>References</h3>
 * <ul>
 * <li>[Mi88] Milenkovic, V. J.,
 * <i>Verifiable implementations of geometric algorithms
 * using finite precision arithmetic</i>.
 * in Artificial Intelligence, 377-401. 1988
 * <li>[TV06] Thompson, Rod and van Oosterom, Peter,
 * <i>Interchange of Spatial Data-Inhibiting Factors</i>,
 * Agile 2006, Visegrad, Hungary. 2006
 * </ul>
 *
 * @author Martin Davis
 *
 */
public class MinimumClearance {
  /**
   * Implements the MinimumClearance distance function:
   * <ul>
   * <li>dist(p1, p2) =
   * <ul>
   * <li>p1 != p2 : p1.distance(p2)
   * <li>p1 == p2 : Double.MAX
   * </ul>
   * <li>dist(p, seg) =
   * <ul>
   * <li>p != seq.p1 && p != seg.p2 : seg.distance(p)
   * <li>ELSE : Double.MAX
   * </ul>
   * </ul>
   * Also computes the values of the nearest points, if any.
   *
   * @author Martin Davis
   *
   */
  private static class MinClearanceDistance implements ItemDistance<FacetSequence> {
    private double minDist = Double.MAX_VALUE;

    private double minX1;

    private double minX2;

    private double minY1;

    private double minY2;

    private final boolean calculateLine;

    public MinClearanceDistance(final boolean calculateLine) {
      this.calculateLine = calculateLine;
    }

    public double distance(final FacetSequence fs1, final FacetSequence fs2) {
      if (!vertexDistance(fs1, fs2)) {
        if (!(fs1.getVertexCount() == 1 && fs2.getVertexCount() == 1)) {
          if (!segmentDistance(fs1, fs2)) {
            segmentDistance(fs2, fs1);
          }
        }
      }
      return this.minDist;
    }

    @Override
    public double distance(final StrTreeLeaf<FacetSequence> b1,
      final StrTreeLeaf<FacetSequence> b2) {
      final FacetSequence fs1 = b1.getItem();
      final FacetSequence fs2 = b2.getItem();
      this.minDist = Double.MAX_VALUE;
      return distance(fs1, fs2);
    }

    public double getMinX1() {
      return this.minX1;
    }

    public double getMinX2() {
      return this.minX2;
    }

    public double getMinY1() {
      return this.minY1;
    }

    public double getMinY2() {
      return this.minY2;
    }

    private boolean segmentDistance(final FacetSequence fs1, final FacetSequence fs2) {
      final int vertexCount1 = fs1.getVertexCount();
      final int vertexCount2 = fs2.getVertexCount();
      for (int i1 = 0; i1 < vertexCount1; i1++) {
        final double x = fs1.getCoordinate(i1, 0);
        final double y = fs1.getCoordinate(i1, 1);

        double x1 = fs2.getCoordinate(0, 0);
        double y1 = fs2.getCoordinate(0, 1);
        for (int i2 = 1; i2 < vertexCount2; i2++) {
          final double x2 = fs2.getCoordinate(i2, 0);
          final double y2 = fs2.getCoordinate(i2, 1);
          if (!(x1 == x && y1 == y) && //
            !(x2 == x && y2 == y)) {
            final double distance = LineSegmentUtil.distanceLinePoint(x1, y1, x2, y2, x, y);
            if (distance < this.minDist) {
              this.minDist = distance;
              if (this.calculateLine) {
                this.minX1 = x;
                this.minY1 = y;
                final Point closestPoint = LineSegmentUtil.closestPoint(x1, y1, x2, y2, x, y);
                this.minX2 = closestPoint.getX();
                this.minY2 = closestPoint.getY();
              }
              if (distance == 0.0) {
                return true;
              }
            }
          }
          x1 = x2;
          y1 = y2;
        }
      }
      return this.minDist <= 0.0;
    }

    private boolean vertexDistance(final FacetSequence fs1, final FacetSequence fs2) {
      final int vertexCount1 = fs1.getVertexCount();
      final int vertexCount2 = fs2.getVertexCount();
      for (int i1 = 0; i1 < vertexCount1; i1++) {
        final double x1 = fs1.getCoordinate(i1, 0);
        final double y1 = fs1.getCoordinate(i1, 1);
        for (int i2 = 0; i2 < vertexCount2; i2++) {
          final double x2 = fs2.getCoordinate(i2, 0);
          final double y2 = fs2.getCoordinate(i2, 1);
          if (!(x1 == x2 && y1 == y2)) {
            final double dx = x2 - x1;
            final double dy = y2 - y1;

            final double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance < this.minDist) {
              this.minDist = distance;
              if (this.calculateLine) {
                this.minX1 = x1;
                this.minY1 = y1;
                this.minX2 = x2;
                this.minY2 = y2;
              }
              if (distance == 0.0) {
                return true;
              }
            }
          }
        }
      }
      return this.minDist <= 0.0;
    }

  }

  /**
   * Computes the Minimum Clearance distance for
   * the given Geometry.
   *
   * @param geometry the input geometry
   * @return the Minimum Clearance distance
   */
  public static double getDistance(final Geometry geometry) {
    final MinimumClearance rp = new MinimumClearance(geometry, false);
    return rp.getDistance();
  }

  /**
   * Gets a LineString containing two points
   * which are at the Minimum Clearance distance
   * for the given Geometry.
   *
   * @param geometry the input geometry
   * @return the value of the minimum clearance distance
   * or <tt>LINESTRING EMPTY</tt> if no Minimum Clearance distance exists
   */
  public static Geometry getLine(final Geometry geometry) {
    final MinimumClearance rp = new MinimumClearance(geometry, true);
    return rp.getLine();
  }

  private final Geometry geometry;

  private double minClearance;

  private Point[] minClearancePts;

  private boolean calculateLine;

  private boolean calculated = false;

  /**
   * Creates an object to compute the Minimum Clearance
   * for the given Geometry
   *
   * @param geom the input geometry
   */
  public MinimumClearance(final Geometry geom) {
    this.geometry = geom;
  }

  public MinimumClearance(final Geometry geometry, final boolean calculateLine) {
    this.geometry = geometry;
    this.calculateLine = calculateLine;
  }

  private void compute() {
    if (!this.calculated) {
      this.calculated = true;
      this.minClearance = Double.MAX_VALUE;

      if (!this.geometry.isEmpty()) {
        final StrTree<FacetSequence> geomTree = FacetSequenceTreeBuilder.build(this.geometry);

        final Pair<FacetSequence, FacetSequence> nearest = geomTree
          .nearestNeighbour(new MinClearanceDistance(this.calculateLine));

        final MinClearanceDistance mcd = new MinClearanceDistance(this.calculateLine);
        this.minClearance = mcd.distance(nearest.getValue1(), nearest.getValue2());

        final GeometryFactory geometryFactory = this.geometry.getGeometryFactory();
        if (this.calculateLine) {
          this.minClearancePts = new Point[] { //
            geometryFactory.point(mcd.getMinX1(), mcd.getMinY1()),
            geometryFactory.point(mcd.getMinX2(), mcd.getMinY2())
          };
        }
      }
    }
  }

  /**
   * Gets the Minimum Clearance distance.
   * <p>
   * If no distance exists
   * (e.g. in the case of two identical points)
   * <tt>Double.MAX_VALUE</tt> is returned.
   *
   * @return the value of the minimum clearance distance
   * or <tt>Double.MAX_VALUE</tt> if no Minimum Clearance distance exists
   */
  public double getDistance() {
    compute();
    return this.minClearance;
  }

  /**
   * Gets a LineString containing two points
   * which are at the Minimum Clearance distance.
   * <p>
   * If no distance could be found
   * (e.g. in the case of two identical points)
   * <tt>LINESTRING EMPTY</tt> is returned.
   *
   * @return the value of the minimum clearance distance
   * or <tt>LINESTRING EMPTY</tt> if no Minimum Clearance distance exists
   */
  public LineString getLine() {
    compute();
    // return empty line string if no min pts where found
    final GeometryFactory geometryFactory = this.geometry.getGeometryFactory();
    if (this.minClearance == Double.MAX_VALUE) {
      return geometryFactory.lineString();
    } else {
      return geometryFactory.lineString(this.minClearancePts);
    }
  }

}
