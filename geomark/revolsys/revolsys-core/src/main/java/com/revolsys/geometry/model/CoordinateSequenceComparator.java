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

package com.revolsys.geometry.model;

import java.util.Comparator;

/**
 * Compares two {@link LineString}s.
 * For sequences of the same dimension, the ordering is lexicographic.
 * Otherwise, lower dimensions are sorted before higher.
 * The dimensions compared can be limited; if this is done
 * ordinate dimensions above the limit will not be compared.
 * <p>
 * If different behaviour is required for comparing size, dimension, or
 * coordinate values, any or all methods can be overridden.
 *
 */
public class CoordinateSequenceComparator implements Comparator {
  /**
   * Compare two <code>double</code>s, allowing for NaN values.
   * NaN is treated as being less than any valid number.
   *
   * @param a a <code>double</code>
   * @param b a <code>double</code>
   * @return -1, 0, or 1 depending on whether a is less than, equal to or greater than b
   */
  public static int compare(final double a, final double b) {
    if (a < b) {
      return -1;
    }
    if (a > b) {
      return 1;
    }

    if (Double.isNaN(a)) {
      if (Double.isNaN(b)) {
        return 0;
      }
      return -1;
    }

    if (Double.isNaN(b)) {
      return 1;
    }
    return 0;
  }

  /**
   * The number of dimensions to test
   */
  protected int dimensionLimit;

  /**
   * Creates a comparator which will test all dimensions.
   */
  public CoordinateSequenceComparator() {
    this.dimensionLimit = Integer.MAX_VALUE;
  }

  /**
   * Creates a comparator which will test only the specified number of dimensions.
   *
   * @param dimensionLimit the number of dimensions to test
   */
  public CoordinateSequenceComparator(final int dimensionLimit) {
    this.dimensionLimit = dimensionLimit;
  }

  /**
   * Compares two {@link LineString}s for relative order.
   *
   * @param o1 a {@link LineString}
   * @param o2 a {@link LineString}
   * @return -1, 0, or 1 depending on whether o1 is less than, equal to, or greater than o2
   */
  @Override
  public int compare(final Object o1, final Object o2) {
    final LineString s1 = (LineString)o1;
    final LineString s2 = (LineString)o2;

    final int size1 = s1.getVertexCount();
    final int size2 = s2.getVertexCount();

    final int dim1 = s1.getAxisCount();
    final int dim2 = s2.getAxisCount();

    int minDim = dim1;
    if (dim2 < minDim) {
      minDim = dim2;
    }
    boolean dimLimited = false;
    if (this.dimensionLimit <= minDim) {
      minDim = this.dimensionLimit;
      dimLimited = true;
    }

    // lower dimension is less than higher
    if (!dimLimited) {
      if (dim1 < dim2) {
        return -1;
      }
      if (dim1 > dim2) {
        return 1;
      }
    }

    // lexicographic ordering of point sequences
    int i = 0;
    while (i < size1 && i < size2) {
      final int ptComp = compareCoordinate(s1, s2, i, minDim);
      if (ptComp != 0) {
        return ptComp;
      }
      i++;
    }
    if (i < size1) {
      return 1;
    }
    if (i < size2) {
      return -1;
    }

    return 0;
  }

  /**
   * Compares the same coordinate of two {@link LineString}s
   * along the given number of dimensions.
   *
   * @param s1 a {@link LineString}
   * @param s2 a {@link LineString}
   * @param i the index of the coordinate to test
   * @param dimension the number of dimensiosn to test
   * @return -1, 0, or 1 depending on whether s1[i] is less than, equal to, or greater than s2[i]
   */
  protected int compareCoordinate(final LineString s1, final LineString s2, final int i,
    final int dimension) {
    for (int d = 0; d < dimension; d++) {
      final double ord1 = s1.getCoordinate(i, d);
      final double ord2 = s2.getCoordinate(i, d);
      final int comp = compare(ord1, ord2);
      if (comp != 0) {
        return comp;
      }
    }
    return 0;
  }
}
