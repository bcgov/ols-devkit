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
package com.revolsys.geometry.model;

/**
 * Models a <b>Dimensionally Extended Nine-Intersection Model (DE-9IM)</b> matrix.
 * DE-9IM matrices (such as "212FF1FF2")
 * specify the topological relationship between two {@link Geometry}s.
 * This class can also represent matrix patterns (such as "T*T******")
 * which are used for matching instances of DE-9IM matrices.
 *
 *  Methods are provided to:
 *  <UL>
 *    <LI> set and query the elements of the matrix in a convenient fashion
 *    <LI> convert to and from the standard string representation (specified in
 *    SFS Section 2.1.13.2).
 *    <LI> test to see if a matrix matches a given pattern string.
 *  </UL>
 *  <P>
 *
 *  For a description of the DE-9IM and the spatial predicates derived from it,
 *  see the <i><A
 *  HREF="http://www.opengis.org/techno/specs.htm">OGC 99-049 OpenGIS Simple Features
 *  Specification for SQL</A></i>, as well as
 *  <i>OGC 06-103r4 OpenGIS
 *  Implementation Standard for Geographic information -
 *  Simple feature access - Part 1: Common architecture</i>
 *  (which provides some further details on certain predicate specifications).
 * <p>
 * The entries of the matrix are defined by the constants in the {@link Dimension} class.
 * The indices of the matrix represent the topological locations
 * that occur in a geometry (Interior, Boundary, Exterior).
 * These are provided as constants in the {@link Location} class.
 *
 *
 *@version 1.7
 */
public class IntersectionMatrix implements Cloneable {
  public static final int BOUNDARY = Location.BOUNDARY.getIndex();

  public static final int EXTERIOR = Location.EXTERIOR.getIndex();

  public static final int INTERIOR = Location.INTERIOR.getIndex();

  /**
   *  Tests if the dimension value matches <tt>TRUE</tt>
   *  (i.e.  has value 0, 1, 2 or TRUE).
   *
   *@param  actualDimensionValue     a number that can be stored in the <code>IntersectionMatrix</code>
   *      . Possible values are <code>{TRUE, FALSE, DONTCARE, 0, 1, 2}</code>.
   *@return true if the dimension value matches TRUE
   */
  public static boolean isTrue(final Dimension actualDimensionValue) {
    return actualDimensionValue.isTrue();
  }

  /**
   *  Tests if the dimension value satisfies the dimension symbol.
   *
   *@param  actualDimensionValue     a number that can be stored in the <code>IntersectionMatrix</code>
   *      . Possible values are <code>{TRUE, FALSE, DONTCARE, 0, 1, 2}</code>.
   *@param  requiredDimensionSymbol  a character used in the string
   *      representation of an <code>IntersectionMatrix</code>. Possible values
   *      are <code>{T, F, * , 0, 1, 2}</code>.
   *@return                          true if the dimension symbol matches
   *      the dimension value
   */
  public static boolean matches(final Dimension actualDimensionValue,
    final char requiredDimensionSymbol) {
    final Dimension requiredDimension = Dimension.toDimensionValue(requiredDimensionSymbol);
    if (requiredDimension == Dimension.DONTCARE) {
      return true;
    }
    if (requiredDimension == Dimension.TRUE && actualDimensionValue.isTrue()) {
      return true;
    }
    if (requiredDimension == Dimension.FALSE && actualDimensionValue == Dimension.FALSE) {
      return true;
    }
    if (requiredDimension == Dimension.P && actualDimensionValue == Dimension.P) {
      return true;
    }
    if (requiredDimension == Dimension.L && actualDimensionValue == Dimension.L) {
      return true;
    }
    if (requiredDimension == Dimension.A && actualDimensionValue == Dimension.A) {
      return true;
    }
    return false;
  }

