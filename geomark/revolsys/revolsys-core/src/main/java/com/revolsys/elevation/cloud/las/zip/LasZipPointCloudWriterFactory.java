package com.revolsys.elevation.cloud.las.zip;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.elevation.cloud.las.LasPointCloudHeader;
import com.revolsys.elevation.cloud.las.LasPointCloudWriter;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;

public class LasZipPointCloudWriterFactory extends BaseObjectWithProperties {

  private LasZipCompressorType compressor = LasZipCompressorType.POINTWISE;

  private LasZipHeader lasZipHeader;

  private int lasZipVersion = 1;

  private final Resource resource;

  private final LasPointCloud pointCloud;

  private final LasPointCloudHeader header;

  public LasZipPointCloudWriterFactory(final LasPointCloud pointCloud, final Resource resource,
    final MapEx properties) {
    this.resource = resource;
    setProperties(properties);
    this.pointCloud = pointCloud;
    this.header = this.pointCloud.getHeader().clone();
    this.header.clear();

    this.lasZipHeader = LasZipHeader.getLasZipHeader(pointCloud);
    if (this.lasZipHeader == null) {
      this.lasZipHeader = LasZipHeader.newLasZipHeader(pointCloud, this.compressor,
        this.lasZipVersion);
      this.header.addLasProperty(LasZipHeader.KAY_LAS_ZIP, "laszip", this.lasZipHeader);
      this.compressor = this.lasZipHeader.getCompressor();
    }
  }

  public int getLasZipVersion() {
    return this.lasZipVersion;
  }

  public LasPointCloudWriter newWriter() {
    LasPointCloudWriter writer;
    final MapEx properties = getProperties();
    switch (this.compressor) {
      case POINTWISE:
        writer = new LasZipPointwiseWriter(this.pointCloud, this.header, this.lasZipHeader,
          this.resource, properties);
      break;
      case POINTWISE_CHUNKED:
        writer = new LasZipPointwiseChunkedWriter(this.pointCloud, this.header, this.lasZipHeader,
          this.resource, properties);
      break;
      case LAYERED_CHUNKED:
        writer = new LasZipLayeredChunkedWriter(this.pointCloud, this.header, this.lasZipHeader,
          this.resource, properties);
      break;
      default:
        return null;
    }
    writer.open();
    return writer;
  }

  public void setCompressor(final LasZipCompressorType compressor) {
    this.compressor = compressor;
  }

  public void setLasZipVersion(final int lasZipVersion) {
    this.lasZipVersion = lasZipVersion;
  }

}
