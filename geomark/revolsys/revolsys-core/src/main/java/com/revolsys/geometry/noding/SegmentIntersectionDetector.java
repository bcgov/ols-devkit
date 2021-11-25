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

import com.revolsys.geometry.algorithm.LineIntersector;
import com.revolsys.geometry.algorithm.RobustLineIntersector;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;

/**
 * Detects and records an intersection between two {@link SegmentString}s,
 * if one exists.  Only a single intersection is recorded.
 * This strategy can be configured to search for <b>proper intersections>/b>.
 * In this case, the presence of <i>any</i> kind of intersection will still be recorded,
 * but searching will continue until either a proper intersection has been found
 * or no intersections are detected.
 *
 * @version 1.7
 */
public class SegmentIntersectionDetector implements SegmentIntersector {
  private boolean findAllTypes = false;

  private boolean findProper = false;

  private boolean hasIntersection = false;

  private boolean hasNonProperIntersection = false;

  private boolean hasProperIntersection = false;

  private Point intPt = null;

  private final LineIntersector li;

  /**
   * Creates an intersection finder using a {@link RobustLineIntersector}.
   */
  public SegmentIntersectionDetector() {
    this(new RobustLineIntersector());
  }

  /**
   * Creates an intersection finder using a given LineIntersector.
   *
   * @param li the LineIntersector to use
   */
  public SegmentIntersectionDetector(final LineIntersector li) {
    this.li = li;
  }

  /**
   * Gets the computed location of the intersection.
   * Due to round-off, the location may not be exact.
   *
   * @return the coordinate for the intersection location
   */
  public Point getIntersection() {
    return this.intPt;
  }

  /**
   * Tests whether an intersection was found.
   *
   * @return true if an intersection was found
   */
  public boolean hasIntersection() {
    return this.hasIntersection;
  }

  /**
   * Tests whether a non-proper intersection was found.
   *
   * @return true if a non-proper intersection was found
   */
  public boolean hasNonProperIntersection() {
    return this.hasNonProperIntersection;
  }

  /**
   * Tests whether a proper intersection was found.
   *
   * @return true if a proper intersection was found
   */
  public boolean hasProperIntersection() {
    return this.hasProperIntersection;
  }

  /**
   * Tests whether processing can terminate,
   * because all required information has been obtained
   * (e.g. an intersection of the desired type has been detected).
   *
   * @return true if processing can terminate
   */
  @Override
  public boolean isDone() {
    /**
     * If finding all types, we can stop
     * when both possible types have been found.
     */
    if (this.findAllTypes) {
      return this.hasProperIntersection && this.hasNonProperIntersection;
    }

    /**
     * If searching for a proper intersection, only stop if one is found
     */
    if (this.findProper) {
      return this.hasProperIntersection;
    }
    return this.hasIntersection;
  }

  /**
   * This method is called by clients
   * of the {@link SegmentIntersector} class to process
   * intersections for two segments of the {@link SegmentString}s being intersected.
   * Note that some clients (such as <code>MonotoneChain</code>s) may optimize away
   * this call for segment pairs which they have determined do not intersect
   * (e.g. by an disjoint envelope test).
   */
  @Override
  public void processIntersections(final SegmentString e0, final int segIndex0,
    final SegmentString e1, final int segIndex1) {
    // don't bother intersecting a segment with itself
    if (e0 == e1 && segIndex0 == segIndex1) {
      return;
    }

    final LineString line1 = e0.getLineString();
    final double line1x1 = line1.getX(segIndex0);
    final double line1y1 = line1.getY(segIndex0);
    final double line1x2 = line1.getX(segIndex0 + 1);
    final double line1y2 = line1.getY(segIndex0 + 1);

    final LineString line2 = e1.getLineString();
    final double line2x1 = line2.getX(segIndex1);
    final double line2y1 = line2.getY(segIndex1);
    final double line2x2 = line2.getX(segIndex1 + 1);
    final double line2y2 = line2.getY(segIndex1 + 1);

    this.li.computeIntersectionLine(line1x1, line1y1, line1x2, line1y2, line2x1, line2y1, line2x2,
      line2y2);

    if (this.li.hasIntersection()) {
      // System.out.println(li);

      // record intersection info
      this.hasIntersection = true;

      final boolean isProper = this.li.isProper();
      if (isProper) {
        this.hasProperIntersection = true;
      }
      if (!isProper) {
        this.hasNonProperIntersection = true;
      }

      /**
       * If this is the kind of intersection we are searching for
       * OR no location has yet been recorded
       * save the location data
       */
      boolean saveLocation = true;
      if (this.findProper && !isProper) {
        saveLocation = false;
      }

      if (this.intPt == null || saveLocation) {

        // record intersection location (approximate)
        this.intPt = this.li.getIntersection(0);

      }
    }
  }

  /**
   * Sets whether processing can terminate once any intersection is found.
   *
   * @param findAllTypes true if processing can terminate once any intersection is found.
   */
  public void setFindAllIntersectionTypes(final boolean findAllTypes) {
    this.findAllTypes = findAllTypes;
  }

  /**
   * Sets whether processing must continue until a proper intersection is found.
   *
   * @param findProper true if processing should continue until a proper intersection is found
   */
  public void setFindProper(final boolean findProper) {
    this.findProper = findProper;
  }
}
