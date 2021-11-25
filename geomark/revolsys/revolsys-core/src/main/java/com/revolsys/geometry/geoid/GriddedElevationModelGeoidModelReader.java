package com.revolsys.geometry.geoid;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelReader;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;

public class GriddedElevationModelGeoidModelReader extends BaseObjectWithProperties
  implements GeoidModelReader {

  private final GriddedElevationModelReader reader;

  private final String geoidName;

  public GriddedElevationModelGeoidModelReader(final Resource resource, final MapEx properties) {
    this.geoidName = properties.getString("geoidName", resource.getBaseName());
    this.reader = GriddedElevationModelReader.newGriddedElevationModelReader(resource, properties);
  }

  @Override
  public void close() {
    if (this.reader != null) {
      this.reader.close();
    }
    super.close();
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.reader.getBoundingBox();
  }

  @Override
  public GeoidModel read() {
    final GriddedElevationModel grid = this.reader.read();
    if (grid == null) {
      return null;
    } else {
      return new GridGeoidModel(this.geoidName, grid);
    }
  }
}
