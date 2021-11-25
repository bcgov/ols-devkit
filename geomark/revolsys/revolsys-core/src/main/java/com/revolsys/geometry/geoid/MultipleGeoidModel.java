package com.revolsys.geometry.geoid;

import java.util.List;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.model.BoundingBox;

public class MultipleGeoidModel extends AbstractGeoidModel {

  private final List<GeoidModel> geoids;

  private final BoundingBox boundingBox;

  public MultipleGeoidModel(final String geoidName, final Iterable<GeoidModel> geoids) {
    super(geoidName);
    this.geoids = Lists.toArray(geoids);
    this.boundingBox = BoundingBox.bboxNew(geoids);
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  @Override
  public double getGeoidHeight(final double lon, final double lat) {
    for (final GeoidModel geoidModel : this.geoids) {
      final double geoidHeight = geoidModel.getGeoidHeight(lon, lat);
      if (Double.isFinite(geoidHeight)) {
        return geoidHeight;
      }
    }
    return Double.NaN;
  }

}
