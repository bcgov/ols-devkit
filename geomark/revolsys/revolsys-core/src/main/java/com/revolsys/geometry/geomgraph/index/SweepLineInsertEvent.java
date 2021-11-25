package com.revolsys.geometry.geomgraph.index;

public class SweepLineInsertEvent extends SweepLineEvent {

  private int deleteEventIndex;

  private final Object label;

  final MonotoneChain object;

  public SweepLineInsertEvent(final Object label, final double x, final MonotoneChain obj) {
    super(x);
    this.label = label;
    this.object = obj;
  }

  public int getDeleteEventIndex() {
    return this.deleteEventIndex;
  }

  @Override
  public boolean isInsert() {
    return true;
  }

  public boolean isSameLabel(final SweepLineInsertEvent ev) {
    // no label set indicates single group
    if (this.label == null) {
      return false;
    }
    return this.label == ev.label;
  }

  public void setDeleteEventIndex(final int deleteEventIndex) {
    this.deleteEventIndex = deleteEventIndex;
  }

}
