package com.revolsys.geometry.graph;

import java.util.Collection;
import java.util.List;

import com.revolsys.geometry.index.quadtree.AbstractIdObjectPointQuadTree;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.visitor.CreateListVisitor;

public class NodeQuadTree<T> extends AbstractIdObjectPointQuadTree<Node<T>> {
  private final Graph<T> graph;

  public NodeQuadTree(final Graph<T> graph) {
    this.graph = graph;
    final Collection<Integer> ids = graph.getNodeIds();
    add(ids);
  }

  @Override
  public void clear() {
    this.graph.clear();
  }

  @Override
  public BoundingBox getBoundingBox(final Node<T> node) {
    if (node == null) {
      return BoundingBox.empty();
    } else {
      final double x = node.getX();
      final double y = node.getY();
      final BoundingBox envelope = new BoundingBoxDoubleXY(x, y, x, y);
      return envelope;
    }
  }

  @Override
  public Point getCoordinates(final Node<T> node) {
    return node;
  }

  @Override
  public int getId(final Node<T> object) {
    return object.getId();
  }

  @Override
  public Node<T> getObject(final Integer id) {
    return this.graph.getNode(id);
  }

  @Override
  public List<Node<T>> getObjects(final List<Integer> ids) {
    return this.graph.getNodes(ids);
  }

  @Override
  public List<Node<T>> query(final BoundingBox envelope) {
    final CreateListVisitor<Node<T>> visitor = new CreateListVisitor<>();
    forEach(envelope, visitor);
    return visitor.getList();
  }
}
