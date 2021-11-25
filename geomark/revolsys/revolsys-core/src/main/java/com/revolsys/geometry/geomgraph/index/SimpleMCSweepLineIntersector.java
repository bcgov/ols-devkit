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
package com.revolsys.geometry.geomgraph.index;

import java.util.Arrays;
import java.util.List;

import com.revolsys.geometry.geomgraph.Edge;

/**
 * Finds all intersections in one or two sets of edges,
 * using an x-axis sweepline algorithm in conjunction with Monotone Chains.
 * While still O(n^2) in the worst case, this algorithm
 * drastically improves the average-case time.
 * The use of MonotoneChains as the items in the index
 * seems to offer an improvement in performance over a sweep-line alone.
 *
 * @version 1.7
 */
public class SimpleMCSweepLineIntersector extends EdgeSetIntersector {
  private final SweepLineEvent[] EMPTY = new SweepLineEvent[0];

  private SweepLineEvent[] events = this.EMPTY;

  private int eventCount = 0;

  private boolean changed = true;

  /**
   * A SimpleMCSweepLineIntersector creates monotone chains from the edges
   * and compares them using a simple sweep-line along the x-axis.
   */
  public SimpleMCSweepLineIntersector() {
  }

  private void add(final Edge edge, final Object edgeSet) {
    SweepLineEvent[] events = this.events;
    final MonotoneChainEdge chainEdge = edge.getMonotoneChainEdge();
    final int[] startIndexes = chainEdge.startIndexes;
    final int count = startIndexes.length - 1;
    final int requiredLength = this.eventCount + 2 * count;
    if (requiredLength >= events.length) {
      int newLength = events.length + (events.length >> 1);
      if (newLength < requiredLength) {
        newLength = requiredLength;
      }
      if (newLength < 16) {
        newLength = 16;
      }
      events = new SweepLineEvent[newLength];
      System.arraycopy(this.events, 0, events, 0, this.eventCount);
      this.events = events;
    }
    for (int i = 0; i < count; i++) {
      final MonotoneChain chain = new MonotoneChain(chainEdge, i);

      double minX = edge.getX(startIndexes[i]);
      double maxX = edge.getX(startIndexes[i + 1]);
      if (minX > maxX) {
        final double t = minX;
        minX = maxX;
        maxX = t;
      }
      final SweepLineInsertEvent minEvent = new SweepLineInsertEvent(edgeSet, minX, chain);
      final SweepLineEvent maxEvent = new SweepLineDeleteEvent(maxX, minEvent);
      events[this.eventCount++] = minEvent;
      events[this.eventCount++] = maxEvent;
    }
    this.changed = true;
  }

  private void add(final List<Edge> edges) {
    for (final Edge edge : edges) {
      // edge is its own group
      add(edge, edge);
    }
  }

  private void add(final List<Edge> edges, final Object edgeSet) {
    for (final Edge edge : edges) {
      add(edge, edgeSet);
    }
  }

  @Override
  public void computeIntersections(final List<Edge> edges0, final List<Edge> edges1,
    final SegmentIntersector si) {
    add(edges0, edges0);
    add(edges1, edges1);
    computeIntersections(si);
  }

  @Override
  public void computeIntersections(final List<Edge> edges, final SegmentIntersector si,
    final boolean testAllSegments) {
    if (testAllSegments) {
      add(edges, null);
    } else {
      add(edges);
    }
    computeIntersections(si);
  }

  private void computeIntersections(final SegmentIntersector si) {
    prepareEvents();
    final SweepLineEvent[] events = this.events;
    final int eventCount = this.eventCount;
    for (int i = 0; i < eventCount; i++) {
      final SweepLineEvent event = events[i];
      if (event.isInsert()) {
        final SweepLineInsertEvent insert = (SweepLineInsertEvent)event;
        final int deleteEventIndex = insert.getDeleteEventIndex();
        processOverlaps(i, deleteEventIndex, insert, si);
      }
    }
  }

  /**
   * Because Delete Events have a link to their corresponding Insert event,
   * it is possible to compute exactly the range of events which must be
   * compared to a given Insert event object.
   */
  private void prepareEvents() {
    if (this.changed) {
      this.changed = false;
      final SweepLineEvent[] events = this.events;
      final int eventCount = this.eventCount;
      Arrays.sort(events, 0, eventCount);
      // set DELETE event indexes
      for (int i = 0; i < eventCount; i++) {
        final SweepLineEvent event = events[i];
        if (event.isDelete()) {
          final SweepLineDeleteEvent delete = (SweepLineDeleteEvent)event;
          final SweepLineInsertEvent insertEvent = delete.getInsertEvent();
          insertEvent.setDeleteEventIndex(i);
        }
      }
    }
  }

  private void processOverlaps(final int start, final int end, final SweepLineInsertEvent insert1,
    final SegmentIntersector intersector) {
    final MonotoneChain chain1 = insert1.object;
    final int chain1Index = chain1.chainIndex;
    final MonotoneChainEdge chain1Edge = chain1.edge;
    final SweepLineEvent[] events = this.events;
    /*
     * Since we might need to test for self-intersections, include current
     * INSERT event object in list of event objects to test. Last index can be
     * skipped, because it must be a Delete event.
     */
    for (int i = start; i < end; i++) {
      final SweepLineEvent event = events[i];
      if (event.isInsert()) {
        final SweepLineInsertEvent insert = (SweepLineInsertEvent)event;
        final MonotoneChain chain2 = insert.object;
        // don't compare edges in same group, if labels are present
        if (!insert1.isSameLabel(insert)) {
          final MonotoneChainEdge chain2Edge = chain2.edge;
          final int chain2Index = chain2.chainIndex;
          chain1Edge.computeIntersectsForChain(chain1Index, chain2Edge, chain2Index, intersector);
        }
      }
    }
  }
}
