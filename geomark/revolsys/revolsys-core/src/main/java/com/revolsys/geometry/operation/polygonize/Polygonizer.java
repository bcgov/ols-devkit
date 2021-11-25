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
package com.revolsys.geometry.operation.polygonize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;

/**
 * Polygonizes a set of {@link Geometry}s which contain linework that
 * represents the edges of a planar graph.
 * All types of Geometry are accepted as input;
 * the constituent linework is extracted as the edges to be polygonized.
 * The processed edges must be correctly noded; that is, they must only meet
 * at their endpoints.  The Polygonizer will run on incorrectly noded input
 * but will not form polygons from non-noded edges,
 * and will report them as errors.
 * <p>
 * The Polygonizer reports the follow kinds of errors:
 * <ul>
 * <li><b>Dangles</b> - edges which have one or both ends which are not incident on another edge endpoint
 * <li><b>Cut Edges</b> - edges which are connected at both ends but which do not form part of polygon
 * <li><b>Invalid Ring Lines</b> - edges which form rings which are invalid
 * (e.g. the component lines contain a self-intersection)
 * </ul>
 *
 * @version 1.7
 */
public class Polygonizer {
  private static void assignHolesToShells(final List<EdgeRing> holeList,
    final List<EdgeRing> shellList) {
    for (final EdgeRing holeER : holeList) {
      assignHoleToShell(holeER, shellList);
    }
  }

  private static void assignHoleToShell(final EdgeRing holeER, final List<EdgeRing> shellList) {
    final EdgeRing shell = EdgeRing.findEdgeRingContaining(holeER, shellList);
    if (shell != null) {
      shell.addHole(holeER.getRing());
    }
  }

  protected List<LineString> cutEdges = new ArrayList<>();

  // initialize with empty collections, in case nothing is computed
  protected Collection<LineString> dangles = new ArrayList<>();

  protected PolygonizeGraph graph;

  protected List<EdgeRing> holeList = null;

  protected List<LineString> invalidRingLines = new ArrayList<>();

  private boolean isCheckingRingsValid = true;

  protected List<Polygon> polygons = null;

  protected List<EdgeRing> shellList = null;

  /**
   * Construct a new polygonizer with the same {@link GeometryFactory}
   * as the input {@link Geometry}s
   */
  public Polygonizer() {
  }

  /**
   * Add a {@link Geometry} to the edges to be polygonized.
   * May be called multiple times.
   * Any dimension of Geometry may be added;
   * the constituent linework will be extracted and used
   *
   * @param geometry a {@link Geometry} with linework to be polygonized
   */
  public void add(final Geometry geometry) {
    for (final LineString line : geometry.getGeometryComponents(LineString.class)) {
      add(line);
    }
  }

  /**
   * Adds a collection of geometries to the edges to be polygonized.
   * May be called multiple times.
   * Any dimension of Geometry may be added;
   * the constituent linework will be extracted and used.
   *
   * @param geometries a list of {@link Geometry}s with linework to be polygonized
   */
  public void add(final Iterable<? extends Geometry> geometries) {
    for (final Geometry geometry : geometries) {
      add(geometry);
    }
  }

  /**
   * Adds a linestring to the graph of polygon edges.
   *
   * @param line the {@link LineString} to add
   */
  private void add(final LineString line) {
    // Construct a new new graph using the factory from the input Geometry
    if (this.graph == null) {
      final GeometryFactory geometryFactory = line.getGeometryFactory();
      this.graph = new PolygonizeGraph(geometryFactory);
    }
    this.graph.addEdge(line);
  }

  public void addLineString(LineString lineString) {
    if (lineString instanceof LinearRing) {
      lineString = ((LinearRing)lineString).newLineStringEmpty();
    }

    // unioning the linestring with the point makes any self intersections
    // explicit.
    final Geometry geometry = lineString.newValidGeometry();

    // Add result to polygonizer
    add(geometry);
  }

