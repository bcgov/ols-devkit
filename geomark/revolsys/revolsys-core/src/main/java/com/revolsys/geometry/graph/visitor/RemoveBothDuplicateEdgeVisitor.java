package com.revolsys.geometry.graph.visitor;

import java.util.Set;
import java.util.function.Consumer;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.util.LineStringUtil;
import com.revolsys.util.ObjectProcessor;

/**
 * Find all edges that share the same line geometry and remove the current edge
 * and matching edges. Can be used to dissolve lines between polygons.
 */
public class RemoveBothDuplicateEdgeVisitor<T>
  implements Consumer<Edge<T>>, ObjectProcessor<Graph<T>> {
  @Override
  public void accept(final Edge<T> edge) {
    final LineString line = edge.getLineString();
    final Node<T> fromNode = edge.getFromNode();
    final Node<T> toNode = edge.getToNode();
    final Set<Edge<T>> edges = fromNode.getEdgesTo(toNode);
    edges.remove(edge);
    boolean hasDuplicate = false;
    for (final Edge<T> edge2 : edges) {
      final LineString line2 = edge2.getLineString();
      if (LineStringUtil.equalsIgnoreDirection(line, line2, 2)) {
        edge2.remove();
        hasDuplicate = true;
      }
    }
    if (hasDuplicate) {
      edge.remove();
    }
  }

  @Override
  public void process(final Graph<T> graph) {
    graph.forEachEdge(this);
  }
}
