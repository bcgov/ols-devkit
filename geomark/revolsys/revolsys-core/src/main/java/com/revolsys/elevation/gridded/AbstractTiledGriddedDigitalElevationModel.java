package com.revolsys.elevation.gridded;

import java.util.Map;

import org.jeometry.common.collection.map.LruMap;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.grid.AbstractGrid;
import com.revolsys.util.IntPair;

public abstract class AbstractTiledGriddedDigitalElevationModel extends AbstractGrid
  implements GriddedElevationModel {

  protected int gridTileSize;

  private final LruMap<IntPair, GriddedElevationModel> models = new LruMap<>(5000);

  private final IntPair getKey = new IntPair();

  private final double tileWidth;

  private final double tileHeight;

  public AbstractTiledGriddedDigitalElevationModel(final GeometryFactory geometryFactory,
    final double minX, final double minY, final int gridTileSize, final int gridCellSize) {
    super(geometryFactory, minX, minY, Integer.MAX_VALUE, Integer.MAX_VALUE, gridCellSize);
    this.gridTileSize = gridTileSize;
    this.tileWidth = gridTileSize * gridCellSize;
    this.tileHeight = gridTileSize * gridCellSize;
  }

  @Override
  public void clear() {
    synchronized (this.models) {
      this.models.clear();
    }
  }

  @Override
  public final void close() {
    try {
      closeDo();
    } finally {
      synchronized (this.models) {
        this.models.clear();
      }
    }
  }

  protected void closeDo() {

  }

  public int getGridTileSize() {
    return this.gridTileSize;
  }

  protected GriddedElevationModel getModel(final int gridX, final int gridY) {
    GriddedElevationModel model;
    final Map<IntPair, GriddedElevationModel> models = this.models;
    final int tileIndexX = (int)Math.floor(gridX / this.tileWidth);
    final int tileIndexY = (int)Math.floor(gridY / this.tileHeight);
    synchronized (models) {
      final IntPair getKey = this.getKey;
      getKey.setValues(tileIndexX, tileIndexY);

      model = models.get(getKey);
      if (model == null) {
        final double tileX = tileIndexX * this.tileWidth;
        final double tileY = tileIndexY * this.tileHeight;
        model = newModel(tileX, tileY);
        models.put(getKey.clone(), model);
      }
    }
    return model;
  }

  @Override
  public double getValueFast(final int gridX, final int gridY) {
    final GriddedElevationModel model = getModel(gridX, gridY);
    if (model == null) {
      return Double.NaN;
    } else {
      final int tileSize = this.gridTileSize;
      final int gridCellX = gridX % tileSize;
      final int gridCellY = gridY % tileSize;
      return model.getValue(gridCellX, gridCellY);
    }
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean isNull(final int x, final int y) {
    return false;
  }

  @Override
  public GriddedElevationModel newGrid(final GeometryFactory geometryFactory, final double x,
    final double y, final int width, final int height, final double gridCellSize) {
    throw new UnsupportedOperationException();
  }

  protected abstract GriddedElevationModel newModel(double tileX, double tileY);

  public AbstractTiledGriddedDigitalElevationModel setCacheSize(final int cacheSize) {
    this.models.setMaxSize(cacheSize);
    return this;
  }

  public void setGridTileSize(final int gridTileSize) {
    this.gridTileSize = gridTileSize;
  }

  @Override
  public void setValue(final int x, final int y, final double elevation) {
  }

}
