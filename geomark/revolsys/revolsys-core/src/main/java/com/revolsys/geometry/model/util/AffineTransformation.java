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

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.util.Assert;

/**
 * Represents an affine transformation on the 2D Cartesian plane.
 * It can be used to transform a {@link Coordinates} or {@link Geometry}.
 * An affine transformation is a mapping of the 2D plane into itself
 * via a series of transformations of the following basic types:
 * <ul>
 * <li>reflection (through a line)
 * <li>rotation (around the origin)
 * <li>scaling (relative to the origin)
 * <li>shearing (in both the X and Y directions)
 * <li>translation
 * </ul>
 * In general, affine transformations preserve straightness and parallel lines,
 * but do not preserve distance or shape.
 * <p>
 * An affine transformation can be represented by a 3x3
 * matrix in the following form:
 * <blockquote><pre>
 * T = | m00 m01 m02 |
 *     | m10 m11 m12 |
 *     |  0   0   1  |
 * </pre></blockquote>
 * A coordinate P = (x, y) can be transformed to a new coordinate P' = (x', y')
 * by representing it as a 3x1 matrix and using matrix multiplication to compute:
 * <blockquote><pre>
 * | x' |  = T x | x |
 * | y' |        | y |
 * | 1  |        | 1 |
 * </pre></blockquote>
 * <h3>Transformation Composition</h3>
 * Affine transformations can be composed using the {@link #compose} method.
 * Composition is computed via multiplication of the
 * transformation matrices, and is defined as:
 * <blockquote><pre>
 * A.compose(B) = T<sub>B</sub> x T<sub>A</sub>
 * </pre></blockquote>
 * This produces a transformation whose effect is that of A followed by B.
 * The methods {@link #reflect}, {@link #rotate},
 * {@link #scale}, {@link #shear}, and {@link #translate}
 * have the effect of composing a transformation of that type with
 * the transformation they are invoked on.
 * <p>
 * The composition of transformations is in general <i>not</i> commutative.
 *
 * <h3>Transformation Inversion</h3>
 * Affine transformations may be invertible or non-invertible.
 * If a transformation is invertible, then there exists
 * an inverse transformation which when composed produces
 * the identity transformation.
 * The {@link #getInverse} method
 * computes the inverse of a transformation, if one exists.
 *
 * @author Martin Davis
 *
 */
