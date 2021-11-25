package com.revolsys.geometry.graph.filter;

import java.util.function.Predicate;

import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.model.Point;

public class NodeCoordinatesFilter<T> implements Predicate<Node<T>> {
  private Predicate<Point> filter;

  public NodeCoordinatesFilter() {
  }

  public NodeCoordinatesFilter(final Predicate<Point> filter) {
    this.filter = filter;
  }

  public Predicate<Point> getFilter() {
    return this.filter;
  }

  public void setFilter(final Predicate<Point> filter) {
    this.filter = filter;
  }

  @Override
  public boolean test(final Node<T> node) {
    return this.filter.test(node);
  }
}
