package com.revolsys.geometry.index.quadtree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.revolsys.geometry.util.RectangleUtil;

public class QuadTreeNode<T> extends AbstractQuadTreeNode<T> {
  private static final long serialVersionUID = 1L;

  private final List<double[]> boundingBoxes = new ArrayList<>();

  private final List<T> items = new ArrayList<>();

  public QuadTreeNode() {
  }

  public QuadTreeNode(final int level, final double minX, final double minY, final double maxX,
    final double maxY) {
    super(level, minX, minY, maxX, maxY);
  }

  @Override
  protected boolean add(final QuadTree<T> tree, final double minX, final double minY,
    final double maxX, final double maxY, final T item) {
    synchronized (this.nodes) {
      final double[] bounds = new double[] {
        minX, minY, maxX, maxY
      };
      int i = 0;

      final List<T> items = this.items;
      for (final T oldItem : items) {
        if (tree.equalsItem(item, oldItem)) {
          this.boundingBoxes.set(i, bounds);
          items.set(i, item);
          return false;
        }
        i++;
      }
      this.boundingBoxes.add(bounds);
      items.add(item);
      return true;
    }
  }

  @Override
  protected void forEachItem(final QuadTree<T> tree, final Consumer<? super T> action) {
    synchronized (this.nodes) {
      for (final T item : this.items) {
        action.accept(item);
      }
    }
  }

  @Override
  protected void forEachItem(final QuadTree<T> tree, final double x, final double y,
    final Consumer<? super T> action) {
    synchronized (this.nodes) {

      int i = 0;
      for (final double[] itemBounds : this.boundingBoxes) {
        if (RectangleUtil.intersectsPoint(itemBounds[0], itemBounds[1], itemBounds[2],
          itemBounds[3], x, y)) {
          final T item = this.items.get(i);
          action.accept(item);
        }
        i++;
      }
    }
  }

  @Override
  protected void forEachItem(final QuadTree<T> tree, final double minX, final double minY,
    final double maxX, final double maxY, final Consumer<? super T> action) {
    synchronized (this.nodes) {
      int i = 0;
      for (final double[] itemBounds : this.boundingBoxes) {
        if (RectangleUtil.intersects(itemBounds[0], itemBounds[1], itemBounds[2], itemBounds[3],
          minX, minY, maxX, maxY)) {
          final T item = this.items.get(i);
          action.accept(item);
        }
        i++;
      }
    }
  }

  @Override
  public int getItemCount() {
    return this.items.size();
  }

  @Override
  protected AbstractQuadTreeNode<T> newNode(final int level, final double minX, final double minY,
    final double maxX, final double maxY) {
    return new QuadTreeNode<>(level, minX, minY, maxX, maxY);
  }

  @Override
  protected boolean removeItem(final QuadTree<T> tree, final T item) {
    synchronized (this.nodes) {
      int i = 0;
      for (final T oldItem : this.items) {
        if (tree.equalsItem(item, oldItem)) {
          this.items.remove(i);
          this.boundingBoxes.remove(i);
          return true;
        }
        i++;
      }
      return false;
    }
  }

}
