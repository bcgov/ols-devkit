package com.revolsys.geometry.graph.filter;

import java.util.function.Predicate;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Node;

public class EdgeHasNodeFilter<T> implements Predicate<Edge<T>> {
  private final Node<T> node;

  public EdgeHasNodeFilter(final Node<T> node) {
    this.node = node;
  }

  @Override
  public boolean test(final Edge<T> edge) {
    return edge.hasNode(this.node);
  }
}
