package com.revolsys.geometry.index.strtree;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.Consumer;

import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.util.Emptyable;

public class StrTreeNode<I> extends BoundingBoxDoubleXY
  implements Emptyable, Boundable<I>, Serializable {
  private static final long serialVersionUID = 1L;

  protected Boundable<I>[] children;

  protected int childCount;

  private final int level;

  /**
   * Constructs an StrTreeNode at the given level in the tree
   * @param level 0 if this node is a leaf, 1 if a parent of a leaf, and so on; the
   * root node will have the highest level
   */
  @SuppressWarnings("unchecked")
  public StrTreeNode(final int nodeCapacity, final int level) {
    this.children = new Boundable[nodeCapacity];
    this.level = level;
  }

  /**
   * Adds either an AbstractNode, or if this is a leaf node, a data object
   * (wrapped in an StrTreeLeaf)
   */
  public void addChild(final Boundable<I> child) {
    this.children[this.childCount++] = child;
  }

  /**
   * @param level -1 to get items
   */
  @Override
  public void boundablesAtLevel(final int level, final Collection<Boundable<I>> boundables) {
    if (getLevel() == level) {
      boundables.add(this);
    } else {
      final int childCount = this.childCount;
      final Boundable<I>[] children = this.children;
      for (int i = 0; i < childCount; i++) {
        final Boundable<I> child = children[i];
        child.boundablesAtLevel(level, boundables);
      }
    }
  }

  protected void computeBounds() {
    clear();
    final int childCount = this.childCount;
    final Boundable<I>[] children = this.children;
    for (int i = 0; i < childCount; i++) {
      final Boundable<I> child = children[i];
      if (child instanceof StrTreeNode) {
        final StrTreeNode<I> childNode = (StrTreeNode<I>)child;
        childNode.computeBounds();
      }
      final double minX = child.getMinX();
      final double minY = child.getMinY();
      final double maxX = child.getMaxX();
      final double maxY = child.getMaxY();
      expandBbox(minX, minY, maxX, maxY);
    }
  }

  /**
   * Gets the count of the {@link Boundable}s at this node.
   *
   * @return the count of boundables at this node
   */
  @Override
  public int getChildCount() {
    return this.childCount;
  }

  @Override
  public Boundable<I>[] getChildren() {
    return this.children;
  }

  @Override
  public int getDepth() {
    int maxChildDepth = 0;
    final int childCount = this.childCount;
    final Boundable<I>[] children = this.children;
    for (int i = 0; i < childCount; i++) {
      final Boundable<I> child = children[i];
      final int childDepth = child.getDepth();
      if (childDepth > maxChildDepth) {
        maxChildDepth = childDepth;
      }
    }
    return maxChildDepth + 1;
  }

  @Override
  public int getItemCount() {
    int itemCount = 0;
    final int childCount = this.childCount;
    final Boundable<I>[] children = this.children;
    for (int i = 0; i < childCount; i++) {
      final Boundable<I> child = children[i];
      itemCount += child.getItemCount();
    }
    return itemCount;
  }

  /**
   * Returns 0 if this node is a leaf, 1 if a parent of a leaf, and so on; the
   * root node will have the highest level
   */
  public int getLevel() {
    return this.level;
  }

  /**
   * Tests whether there are any {@link Boundable}s at this node.
   *
   * @return true if there are boundables at this node
   */
  @Override
  public boolean isEmpty() {
    return this.childCount == 0;
  }

  @Override
  public boolean isNode() {
    return true;
  }

  @Override
  public void query(final double minX, final double minY, final double maxX, final double maxY,
    final Consumer<? super I> action) {
    if (bboxIntersects(minX, minY, maxX, maxY)) {
      final int childCount = this.childCount;
      final Boundable<I>[] children = this.children;
      for (int i = 0; i < childCount; i++) {
        final Boundable<I> child = children[i];
        child.query(minX, minY, maxX, maxY, action);
      }
    }
  }
}
