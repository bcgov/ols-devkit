package com.revolsys.geometry.index.quadtree;

import com.revolsys.geometry.model.GeometryFactory;

public abstract class IdObjectQuadTree<T> extends QuadTree<T> {
  private static final long serialVersionUID = 1L;

  public IdObjectQuadTree(final GeometryFactory geometryFactory) {
    super(geometryFactory, new IdObjectNode<T>());
  }

  protected Object getId(final T item) {
    return item;
  }

  @SuppressWarnings("unchecked")
  protected T getItem(final Object id) {
    return (T)id;
  }

  protected abstract boolean intersectsBounds(Object id, double x, double y);

  protected abstract boolean intersectsBounds(Object id, double minX, double minY, double maxX,
    double maxY);
}
