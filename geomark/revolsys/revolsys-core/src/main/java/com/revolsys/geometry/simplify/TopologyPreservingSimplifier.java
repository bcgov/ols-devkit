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

import java.util.HashMap;
import java.util.Map;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.impl.LineStringDouble;
import com.revolsys.geometry.model.util.GeometryTransformer;

/**
 * Simplifies a geometry and ensures that
 * the result is a valid geometry having the
 * same dimension and number of components as the input,
 * and with the components having the same topological
 * relationship.
 * <p>
 * If the input is a {@link Polygonal} geometry
 * <ul>
 * <li>The result has the same number of shells and holes as the input,
 * with the same topological structure
 * <li>The result rings touch at <b>no more</b> than the number of touching points in the input
 * (although they may touch at fewer points).
 * The key implication of this statement is that if the
 * input is topologically valid, so is the simplified output.
 * </ul>
 * For linear geometries, if the input does not contain
 * any intersecting line segments, this property
 * will be preserved in the output.
 * <p>
 * For all geometry types, the result will contain
 * enough vertices to ensure validity.  For polygons
 * and closed linear geometries, the result will have at
 * least 4 vertices; for open linestrings the result
 * will have at least 2 vertices.
 * <p>
 * All geometry types are handled.
 * Empty and point geometries are returned unchanged.
 * Empty geometry components are deleted.
 * <p>
 * The simplification uses a maximum-distance difference algorithm
 * similar to the Douglas-Peucker algorithm.
 *
 * <h3>KNOWN BUGS</h3>
 * <ul>
 * <li>If a small hole is very near an edge, it is possible for the edge to be moved by
 * a relatively large tolerance value and end up with the hole outside the result shell.
 * Similarly, it is possible for a small polygon component to end up inside
 * a nearby larger polygon.
 * A workaround is to test for this situation in post-processing and remove
 * any invalid holes or polygons.
 * </ul>
 *
 * @author Martin Davis
 * @see DouglasPeuckerSimplifier
 *
 */
public class TopologyPreservingSimplifier {

  class LineStringTransformer extends GeometryTransformer {
    @Override
    protected LineString transformCoordinates(final LineString coords, final Geometry parent) {
      if (coords.getVertexCount() == 0) {
        return null;
      }
      // for linear components (including rings), simplify the linestring
      if (parent instanceof LineString) {
        final TaggedLineString taggedLine = TopologyPreservingSimplifier.this.linestringMap
          .get(parent);
        return new LineStringDouble(taggedLine.getResultCoordinates());
      }
      // for anything else (e.g. points) just copy the coordinates
      return super.transformCoordinates(coords, parent);
    }
  }

  public static Geometry simplify(final Geometry geom, final double distanceTolerance) {
    final TopologyPreservingSimplifier tss = new TopologyPreservingSimplifier(geom);
    tss.setDistanceTolerance(distanceTolerance);
    return tss.getResultGeometry();
  }

  private final Geometry geometry;

  private final TaggedLinesSimplifier lineSimplifier = new TaggedLinesSimplifier();

  private Map<LineString, TaggedLineString> linestringMap;

  public TopologyPreservingSimplifier(final Geometry geometry) {
    this.geometry = geometry;
  }

  public Geometry getResultGeometry() {
    // empty input produces an empty result
    if (this.geometry.isEmpty()) {
      return this.geometry.clone();
    } else {

      this.linestringMap = new HashMap<>();

      for (final LineString line : this.geometry.getGeometryComponents(LineString.class)) {
        // skip empty geometries
        if (!line.isEmpty()) {
          final int minSize = line.isClosed() ? 4 : 2;
          final TaggedLineString taggedLine = new TaggedLineString(line, minSize);
          this.linestringMap.put(line, taggedLine);
        }
      }

      this.lineSimplifier.simplify(this.linestringMap.values());
      final Geometry result = new LineStringTransformer().transform(this.geometry);
      return result;
    }
  }

  /**
   * Sets the distance tolerance for the simplification.
   * All vertices in the simplified geometry will be within this
   * distance of the original geometry.
   * The tolerance value must be non-negative.  A tolerance value
   * of zero is effectively a no-op.
   *
   * @param distanceTolerance the approximation tolerance to use
   */
  public void setDistanceTolerance(final double distanceTolerance) {
    if (distanceTolerance < 0.0) {
      throw new IllegalArgumentException("Tolerance must be non-negative");
    }
    this.lineSimplifier.setDistanceTolerance(distanceTolerance);
  }

}
