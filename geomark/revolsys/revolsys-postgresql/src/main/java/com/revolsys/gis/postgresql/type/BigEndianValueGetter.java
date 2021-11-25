package com.revolsys.gis.postgresql.type;

public class BigEndianValueGetter extends ValueGetter {

  public BigEndianValueGetter(final String wkb) {
    super(wkb);
  }

  @Override
  public int getInt() {
    final int ch1 = read();
    final int ch2 = read();
    final int ch3 = read();
    final int ch4 = read();
    if ((ch1 | ch2 | ch3 | ch4) < 0) {
      throw new IllegalStateException();
    }
    return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0);
  }

  @Override
  public long getLong() {
    final int b1 = read();
    final int b2 = read();
    final int b3 = read();
    final int b4 = read();
    final int b5 = read();
    final int b6 = read();
    final int b7 = read();
    final int b8 = read();
    return ((long)b1 << 56) + //
      ((long)(b2 & 255) << 48) + //
      ((long)(b3 & 255) << 40) + //
      ((long)(b4 & 255) << 32) + //
      ((long)(b5 & 255) << 24) + //
      ((long)(b6 & 255) << 16) + //
      ((long)(b7 & 255) << 8) + //
      ((long)(b8 & 255) << 0);
  }

  @Override
  public boolean isBigEndian() {
    return true;
  }
}
