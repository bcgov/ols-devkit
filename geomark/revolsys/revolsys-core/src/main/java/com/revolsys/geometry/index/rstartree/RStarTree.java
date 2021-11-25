package com.revolsys.geometry.index.rstartree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import com.revolsys.collection.ArrayUtil;
import com.revolsys.geometry.index.SpatialIndex;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.editor.BoundingBoxEditor;
import com.revolsys.util.ExitLoopException;

public class RStarTree<T> implements SpatialIndex<T> {

  private abstract class BoundingBoxComparator implements Comparator<RStarNode<T>> {
    BoundingBox boundingBox;

    protected Comparator<RStarNode<T>> comparator(final BoundingBox boundingBox) {
      this.boundingBox = boundingBox;
      return this;
    }
  }

  private static final int RTREE_CHOOSE_SUBTREE_P = 32;

  private static final double RTREE_REINSERT_P = 0.3;

  private RStarBranch<T> root;

  int size;

  private int nodeMinItemCount = 32;

  private int nodeMaxItemCount = 64;

  private BiPredicate<T, T> equalsItemFunction = (item1, item2) -> item1 == item2;

  private GeometryFactory geometryFactory;

  private final Comparator<RStarNode<T>>[] sortMin = ArrayUtil.newArray(sortBoundingBoxByMin(0),
    sortBoundingBoxByMin(1));

  private final Comparator<RStarNode<T>>[] sortMax = ArrayUtil.newArray(sortBoundingBoxByMax(0),
    sortBoundingBoxByMax(1));

  private final BoundingBoxComparator sortAreaEnlargement = new BoundingBoxComparator() {

    double area;

    @Override
    protected Comparator<RStarNode<T>> comparator(final BoundingBox boundingBox) {
      this.area = boundingBox.getArea();
      return super.comparator(boundingBox);
    }

    @Override
    public int compare(final RStarNode<T> bi1, final RStarNode<T> bi2) {
      final double value1 = this.area - bi1.getArea();
      final double value2 = this.area - bi2.getArea();
      return Double.compare(value1, value2);
    }
  };

  private final BoundingBoxComparator sortOverlapEnlargement = new BoundingBoxComparator() {
    @Override
    public int compare(final RStarNode<T> bi1, final RStarNode<T> bi2) {
      final double value1 = bi1.getBoundingBox().overlappingArea(this.boundingBox);
      final double value2 = bi2.getBoundingBox().overlappingArea(this.boundingBox);
      return Double.compare(value1, value2);
    }
  };

  private final BoundingBoxComparator sortCentreDistance = new BoundingBoxComparator() {
    private double centreX;

    private double centreY;

    @Override
    protected Comparator<RStarNode<T>> comparator(final BoundingBox boundingBox) {
      this.boundingBox = boundingBox;
      this.centreX = boundingBox.getCentreX();
      this.centreY = boundingBox.getCentreY();
      return super.comparator(boundingBox);
    }

    @Override
    public int compare(final RStarNode<T> bi1, final RStarNode<T> bi2) {
      final double value1 = bi1.getBoundingBox().distanceFromCenter(this.centreX, this.centreY);
      final double value2 = bi2.getBoundingBox().distanceFromCenter(this.centreX, this.centreY);
      return Double.compare(value1, value2);
    }
  };

  public RStarTree() {
  }

