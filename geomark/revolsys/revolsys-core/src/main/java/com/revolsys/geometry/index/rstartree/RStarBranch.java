package com.revolsys.geometry.index.rstartree;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;

public class RStarBranch<T> extends BoundingBoxDoubleXY implements RStarNode<T> {
  private static final long serialVersionUID = 1L;

  RStarNode<T>[] items;

  int itemCount;

  boolean hasLeaves;

  private double area = Double.NaN;

  @SuppressWarnings("unchecked")
  private RStarBranch(final int capacity) {
    this.items = (RStarNode<T>[])new RStarNode<?>[capacity];
  }

  public RStarBranch(final int capacity, final RStarBranch<T> item1, final RStarBranch<T> item2) {
    this(capacity);
    this.itemCount = 2;
    this.items[0] = item1;
    this.items[1] = item2;
    this.hasLeaves = false;
    recalculateBoundingBox();
  }

  public RStarBranch(final int capacity, final RStarLeaf<T> item) {
    this(capacity);
    setBoundingBox(item.getBoundingBox());
    this.itemCount = 1;
    this.items[0] = item;
    this.hasLeaves = true;
    recalculateBoundingBox();
  }

  public RStarBranch(final RStarBranch<T> node, final int startIndex) {
    this(node.itemCount - startIndex);
    this.hasLeaves = node.hasLeaves;
    this.itemCount = this.items.length;
    System.arraycopy(node.items, startIndex, this.items, 0, this.itemCount);
    recalculateBoundingBox();
  }

  void addItem(final RStarNode<T> item) {
    if (this.itemCount < this.items.length) {
      this.items[this.itemCount] = item;
    } else {
      final RStarNode<T>[] oldItems = this.items;
      @SuppressWarnings("unchecked")
      final RStarNode<T>[] newItems = (RStarNode<T>[])new RStarNode<?>[oldItems.length
        + (oldItems.length >> 1)];
      System.arraycopy(oldItems, 0, newItems, 0, oldItems.length);
      newItems[oldItems.length] = item;
      this.items = newItems;
    }
    this.itemCount++;

  }

  private void addItemsToList(final List<RStarLeaf<T>> itemList) {
    if (this.hasLeaves) {
      final int itemCount = this.itemCount;
      final RStarNode<T>[] items = this.items;
      for (int i = 0; i < itemCount; i++) {
        final RStarLeaf<T> leaf = (RStarLeaf<T>)items[i];
        itemList.add(leaf);
      }
    } else {
      final int itemCount = this.itemCount;
      final RStarNode<T>[] items = this.items;
      for (int i = 0; i < itemCount; i++) {
        final RStarBranch<T> branch = (RStarBranch<T>)items[i];
        branch.addItemsToList(itemList);
      }
    }
  }

  public void expandBoundingBox(final RStarLeaf<T> leaf) {
    expandBbox(leaf);
    this.area = Double.NaN;
  }

  @Override
  public void forEach(final Consumer<? super T> action) {
    final int itemCount = this.itemCount;
    final RStarNode<T>[] items = this.items;
    for (int i = 0; i < itemCount; i++) {
      final RStarNode<T> item = items[i];
      item.forEach(action);
    }
  }

  @Override
  public void forEach(final double x, final double y, final Consumer<? super T> action) {
    if (bboxCovers(x, y)) {
      final int itemCount = this.itemCount;
      final RStarNode<T>[] items = this.items;
      for (int i = 0; i < itemCount; i++) {
        final RStarNode<T> item = items[i];
        item.forEach(x, y, action);
      }
    }
  }

  @Override
  public void forEach(final double minX, final double minY, final double maxX, final double maxY,
    final Consumer<? super T> action) {
    if (bboxIntersects(minX, minY, maxX, maxY)) {
      final int itemCount = this.itemCount;
      final RStarNode<T>[] items = this.items;
      for (int i = 0; i < itemCount; i++) {
        final RStarNode<T> item = items[i];
        item.forEach(minX, minY, maxX, maxY, action);
      }
    }
  }

  public void forEach(final Predicate<RStarBranch<T>> nodeFilter,
    final Predicate<RStarLeaf<T>> leafFilter, final Consumer<RStarLeaf<T>> action) {
    if (this.hasLeaves) {
      final int itemCount = this.itemCount;
      final RStarNode<T>[] items = this.items;
      for (int i = 0; i < itemCount; i++) {
        final RStarLeaf<T> leaf = (RStarLeaf<T>)items[i];
        if (leafFilter.test(leaf)) {
          action.accept(leaf);
        }
      }
    } else {
      final int itemCount = this.itemCount;
      final RStarNode<T>[] items = this.items;
      for (int i = 0; i < itemCount; i++) {
        final RStarBranch<T> branch = (RStarBranch<T>)items[i];
        if (nodeFilter.test(branch)) {
          branch.forEach(nodeFilter, leafFilter, action);
        }
      }
    }
  }

  @Override
  public double getArea() {
    if (!Double.isFinite(this.area)) {
      this.area = super.getArea();
    }
    return this.area;
  }

