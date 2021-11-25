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
import java.util.Collection;
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Polygonal;

/**
 * Combines {@link Geometry}s
 * to produce a {@link Geometry} of the most appropriate type.
 * Input geometries which are already collections
 * will have their elements extracted first.
 * No validation of the result geometry is performed.
 * (The only case where invalidity is possible is where {@link Polygonal} geometries
 * are combined and result in a self-intersection).
 *
 * @author mbdavis
 * @see GeometryFactory#buildGeometry
 */
public class GeometryCombiner {
  /**
   * Combines a collection of geometries.
   *
   * @param geoms the geometries to combine
   * @return the combined geometry
   */
  public static Geometry combine(final Collection<? extends Geometry> geoms) {
    final GeometryCombiner combiner = new GeometryCombiner(geoms);
    return combiner.combine();
  }

  /**
   * Combines two geometries.
   *
   * @param g0 a geometry to combine
   * @param g1 a geometry to combine
   * @return the combined geometry
   */
  public static Geometry combine(final Geometry g0, final Geometry g1) {
    final GeometryCombiner combiner = new GeometryCombiner(newList(g0, g1));
    return combiner.combine();
  }

  /**
   * Combines three geometries.
   *
   * @param g0 a geometry to combine
   * @param g1 a geometry to combine
   * @param g2 a geometry to combine
   * @return the combined geometry
   */
  public static Geometry combine(final Geometry g0, final Geometry g1, final Geometry g2) {
    final GeometryCombiner combiner = new GeometryCombiner(newList(g0, g1, g2));
    return combiner.combine();
  }

  /**
   * Extracts the GeometryFactory used by the geometries in a collection
   *
   * @param geoms
   * @return a GeometryFactory
   */
  public static GeometryFactory extractFactory(final Collection<? extends Geometry> geoms) {
    if (geoms.isEmpty()) {
      return null;
    }
    return ((Geometry)geoms.iterator().next()).getGeometryFactory();
  }

  /**
   * Creates a list from two items
   *
   * @param obj0
   * @param obj1
   * @return a List containing the two items
   */
  private static List<Geometry> newList(final Geometry obj0, final Geometry obj1) {
    final List<Geometry> list = new ArrayList<>();
    list.add(obj0);
    list.add(obj1);
    return list;
  }

  /**
   * Creates a list from two items
   *
   * @param obj0
   * @param obj1
   * @return a List containing the two items
   */
  private static List<Geometry> newList(final Geometry obj0, final Geometry obj1,
    final Geometry obj2) {
    final List<Geometry> list = new ArrayList<>();
    list.add(obj0);
    list.add(obj1);
    list.add(obj2);
    return list;
  }

  private final GeometryFactory geomFactory;

  private final Collection<? extends Geometry> inputGeoms;

  /**
   * Creates a new combiner for a collection of geometries
   *
   * @param geoms the geometries to combine
   */
  public GeometryCombiner(final Collection<? extends Geometry> geoms) {
    this.geomFactory = extractFactory(geoms);
    this.inputGeoms = geoms;
  }

  /**
   * Computes the combination of the input geometries
   * to produce the most appropriate {@link Geometry}
   *
   * @return a Geometry which is the combination of the inputs
   */
  public Geometry combine() {
    final List<Geometry> elems = new ArrayList<>();
    for (final Geometry g : this.inputGeoms) {
      extractElements(g, elems);
    }

    if (elems.size() == 0) {
      if (this.geomFactory != null) {
        // return an empty GC
        return this.geomFactory.geometryCollection();
      }
      return null;
    }
    // return the "simplest possible" geometry
    return this.geomFactory.buildGeometry(elems);
  }

  private void extractElements(final Geometry geom, final List<Geometry> elems) {
    if (geom != null) {
      for (int i = 0; i < geom.getGeometryCount(); i++) {
        final Geometry elemGeom = geom.getGeometry(i);
        elems.add(elemGeom);
      }
    }
  }

}
