package com.revolsys.raster.io.format.tiff.image;

import java.util.List;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageMapTile;
import com.revolsys.raster.io.format.tiff.TiffDirectory;

public interface TiffImage extends GeoreferencedImage {
  List<GeoreferencedImageMapTile> getOverlappingMapTiles(BoundingBox boundingBox);

  TiffDirectory getTiffDirectory();
}
