package com.revolsys.geometry.index.quadtree;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.Consumer;

import com.revolsys.geometry.index.DoubleBits;
import com.revolsys.geometry.index.IntervalSize;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.util.RectangleUtil;
import com.revolsys.util.Emptyable;

public abstract class AbstractQuadTreeNode<T> implements Emptyable, Serializable {
  private static final long serialVersionUID = 1L;

  private final int level;

  private final double centreX;

  private final double centreY;

  private final double maxX;

  private final double maxY;

  private final double minX;

  private final double minY;

  @SuppressWarnings("unchecked")
  protected final AbstractQuadTreeNode<T>[] nodes = new AbstractQuadTreeNode[4];

  protected AbstractQuadTreeNode() {
    this(Integer.MIN_VALUE, 0, 0, 0, 0);
  }

  public AbstractQuadTreeNode(final int level, final double minX, final double minY,
    final double maxX, final double maxY) {
    this.level = level;
    this.minX = minX;
    this.minY = minY;
    this.maxX = maxX;
    this.maxY = maxY;
    this.centreX = (this.minX + this.maxX) / 2;
    this.centreY = (this.minY + this.maxY) / 2;
  }

  protected abstract boolean add(final QuadTree<T> tree, final double minX, final double minY,
    final double maxX, final double maxY, final T item);

  public void clear() {
    Arrays.fill(this.nodes, null);
  }

  private boolean coversBoundingBox(final double minX, final double minY, final double maxX,
    final double maxY) {
    return this.minX <= minX && maxX <= this.maxX && this.minY <= minY && maxY <= this.maxY;
  }

  public int depth() {
    int depth = 0;
    for (final AbstractQuadTreeNode<T> node : this.nodes) {
      if (node != null) {
        final int nodeDepth = node.depth();
        if (nodeDepth > depth) {
          depth = nodeDepth;
        }
      }
    }
    return depth + 1;
  }

  protected AbstractQuadTreeNode<T> find(final double minX, final double minY, final double maxX,
    final double maxY) {
    final int subnodeIndex = getSubnodeIndex(minX, minY, maxX, maxY);
    if (subnodeIndex == -1) {
      return this;
    }
    final AbstractQuadTreeNode<T> node = this.nodes[subnodeIndex];
    if (node != null) {
      return node.find(minX, minY, maxX, maxY);
    }
    return this;
  }

  protected void forEach(final QuadTree<T> tree, final Consumer<? super T> action) {
    forEachItem(tree, action);

    for (final AbstractQuadTreeNode<T> node : this.nodes) {
      if (node != null) {
        node.forEach(tree, action);
      }
    }
  }

  protected void forEach(final QuadTree<T> tree, final double x, final double y,
    final Consumer<? super T> action) {
    if (isSearchMatch(x, y)) {
      forEachItem(tree, x, y, action);
      for (final AbstractQuadTreeNode<T> node : this.nodes) {
        if (node != null) {
          node.forEach(tree, x, y, action);
        }
      }
    }
  }

  protected void forEach(final QuadTree<T> tree, final double minX, final double minY,
    final double maxX, final double maxY, final Consumer<? super T> action) {
    if (isSearchMatch(minX, minY, maxX, maxY)) {
      forEachItem(tree, minX, minY, maxX, maxY, action);
      for (final AbstractQuadTreeNode<T> node : this.nodes) {
        if (node != null) {
          node.forEach(tree, minX, minY, maxX, maxY, action);
        }
      }
    }
  }

  protected abstract void forEachItem(final QuadTree<T> tree, final Consumer<? super T> action);

  protected void forEachItem(final QuadTree<T> tree, final double x, final double y,
    final Consumer<? super T> action) {
    forEachItem(tree, x, y, x, y, action);
  }

  protected abstract void forEachItem(final QuadTree<T> tree, final double minX, double minY,
    double maxX, double maxY, final Consumer<? super T> action);

  public abstract int getItemCount();

