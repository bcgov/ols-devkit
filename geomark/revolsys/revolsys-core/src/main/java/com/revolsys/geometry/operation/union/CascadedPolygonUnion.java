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
package com.revolsys.geometry.operation.union;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.index.strtree.StrTree;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.TopologyException;
import com.revolsys.util.Property;

/**
 * Provides an efficient method of unioning a collection of
 * {@link Polygonal} geometrys.
 * The geometries are indexed using a spatial index,
 * and unioned recursively in index order.
 * For geometries with a high degree of overlap,
 * this has the effect of reducing the number of vertices
 * early in the process, which increases speed
 * and robustness.
 * <p>
 * This algorithm is faster and more robust than
 * the simple iterated approach of
 * repeatedly unioning each polygon to a result geometry.
 * <p>
 * The <tt>buffer(0)</tt> trick is sometimes faster, but can be less robust and
 * can sometimes take a long time to complete.
 * This is particularly the case where there is a high degree of overlap
 * between the polygons.  In this case, <tt>buffer(0)</tt> is forced to compute
 * with <i>all</i> line segments from the outset,
 * whereas cascading can eliminate many segments
 * at each stage of processing.
 * The best situation for using <tt>buffer(0)</tt> is the trivial case
 * where there is <i>no</i> overlap between the input geometries.
 * However, this case is likely rare in practice.
 *
 * @author Martin Davis
 *
 */
public class CascadedPolygonUnion {
  /**
   * The effectiveness of the index is somewhat sensitive
   * to the node capacity.
   * Testing indicates that a smaller capacity is better.
   * For an StrTree, 4 is probably a good number (since
   * this produces 2x2 "squares").
   */
  private static final int STRTREE_NODE_CAPACITY = 4;

  /**
   * Gets the element at a given list index, or
   * null if the index is out of range.
   *
   * @param list
   * @param index
   * @return the geometry at the given index
   * or null if the index is out of range
   */
  private static Polygonal getPolygon(final List<Polygonal> list, final int index) {
    if (index < 0 || index >= list.size()) {
      return null;
    } else {
      return list.get(index);
    }
  }

  /**
   * Computes a {@link Polygonal} containing only {@link Polygonal} components.
   * Extracts the {@link Polygon}s from the input
   * and returns them as an appropriate {@link Polygonal} geometry.
   * <p>
   * If the input is already <tt>Polygonal</tt>, it is returned unchanged.
   * <p>
   * A particular use case is to filter out non-polygonal components
   * returned from an overlay operation.
   *
   * @param geometry the geometry to filter
   * @return a Polygonal geometry
   */
  private static Polygonal restrictToPolygons(final Geometry geometry) {
    if (geometry instanceof Polygonal) {
      return (Polygonal)geometry;
    } else {
      final List<Polygon> polygons = geometry.getGeometries(Polygon.class);
      if (polygons.size() == 1) {
        return polygons.get(0);
      }
      final GeometryFactory geometryFactory = geometry.getGeometryFactory();
      return geometryFactory.polygonal(polygons);
    }
  }

  /**
   * Computes the union of
   * a collection of {@link Polygonal} {@link Polygonal}s.
   *
   * @param polygons a collection of {@link Polygonal} {@link Polygonal}s
   */
  public static Polygonal union(final Iterable<? extends Polygonal> polygons) {
    final CascadedPolygonUnion op = new CascadedPolygonUnion(polygons);
    return op.union();
  }

  public static Polygonal union(final Polygonal... polygons) {
    return union(Arrays.asList(polygons));
  }

  private GeometryFactory geometryFactory;

  private List<Polygon> polygons = new ArrayList<>();

  /**
   * Creates a new instance to union
   * the given collection of {@link Polygonal}s.
   *
   * @param polygons a collection of {@link Polygonal} {@link Polygonal}s
   */
  public CascadedPolygonUnion(final Iterable<? extends Polygonal> polygons) {
    if (polygons != null) {
      for (final Polygonal polygonal : polygons) {
        for (final Polygon polygon : polygonal.polygons()) {
          this.polygons.add(polygon);
        }
      }
    }
  }

