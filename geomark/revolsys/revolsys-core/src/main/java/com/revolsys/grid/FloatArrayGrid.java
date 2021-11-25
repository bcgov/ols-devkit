package com.revolsys.grid;

import java.util.Arrays;
import java.util.function.DoubleConsumer;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

public class FloatArrayGrid extends AbstractGrid {
  public static final float NULL_VALUE = -Float.MAX_VALUE;

  protected final float[] cells;

  public FloatArrayGrid(final double x, final double y, final int gridWidth, final int gridHeight,
    final double gridCellWidth, final double gridCellHeight, final float[] values) {
    this(GeometryFactory.DEFAULT_3D, x, y, gridWidth, gridHeight, gridCellWidth, gridCellHeight,
      values);
  }

  public FloatArrayGrid(final double x, final double y, final int gridWidth, final int gridHeight,
    final double gridCellSize, final float[] values) {
    this(x, y, gridWidth, gridHeight, gridCellSize, gridCellSize, values);
  }

  public FloatArrayGrid(final GeometryFactory geometryFactory, final BoundingBox boundingBox,
    final int gridWidth, final int gridHeight, final double gridCellWidth,
    final double gridCellHeight, final float[] values) {
    super(geometryFactory, boundingBox, gridWidth, gridHeight, gridCellWidth, gridCellHeight);
    this.cells = values;
  }

  public FloatArrayGrid(final GeometryFactory geometryFactory, final BoundingBox boundingBox,
    final int gridWidth, final int gridHeight, final double gridCellSize, final float[] values) {
    this(geometryFactory, boundingBox, gridWidth, gridHeight, gridCellSize, gridCellSize, values);
  }

  public FloatArrayGrid(final GeometryFactory geometryFactory, final double x, final double y,
    final int gridWidth, final int gridHeight, final double gridCellSize) {
    this(geometryFactory, x, y, gridWidth, gridHeight, gridCellSize, gridCellSize);
  }

  public FloatArrayGrid(final GeometryFactory geometryFactory, final double x, final double y,
    final int gridWidth, final int gridHeight, final double gridCellWidth,
    final double gridCellHeight) {
    super(geometryFactory, x, y, gridWidth, gridHeight, gridCellWidth, gridCellHeight);
    final int size = gridWidth * gridHeight;
    final float[] values = new float[size];
    Arrays.fill(values, NULL_VALUE);
    this.cells = values;
  }

  public FloatArrayGrid(final GeometryFactory geometryFactory, final double x, final double y,
    final int gridWidth, final int gridHeight, final double gridCellWidth,
    final double gridCellHeight, final float[] values) {
    super(geometryFactory, x, y, gridWidth, gridHeight, gridCellWidth, gridCellHeight);
    this.cells = values;
  }

  public FloatArrayGrid(final GeometryFactory geometryFactory, final double x, final double y,
    final int gridWidth, final int gridHeight, final double gridCellSize, final float[] values) {
    this(geometryFactory, x, y, gridWidth, gridHeight, gridCellSize, gridCellSize, values);
  }

  @Override
  public void clear() {
    super.clear();
    Arrays.fill(this.cells, NULL_VALUE);
  }

  @Override
  protected void expandRange() {
    float min = Float.MAX_VALUE;
    float max = -Float.MAX_VALUE;
    for (final float value : this.cells) {
      if (Double.isFinite(value)) {
        if (value < min) {
          min = value;
        }
        if (value > max) {
          max = value;
        }
      }
    }
    final double minZ = min;
    final double maxZ = max;
    setValueRange(minZ, maxZ);

  }

  @Override
  public void forEachValueFinite(final DoubleConsumer action) {
    for (final float value : this.cells) {
      if (Double.isFinite(value)) {
        action.accept(value);
      }
    }
  }

  public float[] getCellsFloat() {
    return this.cells;
  }

  @Override
  public double getValueFast(final int gridX, final int gridY) {
    final int index = gridY * this.gridWidth + gridX;
    final float value = this.cells[index];
    return value;
  }

  @Override
  public boolean hasValueFast(final int gridX, final int gridY) {
    final int index = gridY * this.gridWidth + gridX;
    final float value = this.cells[index];
    if (Double.isFinite(value)) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public FloatArrayGrid newGrid(final GeometryFactory geometryFactory, final double x,
    final double y, final int width, final int height, final double cellSize) {
    return new FloatArrayGrid(geometryFactory, x, y, width, height, cellSize);
  }

  public FloatArrayGrid newGrid(final int gridWidth, final int gridHeight,
    final double gridCellWidth, final double gridCellHeight, final float[] newValues) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final BoundingBox boundingBox = getBoundingBox();
    return new FloatArrayGrid(geometryFactory, boundingBox, gridWidth, gridHeight, gridCellWidth,
      gridCellHeight, newValues);
  }

  @Override
  public FloatArrayGrid resample(final int newGridCellSize) {
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

    final float[] oldValues = this.cells;
    final float[] newValues = new float[newGridWidth * newGridHeight];

    int newIndex = 0;
    for (int gridYMin = 0; gridYMin < gridHeight; gridYMin += stepY) {
      final int gridYMax = gridYMin + stepY;
      for (int gridXMin = 0; gridXMin < gridWidth; gridXMin += stepX) {
        final int gridXMax = gridXMin + stepX;
        int count = 0;
        long sum = 0;
        for (int gridY = gridYMin; gridY < gridYMax; gridY++) {
          for (int gridX = gridXMin; gridX < gridXMax; gridX++) {
            final float oldValue = oldValues[gridY * gridWidth + gridX];
            if (Double.isFinite(oldValue)) {
              count++;
              sum += oldValue;
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
  public void setValue(final int gridX, final int gridY, final double value) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
      final int index = gridY * width + gridX;
      final float valueFloat = (float)value;
      this.cells[index] = valueFloat;
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
