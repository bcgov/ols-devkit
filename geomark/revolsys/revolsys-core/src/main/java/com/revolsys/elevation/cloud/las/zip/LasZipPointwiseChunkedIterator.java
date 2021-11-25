package com.revolsys.elevation.cloud.las.zip;

import java.util.Iterator;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.elevation.cloud.las.LasPointCloudIterator;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.io.channels.DataReader;
import com.revolsys.math.arithmeticcoding.ArithmeticDecoder;

public class LasZipPointwiseChunkedIterator extends LasPointCloudIterator {

  private final ArithmeticDecoder decoder;

  private final LasZipItemCodec[] codecs;

  private final long chunkTableOffset;

  private final long chunkSize;

  private long chunkReadCount;

  private int context = 0;

  public LasZipPointwiseChunkedIterator(final LasPointCloud pointCloud,
    final DataReader reader) {
    super(pointCloud, reader);
    this.decoder = new ArithmeticDecoder();
    final LasZipHeader lasZipHeader = LasZipHeader.getLasZipHeader(pointCloud);
    this.codecs = lasZipHeader.newLazCodecs(this.decoder);

    this.chunkTableOffset = reader.getLong();
    this.chunkSize = lasZipHeader.getChunkSize();
    this.chunkReadCount = this.chunkSize;
  }

  @Override
  public boolean hasNext() {
    return this.index < this.pointCount;
  }

  @Override
  public Iterator<LasPoint> iterator() {
    return this;
  }

  @Override
  protected LasPoint readNext() {
    try {
      LasPoint point;
      if (this.chunkSize == this.chunkReadCount) {
        point = this.pointFormat.readLasPoint(this.pointCloud, this.reader);
        for (final LasZipItemCodec codec : this.codecs) {
          this.context = codec.init(point, this.context);
        }
        this.decoder.init(this.reader);
        this.chunkReadCount = 0;
      } else {
        point = this.pointFormat.newLasPoint(this.pointCloud);
        for (final LasZipItemCodec pointDecompressor : this.codecs) {
          this.context = pointDecompressor.read(point, this.context);
        }
      }
      this.chunkReadCount++;
      return point;
    } catch (final Exception e) {
      close();
      throw Exceptions.wrap("Error decompressing: " + this.pointCloud.getResource(), e);
    }
  }

}
