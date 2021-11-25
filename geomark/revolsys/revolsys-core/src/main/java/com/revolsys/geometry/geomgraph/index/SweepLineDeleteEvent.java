package com.revolsys.geometry.geomgraph.index;

public class SweepLineDeleteEvent extends SweepLineEvent {

  private final SweepLineInsertEvent insertEvent;

  public SweepLineDeleteEvent(final double x, final SweepLineInsertEvent insertEvent) {
    super(x);
    this.insertEvent = insertEvent;
  }

  public SweepLineInsertEvent getInsertEvent() {
    return this.insertEvent;
  }

  @Override
  public boolean isDelete() {
    return true;
  }

}
