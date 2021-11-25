package com.revolsys.geometry.index.rstartree;

import java.util.function.Consumer;

import com.revolsys.geometry.model.BoundingBox;

public class RStarLeaf<T> implements RStarNode<T> {

  private final T item;

  private final BoundingBox boundingBox;

  public RStarLeaf(final T item, final BoundingBox boundingBox) {
    this.item = item;
    this.boundingBox = boundingBox;
  }

  @Override
  public void forEach(final Consumer<? super T> action) {
    action.accept(this.item);
  }

  @Override
  public void forEach(final double x, final double y, final Consumer<? super T> action) {
    if (this.boundingBox.bboxCovers(x, y)) {
      action.accept(this.item);
    }
  }

  @Override
  public void forEach(final double minX, final double minY, final double maxX, final double maxY,
    final Consumer<? super T> action) {
    if (this.boundingBox.bboxIntersects(minX, minY, maxX, maxY)) {
      action.accept(this.item);
    }
  }

  @Override
  public double getArea() {
    return this.boundingBox.getArea();
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public T getItem() {
    return this.item;
  }
}
