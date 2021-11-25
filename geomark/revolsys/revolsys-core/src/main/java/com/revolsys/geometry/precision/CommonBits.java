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
package com.revolsys.geometry.precision;

/**
 * Determines the maximum number of common most-significant
 * bits in the mantissa of one or numbers.
 * Can be used to compute the double-precision number which
 * is represented by the common bits.
 * If there are no common bits, the number computed is 0.0.
 *
 * @version 1.7
 */
public class CommonBits {

  /**
   * Extracts the i'th bit of a bitstring.
   *
   * @param bits the bitstring to extract from
   * @param i the bit to extract
   * @return the value of the extracted bit
   */
  public static int getBit(final long bits, final int i) {
    final long mask = 1L << i;
    return (bits & mask) != 0 ? 1 : 0;
  }

  /**
   * This computes the number of common most-significant bits in the mantissas
   * of two double-precision numbers.
   * It does not count the hidden bit, which is always 1.
   * It does not determine whether the numbers have the same exponent - if they do
   * not, the value computed by this function is meaningless.
   *
   * @param num1 the first number
   * @param num2 the second number
   * @return the number of common most-significant mantissa bits
   */
  public static int numCommonMostSigMantissaBits(final long num1, final long num2) {
    int count = 0;
    for (int i = 52; i >= 0; i--) {
      if (getBit(num1, i) != getBit(num2, i)) {
        return count;
      }
      count++;
    }
    return 52;
  }

  /**
   * Computes the bit pattern for the sign and exponent of a
   * double-precision number.
   *
   * @param num
   * @return the bit pattern for the sign and exponent
   */
  public static long signExpBits(final long num) {
    return num >> 52;
  }

  /**
   * Zeroes the lower n bits of a bitstring.
   *
   * @param bits the bitstring to alter
   * @return the zeroed bitstring
   */
  public static long zeroLowerBits(final long bits, final int nBits) {
    final long invMask = (1L << nBits) - 1L;
    final long mask = ~invMask;
    final long zeroed = bits & mask;
    return zeroed;
  }

  private long commonBits = 0;

  private int commonMantissaBitsCount = 53;

  private long commonSignExp;

  private boolean isFirst = true;

  public CommonBits() {
  }

  public void add(final double num) {
    final long numBits = Double.doubleToLongBits(num);
    if (this.isFirst) {
      this.commonBits = numBits;
      this.commonSignExp = signExpBits(this.commonBits);
      this.isFirst = false;
      return;
    }

    final long numSignExp = signExpBits(numBits);
    if (numSignExp != this.commonSignExp) {
      this.commonBits = 0;
      return;
    }

    // System.out.println(toString(commonBits));
    // System.out.println(toString(numBits));
    this.commonMantissaBitsCount = numCommonMostSigMantissaBits(this.commonBits, numBits);
    this.commonBits = zeroLowerBits(this.commonBits, 64 - (12 + this.commonMantissaBitsCount));
    // System.out.println(toString(commonBits));
  }

  public double getCommon() {
    return Double.longBitsToDouble(this.commonBits);
  }

  /**
   * A representation of the Double bits formatted for easy readability
   */
  public String toString(final long bits) {
    final double x = Double.longBitsToDouble(bits);
    final String numStr = Long.toBinaryString(bits);
    final String padStr = "0000000000000000000000000000000000000000000000000000000000000000"
      + numStr;
    final String bitStr = padStr.substring(padStr.length() - 64);
    final String str = bitStr.substring(0, 1) + "  " + bitStr.substring(1, 12) + "(exp) "
      + bitStr.substring(12) + " [ " + x + " ]";
    return str;
  }

}
