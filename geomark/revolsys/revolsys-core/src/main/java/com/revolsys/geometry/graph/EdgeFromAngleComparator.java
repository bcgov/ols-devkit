package com.revolsys.geometry.graph;

import java.util.Comparator;

import org.jeometry.common.math.Angle;

/**
 * The EdgeComparitor class is used to return edges in a clockwise order.
 *
 * @author Paul Austin
 * @param <T> The type of object stored on the edges in the graph.
 */
public class EdgeFromAngleComparator<T> implements Comparator<Edge<T>> {
  private static final EdgeFromAngleComparator INSTANCE = new EdgeFromAngleComparator();

  @SuppressWarnings("unchecked")
  public static <T> EdgeFromAngleComparator<T> get() {
    return INSTANCE;
  }

  /**
   * Construct a new EdgeComparitor.
   */
  public EdgeFromAngleComparator() {
  }

  /**
   * <p>
   * Compare the two edges at a given node. The comparison is calculated in the
   * following order.
   * </p>
   * <ol>
   * <li>If edge1 does not start or end at the node 1 is returned.</li>
   * <li>If edge2 does not start or end at the node -1 is returned.</li>
   * <li>Otherwise the exit angles {@link Edge#getExitAngle(Node)} from the node
   * for the two edges are compared. </p>
   * </ol>
   *
   * @param edge1 The first edge.
   * @param edge2 The second edge.
   * @see Angle#getTurn(double, double)
   */
  @Override
  public int compare(final Edge<T> edge1, final Edge<T> edge2) {
    final double angle1 = edge1.getFromAngle();
    final double angle2 = edge2.getFromAngle();
    return Angle.getTurn(angle1, angle2);
  }
}
