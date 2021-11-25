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
package com.revolsys.geometry.noding;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.geometry.model.LineString;

/**
 * Dissolves a noded collection of {@link SegmentString}s to produce
 * a set of merged linework with unique segments.
 * A custom {@link SegmentStringMerger} merging strategy
 * can be supplied.
 * This strategy will be called when two identical (up to orientation)
 * strings are dissolved together.
 * The default merging strategy is simply to discard one of the merged strings.
 * <p>
 * A common use for this class is to merge noded edges
 * while preserving topological labelling.
 * This requires a custom merging strategy to be supplied
 * to merge the topology labels appropriately.
 *
 * @version 1.7
 * @see SegmentStringMerger
 */
public class SegmentStringDissolver {
  /**
   * A merging strategy which can be used to update the context data of {@link SegmentString}s
   * which are merged during the dissolve process.
   *
   * @author mbdavis
   *
   */
  public interface SegmentStringMerger {
    /**
     * Updates the context data of a SegmentString
     * when an identical (up to orientation) one is found during dissolving.
     *
     * @param mergeTarget the segment string to update
     * @param ssToMerge the segment string being dissolved
     * @param isSameOrientation <code>true</code> if the strings are in the same direction,
     * <code>false</code> if they are opposite
     */
    void merge(SegmentString mergeTarget, SegmentString ssToMerge, boolean isSameOrientation);
  }

  private final SegmentStringMerger merger;

  private final Map ocaMap = new TreeMap();

  // testing only
  // private List testAddedSS = new ArrayList();

  /**
   * Creates a dissolver with the default merging strategy.
   */
  public SegmentStringDissolver() {
    this(null);
  }

  /**
   * Creates a dissolver with a user-defined merge strategy.
   *
   * @param merger the merging strategy to use
   */
  public SegmentStringDissolver(final SegmentStringMerger merger) {
    this.merger = merger;
  }

  private void add(final OrientedCoordinateArray oca, final SegmentString segString) {
    this.ocaMap.put(oca, segString);
    // testAddedSS.add(oca);
  }

  /**
   * Dissolve all {@link SegmentString}s in the input {@link Collection}
   * @param segments
   */
  public void dissolve(final Collection<SegmentString> segments) {
    for (final SegmentString segment : segments) {
      dissolve(segment);
    }
  }

  /**
   * Dissolve the given {@link SegmentString}.
   *
   * @param segString the string to dissolve
   */
  public void dissolve(final SegmentString segString) {
    final OrientedCoordinateArray oca = new OrientedCoordinateArray(segString.getLineString());
    final SegmentString existing = findMatching(oca, segString);
    if (existing == null) {
      add(oca, segString);
    } else {
      if (this.merger != null) {
        final boolean isSameOrientation = equals(existing.getLineString(), 2,
          segString.getLineString());
        this.merger.merge(existing, segString, isSameOrientation);
      }
    }
  }

  private boolean equals(final LineString points1, final int axisCount, final LineString points2) {
    double maxAxis = Math.max(points1.getAxisCount(), points2.getAxisCount());
    if (maxAxis > axisCount) {
      maxAxis = axisCount;
    }
    if (points1.getAxisCount() < maxAxis) {
      return false;
    } else if (points2.getAxisCount() < maxAxis) {
      return false;
    } else if (points1.getVertexCount() == points2.getVertexCount()) {
      for (int i = 0; i < points1.getVertexCount(); i++) {
        for (int j = 0; j < axisCount; j++) {
          final double value1 = points1.getCoordinate(i, j);
          final double value2 = points2.getCoordinate(i, j);
          if (Double.compare(value1, value2) != 0) {
            return false;
          }
        }
      }
      return true;
    } else {
      return false;
    }
  }

  private SegmentString findMatching(final OrientedCoordinateArray oca,
    final SegmentString segString) {
    final SegmentString matchSS = (SegmentString)this.ocaMap.get(oca);
    /*
     * boolean hasBeenAdded = checkAdded(oca); if (matchSS == null &&
     * hasBeenAdded) { System.out.println("added!"); }
     */
    return matchSS;
  }

  /*
   * private boolean checkAdded(OrientedCoordinateArray oca) { for (Iterator i =
   * testAddedSS.iterator(); i.hasNext(); ) { OrientedCoordinateArray addedOCA =
   * (OrientedCoordinateArray) i.next(); if (oca.compareTo(addedOCA) == 0)
   * return true; } return false; }
   */

  /**
   * Gets the collection of dissolved (i.e. unique) {@link SegmentString}s
   *
   * @return the unique {@link SegmentString}s
   */
  public Collection getDissolved() {
    return this.ocaMap.values();
  }
}
