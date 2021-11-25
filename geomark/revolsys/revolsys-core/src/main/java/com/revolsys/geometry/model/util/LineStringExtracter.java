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
package com.revolsys.geometry.model.util;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;

/**
 * Extracts all the {@link LineString} elements from a {@link Geometry}.
 *
 * @version 1.7
 */
public class LineStringExtracter {
  /**
   * Extracts the {@link LineString} elements from a single {@link Geometry}
   * and returns them in a {@link List}.
   *
   * @param geometry the geometry from which to extract
   */
  public static List<LineString> getLines(final Geometry geometry) {
    return getLines(geometry, new ArrayList<LineString>());
  }

  /**
   * Extracts the {@link LineString} elements from a single {@link Geometry}
   * and adds them to the provided {@link List}.
   *
   * @param geom the geometry from which to extract
   * @param lines the list to add the extracted LineStrings to
   */
  public static List<LineString> getLines(final Geometry geometry, final List<LineString> list) {
    for (final Geometry part : geometry.geometries()) {
      if (part instanceof LineString) {
        list.add((LineString)part);
      }
    }
    return list;
  }
}
