package com.revolsys.geometry.graph.comparator;

import java.util.Comparator;

import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.model.Point;

/**
 * Compare the distance of nodes from a given node.
 *
 * @author paustin
 */
public class NodeDistanceComparator<T> implements Comparator<Node<T>> {

  private final boolean invert;

  private final Node<T> node;

  public NodeDistanceComparator(final Node<T> node) {
    this.node = node;
    this.invert = false;
  }

  public NodeDistanceComparator(final Node<T> node, final boolean invert) {
    this.node = node;
    this.invert = invert;
  }

  @Override
  public int compare(final Node<T> node1, final Node<T> node2) {
    int compare;
    final double distance1 = node1.distancePoint(this.node);
    final double distance2 = node2.distancePoint(this.node);
    if (distance1 == distance2) {
      final Point point1 = node1;
      final Point point2 = node2;
      compare = point1.compareTo(point2);
    } else if (distance1 < distance2) {
      compare = -1;
    } else {
      compare = 1;
    }

    if (this.invert) {
      return -compare;
    } else {
      return compare;
    }
  }

  public boolean isInvert() {
    return this.invert;
  }

}
