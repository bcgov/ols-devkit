package com.revolsys.geometry.dissolve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import com.revolsys.geometry.edgegraph.HalfEdge;
import com.revolsys.geometry.edgegraph.MarkHalfEdge;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.PointList;

/**
 * Dissolves the linear components
 * from a collection of {@link Geometry}s
 * into a set of maximal-length {@link Linestring}s
 * in which every unique segment appears once only.
 * The output linestrings run between node vertices
 * of the input, which are vertices which have
 * either degree 1, or degree 3 or greater.
 * <p>
 * Use cases for dissolving linear components
 * include generalization
 * (in particular, simplifying polygonal coverages),
 * and visualization
 * (in particular, avoiding symbology conflicts when
 * depicting shared polygon boundaries).
 * <p>
 * This class does <b>not</b> node the input lines.
 * If there are line segments crossing in the input,
 * they will still cross in the output.
 *
 * @author Martin Davis
 *
 */
public class LineDissolver {
  /**
   * Dissolves the linear components in a geometry.
   *
   * @param g the geometry to dissolve
   * @return the dissolved lines
   */
  public static Geometry dissolve(final Geometry g) {
    final LineDissolver d = new LineDissolver();
    d.add(g);
    return d.getResult();
  }

  private GeometryFactory factory;

  private final DissolveEdgeGraph graph;

  private final List lines = new ArrayList();

  private final Stack nodeEdgeStack = new Stack();

  private Geometry result;

  private DissolveHalfEdge ringStartEdge;

  public LineDissolver() {
    this.graph = new DissolveEdgeGraph();
  }

  /**
   * Adds a collection of Geometries to be processed. May be called multiple times.
   * Any dimension of Geometry may be added; the constituent linework will be
   * extracted.
   *
   * @param geometries the geometries to be line-merged
   */
  public void add(final Collection geometries) {
    for (final Object element : geometries) {
      final Geometry geometry = (Geometry)element;
      add(geometry);
    }
  }

  /**
   * Adds a {@link Geometry} to be dissolved.
   * Any number of geometries may be added by calling this method multiple times.
   * Any type of Geometry may be added.  The constituent linework will be
   * extracted to be dissolved.
   *
   * @param geometry geometry to be line-merged
   */
  public void add(final Geometry geometry) {
    for (final LineString line : geometry.getGeometryComponents(LineString.class)) {
      add(line);
    }
  }

  private void add(final LineString lineString) {
    if (this.factory == null) {
      this.factory = lineString.getGeometryFactory();
    }
    final LineString seq = lineString;
    for (int i = 1; i < seq.getVertexCount(); i++) {
      final DissolveHalfEdge e = (DissolveHalfEdge)this.graph.addEdge(seq.getPoint(i - 1),
        seq.getPoint(i));
      /**
       * Record source initial segments, so that they can be reflected in output when needed
       * (i.e. during formation of isolated rings)
       */
      if (i == 1) {
        e.setStart();
      }
    }
  }

  private void addLine(final PointList line) {
    this.lines.add(this.factory.lineString(line.toPointArray()));
  }

  /**
   * Builds a line starting from the given edge.
   * The start edge origin is a node (valence = 1 or >= 3),
   * unless it is part of a pure ring.
   * A pure ring has no other incident lines.
   * In this case the start edge may occur anywhere on the ring.
   *
   * The line is built up to the next node encountered,
   * or until the start edge is re-encountered
   * (which happens if the edges form a ring).
   *
   * @param eStart
   */
  private void buildLine(final HalfEdge eStart) {
    final PointList line = new PointList();
    DissolveHalfEdge e = (DissolveHalfEdge)eStart;
    this.ringStartEdge = null;

    MarkHalfEdge.markBoth(e);
    line.add(e.orig().newPoint(), false);
    // scan along the path until a node is found (if one exists)
    while (e.sym().degree() == 2) {
      updateRingStartEdge(e);
      final DissolveHalfEdge eNext = (DissolveHalfEdge)e.next();
      // check if edges form a ring - if so, we're done
      if (eNext == eStart) {
        buildRing(this.ringStartEdge);
        return;
      }
      // add point to line, and move to next edge
      line.add(eNext.orig().newPoint(), false);
      e = eNext;
      MarkHalfEdge.markBoth(e);
    }
    // add final node
    line.add(e.dest().newPoint(), false);

    // queue up the final node edges
    stackEdges(e.sym());
    // store the scanned line
    addLine(line);
  }

  /**
   * For each edge in stack
   * (which must originate at a node)
   * extracts the line it initiates.
   */
  private void buildLines() {
    while (!this.nodeEdgeStack.empty()) {
      final HalfEdge e = (HalfEdge)this.nodeEdgeStack.pop();
      if (MarkHalfEdge.isMarked(e)) {
        continue;
      }
      buildLine(e);
    }
  }

  private void buildRing(final HalfEdge eStartRing) {
    final PointList line = new PointList();
    HalfEdge e = eStartRing;

    line.add(e.orig().newPoint(), false);
    // scan along the path until a node is found (if one exists)
    while (e.sym().degree() == 2) {
      final HalfEdge eNext = e.next();
      // check if edges form a ring - if so, we're done
      if (eNext == eStartRing) {
        break;
      }

      // add point to line, and move to next edge
      line.add(eNext.orig().newPoint(), false);
      e = eNext;
    }
    // add final node
    line.add(e.dest().newPoint(), false);

    // store the scanned line
    addLine(line);
  }

  private void computeResult() {
    final Collection edges = this.graph.getVertexEdges();
    for (final Object edge : edges) {
      final HalfEdge e = (HalfEdge)edge;
      if (MarkHalfEdge.isMarked(e)) {
        continue;
      }
      process(e);
    }
    this.result = this.factory.buildGeometry(this.lines);
  }

  /**
   * Gets the dissolved result as a MultiLineString.
   *
   * @return the dissolved lines
   */
  public Geometry getResult() {
    if (this.result == null) {
      computeResult();
    }
    return this.result;
  }

  private void process(final HalfEdge e) {
    HalfEdge eNode = e.prevNode();
    // if edge is in a ring, just process this edge
    if (eNode == null) {
      eNode = e;
    }
    stackEdges(eNode);
    // extract lines from node edges in stack
    buildLines();
  }

  /**
   * Adds edges around this node to the stack.
   *
   * @param node
   */
  private void stackEdges(final HalfEdge node) {
    HalfEdge e = node;
    do {
      if (!MarkHalfEdge.isMarked(e)) {
        this.nodeEdgeStack.add(e);
      }
      e = e.oNext();
    } while (e != node);

  }

  /**
   * Updates the tracked ringStartEdge
   * if the given edge has a lower origin
   * (using the standard {@link Coordinates} ordering).
   *
   * Identifying the lowest starting node meets two goals:
   * <ul>
   * <li>It ensures that isolated input rings are created using the original node and orientation
   * <li>For isolated rings formed from multiple input linestrings,
   * it provides a canonical node and orientation for the output
   * (rather than essentially random, and thus hard to test).
   * </ul>
   *
   * @param e
   */
  private void updateRingStartEdge(DissolveHalfEdge e) {
    if (!e.isStart()) {
      e = (DissolveHalfEdge)e.sym();
      if (!e.isStart()) {
        return;
      }
    }
    // here e is known to be a start edge
    if (this.ringStartEdge == null) {
      this.ringStartEdge = e;
      return;
    }
    if (e.orig().compareTo(this.ringStartEdge.orig()) < 0) {
      this.ringStartEdge = e;
    }
  }

}
