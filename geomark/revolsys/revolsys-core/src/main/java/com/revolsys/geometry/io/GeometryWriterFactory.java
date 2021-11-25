package com.revolsys.geometry.io;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Set;

import org.jeometry.common.data.type.DataType;

import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.set.Sets;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.io.FileIoFactory;
import com.revolsys.spring.resource.Resource;

public interface GeometryWriterFactory extends FileIoFactory {
  static Set<DataType> ALL_GEOMETRY_TYPES = Sets.newLinkedHash(//
    GeometryDataTypes.GEOMETRY, //
    GeometryDataTypes.GEOMETRY_COLLECTION, //
    GeometryDataTypes.POINT, //
    GeometryDataTypes.MULTI_POINT, //
    GeometryDataTypes.LINEAR_RING, //
    GeometryDataTypes.LINE_STRING, //
    GeometryDataTypes.MULTI_LINE_STRING, //
    GeometryDataTypes.POLYGON, //
    GeometryDataTypes.MULTI_POLYGON);

  GeometryWriter newGeometryWriter(final Resource resource, MapEx properties);

  GeometryWriter newGeometryWriter(String baseName, OutputStream out, Charset charset);

}
