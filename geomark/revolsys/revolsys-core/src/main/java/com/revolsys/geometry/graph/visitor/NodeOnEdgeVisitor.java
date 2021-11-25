package com.revolsys.geometry.graph.visitor;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.index.IdObjectIndex;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.util.LineStringUtil;
import com.revolsys.visitor.CreateListVisitor;
import com.revolsys.visitor.DelegatingVisitor;

public class NodeOnEdgeVisitor<T> extends DelegatingVisitor<Edge<T>> {
  public static <T> List<Edge<T>> getEdges(final Graph<T> graph, final Node<T> node,
    final double maxDistance) {
    final CreateListVisitor<Edge<T>> results = new CreateListVisitor<>();
    final Point point = node;
    final BoundingBox boundingBox = point.getBoundingBox() //
      .bboxEditor() //
      .expandDelta(maxDistance);
    final IdObjectIndex<Edge<T>> index = graph.getEdgeIndex();
    final NodeOnEdgeVisitor<T> visitor = new NodeOnEdgeVisitor<>(node, boundingBox, maxDistance,
      results);
    index.forEach(boundingBox, visitor);
    final List<Edge<T>> edges = results.getList();
    Collections.sort(edges);
    return edges;

  }

  private final BoundingBox boundingBox;

  private final double maxDistance;

  private final Node<T> node;

  private final Point point;

  public NodeOnEdgeVisitor(final Node<T> node, final BoundingBox boundingBox,
    final double maxDistance, final Consumer<Edge<T>> matchVisitor) {
    super(matchVisitor);
    this.node = node;
    this.boundingBox = boundingBox;
    this.maxDistance = maxDistance;
    this.point = node;
  }

  @Override
  public void accept(final Edge<T> edge) {
    if (!edge.hasNode(this.node)) {
      final LineString line = edge.getLineString();
      if (line.getBoundingBox().bboxIntersects(this.boundingBox)) {
        if (LineStringUtil.isPointOnLine(line, this.point, this.maxDistance)) {
          super.accept(edge);
        }
      }
    }
  }

}
