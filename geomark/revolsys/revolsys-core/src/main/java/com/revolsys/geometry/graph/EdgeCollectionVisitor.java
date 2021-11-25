package com.revolsys.geometry.graph;

import java.util.Collection;
import java.util.function.Consumer;

public class EdgeCollectionVisitor<T> implements Consumer<Edge<T>> {

  private final Collection<T> objects;

  public EdgeCollectionVisitor(final Collection<T> objects) {
    this.objects = objects;
  }

  @Override
  public void accept(final Edge<T> edge) {
    final T object = edge.getObject();
    this.objects.add(object);
  }
}
