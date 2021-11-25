package com.revolsys.geometry.io;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.FileIoFactory;
import com.revolsys.io.ReadIoFactory;
import com.revolsys.spring.resource.Resource;

public interface GeometryReaderFactory extends FileIoFactory, ReadIoFactory {
  default GeometryReader newGeometryReader(final Object source) {
    final Resource resource = Resource.getResource(source);
    return newGeometryReader(resource);
  }

  default GeometryReader newGeometryReader(final Resource resource) {
    return newGeometryReader(resource, MapEx.EMPTY);
  }

  GeometryReader newGeometryReader(final Resource resource, MapEx properties);
}
