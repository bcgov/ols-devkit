package com.revolsys.geometry.index;

/**
 * DoubleBits manipulates Double numbers
 * by using bit manipulation and bit-field extraction.
 * For some operations (such as determining the exponent)
 * this is more accurate than using mathematical operations
 * (which suffer from round-off error).
 * <p>
 * The algorithms and constants in this class
 * apply only to IEEE-754 double-precision floating point format.
 *
 * @version 1.7
 */
public class DoubleBits {

  public static final int EXPONENT_BIAS = 1023;

  public static int exponent(final double value) {
    final long valueBits = Double.doubleToLongBits(value);
    final int signExp = (int)(valueBits >> 52);
    final int exp = signExp & 0x07ff;
    return exp - EXPONENT_BIAS;
  }

  public static double maximumCommonMantissa(final double d1, final double d2) {
    if (d1 == 0.0 || d2 == 0.0) {
      return 0.0;
    }

    final DoubleBits db1 = new DoubleBits(d1);
    final DoubleBits db2 = new DoubleBits(d2);

    if (db1.getExponent() != db2.getExponent()) {
      return 0.0;
    }

    final int maxCommon = db1.numCommonMantissaBits(db2);
    db1.zeroLowerBits(64 - (12 + maxCommon));
    return db1.getDouble();
  }

  public static double powerOf2(final int exp) {
    if (exp > 1023 || exp < -1022) {
      throw new IllegalArgumentException("Exponent out of bounds");
    }
    final long expBias = exp + EXPONENT_BIAS;
    final long bits = expBias << 52;
    return Double.longBitsToDouble(bits);
  }

  public static String toBinaryString(final double d) {
    final DoubleBits db = new DoubleBits(d);
    return db.toString();
  }

  public static double truncateToPowerOfTwo(final double d) {
    final DoubleBits db = new DoubleBits(d);
    db.zeroLowerBits(52);
    return db.getDouble();
  }

  private final double x;

  private long xBits;

  public DoubleBits(final double x) {
    this.x = x;
    this.xBits = Double.doubleToLongBits(x);
  }

  /**
   * Determines the exponent for the number
   */
  public int biasedExponent() {
    final int signExp = (int)(this.xBits >> 52);
    final int exp = signExp & 0x07ff;
    return exp;
  }

  public int getBit(final int i) {
    final long mask = 1L << i;
    return (this.xBits & mask) != 0 ? 1 : 0;
  }

  public double getDouble() {
    return Double.longBitsToDouble(this.xBits);
  }

  /**
   * Determines the exponent for the number
   */
  public int getExponent() {
    return biasedExponent() - EXPONENT_BIAS;
  }

  /**
   * This computes the number of common most-significant bits in the mantissa.
   * It does not count the hidden bit, which is always 1.
   * It does not determine whether the numbers have the same exponent - if they do
   * not, the value computed by this function is meaningless.
   * @param db
   * @return the number of common most-significant mantissa bits
   */
  public int numCommonMantissaBits(final DoubleBits db) {
    for (int i = 0; i < 52; i++) {
      final int bitIndex = i + 12;
      if (getBit(i) != db.getBit(i)) {
        return i;
      }
    }
    return 52;
  }

  /**
   * A representation of the Double bits formatted for easy readability
   */
  @Override
  public String toString() {
    final String numStr = Long.toBinaryString(this.xBits);
    // 64 zeroes!
    final String zero64 = "0000000000000000000000000000000000000000000000000000000000000000";
    final String padStr = zero64 + numStr;
    final String bitStr = padStr.substring(padStr.length() - 64);
    final String str = bitStr.substring(0, 1) + "  " + bitStr.substring(1, 12) + "(" + getExponent()
      + ") " + bitStr.substring(12) + " [ " + this.x + " ]";
    return str;
  }

  public void zeroLowerBits(final int nBits) {
    final long invMask = (1L << nBits) - 1L;
    final long mask = ~invMask;
    this.xBits &= mask;
  }
}
