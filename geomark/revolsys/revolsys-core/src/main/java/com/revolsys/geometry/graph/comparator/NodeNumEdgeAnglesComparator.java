package com.revolsys.geometry.graph.comparator;

import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.graph.attribute.NodeProperties;

public class NodeNumEdgeAnglesComparator<T> extends NodeDegreeComparator<T> {

  public NodeNumEdgeAnglesComparator() {
  }

  public NodeNumEdgeAnglesComparator(final boolean invert) {
    super(invert);
  }

  @Override
  public int compare(final Node<T> node1, final Node<T> node2) {
    int compare;
    final int numAngles1 = NodeProperties.getEdgeAngles(node1).size();
    final int numAngles2 = NodeProperties.getEdgeAngles(node2).size();
    if (numAngles1 == numAngles2) {
      return super.compare(node1, node2);
    } else if (numAngles1 < numAngles2) {
      compare = -1;
    } else {
      compare = 1;
    }

    if (isInvert()) {
      return -compare;
    } else {
      return compare;
    }
  }
}
