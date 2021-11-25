package com.revolsys.elevation.gridded;

import java.util.Map;

import com.revolsys.io.IoFactory;
import com.revolsys.io.Writer;
import com.revolsys.spring.resource.Resource;

public interface GriddedElevationModelWriter extends Writer<GriddedElevationModel> {
  static boolean isWritable(final Object target) {
    return IoFactory.isAvailable(GriddedElevationModelWriterFactory.class, target);
  }

  static GriddedElevationModelWriter newGriddedElevationModelWriter(final Object target,
    final Map<String, ? extends Object> properties) {
    final GriddedElevationModelWriterFactory factory = IoFactory
      .factory(GriddedElevationModelWriterFactory.class, target);
    if (factory == null) {
      return null;
    } else {
      final Resource resource = Resource.getResource(target);
      final GriddedElevationModelWriter writer = factory.newGriddedElevationModelWriter(resource);
      writer.setProperties(properties);
      return writer;
    }
  }
}
