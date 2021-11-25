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
package com.revolsys.geometry.index.sweepline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A sweepline implements a sorted index on a set of intervals.
 * It is used to compute all overlaps between the interval in the index.
 *
 * @version 1.7
 */
public class SweepLineIndex {

  List events = new ArrayList();

  private boolean indexBuilt;

  public SweepLineIndex() {
  }

  public void add(final SweepLineInterval sweepInt) {
    final SweepLineEvent insertEvent = new SweepLineEvent(sweepInt.getMin(), null, sweepInt);
    this.events.add(insertEvent);
    this.events.add(new SweepLineEvent(sweepInt.getMax(), insertEvent, sweepInt));
  }

  /**
   * Because Delete Events have a link to their corresponding Insert event,
   * it is possible to compute exactly the range of events which must be
   * compared to a given Insert event object.
   */
  private void buildIndex() {
    if (this.indexBuilt) {
      return;
    }
    Collections.sort(this.events);
    for (int i = 0; i < this.events.size(); i++) {
      final SweepLineEvent ev = (SweepLineEvent)this.events.get(i);
      if (ev.isDelete()) {
        ev.getInsertEvent().setDeleteEventIndex(i);
      }
    }
    this.indexBuilt = true;
  }

  public void computeOverlaps(final SweepLineOverlapAction action) {
    buildIndex();

    for (int i = 0; i < this.events.size(); i++) {
      final SweepLineEvent ev = (SweepLineEvent)this.events.get(i);
      if (ev.isInsert()) {
        processOverlaps(i, ev.getDeleteEventIndex(), ev.getInterval(), action);
      }
    }
  }

  private void processOverlaps(final int start, final int end, final SweepLineInterval s0,
    final SweepLineOverlapAction action) {
    /**
     * Since we might need to test for self-intersections,
     * include current insert event object in list of event objects to test.
     * Last index can be skipped, because it must be a Delete event.
     */
    for (int i = start; i < end; i++) {
      final SweepLineEvent ev = (SweepLineEvent)this.events.get(i);
      if (ev.isInsert()) {
        final SweepLineInterval s1 = ev.getInterval();
        action.overlap(s0, s1);
      }
    }

  }

}
