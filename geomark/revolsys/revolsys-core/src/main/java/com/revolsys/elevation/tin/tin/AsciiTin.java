package com.revolsys.elevation.tin.tin;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkReaderFactory;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkWriter;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkWriterFactory;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.AbstractIoFactory;
import com.revolsys.spring.resource.Resource;

public class AsciiTin extends AbstractIoFactory
  implements TriangulatedIrregularNetworkReaderFactory, TriangulatedIrregularNetworkWriterFactory {
  public AsciiTin() {
    super("ASCII TIN");
    addMediaTypeAndFileExtension("image/x-tin", "tin");
  }

  @Override
  public boolean isReadFromZipFileSupported() {
    return true;
  }

  @Override
  public TriangulatedIrregularNetwork newTriangulatedIrregularNetwork(final Resource resource,
    final MapEx properties) {
    try {
      GeometryFactory geometryFactory = GeometryFactory.floating3d(resource);
      if (geometryFactory == null) {
        geometryFactory = Maps.get(properties, TriangulatedIrregularNetwork.GEOMETRY_FACTORY);
        if (geometryFactory == null) {
          geometryFactory = GeometryFactory.DEFAULT_3D;
        }
      }
      try (
        AsciiTinReader tinReader = new AsciiTinReader(geometryFactory, resource)) {
        return tinReader.read();
      }
    } catch (final Throwable e) {
      throw Exceptions.wrap("Error reading: " + resource, e);
    }
  }

  @Override
  public TriangulatedIrregularNetworkWriter newTriangulatedIrregularNetworkWriter(
    final Resource resource) {
    return new AsciiTinWriter(resource);
  }

}