  /**
   *  Tests if each of the actual dimension symbols in a matrix string satisfies the
   *  corresponding required dimension symbol in a pattern string.
   *
   *@param  actualDimensionSymbols    nine dimension symbols to validate.
   *      Possible values are <code>{T, F, * , 0, 1, 2}</code>.
   *@param  requiredDimensionSymbols  nine dimension symbols to validate
   *      against. Possible values are <code>{T, F, * , 0, 1, 2}</code>.
   *@return                           true if each of the required dimension
   *      symbols encompass the corresponding actual dimension symbol
   */
  public static boolean matches(final String actualDimensionSymbols,
    final String requiredDimensionSymbols) {
    final IntersectionMatrix m = new IntersectionMatrix(actualDimensionSymbols);
    return m.matches(requiredDimensionSymbols);
  }

  /**
   *  Internal representation of this <code>IntersectionMatrix</code>.
   */
  private final Dimension[][] matrix;

  private Dimension dimension1;

  private Dimension dimension2;

  /**
   *  Creates an <code>IntersectionMatrix</code> with <code>FALSE</code>
   *  dimension values.
   */
  public IntersectionMatrix() {
    this.matrix = new Dimension[3][3];
    setAll(Dimension.FALSE);
  }

  public IntersectionMatrix(final Dimension dimension1, final Dimension dimension2) {
    this();
    this.dimension1 = dimension1;
    this.dimension2 = dimension2;
  }

  /**
   *  Creates an <code>IntersectionMatrix</code> with the same elements as
   *  <code>other</code>.
   *
   *@param  other  an <code>IntersectionMatrix</code> to copy
   */
  public IntersectionMatrix(final IntersectionMatrix other) {
    this(other.dimension1, other.dimension2);
    this.matrix[INTERIOR][INTERIOR] = other.matrix[INTERIOR][INTERIOR];
    this.matrix[INTERIOR][BOUNDARY] = other.matrix[INTERIOR][BOUNDARY];
    this.matrix[INTERIOR][EXTERIOR] = other.matrix[INTERIOR][EXTERIOR];
    this.matrix[BOUNDARY][INTERIOR] = other.matrix[BOUNDARY][INTERIOR];
    this.matrix[BOUNDARY][BOUNDARY] = other.matrix[BOUNDARY][BOUNDARY];
    this.matrix[BOUNDARY][EXTERIOR] = other.matrix[BOUNDARY][EXTERIOR];
    this.matrix[EXTERIOR][INTERIOR] = other.matrix[EXTERIOR][INTERIOR];
    this.matrix[EXTERIOR][BOUNDARY] = other.matrix[EXTERIOR][BOUNDARY];
    this.matrix[EXTERIOR][EXTERIOR] = other.matrix[EXTERIOR][EXTERIOR];
  }

  /**
   *  Creates an <code>IntersectionMatrix</code> with the given dimension
   *  symbols.
   *
   *@param  elements  a String of nine dimension symbols in row major order
   */
  public IntersectionMatrix(final String elements) {
    this();
    set(elements);
  }

