package com.revolsys.geometry.index.intervalrtree;

import java.util.Comparator;

public class NodeComparator<V> implements Comparator<IntervalRTreeNode<V>> {
  @Override
  public int compare(final IntervalRTreeNode<V> n1, final IntervalRTreeNode<V> n2) {
    final double mid1 = (n1.getMin() + n1.getMax()) / 2;
    final double mid2 = (n2.getMin() + n2.getMax()) / 2;
    if (mid1 < mid2) {
      return -1;
    }
    if (mid1 > mid2) {
      return 1;
    }
    return 0;
  }
}
