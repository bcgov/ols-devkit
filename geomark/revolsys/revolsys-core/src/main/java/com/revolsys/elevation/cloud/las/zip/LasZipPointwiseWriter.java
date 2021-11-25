package com.revolsys.elevation.cloud.las.zip;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.elevation.cloud.las.LasPointCloudHeader;
import com.revolsys.elevation.cloud.las.LasPointCloudWriter;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.math.arithmeticcoding.ArithmeticEncoder;
import com.revolsys.spring.resource.Resource;

public class LasZipPointwiseWriter extends LasPointCloudWriter {

  private LasZipItemCodec[] codecs;

  private int contextIndex;

  private int count = 0;

  private ArithmeticEncoder encoder;

  private final LasZipHeader lasZipHeader;

  public LasZipPointwiseWriter(final LasPointCloud pointCloud, final LasPointCloudHeader header,
    final LasZipHeader lasZipHeader, final Resource resource, final MapEx properties) {
    super(resource);
    setProperties(properties);
    setPointCloud(pointCloud);
    this.header = header;
    this.lasZipHeader = lasZipHeader;
  }

  @Override
  public void close() {
    done();
    super.close();
  }

  public void done() {
    this.encoder.done();
  }

  @Override
  public void open() {
    super.open();
    this.encoder = new ArithmeticEncoder(this.out);
    this.codecs = this.lasZipHeader.newLazCodecs(this.encoder);
  }

  @Override
  public void writePoint(final LasPoint point) {
    this.count++;
    this.header.addCounts(point);
    if (this.count == 1) {
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
