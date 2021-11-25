package com.revolsys.geometry.graph.filter;

import java.util.function.Predicate;

import com.revolsys.geometry.graph.Edge;

public class EdgeObjectFilter<T> implements Predicate<Edge<T>> {
  private Predicate<T> filter;

  public EdgeObjectFilter() {
  }

  public EdgeObjectFilter(final Predicate<T> filter) {
    this.filter = filter;
  }

  public Predicate<T> getFilter() {
    return this.filter;
  }

  public void setFilter(final Predicate<T> filter) {
    this.filter = filter;
  }

  @Override
  public boolean test(final Edge<T> edge) {
    final T object = edge.getObject();
    return this.filter.test(object);
  }
}
