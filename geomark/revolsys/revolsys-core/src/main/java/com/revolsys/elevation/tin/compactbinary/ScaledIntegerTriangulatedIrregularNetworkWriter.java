package com.revolsys.elevation.tin.compactbinary;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkWriter;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;

public class ScaledIntegerTriangulatedIrregularNetworkWriter extends BaseObjectWithProperties
  implements TriangulatedIrregularNetworkWriter {

  private final Resource resource;

  public ScaledIntegerTriangulatedIrregularNetworkWriter(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public void close() {
    super.close();
  }

  @Override
  public void flush() {
  }

  @Override
  public void open() {
  }

  @Override
  public void write(final TriangulatedIrregularNetwork tin) {
    try (
      ChannelWriter out = this.resource.newChannelWriter()) {
      final BoundingBox tinBoundingBox = tin.getBoundingBox();

      final GeometryFactory geometryFactory = tin.getGeometryFactory().convertToFixed(1000.0);

      out.putBytes(ScaledIntegerTriangulatedIrregularNetwork.FILE_TYPE_BYTES);
      out.putShort(ScaledIntegerTriangulatedIrregularNetwork.VERSION);
      geometryFactory.writeOffsetScaled3d(out);
      out.putDouble(tinBoundingBox.getMinX()); // minX
      out.putDouble(tinBoundingBox.getMinY()); // minY
      out.putDouble(tinBoundingBox.getMaxX()); // maxX
      out.putDouble(tinBoundingBox.getMaxY()); // maxY

      tin.forEachTriangle(triangle -> {
        for (int i = 0; i < 3; i++) {
          final double x = triangle.getX(i);
          final double y = triangle.getY(i);
          final double z = triangle.getZ(i);
          final int intX = geometryFactory.toIntX(x);
          out.putInt(intX);
          final int intY = geometryFactory.toIntY(y);
          out.putInt(intY);
          final int intZ = geometryFactory.toIntZ(z);
          out.putInt(intZ);
        }
      });
    } catch (final Exception e) {
      throw Exceptions.wrap("Unable to write: " + this.resource, e);
    }
  }
}
