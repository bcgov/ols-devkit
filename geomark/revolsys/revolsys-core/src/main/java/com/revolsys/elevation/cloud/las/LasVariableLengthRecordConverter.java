package com.revolsys.elevation.cloud.las;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.revolsys.util.Pair;

public abstract class LasVariableLengthRecordConverter {

  public static LasVariableLengthRecordConverter bytes(final Pair<String, Integer> key) {
    return new LasVariableLengthRecordConverterFunction(key, (header, bytes) -> {
      return bytes;
    });
  }

  public static LasVariableLengthRecordConverter doubleArray(final Pair<String, Integer> key) {
    return new LasVariableLengthRecordConverterFunction(key, (header, bytes) -> {
      final int length = bytes.length / 8;
      final double[] values = new double[length];
      final ByteBuffer buffer = ByteBuffer.wrap(bytes);
      buffer.order(ByteOrder.LITTLE_ENDIAN);
      for (int i = 0; i < length; i++) {
        final double value = buffer.getDouble();
        values[i] = value;
      }
      return values;
    });
  }

  public static int[] getUnsignedShortArray(final byte[] bytes) {
    final int length = bytes.length / 2;
    final int[] values = new int[length];
    final ByteBuffer buffer = ByteBuffer.wrap(bytes);
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < length; i++) {
      final short value = buffer.getShort();
      values[i] = Short.toUnsignedInt(value);
    }
    return values;
  }

  private final Pair<String, Integer> key;

  public LasVariableLengthRecordConverter(final Pair<String, Integer> key) {
    this.key = key;
    LasPointCloudHeader.addVariableLengthRecordConverter(this);
  }

  public Pair<String, Integer> getKey() {
    return this.key;
  }

  public byte[] objectToBytes(final LasPointCloud pointCloud,
    final LasVariableLengthRecord variable) {
    return null;
  }

  public abstract <V> V readObject(LasPointCloudHeader header, byte[] bytes);
}
