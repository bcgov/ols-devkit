package com.revolsys.geometry.graph;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;

public class NodeList<T> extends AbstractList<Node<T>> {

  private final Graph<T> graph;

  private final List<Integer> nodeIds;

  public NodeList(final Graph<T> graph, final List<Integer> nodeIds) {
    this.graph = graph;
    this.nodeIds = nodeIds;
  }

  @Override
  public void add(final int index, final Node<T> node) {
    final int nodeId = node.getId();
    this.nodeIds.add(index, nodeId);
  }

  @Override
  public boolean add(final Node<T> node) {
    final int nodeId = node.getId();
    return this.nodeIds.add(nodeId);
  }

  @Override
  public boolean addAll(final Collection<? extends Node<T>> collection) {
    boolean added = false;
    for (final Node<T> node : collection) {
      added = true;
      add(node);
    }
    return added;
  }

  @Override
  public boolean addAll(int i, final Collection<? extends Node<T>> collection) {
    boolean added = false;
    for (final Node<T> node : collection) {
      added = true;
      add(i++, node);
    }
    return added;
  }

  @Override
  public Node<T> get(final int index) {
    final Integer nodeId = this.nodeIds.get(index);
    return this.graph.getNode(nodeId);
  }

  @Override
  public boolean remove(final Object o) {
    if (o instanceof Node) {
      @SuppressWarnings("unchecked")
      final Node<T> node = (Node<T>)o;
      final Object id = node.getId();
      return this.nodeIds.remove(id);
    } else {
      return false;
    }
  }

  @Override
  public boolean removeAll(final Collection<?> collection) {
    boolean removed = false;
    for (final Object object : collection) {
      removed |= remove(object);
    }
    return removed;
  }

  @Override
  public Node<T> set(final int index, final Node<T> edge) {
    final int nodeId = edge.getId();
    this.nodeIds.set(index, nodeId);
    return edge;
  }

  @Override
  public int size() {
    return this.nodeIds.size();
  }

}
