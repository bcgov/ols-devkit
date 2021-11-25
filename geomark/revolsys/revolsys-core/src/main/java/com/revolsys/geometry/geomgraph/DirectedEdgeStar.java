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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.TopologyException;
import com.revolsys.geometry.util.Assert;

/**
 * A DirectedEdgeStar is an ordered list of <b>outgoing</b> DirectedEdges around a node.
 * It supports labelling the edges as well as linking the edges to form both
 * MaximalEdgeRings and MinimalEdgeRings.
 *
 * @version 1.7
 */
public class DirectedEdgeStar extends EdgeEndStar<DirectedEdge> {

  private Label label;

  private final int LINKING_TO_OUTGOING = 2;

  /**
   * A list of all outgoing edges in the result, in CCW order
   */
  private List<DirectedEdge> resultAreaEdgeList;

  private final int SCANNING_FOR_INCOMING = 1;

  public DirectedEdgeStar() {
  }

  public void computeDepths(final DirectedEdge de) {
    final int edgeIndex = findIndex(de);
    final int startDepth = de.getDepth(Position.LEFT);
    final int targetLastDepth = de.getDepth(Position.RIGHT);
    // compute the depths from this edge up to the end of the edge array
    final int nextDepth = computeDepths(edgeIndex + 1, this.edgeList.size(), startDepth);
    // compute the depths for the initial part of the array
    final int lastDepth = computeDepths(0, edgeIndex, nextDepth);
    if (lastDepth != targetLastDepth) {
      throw new TopologyException("depth mismatch at " + de.getCoordinate());
    }
  }

  /**
   * Compute the DirectedEdge depths for a subsequence of the edge array.
   *
   * @return the last depth assigned (from the R side of the last edge visited)
   */
  private int computeDepths(final int startIndex, final int endIndex, final int startDepth) {
    int currDepth = startDepth;
    for (int i = startIndex; i < endIndex; i++) {
      final DirectedEdge nextDe = this.edgeList.get(i);
      nextDe.setEdgeDepths(Position.RIGHT, currDepth);
      currDepth = nextDe.getDepth(Position.LEFT);
    }
    return currDepth;
  }

  /**
   * Compute the labelling for all dirEdges in this star, as well
   * as the overall labelling
   */
  @Override
  public void computeLabelling(final GeometryGraph[] geom) {
    // Debug.print(this);
    super.computeLabelling(geom);

    // determine the overall labelling for this DirectedEdgeStar
    // (i.e. for the node it is based at)
    this.label = new Label(Location.NONE);
    for (final DirectedEdge de : this) {

      final Edge e = de.getEdge();
      final Label eLabel = e.getLabel();
      for (int i = 0; i < 2; i++) {
        final Location eLoc = eLabel.getLocation(i);
        if (eLoc == Location.INTERIOR || eLoc == Location.BOUNDARY) {
          this.label.setLocation(i, Location.INTERIOR);
        }
      }
    }
    // Debug.print(this);
  }

  /**
   * Traverse the star of edges, maintaing the current location in the result
   * area at this node (if any).
   * If any L edges are found in the interior of the result, mark them as covered.
   */
  public void findCoveredLineEdges() {
    // Debug.print("findCoveredLineEdges");
    // Debug.print(this);
    // Since edges are stored in CCW order around the node,
    // as we move around the ring we move from the right to the left side of the
    // edge

    /**
     * Find first DirectedEdge of result area (if any).
     * The interior of the result is on the RHS of the edge,
     * so the start location will be:
     * - INTERIOR if the edge is outgoing
     * - EXTERIOR if the edge is incoming
     */
    Location startLoc = Location.NONE;
    for (final DirectedEdge nextOut : this) {
      final DirectedEdge nextIn = nextOut.getSym();
      if (!nextOut.isLineEdge()) {
        if (nextOut.isInResult()) {
          startLoc = Location.INTERIOR;
          break;
        }
        if (nextIn.isInResult()) {
          startLoc = Location.EXTERIOR;
          break;
        }
      }
    }
    // no A edges found, so can't determine if L edges are covered or not
    if (startLoc == Location.NONE) {
      return;
    }

    /**
     * move around ring, keeping track of the current location
     * (Interior or Exterior) for the result area.
     * If L edges are found, mark them as covered if they are in the interior
     */
    Location currLoc = startLoc;
    for (final DirectedEdge nextOut : this) {
      final DirectedEdge nextIn = nextOut.getSym();
      if (nextOut.isLineEdge()) {
        nextOut.getEdge().setCovered(currLoc == Location.INTERIOR);
        // Debug.println(nextOut);
      } else { // edge is an Area edge
        if (nextOut.isInResult()) {
          currLoc = Location.EXTERIOR;
        }
        if (nextIn.isInResult()) {
          currLoc = Location.INTERIOR;
        }
      }
    }
  }

