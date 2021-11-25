package com.revolsys.geometry.graph.visitor;

import java.util.List;
import java.util.function.Consumer;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.index.IdObjectIndex;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.LineString;
import com.revolsys.visitor.CreateListVisitor;
import com.revolsys.visitor.DelegatingVisitor;

public class BoundingBoxIntersectsEdgeVisitor<T> extends DelegatingVisitor<Edge<T>> {
  public static <T> List<Edge<T>> getEdges(final Graph<T> graph, final Edge<T> edge,
    final double maxDistance) {
    final CreateListVisitor<Edge<T>> results = new CreateListVisitor<>();

    final LineString line = edge.getLineString();
    final BoundingBox boundingBox = line.getBoundingBox() //
      .bboxEditor() //
      .expandDelta(maxDistance);
    final BoundingBoxIntersectsEdgeVisitor<T> visitor = new BoundingBoxIntersectsEdgeVisitor<>(
      boundingBox, results);
    final IdObjectIndex<Edge<T>> index = graph.getEdgeIndex();
    index.forEach(boundingBox, visitor);
    final List<Edge<T>> list = results.getList();
    list.remove(edge);
    return list;

  }

  private final BoundingBox boundingBox;

  public BoundingBoxIntersectsEdgeVisitor(final BoundingBox boundingBox,
    final Consumer<Edge<T>> matchVisitor) {
    super(matchVisitor);
    this.boundingBox = boundingBox;
  }

  @Override
  public void accept(final Edge<T> edge) {
    final com.revolsys.geometry.model.BoundingBox envelope = edge.getBoundingBox();
    if (this.boundingBox.bboxIntersects(envelope)) {
      super.accept(edge);
    }
  }
}
