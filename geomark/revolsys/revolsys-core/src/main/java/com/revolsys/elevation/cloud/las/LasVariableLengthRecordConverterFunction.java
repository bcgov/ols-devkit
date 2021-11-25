package com.revolsys.elevation.cloud.las;

import java.util.function.BiFunction;

import com.revolsys.util.Pair;

public class LasVariableLengthRecordConverterFunction extends LasVariableLengthRecordConverter {

  private final BiFunction<LasPointCloudHeader, byte[], Object> readFunction;

  private final BiFunction<LasPointCloud, LasVariableLengthRecord, byte[]> toBytesFunction;

  public LasVariableLengthRecordConverterFunction(final Pair<String, Integer> key,
    final BiFunction<LasPointCloudHeader, byte[], Object> readFunction) {
    this(key, readFunction, null);
  }

  public LasVariableLengthRecordConverterFunction(final Pair<String, Integer> key,
    final BiFunction<LasPointCloudHeader, byte[], Object> readFunction,
    final BiFunction<LasPointCloud, LasVariableLengthRecord, byte[]> toBytesFunction) {
    super(key);
    this.readFunction = readFunction;
    this.toBytesFunction = toBytesFunction;
  }

  public LasVariableLengthRecordConverterFunction(final String userId, final int recordId,
    final BiFunction<LasPointCloudHeader, byte[], Object> readFunction) {
    this(new Pair<>(userId, recordId), readFunction);
  }

  public LasVariableLengthRecordConverterFunction(final String userId, final int recordId,
    final BiFunction<LasPointCloudHeader, byte[], Object> readFunction,
    final BiFunction<LasPointCloud, LasVariableLengthRecord, byte[]> toBytesFunction) {
    this(new Pair<>(userId, recordId), readFunction, toBytesFunction);
  }

  @Override
  public byte[] objectToBytes(final LasPointCloud pointCloud,
    final LasVariableLengthRecord variable) {
    if (this.toBytesFunction == null) {
      return null;
    } else {
      return this.toBytesFunction.apply(pointCloud, variable);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V readObject(final LasPointCloudHeader header, final byte[] bytes) {
    return (V)this.readFunction.apply(header, bytes);
  }
}