  public Label getLabel() {
    return this.label;
  }

  public int getOutgoingDegree() {
    int degree = 0;
    for (final DirectedEdge de : this) {
      if (de.isInResult()) {
        degree++;
      }
    }
    return degree;
  }

  public int getOutgoingDegree(final EdgeRing er) {
    int degree = 0;
    for (final DirectedEdge de : this) {
      if (de.getEdgeRing() == er) {
        degree++;
      }
    }
    return degree;
  }

  private List<DirectedEdge> getResultAreaEdges() {
    // print(System.out);
    if (this.resultAreaEdgeList != null) {
      return this.resultAreaEdgeList;
    }
    this.resultAreaEdgeList = new ArrayList<>();
    for (final DirectedEdge de : this) {
      if (de.isInResult() || de.getSym().isInResult()) {
        this.resultAreaEdgeList.add(de);
      }
    }
    return this.resultAreaEdgeList;
  }

  public DirectedEdge getRightmostEdge() {
    final List<DirectedEdge> edges = getEdges();
    final int size = edges.size();
    if (size < 1) {
      return null;
    }
    final DirectedEdge de0 = edges.get(0);
    if (size == 1) {
      return de0;
    }
    final DirectedEdge deLast = edges.get(size - 1);

    final int quad0 = de0.getQuadrant();
    final int quad1 = deLast.getQuadrant();
    if (Quadrant.isNorthern(quad0) && Quadrant.isNorthern(quad1)) {
      return de0;
    } else if (!Quadrant.isNorthern(quad0) && !Quadrant.isNorthern(quad1)) {
      return deLast;
    } else {
      // edges are in different hemispheres - make sure we return one that is
      // non-horizontal
      if (de0.getDy() != 0) {
        return de0;
      } else if (deLast.getDy() != 0) {
        return deLast;
      }
    }
    Assert.shouldNeverReachHere("found two horizontal edges incident on node");
    return null;

  }

  /**
   * Insert a directed edge in the list
   */
  @Override
  public void insert(final EdgeEnd edgeEnd) {
    final DirectedEdge de = (DirectedEdge)edgeEnd;
    insertEdgeEnd(de, de);
  }

  public void linkAllDirectedEdges() {
    getEdges();
    // find first area edge (if any) to start linking at
    DirectedEdge prevOut = null;
    DirectedEdge firstIn = null;
    // link edges in CW order
    for (int i = this.edgeList.size() - 1; i >= 0; i--) {
      final DirectedEdge nextOut = this.edgeList.get(i);
      final DirectedEdge nextIn = nextOut.getSym();
      if (firstIn == null) {
        firstIn = nextIn;
      }
      if (prevOut != null) {
        nextIn.setNext(prevOut);
      }
      // record outgoing edge, in order to link the last incoming edge
      prevOut = nextOut;
    }
    firstIn.setNext(prevOut);
    // Debug.print(this);
  }

