package com.revolsys.elevation.gridded;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.grid.AbstractGrid;
import com.revolsys.io.BaseCloseable;

public abstract class DirectFileElevationModel extends AbstractGrid
  implements GriddedElevationModel, BaseCloseable {

  protected final int elevationByteCount;

  protected final int headerSize;

  private boolean open = true;

  public DirectFileElevationModel(final GeometryFactory geometryFactory, final double minX,
    final double minY, final int gridWidth, final int gridHeight, final double gridCellSize,
    final int headerSize, final int elevationByteCount) {
    super(geometryFactory, minX, minY, gridWidth, gridHeight, gridCellSize);
    this.headerSize = headerSize;
    this.elevationByteCount = elevationByteCount;
  }

  public DirectFileElevationModel(final int headerSize, final int elevationByteCount) {
    this.headerSize = headerSize;
    this.elevationByteCount = elevationByteCount;
  }

  @Override
  public void clear() {
  }

  @Override
  public void close() {
    this.open = false;
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    close();
  }

  @Override
  public double getValueFast(final int gridX, final int gridY) {
    final int offset = this.headerSize + (gridY * this.gridWidth + gridX) * this.elevationByteCount;
    return readElevation(offset);
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean isNull(final int x, final int y) {
    return false;
  }

  public boolean isOpen() {
    return this.open;
  }

  @Override
  public GriddedElevationModel newGrid(final GeometryFactory geometryFactory, final double x,
    final double y, final int width, final int height, final double gridCellSize) {
    // TODO Auto-generated method stub
    return null;
  }

  protected abstract double readElevation(final int offset);

  @Override
  public void setValue(final int gridX, final int gridY, final double elevation) {
    final int gridWidth = getGridWidth();
    final int offset = this.headerSize + (gridY * gridWidth + gridX) * this.elevationByteCount;
    writeElevation(offset, elevation);
  }

  protected abstract void writeElevation(int offset, double elevation);
}
