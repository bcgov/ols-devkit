package com.revolsys.geometry.graph.comparator;

import java.util.Comparator;

import com.revolsys.geometry.graph.Edge;

/**
 * A comparator which will compare the length of the lines of two edges, edges
 * with a shorter length will be returned before those with a longer length.
 *
 * @author Paul Austin
 */
public class EdgeLengthComparator<T> implements Comparator<Edge<T>> {

  private boolean invert = false;

  public EdgeLengthComparator() {
  }

  public EdgeLengthComparator(final boolean invert) {
    this.invert = invert;
  }

  @Override
  public int compare(final Edge<T> edge1, final Edge<T> edge2) {
    int compare;
    final double length1 = edge1.getLength();
    final double length2 = edge2.getLength();
    compare = Double.compare(length1, length2);

    if (this.invert) {
      return -compare;
    } else {
      return compare;
    }
  }
}