  // ========================================================
  /*
   * The following methods are for experimentation only
   */

  /**
   * Unions a list of geometries
   * by treating the list as a flattened binary tree,
   * and performing a cascaded union on the tree.
   */
  private Polygonal binaryUnion(final List<Polygonal> polygons) {
    return binaryUnion(polygons, 0, polygons.size());
  }

  /**
   * Unions a section of a list using a recursive binary union on each half
   * of the section.
   *
   * @param polygons the list of geometries containing the section to union
   * @param start the start index of the section
   * @param end the index after the end of the section
   * @return the union of the list section
   */
  private Polygonal binaryUnion(final List<Polygonal> polygons, final int start, final int end) {
    if (end - start <= 1) {
      final Polygonal polygon1 = getPolygon(polygons, start);
      return unionSafe(polygon1, null);
    } else if (end - start == 2) {
      final Polygonal polygon1 = getPolygon(polygons, start);
      final Polygonal polygon2 = getPolygon(polygons, start + 1);
      return unionSafe(polygon1, polygon2);
    } else {

      // recurse on both halves of the list
      final int mid = (end + start) / 2;
      final Polygonal polygon1 = binaryUnion(polygons, start, mid);
      final Polygonal polygon2 = binaryUnion(polygons, mid, end);
      return unionSafe(polygon1, polygon2);
    }
  }

  // =======================================

  private Polygonal extractByEnvelope(final BoundingBox envelope, final Polygonal polygonal,
    final List<Polygonal> disjointGeoms) {
    final List<Polygon> intersectingGeoms = new ArrayList<>();
    for (final Polygon polygon : polygonal.polygons()) {
      final BoundingBox boundingBox = polygon.getBoundingBox();
      if (boundingBox.bboxIntersects(envelope)) {
        intersectingGeoms.add(polygon);
      } else {
        disjointGeoms.add(polygon);
      }
    }
    if (intersectingGeoms.isEmpty()) {
      return null;
    } else {
      return this.geometryFactory.geometry(intersectingGeoms);
    }
  }

  /**
   * Reduces a tree of geometries to a list of geometries
   * by recursively unioning the subtrees in the list.
   *
   * @param geomTree a tree-structured list of geometries
   * @return a list of Geometrys
   */
  private List<Polygonal> reduceToGeometries(final List<?> items) {
    final List<Polygonal> geoms = new ArrayList<>();
    for (final Object item : items) {
      Polygonal polygon = null;
      if (item instanceof List) {
        final List<?> childItems = (List<?>)item;
        polygon = unionTree(childItems);
      } else if (item instanceof Polygonal) {
        polygon = (Polygonal)item;
      }
      geoms.add(polygon);
    }
    return geoms;
  }

  /**
   * Computes the union of the input geometries.
   * <p>
   * This method discards the input geometries as they are processed.
   * In many input cases this reduces the memory retained
   * as the operation proceeds.
   * Optimal memory usage is achieved
   * by disposing of the original input collection
   * before calling this method.
   *
   * @return the union of the input geometries
   * or null if no input geometries were provided
   * @throws IllegalStateException if this method is called more than once
   */
  public Polygonal union() {
    if (this.polygons == null) {
      throw new IllegalStateException("union() method cannot be called twice");
    } else if (this.polygons.isEmpty()) {
      return GeometryFactory.DEFAULT_2D.polygon();
    } else {
      this.geometryFactory = this.polygons.get(0).getGeometryFactory();

      /**
       * A spatial index to organize the collection
       * into groups of close geometries.
       * This makes unioning more efficient, since vertices are more likely
       * to be eliminated on each round.
       */
      final StrTree<Polygon> index = new StrTree<>(STRTREE_NODE_CAPACITY);
      for (final Polygon polygon : this.polygons) {
        final BoundingBox boundingBox = polygon.getBoundingBox();
        index.insertItem(boundingBox, polygon);
      }
      this.polygons = null;

      final List<?> itemTree = index.itemsTree();
      final Polygonal unionAll = unionTree(itemTree);
      return unionAll;
    }
  }

