package com.revolsys.geometry.index.quadtree;

import java.util.function.Consumer;

public class IdObjectNode<T> extends AbstractQuadTreeNode<T> {
  private static final long serialVersionUID = 1L;

  private Object[] ids = new Object[0];

  private int itemCount = 0;

  public IdObjectNode() {
  }

  public IdObjectNode(final int level, final double minX, final double minY, final double maxX,
    final double maxY) {
    super(level, minX, minY, maxX, maxY);
  }

  @Override
  protected boolean add(final QuadTree<T> tree, final double minX, final double minY,
    final double maxX, final double maxY, final T item) {
    synchronized (this.nodes) {
      final IdObjectQuadTree<T> idObjectTree = (IdObjectQuadTree<T>)tree;
      final Object id = idObjectTree.getId(item);
      {
        final Object[] ids = this.ids;
        final int itemCount = this.itemCount;
        for (int i = 0; i < itemCount; i++) {
          final Object oldId = ids[i];
          if (oldId.equals(id)) {
            ids[i] = item;
            return false;
          }
          i++;
        }
      }
      this.itemCount++;
      final int oldLength = this.ids.length;
      if (oldLength < this.itemCount) {
        final Object[] newIds = new Object[this.itemCount];
        System.arraycopy(this.ids, 0, newIds, 0, oldLength);
        this.ids = newIds;
      }
      this.ids[this.itemCount - 1] = id;
      return true;
    }
  }

  @Override
  protected void forEachItem(final QuadTree<T> tree, final Consumer<? super T> action) {
    final IdObjectQuadTree<T> idObjectTree = (IdObjectQuadTree<T>)tree;
    synchronized (this.nodes) {
      final Object[] ids = this.ids;
      final int itemCount = this.itemCount;
      for (int i = 0; i < itemCount; i++) {
        final Object id = ids[i];
        final T item = idObjectTree.getItem(id);
        action.accept(item);
      }
    }
  }

  @Override
  protected void forEachItem(final QuadTree<T> tree, final double x, final double y,
    final Consumer<? super T> action) {
    synchronized (this.nodes) {
      final IdObjectQuadTree<T> idObjectTree = (IdObjectQuadTree<T>)tree;
      final Object[] ids = this.ids;
      final int itemCount = this.itemCount;
      for (int i = 0; i < itemCount; i++) {
        final Object id = ids[i];
        if (idObjectTree.intersectsBounds(id, x, y)) {
          final T item = idObjectTree.getItem(id);
          action.accept(item);
        }
      }
    }
  }

  @Override
  protected void forEachItem(final QuadTree<T> tree, final double minX, final double minY,
    final double maxX, final double maxY, final Consumer<? super T> action) {
    synchronized (this.nodes) {
      final IdObjectQuadTree<T> idObjectTree = (IdObjectQuadTree<T>)tree;
      final Object[] ids = this.ids;
      final int itemCount = this.itemCount;
      for (int i = 0; i < itemCount; i++) {
        final Object id = ids[i];
        if (idObjectTree.intersectsBounds(id, minX, minY, maxX, maxY)) {
          final T item = idObjectTree.getItem(id);
          action.accept(item);
        }
      }
    }
  }

  @Override
  public int getItemCount() {
    return this.itemCount;
  }

  @Override
  protected AbstractQuadTreeNode<T> newNode(final int level, final double minX, final double minY,
    final double maxX, final double maxY) {
    return new IdObjectNode<>(level, minX, minY, maxX, maxY);
  }

  @Override
  protected boolean removeItem(final QuadTree<T> tree, final T item) {
    synchronized (this.nodes) {
      boolean removed = false;
      final IdObjectQuadTree<T> idObjectTree = (IdObjectQuadTree<T>)tree;
      final Object id = idObjectTree.getId(item);
      int index = 0;
      for (final Object oldId : this.ids) {
        if (index >= this.itemCount) {
          break;
        } else if (id.equals(oldId)) {
          if (index < this.itemCount - 1) {
            final int copyCount = this.itemCount - index - 1;
            System.arraycopy(this.ids, index + 1, this.ids, index, copyCount);
          }
          this.itemCount--;
          this.ids[this.itemCount] = null;
          removed = true;
        }
        index++;
      }
      return removed;
    }
  }
}
