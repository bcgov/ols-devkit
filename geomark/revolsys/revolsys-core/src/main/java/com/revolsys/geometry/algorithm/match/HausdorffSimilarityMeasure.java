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

package com.revolsys.geometry.algorithm.match;

import com.revolsys.geometry.algorithm.distance.DiscreteHausdorffDistance;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;

/**
 * Measures the degree of similarity between two {@link Geometry}s
 * using the Hausdorff distance metric.
 * The measure is normalized to lie in the range [0, 1].
 * Higher measures indicate a great degree of similarity.
 * <p>
 * The measure is computed by computing the Hausdorff distance
 * between the input geometries, and then normalizing
 * this by dividing it by the diagonal distance across
 * the envelope of the combined geometries.
 *
 * @author mbdavis
 *
 */
public class HausdorffSimilarityMeasure implements SimilarityMeasure {
  /*
   * public static double measure(Geometry a, Geometry b) {
   * HausdorffSimilarityMeasure gv = new HausdorffSimilarityMeasure(a, b);
   * return gv.measure(); }
   */

  /*
   * Densify a small amount to increase accuracy of Hausdorff distance
   */
  private static final double DENSIFY_FRACTION = 0.25;

  public static double diagonalSize(final BoundingBox env) {
    if (env.isEmpty()) {
      return 0.0;
    }

    final double width = env.getWidth();
    final double hgt = env.getHeight();
    return Math.sqrt(width * width + hgt * hgt);
  }

  public HausdorffSimilarityMeasure() {
  }

  @Override
  public double measure(final Geometry g1, final Geometry g2) {
    final double distance = DiscreteHausdorffDistance.distance(g1, g2, DENSIFY_FRACTION);

    final BoundingBox boundingBox = BoundingBox.bboxNew(g1, g2);
    final double envSize = diagonalSize(boundingBox);
    // normalize so that more similarity produces a measure closer to 1
    final double measure = 1 - distance / envSize;

    return measure;
  }
}
