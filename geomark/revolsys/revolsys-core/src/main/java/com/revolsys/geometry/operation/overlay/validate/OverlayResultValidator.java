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
package com.revolsys.geometry.operation.overlay.validate;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.operation.overlay.OverlayOp;
import com.revolsys.geometry.operation.overlay.snap.GeometrySnapper;

/**
 * Validates that the result of an overlay operation is
 * geometrically correct, within a determined tolerance.
 * Uses fuzzy point location to find points which are
 * definitely in either the interior or exterior of the result
 * geometry, and compares these results with the expected ones.
 * <p>
 * This algorithm is only useful where the inputs are polygonal.
 * This is a heuristic test, and may return false positive results
 * (I.e. it may fail to detect an invalid result.)
 * It should never return a false negative result, however
 * (I.e. it should never report a valid result as invalid.)
 *
 * @author Martin Davis
 * @version 1.7
 * @see OverlayOp
 */
public class OverlayResultValidator {
  private static final double TOLERANCE = 0.000001;

  private static double computeBoundaryDistanceTolerance(final Geometry g0, final Geometry g1) {
    return Math.min(GeometrySnapper.computeSizeBasedSnapTolerance(g0),
      GeometrySnapper.computeSizeBasedSnapTolerance(g1));
  }

  public static boolean isValid(final Geometry a, final Geometry b, final int overlayOp,
    final Geometry result) {
    final OverlayResultValidator validator = new OverlayResultValidator(a, b, result);
    return validator.isValid(overlayOp);
  }

  private double boundaryDistanceTolerance = TOLERANCE;

  private final Geometry geometry1;

  private final Geometry geometry2;

  private Point invalidLocation = null;

  private final FuzzyPointLocator locFinder1;

  private final FuzzyPointLocator locFinder2;

  private final FuzzyPointLocator locFinder3;

  private final List<Point> testPoints = new ArrayList<>();

  public OverlayResultValidator(final Geometry a, final Geometry b, final Geometry result) {
    /**
     * The tolerance to use needs to depend on the size of the geometries.
     * It should not be more precise than double-precision can support.
     */
    this.boundaryDistanceTolerance = computeBoundaryDistanceTolerance(a, b);
    this.geometry1 = a;
    this.geometry2 = b;
    this.locFinder1 = new FuzzyPointLocator(a, this.boundaryDistanceTolerance);
    this.locFinder2 = new FuzzyPointLocator(b, this.boundaryDistanceTolerance);
    this.locFinder3 = new FuzzyPointLocator(result, this.boundaryDistanceTolerance);
  }

  private void addTestPts(final Geometry g) {
    final OffsetPointGenerator ptGen = new OffsetPointGenerator(g);
    this.testPoints.addAll(ptGen.getPoints(5 * this.boundaryDistanceTolerance));
  }

  private boolean checkValid(final int overlayOp, final Point pt) {
    final Location location1 = this.locFinder1.getLocation(pt);
    final Location location2 = this.locFinder2.getLocation(pt);
    final Location location3 = this.locFinder3.getLocation(pt);
    // If any location is on the Boundary, can't deduce anything, so just return
    // true
    if (location1 == Location.BOUNDARY || location2 == Location.BOUNDARY
      || location3 == Location.BOUNDARY) {
      return true;
    }

    final boolean expectedInterior = OverlayOp.isResultOfOp(location1, location2, overlayOp);

    final boolean resultInInterior = location3 == Location.INTERIOR;
    // MD use simpler: boolean isValid = (expectedInterior == resultInInterior);
    final boolean isValid = !(expectedInterior ^ resultInInterior);

    if (!isValid) {
      System.out.println("Overlay result invalid - A:" + Location.toLocationSymbol(location1)
        + " B:" + Location.toLocationSymbol(location2) + " expected:"
        + (expectedInterior ? 'i' : 'e') + " actual:" + Location.toLocationSymbol(location3));
    }

    return isValid;
  }

  public Point getInvalidLocation() {
    return this.invalidLocation;
  }

  public boolean isValid(final int overlayOp) {
    addTestPts(this.geometry1);
    addTestPts(this.geometry2);
    for (final Point point : this.testPoints) {
      if (!checkValid(overlayOp, point)) {
        this.invalidLocation = point;
        return false;
      }
    }
    return true;
  }
}
