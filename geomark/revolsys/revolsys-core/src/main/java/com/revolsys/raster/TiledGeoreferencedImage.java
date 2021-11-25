package com.revolsys.raster;

import java.util.List;

import com.revolsys.geometry.model.BoundingBox;

public interface TiledGeoreferencedImage extends GeoreferencedImage {
  List<GeoreferencedImageMapTile> getOverlappingMapTiles(final BoundingBox boundingBox,
    double resolution);

  double getResolution(BoundingBox boundingBox, double resolution);
}
