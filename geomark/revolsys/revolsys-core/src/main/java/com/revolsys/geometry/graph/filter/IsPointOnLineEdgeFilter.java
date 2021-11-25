package com.revolsys.geometry.graph.filter;

import java.util.function.Predicate;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.util.LineStringUtil;

public class IsPointOnLineEdgeFilter<T> implements Predicate<Node<T>> {

  private final Edge<T> edge;

  private final BoundingBox boundingBox;

  private final double maxDistance;

  public IsPointOnLineEdgeFilter(final Edge<T> edge, final double maxDistance) {
    this.edge = edge;
    this.maxDistance = maxDistance;
    this.boundingBox = edge.getBoundingBox() //
      .bboxEditor() //
      .expandDelta(maxDistance);
  }

  public com.revolsys.geometry.model.BoundingBox getEnvelope() {
    return this.boundingBox;
  }

  @Override
  public boolean test(final Node<T> node) {
    final LineString line = this.edge.getLineString();
    if (!this.edge.hasNode(node)) {
      if (node.intersectsBbox(this.boundingBox)) {
        if (LineStringUtil.isPointOnLine(line, node, this.maxDistance)) {
          return true;
        }
      }
    }
    return false;
  }

}
