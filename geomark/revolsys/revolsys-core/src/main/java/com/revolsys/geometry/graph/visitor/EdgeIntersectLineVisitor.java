package com.revolsys.geometry.graph.visitor;

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

public class EdgeIntersectLineVisitor<T> implements Consumer<Edge<T>> {

  public static <T> List<Edge<T>> getEdges(final Graph<T> graph, final LineString line) {
    final CreateListVisitor<Edge<T>> results = new CreateListVisitor<>();
    final BoundingBox env = line.getBoundingBox();
    final IdObjectIndex<Edge<T>> index = graph.getEdgeIndex();
    index.forEach(env, new EdgeIntersectLineVisitor<>(line, results));
    return results.getList();

  }

  private final LineString line;

  private final Consumer<Edge<T>> matchVisitor;

  public EdgeIntersectLineVisitor(final LineString line, final Consumer<Edge<T>> matchVisitor) {
    this.line = line;
    this.matchVisitor = matchVisitor;
  }

  @Override
  public void accept(final Edge<T> edge) {
    final LineString line = edge.getLineString();
    final IntersectionMatrix relate = this.line.relate(line);
    if (relate.get(0, 0) == Dimension.L) {
      this.matchVisitor.accept(edge);
    }
  }

}
