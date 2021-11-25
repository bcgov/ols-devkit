package org.jeometry.coordinatesystem.util;

import java.util.Arrays;

public class ByteArray {
  private final byte[] data;

  private final int hashCode;

  public ByteArray(final byte[] data) {
    this.data = data;
    this.hashCode = Arrays.hashCode(data);
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof ByteArray) {
      final ByteArray array = (ByteArray)object;
      return Arrays.equals(this.data, array.data);
    }
    return false;
  }

  public byte[] getData() {
    return this.data.clone();
  }

  @Override
  public int hashCode() {
    return this.hashCode;
  }

  @Override
  public String toString() {
    return Arrays.toString(this.data);
  }
}