  public void linkMinimalDirectedEdges(final EdgeRing er) {
    // find first area edge (if any) to start linking at
    DirectedEdge firstOut = null;
    DirectedEdge incoming = null;
    int state = this.SCANNING_FOR_INCOMING;
    // link edges in CW order
    for (int i = this.resultAreaEdgeList.size() - 1; i >= 0; i--) {
      final DirectedEdge nextOut = this.resultAreaEdgeList.get(i);
      final DirectedEdge nextIn = nextOut.getSym();

      // record first outgoing edge, in order to link the last incoming edge
      if (firstOut == null && nextOut.getEdgeRing() == er) {
        firstOut = nextOut;
      }

      switch (state) {
        case SCANNING_FOR_INCOMING:
          if (nextIn.getEdgeRing() != er) {
            continue;
          }
          incoming = nextIn;
          state = this.LINKING_TO_OUTGOING;
        break;
        case LINKING_TO_OUTGOING:
          if (nextOut.getEdgeRing() != er) {
            continue;
          }
          incoming.setNextMin(nextOut);
          state = this.SCANNING_FOR_INCOMING;
        break;
      }
    }
    // print(System.out);
    if (state == this.LINKING_TO_OUTGOING) {
      Assert.isTrue(firstOut != null, "found null for first outgoing dirEdge");
      Assert.isTrue(firstOut.getEdgeRing() == er, "unable to link last incoming dirEdge");
      incoming.setNextMin(firstOut);
    }
  }

  /**
   * Traverse the star of DirectedEdges, linking the included edges together.
   * To link two dirEdges, the <next> pointer for an incoming dirEdge
   * is set to the next outgoing edge.
   * <p>
   * DirEdges are only linked if:
   * <ul>
   * <li>they belong to an area (i.e. they have sides)
   * <li>they are marked as being in the result
   * </ul>
   * <p>
   * Edges are linked in CCW order (the order they are stored).
   * This means that rings have their face on the Right
   * (in other words,
   * the topological location of the face is given by the RHS label of the DirectedEdge)
   * <p>
   * PRECONDITION: No pair of dirEdges are both marked as being in the result
   */
  public void linkResultDirectedEdges() {
    // make sure edges are copied to resultAreaEdges list
    getResultAreaEdges();
    // find first area edge (if any) to start linking at
    DirectedEdge firstOut = null;
    DirectedEdge incoming = null;
    int state = this.SCANNING_FOR_INCOMING;
    // link edges in CCW order
    for (final DirectedEdge nextOut : this.resultAreaEdgeList) {
      final DirectedEdge nextIn = nextOut.getSym();

      // skip de's that we're not interested in
      if (!nextOut.getLabel().isArea()) {
        continue;
      }

      // record first outgoing edge, in order to link the last incoming edge
      if (firstOut == null && nextOut.isInResult()) {
        firstOut = nextOut;
        // assert: sym.isInResult() == false, since pairs of dirEdges should
        // have been removed already
      }

      switch (state) {
        case SCANNING_FOR_INCOMING:
          if (!nextIn.isInResult()) {
            continue;
          }
          incoming = nextIn;
          state = this.LINKING_TO_OUTGOING;
        break;
        case LINKING_TO_OUTGOING:
          if (!nextOut.isInResult()) {
            continue;
          }
          incoming.setNext(nextOut);
          state = this.SCANNING_FOR_INCOMING;
        break;
      }
    }
    // Debug.print(this);
    if (state == this.LINKING_TO_OUTGOING) {
      // Debug.print(firstOut == null, this);
      if (firstOut == null) {
        throw new TopologyException("no outgoing dirEdge found", getCoordinate());
      }
      // Assert.isTrue(firstOut != null, "no outgoing dirEdge found (at " +
      // getCoordinate() );
      if (!firstOut.isInResult()) {
        throw new IllegalStateException("unable to link last incoming dirEdge");
      }
      incoming.setNext(firstOut);
    }
  }

  /**
   * For each dirEdge in the star,
   * merge the label from the sym dirEdge into the label
   */
  public void mergeSymLabels() {
    for (final DirectedEdge de : this) {
      final Label label = de.getLabel();
      label.merge(de.getSym().getLabel());
    }
  }

  @Override
  public void print(final PrintStream out) {
    System.out.println("DirectedEdgeStar: " + getCoordinate());
    for (final DirectedEdge de : this) {
      out.print("out ");
      de.print(out);
      out.println();
      out.print("in ");
      de.getSym().print(out);
      out.println();
    }
  }

  /**
   * Update incomplete dirEdge labels from the labelling for the node
   */
  public void updateLabelling(final Label nodeLabel) {
    for (final DirectedEdge de : this) {
      final Label label = de.getLabel();
      label.setAllLocationsIfNull(0, nodeLabel.getLocation(0));
      label.setAllLocationsIfNull(1, nodeLabel.getLocation(1));
    }
  }
}
