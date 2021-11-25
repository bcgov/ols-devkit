package com.revolsys.geometry.graph.comparator;

import java.util.Comparator;

import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.model.Point;

public class NodeDegreeComparator<T> implements Comparator<Node<T>> {

  private boolean invert = false;

  public NodeDegreeComparator() {
  }

  public NodeDegreeComparator(final boolean invert) {
    this.invert = invert;
  }

  @Override
  public int compare(final Node<T> node1, final Node<T> node2) {
    int compare;
    final int degree1 = node1.getDegree();
    final int degree2 = node2.getDegree();
    if (degree1 == degree2) {
      final Point point1 = node1;
      final Point point2 = node2;
      compare = point1.compareTo(point2);
    } else if (degree1 < degree2) {
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
