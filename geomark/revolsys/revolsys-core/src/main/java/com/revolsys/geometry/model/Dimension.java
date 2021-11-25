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

import java.util.HashMap;
import java.util.Map;

/**
 * Provides constants representing the dimensions of a point, a curve and a surface.
 * Also provides constants representing the dimensions of the empty geometry and
 * non-empty geometries, and the wildcard constant {@link #DONTCARE} meaning "any dimension".
 * These constants are used as the entries in {@link IntersectionMatrix}s.
 *
 * @version 1.7
 */
public enum Dimension {

  /** Dimension value of the empty geometry (-1). */
  FALSE(-1, 'F'), //
  /** Dimension value of non-empty geometries (= {P, L, A}) */
  TRUE(-2, 'T'), //
  /** Dimension value for any dimension (= {FALSE, TRUE}). */
  DONTCARE(-3, '*'), //
  /** Dimension value of a point (0). */
  P(0, '0'), //
  /** Dimension value of a curve (1). */
  L(1, '1'), //
  /** Dimension value of a surface (2). */
  A(2, '2'),//
  ;

  private static Map<Character, Dimension> DIMENSION_BY_SYMBOL = new HashMap<>();

  static {
    for (final Dimension dimension : values()) {
      final char symbol = dimension.symbol;
      DIMENSION_BY_SYMBOL.put(symbol, dimension);
    }
  }

  /**
   *  Converts the dimension symbol to a dimension value, for example, <code>'*' => DONTCARE</code>
   *  .
   *
   *@param  dimensionSymbol  a character for use in the string representation of
   *      an <code>IntersectionMatrix</code>. Possible values are <code>{T, F, * , 0, 1, 2}</code>
   *      .
   *@return a number that can be stored in the <code>IntersectionMatrix</code>
   *      . Possible values are <code>{TRUE, FALSE, DONTCARE, 0, 1, 2}</code>.
   */
  public static Dimension toDimensionValue(final char dimensionSymbol) {
    final Dimension dimension = DIMENSION_BY_SYMBOL.get(dimensionSymbol);
    if (dimension == null) {
      throw new IllegalArgumentException("Unknown dimension symbol: " + dimensionSymbol);
    } else {
      return dimension;
    }
  }

  private int code;

  private char symbol;

  private boolean isTrue;

  private Dimension(final int code, final char symbol) {
    this.code = code;
    this.symbol = symbol;
    this.isTrue = code == -2 || code >= 0;
  }

  public int getCode() {
    return this.code;
  }

  public char getSymbol() {
    return this.symbol;
  }

  public boolean isArea() {
    return this == A;
  }

  public boolean isGreaterThan(final Dimension dimension) {
    return this.code > dimension.code;
  }

  public boolean isLessThan(final Dimension dimension) {
    return this.code < dimension.code;
  }

  public boolean isLine() {
    return this == L;
  }

  public boolean isPoint() {
    return this == P;
  }

  public boolean isTrue() {
    return this.isTrue;
  }

  @Override
  public String toString() {
    return Character.toString(this.symbol);
  }
}
