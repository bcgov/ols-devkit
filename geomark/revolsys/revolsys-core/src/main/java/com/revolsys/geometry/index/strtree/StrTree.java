/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.geometry.index.strtree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import com.revolsys.geometry.index.SpatialIndex;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.util.PriorityQueue;
import com.revolsys.util.Emptyable;
import com.revolsys.util.ExitLoopException;
import com.revolsys.util.Pair;

/**
 *  A query-only R-tree created using the Sort-Tile-Recursive (STR) algorithm.
 *  For two-dimensional spatial data.
 * <P>
 *  The STR packed R-tree is simple to implement and maximizes space
 *  utilization; that is, as many leaves as possible are filled to capacity.
 *  Overlap between nodes is far less than in a basic R-tree. However, once the
 *  tree has been built (explicitly or on the first call to #query), items may
 *  not be added or removed.
 * <P>
 * Described in: P. Rigaux, Michel Scholl and Agnes Voisard.
 * <i>Spatial Databases With Application To GIS</i>.
 * Morgan Kaufmann, San Francisco, 2002.
 *
 * @version 1.7
 */
public class StrTree<I>
  implements Emptyable, Serializable, Comparator<Boundable<I>>, SpatialIndex<I> {

  private static final int DEFAULT_NODE_CAPACITY = 10;

  private static final long serialVersionUID = 259274702368956900L;

  private boolean built = false;

  /**
   * Set to <tt>null</tt> when index is built, to avoid retaining memory.
   */
  private List<StrTreeLeaf<I>> strTreeLeafs = new ArrayList<>();

  protected final int nodeCapacity;

  protected StrTreeNode<I> root;

  /**
   * Constructs an StrTree with the default node capacity.
   */
  public StrTree() {
    this(DEFAULT_NODE_CAPACITY);
  }

  /**
   * Constructs an StrTree with the given maximum number of child nodes that
   * a node may have.
   * <p>
   * The minimum recommended capacity setting is 4.
   *
   */
  public StrTree(final int nodeCapacity) {
    if (nodeCapacity < 2) {
      throw new IllegalArgumentException("Node capacity must be greater than 1");
    }
    this.nodeCapacity = nodeCapacity;
  }

  protected List<Boundable<I>> boundablesAtLevel(final int level) {
    final List<Boundable<I>> boundables = new ArrayList<>();
    this.root.boundablesAtLevel(level, boundables);
    return boundables;
  }

  /**
   * Creates parent nodes, grandparent nodes, and so forth up to the root
   * node, for the data that has been inserted into the tree. Can only be
   * called once, and thus can be called only after all of the data has been
   * inserted into the tree.
   */
  public synchronized void build() {
    if (this.built) {
      return;
    }
    this.root = this.strTreeLeafs.isEmpty() ? newNode(0)
      : newNodeHigherLevels(this.strTreeLeafs, -1);
    // the item list is no longer needed
    this.strTreeLeafs = null;
    this.built = true;
    this.root.computeBounds();
  }

  @Override
  public void clear() {
    this.root = null;
  }

  @Override
  public int compare(final Boundable<I> o1, final Boundable<I> o2) {
    return Double.compare(o1.getCentreY(), o2.getCentreY());
  }

  public int compareX(final Boundable<I> o1, final Boundable<I> o2) {
    return Double.compare(o1.getCentreX(), o2.getCentreX());
  }

  protected int depth() {
    if (isEmpty()) {
      return 0;
    } else {
      build();
      return this.root.getDepth();
    }
  }

  @Override
  public boolean forEach(final BoundingBoxProxy boundingBox, final Consumer<? super I> action) {
    try {
      query(boundingBox.getBoundingBox(), action);
      return true;
    } catch (final ExitLoopException e) {
      return false;
    }
  }

  @Override
  public boolean forEach(final Consumer<? super I> action) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean forEach(final double x, final double y, final Consumer<? super I> action) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean forEach(final double minX, final double minY, final double maxX, final double maxY,
    final Consumer<? super I> action) {
    try {
      final BoundingBox boundingBox = getGeometryFactory().newBoundingBox(minX, minY, maxX, maxY);
      query(boundingBox, action);
      return true;
    } catch (final ExitLoopException e) {
      return false;
    }
  }

  /**
   * Returns the maximum number of child nodes that a node may have
   */
  public int getNodeCapacity() {
    return this.nodeCapacity;
  }

  public StrTreeNode<I> getRoot() {
    build();
    return this.root;
  }

  @Override
  public int getSize() {
    return this.root.getItemCount();
  }

  protected void insert(final BoundingBox bounds, final I item) {
    if (this.built) {
      throw new IllegalStateException(
        "Cannot insert items into an STR packed R-tree after it has been built.");
    }
    final StrTreeLeaf<I> itemBoundable = new StrTreeLeaf<>(bounds, item);
    this.strTreeLeafs.add(itemBoundable);
  };

  /**
   * Inserts an item having the given bounds into the tree.
   */

  @Override
  public void insertItem(final BoundingBox itemEnv, final I item) {
    if (!itemEnv.isEmpty()) {
      insert(itemEnv, item);
    }
  }

  /**
   * Tests whether the index contains any items.
   * This method does not build the index,
   * so items can still be inserted after it has been called.
   *
   * @return true if the index does not contain any items
   */

  @Override
  public boolean isEmpty() {
    if (!this.built) {
      return this.strTreeLeafs.isEmpty();
    }
    return this.root.isEmpty();
  }

  /**
   * Gets a tree structure (as a nested list)
   * corresponding to the structure of the items and nodes in this tree.
   * <p>
   * The returned {@link List}s contain either {@link Object} items,
   * or Lists which correspond to subtrees of the tree
   * Subtrees which do not contain any items are not included.
   * <p>
   * Builds the tree if necessary.
   *
   * @return a List of items and/or Lists
   */
  public List<?> itemsTree() {
    build();

    final List<?> valuesTree = itemsTree(this.root);
    if (valuesTree == null) {
      return new ArrayList<>();
    }
    return valuesTree;
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  private List itemsTree(final StrTreeNode<I> node) {
    final List valuesTreeForNode = new ArrayList();
    final int childCount = node.childCount;
    final Boundable<I>[] children = node.children;
    for (int i = 0; i < childCount; i++) {
      final Boundable<I> child = children[i];
      if (child instanceof StrTreeNode) {
        final List valuesTreeForChild = itemsTree((StrTreeNode<I>)child);
        // only add if not null (which indicates an item somewhere in this tree
        if (valuesTreeForChild != null) {
          valuesTreeForNode.add(valuesTreeForChild);
        }
      } else if (child instanceof StrTreeLeaf) {
        valuesTreeForNode.add(((StrTreeLeaf)child).getItem());
      } else {
        throw new IllegalStateException("Shouldn't reach this code");
      }
    }
    if (valuesTreeForNode.size() <= 0) {
      return null;
    }
    return valuesTreeForNode;
  }

  protected StrTreeNode<I> lastNode(final List<StrTreeNode<I>> nodes) {
    return nodes.get(nodes.size() - 1);
  }

  private Pair<I, I> nearestNeighbour(final BoundablePair<I> initBndPair,
    final ItemDistance<I> itemDistance) {
    return nearestNeighbour(initBndPair, itemDistance, Double.POSITIVE_INFINITY);
  }

  private Pair<I, I> nearestNeighbour(final BoundablePair<I> initBndPair,
    final ItemDistance<I> itemDistance, final double maxDistance) {
    double distanceLowerBound = maxDistance;
    BoundablePair<I> minPair = null;

    // initialize internal structures
    final PriorityQueue<BoundablePair<I>> priorityQueue = new PriorityQueue<>();

    // initialize queue
    priorityQueue.add(initBndPair);

    while (!priorityQueue.isEmpty() && distanceLowerBound > 0.0) {
      // pop head of queue and expand one side of pair
      final BoundablePair<I> bndPair = priorityQueue.poll();
      final double currentDistance = bndPair.getDistance();

      /**
       * If the distance for the first node in the queue
       * is >= the current minimum distance, all other nodes
       * in the queue must also have a greater distance.
       * So the current minDistance must be the true minimum,
       * and we are done.
       */
      if (currentDistance >= distanceLowerBound) {
        break;
      }

      /**
       * If the pair members are leaves
       * then their distance is the exact lower bound.
       * Update the distanceLowerBound to reflect this
       * (which must be smaller, due to the test
       * immediately prior to this).
       */
      if (bndPair.isLeaves()) {
        // assert: currentDistance < minimumDistanceFound
        distanceLowerBound = currentDistance;
        minPair = bndPair;
      } else {
        // testing - does allowing a tolerance improve speed?
        // Ans: by only about 10% - not enough to matter
        /*
         * double maxDist = bndPair.getMaximumDistance(); if (maxDist * .99 <
         * lastComputedDistance) return; //
         */

        /**
         * Otherwise, expand one side of the pair,
         * (the choice of which side to expand is heuristically determined)
         * and insert the new expanded pairs into the queue
         */
        bndPair.expandToQueue(priorityQueue, itemDistance, distanceLowerBound);
      }
    }

    final Boundable<I> boundable1 = minPair.getBoundable(0);
    final Boundable<I> boundable2 = minPair.getBoundable(1);
    final I item1 = boundable1.getItem();
    final I item2 = boundable2.getItem();
    return new Pair<>(item1, item2);
  }

  /**
   * Finds the item in this tree which is nearest to the given {@link Object},
   * using {@link ItemDistance} as the distance metric.
   * A Branch-and-Bound tree traversal algorithm is used
   * to provide an efficient search.
   * <p>
   * The query <tt>object</tt> does <b>not</b> have to be
   * contained in the tree, but it does
   * have to be compatible with the <tt>itemDistance</tt>
   * distance metric.
   *
   * @param env the envelope of the query item
   * @param item the item to find the nearest neighbour of
   * @param itemDistance a distance metric applicable to the items in this tree and the query item
   * @return the nearest item in this tree
   */
  public I nearestNeighbour(final BoundingBox env, final I item,
    final ItemDistance<I> itemDistance) {
    final Boundable<I> bnd = new StrTreeLeaf<>(env, item);
    final StrTreeNode<I> root = getRoot();
    final BoundablePair<I> bp = new BoundablePair<>(root, bnd, itemDistance);
    return nearestNeighbour(bp, itemDistance).getValue1();
  }

  /**
   * Finds the two nearest items in the tree,
   * using {@link ItemDistance} as the distance metric.
   * A Branch-and-Bound tree traversal algorithm is used
   * to provide an efficient search.
   *
   * @param itemDistance a distance metric applicable to the items in this tree
   * @return the pair of the nearest items
   */
  public Pair<I, I> nearestNeighbour(final ItemDistance<I> itemDistance) {
    final StrTreeNode<I> root = getRoot();
    final BoundablePair<I> bp = new BoundablePair<>(root, root, itemDistance);
    return nearestNeighbour(bp, itemDistance);
  }

  /**
   * Finds the two nearest items from this tree
   * and another tree,
   * using {@link ItemDistance} as the distance metric.
   * A Branch-and-Bound tree traversal algorithm is used
   * to provide an efficient search.
   * The result value is a pair of items,
   * the first from this tree and the second
   * from the argument tree.
   *
   * @param tree another tree
   * @param itemDistance a distance metric applicable to the items in the trees
   * @return the pair of the nearest items, one from each tree
   */
  public Pair<I, I> nearestNeighbour(final StrTree<I> tree, final ItemDistance<I> itemDistance) {
    final BoundablePair<I> bp = new BoundablePair<>(getRoot(), tree.getRoot(), itemDistance);
    return nearestNeighbour(bp, itemDistance);
  }

  protected StrTreeNode<I> newNode(final int level) {
    return new StrTreeNode<>(this.nodeCapacity, level);
  }

  /**
   * Creates the levels higher than the given level
   *
   * @param boundablesOfALevel
   *            the level to build on
   * @param level
   *            the level of the Boundables, or -1 if the boundables are item
   *            boundables (that is, below level 0)
   * @return the root, which may be a ParentNode or a LeafNode
   */
  private StrTreeNode<I> newNodeHigherLevels(final List<? extends Boundable<I>> boundablesOfALevel,
    final int level) {
    if (boundablesOfALevel.isEmpty()) {
      throw new IllegalArgumentException("Must not be empty");
    }
    final List<StrTreeNode<I>> parentBoundables = newParentBoundables(boundablesOfALevel,
      level + 1);
    if (parentBoundables.size() == 1) {
      return parentBoundables.get(0);
    }
    return newNodeHigherLevels(parentBoundables, level + 1);
  }

  /**
   * Creates the parent level for the given child level. First, orders the items
   * by the x-values of the midpoints, and groups them into vertical slices.
   * For each slice, orders the items by the y-values of the midpoints, and
   * group them into runs of size M (the node capacity). For each run, creates
   * a new (parent) node.
   */

  protected List<StrTreeNode<I>> newParentBoundables(
    final List<? extends Boundable<I>> childBoundables, final int newLevel) {
    if (childBoundables.isEmpty()) {
      throw new IllegalArgumentException("Must not be empty");
    }
    final int minLeafCount = (int)Math.ceil(childBoundables.size() / (double)getNodeCapacity());
    final List<Boundable<I>> sortedChildBoundables = new ArrayList<>(childBoundables);
    Collections.sort(sortedChildBoundables, this::compareX);
    final List<List<Boundable<I>>> verticalSlices = verticalSlices(sortedChildBoundables,
      (int)Math.ceil(Math.sqrt(minLeafCount)));
    return newParentBoundablesFromVerticalSlices(verticalSlices, newLevel);
  }

  /**
   * Sorts the childBoundables then divides them into groups of size M, where
   * M is the node capacity.
   */
  protected List<StrTreeNode<I>> newParentBoundablesFromVerticalSlice(
    final List<? extends Boundable<I>> childBoundables, final int newLevel) {
    if (childBoundables.isEmpty()) {
      throw new IllegalArgumentException("Must not be empty");
    }
    final List<Boundable<I>> sortedChildBoundables = new ArrayList<>(childBoundables);
    Collections.sort(sortedChildBoundables, this);

    final List<StrTreeNode<I>> parentBoundables = new ArrayList<>();
    final int nodeCapacity = getNodeCapacity();
    StrTreeNode<I> childNode = newNode(newLevel);
    parentBoundables.add(childNode);

    int count = 0;
    for (final Boundable<I> childBoundable : sortedChildBoundables) {
      if (count == nodeCapacity) {
        count = 0;
        childNode = newNode(newLevel);
        parentBoundables.add(childNode);
      }
      childNode.addChild(childBoundable);
      count++;
    }
    return parentBoundables;
  }

  private List<StrTreeNode<I>> newParentBoundablesFromVerticalSlices(
    final List<List<Boundable<I>>> verticalSlices, final int newLevel) {
    final List<StrTreeNode<I>> parentBoundables = new ArrayList<>();
    for (final List<? extends Boundable<I>> verticalSlice : verticalSlices) {
      parentBoundables.addAll(newParentBoundablesFromVerticalSlice(verticalSlice, newLevel));
    }
    return parentBoundables;
  }

  /**
   *  Also builds the tree, if necessary.
   */
  public List<I> query(final BoundingBox searchBounds) {
    build();
    final List<I> matches = new ArrayList<>();
    if (!isEmpty()) {
      query(searchBounds, matches::add);
    }
    return matches;
  }

  /**
   *  Also builds the tree, if necessary.
   */
  public void query(final BoundingBox boundingBox, final Consumer<? super I> visitor) {
    build();
    if (!isEmpty()) {
      try {
        final double minX = boundingBox.getMinX();
        final double minY = boundingBox.getMinY();
        final double maxX = boundingBox.getMaxX();
        final double maxY = boundingBox.getMaxY();
        this.root.query(minX, minY, maxX, maxY, visitor);
      } catch (final ExitLoopException e) {
      }
    }
  }

  @Override
  public boolean removeItem(final BoundingBox getItems, final I item) {
    throw new UnsupportedOperationException();
  }

  public int size() {
    if (isEmpty()) {
      return 0;
    } else {
      build();
      return this.root.getItemCount();
    }
  }

  /**
   * @param childBoundables Must be sorted by the x-value of the envelope midpoints
   */
  protected List<List<Boundable<I>>> verticalSlices(final List<Boundable<I>> childBoundables,
    final int sliceCount) {
    final int sliceCapacity = (int)Math.ceil(childBoundables.size() / (double)sliceCount);
    final List<List<Boundable<I>>> slices = new ArrayList<>(sliceCapacity);
    final Iterator<Boundable<I>> i = childBoundables.iterator();
    for (int j = 0; j < sliceCount; j++) {
      final List<Boundable<I>> slice = new ArrayList<>();
      slices.add(slice);
      int boundablesAddedToSlice = 0;
      while (i.hasNext() && boundablesAddedToSlice < sliceCapacity) {
        final Boundable<I> childBoundable = i.next();
        slice.add(childBoundable);
        boundablesAddedToSlice++;
      }
    }
    return slices;
  }

}
