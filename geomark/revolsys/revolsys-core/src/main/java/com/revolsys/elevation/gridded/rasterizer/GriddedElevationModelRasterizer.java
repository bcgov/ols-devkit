package com.revolsys.elevation.gridded.rasterizer;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.raster.BufferedGeoreferencedImage;
import com.revolsys.util.IconNameProxy;

public interface GriddedElevationModelRasterizer extends BoundingBoxProxy, Cloneable, IconNameProxy,
  MapSerializer, ObjectWithProperties, PropertyChangeSupportProxy {

  GriddedElevationModelRasterizer clone();

  GriddedElevationModel getElevationModel();

  int getHeight();

  double getMaxZ();

  double getMinZ();

  String getName();

  default int getValue(final int index) {
    final int width = getWidth();
    final int height = getHeight();
    final int gridX = index % width;
    final int gridY = height - 1 - (index - gridX) / width;
    return getValue(gridX, gridY);
  }

  int getValue(final int gridX, int gridY);

  int getWidth();

  default void rasterize(final BufferedGeoreferencedImage image) {
    final BufferedImage bufferedImage = image.getBufferedImage();
    final Raster data = bufferedImage.getRaster();
    final DataBuffer dataBuffer = data.getDataBuffer();
    rasterize(dataBuffer);
  }

  default void rasterize(final DataBuffer imageBuffer) {
    final int width = getWidth();
    final int height = getHeight();
    int index = 0;
    for (int gridY = height - 1; gridY >= 0; gridY--) {
      for (int gridX = 0; gridX < width; gridX++) {
        final int value = getValue(gridX, gridY);

        imageBuffer.setElem(index, value);
        index++;
      }
    }
  }

  void setElevationModel(GriddedElevationModel elevationModel);

  void setMaxZ(double maxZ);

  void setMinZ(double minZ);

  void updateValues();
}
