package com.revolsys.elevation.gridded;

import java.io.IOException;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.editor.LineStringEditor;
import com.revolsys.grid.IntArrayScaleGrid;
import com.revolsys.io.channels.ChannelWriter;

public class IntArrayScaleGriddedElevationModel extends IntArrayScaleGrid
  implements GriddedElevationModel {

  public IntArrayScaleGriddedElevationModel(final GeometryFactory geometryFactory,
    final BoundingBox boundingBox, final int gridWidth, final int gridHeight,
    final double gridCellWidth, final double gridCellHeight, final int[] cells) {
    super(geometryFactory, boundingBox, gridWidth, gridHeight, gridCellWidth, gridCellHeight,
      cells);
  }

  public IntArrayScaleGriddedElevationModel(final GeometryFactory geometryFactory,
    final BoundingBox boundingBox, final int gridWidth, final int gridHeight,
    final double gridCellSize, final int[] cells) {
    super(geometryFactory, boundingBox, gridWidth, gridHeight, gridCellSize, cells);
  }

  public IntArrayScaleGriddedElevationModel(final GeometryFactory geometryFactory, final double x,
    final double y, final int gridWidth, final int gridHeight, final double gridCellSize) {
    super(geometryFactory, x, y, gridWidth, gridHeight, gridCellSize);
  }

  public IntArrayScaleGriddedElevationModel(final GeometryFactory geometryFactory, final double x,
    final double y, final int gridWidth, final int gridHeight, final double gridCellWidth,
    final double gridCellHeight) {
    super(geometryFactory, x, y, gridWidth, gridHeight, gridCellWidth, gridCellHeight);
  }

  @Override
  public LineStringEditor getNullBoundaryPoints() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final LineStringEditor points = new LineStringEditor(geometryFactory);
    final double minX = getGridMinX();
    final double minY = getGridMinY();

    final double gridCellWidth = getGridCellWidth();
    final double gridCellheight = getGridCellHeight();
    final int gridHeight = getGridHeight();
    final int gridWidth = getGridWidth();
    final int[] cells = this.cells;
    int index = 0;
    final int[] offsets = {
      -1, 0, 1
    };
    for (int gridY = 0; gridY < gridHeight; gridY++) {
      for (int gridX = 0; gridX < gridWidth; gridX++) {
        final int valueInt = cells[index];
        if (valueInt == NULL_VALUE) {
          int countZ = 0;
          long sumZ = 0;
          for (final int offsetY : offsets) {
            if (!(gridY == 0 && offsetY == -1 || gridY == gridHeight - 1 && offsetY == 1)) {
              final int offsetIndex = index + offsetY * gridWidth;
              for (final int offsetX : offsets) {
                if (!(gridX == 0 && offsetX == -1 || gridX == gridWidth - 1 && offsetX == 1)) {
                  final int valueNeighbour = cells[offsetIndex + offsetX];
                  if (valueNeighbour != NULL_VALUE) {
                    sumZ += valueNeighbour;
                    countZ++;
                  }
                }
              }
            }
          }

          if (countZ > 0) {
            final double x = minX + gridCellWidth * gridX;
            final double y = minY + gridCellheight * gridY;
            final double value = toDoubleZ((int)(sumZ / countZ));
            points.appendVertex(x, y, value);
          }
        }
        index++;
      }
    }
    return points;
  }

  @Override
  public IntArrayScaleGriddedElevationModel newGrid(final GeometryFactory geometryFactory,
    final double x, final double y, final int width, final int height, final double cellSize) {
    return new IntArrayScaleGriddedElevationModel(geometryFactory, x, y, width, height, cellSize);
  }

  @Override
  public IntArrayScaleGriddedElevationModel newGrid(final int gridWidth, final int gridHeight,
    final double gridCellWidth, final double gridCellHeight, final int[] newValues) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final BoundingBox boundingBox = getBoundingBox();
    return new IntArrayScaleGriddedElevationModel(geometryFactory, boundingBox, gridWidth,
      gridHeight, gridCellWidth, gridCellHeight, newValues);
  }

  @Override
  public IntArrayScaleGriddedElevationModel resample(final int newGridCellSize) {
    return (IntArrayScaleGriddedElevationModel)super.resample(newGridCellSize);
  }

  public void writeIntArray(final ChannelWriter out) throws IOException {
    final int[] values = this.cells;
    for (final int valueInt : values) {
      out.putInt(valueInt);
    }
  }

}
