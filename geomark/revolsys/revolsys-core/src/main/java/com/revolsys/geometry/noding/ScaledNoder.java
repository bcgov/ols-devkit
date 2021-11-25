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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.model.impl.LineStringDouble;

/**
 * Wraps a {@link Noder} and transforms its input
 * into the integer domain.
 * This is intended for use with Snap-Rounding noders,
 * which typically are only intended to work in the integer domain.
 * Offsets can be provided to increase the number of digits of available precision.
 * <p>
 * Clients should be aware that rescaling can involve loss of precision,
 * which can cause zero-length line segments to be created.
 * These in turn can cause problems when used to build a planar graph.
 * This situation should be checked for and collapsed segments removed if necessary.
 *
 * @version 1.7
 */
public class ScaledNoder implements Noder {
  private boolean isScaled = false;

  private final Noder noder;

  private double offsetX;

  private double offsetY;

  private final double scaleFactor;

  public ScaledNoder(final Noder noder, final double scaleFactor) {
    this(noder, scaleFactor, 0, 0);
  }

  public ScaledNoder(final Noder noder, final double scaleFactor, final double offsetX,
    final double offsetY) {
    this.noder = noder;
    this.scaleFactor = scaleFactor;
    // no need to scale if input precision is already integral
    this.isScaled = !isIntegerPrecision();
  }

  @Override
  public void computeNodes(final Collection<NodedSegmentString> inputSegStrings) {
    Collection<NodedSegmentString> intSegStrings = inputSegStrings;
    if (this.isScaled) {
      intSegStrings = scale(inputSegStrings);
    }
    this.noder.computeNodes(intSegStrings);
  }

  @Override
  public Collection<NodedSegmentString> getNodedSubstrings() {
    final Collection<NodedSegmentString> segments = this.noder.getNodedSubstrings();
    if (this.isScaled) {
      return rescale(segments);
    }
    return segments;
  }

  public boolean isIntegerPrecision() {
    return this.scaleFactor == 1.0;
  }

  private Collection<NodedSegmentString> rescale(final Collection<NodedSegmentString> segments) {
    final List<NodedSegmentString> newSegments = new ArrayList<>();
    for (final NodedSegmentString segment : segments) {
      final NodedSegmentString newSegment = rescale(segment);
      newSegments.add(newSegment);
    }
    return newSegments;
  }

  private NodedSegmentString rescale(final NodedSegmentString segment) {
    final LineString points = segment.getLineString();
    final int axisCount = points.getAxisCount();
    final int vertexCount = points.getVertexCount();
    final double[] coordinates = new double[vertexCount * axisCount];
    for (int i = 0; i < vertexCount; i++) {
      final double x = points.getX(i) / this.scaleFactor + this.offsetX;
      final double y = points.getY(i) / this.scaleFactor + this.offsetY;
      CoordinatesListUtil.setCoordinates(coordinates, axisCount, i, x, y);
      for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
        final double value = points.getCoordinate(i, axisIndex);
        coordinates[i * axisCount + axisIndex] = value;
      }
    }
    final LineStringDouble newPoints = new LineStringDouble(axisCount, coordinates);
    final Object data = segment.getData();
    return new NodedSegmentString(newPoints, data);
  }

  private Collection<NodedSegmentString> scale(final Collection<NodedSegmentString> segments) {
    final List<NodedSegmentString> result = new ArrayList<>();
    for (final NodedSegmentString segment : segments) {
      final Object data = segment.getData();
      final LineString scale = scale(segment);
      final NodedSegmentString nodedSegmentString = new NodedSegmentString(scale, data);
      result.add(nodedSegmentString);
    }
    return result;
  }

  private LineString scale(final NodedSegmentString segment) {
    final int vertexCount = segment.size();
    final int axisCount = segment.getLineString().getAxisCount();
    final double[] coordinates = new double[vertexCount * axisCount];
    double previousX = Double.NaN;
    double previousY = Double.NaN;
    int j = 0;
    for (int i = 0; i < vertexCount; i++) {
      final double x = Math.round((segment.getX(i) - this.offsetX) * this.scaleFactor);
      final double y = Math.round((segment.getY(i) - this.offsetY) * this.scaleFactor);
      final double z = segment.getZ(i);
      if (i == 0 || x != previousX && y != previousY) {
        CoordinatesListUtil.setCoordinates(coordinates, axisCount, j++, x, y, z);
      }
      previousX = x;
      previousY = y;
    }
    final LineString points = LineStringDouble.newLineStringDouble(axisCount, j, coordinates);
    return points;
  }
}
