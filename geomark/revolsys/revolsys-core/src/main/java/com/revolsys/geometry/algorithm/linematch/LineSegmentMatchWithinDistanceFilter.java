package com.revolsys.geometry.algorithm.linematch;

import java.util.function.Predicate;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.model.BoundingBox;

public class LineSegmentMatchWithinDistanceFilter implements Predicate<Edge<LineSegmentMatch>> {
  private final BoundingBox boundingBox;

  private final double maxDistance;

  private final Node<LineSegmentMatch> node;

  public LineSegmentMatchWithinDistanceFilter(final Node<LineSegmentMatch> node,
    final double maxDistance) {
    this.node = node;
    this.maxDistance = maxDistance;
    this.boundingBox = node.getBoundingBox() //
      .bboxEditor() //
      .expandDelta(maxDistance);
  }

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  @Override
  public boolean test(final Edge<LineSegmentMatch> edge) {
    if (!edge.hasNode(this.node) && edge.distancePoint(this.node) < this.maxDistance) {
      return true;
    } else {
      return false;
    }
  }
}
