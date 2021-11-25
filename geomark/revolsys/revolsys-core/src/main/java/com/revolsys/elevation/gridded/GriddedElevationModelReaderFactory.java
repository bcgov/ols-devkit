package com.revolsys.elevation.gridded;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.geoid.GeoidModelReader;
import com.revolsys.geometry.geoid.GeoidModelReaderFactory;
import com.revolsys.geometry.geoid.GriddedElevationModelGeoidModelReader;
import com.revolsys.spring.resource.Resource;

public interface GriddedElevationModelReaderFactory extends GeoidModelReaderFactory {
  @Override
  default GeoidModelReader newGeoidModelReader(final Resource resource, final MapEx properties) {
    return new GriddedElevationModelGeoidModelReader(resource, properties);
  }

  default GriddedElevationModel newGriddedElevationModel(final Resource resource,
    final MapEx properties) {
    try (
      GriddedElevationModelReader reader = newGriddedElevationModelReader(resource, properties)) {
      return reader.read();
    }
  }

  GriddedElevationModelReader newGriddedElevationModelReader(Resource resource, MapEx properties);
}
