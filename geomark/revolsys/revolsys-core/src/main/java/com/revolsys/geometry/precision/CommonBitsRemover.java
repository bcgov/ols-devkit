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
package com.revolsys.geometry.precision;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.vertex.Vertex;

/**
 * Removes common most-significant mantissa bits
 * from one or more {@link Geometry}s.
 * <p>
 * The CommonBitsRemover "scavenges" precision
 * which is "wasted" by a large displacement of the geometry
 * from the origin.
 * For example, if a small geometry is displaced from the origin
 * by a large distance,
 * the displacement increases the significant figures in the coordinates,
 * but does not affect the <i>relative</i> topology of the geometry.
 * Thus the geometry can be translated back to the origin
 * without affecting its topology.
 * In order to compute the translation without affecting
 * the full precision of the coordinate values,
 * the translation is performed at the bit level by
 * removing the common leading mantissa bits.
 * <p>
 * If the geometry envelope already contains the origin,
 * the translation procedure cannot be applied.
 * In this case, the common bits value is computed as zero.
 * <p>
 * If the geometry crosses the Y axis but not the X axis
 * (and <i>mutatis mutandum</i>),
 * the common bits for Y are zero,
 * but the common bits for X are non-zero.
 *
 * @version 1.7
 */
public class CommonBitsRemover {
  // TODO currently doesn't do anything
  // class Translater implements CoordinateFilter {
  // Point trans = null;
  //
  // public Translater(final Point trans) {
  // this.trans = trans;
  // }
  //
  // @Override
  // public void filter(final Point coord) {
  // coord.setX(coord.getX() + trans.getX());
  // coord.setY(coord.getY() + trans.getY());
  // }
  //
  // }

  private final CommonBits commonBitsX = new CommonBits();

  private final CommonBits commonBitsY = new CommonBits();

  private Point commonCoord;

  public CommonBitsRemover() {
  }

  /**
   * Add a geometry to the set of geometries whose common bits are
   * being computed.  After this method has executed the
   * common coordinate reflects the common bits of all added
   * geometries.
   *
   * @param geometry a Geometry to test for common bits
   */
  public void add(final Geometry geometry) {
    for (final Vertex vertex : geometry.vertices()) {
      this.commonBitsX.add(vertex.getX());
      this.commonBitsY.add(vertex.getY());

    }
    this.commonCoord = new PointDoubleXY(this.commonBitsX.getCommon(),
      this.commonBitsY.getCommon());
  }

  /**
   * Adds the common coordinate bits back into a Geometry.
   * The coordinates of the Geometry are changed.
   *
   * @param geom the Geometry to which to add the common coordinate bits
   */
  public void addCommonBits(final Geometry geom) {
    // final Translater trans = new Translater(commonCoord);
    // geom.apply(trans);
    // geom.geometryChanged();
  }

  /**
   * The common bits of the Point in the supplied Geometries.
   */
  public Point getCommonCoordinate() {
    return this.commonCoord;
  }

  /**
   * Removes the common coordinate bits from a Geometry.
   * The coordinates of the Geometry are changed.
   *
   * @param geom the Geometry from which to remove the common coordinate bits
   * @return the shifted Geometry
   */
  public Geometry removeCommonBits(final Geometry geom) {
    // final double x = commonCoord.getX();
    // final double y = commonCoord.getY();
    // if (x == 0.0 && y == 0.0) {
    // return geom;
    // } else {
    // final Point invCoord = new PointDouble(-x, -y,
    // );
    // final Translater trans = new Translater(invCoord);
    // geom.apply(trans);
    // geom.geometryChanged();
    return geom;
    // }
  }

}
