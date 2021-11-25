package com.revolsys.geometry.geomgraph.index;

public class SweepLineEvent implements Comparable<SweepLineEvent> {

  private final double x;

  public SweepLineEvent(final double x) {
    this.x = x;
  }

  /**
   * Events are ordered first by their x-value, and then by their eventType.
   * Insert events are sorted before Delete events, so that
   * items whose Insert and Delete events occur at the same x-value will be
   * correctly handled.
   */
  @Override
  public int compareTo(final SweepLineEvent event) {
    final int compare = Double.compare(this.x, event.x);
    if (compare == 0) {
      if (isInsert()) {
        if (event.isInsert()) {
          return 0;
        } else {
          return -1;
        }
      } else {
        if (event.isInsert()) {
          return 1;
        } else {
          return 0;
        }
      }
    }
    return compare;
  }

  public boolean isDelete() {
    return false;
  }

  public boolean isInsert() {
    return false;
  }

}
