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

package com.revolsys.geometry.operation.distance;

import com.revolsys.geometry.index.strtree.ItemDistance;
import com.revolsys.geometry.index.strtree.StrTree;
import com.revolsys.geometry.index.strtree.StrTreeLeaf;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.util.Pair;

/**
 * Computes the distance between the facets (segments and vertices)
 * of two {@link Geometry}s
 * using a Branch-and-Bound algorithm.
 * The Branch-and-Bound algorithm operates over a
 * traversal of R-trees built
 * on the target and possibly also the query geometries.
 * <p>
 * This approach provides the following benefits:
 * <ul>
 * <li>Performance is improved due to the effects of the
 * R-tree index
 * and the pruning due to the Branch-and-Bound approach
 * <li>The spatial index on the target geometry can be cached
 * to allow reuse in an incremental query situation.
 * </ul>
 * Using this technique can be much more performant
 * than using {@link #getDistance(Geometry)}
 * when one or both
 * input geometries are large,
 * or when evaluating many distance computations against
 * a single geometry.
 * <p>
 * This class is not thread-safe.
 *
 * @author Martin Davis
 *
 */
public class IndexedFacetDistance {
  /**
   * Tests whether the base geometry lies within
   * a specified distance of the given geometry.
   *
  //   * @param g the geometry to test
  //   * @param maximumDistance the maximum distance to test
  //   * @return true if the geometry lies with the specified distance
   */
  // TODO: implement this
  /*
   * public boolean isWithinDistance(Geometry g, double maximumDistance) {
   * StrTree tree2 = FacetSequenceTreeBuilder.build(g); double dist =
   * findMinDistance(cachedTree.getRoot(), tree2.getRoot(), maximumDistance); if
   * (dist <= maximumDistance) return false; return true; }
   */

  private static class FacetSequenceDistance implements ItemDistance<FacetSequence> {
    @Override
    public double distance(final StrTreeLeaf<FacetSequence> item1,
      final StrTreeLeaf<FacetSequence> item2) {
      final FacetSequence fs1 = item1.getItem();
      final FacetSequence fs2 = item2.getItem();
      return fs1.distance(fs2);
    }
  }

  /**
   * Computes the distance between two geometries using
   * the indexed approach.
   * <p>
   * For geometries with many segments or points,
   * this can be faster than using a simple distance
   * algorithm.
   *
   * @param g1 a geometry
   * @param g2 a geometry
   * @return the distance between the two geometries
   */
  public static double distance(final Geometry g1, final Geometry g2) {
    final IndexedFacetDistance dist = new IndexedFacetDistance(g1);
    return dist.getDistance(g2);
  }

  private final StrTree<FacetSequence> cachedTree;

  /**
   * Creates a new distance-finding instance for a given target {@link Geometry}.
   * <p>
   * Distances will be computed to all facets of the input geometry.
   * The facets of the geometry are the discrete segments and points
   * contained in its components.
   * In the case of {@link Lineal} and {@link Punctual} inputs,
   * this is equivalent to computing the conventional distance.
   * In the case of {@link Polygonal} inputs, this is equivalent
   * to computing the distance to the polygons boundaries.
   *
   * @param g1 a Geometry, which may be of any type.
   */
  public IndexedFacetDistance(final Geometry g1) {
    this.cachedTree = FacetSequenceTreeBuilder.build(g1);
  }

  /**
   * Computes the distance from the base geometry to
   * the given geometry, up to and including a given
   * maximum distance.
   *
   * @param g the geometry to compute the distance to
   * @param maximumDistance the maximum distance to compute.
   *
   * @return the computed distance,
   *    or <tt>maximumDistance</tt> if the true distance is determined to be greater
   */
  // TODO: implement this
  /*
   * public double getDistanceWithin(Geometry g, double maximumDistance) {
   * StrTree tree2 = FacetSequenceTreeBuilder.build(g); Object[] obj =
   * cachedTree.nearestNeighbours(tree2, new FacetSequenceDistance()); return
   * facetDistance(obj); }
   */

  /**
   * Computes the distance from the base geometry to
   * the given geometry.
   *
   * @param g the geometry to compute the distance to
   *
   * @return the computed distance
   */
  public double getDistance(final Geometry g) {
    final StrTree<FacetSequence> tree2 = FacetSequenceTreeBuilder.build(g);
    final Pair<FacetSequence, FacetSequence> obj = this.cachedTree.nearestNeighbour(tree2,
      new FacetSequenceDistance());
    final FacetSequence o1 = obj.getValue1();
    final FacetSequence o2 = obj.getValue2();
    return o1.distance(o2);
  }
}
