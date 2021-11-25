package com.revolsys.geometry.index.rtree;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.collection.ArrayUtil;
import com.revolsys.geometry.model.BoundingBox;

public class RTreeBranch<T> extends RTreeNode<T> {

  private RTreeNode<T>[] nodes;

  private int size;

  public RTreeBranch() {
  }

  @SuppressWarnings("unchecked")
  public RTreeBranch(final int size) {
    this.nodes = ArrayUtil.newArray(RTreeNode.class, size);
  }

  protected RTreeBranch(final int size, final List<RTreeNode<T>> nodes) {
    this(size);
    for (final RTreeNode<T> node : nodes) {
      add(node);
    }
  }

  private void add(final RTreeNode<T> node) {
    this.nodes[this.size] = node;
    this.size++;
    expandBoundingBox(node);
  }

  @Override
  protected RTreeLeaf<T> chooseLeaf(final List<RTreeBranch<T>> path,
    final BoundingBox boundingBox) {
    expandBoundingBox(boundingBox);
    path.add(this);
    double minExpansion = Float.MAX_VALUE;
    RTreeNode<T> next = null;
    final int childCount = this.size;
    final RTreeNode<T>[] childNodes = this.nodes;
    for (int i = 0; i < childCount; i++) {
      final RTreeNode<T> childNode = childNodes[i];
      final double expansion = childNode.getRequiredExpansion(boundingBox);
      if (expansion < minExpansion) {
        minExpansion = expansion;
        next = childNode;
      } else if (expansion == minExpansion) {
        final double childArea = childNode.getArea();
        final double minArea = next.getArea();
        if (childArea < minArea) {
          next = childNode;
        }
      }
    }
    return next.chooseLeaf(path, boundingBox);
  }

  @Override
  protected void expandBoundingBox(final double... bounds) {
    super.expandBoundingBox(bounds);
  }

  @Override
  protected void expandBoundingBox(final double minX, final double minY, final double maxX,
    final double maxY) {
    super.expandBoundingBox(minX, minY, maxX, maxY);
  }

  @Override
  public void forEach(final double x, final double y, final Consumer<? super T> action) {
    final int childCount = this.size;
    final RTreeNode<T>[] childNodes = this.nodes;
    for (int i = 0; i < childCount; i++) {
      final RTreeNode<T> childNode = childNodes[i];
      if (childNode.intersectsBoundingBox(x, y)) {
        childNode.forEach(x, y, action);
      }
    }
  }

  @Override
  public void forEach(final double minX, final double minY, final double maxX, final double maxY,
    final Consumer<? super T> action) {
    final int childCount = this.size;
    final RTreeNode<T>[] childNodes = this.nodes;
    for (int i = 0; i < childCount; i++) {
      final RTreeNode<T> childNode = childNodes[i];
      if (childNode.intersectsBoundingBox(minX, minY, maxX, maxY)) {
        childNode.forEach(minX, minY, maxX, maxY, action);
      }
    }
  }

  @Override
  public void forEach(final double minX, final double minY, final double maxX, final double maxY,
    final Predicate<? super T> filter, final Consumer<? super T> action) {
    final int childCount = this.size;
    final RTreeNode<T>[] childNodes = this.nodes;
    for (int i = 0; i < childCount; i++) {
      final RTreeNode<T> childNode = childNodes[i];
      if (childNode.intersectsBoundingBox(minX, minY, maxX, maxY)) {
        childNode.forEach(minX, minY, maxX, maxY, filter, action);
      }
    }
  }

  @Override
  public void forEachValue(final Consumer<? super T> action) {
    final int childCount = this.size;
    final RTreeNode<T>[] childNodes = this.nodes;
    for (int i = 0; i < childCount; i++) {
      final RTreeNode<T> childNode = childNodes[i];
      childNode.forEachValue(action);
    }
  }

  @Override
  public void forEachValue(final Predicate<? super T> filter, final Consumer<? super T> action) {
    final int childCount = this.size;
    final RTreeNode<T>[] childNodes = this.nodes;
    for (int i = 0; i < childCount; i++) {
      final RTreeNode<T> childNode = childNodes[i];
      childNode.forEachValue(filter, action);
    }
  }

  public int getSize() {
    return this.size;
  }

  @Override
  public boolean remove(final double minX, final double minY, final double maxX, final double maxY,
    final T object) {
    final int childCount = this.size;
    final RTreeNode<T>[] childNodes = this.nodes;
    for (int i = 0; i < childCount; i++) {
      final RTreeNode<T> childNode = childNodes[i];
      if (childNode.covers(minX, minY, maxX, maxY)) {
        if (childNode.remove(minX, minY, maxX, maxY, object)) {
          updateEnvelope();
          return true;
        }
      }
    }
    return false;
  }

  public void replace(final RTreeNode<T> node, final List<RTreeNode<T>> newNodes) {
    for (int i = 1; i < newNodes.size(); i++) {
      final RTreeNode<T> newNode = newNodes.get(i);
      add(newNode);
    }
    final int childCount = this.size;
    final RTreeNode<T>[] childNodes = this.nodes;
    for (int i = 0; i < childCount; i++) {
      final RTreeNode<T> childNode = childNodes[i];
      if (childNode == node) {
        childNodes[i] = newNodes.get(0);
        return;
      }
    }
  }

  public List<RTreeNode<T>> split(final RTreeNode<T> node, final List<RTreeNode<T>> newNodes) {
    final RTreeBranch<T> branch1 = new RTreeBranch<>(this.nodes.length);
    final RTreeBranch<T> branch2 = new RTreeBranch<>(this.nodes.length);

    // TODO Add some ordering to the results
    final int midPoint = (int)Math.ceil(this.size / 2.0);
    for (int i = 0; i <= midPoint; i++) {
      final RTreeNode<T> childNode = this.nodes[i];
      if (childNode == node) {
        branch1.add(newNodes.get(0));
      } else {
        branch1.add(childNode);
      }
    }
    for (int i = midPoint + 1; i < this.size; i++) {
      final RTreeNode<T> childNode = this.nodes[i];
      if (childNode == node) {
        branch1.add(newNodes.get(0));
      } else {
        branch2.add(childNode);
      }
    }
    final RTreeNode<T> newNode = newNodes.get(1);
    branch2.add(newNode);
    return Arrays.<RTreeNode<T>> asList(branch1, branch2);
  }

  @Override
  protected void updateEnvelope() {
    double minX = Double.MAX_VALUE;
    double maxX = -Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxY = -Double.MAX_VALUE;

    final int childCount = this.size;
    final RTreeNode<T>[] childNodes = this.nodes;
    for (int i = 0; i < childCount; i++) {
      final RTreeNode<T> childNode = childNodes[i];
      final double nodeMinX = childNode.getMinX();
      if (nodeMinX < minX) {
        minX = nodeMinX;
      }
      final double nodeMinY = childNode.getMinY();
      if (nodeMinY < minY) {
        minY = nodeMinY;
      }

      final double nodeMaxX = childNode.getMaxX();
      if (nodeMaxX > maxX) {
        maxX = nodeMaxX;
      }
      final double nodeMaxY = childNode.getMaxY();
      if (nodeMaxY > maxY) {
        maxY = nodeMaxY;
      }
    }
    setBoundingBox(minX, minY, maxX, maxY);
  }
}
