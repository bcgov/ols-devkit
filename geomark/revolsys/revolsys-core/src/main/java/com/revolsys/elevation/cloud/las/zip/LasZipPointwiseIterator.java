package com.revolsys.elevation.cloud.las.zip;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.elevation.cloud.las.LasPointCloudIterator;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.io.channels.DataReader;
import com.revolsys.math.arithmeticcoding.ArithmeticDecoder;

public class LasZipPointwiseIterator extends LasPointCloudIterator {

  private final ArithmeticDecoder decoder;

  private final LasZipItemCodec[] pointDecompressors;

  public LasZipPointwiseIterator(final LasPointCloud pointCloud, final DataReader reader) {
    super(pointCloud, reader);
    this.decoder = new ArithmeticDecoder();
    final LasZipHeader lasZipHeader = LasZipHeader.getLasZipHeader(pointCloud);
    this.pointDecompressors = lasZipHeader.newLazCodecs(this.decoder);
  }

  @Override
  protected LasPoint readNext() {
    try {
      LasPoint point;
      if (this.index == 0) {
        final DataReader reader = this.reader;
        point = this.pointFormat.readLasPoint(this.pointCloud, reader);
        for (final LasZipItemCodec pointDecompressor : this.pointDecompressors) {
          pointDecompressor.init(point, 0);
        }
        this.decoder.init(reader);
      } else {
        point = this.pointFormat.newLasPoint(this.pointCloud);
        for (final LasZipItemCodec pointDecompressor : this.pointDecompressors) {
          pointDecompressor.read(point, 0);
        }
      }
      return point;
    } catch (final Exception e) {
      final long index = this.index;
      close();
      throw Exceptions
        .wrap("Error decompressing: " + this.pointCloud.getResource() + "\npointCount=" + index, e);
    }
  }

}
