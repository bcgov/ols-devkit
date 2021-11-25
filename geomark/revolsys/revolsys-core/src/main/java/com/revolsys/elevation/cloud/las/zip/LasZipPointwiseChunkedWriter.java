package com.revolsys.elevation.cloud.las.zip;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.elevation.cloud.las.LasPointCloudHeader;
import com.revolsys.elevation.cloud.las.LasPointCloudWriter;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingInteger;
import com.revolsys.math.arithmeticcoding.ArithmeticEncoder;
import com.revolsys.spring.resource.Resource;

public class LasZipPointwiseChunkedWriter extends LasPointCloudWriter {
  private long chunkMaxSize = Integer.MAX_VALUE;

  private int chunkPointCount;

  private final List<Integer> chunksByteCounts = new ArrayList<>();

  private int chunksCount = 0;

  private final List<Integer> chunksPointCounts = new ArrayList<>();

  private long chunkStartPosition;

  private long chunkTableStartPosition = -1;

  private LasZipItemCodec[] codecs;

  private int contextIndex;

  private ArithmeticEncoder encoder;

  private final LasZipHeader lasZipHeader;

  public LasZipPointwiseChunkedWriter(final LasPointCloud pointCloud,
    final LasPointCloudHeader header, final LasZipHeader lasZipHeader, final Resource resource,
    final MapEx properties) {
    super(resource);
    setProperties(properties);
    setPointCloud(pointCloud);
    this.header = header;
    this.lasZipHeader = lasZipHeader;
    this.chunkMaxSize = this.lasZipHeader.getChunkSize();
    this.chunkPointCount = (int)this.chunkMaxSize;
  }

  private void addChunkToTable() {
    final long position = this.out.position();
    if (this.chunkMaxSize == Integer.MAX_VALUE) {
      this.chunksPointCounts.add(this.chunkPointCount);
    }
    this.chunksByteCounts.add((int)(position - this.chunkStartPosition));
    this.chunkStartPosition = position;
    this.chunksCount++;
  }

  @Override
  public void close() {
    done();
    super.close();
  }

  public void done() {
    if (this.chunkPointCount == 0) {
      if (this.chunkStartPosition != 0) {
        writeChunkTable();
      }
    } else {
      this.encoder.done();
      if (this.chunkStartPosition != 0) {
        if (this.chunkPointCount != 0) {
          addChunkToTable();
        }
        writeChunkTable();
      }
    }
  }

  @Override
  public void open() {
    super.open();
    this.encoder = new ArithmeticEncoder(this.out);
    this.codecs = this.lasZipHeader.newLazCodecs(this.encoder);
    this.chunkPointCount = 0;
    this.chunksCount = 0;
    if (this.out.isSeekable()) {
      this.chunkTableStartPosition = this.out.position();
    } else {
      this.chunkTableStartPosition = -1;
    }
    this.out.putLong(this.chunkTableStartPosition);
    this.chunkStartPosition = this.out.position();
  }

  private void startChunk() {
    if (this.chunksCount == Integer.MAX_VALUE) {
      this.chunksCount = 0;
      if (this.out.isSeekable()) {
        this.chunkTableStartPosition = this.out.position();
      } else {
        this.chunkTableStartPosition = -1;
      }
      this.out.putLong(this.chunkTableStartPosition);
      this.chunkStartPosition = this.out.position();
    }
  }

  private void writeChunkTable() {
    final long position = this.out.position();
    if (this.chunkTableStartPosition != -1) {
      this.out.seek(this.chunkTableStartPosition);
      this.out.putLong(position);
      this.out.seek(position);
    }
    this.out.putInt(0);
    this.out.putInt(this.chunksCount);

    if (this.chunksCount > 0) {
      this.encoder.init();
      final ArithmeticCodingInteger ic = new ArithmeticCodingInteger(this.encoder, 32, 2);
      ic.initCompressor();
      for (int i = 0; i < this.chunksCount; i++) {
        if (this.chunkMaxSize == Integer.MAX_VALUE) {
          ic.compress(i != 0 ? this.chunksPointCounts.get(i - 1) : 0, this.chunksPointCounts.get(i),
            0);
        }
        ic.compress(i != 0 ? this.chunksByteCounts.get(i - 1) : 0, this.chunksByteCounts.get(i), 1);
      }
      this.encoder.done();
    }
    if (this.chunkTableStartPosition == -1) {
      this.out.putLong(position);
    }
  }

  @Override
  public void writePoint(final LasPoint point) {
    this.header.addCounts(point);
    if (this.chunkPointCount == this.chunkMaxSize) {
      this.encoder.done();
      addChunkToTable();
      startChunk();
      this.chunkPointCount = 0;
    }
    this.chunkPointCount++;
    if (this.chunkPointCount == 1) {
      point.writeLasPoint(this.out);
      for (final LasZipItemCodec codec : this.codecs) {
        this.contextIndex = codec.init(point, this.contextIndex);
      }
      this.encoder.init();
    } else {
      for (final LasZipItemCodec codec : this.codecs) {
        this.contextIndex = codec.write(point, this.contextIndex);
      }
    }
  }
}
