package com.revolsys.grid;

import java.util.Arrays;
import java.util.function.DoubleConsumer;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

public class IntArrayScaleGrid extends AbstractGrid {
  public static final int NULL_VALUE = Integer.MIN_VALUE;

  protected final int[] cells;

  public IntArrayScaleGrid(final GeometryFactory geometryFactory, final BoundingBox boundingBox,
    final int gridWidth, final int gridHeight, final double gridCellWidth,
    final double gridCellHeight, final int[] cells) {
    super(geometryFactory, boundingBox, gridWidth, gridHeight, gridCellWidth, gridCellHeight);
    this.cells = cells;
    expandRange();
  }

  public IntArrayScaleGrid(final GeometryFactory geometryFactory, final BoundingBox boundingBox,
    final int gridWidth, final int gridHeight, final double gridCellSize, final int[] cells) {
    this(geometryFactory, boundingBox, gridWidth, gridHeight, gridCellSize, gridCellSize, cells);
  }

  public IntArrayScaleGrid(final GeometryFactory geometryFactory, final double x, final double y,
    final int gridWidth, final int gridHeight, final double gridCellSize) {
    this(geometryFactory, x, y, gridWidth, gridHeight, gridCellSize, gridCellSize);
  }

  public IntArrayScaleGrid(final GeometryFactory geometryFactory, final double x, final double y,
    final int gridWidth, final int gridHeight, final double gridCellWidth,
    final double gridCellHeight) {
    super(geometryFactory, x, y, gridWidth, gridHeight, gridCellWidth, gridCellHeight);
    final int size = gridWidth * gridHeight;
    final int[] cells = new int[size];
    Arrays.fill(cells, NULL_VALUE);
    this.cells = cells;
  }

  @Override
  public void clear() {
    super.clear();
    Arrays.fill(this.cells, NULL_VALUE);
  }

  @Override
  protected void expandRange() {
    int min = Integer.MAX_VALUE;
    int max = Integer.MIN_VALUE;
    for (final int valueInt : this.cells) {
      if (valueInt != NULL_VALUE) {
        if (valueInt < min) {
          min = valueInt;
        }
        if (valueInt > max) {
          max = valueInt;
        }
      }
    }
    final double minZ = toDoubleZ(min);
    final double maxZ = toDoubleZ(max);
    setValueRange(minZ, maxZ);

  }

  @Override
  public void forEachValueFinite(final DoubleConsumer action) {
    for (final int elevation : this.cells) {
      if (elevation != NULL_VALUE) {
        final double value = toDoubleZ(elevation);
        action.accept(value);
      }
    }
  }

  public int[] getCellsInt() {
    return this.cells;
  }

  @Override
  public double getValueFast(final int gridX, final int gridY) {
    final int index = gridY * this.gridWidth + gridX;
    final int elevationInt = this.cells[index];
    if (elevationInt == NULL_VALUE) {
      return Double.NaN;
    } else {
      return toDoubleZ(elevationInt);
    }
  }

  public int getValueInt(final int gridX, final int gridY) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
      final int index = gridY * width + gridX;
      return this.cells[index];
    } else {
      return NULL_VALUE;
    }
  }

  @Override
  public boolean hasValueFast(final int gridX, final int gridY) {
    final int gridWidth1 = this.gridWidth;
    final int index = gridY * gridWidth1 + gridX;
    final int elevationInt = this.cells[index];
    if (elevationInt == NULL_VALUE) {
      return false;
    } else {
      return true;
    }
  }

  @Override
  public IntArrayScaleGrid newGrid(final GeometryFactory geometryFactory, final double x,
    final double y, final int width, final int height, final double cellSize) {
    return new IntArrayScaleGrid(geometryFactory, x, y, width, height, cellSize);
  }

  public IntArrayScaleGrid newGrid(final int gridWidth, final int gridHeight,
    final double gridCellWidth, final double gridCellHeight, final int[] newValues) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final BoundingBox boundingBox = getBoundingBox();
    return new IntArrayScaleGrid(geometryFactory, boundingBox, gridWidth, gridHeight, gridCellWidth,
      gridCellHeight, newValues);
  }

  @Override
  public Grid resample(final int newGridCellSize) {
    final double gridCellWidth = getGridCellWidth();
    final double gridCellHeight = getGridCellHeight();
    final double cellRatioX = gridCellWidth / newGridCellSize;
    final double cellRatioY = gridCellHeight / newGridCellSize;
    final int stepX = (int)Math.round(1 / cellRatioX);
    final int stepY = (int)Math.round(1 / cellRatioY);
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();

    final int newGridWidth = (int)Math.round(gridWidth * cellRatioX);
    final int newGridHeight = (int)Math.round(gridHeight * cellRatioY);

    final int[] oldValues = this.cells;
    final int[] newValues = new int[newGridWidth * newGridHeight];

    int newIndex = 0;
    for (int gridYMin = 0; gridYMin < gridHeight; gridYMin += stepY) {
      final int gridYMax = gridYMin + stepY;
      for (int gridXMin = 0; gridXMin < gridWidth; gridXMin += stepX) {
        final int gridXMax = gridXMin + stepX;
        int count = 0;
        long sum = 0;
        for (int gridY = gridYMin; gridY < gridYMax; gridY++) {
          for (int gridX = gridXMin; gridX < gridXMax; gridX++) {
            final int elevation = oldValues[gridY * gridWidth + gridX];
            if (elevation != NULL_VALUE) {
              count++;
              sum += elevation;
            }
          }
        }
        if (count > 0) {
          newValues[newIndex] = (int)(sum / count);
        } else {
          newValues[newIndex] = NULL_VALUE;
        }
        newIndex++;
      }
    }
    return newGrid(newGridWidth, newGridHeight, newGridCellSize, newGridCellSize, newValues);
  }

  @Override
  protected void setGeometryFactory(GeometryFactory geometryFactory) {
    if (geometryFactory.getScaleZ() <= 0) {
      if (geometryFactory.getAxisCount() < 3) {
        geometryFactory = geometryFactory.convertAxisCount(3);
      }
      final double[] scales = geometryFactory.getScales();
      scales[2] = 1000;
      geometryFactory = geometryFactory.convertScales(scales);
    }
    super.setGeometryFactory(geometryFactory);
  }

  @Override
  public void setValue(final int gridX, final int gridY, final double elevation) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
      final int index = gridY * width + gridX;
      final int elevationInt = geometryFactory.toIntZ(elevation);
      this.cells[index] = elevationInt;
      clearCachedObjects();
    }
  }

  @Override
  public void setValueNull(final int gridX, final int gridY) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
      final int index = gridY * width + gridX;
      this.cells[index] = NULL_VALUE;
      clearCachedObjects();
    }
  }

}
