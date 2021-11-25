package com.revolsys.geometry.edgegraph;

import com.revolsys.geometry.model.Point;

/**
 * A {@link HalfEdge} which supports
 * marking edges with a boolean flag.
 * Useful for algorithms which perform graph traversals.
 *
 * @author Martin Davis
 *
 */
public class MarkHalfEdge extends HalfEdge {
  /**
   * Tests whether the given edge is marked.
   *
   * @param e the edge to test
   * @return true if the edge is marked
   */
  public static boolean isMarked(final HalfEdge e) {
    return ((MarkHalfEdge)e).isMarked();
  }

  /**
   * Marks the given edge.
   *
   * @param e the edge to mark
   */
  public static void mark(final HalfEdge e) {
    ((MarkHalfEdge)e).mark();
  }

  /**
   * Marks the edges in a pair.
   *
   * @param e an edge of the pair to mark
   */
  public static void markBoth(final HalfEdge e) {
    ((MarkHalfEdge)e).mark();
    ((MarkHalfEdge)e.sym()).mark();
  }

  /**
   * Sets the mark for the given edge to a boolean value.
   *
   * @param e the edge to set
   * @param isMarked the mark value
   */
  public static void setMark(final HalfEdge e, final boolean isMarked) {
    ((MarkHalfEdge)e).setMark(isMarked);
  }

  /**
   * Sets the mark for the given edge pair to a boolean value.
   *
   * @param e an edge of the pair to update
   * @param isMarked the mark value to set
   */
  public static void setMarkBoth(final HalfEdge e, final boolean isMarked) {
    ((MarkHalfEdge)e).setMark(isMarked);
    ((MarkHalfEdge)e.sym()).setMark(isMarked);
  }

  private boolean isMarked = false;

  /**
   * Creates a new marked edge.
   *
   * @param orig the coordinate of the edge origin
   */
  public MarkHalfEdge(final Point orig) {
    super(orig);
  }

  /**
   * Tests whether this edge is marked.
   *
   * @return true if this edge is marked
   */
  public boolean isMarked() {
    return this.isMarked;
  }

  /**
   * Marks this edge.
   *
   */
  public void mark() {
    this.isMarked = true;
  }

  /**
   * Sets the value of the mark on this edge.
   *
   * @param isMarked the mark value to set
   */
  public void setMark(final boolean isMarked) {
    this.isMarked = isMarked;
  }

}