  public void addPolygon(final Polygon polygon) {
    for (final LinearRing ring : polygon.rings()) {
      addLineString(ring);
    }
  }

  private void findShellsAndHoles(final List<EdgeRing> edgeRingList) {
    this.holeList = new ArrayList<>();
    this.shellList = new ArrayList<>();
    for (final EdgeRing edgeRing : edgeRingList) {
      if (edgeRing.isHole()) {
        this.holeList.add(edgeRing);
      } else {
        this.shellList.add(edgeRing);
      }

    }
  }

  private void findValidRings(final List<EdgeRing> edgeRingList,
    final List<EdgeRing> validEdgeRingList, final List<LineString> invalidRingList) {
    for (final EdgeRing edgeRing : edgeRingList) {
      if (edgeRing.isValid()) {
        validEdgeRingList.add(edgeRing);
      } else {
        invalidRingList.add(edgeRing.getLineString());
      }
    }
  }

  /**
   * Gets the list of cut edges found during polygonization.
   * @return a collection of the input {@link LineString}s which are cut edges
   */
  public Collection<LineString> getCutEdges() {
    polygonize();
    return this.cutEdges;
  }

  /**
   * Gets the list of dangling lines found during polygonization.
   * @return a collection of the input {@link LineString}s which are dangles
   */
  public Collection<LineString> getDangles() {
    polygonize();
    return this.dangles;
  }

  public GeometryFactory getGeometryFactory() {
    if (this.graph == null) {
      return GeometryFactory.DEFAULT_3D;
    } else {
      return this.graph.getGeometryFactory();
    }
  }

  /**
   * Gets the list of lines forming invalid rings found during polygonization.
   * @return a collection of the input {@link LineString}s which form invalid rings
   */
  public Collection<LineString> getInvalidRingLines() {
    polygonize();
    return this.invalidRingLines;
  }

  public Polygonal getPolygonal() {
    final List<Polygon> polygons = getPolygons();
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (polygons.isEmpty()) {
      return geometryFactory.polygon();
    } else {
      return geometryFactory.polygonal(polygons);
    }
  }

  /**
   * Gets the list of polygons formed by the polygonization.
   * @return a collection of {@link Polygon}s
   */
  public List<Polygon> getPolygons() {
    polygonize();
    return this.polygons;
  }

  /**
   * Performs the polygonization, if it has not already been carried out.
   */
  private void polygonize() {
    // check if already computed
    if (this.polygons != null) {
      return;
    }
    this.polygons = new ArrayList<>();

    // if no geometries were supplied it's possible that graph is null
    if (this.graph == null) {
      return;
    }

    this.dangles = this.graph.deleteDangles();
    this.cutEdges = this.graph.deleteCutEdges();
    final List<EdgeRing> edgeRingList = this.graph.getEdgeRings();

    // Debug.printTime("Build Edge Rings");

    List<EdgeRing> validEdgeRingList = new ArrayList<>();
    this.invalidRingLines = new ArrayList<>();
    if (this.isCheckingRingsValid) {
      findValidRings(edgeRingList, validEdgeRingList, this.invalidRingLines);
    } else {
      validEdgeRingList = edgeRingList;
    }
    // Debug.printTime("Validate Rings");

    findShellsAndHoles(validEdgeRingList);
    assignHolesToShells(this.holeList, this.shellList);

    // Debug.printTime("Assign Holes");

    this.polygons = new ArrayList<>();
    for (final EdgeRing edgeRing : this.shellList) {
      final Polygon polygon = edgeRing.getPolygon();
      this.polygons.add(polygon);
    }
  }

  /**
   * Allows disabling the valid ring checking,
   * to optimize situations where invalid rings are not expected.
   * <p>
   * The default is <code>true</code.
   *
   * @param isCheckingRingsValid true if generated rings should be checked for validity
   */
  public void setCheckRingsValid(final boolean isCheckingRingsValid) {
    this.isCheckingRingsValid = isCheckingRingsValid;
  }

}
