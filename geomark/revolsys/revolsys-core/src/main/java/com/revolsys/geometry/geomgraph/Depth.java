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
package com.revolsys.geometry.geomgraph;

import java.util.Arrays;

import com.revolsys.geometry.model.Location;

/**
 * A Depth object records the topological depth of the sides
 * of an Edge for up to two Geometries.
 * @version 1.7
 */
public class Depth {

  private final static int NULL_VALUE = -1;

  public static int depthAtLocation(final Location location) {
    if (location == Location.EXTERIOR) {
      return 0;
    }
    if (location == Location.INTERIOR) {
      return 1;
    }
    return NULL_VALUE;
  }

  private final int[][] depth = new int[2][3];

  public Depth() {
    // initialize depth array to a sentinel value
    for (final int[] depths : this.depth) {
      Arrays.fill(depths, NULL_VALUE);
    }
  }

  public void add(final int geomIndex, final int posIndex, final Location location) {
    if (location == Location.INTERIOR) {
      this.depth[geomIndex][posIndex]++;
    }
  }

  public void add(final Label lbl) {
    int i = 0;
    for (final int[] depths : this.depth) {
      for (int j = 1; j < 3; j++) {
        final Location loc = lbl.getLocation(i, j);
        if (loc == Location.EXTERIOR || loc == Location.INTERIOR) {
          // initialize depth if it is null, otherwise add this location value
          if (isNull(i, j)) {
            depths[j] = depthAtLocation(loc);
          } else {
            depths[j] += depthAtLocation(loc);
          }
        }
      }
      i++;
    }
  }

  public int getDelta(final int geomIndex) {
    return this.depth[geomIndex][Position.RIGHT] - this.depth[geomIndex][Position.LEFT];
  }

  public int getDepth(final int geomIndex, final int posIndex) {
    return this.depth[geomIndex][posIndex];
  }

  public Location getLocation(final int geomIndex, final int posIndex) {
    if (this.depth[geomIndex][posIndex] <= 0) {
      return Location.EXTERIOR;
    }
    return Location.INTERIOR;
  }

  /**
   * A Depth object is null (has never been initialized) if all depths are null.
   */
  public boolean isNull() {
    for (final int[] depths : this.depth) {
      for (final int depth : depths) {
        if (depth != NULL_VALUE) {
          return false;
        }
      }
    }
    return true;
  }

  public boolean isNull(final int geomIndex) {
    return this.depth[geomIndex][1] == NULL_VALUE;
  }

  public boolean isNull(final int geomIndex, final int posIndex) {
    return this.depth[geomIndex][posIndex] == NULL_VALUE;
  }

  /**
   * Normalize the depths for each geometry, if they are non-null.
   * A normalized depth
   * has depth values in the set { 0, 1 }.
   * Normalizing the depths
   * involves reducing the depths by the same amount so that at least
   * one of them is 0.  If the remaining value is > 0, it is set to 1.
   */
  public void normalize() {
    for (final int[] depths : this.depth) {
      int minDepth = depths[1];
      if (!(minDepth == NULL_VALUE)) {
        if (depths[2] < minDepth) {
          minDepth = depths[2];
        }

        if (minDepth < 0) {
          minDepth = 0;
        }
        for (int j = 1; j < 3; j++) {
          int newValue = 0;
          if (depths[j] > minDepth) {
            newValue = 1;
          }
          depths[j] = newValue;
        }
      }
    }
  }

  public void setDepth(final int geomIndex, final int posIndex, final int depthValue) {
    this.depth[geomIndex][posIndex] = depthValue;
  }

  @Override
  public String toString() {
    return "A: " + this.depth[0][1] + "," + this.depth[0][2] + " B: " + this.depth[1][1] + ","
      + this.depth[1][2];
  }
}
