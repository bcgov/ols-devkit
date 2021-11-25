package com.revolsys.elevation.cloud.las.zip;

import java.util.function.BiFunction;

import com.revolsys.collection.map.IntHashMap;
import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.elevation.cloud.las.LasPointCloudIterator;
import com.revolsys.io.channels.ChannelReader;

public enum LasZipCompressorType {
  POINTWISE(1, LasZipPointwiseIterator::new), //
  POINTWISE_CHUNKED(2, LasZipPointwiseChunkedIterator::new), //
  LAYERED_CHUNKED(3, LasZipLayeredChunkedIterator::new) //
  ;

  private static final IntHashMap<LasZipCompressorType> FORMAT_BY_ID = new IntHashMap<>();
  static {
    for (final LasZipCompressorType code : values()) {
      FORMAT_BY_ID.put(code.id, code);
    }
  }

  public static LasZipCompressorType getById(final int id) {
    final LasZipCompressorType code = FORMAT_BY_ID.get(id);
    if (code == null) {
      throw new IllegalArgumentException("Unsupported Las Point compressor=" + id);
    } else {
      return code;
    }
  }

  private BiFunction<LasPointCloud, ChannelReader, LasPointCloudIterator> iteratorConstructor;

  private int id;

  private LasZipCompressorType(final int id,
    final BiFunction<LasPointCloud, ChannelReader, LasPointCloudIterator> iteratorConstructor) {
    this.id = id;
    this.iteratorConstructor = iteratorConstructor;
  }

  public int getId() {
    return this.id;
  }

  public LasPointCloudIterator newIterator(final LasPointCloud pointCloud,
    final ChannelReader reader) {
    return this.iteratorConstructor.apply(pointCloud, reader);
  }
}
