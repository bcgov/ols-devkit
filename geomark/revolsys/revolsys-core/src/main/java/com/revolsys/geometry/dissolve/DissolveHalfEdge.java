package com.revolsys.geometry.dissolve;

import com.revolsys.geometry.edgegraph.MarkHalfEdge;
import com.revolsys.geometry.model.Point;

/**
 * A HalfEdge which carries information
 * required to support {@link LineDissolver}.
 *
 * @author Martin Davis
 *
 */
class DissolveHalfEdge extends MarkHalfEdge {
  private boolean isStart = false;

  public DissolveHalfEdge(final Point orig) {
    super(orig);
  }

  /**
   * Tests whether this edge is the starting segment
   * in a LineString being dissolved.
   *
   * @return true if this edge is a start segment
   */
  public boolean isStart() {
    return this.isStart;
  }

  /**
   * Sets this edge to be the start segment of an input LineString.
   */
  public void setStart() {
    this.isStart = true;
  }
}
