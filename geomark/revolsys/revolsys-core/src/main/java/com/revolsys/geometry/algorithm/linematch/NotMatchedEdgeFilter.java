package com.revolsys.geometry.algorithm.linematch;

import java.util.function.Predicate;

import com.revolsys.geometry.graph.Edge;

public class NotMatchedEdgeFilter implements Predicate<Edge<LineSegmentMatch>> {
  private final int index;

  public NotMatchedEdgeFilter(final int index) {
    this.index = index;
  }

  @Override
  public boolean test(final Edge<LineSegmentMatch> edge) {
    return edge.getObject().hasMatches(this.index);
  }

}
