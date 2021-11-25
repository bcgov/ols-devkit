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
package com.revolsys.geometry.index.bintree;

import com.revolsys.geometry.util.Assert;

/**
 * A node of a {@link Bintree}.
 *
 * @version 1.7
 */
public class Node extends NodeBase {
  public static Node newNode(final Interval itemInterval) {
    final Key key = new Key(itemInterval);

    // System.out.println("input: " + env + " binaryEnv: " +
    // key.getEnvelope());
    final Node node = new Node(key.getInterval(), key.getLevel());
    return node;
  }

  public static Node newNodeExpanded(final Node node, final Interval addInterval) {
    final Interval expandInt = new Interval(addInterval);
    if (node != null) {
      expandInt.expandToInclude(node.interval);
    }

    final Node largerNode = newNode(expandInt);
    if (node != null) {
      largerNode.insert(node);
    }
    return largerNode;
  }

  private final double centre;

  private final Interval interval;

  private final int level;

  public Node(final Interval interval, final int level) {
    this.interval = interval;
    this.level = level;
    this.centre = (interval.getMin() + interval.getMax()) / 2;
  }

  /**
   * Returns the smallest <i>existing</i>
   * node containing the envelope.
   */
  public NodeBase find(final Interval searchInterval) {
    final int subnodeIndex = getSubnodeIndex(searchInterval, this.centre);
    if (subnodeIndex == -1) {
      return this;
    }
    if (this.subnode[subnodeIndex] != null) {
      // query lies in subnode, so search it
      final Node node = this.subnode[subnodeIndex];
      return node.find(searchInterval);
    }
    // no existing subnode, so return this one anyway
    return this;
  }

  public Interval getInterval() {
    return this.interval;
  }

  /**
   * Returns the subnode containing the envelope.
   * Creates the node if
   * it does not already exist.
   */
  public Node getNode(final Interval searchInterval) {
    final int subnodeIndex = getSubnodeIndex(searchInterval, this.centre);
    // if index is -1 searchEnv is not contained in a subnode
    if (subnodeIndex != -1) {
      // create the node if it does not exist
      final Node node = getSubnode(subnodeIndex);
      // recursively search the found/created node
      return node.getNode(searchInterval);
    } else {
      return this;
    }
  }

  /**
   * get the subnode for the index.
   * If it doesn't exist, create it
   */
  private Node getSubnode(final int index) {
    if (this.subnode[index] == null) {
      this.subnode[index] = newSubnode(index);
    }
    return this.subnode[index];
  }

  void insert(final Node node) {
    Assert.isTrue(this.interval == null || this.interval.contains(node.interval));
    final int index = getSubnodeIndex(node.interval, this.centre);
    if (node.level == this.level - 1) {
      this.subnode[index] = node;
    } else {
      // the node is not a direct child, so make a new child node to contain it
      // and recursively insert the node
      final Node childNode = newSubnode(index);
      childNode.insert(node);
      this.subnode[index] = childNode;
    }
  }

  @Override
  protected boolean isSearchMatch(final Interval itemInterval) {
    // System.out.println(itemInterval + " overlaps " + interval + " : "
    // + itemInterval.overlaps(interval));
    return itemInterval.overlaps(this.interval);
  }

  private Node newSubnode(final int index) {
    // Construct a new new subnode in the appropriate interval

    double min = 0.0;
    double max = 0.0;

    switch (index) {
      case 0:
        min = this.interval.getMin();
        max = this.centre;
      break;
      case 1:
        min = this.centre;
        max = this.interval.getMax();
      break;
    }
    final Interval subInt = new Interval(min, max);
    final Node node = new Node(subInt, this.level - 1);
    return node;
  }

}