  private AbstractQuadTreeNode<T> getNode(final double minX, final double minY, final double maxX,
    final double maxY) {
    final int subnodeIndex = getSubnodeIndex(minX, minY, maxX, maxY);
    if (subnodeIndex == -1) {
      return this;
    } else {
      final AbstractQuadTreeNode<T> node = getSubnode(subnodeIndex);
      return node.getNode(minX, minY, maxX, maxY);
    }
  }

  private AbstractQuadTreeNode<T> getSubnode(final int index) {
    final AbstractQuadTreeNode<T>[] nodes = this.nodes;
    final AbstractQuadTreeNode<T> node = nodes[index];
    if (node == null) {
      final AbstractQuadTreeNode<T> newNode = newSubnode(index);
      nodes[index] = newNode;
      return newNode;
    }
    return node;
  }

  private int getSubnodeIndex(final double minX, final double minY, final double maxX,
    final double maxY) {
    int subnodeIndex = -1;
    if (minX >= this.centreX) {
      if (minY >= this.centreY) {
        subnodeIndex = 3;
      }
      if (maxY <= this.centreY) {
        subnodeIndex = 1;
      }
    }
    if (maxX <= this.centreX) {
      if (minY >= this.centreY) {
        subnodeIndex = 2;
      }
      if (maxY <= this.centreY) {
        subnodeIndex = 0;
      }
    }
    return subnodeIndex;
  }

  private boolean hasChildren() {
    for (final AbstractQuadTreeNode<T> node : this.nodes) {
      if (node != null) {
        return true;
      }
    }
    return false;
  }

  private boolean hasItems() {
    return getItemCount() > 0;
  }

  private boolean insertContained(final QuadTree<T> tree, final double minX, final double minY,
    final double maxX, final double maxY, final T item) {
    final boolean isZeroX = IntervalSize.isZeroWidth(maxX, minX);
    final boolean isZeroY = IntervalSize.isZeroWidth(maxY, minY);
    AbstractQuadTreeNode<T> node;
    if (isZeroX || isZeroY) {
      node = find(minX, minY, maxX, maxY);
    } else {
      node = getNode(minX, minY, maxX, maxY);
    }
    return node.add(tree, minX, minY, maxX, maxY, item);
  }

  private void insertNode(final AbstractQuadTreeNode<T> node) {
    final int index = getSubnodeIndex(node.minX, node.minY, node.maxX, node.maxY);
    if (node.level == this.level - 1) {
      this.nodes[index] = node;
    } else {
      final AbstractQuadTreeNode<T> childNode = newSubnode(index);
      childNode.insertNode(node);
      this.nodes[index] = childNode;
    }
  }

  protected boolean insertRoot(final QuadTree<T> tree, final double minX, final double minY,
    final double maxX, final double maxY, final T item) {
    final int index = getSubnodeIndex(minX, minY, maxX, maxY);
    if (index == -1) {
      return add(tree, minX, minY, maxX, maxY, item);
    } else {
      AbstractQuadTreeNode<T> node = this.nodes[index];
      if (node == null) {
        final AbstractQuadTreeNode<T> newNode = newNode(minX, minY, maxX, maxY);
        this.nodes[index] = newNode;
        node = newNode;
      } else if (!node.coversBoundingBox(minX, minY, maxX, maxY)) {
        final AbstractQuadTreeNode<T> newNode = node.newNodeExpanded(minX, minY, maxX, maxY);
        this.nodes[index] = newNode;
        node = newNode;
      }
      return node.insertContained(tree, minX, minY, maxX, maxY, item);
    }
  }

  @Override
  public boolean isEmpty() {
    final boolean isEmpty = !hasItems();
    for (final AbstractQuadTreeNode<T> node : this.nodes) {
      if (node != null) {
        if (!node.isEmpty()) {
          return false;
        }
      }
    }
    return isEmpty;
  }

  private boolean isPrunable() {
    return !(hasChildren() || hasItems());
  }

  private boolean isRoot() {
    return this.level == Integer.MIN_VALUE;
  }

  private boolean isSearchMatch(final double x, final double y) {
    if (isRoot()) {
      return true;
    } else {
      return this.minX <= x && x <= this.maxX && this.minY <= y && y <= this.maxY;
    }
  }

