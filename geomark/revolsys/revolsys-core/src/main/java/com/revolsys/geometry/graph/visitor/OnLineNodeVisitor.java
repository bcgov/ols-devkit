package com.revolsys.geometry.graph.visitor;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.index.IdObjectIndex;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.util.LineStringUtil;
import com.revolsys.visitor.CreateListVisitor;

public class OnLineNodeVisitor<T> implements Consumer<Node<T>> {
  public static <T> List<Node<T>> getNodes(final Graph<T> graph, final LineString line,
    final double maxDistance) {
    if (line == null) {
      return Collections.emptyList();
    } else {
      final CreateListVisitor<Node<T>> results = new CreateListVisitor<>();
      final BoundingBox env = line.getBoundingBox() //
        .bboxEditor() //
        .expandDelta(maxDistance);
      final IdObjectIndex<Node<T>> index = graph.getNodeIndex();
      final OnLineNodeVisitor<T> visitor = new OnLineNodeVisitor<>(line, results);
      index.forEach(env, visitor);
      return results.getList();
    }
  }

  private final LineString line;

  private final Consumer<Node<T>> matchVisitor;

  public OnLineNodeVisitor(final LineString line, final Consumer<Node<T>> matchVisitor) {
    this.line = line;
    this.matchVisitor = matchVisitor;
  }

  @Override
  public void accept(final Node<T> node) {
    final Point point = node;
    if (LineStringUtil.isPointOnLine(this.line, point)) {
      this.matchVisitor.accept(node);
    }
  }

}
