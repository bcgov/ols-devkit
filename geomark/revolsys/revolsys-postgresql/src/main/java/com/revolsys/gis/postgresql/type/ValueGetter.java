package com.revolsys.gis.postgresql.type;

public abstract class ValueGetter {

  protected String data;

  protected int index = 2;

  public ValueGetter(final String data) {
    this.data = data;
  }

  public int getByte() {
    return (byte)read();
  }

  public double getDouble() {
    final long bitrep = getLong();
    return Double.longBitsToDouble(bitrep);
  }

  public abstract int getInt();

  public abstract long getLong();

  public abstract boolean isBigEndian();

  /**
   * Get a byte, should be equal for all endians
   */
  public int read() {
    final String data = this.data;
    int index = this.index;
    if (this.index < data.length() - 1) {
      int value;
      final char highChar = data.charAt(index++);
      if (highChar >= '0' && highChar <= '9') {
        value = highChar - '0';
      } else if (highChar >= 'A' && highChar <= 'F') {
        value = highChar - 'A' + 10;
      } else if (highChar >= 'a' && highChar <= 'f') {
        value = highChar - 'a' + 10;
      } else {
        throw new IllegalArgumentException("No valid Hex char " + highChar);
      }
      value <<= 4;

      final char lowChar = data.charAt(index++);
      if (lowChar >= '0' && lowChar <= '9') {
        value += lowChar - '0';
      } else if (lowChar >= 'A' && lowChar <= 'F') {
        value += lowChar - 'A' + 10;
      } else if (lowChar >= 'a' && lowChar <= 'f') {
        value += lowChar - 'a' + 10;
      } else {
        throw new IllegalArgumentException("No valid Hex char " + lowChar);
      }
      this.index = index;
      return value;
    } else {
      return -1;
    }
  }
}