  public RStarNode<T> getItem(final int index) {
    return this.items[index];
  }

  public int getItemCount() {
    return this.itemCount;
  }

  public RStarBranch<T> getMinimum(final Comparator<RStarNode<T>> comparator) {
    RStarBranch<T> minItem = null;
    final int itemCount = this.itemCount;
    final RStarNode<T>[] items = this.items;
    for (int i = 0; i < itemCount; i++) {
      final RStarBranch<T> branch = (RStarBranch<T>)items[i];
      if (minItem == null) {
        minItem = branch;
      } else if (comparator.compare(minItem, branch) > 0) {
        minItem = branch;
      }

    }
    return minItem;
  }

  public RStarBranch<T> getMinimum(final Comparator<RStarNode<T>> comparator, final int maxCount) {
    RStarBranch<T> minItem = null;
    int count = 0;
    final int itemCount = this.itemCount;
    final RStarNode<T>[] items = this.items;
    for (int i = 0; i < itemCount; i++) {
      if (count > maxCount) {
        return minItem;
      }
      final RStarBranch<T> branch = (RStarBranch<T>)items[i];
      if (minItem == null) {
        minItem = branch;
      } else if (comparator.compare(minItem, branch) > 0) {
        minItem = branch;
      }
      count++;
    }
    return minItem;
  }

  public boolean isHasLeaves() {
    return this.hasLeaves;
  }

  public void recalculateBoundingBox() {
    clear();
    final int itemCount = this.itemCount;
    final RStarNode<T>[] items = this.items;
    for (int i = 0; i < itemCount; i++) {
      final RStarNode<T> item = items[i];
      expandBbox(item);
    }
  }

  public boolean remove(final RStarTree<T> tree, final BoundingBox boundingBox,
    final Predicate<RStarLeaf<T>> leafRemoveFilter, final List<RStarLeaf<T>> itemsToReinsert,
    final boolean isRoot) {

    if (bboxIntersects(boundingBox)) {
      boolean removed = false;
      final RStarNode<T>[] items = this.items;
      for (int i = 0; i < this.itemCount; i++) {
        final RStarNode<T> item = items[i];
        if (boundingBox.bboxIntersects(boundingBox)) {
          if (this.hasLeaves) {
            final RStarLeaf<T> leaf = (RStarLeaf<T>)item;
            if (leafRemoveFilter.test(leaf)) {
              removeItem(i);
              tree.size--;
              removed = true;
            }
          } else {
            final RStarBranch<T> branch = (RStarBranch<T>)item;
            if (branch.remove(tree, boundingBox, leafRemoveFilter, itemsToReinsert, false)) {
              removeItem(i);
              removed = true;
            }
          }
        }

      }

      return removed && removeIfEmpty(tree, isRoot, itemsToReinsert);
    }

    return false;
  }

  public boolean remove(final RStarTree<T> tree, final Predicate<RStarLeaf<T>> leafRemoveFilter,
    final List<RStarLeaf<T>> itemsToReinsert, final boolean isRoot) {

    // this is the easy part: remove nodes if they need to be removed
    if (this.hasLeaves) {
      final int itemCount = this.itemCount;
      final RStarNode<T>[] items = this.items;
      for (int i = 0; i < itemCount; i++) {
        final RStarLeaf<T> leaf = (RStarLeaf<T>)items[i];
        if (leafRemoveFilter.test(leaf)) {
          removeItem(i);
          tree.size--;
        }

      }
    } else {
      final int itemCount = this.itemCount;
      final RStarNode<T>[] items = this.items;
      for (int i = 0; i < itemCount; i++) {
        final RStarBranch<T> node = (RStarBranch<T>)items[i];
        if (node.remove(tree, leafRemoveFilter, itemsToReinsert, false)) {
          removeItem(i);
        }

      }
    }

    return removeIfEmpty(tree, isRoot, itemsToReinsert);
  }

  protected boolean removeIfEmpty(final RStarTree<T> tree, final boolean isRoot,
    final List<RStarLeaf<T>> itemsToReinsert) {
    if (isRoot) {
      if (this.itemCount == 0) {
        this.hasLeaves = true;
        clear();
      }
      return false;
    } else {
      if (this.itemCount == 0) {
        return true;
      } else if (this.itemCount < tree.getNodeMinItemCount()) {
        addItemsToList(itemsToReinsert);
        return true;
      } else {
        return false;
      }
    }
  }

  public void removeItem(final int index) {
    final RStarNode<T>[] items = this.items;
    System.arraycopy(items, 0, items, 0, index);
    System.arraycopy(items, index + 1, items, index, this.itemCount - index - 1);
    this.itemCount--;
    items[this.itemCount] = null;
  }

  public void setSize(final int size) {
    this.itemCount = size;
    Arrays.fill(this.items, size, this.items.length, null);
    recalculateBoundingBox();
  }

  public void sortItems(final Comparator<RStarNode<T>> comparator) {
    Arrays.sort(this.items, 0, this.itemCount, comparator);
  }
}
