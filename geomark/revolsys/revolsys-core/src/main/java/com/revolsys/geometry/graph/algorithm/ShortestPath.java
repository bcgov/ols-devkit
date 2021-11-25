package com.revolsys.geometry.graph.algorithm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.graph.Node;

public class ShortestPath<T> {

  private final Map<Node<T>, Double> distanceByNode = new HashMap<>();

  private final Set<Node<T>> nodesToProcess = new TreeSet<>((a, b) -> {
    if (a.getId() == b.getId()) {
      return 0;
    } else {
      final double distance1 = getDistance(a);
      final double distance2 = getDistance(b);
      final int compare = Double.compare(distance1, distance2);
      if (compare == 0) {
        return -1;
      } else {
        return compare;
      }
    }
  });

  private final Map<Node<T>, Edge<T>> predecessorEdgeByNode = new HashMap<>();

  private final Set<Node<T>> processeNodes = new HashSet<>();

  public ShortestPath(final Graph<T> graph, final Node<T> source) {
    this.distanceByNode.put(source, 0.0);
    this.nodesToProcess.add(source);
    while (!this.nodesToProcess.isEmpty()) {
      final Node<T> node = getNextNode();
      processNode(node);
    }
  }

  private double getDistance(final Node<T> destination) {
    return this.distanceByNode.getOrDefault(destination, Double.MAX_VALUE);
  }

  private Node<T> getNextNode() {
    for (final Iterator<Node<T>> iterator = this.nodesToProcess.iterator(); iterator.hasNext();) {
      final Node<T> node = iterator.next();
      iterator.remove();
      this.processeNodes.add(node);
      return node;
    }
    return null;
  }

  public List<Edge<T>> getPath(final Node<T> target) {
    final LinkedList<Edge<T>> path = new LinkedList<>();
    Node<T> currentNode = target;
    Edge<T> currentEdge = this.predecessorEdgeByNode.get(currentNode);
    while (currentEdge != null) {
      path.addFirst(currentEdge);
      currentNode = currentEdge.getOppositeNode(currentNode);
      currentEdge = this.predecessorEdgeByNode.get(currentNode);
    }
    return path;
  }

  private boolean isProcessed(final Node<T> vertex) {
    return this.processeNodes.contains(vertex);
  }

  private void processNode(final Node<T> node) {
    for (final Edge<T> edge : node.getEdges()) {
      final Node<T> target = edge.getOppositeNode(node);
      if (!isProcessed(target)) {
        final double nodeShortestDistance = this.distanceByNode.getOrDefault(node,
          Double.MAX_VALUE);
        final double targetShortestDistance = this.distanceByNode.getOrDefault(target,
          Double.MAX_VALUE);
        final double length = edge.getLength();
        final double edgeDistance = nodeShortestDistance + length;
        if (targetShortestDistance > edgeDistance) {
          this.distanceByNode.put(target, edgeDistance);
          this.predecessorEdgeByNode.put(target, edge);
          this.nodesToProcess.add(target);
        }
      }
    }
  }

}