public class AffineTransformation implements Cloneable
// ,
// CoordinateSequenceFilter
{

  /**
   * Creates a transformation for a reflection about the
   * line (0,0) - (x,y).
   *
   * @param x the x-ordinate of a point on the reflection line
   * @param y the y-ordinate of a point on the reflection line
   * @return a transformation for the reflection
   */
  public static AffineTransformation reflectionInstance(final double x, final double y) {
    final AffineTransformation trans = new AffineTransformation();
    trans.setToReflection(x, y);
    return trans;
  }

  /**
   * Creates a transformation for a reflection about the
   * line (x0,y0) - (x1,y1).
   *
   * @param x0 the x-ordinate of a point on the reflection line
   * @param y0 the y-ordinate of a point on the reflection line
   * @param x1 the x-ordinate of a another point on the reflection line
   * @param y1 the y-ordinate of a another point on the reflection line
   * @return a transformation for the reflection
   */
  public static AffineTransformation reflectionInstance(final double x0, final double y0,
    final double x1, final double y1) {
    final AffineTransformation trans = new AffineTransformation();
    trans.setToReflection(x0, y0, x1, y1);
    return trans;
  }

  /**
   * Creates a transformation for a rotation
   * about the origin
   * by an angle <i>theta</i>.
   * Positive angles correspond to a rotation
   * in the counter-clockwise direction.
   *
   * @param theta the rotation angle, in radians
   * @return a transformation for the rotation
   */
  public static AffineTransformation rotationInstance(final double theta) {
    return rotationInstance(Math.sin(theta), Math.cos(theta));
  }

  /**
   * Creates a transformation for a rotation
   * by an angle <i>theta</i>,
   * specified by the sine and cosine of the angle.
   * This allows providing exact values for sin(theta) and cos(theta)
   * for the common case of rotations of multiples of quarter-circles.
   *
   * @param sinTheta the sine of the rotation angle
   * @param cosTheta the cosine of the rotation angle
   * @return a transformation for the rotation
   */
  public static AffineTransformation rotationInstance(final double sinTheta,
    final double cosTheta) {
    final AffineTransformation trans = new AffineTransformation();
    trans.setToRotation(sinTheta, cosTheta);
    return trans;
  }

  /**
   * Creates a transformation for a rotation
   * about the point (x,y) by an angle <i>theta</i>.
   * Positive angles correspond to a rotation
   * in the counter-clockwise direction.
   *
   * @param theta the rotation angle, in radians
   * @param x the x-ordinate of the rotation point
   * @param y the y-ordinate of the rotation point
   * @return a transformation for the rotation
   */
  public static AffineTransformation rotationInstance(final double theta, final double x,
    final double y) {
    return rotationInstance(Math.sin(theta), Math.cos(theta), x, y);
  }

  /**
   * Creates a transformation for a rotation
   * about the point (x,y) by an angle <i>theta</i>,
   * specified by the sine and cosine of the angle.
   * This allows providing exact values for sin(theta) and cos(theta)
   * for the common case of rotations of multiples of quarter-circles.
   *
   * @param sinTheta the sine of the rotation angle
   * @param cosTheta the cosine of the rotation angle
   * @param x the x-ordinate of the rotation point
   * @param y the y-ordinate of the rotation point
   * @return a transformation for the rotation
   */
  public static AffineTransformation rotationInstance(final double sinTheta, final double cosTheta,
    final double x, final double y) {
    final AffineTransformation trans = new AffineTransformation();
    trans.setToRotation(sinTheta, cosTheta, x, y);
    return trans;
  }

  /**
   * Creates a transformation for a scaling relative to the origin.
   *
   * @param xScale the value to scale by in the x direction
   * @param yScale the value to scale by in the y direction
   * @return a transformation for the scaling
   */
  public static AffineTransformation scaleInstance(final double xScale, final double yScale) {
    final AffineTransformation trans = new AffineTransformation();
    trans.setToScale(xScale, yScale);
    return trans;
  }

  /**
   * Creates a transformation for a scaling relative to the point (x,y).
   *
   * @param xScale the value to scale by in the x direction
   * @param yScale the value to scale by in the y direction
   * @param x the x-ordinate of the point to scale around
   * @param y the y-ordinate of the point to scale around
   * @return a transformation for the scaling
   */
  public static AffineTransformation scaleInstance(final double xScale, final double yScale,
    final double x, final double y) {
    final AffineTransformation trans = new AffineTransformation();
    trans.translate(-x, -y);
    trans.scale(xScale, yScale);
    trans.translate(x, y);
    return trans;
  }

  /**
   * Creates a transformation for a shear.
   *
   * @param xShear the value to shear by in the x direction
   * @param yShear the value to shear by in the y direction
   * @return a tranformation for the shear
   */
  public static AffineTransformation shearInstance(final double xShear, final double yShear) {
    final AffineTransformation trans = new AffineTransformation();
    trans.setToShear(xShear, yShear);
    return trans;
  }

  /**
   * Creates a transformation for a translation.
   *
   * @param x the value to translate by in the x direction
   * @param y the value to translate by in the y direction
   * @return a tranformation for the translation
   */
  public static AffineTransformation translationInstance(final double x, final double y) {
    final AffineTransformation trans = new AffineTransformation();
    trans.setToTranslation(x, y);
    return trans;
  }

  // affine matrix entries
  // (bottom row is always [ 0 0 1 ])
  private double m00;

  private double m01;

  private double m02;

  private double m10;

  private double m11;

  private double m12;

  /**
   * Constructs a new identity transformation
   */
  public AffineTransformation() {
    setToIdentity();
  }

  /**
   * Constructs a transformation which is
   * a copy of the given one.
   *
   * @param trans the transformation to copy
   */
  public AffineTransformation(final AffineTransformation trans) {
    setTransformation(trans);
  }

  /**
   * Constructs a new transformation whose
   * matrix has the specified values.
   *
   * @param m00 the entry for the [0, 0] element in the transformation matrix
   * @param m01 the entry for the [0, 1] element in the transformation matrix
   * @param m02 the entry for the [0, 2] element in the transformation matrix
   * @param m10 the entry for the [1, 0] element in the transformation matrix
   * @param m11 the entry for the [1, 1] element in the transformation matrix
   * @param m12 the entry for the [1, 2] element in the transformation matrix
   */
  public AffineTransformation(final double m00, final double m01, final double m02,
    final double m10, final double m11, final double m12) {
    setTransformation(m00, m01, m02, m10, m11, m12);
  }

  /**
   * Constructs a new transformation whose
   * matrix has the specified values.
   *
   * @param matrix an array containing the 6 values { m00, m01, m02, m10, m11, m12 }
   * @throws NullPointerException if matrix is null
   * @throws ArrayIndexOutOfBoundsException if matrix is too small
   */
  public AffineTransformation(final double[] matrix) {
    this.m00 = matrix[0];
    this.m01 = matrix[1];
    this.m02 = matrix[2];
    this.m10 = matrix[3];
    this.m11 = matrix[4];
    this.m12 = matrix[5];
  }

  /**
   * Constructs a transformation
   * which maps the given source
   * points into the given destination points.
   *
   * @param src0 source point 0
   * @param src1 source point 1
   * @param src2 source point 2
   * @param dest0 the mapped point for source point 0
   * @param dest1 the mapped point for source point 1
   * @param dest2 the mapped point for source point 2
   *
   */
  public AffineTransformation(final Point src0, final Point src1, final Point src2,
    final Point dest0, final Point dest1, final Point dest2) {
  }

  /**
   * Clones this transformation
   *
   * @return a copy of this transformation
   */
  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (final Exception ex) {
      Assert.shouldNeverReachHere();
    }
    return null;
  }

  /**
   * Updates this transformation to be
   * the composition of this transformation with the given {@link AffineTransformation}.
   * This produces a transformation whose effect
   * is equal to applying this transformation
   * followed by the argument transformation.
   * Mathematically,
   * <blockquote><pre>
   * A.compose(B) = T<sub>B</sub> x T<sub>A</sub>
   * </pre></blockquote>
   *
   * @param trans an affine transformation
   * @return this transformation, with an updated matrix
   */
  public AffineTransformation compose(final AffineTransformation trans) {
    final double mp00 = trans.m00 * this.m00 + trans.m01 * this.m10;
    final double mp01 = trans.m00 * this.m01 + trans.m01 * this.m11;
    final double mp02 = trans.m00 * this.m02 + trans.m01 * this.m12 + trans.m02;
    final double mp10 = trans.m10 * this.m00 + trans.m11 * this.m10;
    final double mp11 = trans.m10 * this.m01 + trans.m11 * this.m11;
    final double mp12 = trans.m10 * this.m02 + trans.m11 * this.m12 + trans.m12;
    this.m00 = mp00;
    this.m01 = mp01;
    this.m02 = mp02;
    this.m10 = mp10;
    this.m11 = mp11;
    this.m12 = mp12;
    return this;
  }

  /**
   * Updates this transformation to be the composition
   * of a given {@link AffineTransformation} with this transformation.
   * This produces a transformation whose effect
   * is equal to applying the argument transformation
   * followed by this transformation.
   * Mathematically,
   * <blockquote><pre>
   * A.composeBefore(B) = T<sub>A</sub> x T<sub>B</sub>
   * </pre></blockquote>
   *
   * @param trans an affine transformation
   * @return this transformation, with an updated matrix
   */
  public AffineTransformation composeBefore(final AffineTransformation trans) {
    final double mp00 = this.m00 * trans.m00 + this.m01 * trans.m10;
    final double mp01 = this.m00 * trans.m01 + this.m01 * trans.m11;
    final double mp02 = this.m00 * trans.m02 + this.m01 * trans.m12 + this.m02;
    final double mp10 = this.m10 * trans.m00 + this.m11 * trans.m10;
    final double mp11 = this.m10 * trans.m01 + this.m11 * trans.m11;
    final double mp12 = this.m10 * trans.m02 + this.m11 * trans.m12 + this.m12;
    this.m00 = mp00;
    this.m01 = mp01;
    this.m02 = mp02;
    this.m10 = mp10;
    this.m11 = mp11;
    this.m12 = mp12;
    return this;
  }

  /**
   * Tests if an object is an
   * <tt>AffineTransformation</tt>
   * and has the same matrix as
   * this transformation.
   *
   * @param obj an object to test
   * @return true if the given object is equal to this object
   */
  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof AffineTransformation)) {
      return false;
    }

    final AffineTransformation trans = (AffineTransformation)obj;
    return this.m00 == trans.m00 && this.m01 == trans.m01 && this.m02 == trans.m02
      && this.m10 == trans.m10 && this.m11 == trans.m11 && this.m12 == trans.m12;
  }

  /**
   * Transforms the i'th coordinate in the input sequence
   *
   *@param seq  a <code>LineString</code>
   *@param i the index of the coordinate to transform
   */
  // @Override
  // public void filter(final LineString seq, final int i) {
  // transform(seq, i);
  // }

  /**
   * Computes the determinant of the transformation matrix.
   * The determinant is computed as:
   * <blockquote><pre>
   * | m00 m01 m02 |
   * | m10 m11 m12 | = m00 * m11 - m01 * m10
   * |  0   0   1  |
   * </pre></blockquote>
   * If the determinant is zero,
   * the transform is singular (not invertible),
   * and operations which attempt to compute
   * an inverse will throw a <tt>NoninvertibleTransformException</tt>.

   * @return the determinant of the transformation
   * @see #getInverse()
   */
  public double getDeterminant() {
    return this.m00 * this.m11 - this.m01 * this.m10;
  }

  /**
   * Computes the inverse of this transformation, if one
   * exists.
   * The inverse is the transformation which when
   * composed with this one produces the identity
   * transformation.
   * A transformation has an inverse if and only if it
   * is not singular (i.e. its
   * determinant is non-zero).
   * Geometrically, an transformation is non-invertible
   * if it maps the plane to a line or a point.
   * If no inverse exists this method
   * will throw a <tt>NoninvertibleTransformationException</tt>.
   * <p>
   * The matrix of the inverse is equal to the
   * inverse of the matrix for the transformation.
   * It is computed as follows:
   * <blockquote><pre>
   *                 1
   * inverse(A)  =  ---   x  adjoint(A)
   *                det
   *
   *
   *             =   1       |  m11  -m01   m01*m12-m02*m11  |
   *                ---   x  | -m10   m00  -m00*m12+m10*m02  |
   *                det      |  0     0     m00*m11-m10*m01  |
   *
   *
   *
   *             = |  m11/det  -m01/det   m01*m12-m02*m11/det |
   *               | -m10/det   m00/det  -m00*m12+m10*m02/det |
   *               |   0           0          1               |
   *
   * </pre></blockquote>
   *
   * @return a new inverse transformation
   * @throws NoninvertibleTransformationException
   * @see #getDeterminant()
   */
  public AffineTransformation getInverse() throws NoninvertibleTransformationException {
    final double det = getDeterminant();
    if (det == 0) {
      throw new NoninvertibleTransformationException("Transformation is non-invertible");
    }

    final double im00 = this.m11 / det;
    final double im10 = -this.m10 / det;
    final double im01 = -this.m01 / det;
    final double im11 = this.m00 / det;
    final double im02 = (this.m01 * this.m12 - this.m02 * this.m11) / det;
    final double im12 = (-this.m00 * this.m12 + this.m10 * this.m02) / det;

    return new AffineTransformation(im00, im01, im02, im10, im11, im12);
  }

  /**
   * Gets an array containing the entries
   * of the transformation matrix.
   * Only the 6 non-trivial entries are returned,
   * in the sequence:
   * <pre>
   * m00, m01, m02, m10, m11, m12
   * </pre>
   *
   * @return an array of length 6
   */
  public double[] getMatrixEntries() {
    return new double[] {
      this.m00, this.m01, this.m02, this.m10, this.m11, this.m12
    };
  }

  /**
   * Reports that this filter should continue to be executed until
   * all coordinates have been transformed.
   *
   * @return false
   */
  // @Override
  // public boolean isDone() {
  // return false;
  // }
  //
  // @Override
  // public boolean isGeometryChanged() {
  // return true;
  // }

  /**
   * Tests if this transformation is the identity transformation.
   *
   * @return true if this is the identity transformation
   */
  public boolean isIdentity() {
    return this.m00 == 1 && this.m01 == 0 && this.m02 == 0 && this.m10 == 0 && this.m11 == 1
      && this.m12 == 0;
  }

  /**
   * Updates the value of this transformation
   * to that of a reflection transformation composed
   * with the current value.
   *
   * @param x the x-ordinate of the line to reflect around
   * @param y the y-ordinate of the line to reflect around
   * @return this transformation, with an updated matrix
   */
  public AffineTransformation reflect(final double x, final double y) {
    compose(reflectionInstance(x, y));
    return this;
  }

  /**
   * Updates the value of this transformation
   * to that of a reflection transformation composed
   * with the current value.
   *
   * @param x0 the x-ordinate of a point on the line to reflect around
   * @param y0 the y-ordinate of a point on the line to reflect around
   * @param x1 the x-ordinate of a point on the line to reflect around
   * @param y1 the y-ordinate of a point on the line to reflect around
   * @return this transformation, with an updated matrix
   */
  public AffineTransformation reflect(final double x0, final double y0, final double x1,
    final double y1) {
    compose(reflectionInstance(x0, y0, x1, y1));
    return this;
  }

  /**
   * Updates the value of this transformation
   * to that of a rotation transformation composed
   * with the current value.
   * Positive angles correspond to a rotation
   * in the counter-clockwise direction.
   *
   * @param theta the angle to rotate by, in radians
   * @return this transformation, with an updated matrix
   */
  public AffineTransformation rotate(final double theta) {
    compose(rotationInstance(theta));
    return this;
  }

  /**
   * Updates the value of this transformation
   * to that of a rotation around the origin composed
   * with the current value,
   * with the sin and cos of the rotation angle specified directly.
   *
   * @param sinTheta the sine of the angle to rotate by
   * @param cosTheta the cosine of the angle to rotate by
   * @return this transformation, with an updated matrix
   */
  public AffineTransformation rotate(final double sinTheta, final double cosTheta) {
    compose(rotationInstance(sinTheta, cosTheta));
    return this;
  }

  /**
   * Updates the value of this transformation
   * to that of a rotation around a given point composed
   * with the current value.
   * Positive angles correspond to a rotation
   * in the counter-clockwise direction.
   *
   * @param theta the angle to rotate by, in radians
   * @param x the x-ordinate of the rotation point
   * @param y the y-ordinate of the rotation point
   * @return this transformation, with an updated matrix
   */
  public AffineTransformation rotate(final double theta, final double x, final double y) {
    compose(rotationInstance(theta, x, y));
    return this;
  }

  /**
   * Updates the value of this transformation
   * to that of a rotation around a given point composed
   * with the current value,
   * with the sin and cos of the rotation angle specified directly.
   *
   * @param sinTheta the sine of the angle to rotate by
   * @param cosTheta the cosine of the angle to rotate by
   * @param x the x-ordinate of the rotation point
   * @param y the y-ordinate of the rotation point
   * @return this transformation, with an updated matrix
   */
  public AffineTransformation rotate(final double sinTheta, final double cosTheta, final double x,
    final double y) {
    compose(rotationInstance(sinTheta, cosTheta));
    return this;
  }

  /**
   * Updates the value of this transformation
   * to that of a scale transformation composed
   * with the current value.
   *
   * @param xScale the value to scale by in the x direction
   * @param yScale the value to scale by in the y direction
   * @return this transformation, with an updated matrix
   */
  public AffineTransformation scale(final double xScale, final double yScale) {
    compose(scaleInstance(xScale, yScale));
    return this;
  }

  /**
   * Sets this transformation to be the identity transformation.
   * The identity transformation has the matrix:
   * <blockquote><pre>
   * | 1 0 0 |
   * | 0 1 0 |
   * | 0 0 1 |
   * </pre></blockquote>
   * @return this transformation, with an updated matrix
   */
  public AffineTransformation setToIdentity() {
    this.m00 = 1.0;
    this.m01 = 0.0;
    this.m02 = 0.0;
    this.m10 = 0.0;
    this.m11 = 1.0;
    this.m12 = 0.0;
    return this;
  }

  /**
   * Sets this transformation to be a reflection
   * about the line defined by vector (x,y).
   * The transformation for a reflection
   * is computed by:
   * <blockquote><pre>
   * d = sqrt(x<sup>2</sup> + y<sup>2</sup>)
   * sin = y / d;
   * cos = x / d;
   *
   * T<sub>ref</sub> = T<sub>rot(sin, cos)</sub> x T<sub>scale(1, -1)</sub> x T<sub>rot(-sin, cos)</sub
   * </pre></blockquote>
   *
   * @param x the x-component of the reflection line vector
   * @param y the y-component of the reflection line vector
   * @return this transformation, with an updated matrix
   */
  public AffineTransformation setToReflection(final double x, final double y) {
    if (x == 0.0 && y == 0.0) {
      throw new IllegalArgumentException("Reflection vector must be non-zero");
    }

    /**
     * Handle special case - x = y.
     * This case is specified explicitly to avoid roundoff error.
     */
    if (x == y) {
      this.m00 = 0.0;
      this.m01 = 1.0;
      this.m02 = 0.0;
      this.m10 = 1.0;
      this.m11 = 0.0;
      this.m12 = 0.0;
      return this;
    }

    // rotate vector to positive x axis direction
    final double d = Math.sqrt(x * x + y * y);
    final double sin = y / d;
    final double cos = x / d;
    rotate(-sin, cos);
    // reflect about the x-axis
    scale(1, -1);
    // rotate back
    rotate(sin, cos);
    return this;
  }

  /**
   * Sets this transformation to be a reflection
   * about the line defined by a line <tt>(x0,y0) - (x1,y1)</tt>.
   *
   * @param x0 the X ordinate of one point on the reflection line
   * @param y0 the Y ordinate of one point on the reflection line
   * @param x1 the X ordinate of another point on the reflection line
   * @param y1 the Y ordinate of another point on the reflection line
   * @return this transformation, with an updated matrix
   */
  public AffineTransformation setToReflection(final double x0, final double y0, final double x1,
    final double y1) {
    if (x0 == x1 && y0 == y1) {
      throw new IllegalArgumentException("Reflection line points must be distinct");
    }
    // translate line vector to origin
    setToTranslation(-x0, -y0);

    // rotate vector to positive x axis direction
    final double dx = x1 - x0;
    final double dy = y1 - y0;
    final double d = Math.sqrt(dx * dx + dy * dy);
    final double sin = dy / d;
    final double cos = dx / d;
    rotate(-sin, cos);
    // reflect about the x axis
    scale(1, -1);
    // rotate back
    rotate(sin, cos);
    // translate back
    translate(x0, y0);
    return this;
  }

  /**
   * Explicitly computes the math for a reflection.  May not work.
   * @param x0 the X ordinate of one point on the reflection line
   * @param y0 the Y ordinate of one point on the reflection line
   * @param x1 the X ordinate of another point on the reflection line
   * @param y1 the Y ordinate of another point on the reflection line
   * @return this transformation, with an updated matrix
   */
  public AffineTransformation setToReflectionBasic(final double x0, final double y0,
    final double x1, final double y1) {
    if (x0 == x1 && y0 == y1) {
      throw new IllegalArgumentException("Reflection line points must be distinct");
    }
    final double dx = x1 - x0;
    final double dy = y1 - y0;
    final double d = Math.sqrt(dx * dx + dy * dy);
    final double sin = dy / d;
    final double cos = dx / d;
    final double cs2 = 2 * sin * cos;
    final double c2s2 = cos * cos - sin * sin;
    this.m00 = c2s2;
    this.m01 = cs2;
    this.m02 = 0.0;
    this.m10 = cs2;
    this.m11 = -c2s2;
    this.m12 = 0.0;
    return this;
  }

  /**
   * Sets this transformation to be a rotation around the origin.
   * A positive rotation angle corresponds
   * to a counter-clockwise rotation.
   * The transformation matrix for a rotation
   * by an angle <tt>theta</tt>
   * has the value:
   * <blockquote><pre>
   * |  cos(theta)  -sin(theta)   0 |
   * |  sin(theta)   cos(theta)   0 |
   * |           0            0   1 |
   * </pre></blockquote>
   *
   * @param theta the rotation angle, in radians
   * @return this transformation, with an updated matrix
   */
  public AffineTransformation setToRotation(final double theta) {
    setToRotation(Math.sin(theta), Math.cos(theta));
    return this;
  }

  /**
   * Sets this transformation to be a rotation around the origin
   * by specifying the sin and cos of the rotation angle directly.
   * The transformation matrix for the rotation
   * has the value:
   * <blockquote><pre>
   * |  cosTheta  -sinTheta   0 |
   * |  sinTheta   cosTheta   0 |
   * |         0          0   1 |
   * </pre></blockquote>
   *
   * @param sinTheta the sine of the rotation angle
   * @param cosTheta the cosine of the rotation angle
   * @return this transformation, with an updated matrix
   */
  public AffineTransformation setToRotation(final double sinTheta, final double cosTheta) {
    this.m00 = cosTheta;
    this.m01 = -sinTheta;
    this.m02 = 0.0;
    this.m10 = sinTheta;
    this.m11 = cosTheta;
    this.m12 = 0.0;
    return this;
  }

  /**
   * Sets this transformation to be a rotation
   * around a given point (x,y).
   * A positive rotation angle corresponds
   * to a counter-clockwise rotation.
   * The transformation matrix for a rotation
   * by an angle <tt>theta</tt>
   * has the value:
   * <blockquote><pre>
   * |  cosTheta  -sinTheta   x-x*cos+y*sin |
   * |  sinTheta   cosTheta   y-x*sin-y*cos |
   * |           0            0   1 |
   * </pre></blockquote>
   *
   * @param theta the rotation angle, in radians
   * @param x the x-ordinate of the rotation point
   * @param y the y-ordinate of the rotation point
   * @return this transformation, with an updated matrix
   */
  public AffineTransformation setToRotation(final double theta, final double x, final double y) {
    setToRotation(Math.sin(theta), Math.cos(theta), x, y);
    return this;
  }

  /**
   * Sets this transformation to be a rotation
   * around a given point (x,y)
   * by specifying the sin and cos of the rotation angle directly.
   * The transformation matrix for the rotation
   * has the value:
   * <blockquote><pre>
   * |  cosTheta  -sinTheta   x-x*cos+y*sin |
   * |  sinTheta   cosTheta   y-x*sin-y*cos |
   * |         0          0         1       |
   * </pre></blockquote>
   *
   * @param sinTheta the sine of the rotation angle
   * @param cosTheta the cosine of the rotation angle
   * @param x the x-ordinate of the rotation point
   * @param y the y-ordinate of the rotation point
   * @return this transformation, with an updated matrix
   */
  public AffineTransformation setToRotation(final double sinTheta, final double cosTheta,
    final double x, final double y) {
    this.m00 = cosTheta;
    this.m01 = -sinTheta;
    this.m02 = x - x * cosTheta + y * sinTheta;
    this.m10 = sinTheta;
    this.m11 = cosTheta;
    this.m12 = y - x * sinTheta - y * cosTheta;
    return this;
  }

  /**
   * Sets this transformation to be a scaling.
   * The transformation matrix for a scale
   * has the value:
   * <blockquote><pre>
   * |  xScale      0  dx |
   * |  1      yScale  dy |
   * |  0           0   1 |
   * </pre></blockquote>
   *
   * @param xScale the amount to scale x-ordinates by
   * @param yScale the amount to scale y-ordinates by
   * @return this transformation, with an updated matrix
   */
  public AffineTransformation setToScale(final double xScale, final double yScale) {
    this.m00 = xScale;
    this.m01 = 0.0;
    this.m02 = 0.0;
    this.m10 = 0.0;
    this.m11 = yScale;
    this.m12 = 0.0;
    return this;
  }

  /**
   * Sets this transformation to be a shear.
   * The transformation matrix for a shear
   * has the value:
   * <blockquote><pre>
   * |  1      xShear  0 |
   * |  yShear      1  0 |
   * |  0           0  1 |
   * </pre></blockquote>
   * Note that a shear of (1, 1) is <i>not</i>
   * equal to shear(1, 0) composed with shear(0, 1).
   * Instead, shear(1, 1) corresponds to a mapping onto the
   * line x = y.
   *
   * @param xShear the x component to shear by
   * @param yShear the y component to shear by
   * @return this transformation, with an updated matrix
   */
  public AffineTransformation setToShear(final double xShear, final double yShear) {
    this.m00 = 1.0;
    this.m01 = xShear;
    this.m02 = 0.0;
    this.m10 = yShear;
    this.m11 = 1.0;
    this.m12 = 0.0;
    return this;
  }

  /**
   * Sets this transformation to be a translation.
   * For a translation by the vector (x, y)
   * the transformation matrix has the value:
   * <blockquote><pre>
   * |  1  0  dx |
   * |  1  0  dy |
   * |  0  0   1 |
   * </pre></blockquote>
   * @param dx the x component to translate by
   * @param dy the y component to translate by
   * @return this transformation, with an updated matrix
   */
  public AffineTransformation setToTranslation(final double dx, final double dy) {
    this.m00 = 1.0;
    this.m01 = 0.0;
    this.m02 = dx;
    this.m10 = 0.0;
    this.m11 = 1.0;
    this.m12 = dy;
    return this;
  }

  /**
   * Sets this transformation to be a copy of the given one
   *
   * @param trans a transformation to copy
   * @return this transformation, with an updated matrix
   */
  public AffineTransformation setTransformation(final AffineTransformation trans) {
    this.m00 = trans.m00;
    this.m01 = trans.m01;
    this.m02 = trans.m02;
    this.m10 = trans.m10;
    this.m11 = trans.m11;
    this.m12 = trans.m12;
    return this;
  }

  /**
   * Sets this transformation's matrix to have the given values.
   *
   * @param m00 the entry for the [0, 0] element in the transformation matrix
   * @param m01 the entry for the [0, 1] element in the transformation matrix
   * @param m02 the entry for the [0, 2] element in the transformation matrix
   * @param m10 the entry for the [1, 0] element in the transformation matrix
   * @param m11 the entry for the [1, 1] element in the transformation matrix
   * @param m12 the entry for the [1, 2] element in the transformation matrix
   * @return this transformation, with an updated matrix
   */
  public AffineTransformation setTransformation(final double m00, final double m01,
    final double m02, final double m10, final double m11, final double m12) {
    this.m00 = m00;
    this.m01 = m01;
    this.m02 = m02;
    this.m10 = m10;
    this.m11 = m11;
    this.m12 = m12;
    return this;
  }

  /**
   * Updates the value of this transformation
   * to that of a shear transformation composed
   * with the current value.
   *
   * @param xShear the value to shear by in the x direction
   * @param yShear the value to shear by in the y direction
   * @return this transformation, with an updated matrix
   */
  public AffineTransformation shear(final double xShear, final double yShear) {
    compose(shearInstance(xShear, yShear));
    return this;
  }

  /**
   * Gets a text representation of this transformation.
   * The string is of the form:
   * <pre>
   * AffineTransformation[[m00, m01, m02], [m10, m11, m12]]
   * </pre>
   *
   * @return a string representing this transformation
   *
   */
  @Override
  public String toString() {
    return "AffineTransformation[[" + this.m00 + ", " + this.m01 + ", " + this.m02 + "], ["
      + this.m10 + ", " + this.m11 + ", " + this.m12 + "]]";
  }

  /**
   * Cretaes a new @link Geometry which is the result
   * of this transformation applied to the input Geometry.
   *
   *@param seq  a <code>Geometry</code>
   *@return a transformed Geometry
   */
  public Geometry transform(final Geometry g) {
    // TODO
    throw new UnsupportedOperationException("Need to implement");
    // Geometry g2 = (Geometry) g.clone();
    // g2.apply(this);
    // return g2;
  }

  /**
   * Applies this transformation to the i'th coordinate
   * in the given LineString.
   *
   *@param seq  a <code>LineString</code>
   *@param i the index of the coordinate to transform
   */
  // public void transform(final LineString seq, final int i) {
  // final double xp = m00 * seq.getValue(i, 0) + m01 * seq.getValue(i, 1) +
  // m02;
  // final double yp = m10 * seq.getValue(i, 0) + m11 * seq.getValue(i, 1) +
  // m12;
  // seq.setValue(i, 0, xp);
  // seq.setValue(i, 1, yp);
  // }

  /**
   * Applies this transformation to the <tt>src</tt> coordinate
   * and places the results in the <tt>dest</tt> coordinate
   * (which may be the same as the source).
   *
   * @param src the coordinate to transform
   * @param dest the coordinate to accept the results
   * @return the <tt>dest</tt> coordinate
   */
  public Point transform(final Point src) {
    final double xp = this.m00 * src.getX() + this.m01 * src.getY() + this.m02;
    final double yp = this.m10 * src.getX() + this.m11 * src.getY() + this.m12;
    return new PointDoubleXY(xp, yp);
  }

  /**
   * Updates the value of this transformation
   * to that of a translation transformation composed
   * with the current value.
   *
   * @param x the value to translate by in the x direction
   * @param y the value to translate by in the y direction
   * @return this transformation, with an updated matrix
   */
  public AffineTransformation translate(final double x, final double y) {
    compose(translationInstance(x, y));
    return this;
  }
}
