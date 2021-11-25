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

import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;

/**
 * Supports creating {@link AffineTransformation}s defined by various kinds of
 * inputs and transformation mapping rules.
 *
 * @author Martin Davis
 *
 */
public class AffineTransformationFactory {
  /**
   * Creates a tranformation from a set of three control vectors. A control
   * vector consists of a source point and a destination point, which is the
   * image of the source point under the desired transformation. Three control
   * vectors allows defining a fully general affine transformation.
   *
   * @param src0
   * @param src1
   * @param src2
   * @param dest0
   * @param dest1
   * @param dest2
   * @return the computed transformation
   */
  public static AffineTransformation mnewFromControlVectors(final Point src0, final Point src1,
    final Point src2, final Point dest0, final Point dest1, final Point dest2) {
    final AffineTransformationBuilder builder = new AffineTransformationBuilder(src0, src1, src2,
      dest0, dest1, dest2);
    return builder.getTransformation();
  }

  /**
   * Creates an AffineTransformation defined by a maping between two baselines.
   * The computed transformation consists of:
   * <ul>
   * <li>a translation
   * from the start point of the source baseline to the start point of the destination baseline,
   * <li>a rotation through the angle between the baselines about the destination start point,
   * <li>and a scaling equal to the ratio of the baseline lengths.
   * </ul>
   * If the source baseline has zero length, an identity transformation is returned.
   *
   * @param src0 the start point of the source baseline
   * @param src1 the end point of the source baseline
   * @param dest0 the start point of the destination baseline
   * @param dest1 the end point of the destination baseline
   * @return the computed transformation
   */
  public static AffineTransformation newFromBaseLines(final Point src0, final Point src1,
    final Point dest0, final Point dest1) {
    final Point rotPt = new PointDoubleXY(src0.getX() + dest1.getX() - dest0.getX(),
      src0.getY() + dest1.getY() - dest0.getY());

    final double ang = Point.angleBetweenOriented(src1, src0, rotPt);

    final double srcDist = src1.distancePoint(src0);
    final double destDist = dest1.distancePoint(dest0);

    // return identity if transformation would be degenerate
    if (srcDist == 0.0) {
      return new AffineTransformation();
    }

    final double scale = destDist / srcDist;

    final AffineTransformation trans = AffineTransformation.translationInstance(-src0.getX(),
      -src0.getY());
    trans.rotate(ang);
    trans.scale(scale, scale);
    trans.translate(dest0.getX(), dest0.getY());
    return trans;
  }

  /**
   * Creates an AffineTransformation defined by a single control vector. A
   * control vector consists of a source point and a destination point, which is
   * the image of the source point under the desired transformation. This
   * produces a translation.
   *
   * @param src0
   *          the start point of the control vector
   * @param dest0
   *          the end point of the control vector
   * @return the computed transformation
   */
  public static AffineTransformation newFromControlVectors(final Point src0, final Point dest0) {
    final double dx = dest0.getX() - src0.getX();
    final double dy = dest0.getY() - src0.getY();
    return AffineTransformation.translationInstance(dx, dy);
  }

  /**
   * Creates an AffineTransformation defined by a pair of control vectors. A
   * control vector consists of a source point and a destination point, which is
   * the image of the source point under the desired transformation. The
   * computed transformation is a combination of one or more of a uniform scale,
   * a rotation, and a translation (i.e. there is no shear component and no
   * reflection)
   *
   * @param src0
   * @param src1
   * @param dest0
   * @param dest1
   * @return the computed transformation
   * @return null if the control vectors do not determine a well-defined transformation
   */
  public static AffineTransformation newFromControlVectors(final Point src0, final Point src1,
    final Point dest0, final Point dest1) {
    final Point rotPt = new PointDoubleXY(dest1.getX() - dest0.getX(), dest1.getY() - dest0.getY());

    final double ang = Point.angleBetweenOriented(src1, src0, rotPt);

    final double srcDist = src1.distancePoint(src0);
    final double destDist = dest1.distancePoint(dest0);

    if (srcDist == 0.0) {
      return null;
    }

    final double scale = destDist / srcDist;

    final AffineTransformation trans = AffineTransformation.translationInstance(-src0.getX(),
      -src0.getY());
    trans.rotate(ang);
    trans.scale(scale, scale);
    trans.translate(dest0.getX(), dest0.getY());
    return trans;
  }

  /**
   * Creates an AffineTransformation defined by a set of control vectors.
   * Between one and three vectors must be supplied.
   *
   * @param src
   *          the source points of the vectors
   * @param dest
   *          the destination points of the vectors
   * @return the computed transformation
   * @throws IllegalArgumentException
   *           if the control vector arrays are too short, long or of different
   *           lengths
   */
  public static AffineTransformation newFromControlVectors(final Point[] src, final Point[] dest) {
    if (src.length != dest.length) {
      throw new IllegalArgumentException("Src and Dest arrays are not the same length");
    }
    if (src.length <= 0) {
      throw new IllegalArgumentException("Too few control points");
    }
    if (src.length > 3) {
      throw new IllegalArgumentException("Too many control points");
    }

    if (src.length == 1) {
      return newFromControlVectors(src[0], dest[0]);
    }
    if (src.length == 2) {
      return newFromControlVectors(src[0], src[1], dest[0], dest[1]);
    }

    return mnewFromControlVectors(src[0], src[1], src[2], dest[0], dest[1], dest[2]);
  }

}
