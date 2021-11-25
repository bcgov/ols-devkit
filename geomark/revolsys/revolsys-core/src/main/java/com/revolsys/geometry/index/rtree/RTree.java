package com.revolsys.geometry.index.rtree;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.geometry.index.SpatialIndex;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.util.ExitLoopException;

public class RTree<T> implements SpatialIndex<T> {

  private int maxEntries;

  private RTreeNode<T> root = new RTreeLeaf<>(this.maxEntries);

  private int size;

  public RTree() {
    this(12, 32);
  }

  public RTree(final int minEntries, final int maxEntries) {
    this.maxEntries = maxEntries;
    clear();
  }

  @Override
  public void clear() {
    this.size = 0;
    this.root = new RTreeLeaf<>(this.maxEntries);
  }

  @Override
  public boolean forEach(final Consumer<? super T> action) {
    try {
      this.root.forEachValue(action);
      return true;
    } catch (final ExitLoopException e) {
      return false;
    }
  }

  @Override
  public boolean forEach(final double x, final double y, final Consumer<? super T> action) {
    try {
      this.root.forEach(x, y, action);
      return true;
    } catch (final ExitLoopException e) {
      return false;
    }
  }

  @Override
  public boolean forEach(final double minX, final double minY, final double maxX, final double maxY,
    final Consumer<? super T> action) {
    try {
      this.root.forEach(minX, minY, maxX, maxY, action);
      return true;
    } catch (final ExitLoopException e) {
      return false;
    }
  }

  @Override
  public boolean forEach(final double minX, final double minY, final double maxX, final double maxY,
    final Predicate<? super T> filter, final Consumer<? super T> action) {
    try {
      this.root.forEach(minX, minY, maxX, maxY, filter, action);
      return true;
    } catch (final ExitLoopException e) {
      return false;
    }
  }

  @Override
  public boolean forEach(final Predicate<? super T> filter, final Consumer<? super T> action) {
    try {
      this.root.forEachValue(filter, action);
      return true;
    } catch (final ExitLoopException e) {
      return false;
    }
  }

  @Override
  public int getSize() {
    return this.size;
  }

  @Override
  public void insertItem(final BoundingBox boundingBox, final T object) {
    final LinkedList<RTreeBranch<T>> path = new LinkedList<>();
    final RTreeLeaf<T> leaf = this.root.chooseLeaf(path, boundingBox);
    if (leaf.getSize() == this.maxEntries) {
      final List<RTreeNode<T>> newNodes = leaf.split(object, boundingBox);
      replace(path, leaf, newNodes);
    } else {
      leaf.add(boundingBox, object);
    }
    this.size++;
  }

  @Override
  public boolean removeItem(final BoundingBox boundingBox, final T object) {
    final double minX = boundingBox.getMinX();
    final double minY = boundingBox.getMinY();
    final double maxX = boundingBox.getMaxX();
    final double maxY = boundingBox.getMaxY();
    if (this.root.remove(minX, minY, maxX, maxY, object)) {
      this.size--;
      return true;
    } else {
      return false;
    }
  }

  private void replace(final LinkedList<RTreeBranch<T>> path, final RTreeNode<T> oldNode,
    final List<RTreeNode<T>> newNodes) {
    if (path.isEmpty()) {
      this.root = new RTreeBranch<>(this.maxEntries, newNodes);
    } else {
      final RTreeBranch<T> parentNode = path.removeLast();
      if (parentNode.getSize() + newNodes.size() - 1 >= this.maxEntries) {
        final List<RTreeNode<T>> newParentNodes = parentNode.split(oldNode, newNodes);
        replace(path, parentNode, newParentNodes);
      } else {
        parentNode.replace(oldNode, newNodes);
      }
    }

  }

}
