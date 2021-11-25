package com.revolsys.record.io.format.scaledint;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.io.GeometryWriter;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.spring.resource.Resource;

public class ScaledIntegerPointCloudGeometryWriter extends AbstractWriter<Geometry>
  implements GeometryWriter {
  private boolean initialized;

  private final Resource resource;

  private ChannelWriter writer;

  private GeometryFactory geometryFactory = GeometryFactory.fixed3d(0, 1000.0, 1000.0, 1000.0);

  private ByteBuffer byteBuffer;

  public ScaledIntegerPointCloudGeometryWriter(final Resource resource, final MapEx properties) {
    this.resource = resource;
    setProperties(properties);
  }

  @Override
  public void close() {
    final ChannelWriter writer = this.writer;
    this.writer = null;
    if (writer != null) {
      writer.close();
    }
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  private void initialize() throws IOException {
    if (!this.initialized) {
      this.initialized = true;
      final ChannelWriter writer = ChannelWriter.newChannelWriterCompressed(this.resource,
        this.byteBuffer);
      this.writer = writer;
      final GeometryFactory geometryFactory = this.geometryFactory;
      writer.putBytes(ScaledIntegerPointCloud.FILE_TYPE_HEADER_BYTES); // File
                                                                       // type
      writer.putShort(ScaledIntegerPointCloud.VERSION); // version
      writer.putShort((short)0); // Flags
      geometryFactory.writeOffsetScaled3d(writer);
    }
  }

  public void setByteBuffer(final ByteBuffer byteBuffer) {
    this.byteBuffer = byteBuffer;
    if (byteBuffer != null) {
      byteBuffer.order(ByteOrder.BIG_ENDIAN);
    }
  }

  @Override
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    double defaultScale;
    if (geometryFactory.isGeographic()) {
      defaultScale = 1000;
    } else {
      defaultScale = 10000000;
    }
    this.geometryFactory = geometryFactory.convertToFixed(defaultScale);

  }

  @Override
  public String toString() {
    if (this.resource == null) {
      return super.toString();
    } else {
      return this.resource.toString();
    }
  }

  public void write(final double x, final double y, final double z) {
    try {
      initialize();
      final ChannelWriter writer = this.writer;
      final GeometryFactory geometryFactory = this.geometryFactory;
      final int xInt = geometryFactory.toIntX(x);
      writer.putInt(xInt);
      final int yInt = geometryFactory.toIntY(y);
      writer.putInt(yInt);
      final int zInt = geometryFactory.toIntZ(z);
      writer.putInt(zInt);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public void write(final Geometry geometry) {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      final double x = point.getX();
      final double y = point.getY();
      final double z = point.getZ();
      write(x, y, z);
    } else {
      throw new IllegalArgumentException("Only points supported: " + geometry);
    }
  }
}
