
package com.revolsys.geometry.index.kdtree;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.geometry.model.impl.PointDoubleXY;

public class KdNode extends PointDoubleXY {
  private static final long serialVersionUID = 1L;

  private int count;

  private KdNode left;

  private KdNode right;

  public KdNode(final double x, final double y) {
    super(x, y);
    this.left = null;
    this.right = null;
    this.count = 1;
  }

  <N extends KdNode> void forEachNode(final boolean axisX, final double minX, final double minY,
    final double maxX, final double maxY, final Consumer<N> action) {
    final double x = this.x;
    final double y = this.y;

    boolean queryLeft;
    boolean queryRight;
    if (axisX) {
      queryLeft = minX < x;
      queryRight = x <= maxX;
    } else {
      queryLeft = minY < y;
      queryRight = y <= maxY;
    }
    if (this.left != null && queryLeft) {
      this.left.forEachNode(!axisX, minX, minY, maxX, maxY, action);
    }
    if (minX <= x && x <= maxX && minY <= y && y <= maxY) {
      @SuppressWarnings("unchecked")
      final N node = (N)this;
      action.accept(node);
    }
    if (this.right != null && queryRight) {
      this.right.forEachNode(!axisX, minX, minY, maxX, maxY, action);
    }
  }

  <N extends KdNode> void forEachNode(final boolean axisX, final double minX, final double minY,
    final double maxX, final double maxY, final Predicate<? super N> filter,
    final Consumer<N> action) {
    final double x = this.x;
    final double y = this.y;

    boolean queryLeft;
    boolean queryRight;
    if (axisX) {
      queryLeft = minX < x;
      queryRight = x <= maxX;
    } else {
      queryLeft = minY < y;
      queryRight = y <= maxY;
    }
    if (this.left != null && queryLeft) {
      this.left.forEachNode(!axisX, minX, minY, maxX, maxY, filter, action);
    }
    if (minX <= x && x <= maxX && minY <= y && y <= maxY) {
      @SuppressWarnings("unchecked")
      final N node = (N)this;
      if (filter.test(node)) {
        action.accept(node);
      }
    }
    if (this.right != null && queryRight) {
      this.right.forEachNode(!axisX, minX, minY, maxX, maxY, filter, action);
    }
  }

  public int getCount() {
    return this.count;
  }

  public KdNode getLeft() {
    return this.left;
  }

  public KdNode getRight() {
    return this.right;
  }

  void increment() {
    this.count = this.count + 1;
  }

  public boolean isRepeated() {
    return this.count > 1;
  }

  void setLeft(final KdNode left) {
    this.left = left;
  }

  void setRight(final KdNode right) {
    this.right = right;
  }
}