  private boolean isSearchMatch(final double minX, final double minY, final double maxX,
    final double maxY) {
    if (isRoot()) {
      return true;
    } else {
      return !(minX > this.maxX || maxX < this.minX || minY > this.maxY || maxY < this.minY);
    }
  }

  private AbstractQuadTreeNode<T> newNode(final double minX, final double minY, final double maxX,
    final double maxY) {
    final double dx = maxX - minX;
    final double dy = maxY - minY;
    final double dMax = dx > dy ? dx : dy;
    int level = DoubleBits.exponent(dMax) + 1;

    double quadSize = DoubleBits.powerOf2(level);
    double newMinX = Math.floor(minX / quadSize) * quadSize;
    double newMinY = Math.floor(minY / quadSize) * quadSize;
    double newMaxX = newMinX + quadSize;
    double newMaxY = newMinY + quadSize;

    while (!RectangleUtil.covers(newMinX, newMinY, newMaxX, newMaxY, minX, minY, maxX, maxY)) {
      level++;
      quadSize = DoubleBits.powerOf2(level);
      newMinX = Math.floor(minX / quadSize) * quadSize;
      newMinY = Math.floor(minY / quadSize) * quadSize;
      newMaxX = newMinX + quadSize;
      newMaxY = newMinY + quadSize;
    }

    return newNode(level, newMinX, newMinY, newMaxX, newMaxY);
  }

  protected abstract AbstractQuadTreeNode<T> newNode(int level, double minX, final double minY,
    double maxX, final double maxY);

  private AbstractQuadTreeNode<T> newNodeExpanded(double minX, double minY, double maxX,
    double maxY) {
    if (this.minX < minX) {
      minX = this.minX;
    }
    if (this.maxX > maxX) {
      maxX = this.maxX;
    }
    if (this.minY < minY) {
      minY = this.minY;
    }
    if (this.maxY > maxY) {
      maxY = this.maxY;
    }
    final AbstractQuadTreeNode<T> newNode = newNode(minX, minY, maxX, maxY);
    newNode.insertNode(this);
    return newNode;
  }

  private AbstractQuadTreeNode<T> newSubnode(final int index) {
    // Construct a new new subquad in the appropriate quadrant

    double minX = 0.0;
    double maxX = 0.0;
    double minY = 0.0;
    double maxY = 0.0;

    final double centreX = this.centreX;
    final double centreY = this.centreY;
    switch (index) {
      case 0:
        minX = this.minX;
        maxX = centreX;
        minY = this.minY;
        maxY = centreY;
      break;
      case 1:
        minX = centreX;
        maxX = this.maxX;
        minY = this.minY;
        maxY = centreY;
      break;
      case 2:
        minX = this.minX;
        maxX = centreX;
        minY = centreY;
        maxY = this.maxY;
      break;
      case 3:
        minX = centreX;
        maxX = this.maxX;
        minY = centreY;
        maxY = this.maxY;
      break;
    }
    final AbstractQuadTreeNode<T> node = newNode(this.level - 1, minX, minY, maxX, maxY);
    return node;
  }

  protected boolean removeItem(final QuadTree<T> tree, final double minX, final double minY,
    final double maxX, final double maxY, final T item) {
    boolean removed = false;
    if (isSearchMatch(minX, minY, maxX, maxY)) {
      final AbstractQuadTreeNode<T>[] nodes = this.nodes;
      final int nodeCount = nodes.length;
      for (int i = 0; i < nodeCount; i++) {
        final AbstractQuadTreeNode<T> node = nodes[i];
        if (node != null) {
          if (node.removeItem(tree, minX, minY, maxX, maxY, item)) {
            if (node.isPrunable()) {
              nodes[i] = null;
            }
            removed = true;
          }
        }
      }
      removed |= removeItem(tree, item);
    }
    return removed;
  }

  protected abstract boolean removeItem(final QuadTree<T> tree, final T item);

  @Override
  public String toString() {
    return this.level + " " + BoundingBox.bboxToWkt(this.minX, this.minY, this.maxX, this.maxY)
      + " " + getItemCount();
  }
}