  /**
   * Encapsulates the actual unioning of two polygonal geometries.
   *
   * @param polygonal1
   * @param polygonal2
   * @return
   */
  private Polygonal unionActual(final Polygonal polygonal1, final Polygonal polygonal2) {
    if (Property.isEmpty(polygonal1)) {
      return polygonal2;
    } else if (Property.isEmpty(polygonal2)) {
      return polygonal1;
    } else {
      try {
        final Geometry union = polygonal1.union(polygonal2);
        return restrictToPolygons(union);
      } catch (final TopologyException e) {
        final List<Polygon> polygons = new ArrayList<>();
        Lists.addAll(polygons, polygonal1.polygons());
        Lists.addAll(polygons, polygonal2.polygons());
        return GeometryFactory.newGeometry(polygons);
      }
    }
  }

  private Polygonal unionOptimized(final Polygonal polygonal1, final Polygonal polygonal2) {
    final BoundingBox boundingBox1 = polygonal1.getBoundingBox();
    final BoundingBox boundingBox2 = polygonal2.getBoundingBox();
    // *
    if (!boundingBox1.bboxIntersects(boundingBox2)) {
      final Polygonal polygonal = this.geometryFactory.geometry(polygonal1, polygonal2);
      return polygonal;
    } else if (polygonal1.getGeometryCount() <= 1 && polygonal2.getGeometryCount() <= 1) {
      return unionActual(polygonal1, polygonal2);
    } else {
      final BoundingBox boundingBoxIntersection = boundingBox1.bboxIntersection(boundingBox2);
      return unionUsingEnvelopeIntersection(polygonal1, polygonal2, boundingBoxIntersection);
    }
  }

  /**
   * Computes the union of two geometries,
   * either or both of which may be null.
   *
   * @param polygonal1 a Geometry
   * @param polygonal2 a Geometry
   * @return the union of the input(s)
   * or null if both inputs are null
   */
  private Polygonal unionSafe(final Polygonal polygonal1, final Polygonal polygonal2) {
    if (polygonal1 == null && polygonal2 == null) {
      return null;
    } else if (polygonal1 == null) {
      return polygonal2;
    } else if (polygonal2 == null) {
      return polygonal1;
    } else {
      return unionOptimized(polygonal1, polygonal2);
    }
  }

  /**
   * Recursively unions all subtrees in the list into single geometries.
   * The result is a list of Geometrys only
   */
  private Polygonal unionTree(final List<?> items) {
    final List<Polygonal> geoms = reduceToGeometries(items);
    final Polygonal union = binaryUnion(geoms);
    return union;
  }

  /**
   * Unions two polygonal geometries, restricting computation
   * to the envelope intersection where possible.
   * The case of MultiPolygons is optimized to union only
   * the polygons which lie in the intersection of the two geometry's envelopes.
   * Polygons outside this region can simply be combined with the union result,
   * which is potentially much faster.
   * This case is likely to occur often during cascaded union, and may also
   * occur in real world data (such as unioning data for parcels on different street blocks).
   *
   * @param polygonal1 a polygonal geometry
   * @param polygonal2 a polygonal geometry
   * @param common the intersection of the envelopes of the inputs
   * @return the union of the inputs
   */
  private Polygonal unionUsingEnvelopeIntersection(final Polygonal polygonal1,
    final Polygonal polygonal2, final BoundingBox common) {
    final List<Polygonal> disjointPolygons = new ArrayList<>();

    final Polygonal newPolygonal1 = extractByEnvelope(common, polygonal1, disjointPolygons);
    final Polygonal newPolygonal2 = extractByEnvelope(common, polygonal2, disjointPolygons);

    final Polygonal union = unionActual(newPolygonal1, newPolygonal2);

    disjointPolygons.add(union);
    final Polygonal overallUnion = this.geometryFactory.geometry(disjointPolygons);
    return overallUnion;
  }
}
