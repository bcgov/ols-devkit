package com.revolsys.gis.postgresql.type;

public class LittleEndianValueGetter extends ValueGetter {
  public LittleEndianValueGetter(final String wkb) {
    super(wkb);
  }

  @Override
  public int getInt() {
    final int b1 = read();
    final int b2 = read();
    final int b3 = read();
    final int b4 = read();
    if ((b1 | b2 | b3 | b4) < 0) {
      throw new IllegalStateException();
    }
    final int value = (b4 << 24) + (b3 << 16) + (b2 << 8) + b1;

    return value;
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
    return ((long)b8 << 56) + //
      ((long)(b7 & 255) << 48) + //
      ((long)(b6 & 255) << 40) + //
      ((long)(b5 & 255) << 32) + //
      ((long)(b4 & 255) << 24) + //
      ((long)(b3 & 255) << 16) + //
      ((long)(b2 & 255) << 8) + //
      ((long)(b1 & 255) << 0);
  }

  @Override
  public boolean isBigEndian() {
    return false;
  }
}
