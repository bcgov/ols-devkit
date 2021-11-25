package com.revolsys.geometry.graph.visitor;

import java.util.Collection;
import java.util.List;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.graph.Node;

public class SplitEdgesCloseToNodeVisitor<T> extends AbstractNodeListenerVisitor<T> {
  public static final String EDGE_CLOSE_TO_NODE = "Edge close to node";

  private final Graph<T> graph;

  private final double maxDistance;

  private Collection<Edge<T>> newEdges = null;

  private String ruleName = EDGE_CLOSE_TO_NODE;

  private Collection<T> splitObjects = null;

  public SplitEdgesCloseToNodeVisitor(final Graph<T> graph, final double maxDistance) {
    this.graph = graph;
    this.maxDistance = maxDistance;
  }

  public SplitEdgesCloseToNodeVisitor(final Graph<T> graph, final String ruleName,
    final double maxDistance) {
    this.graph = graph;
    this.ruleName = ruleName;
    this.maxDistance = maxDistance;
  }

  @Override
  public void accept(final Node<T> node) {
    final List<Edge<T>> closeEdges = EdgeLessThanDistanceToNodeVisitor
      .edgesWithinDistance(this.graph, node, this.maxDistance);
    for (final Edge<T> edge : closeEdges) {
      final T object = edge.getObject();
      final String typePath = this.graph.getTypeName(edge);
      final List<Edge<T>> splitEdges = this.graph.splitEdge(edge, node);
      if (splitEdges.size() > 1) {
        nodeEvent(node, typePath, this.ruleName, "Fixed", null);
        if (this.splitObjects != null) {
          this.splitObjects.add(object);
        }
        if (this.newEdges != null) {
          this.newEdges.remove(edge);
          this.newEdges.addAll(splitEdges);
        }
      }
    }
  }

  public double getMaxDistance() {
    return this.maxDistance;
  }

  public Collection<Edge<T>> getNewEdges() {
    return this.newEdges;
  }

  public Collection<T> getSplitObjects() {
    return this.splitObjects;
  }

  public void setNewEdges(final Collection<Edge<T>> newEdges) {
    this.newEdges = newEdges;
  }

  public void setSplitObjects(final Collection<T> splitObjects) {
    this.splitObjects = splitObjects;
  }

}
