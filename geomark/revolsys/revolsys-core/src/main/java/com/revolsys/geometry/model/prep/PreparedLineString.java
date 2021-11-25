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
package com.revolsys.geometry.model.prep;

import java.util.List;

import com.revolsys.geometry.model.Dimension;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.impl.AbstractDelegatingLineString;
import com.revolsys.geometry.noding.FastSegmentSetIntersectionFinder;
import com.revolsys.geometry.noding.SegmentStringUtil;

/**
 * A prepared version for {@link Lineal} geometries.
 * <p>
 * Instances of this class are thread-safe.
 *
 * @author mbdavis
 *
 */
public class PreparedLineString extends AbstractDelegatingLineString {
  private static final long serialVersionUID = 1L;

  private FastSegmentSetIntersectionFinder segIntFinder = null;

  public PreparedLineString(final LineString line) {
    super(line);
  }

  public synchronized FastSegmentSetIntersectionFinder getIntersectionFinder() {
    /**
     * MD - Another option would be to use a simple scan for
     * segment testing for small geometries.
     * However, testing indicates that there is no particular advantage
     * to this approach.
     */
    if (this.segIntFinder == null) {
      this.segIntFinder = new FastSegmentSetIntersectionFinder(
        SegmentStringUtil.extractSegmentStrings(getLineString()));
    }
    return this.segIntFinder;
  }

  @Override
  public boolean intersects(final Geometry geometry) {
    if (bboxIntersects(geometry)) {
      /**
       * If any segments intersect, obviously intersects = true
       */
      final List lineSegStr = SegmentStringUtil.extractSegmentStrings(geometry);
      // only request intersection finder if there are segments (ie NOT for
      // point
      // inputs)
      if (lineSegStr.size() > 0) {
        final boolean segsIntersect = getIntersectionFinder().intersects(lineSegStr);
        // MD - performance testing
        // boolean segsIntersect = false;
        if (segsIntersect) {
          return true;
        }
      }
      /**
       * For L/L case we are done
       */
      final Dimension dimension = geometry.getDimension();
      if (dimension.isLine()) {
        return false;
      } else if (dimension.isArea() && Geometry.isAnyTargetComponentInTest(this, geometry)) {
        /**
         * For L/A case, need to check for proper inclusion of the target in the test
         */
        return true;
      } else if (dimension.isPoint()) {
        /**
         * For L/P case, need to check if any points lie on line(s)
         */
        return isAnyTestPointInTarget(geometry);
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  /**
   * Tests whether any representative point of the test Geometry intersects
   * the target geometry.
   * Only handles test geometries which are Punctual (dimension 0)
   *
   * @param geom a Punctual geometry to test
   * @return true if any point of the argument intersects the prepared geometry
   */
  public boolean isAnyTestPointInTarget(final Geometry geometry) {
    /**
     * This could be optimized by using the segment index on the lineal target.
     * However, it seems like the L/P case would be pretty rare in practice.
     */
    return Boolean.TRUE == geometry.findVertex((x, y) -> {
      if (intersects(x, y)) {
        return true;
      } else {
        return null;
      }
    });
  }

  @Override
  public PreparedLineString prepare() {
    return this;
  }
}
