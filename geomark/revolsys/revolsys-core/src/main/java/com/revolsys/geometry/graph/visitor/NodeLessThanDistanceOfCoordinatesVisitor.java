package com.revolsys.geometry.graph.visitor;

import java.util.function.Consumer;

import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.model.Point;

public class NodeLessThanDistanceOfCoordinatesVisitor<T> implements Consumer<Node<T>> {
  private final Point coordinates;

  private final Consumer<Node<T>> matchVisitor;

  private final double maxDistance;

  public NodeLessThanDistanceOfCoordinatesVisitor(final Point coordinates, final double maxDistance,
    final Consumer<Node<T>> matchVisitor) {
    this.coordinates = coordinates;
    this.maxDistance = maxDistance;
    this.matchVisitor = matchVisitor;
  }

  @Override
  public void accept(final Node<T> node) {
    final double distance = this.coordinates.distancePoint(node);
    if (distance < this.maxDistance) {
      this.matchVisitor.accept(node);
    }
  }

}
