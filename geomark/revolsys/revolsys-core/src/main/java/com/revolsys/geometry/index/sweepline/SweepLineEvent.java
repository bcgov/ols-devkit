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

/**
 * @version 1.7
 */
public class SweepLineEvent implements Comparable {
  public static final int DELETE = 2;

  public static final int INSERT = 1;

  private int deleteEventIndex;

  private int eventType;

  private final SweepLineEvent insertEvent; // null if this is an INSERT event

  SweepLineInterval sweepInt;

  private final double xValue;

  public SweepLineEvent(final double x, final SweepLineEvent insertEvent,
    final SweepLineInterval sweepInt) {
    this.xValue = x;
    this.insertEvent = insertEvent;
    this.eventType = INSERT;
    if (insertEvent != null) {
      this.eventType = DELETE;
    }
    this.sweepInt = sweepInt;
  }

  /**
   * ProjectionEvents are ordered first by their x-value, and then by their eventType.
   * It is important that Insert events are sorted before Delete events, so that
   * items whose Insert and Delete events occur at the same x-value will be
   * correctly handled.
   */
  @Override
  public int compareTo(final Object o) {
    final SweepLineEvent pe = (SweepLineEvent)o;
    if (this.xValue < pe.xValue) {
      return -1;
    }
    if (this.xValue > pe.xValue) {
      return 1;
    }
    if (this.eventType < pe.eventType) {
      return -1;
    }
    if (this.eventType > pe.eventType) {
      return 1;
    }
    return 0;
  }

  public int getDeleteEventIndex() {
    return this.deleteEventIndex;
  }

  public SweepLineEvent getInsertEvent() {
    return this.insertEvent;
  }

  SweepLineInterval getInterval() {
    return this.sweepInt;
  }

  public boolean isDelete() {
    return this.insertEvent != null;
  }

  public boolean isInsert() {
    return this.insertEvent == null;
  }

  public void setDeleteEventIndex(final int deleteEventIndex) {
    this.deleteEventIndex = deleteEventIndex;
  }

}
