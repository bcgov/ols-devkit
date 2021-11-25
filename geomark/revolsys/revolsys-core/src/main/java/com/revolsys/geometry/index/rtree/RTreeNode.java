package com.revolsys.geometry.index.rtree;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.geometry.index.BoundingBoxNode;
import com.revolsys.geometry.model.BoundingBox;

public abstract class RTreeNode<T> extends BoundingBoxNode {

  public RTreeNode() {
  }

  protected abstract RTreeLeaf<T> chooseLeaf(final List<RTreeBranch<T>> path,
    final BoundingBox boundingBox);

  @Override
  protected void expandBoundingBox(final BoundingBox boundingBox) {
    super.expandBoundingBox(boundingBox);
  }

  public abstract void forEach(final double x, final double y, final Consumer<? super T> action);

  public abstract void forEach(double minX, double minY, double maxX, double maxY,
    Consumer<? super T> action);

  public abstract void forEach(double minX, double minY, double maxX, double maxY,
    Predicate<? super T> filter, Consumer<? super T> action);

  public abstract void forEachValue(Consumer<? super T> action);

  public abstract void forEachValue(Predicate<? super T> filter, Consumer<? super T> action);

  protected double getRequiredExpansion(final BoundingBox boundingBox) {
    double areaExpansion = 0;

    final double minX1 = getMinX();
    final double minY1 = getMinY();
    final double maxX1 = getMaxX();
    final double maxY1 = getMaxY();

    final double minX2 = boundingBox.getMinX();
    final double minY2 = boundingBox.getMinY();
    final double maxX2 = boundingBox.getMaxX();
    final double maxY2 = boundingBox.getMaxY();

    final double maxWidth = Math.max(maxX1, maxX2) - Math.min(minX1, minX2);
    final double maxHeight = Math.max(maxY1, maxY2) - Math.min(minY1, minY2);
    if (minX1 > minX2) {
      areaExpansion += (minX1 - minX2) * maxHeight;
    }
    if (maxX1 < maxX2) {
      areaExpansion += (maxX2 - maxX1) * maxHeight;
    }
    if (minY1 > minY2) {
      areaExpansion += (minY1 - minY2) * maxWidth;
    }
    if (maxY1 < maxY2) {
      areaExpansion += (maxY2 - maxY1) * maxWidth;
    }

    return areaExpansion;
  }

  public abstract boolean remove(final double minX, final double minY, final double maxX,
    final double maxY, T object);

  protected abstract void updateEnvelope();
}
