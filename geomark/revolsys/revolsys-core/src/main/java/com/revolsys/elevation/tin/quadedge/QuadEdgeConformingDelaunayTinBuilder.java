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
package com.revolsys.elevation.tin.quadedge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.PointList;
import com.revolsys.geometry.model.Polygonal;

/**
 * A utility class which creates Conforming Delaunay Trianglulations
 * from collections of points and linear constraints, and extract the resulting
 * triangulation edges or triangles as geometries.
 *
 * @author Martin Davis
 *
 */
public class QuadEdgeConformingDelaunayTinBuilder {
  /**
   * Extracts the unique {@link Coordinates}s from the given {@link Geometry}.
   * @param geom the geometry to extract from
   * @return a List of the unique Coordinates
   */
  public static PointList extractUniqueCoordinates(final Geometry geom) {
    if (geom == null) {
      return new PointList();
    }

    return unique(geom.vertices(), geom.getVertexCount());
  }

  private static List<LineSegmentDoubleData> newConstraintSegments(final Geometry geom) {
    final List<LineString> lines = geom.getGeometryComponents(LineString.class);
    final List<LineSegmentDoubleData> constraintSegs = new ArrayList<>();
    for (final LineString line : lines) {
      newConstraintSegments(line, constraintSegs);
    }
    return constraintSegs;
  }

  private static void newConstraintSegments(final LineString line,
    final List<LineSegmentDoubleData> constraintSegs) {
    final int vertexCount = line.getVertexCount();
    if (vertexCount > 0) {
      double x1 = line.getX(0);
      double y1 = line.getY(0);
      double z1 = line.getZ(0);
      for (int vertexIndex = 1; vertexIndex < vertexCount; vertexIndex++) {
        final double x2 = line.getX(vertexIndex);
        final double y2 = line.getY(vertexIndex);
        final double z2 = line.getZ(vertexIndex);
        constraintSegs.add(new LineSegmentDoubleData(x1, y1, z1, x2, y2, z2));

        x1 = x2;
        y1 = y2;
        z1 = z2;
      }
    }
  }

  public static PointList unique(final Iterable<? extends Point> points, final int vertexCount) {
    final Point[] pointArray = new Point[vertexCount];
    int vertexIndex = 0;
    for (final Point point : points) {
      pointArray[vertexIndex++] = point.newPoint();
    }
    Arrays.sort(pointArray);
    final PointList coordList = new PointList(pointArray, false);
    return coordList;
  }

  private Geometry constraintLines;

  private final Map<ConstraintVertex, ConstraintVertex> constraintVertexMap = new TreeMap<>();

  private List<Point> sitePoints;

  private QuadEdgeSubdivision subdivision = null;

  private double tolerance = 0.0;

  public QuadEdgeConformingDelaunayTinBuilder() {
  }

  /**
   * Gets the edges of the computed triangulation as a {@link Lineal}.
   *
   * @param geomFact the geometry factory to use to create the output
   * @return the edges of the triangulation
   */
  public Lineal getEdgesLineal(final GeometryFactory geomFact) {
    init();
    return this.subdivision.getEdgesLineal(geomFact);
  }

  /**
   * Gets the QuadEdgeSubdivision which models the computed triangulation.
   *
   * @return the subdivision containing the triangulation
   */
  public QuadEdgeSubdivision getSubdivision() {
    init();
    return this.subdivision;
  }

  /**
   * Gets the faces of the computed triangulation as a {@link Polygonal}.
   *
   * @param geomFact the geometry factory to use to create the output
   * @return the faces of the triangulation
   */
  public Polygonal getTrianglesPolygonal(final GeometryFactory geomFact) {
    init();
    return this.subdivision.getTrianglesPolygonal(geomFact);
  }

  private void init() {
    if (this.subdivision != null) {
      return;
    }

    List<LineSegmentDoubleData> segments = new ArrayList<>();
    if (this.constraintLines != null) {

      initVertices(this.constraintLines);
      segments = newConstraintSegments(this.constraintLines);
    }
    final List<ConstraintVertex> sites = newSiteVertices(this.sitePoints);

    final ConformingDelaunayTriangulator cdt = new ConformingDelaunayTriangulator(sites,
      GeometryFactory.fixed3d(0, this.tolerance, this.tolerance, 0));

    cdt.setConstraints(segments, new ArrayList<>(this.constraintVertexMap.values()));

    cdt.buildTin();
    cdt.enforceConstraints();
    this.subdivision = cdt.getSubdivision();
  }

  private void initVertices(final Geometry geom) {
    for (final Point point : geom.vertices()) {
      final ConstraintVertex vertex = new ConstraintVertex(point);
      this.constraintVertexMap.put(vertex, vertex);
    }
  }

  private List<ConstraintVertex> newSiteVertices(final Collection<Point> coords) {
    final List<ConstraintVertex> verts = new ArrayList<>();
    for (final Point coord : coords) {
      if (this.constraintVertexMap.containsKey(coord)) {
        continue;
      }
      verts.add(new ConstraintVertex(coord));
    }
    return verts;
  }

  /**
   * Sets the linear constraints to be conformed to.
   * All linear components in the input will be used as constraints.
   * The constraint vertices do not have to be disjoint from
   * the site vertices.
   * The constraints must not contain duplicate segments (up to orientation).
   *
   * @param constraintLines the lines to constraint to
   */
  public void setConstraints(final Geometry constraintLines) {
    this.constraintLines = constraintLines;
  }

  /**
   * Sets the sites (point or vertices) which will be triangulated.
   * All vertices of the given geometry will be used as sites.
   * The site vertices do not have to contain the constraint
   * vertices as well; any site vertices which are
   * identical to a constraint vertex will be removed
   * from the site vertex set.
   *
   * @param geom the geometry from which the sites will be extracted.
   */
  public void setSites(final Geometry geom) {
    this.sitePoints = extractUniqueCoordinates(geom);
  }

  /**
   * Sets the snapping tolerance which will be used
   * to improved the robustness of the triangulation computation.
   * A tolerance of 0.0 specifies that no snapping will take place.
   *
   * @param tolerance the tolerance distance to use
   */
  public void setTolerance(final double tolerance) {
    this.tolerance = tolerance;
  }

}
