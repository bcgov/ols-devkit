package com.revolsys.geometry.graph.visitor;

import java.util.List;
import java.util.function.Consumer;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.graph.Node;

public class SplitEdgesOnNodeVisitor<T> implements Consumer<Node<T>> {

  @Override
  public void accept(final Node<T> node) {
    while (splitEdgesCloseToNode(node)) {
    }
  }

  /**
   * Split edges which the node is on the line of the edge. The edge will only
   * be split if the original node has at least one edge which shares the
   * first two coordinates as one of the split lines.
   *
   * @param node The node.
   * @return True if an edge was split, false otherwise.
   */
  private boolean splitEdgesCloseToNode(final Node<T> node) {
    final Graph<T> graph = node.getGraph();
    final List<Edge<T>> nodeEdges = node.getEdges();
    if (!nodeEdges.isEmpty()) {
      final List<Edge<T>> edges = NodeOnEdgeVisitor.getEdges(graph, node, 1);
      for (final Edge<T> edge : edges) {
        if (!edge.isRemoved() && !node.hasEdge(edge)) {
          graph.splitEdge(edge, node);
          return true;
        }
      }
    }
    return false;
  }

}
