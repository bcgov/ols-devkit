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

package com.revolsys.geometry.noding.snapround;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.noding.NodedSegmentString;
import com.revolsys.geometry.noding.Noder;
import com.revolsys.geometry.noding.NodingValidator;

/**
 * Nodes the linework in a list of {@link Geometry}s using Snap-Rounding
 * to a given scale.
 * <p>
 * The input coordinates are expected to be rounded
 * to the given precision model.
 * This class does not perform that function.
 * <code>GeometryPrecisionReducer</code> may be used to do this.
 * <p>
 * This class does <b>not</b> dissolve the output linework,
 * so there may be duplicate linestrings in the output.
 * Subsequent processing (e.g. polygonization) may require
 * the linework to be unique.  Using <code>UnaryUnion</code> is one way
 * to do this (although this is an inefficient approach).
 *
 *
 */
public class GeometryNoder {
  private GeometryFactory geomFact;

  private boolean isValidityChecked = false;

  private final double scale;

  /**
   * Creates a new noder which snap-rounds to a grid specified
   * by the given scale.
   *
   * @param pm the precision model for the grid to snap-round to
   */
  public GeometryNoder(final double scale) {
    this.scale = scale;
  }

  private List<LineString> extractLines(final Collection<? extends Geometry> geometries) {
    final List<LineString> lines = new ArrayList<>();
    for (final Geometry geometry : geometries) {
      final List<LineString> geometryLines = geometry.getGeometryComponents(LineString.class);
      lines.addAll(geometryLines);
    }
    return lines;
  }

  /**
   * Nodes the linework of a set of Geometrys using SnapRounding.
   *
   * @param geometries a Collection of Geometrys of any type
   * @return a List of LineStrings representing the noded linework of the input
   */
  public List<LineString> node(final Collection<? extends Geometry> geometries) {
    // get geometry factory
    final Geometry geom0 = geometries.iterator().next();
    this.geomFact = geom0.getGeometryFactory();

    final Collection<LineString> lines = extractLines(geometries);
    final List<NodedSegmentString> segStrings = toSegmentStrings(lines);
    // Noder sr = new SimpleSnapRounder(pm);
    final Noder sr = new MCIndexSnapRounder(this.scale);
    sr.computeNodes(segStrings);
    final Collection<NodedSegmentString> nodedLines = sr.getNodedSubstrings();

    // TODO: improve this to check for full snap-rounded correctness
    if (this.isValidityChecked) {
      final NodingValidator nv = new NodingValidator(nodedLines);
      nv.checkValid();
    }

    return toLineStrings(nodedLines);
  }

  /**
   * Sets whether noding validity is checked after noding is performed.
   *
   * @param isValidityChecked
   */
  public void setValidate(final boolean isValidityChecked) {
    this.isValidityChecked = isValidityChecked;
  }

  private List<LineString> toLineStrings(final Collection<NodedSegmentString> segStrings) {
    final List<LineString> lines = new ArrayList<>();
    for (final NodedSegmentString ss : segStrings) {
      if (ss.size() > 1) {
        lines.add(this.geomFact.lineString(ss.getLineString()));
      }
    }
    return lines;
  }

  private List<NodedSegmentString> toSegmentStrings(final Collection<LineString> lines) {
    final List<NodedSegmentString> segStrings = new ArrayList<>();
    for (final LineString line : lines) {
      segStrings.add(new NodedSegmentString(line, null));
    }
    return segStrings;
  }
}
