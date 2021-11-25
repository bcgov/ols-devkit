package com.revolsys.io.map;

import com.revolsys.io.FileIoFactory;
import com.revolsys.io.ReadIoFactory;
import com.revolsys.spring.resource.Resource;

public interface MapReaderFactory extends FileIoFactory, ReadIoFactory {
  default MapReader newMapReader(final Object source) {
    final Resource resource = Resource.getResource(source);
    return newMapReader(resource);
  }

  MapReader newMapReader(final Resource resource);
}
