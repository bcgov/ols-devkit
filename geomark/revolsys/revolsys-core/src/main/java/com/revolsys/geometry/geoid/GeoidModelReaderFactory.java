package com.revolsys.geometry.geoid;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.ReadIoFactory;
import com.revolsys.spring.resource.Resource;

public interface GeoidModelReaderFactory extends ReadIoFactory {
  default GeoidModel newGeoidModel(final Resource resource, final MapEx properties) {
    try (
      GeoidModelReader reader = newGeoidModelReader(resource, properties)) {
      return reader.read();
    }
  }

  GeoidModelReader newGeoidModelReader(Resource resource, MapEx properties);
}
