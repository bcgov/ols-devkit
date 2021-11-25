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

package com.revolsys.geometry.operation.overlay.snap;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.operation.overlay.OverlayOp;
import com.revolsys.geometry.precision.CommonBitsRemover;

/**
 * Performs an overlay operation using snapping and enhanced precision
 * to improve the robustness of the result.
 * This class <i>always</i> uses snapping.
 * This is less performant than the standard JTS overlay code,
 * and may even introduce errors which were not present in the original data.
 * For this reason, this class should only be used
 * if the standard overlay code fails to produce a correct result.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class SnapOverlayOp {
  public static Geometry difference(final Geometry g0, final Geometry g1) {
    return overlayOp(g0, g1, OverlayOp.DIFFERENCE);
  }

  public static Geometry intersection(final Geometry g0, final Geometry g1) {
    return overlayOp(g0, g1, OverlayOp.INTERSECTION);
  }

  public static Geometry overlayOp(final Geometry g0, final Geometry g1, final int opCode) {
    final SnapOverlayOp op = new SnapOverlayOp(g0, g1);
    return op.getResultGeometry(opCode);
  }

  public static Geometry symDifference(final Geometry g0, final Geometry g1) {
    return overlayOp(g0, g1, OverlayOp.SYMDIFFERENCE);
  }

  public static Geometry union(final Geometry g0, final Geometry g1) {
    return overlayOp(g0, g1, OverlayOp.UNION);
  }

  private CommonBitsRemover cbr;

  private final Geometry[] geom = new Geometry[2];

  private double snapTolerance;

  public SnapOverlayOp(final Geometry g1, final Geometry g2) {
    this.geom[0] = g1;
    this.geom[1] = g2;
    computeSnapTolerance();
  }

  private void computeSnapTolerance() {
    this.snapTolerance = GeometrySnapper.computeOverlaySnapTolerance(this.geom[0], this.geom[1]);

    // System.out.println("Snap tol = " + snapTolerance);
  }

  public Geometry getResultGeometry(final int opCode) {
    // Geometry[] selfSnapGeom = new Geometry[] { selfSnap(geom[0]),
    // selfSnap(geom[1])};
    final Geometry[] prepGeom = snap(this.geom);
    final Geometry result = OverlayOp.overlayOp(prepGeom[0], prepGeom[1], opCode);
    return prepareResult(result);
  }

  private Geometry prepareResult(final Geometry geom) {
    this.cbr.addCommonBits(geom);
    return geom;
  }

  private Geometry[] removeCommonBits(final Geometry[] geom) {
    this.cbr = new CommonBitsRemover();
    this.cbr.add(geom[0]);
    this.cbr.add(geom[1]);
    final Geometry remGeom[] = new Geometry[2];
    remGeom[0] = this.cbr.removeCommonBits(geom[0].clone());
    remGeom[1] = this.cbr.removeCommonBits(geom[1].clone());
    return remGeom;
  }

  private Geometry[] snap(final Geometry[] geom) {
    final Geometry[] remGeom = removeCommonBits(geom);
    final Geometry[] snapGeom = GeometrySnapper.snap(remGeom[0], remGeom[1], this.snapTolerance);
    return snapGeom;
  }

}