  /**
   * Adds one matrix to another.
   * Addition is defined by taking the maximum dimension value of each position
   * in the summand matrices.
   *
   * @param im the matrix to add
   */
  public void add(final IntersectionMatrix im) {
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        setAtLeast(i, j, im.get(i, j));
      }
    }
  }

  /**
   *  Returns the value of one of this matrix
   *  entries.
   *  The value of the provided index is one of the
   *  values from the {@link Location} class.
   *  The value returned is a constant
   *  from the {@link Dimension} class.
   *
   *@param  row     the row of this <code>IntersectionMatrix</code>, indicating
   *      the interior, boundary or exterior of the first <code>Geometry</code>
   *@param  column  the column of this <code>IntersectionMatrix</code>,
   *      indicating the interior, boundary or exterior of the second <code>Geometry</code>
   *@return         the dimension value at the given matrix position.
   */
  public Dimension get(final int row, final int column) {
    return this.matrix[row][column];
  }

  /**
   *  Tests whether this <code>IntersectionMatrix</code> is
   *  T*****FF*.
   *
   *@return    <code>true</code> if the first <code>Geometry</code> contains the
   *      second
   */
  public boolean isContains() {
    return isTrue(this.matrix[INTERIOR][INTERIOR])
      && this.matrix[EXTERIOR][INTERIOR] == Dimension.FALSE
      && this.matrix[EXTERIOR][BOUNDARY] == Dimension.FALSE;
  }

  /**
   *  Returns <code>true</code> if this <code>IntersectionMatrix</code> is
   *    <code>T*F**F***</code>
   * or <code>*TF**F***</code>
   * or <code>**FT*F***</code>
   * or <code>**F*TF***</code>
   *
   *@return    <code>true</code> if the first <code>Geometry</code>
   * is covered by the second
   */
  public boolean isCoveredBy() {
    final boolean hasPointInCommon = isTrue(this.matrix[INTERIOR][INTERIOR])
      || isTrue(this.matrix[INTERIOR][BOUNDARY]) || isTrue(this.matrix[BOUNDARY][INTERIOR])
      || isTrue(this.matrix[BOUNDARY][BOUNDARY]);

    return hasPointInCommon && this.matrix[INTERIOR][EXTERIOR] == Dimension.FALSE
      && this.matrix[BOUNDARY][EXTERIOR] == Dimension.FALSE;
  }

  /**
   *  Returns <code>true</code> if this <code>IntersectionMatrix</code> is
   *    <code>T*****FF*</code>
   * or <code>*T****FF*</code>
   * or <code>***T**FF*</code>
   * or <code>****T*FF*</code>
   *
   *@return    <code>true</code> if the first <code>Geometry</code> covers the
   *      second
   */
  public boolean isCovers() {
    final boolean hasPointInCommon = isTrue(this.matrix[INTERIOR][INTERIOR])
      || isTrue(this.matrix[INTERIOR][BOUNDARY]) || isTrue(this.matrix[BOUNDARY][INTERIOR])
      || isTrue(this.matrix[BOUNDARY][BOUNDARY]);

    return hasPointInCommon && this.matrix[EXTERIOR][INTERIOR] == Dimension.FALSE
      && this.matrix[EXTERIOR][BOUNDARY] == Dimension.FALSE;
  }

  /**
   * Tests whether this geometry crosses the
   * specified geometry.
   * <p>
   * The <code>crosses</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The geometries have some but not all interior points in common.
   * <li>The DE-9IM Intersection Matrix for the two geometries is
   *   <ul>
   *    <li>T*T****** (for P/L, P/A, and L/A situations)
   *    <li>T*****T** (for L/P, L/A, and A/L situations)
   *    <li>0******** (for L/L situations)
   *   </ul>
   * </ul>
   * For any other combination of dimensions this predicate returns <code>false</code>.
   * <p>
   * The SFS defined this predicate only for P/L, P/A, L/L, and L/A situations.
   * JTS extends the definition to apply to L/P, A/P and A/L situations as well.
   * This makes the relation symmetric.
   *
   *@return                       <code>true</code> if the two <code>Geometry</code>s
   *      related by this <code>IntersectionMatrix</code> cross.
   */
  public boolean isCrosses() {
    return isCrosses(this.dimension1, this.dimension2);
  }

  /**
   * Tests whether this geometry crosses the
   * specified geometry.
   * <p>
   * The <code>crosses</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The geometries have some but not all interior points in common.
   * <li>The DE-9IM Intersection Matrix for the two geometries is
   *   <ul>
   *    <li>T*T****** (for P/L, P/A, and L/A situations)
   *    <li>T*****T** (for L/P, L/A, and A/L situations)
   *    <li>0******** (for L/L situations)
   *   </ul>
   * </ul>
   * For any other combination of dimensions this predicate returns <code>false</code>.
   * <p>
   * The SFS defined this predicate only for P/L, P/A, L/L, and L/A situations.
   * JTS extends the definition to apply to L/P, A/P and A/L situations as well.
   * This makes the relation symmetric.
   *
   *@param  dimensionOfGeometryA  the dimension of the first <code>Geometry</code>
   *@param  dimensionOfGeometryB  the dimension of the second <code>Geometry</code>
   *@return                       <code>true</code> if the two <code>Geometry</code>s
   *      related by this <code>IntersectionMatrix</code> cross.
   */
  public boolean isCrosses(final Dimension dimensionOfGeometryA,
    final Dimension dimensionOfGeometryB) {
    if (dimensionOfGeometryA == Dimension.P && dimensionOfGeometryB == Dimension.L
      || dimensionOfGeometryA == Dimension.P && dimensionOfGeometryB == Dimension.A
      || dimensionOfGeometryA == Dimension.L && dimensionOfGeometryB == Dimension.A) {
      return isTrue(this.matrix[INTERIOR][INTERIOR]) && isTrue(this.matrix[INTERIOR][EXTERIOR]);
    }
    if (dimensionOfGeometryA == Dimension.L && dimensionOfGeometryB == Dimension.P
      || dimensionOfGeometryA == Dimension.A && dimensionOfGeometryB == Dimension.P
      || dimensionOfGeometryA == Dimension.A && dimensionOfGeometryB == Dimension.L) {
      return isTrue(this.matrix[INTERIOR][INTERIOR]) && isTrue(this.matrix[EXTERIOR][INTERIOR]);
    }
    if (dimensionOfGeometryA == Dimension.L && dimensionOfGeometryB == Dimension.L) {
      return this.matrix[INTERIOR][INTERIOR].isPoint();
    }
    return false;
  }

  /**
   *  Returns <code>true</code> if this <code>IntersectionMatrix</code> is
   *  FF*FF****.
   *
   *@return    <code>true</code> if the two <code>Geometry</code>s related by
   *      this <code>IntersectionMatrix</code> are disjoint
   */
  public boolean isDisjoint() {
    return this.matrix[INTERIOR][INTERIOR] == Dimension.FALSE
      && this.matrix[INTERIOR][BOUNDARY] == Dimension.FALSE
      && this.matrix[BOUNDARY][INTERIOR] == Dimension.FALSE
      && this.matrix[BOUNDARY][BOUNDARY] == Dimension.FALSE;
  }

  public boolean isEquals() {
    return isTrue(this.matrix[INTERIOR][INTERIOR])
      && this.matrix[INTERIOR][EXTERIOR] == Dimension.FALSE
      && this.matrix[BOUNDARY][EXTERIOR] == Dimension.FALSE
      && this.matrix[EXTERIOR][INTERIOR] == Dimension.FALSE
      && this.matrix[EXTERIOR][BOUNDARY] == Dimension.FALSE;
  }

  /**
   *  Tests whether the argument dimensions are equal and
   *  this <code>IntersectionMatrix</code> matches
   *  the pattern <tt>T*F**FFF*</tt>.
   *  <p>
   *  <b>Note:</b> This pattern differs from the one stated in
   *  <i>Simple feature access - Part 1: Common architecture</i>.
   *  That document states the pattern as <tt>TFFFTFFFT</tt>.  This would
   *  specify that
   *  two identical <tt>POINT</tt>s are not equal, which is not desirable behaviour.
   *  The pattern used here has been corrected to compute equality in this situation.
   *
   *@param  dimensionOfGeometryA  the dimension of the first <code>Geometry</code>
   *@param  dimensionOfGeometryB  the dimension of the second <code>Geometry</code>
   *@return                       <code>true</code> if the two <code>Geometry</code>s
   *      related by this <code>IntersectionMatrix</code> are equal; the
   *      <code>Geometry</code>s must have the same dimension to be equal
   */
  public boolean isEquals(final Dimension dimensionOfGeometryA,
    final Dimension dimensionOfGeometryB) {
    if (dimensionOfGeometryA != dimensionOfGeometryB) {
      return false;
    }
    return isEquals();
  }

  /**
   *  Returns <code>true</code> if <code>isDisjoint</code> returns false.
   *
   *@return    <code>true</code> if the two <code>Geometry</code>s related by
   *      this <code>IntersectionMatrix</code> intersect
   */
  public boolean isIntersects() {
    return !isDisjoint();
  }

  /**
   *  Returns <code>true</code> if this <code>IntersectionMatrix</code> is
   *  <UL>
   *    <LI> T*T***T** (for two points or two surfaces)
   *    <LI> 1*T***T** (for two curves)
   *  </UL>.
   *
    *@return                       <code>true</code> if the two <code>Geometry</code>s
   *      related by this <code>IntersectionMatrix</code> overlap. For this
   *      function to return <code>true</code>, the <code>Geometry</code>s must
   *      be two points, two curves or two surfaces.
   */
  public boolean isOverlaps() {
    return isOverlaps(this.dimension1, this.dimension2);
  }

  /**
   *  Returns <code>true</code> if this <code>IntersectionMatrix</code> is
   *  <UL>
   *    <LI> T*T***T** (for two points or two surfaces)
   *    <LI> 1*T***T** (for two curves)
   *  </UL>.
   *
   *@param  dimensionOfGeometryA  the dimension of the first <code>Geometry</code>
   *@param  dimensionOfGeometryB  the dimension of the second <code>Geometry</code>
   *@return                       <code>true</code> if the two <code>Geometry</code>s
   *      related by this <code>IntersectionMatrix</code> overlap. For this
   *      function to return <code>true</code>, the <code>Geometry</code>s must
   *      be two points, two curves or two surfaces.
   */
  public boolean isOverlaps(final Dimension dimensionOfGeometryA,
    final Dimension dimensionOfGeometryB) {
    if (dimensionOfGeometryA == Dimension.P && dimensionOfGeometryB == Dimension.P
      || dimensionOfGeometryA == Dimension.A && dimensionOfGeometryB == Dimension.A) {
      return isTrue(this.matrix[INTERIOR][INTERIOR]) && isTrue(this.matrix[INTERIOR][EXTERIOR])
        && isTrue(this.matrix[EXTERIOR][INTERIOR]);
    }
    if (dimensionOfGeometryA == Dimension.L && dimensionOfGeometryB == Dimension.L) {
      return this.matrix[INTERIOR][INTERIOR].isLine() && isTrue(this.matrix[INTERIOR][EXTERIOR])
        && isTrue(this.matrix[EXTERIOR][INTERIOR]);
    }
    return false;
  }

  /**
   *  Returns <code>true</code> if this <code>IntersectionMatrix</code> is
   *  FT*******, F**T***** or F***T****.
   *
    *@return                       <code>true</code> if the two <code>Geometry</code>
   *      s related by this <code>IntersectionMatrix</code> touch; Returns false
   *      if both <code>Geometry</code>s are points.
   */
  public boolean isTouches() {
    return isTouches(this.dimension1, this.dimension2);
  }

  /**
   *  Returns <code>true</code> if this <code>IntersectionMatrix</code> is
   *  FT*******, F**T***** or F***T****.
   *
   *@param  dimensionOfGeometryA  the dimension of the first <code>Geometry</code>
   *@param  dimensionOfGeometryB  the dimension of the second <code>Geometry</code>
   *@return                       <code>true</code> if the two <code>Geometry</code>
   *      s related by this <code>IntersectionMatrix</code> touch; Returns false
   *      if both <code>Geometry</code>s are points.
   */
  public boolean isTouches(final Dimension dimensionOfGeometryA,
    final Dimension dimensionOfGeometryB) {
    if (dimensionOfGeometryA.isGreaterThan(dimensionOfGeometryB)) {
      // no need to get transpose because pattern matrix is symmetrical
      return isTouches(dimensionOfGeometryB, dimensionOfGeometryA);
    }
    if (dimensionOfGeometryA == Dimension.A && dimensionOfGeometryB == Dimension.A
      || dimensionOfGeometryA == Dimension.L && dimensionOfGeometryB == Dimension.L
      || dimensionOfGeometryA == Dimension.L && dimensionOfGeometryB == Dimension.A
      || dimensionOfGeometryA == Dimension.P && dimensionOfGeometryB == Dimension.A
      || dimensionOfGeometryA == Dimension.P && dimensionOfGeometryB == Dimension.L) {
      return this.matrix[INTERIOR][INTERIOR] == Dimension.FALSE
        && (isTrue(this.matrix[INTERIOR][BOUNDARY]) || isTrue(this.matrix[BOUNDARY][INTERIOR])
          || isTrue(this.matrix[BOUNDARY][BOUNDARY]));
    }
    return false;
  }

  /**
   *  Tests whether this <code>IntersectionMatrix</code> is
   *  T*F**F***.
   *
   *@return    <code>true</code> if the first <code>Geometry</code> is within
   *      the second
   */
  public boolean isWithin() {
    return isTrue(this.matrix[INTERIOR][INTERIOR])
      && this.matrix[INTERIOR][EXTERIOR] == Dimension.FALSE
      && this.matrix[BOUNDARY][EXTERIOR] == Dimension.FALSE;
  }

  /**
   *  Returns whether the elements of this <code>IntersectionMatrix</code>
   *  satisfies the required dimension symbols.
   *
   *@param  requiredDimensionSymbols  nine dimension symbols with which to
   *      compare the elements of this <code>IntersectionMatrix</code>. Possible
   *      values are <code>{T, F, * , 0, 1, 2}</code>.
   *@return                           <code>true</code> if this <code>IntersectionMatrix</code>
   *      matches the required dimension symbols
   */
  public boolean matches(final String requiredDimensionSymbols) {
    if (requiredDimensionSymbols.length() != 9) {
      throw new IllegalArgumentException("Should be length 9: " + requiredDimensionSymbols);
    }
    for (int ai = 0; ai < 3; ai++) {
      for (int bi = 0; bi < 3; bi++) {
        if (!matches(this.matrix[ai][bi], requiredDimensionSymbols.charAt(3 * ai + bi))) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   *  Changes the value of one of this <code>IntersectionMatrix</code>s
   *  elements.
   *
   *@param  row             the row of this <code>IntersectionMatrix</code>,
   *      indicating the interior, boundary or exterior of the first <code>Geometry</code>
   *@param  column          the column of this <code>IntersectionMatrix</code>,
   *      indicating the interior, boundary or exterior of the second <code>Geometry</code>
   *@param  dimensionValue  the new value of the element
   */
  public void set(final int row, final int column, final Dimension dimensionValue) {
    this.matrix[row][column] = dimensionValue;
  }

  public void set(final Location row, final Location column, final Dimension dimensionValue) {
    set(row.getIndex(), column.getIndex(), dimensionValue);
  }

  /**
   *  Changes the elements of this <code>IntersectionMatrix</code> to the
   *  dimension symbols in <code>dimensionSymbols</code>.
   *
   *@param  dimensionSymbols  nine dimension symbols to which to set this <code>IntersectionMatrix</code>
   *      s elements. Possible values are <code>{T, F, * , 0, 1, 2}</code>
   */
  public void set(final String dimensionSymbols) {
    for (int i = 0; i < dimensionSymbols.length(); i++) {
      final int row = i / 3;
      final int col = i % 3;
      this.matrix[row][col] = Dimension.toDimensionValue(dimensionSymbols.charAt(i));
    }
  }

  /**
   *  Changes the elements of this <code>IntersectionMatrix</code> to <code>dimensionValue</code>
   *  .
   *
   *@param  dimensionValue  the dimension value to which to set this <code>IntersectionMatrix</code>
   *      s elements. Possible values <code>{TRUE, FALSE, DONTCARE, 0, 1, 2}</code>
   *      .
   */
  public void setAll(final Dimension dimensionValue) {
    for (int ai = 0; ai < 3; ai++) {
      for (int bi = 0; bi < 3; bi++) {
        this.matrix[ai][bi] = dimensionValue;
      }
    }
  }

  /**
   *  Changes the specified element to <code>minimumDimensionValue</code> if the
   *  element is less.
   *
   *@param  row                    the row of this <code>IntersectionMatrix</code>
   *      , indicating the interior, boundary or exterior of the first <code>Geometry</code>
   *@param  column                 the column of this <code>IntersectionMatrix</code>
   *      , indicating the interior, boundary or exterior of the second <code>Geometry</code>
   *@param  minimumDimensionValue  the dimension value with which to compare the
   *      element. The order of dimension values from least to greatest is
   *      <code>{DONTCARE, TRUE, FALSE, 0, 1, 2}</code>.
   */
  public void setAtLeast(final int row, final int column, final Dimension minimumDimensionValue) {
    if (this.matrix[row][column].isLessThan(minimumDimensionValue)) {
      this.matrix[row][column] = minimumDimensionValue;
    }
  }

  /**
   *  For each element in this <code>IntersectionMatrix</code>, changes the
   *  element to the corresponding minimum dimension symbol if the element is
   *  less.
   *
   *@param  minimumDimensionSymbols  nine dimension symbols with which to
   *      compare the elements of this <code>IntersectionMatrix</code>. The
   *      order of dimension values from least to greatest is <code>{DONTCARE, TRUE, FALSE, 0, 1, 2}</code>
   *      .
   */
  public void setAtLeast(final String minimumDimensionSymbols) {
    for (int i = 0; i < minimumDimensionSymbols.length(); i++) {
      final int row = i / 3;
      final int col = i % 3;
      setAtLeast(row, col, Dimension.toDimensionValue(minimumDimensionSymbols.charAt(i)));
    }
  }

  /**
   *  If row >= 0 and column >= 0, changes the specified element to <code>minimumDimensionValue</code>
   *  if the element is less. Does nothing if row <0 or column < 0.
   *
   *@param  row                    the row of this <code>IntersectionMatrix</code>
   *      , indicating the interior, boundary or exterior of the first <code>Geometry</code>
   *@param  column                 the column of this <code>IntersectionMatrix</code>
   *      , indicating the interior, boundary or exterior of the second <code>Geometry</code>
   *@param  minimumDimensionValue  the dimension value with which to compare the
   *      element. The order of dimension values from least to greatest is
   *      <code>{DONTCARE, TRUE, FALSE, 0, 1, 2}</code>.
   */
  public void setAtLeastIfValid(final int row, final int column,
    final Dimension minimumDimensionValue) {
    if (row >= 0 && column >= 0) {
      setAtLeast(row, column, minimumDimensionValue);
    }
  }

  public void setAtLeastIfValid(final Location row, final Location column,
    final Dimension minimumDimensionValue) {
    setAtLeastIfValid(row.getIndex(), column.getIndex(), minimumDimensionValue);
  }

  /**
   *  Returns a nine-character <code>String</code> representation of this <code>IntersectionMatrix</code>
   *  .
   *
   *@return    the nine dimension symbols of this <code>IntersectionMatrix</code>
   *      in row-major order.
   */
  @Override
  public String toString() {
    final StringBuilder buf = new StringBuilder("123456789");
    for (int ai = 0; ai < 3; ai++) {
      for (int bi = 0; bi < 3; bi++) {
        buf.setCharAt(3 * ai + bi, this.matrix[ai][bi].getSymbol());
      }
    }
    return buf.toString();
  }

  /**
   *  Transposes this IntersectionMatrix.
   *
   *@return    this <code>IntersectionMatrix</code> as a convenience
   */
  public IntersectionMatrix transpose() {
    Dimension temp = this.matrix[1][0];
    this.matrix[1][0] = this.matrix[0][1];
    this.matrix[0][1] = temp;
    temp = this.matrix[2][0];
    this.matrix[2][0] = this.matrix[0][2];
    this.matrix[0][2] = temp;
    temp = this.matrix[2][1];
    this.matrix[2][1] = this.matrix[1][2];
    this.matrix[1][2] = temp;
    return this;
  }
}
