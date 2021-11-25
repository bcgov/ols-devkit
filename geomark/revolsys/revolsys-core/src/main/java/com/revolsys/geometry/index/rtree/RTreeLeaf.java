package com.revolsys.geometry.index.rtree;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.geometry.model.BoundingBox;

public class RTreeLeaf<T> extends RTreeNode<T> {

  private BoundingBox[] objectBoundingBoxes;

  private T[] objects;

  private int size = 0;

  public RTreeLeaf() {
  }

  @SuppressWarnings("unchecked")
  public RTreeLeaf(final int size) {
    this.objects = (T[])new Object[size];
    this.objectBoundingBoxes = new BoundingBox[size];
  }

  public void add(final BoundingBox objectBoundingBox, final T object) {
    this.objectBoundingBoxes[this.size] = objectBoundingBox;
    this.objects[this.size] = object;
    this.size++;
    expandBoundingBox(objectBoundingBox);
  }

  @Override
  protected RTreeLeaf<T> chooseLeaf(final List<RTreeBranch<T>> path,
    final BoundingBox boundingBox) {
    return this;
  }

  @Override
  public void forEach(final double x, final double y, final Consumer<? super T> action) {
    for (int i = 0; i < this.size; i++) {
      final BoundingBox objectBounds = this.objectBoundingBoxes[i];
      if (objectBounds.bboxIntersects(x, y)) {
        final T object = this.objects[i];
        action.accept(object);
      }
    }
  }

  @Override
  public void forEach(final double minX, final double minY, final double maxX, final double maxY,
    final Consumer<? super T> action) {
    for (int i = 0; i < this.size; i++) {
      final BoundingBox objectBounds = this.objectBoundingBoxes[i];
      if (objectBounds.bboxIntersects(minX, minY, maxX, maxY)) {
        final T object = this.objects[i];
        action.accept(object);
      }
    }
  }

  @Override
  public void forEach(final double minX, final double minY, final double maxX, final double maxY,
    final Predicate<? super T> filter, final Consumer<? super T> action) {
    for (int i = 0; i < this.size; i++) {
      final BoundingBox objectBounds = this.objectBoundingBoxes[i];
      if (objectBounds.bboxIntersects(minX, minY, maxX, maxY)) {
        final T object = this.objects[i];
        if (filter.test(object)) {
          action.accept(object);
        }
      }
    }
  }

  @Override
  public void forEachValue(final Consumer<? super T> action) {
    for (int i = 0; i < this.size; i++) {
      final T object = this.objects[i];
      action.accept(object);
    }
  }

  @Override
  public void forEachValue(final Predicate<? super T> filter, final Consumer<? super T> action) {
    for (int i = 0; i < this.size; i++) {
      final T object = this.objects[i];
      if (filter.test(object)) {
        action.accept(object);
      }
    }
  }

  public int getSize() {
    return this.size;
  }

  @Override
  public boolean remove(final double minX, final double minY, final double maxX, final double maxY,
    final T object) {
    for (int i = 0; i < this.size; i++) {
      final BoundingBox objectBounds = this.objectBoundingBoxes[i];
      final T object1 = this.objects[i];
      if (object1 == object) {
        if (objectBounds.getMinX() == minX && objectBounds.getMinY() == minY
          && objectBounds.getMaxX() == maxX && objectBounds.getMaxY() == maxY) {
          System.arraycopy(this.objectBoundingBoxes, i + 1, this.objectBoundingBoxes, i,
            this.size - i - 1);
          this.objectBoundingBoxes[this.size - 1] = null;
          System.arraycopy(this.objects, i + 1, this.objects, i, this.size - i - 1);
          this.objects[this.size - 1] = null;
          this.size--;
          updateEnvelope();
          return true;
        } else {
          // ERROR
        }
      }
    }
    return false;
  }

  public List<RTreeNode<T>> split(final T object, final BoundingBox objectBoundingBox) {
    final RTreeLeaf<T> leaf1 = new RTreeLeaf<>(this.objects.length);
    final RTreeLeaf<T> leaf2 = new RTreeLeaf<>(this.objects.length);

    // TODO Add some ordering to the results
    final int midPoint = (int)Math.ceil(this.size / 2.0);
    for (int i = 0; i <= midPoint; i++) {
      final BoundingBox objectBounds = this.objectBoundingBoxes[i];
      final T object1 = this.objects[i];
      leaf1.add(objectBounds, object1);
    }
    for (int i = midPoint + 1; i < this.size; i++) {
      final BoundingBox objectBounds = this.objectBoundingBoxes[i];
      final T object1 = this.objects[i];
      leaf2.add(objectBounds, object1);
    }
    leaf2.add(objectBoundingBox, object);
    return Arrays.<RTreeNode<T>> asList(leaf1, leaf2);
  }

  @Override
  protected void updateEnvelope() {
    double minX = Double.MAX_VALUE;
    double maxX = -Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxY = -Double.MAX_VALUE;

    for (int i = 0; i < this.size; i++) {
      final BoundingBox objectBounds = this.objectBoundingBoxes[i];
      final double nodeMinX = objectBounds.getMinX();
      if (nodeMinX < minX) {
        minX = nodeMinX;
      }
      final double nodeMinY = objectBounds.getMinY();
      if (nodeMinY < minY) {
        minY = nodeMinY;
      }

      final double nodeMaxX = objectBounds.getMaxX();
      if (nodeMaxX > maxX) {
        maxX = nodeMaxX;
      }
      final double nodeMaxY = objectBounds.getMaxY();
      if (nodeMaxY > maxY) {
        maxY = nodeMaxY;
      }
    }
    setBoundingBox(minX, minY, maxX, maxY);
  }
}
