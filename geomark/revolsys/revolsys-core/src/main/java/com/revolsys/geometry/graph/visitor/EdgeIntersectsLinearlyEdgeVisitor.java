package com.revolsys.geometry.graph.visitor;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.index.IdObjectIndex;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Dimension;
import com.revolsys.geometry.model.IntersectionMatrix;
import com.revolsys.geometry.model.LineString;
import com.revolsys.visitor.CreateListVisitor;

public class EdgeIntersectsLinearlyEdgeVisitor<T> implements Consumer<Edge<T>> {

  public static <T> List<Edge<T>> getEdges(final Graph<T> graph, final Edge<T> edge) {
    final CreateListVisitor<Edge<T>> results = new CreateListVisitor<>();
    final LineString line = edge.getLineString();
    final BoundingBox env = line.getBoundingBox();
    final IdObjectIndex<Edge<T>> index = graph.getEdgeIndex();
    index.forEach(env, new EdgeIntersectsLinearlyEdgeVisitor<>(edge, results));
    final List<Edge<T>> edges = results.getList();
    Collections.sort(edges);
    return edges;

  }

  private final Edge<T> edge;

  private final Consumer<Edge<T>> matchVisitor;

  public EdgeIntersectsLinearlyEdgeVisitor(final Edge<T> edge,
    final Consumer<Edge<T>> matchVisitor) {
    this.edge = edge;
    this.matchVisitor = matchVisitor;
  }

  @Override
  public void accept(final Edge<T> edge2) {
    if (edge2 != this.edge) {
      final LineString line1 = this.edge.getLineString();
      final LineString line2 = edge2.getLineString();
      final BoundingBox envelope1 = line1.getBoundingBox();
      final BoundingBox envelope2 = line2.getBoundingBox();
      if (envelope1.bboxIntersects(envelope2)) {
        final IntersectionMatrix relate = line1.relate(line2);
        if (relate.get(0, 0) == Dimension.L) {
          this.matchVisitor.accept(edge2);
        }
      }
    }
  }

}
