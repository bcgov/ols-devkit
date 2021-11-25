package com.revolsys.geometry.graph;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EdgeList<T> extends AbstractList<Edge<T>> {

  private List<Integer> edgeIds;

  private final Graph<T> graph;

  public EdgeList(final Graph<T> graph) {
    this(graph, new ArrayList<Integer>());
  }

  public EdgeList(final Graph<T> graph, final List<Integer> edgeIds) {
    this.graph = graph;
    this.edgeIds = edgeIds;
  }

  @Override
  public boolean add(final Edge<T> edge) {
    final int edgeId = edge.getId();
    return this.edgeIds.add(edgeId);
  }

  @Override
  public void add(final int index, final Edge<T> edge) {
    final int edgeId = edge.getId();
    this.edgeIds.add(index, edgeId);
  }

  @Override
  public boolean addAll(final Collection<? extends Edge<T>> collection) {
    boolean added = false;
    for (final Edge<T> edge : collection) {
      added = true;
      add(edge);
    }
    return added;
  }

  @Override
  public boolean addAll(int i, final Collection<? extends Edge<T>> collection) {
    boolean added = false;
    for (final Edge<T> edge : collection) {
      added = true;
      add(i++, edge);
    }
    return added;
  }

  @Override
  public Edge<T> get(final int index) {
    final Integer edgeId = this.edgeIds.get(index);
    return this.graph.getEdge(edgeId);
  }

  @Override
  public boolean remove(final Object o) {
    if (o instanceof Edge) {
      @SuppressWarnings("unchecked")
      final Edge<T> edge = (Edge<T>)o;
      final Object id = edge.getId();
      return this.edgeIds.remove(id);
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
  public Edge<T> set(final int index, final Edge<T> edge) {
    final int edgeId = edge.getId();
    this.edgeIds.set(index, edgeId);
    return edge;
  }

  @Override
  public int size() {
    return this.edgeIds.size();
  }

}
