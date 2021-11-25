package com.revolsys.elevation.gridded.scaledint;

import java.nio.file.Path;

import com.revolsys.elevation.gridded.AbstractTiledGriddedDigitalElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.spring.resource.Resource;

public class TiledMemoryScaledIntegerGriddedDigitalElevationModel
  extends AbstractTiledGriddedDigitalElevationModel {

  private final int coordinateSystemId;

  private final Path baseDirectory;

  private final String filePrefix;

  private final String tileWidthString;

  public TiledMemoryScaledIntegerGriddedDigitalElevationModel(final Path baseDirectory,
    final String filePrefix, final GeometryFactory geometryFactory, final double minX,
    final double minY, final int gridTileSize, final int gridCellSize) {
    super(geometryFactory, minX, minY, gridTileSize, gridCellSize);
    this.tileWidthString = Integer.toString(gridCellSize * gridTileSize);
    this.filePrefix = filePrefix + "_" + getHorizontalCoordinateSystemId() + "_"
      + this.tileWidthString;
    this.coordinateSystemId = geometryFactory.getHorizontalCoordinateSystemId();
    this.baseDirectory = baseDirectory//
      .resolve(ScaledIntegerGriddedDigitalElevation.FILE_EXTENSION)//
      .resolve(Integer.toString(this.coordinateSystemId)) //
      .resolve(this.tileWidthString)//
    ;
  }

  public TiledMemoryScaledIntegerGriddedDigitalElevationModel(final Resource baseResource,
    final String filePrefix, final GeometryFactory geometryFactory, final double minX,
    final double minY, final int gridTileSize, final int gridCellSize) {
    this(baseResource.toPath(), filePrefix, geometryFactory, minX, minY, gridTileSize,
      gridCellSize);
  }

  @Override
  protected GriddedElevationModel newModel(final double tileX, final double tileY) {
    final int tileXInt = (int)tileX;
    final int tileYInt = (int)tileY;

    final String fileName = this.filePrefix + "_" + tileXInt + "_" + tileYInt + "."
      + ScaledIntegerGriddedDigitalElevation.FILE_EXTENSION;
    final Path path = this.baseDirectory //
      .resolve(Integer.toString(tileXInt)) //
      .resolve(fileName);

    final GeometryFactory geometryFactory = getGeometryFactory();
    return GriddedElevationModel.newGriddedElevationModel(path, geometryFactory);
  }

}
