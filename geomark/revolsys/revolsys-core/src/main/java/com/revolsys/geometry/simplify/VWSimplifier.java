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

package com.revolsys.geometry.simplify;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.MultiPolygon;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.impl.LineStringDouble;
import com.revolsys.geometry.model.util.GeometryTransformer;

/**
 * Simplifies a {@link Geometry} using the Visvalingam-Whyatt area-based algorithm.
 * Ensures that any polygonal geometries returned are valid. Simple lines are not
 * guaranteed to remain simple after simplification. All geometry types are
 * handled. Empty and point geometries are returned unchanged. Empty geometry
 * components are deleted.
 * <p>
 * The simplification tolerance is specified as a distance.
 * This is converted to an area tolerance by squaring it.
 * <p>
 * Note that in general this algorithm does not preserve topology - e.g. polygons can be split,
 * collapse to lines or disappear holes can be created or disappear, and lines
 * can cross.
 *
 * <h3>Known Bugs</h3>
 * <ul>
 * <li>Not yet optimized for performance
 * <li>Does not simplify the endpoint of rings
 * </ul>
 * <h3>To Do</h3>
 * <ul>
 * <li>Allow specifying desired number of vertices in the output
 * </ul>
 *
 * @version 1.7
 */
public class VWSimplifier {

  class VWTransformer extends GeometryTransformer {
    private boolean isEnsureValidTopology = true;

    public VWTransformer(final boolean isEnsureValidTopology) {
      this.isEnsureValidTopology = isEnsureValidTopology;
    }

    /**
     * Creates a valid area geometry from one that possibly has bad topology
     * (i.e. self-intersections). Since buffer can handle invalid topology, but
     * always returns valid geometry, constructing a 0-width buffer "corrects"
     * the topology. Note this only works for area geometries, since buffer
     * always returns areas. This also may return empty geometries, if the input
     * has no actual area.
     *
     * @param rawAreaGeom
     *          an area geometry possibly containing self-intersections
     * @return a valid area geometry
     */
    private Geometry newValidArea(final Geometry rawAreaGeom) {
      if (this.isEnsureValidTopology) {
        return rawAreaGeom.buffer(0.0);
      }
      return rawAreaGeom;
    }

    @Override
    protected LineString transformCoordinates(final LineString coords, final Geometry parent) {
      if (coords.getVertexCount() == 0) {
        return coords;
      } else {
        final Point[] newPts = VWLineSimplifier.simplify(coords,
          VWSimplifier.this.distanceTolerance);
        return new LineStringDouble(newPts);
      }
    }

    /**
     * Simplifies a LinearRing. If the simplification results in a degenerate
     * ring, remove the component.
     *
     * @return null if the simplification results in a degenerate ring
     */
    @Override
    protected Geometry transformLinearRing(final LinearRing geom, final Geometry parent) {
      final boolean removeDegenerateRings = parent instanceof Polygon;
      final Geometry simpResult = super.transformLinearRing(geom, parent);
      if (removeDegenerateRings && !(simpResult instanceof LinearRing)) {
        return null;
      }
      ;
      return simpResult;
    }

    /**
     * Simplifies a MultiPolygon, fixing it if required.
     */
    @Override
    protected Geometry transformMultiPolygon(final Polygonal geom, final Geometry parent) {
      final Geometry rawGeom = super.transformMultiPolygon(geom, parent);
      return newValidArea(rawGeom);
    }

    /**
     * Simplifies a polygon, fixing it if required.
     */
    @Override
    protected Geometry transformPolygon(final Polygon geom, final Geometry parent) {
      // empty geometries are simply removed
      if (geom.isEmpty()) {
        return null;
      }
      final Geometry rawGeom = super.transformPolygon(geom, parent);
      // don't try and correct if the parent is going to do this
      if (parent instanceof MultiPolygon) {
        return rawGeom;
      }
      return newValidArea(rawGeom);
    }
  }

  /**
   * Simplifies a geometry using a given tolerance.
   *
   * @param geom geometry to simplify
   * @param distanceTolerance the tolerance to use
   * @return a simplified version of the geometry
   */
  public static Geometry simplify(final Geometry geom, final double distanceTolerance) {
    final VWSimplifier simp = new VWSimplifier(geom);
    simp.setDistanceTolerance(distanceTolerance);
    return simp.getResultGeometry();
  }

  private double distanceTolerance;

  private final Geometry inputGeom;

  private boolean isEnsureValidTopology = true;

  /**
   * Creates a simplifier for a given geometry.
   *
   * @param inputGeom the geometry to simplify
   */
  public VWSimplifier(final Geometry inputGeom) {
    this.inputGeom = inputGeom;
  }

  /**
   * Gets the simplified geometry.
   *
   * @return the simplified geometry
   */
  public Geometry getResultGeometry() {
    // empty input produces an empty result
    if (this.inputGeom.isEmpty()) {
      return this.inputGeom.clone();
    }

    return new VWTransformer(this.isEnsureValidTopology).transform(this.inputGeom);
  }

  /**
   * Sets the distance tolerance for the simplification. All vertices in the
   * simplified geometry will be within this distance of the original geometry.
   * The tolerance value must be non-negative.
   *
   * @param distanceTolerance
   *          the approximation tolerance to use
   */
  public void setDistanceTolerance(final double distanceTolerance) {
    if (distanceTolerance < 0.0) {
      throw new IllegalArgumentException("Tolerance must be non-negative");
    }
    this.distanceTolerance = distanceTolerance;
  }

  /**
   * Controls whether simplified polygons will be "fixed" to have valid
   * topology. The caller may choose to disable this because:
   * <ul>
   * <li>valid topology is not required
   * <li>fixing topology is a relative expensive operation
   * <li>in some pathological cases the topology fixing operation may either
   * fail or run for too long
   * </ul>
   *
   * The default is to fix polygon topology.
   *
   * @param isEnsureValidTopology
   */
  public void setEnsureValid(final boolean isEnsureValidTopology) {
    this.isEnsureValidTopology = isEnsureValidTopology;
  }

}
