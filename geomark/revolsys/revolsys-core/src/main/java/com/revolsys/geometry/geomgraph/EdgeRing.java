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
package com.revolsys.geometry.geomgraph;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.TopologyException;
import com.revolsys.geometry.model.editor.LineStringEditor;

/**
 * @version 1.7
 */
public abstract class EdgeRing implements BoundingBoxProxy {

  private final List<DirectedEdge> edges = new ArrayList<>(); // the

  // edges for this EdgeRing

  protected GeometryFactory geometryFactory;

  private final List<EdgeRing> holes = new ArrayList<>(); // a list of

  // DirectedEdges
  // making up

  // this EdgeRing

  private boolean isHole;

  private final Label label = new Label(Location.NONE); // label stores the

  // locations of each
  // geometry on the face
  // surrounded by this
  // ring

  private int maxNodeDegree = -1;

  private LinearRing ring; // the ring created for this EdgeRing

  // is its containing shell

  private EdgeRing shell; // if non-null, the ring is a hole and this EdgeRing

  // EdgeRings
  // which

  // are holes in this EdgeRing

  protected DirectedEdge startDe; // the directed edge which starts the list of

  public EdgeRing(final DirectedEdge start, final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    computePoints(start);
  }

  public void addHole(final EdgeRing ring) {
    this.holes.add(ring);
  }

  private void addPoints(final LineStringEditor points, final Edge edge, final boolean isForward,
    final boolean isFirstEdge) {
    final LineString line = edge.getLineString();
    final int numPoints = line.getVertexCount();
    if (isForward) {
      int startIndex = 1;
      if (isFirstEdge) {
        startIndex = 0;
      }
      for (int i = startIndex; i < numPoints; i++) {
        final double x = line.getX(i);
        final double y = line.getY(i);
        points.appendVertex(x, y);
      }
    } else { // is backward
      int startIndex = numPoints - 2;
      if (isFirstEdge) {
        startIndex = numPoints - 1;
      }
      for (int i = startIndex; i >= 0; i--) {
        final double x = line.getX(i);
        final double y = line.getY(i);
        points.appendVertex(x, y);
      }
    }
  }

  private void computeMaxNodeDegree() {
    this.maxNodeDegree = 0;
    DirectedEdge de = this.startDe;
    do {
      final Node node = de.getNode();
      final int degree = ((DirectedEdgeStar)node.getEdges()).getOutgoingDegree(this);
      if (degree > this.maxNodeDegree) {
        this.maxNodeDegree = degree;
      }
      de = getNext(de);
    } while (de != this.startDe);
    this.maxNodeDegree *= 2;
  }

  /**
   * Collect all the points from the DirectedEdges of this ring into a contiguous list
   */
  private void computePoints(final DirectedEdge start) {
    final LineStringEditor points = new LineStringEditor(this.geometryFactory, 10);
    this.startDe = start;
    DirectedEdge de = start;
    boolean isFirstEdge = true;
    do {
      // Assert.isTrue(de != null, "found null Directed Edge");
      if (de == null) {
        throw new TopologyException("Found null DirectedEdge");
      }
      if (de.getEdgeRing() == this) {
        throw new TopologyException(
          "Directed Edge visited twice during ring-building at " + de.getCoordinate());
      }

      this.edges.add(de);
      final Label label = de.getLabel();
      if (!label.isArea()) {
        throw new IllegalStateException("Label is not an area");
      }
      mergeLabel(label);
      addPoints(points, de.getEdge(), de.isForward(), isFirstEdge);
      isFirstEdge = false;
      setEdgeRing(de, this);
      de = getNext(de);
    } while (de != this.startDe);
    this.ring = points.newLinearRing();
    this.isHole = this.ring.isCounterClockwise();
  }

  /**
   * This method will cause the ring to be computed.
   * It will also check any holes, if they have been assigned.
   */
  public boolean containsPoint(final Point p) {
    final LinearRing shell = getLinearRing();
    final BoundingBox env = shell.getBoundingBox();
    if (!env.bboxCovers(p)) {
      return false;
    }
    if (!shell.isPointInRing(p)) {
      return false;
    }

    for (final EdgeRing hole : this.holes) {
      if (hole.containsPoint(p)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.ring.getBoundingBox();
  }

  /**
   * Returns the list of DirectedEdges that make up this EdgeRing
   */
  public List<DirectedEdge> getEdges() {
    return this.edges;
  }

  public Point getFirstPoint() {
    return this.ring.getPoint(0);
  }

  public Label getLabel() {
    return this.label;
  }

  public LinearRing getLinearRing() {
    return this.ring;
  }

  public int getMaxNodeDegree() {
    if (this.maxNodeDegree < 0) {
      computeMaxNodeDegree();
    }
    return this.maxNodeDegree;
  }

  abstract public DirectedEdge getNext(DirectedEdge de);

  public EdgeRing getShell() {
    return this.shell;
  }

  public boolean isHole() {
    // computePoints();
    return this.isHole;
  }

  public boolean isIsolated() {
    return this.label.getGeometryCount() == 1;
  }

  public boolean isShell() {
    return this.shell == null;
  }

  protected void mergeLabel(final Label deLabel) {
    mergeLabel(deLabel, 0);
    mergeLabel(deLabel, 1);
  }

  /**
   * Merge the RHS label from a DirectedEdge into the label for this EdgeRing.
   * The DirectedEdge label may be null.  This is acceptable - it results
   * from a node which is NOT an intersection node between the Geometries
   * (e.g. the end node of a LinearRing).  In this case the DirectedEdge label
   * does not contribute any information to the overall labelling, and is simply skipped.
   */
  protected void mergeLabel(final Label deLabel, final int geomIndex) {
    final Location loc = deLabel.getLocation(geomIndex, Position.RIGHT);
    // no information to be had from this label
    if (loc == Location.NONE) {
      return;
    }
    // if there is no current RHS value, set it
    if (this.label.getLocation(geomIndex) == Location.NONE) {
      this.label.setLocation(geomIndex, loc);
      return;
    }
  }

  abstract public void setEdgeRing(DirectedEdge de, EdgeRing er);

  public void setInResult() {
    DirectedEdge de = this.startDe;
    do {
      de.getEdge().setInResult(true);
      de = de.getNext();
    } while (de != this.startDe);
  }

  public void setShell(final EdgeRing shell) {
    this.shell = shell;
    if (shell != null) {
      shell.addHole(this);
    }
  }

  public Polygon toPolygon(final GeometryFactory geometryFactory) {
    final List<LinearRing> rings = new ArrayList<>();
    rings.add(getLinearRing());
    for (final EdgeRing element : this.holes) {
      final LinearRing ring = element.getLinearRing();
      rings.add(ring);
    }
    final Polygon poly = geometryFactory.polygon(rings);
    return poly;
  }

  @Override
  public String toString() {
    return this.ring.toString();
  }

}
