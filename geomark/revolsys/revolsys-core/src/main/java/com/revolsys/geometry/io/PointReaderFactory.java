package com.revolsys.geometry.io;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.FileIoFactory;
import com.revolsys.io.ReadIoFactory;
import com.revolsys.spring.resource.Resource;

public interface PointReaderFactory extends FileIoFactory, ReadIoFactory {
  default PointReader newPointReader(final Object source) {
    final Resource resource = Resource.getResource(source);
    return newPointReader(resource);
  }

  default PointReader newPointReader(final Resource resource) {
    return newPointReader(resource, MapEx.EMPTY);
  }

  PointReader newPointReader(final Resource resource, MapEx properties);
}
