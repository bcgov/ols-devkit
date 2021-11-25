package com.revolsys.geometry.graph.filter;

import java.util.function.Predicate;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.model.LineString;

public class LineFilter<T> implements Predicate<Edge<T>> {
  private final Predicate<LineString> filter;

  public LineFilter(final Predicate<LineString> filter) {
    this.filter = filter;
  }

  @Override
  public boolean test(final Edge<T> edge) {
    final LineString line = edge.getLineString();
    return this.filter.test(line);
  }

}