  public RStarTree(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  // choose subtree: only pass this items that do not have leaves
  // I took out the loop portion of this algorithm, so it only
  // picks a subtree at that particular level
  private RStarBranch<T> chooseSubtree(final RStarBranch<T> node,
    final BoundingBoxProxy boundProxy) {
    final BoundingBox bound = boundProxy.getBoundingBox();
    // If the child pointers in N point to leaves
    if (((RStarBranch<?>)node.getItem(0)).isHasLeaves()) {
      // determine the minimum overlap cost
      if (this.nodeMaxItemCount > RTREE_CHOOSE_SUBTREE_P * 2 / 3
        && node.getItemCount() > RTREE_CHOOSE_SUBTREE_P) {
        // alternative algorithm:
        // Sort the rectangles in N in increasing order of
        // then area enlargement needed to include the new
        // data rectangle

        // Let A be the group of the first p entries
        node.sortItems(this.sortAreaEnlargement.comparator(bound));

        // From the items in A, considering all items in
        // N, choose the leaf whose rectangle needs least
        // overlap enlargement

        return node.getMinimum(this.sortOverlapEnlargement.comparator(bound),
          RTREE_CHOOSE_SUBTREE_P);
      }
      return node.getMinimum(this.sortOverlapEnlargement.comparator(bound));
    }
    return node.getMinimum(this.sortAreaEnlargement.comparator(bound));
  }

  @Override
  public void clear() {
    this.root = null;
    this.size = 0;

  }

  protected boolean equalsItem(final RStarLeaf<T> leaf, final T item) {
    final boolean equalsItemDo = leaf.getItem() == item;
    return equalsItemDo;
  }

  @Override
  public boolean forEach(final Consumer<? super T> action) {
    if (this.root != null) {
      try {
        this.root.forEach(action);
      } catch (final ExitLoopException e) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean forEach(final double x, final double y, final Consumer<? super T> action) {
    if (this.root != null) {
      try {
        this.root.forEach(x, y, action);
      } catch (final ExitLoopException e) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean forEach(final double minX, final double minY, final double maxX, final double maxY,
    final Consumer<? super T> action) {
    if (this.root != null) {
      try {
        this.root.forEach(minX, minY, maxX, maxY, action);
      } catch (final ExitLoopException e) {
        return false;
      }
    }
    return true;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  int getNodeMaxItemCount() {
    return this.nodeMaxItemCount;
  }

  int getNodeMinItemCount() {
    return this.nodeMinItemCount;
  }

  @Override
  public int getSize() {
    return this.size;
  }

  // inserts nodes recursively. As an optimization, the algorithm steps are
  // way out of order. :) If this returns something, then that item should
  // be added to the caller's level of the tree
  RStarBranch<T> insertInternal(final RStarLeaf<T> leaf, final RStarBranch<T> node,
    final boolean firstInsert) {
    node.expandBoundingBox(leaf);

    // CS2: If we're at a leaf, then use that level
    if (node.hasLeaves) {
      // I2: If N has less than M items, accommodate E in N
      node.addItem(leaf);
    } else {
      // I1: Invoke ChooseSubtree. with the level as a parameter,
      // to find an appropriate node N, m which to place the
      // new leaf E

      // of course, this already does all of that recursively. we just need to
      // determine whether we need to split the overflow or not
      final RStarBranch<T> tmp_node = insertInternal(leaf, chooseSubtree(node, leaf), firstInsert);

      if (tmp_node == null) {
        return null;
      }

      // this gets joined to the list of items at this level
      node.addItem(tmp_node);
    }

    if (node.getItemCount() > this.nodeMaxItemCount) {
      if (node != this.root && firstInsert) {
        reinsert(node);
        return null;
      } else {

        final RStarBranch<T> splitItem = split(node);

        if (node == this.root) {
          final RStarBranch<T> newRoot = new RStarBranch<>(this.nodeMaxItemCount, this.root,
            splitItem);
          this.root = newRoot;
          return null;
        } else {
          return splitItem;
        }
      }
    } else {
      return null;
    }
  }

  @Override
  public void insertItem(final BoundingBox boundingBox, final T item) {
    final RStarLeaf<T> newLeaf = new RStarLeaf<>(item, boundingBox);
    if (this.root == null) {
      this.root = new RStarBranch<>(this.nodeMinItemCount, newLeaf);
    } else {
      insertInternal(newLeaf, this.root, true);
    }
    this.size += 1;
  }

  private void insertLeaves(final List<RStarLeaf<T>> leaves) {
    if (!leaves.isEmpty()) {
      for (final RStarLeaf<T> leaf : leaves) {
        insertInternal(leaf, this.root, true);
      }
    }
  }

  // This routine is used to do the opportunistic reinsertion that the
  // R algorithm calls for
  @SuppressWarnings("unchecked")
  private void reinsert(final RStarBranch<T> node) {

    final int itemCount = node.getItemCount();
    int keePItemCount = (int)(itemCount * RTREE_REINSERT_P);
    if (keePItemCount <= 0) {
      keePItemCount = 1;
    }

    node.sortItems(this.sortCentreDistance.comparator(node.getBoundingBox()));

    final List<RStarNode<T>> removedItems = new ArrayList<>();
    for (int i = keePItemCount; i < itemCount; i++) {
      removedItems.add(node.getItem(i));
    }
    node.setSize(keePItemCount);

    node.recalculateBoundingBox();

    for (final BoundingBoxProxy item : removedItems) {
      final RStarLeaf<T> leaf = (RStarLeaf<T>)item;
      insertInternal(leaf, this.root, false);
    }
  }

  @Override
  public boolean removeItem(final BoundingBox boundingBox, final T item) {
    final List<RStarLeaf<T>> itemsToReinsert = new ArrayList<>();
    this.root.remove(this, boundingBox, leaf -> {
      if (leaf.getBoundingBox().equals(boundingBox)) {
        final T leafItem = leaf.getItem();
        if (leafItem == null) {
          return item == null;
        } else if (item != null) {
          return this.equalsItemFunction.test(leafItem, item);
        }
      }
      return false;
    }, itemsToReinsert, true);
    insertLeaves(itemsToReinsert);
    return true;
  }

  /**
   * Remove items covered by the bounding box
   *
   * @param boundingBox
   */
  public void removeItems(final BoundingBox boundingBox) {
    if (this.root != null) {
      final List<RStarLeaf<T>> itemsToReinsert = new ArrayList<>();
      this.root.remove(this, boundingBox, leaf -> boundingBox.bboxCovers(leaf), itemsToReinsert,
        true);
      insertLeaves(itemsToReinsert);
    }
  }

  public RStarTree<T> setEqualsItemFunction(final BiPredicate<T, T> equalsItemFunction) {
    this.equalsItemFunction = equalsItemFunction;
    return this;
  }

  @Override
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public RStarTree<T> setNodeMaxItemCount(final int nodeMaxItemCount) {
    if (this.size == 0) {
      this.nodeMaxItemCount = nodeMaxItemCount;
    } else {
      throw new IllegalStateException("Cannot set nodeMaxItemCount after items have been added");
    }
    return this;
  }

  public RStarTree<T> setNodeMinItemCount(final int nodeMinItemCount) {
    if (this.size == 0) {
      this.nodeMinItemCount = nodeMinItemCount;
    } else {
      throw new IllegalStateException("Cannot set nodeMinItemCount after items have been added");
    }
    return this;
  }

  private Comparator<RStarNode<T>> sortBoundingBoxByMax(final int axis) {
    return (bi1, bi2) -> {
      final double value1 = bi1.getBoundingBox().getMax(axis);
      final double value2 = bi2.getBoundingBox().getMax(axis);
      return Double.compare(value1, value2);
    };
  }

  private Comparator<RStarNode<T>> sortBoundingBoxByMin(final int axis) {
    return (bi1, bi2) -> {
      final double value1 = bi1.getBoundingBox().getMin(axis);
      final double value2 = bi2.getBoundingBox().getMin(axis);
      return Double.compare(value1, value2);
    };
  }

  // this combines Split, ChooseSplitAxis, and ChooseSplitIndex into
  // one function as an optimization (they all share data structures,
  // so it would be pointless to do all of that copying)
  //
  // This returns a node, which should be added to the items of the
  // passed node's parent
  private RStarBranch<T> split(final RStarBranch<T> node) {

    final int n_items = node.getItemCount();
    final int distribution_count = n_items - 2 * this.nodeMinItemCount + 1;

    int splitAxis = 2 + 1;
    int splitEdge = 0;
    int splitIndex = 0;
    int splitMargin = 0;

    // S1: Invoke ChooseSplitAxis to determine the axis,
    // perpendicular to which the split 1s performed
    // S2: Invoke ChooseSplitIndex to determine the best
    // distribution into two groups along that axis

    // NOTE: We don't compare against node.bound, so it gets overwritten
    // at the end of the loop

    // CSA1: For each axis
    for (int axis = 0; axis < 2; axis++) {
      // initialize per-loop items
      int margin = 0;
      int distEdge = 0;
      int distIndex = 0;

      double dist_area = Double.MAX_VALUE;
      double dist_overlap = Double.MAX_VALUE;

      // Sort the items by the lower then by the upper
      // edge of their bounding box on this particular axis and
      // determine all distributions as described . Compute S. the
      // sum of all margin-values of the different
      // distributions

      // lower edge == 0, upper edge = 1
      for (int edge = 0; edge < 2; edge++) {
        // sort the items by the correct key (upper edge, lower edge)
        if (edge == 0) {
          node.sortItems(this.sortMin[axis]);
        } else {
          node.sortItems(this.sortMax[axis]);
        }
        // Distributions: pick a point m in the middle of the thing, call the
        // left
        // R1 and the right R2. Calculate the bounding box of R1 and R2, then
        // calculate the margins. Then do it again for some more points
        for (int k = 0; k < distribution_count; k++) {
          double area = 0;

          final BoundingBoxEditor R1 = new BoundingBoxEditor();
          final BoundingBoxEditor R2 = new BoundingBoxEditor();

          int i = 0;

          final int itemCount = node.getItemCount();
          final RStarNode<T>[] items = node.items;
          for (int itemIndex = 0; itemIndex < itemCount; itemIndex++) {
            final BoundingBoxProxy boundable = items[itemIndex];
            if (i <= this.nodeMinItemCount + k) {
              R1.addBbox(boundable);
            } else {
              R2.addBbox(boundable);
            }
            i++;
          }

          // calculate the three values
          margin += R1.edgeDeltas() + R2.edgeDeltas();
          area += R1.getArea() + R2.getArea();
          final double overlap = R1.overlappingArea(R2);

          // CSI1: Along the split axis, choose the distribution with the
          // minimum overlap-value. Resolve ties by choosing the distribution
          // with minimum area-value.
          if (overlap < dist_overlap || overlap == dist_overlap && area < dist_area) {
            // if so, store the parameters that allow us to recreate it at the
            // end
            distEdge = edge;
            distIndex = this.nodeMinItemCount + k;
            dist_overlap = overlap;
            dist_area = area;
          }
        }
      }

      // CSA2: Choose the axis with the minimum S as split axis
      if (splitAxis == 2 + 1 || splitMargin > margin) {
        splitAxis = axis;
        splitMargin = margin;
        splitEdge = distEdge;
        splitIndex = distIndex;
      }
    }

    // S3: Distribute the items into two groups

    // ok, we're done, and the best distribution on the selected split
    // axis has been recorded, so we just have to recreate it and
    // return the correct index

    if (splitEdge == 0) {
      node.sortItems(sortBoundingBoxByMin(splitAxis));
      // only reinsert the sort key if we have to
    } else if (splitAxis != 2 - 1) {
      node.sortItems(sortBoundingBoxByMax(splitAxis));
    }
    // distribute the end of the array to the new node, then erase them from the
    // original node
    final RStarBranch<T> newNode = new RStarBranch<>(node, splitIndex);

    node.setSize(splitIndex);

    node.recalculateBoundingBox();
    newNode.recalculateBoundingBox();

    return newNode;
  }
}
