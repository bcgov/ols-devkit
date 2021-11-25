package com.revolsys.grid;

import java.util.Arrays;
import java.util.LinkedList;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.simplify.DouglasPeuckerSimplifier;

public class BitGrid extends AbstractGrid {

  private static final int BIT_INDEX_MASK = 63;

  public static final BitGrid newBitGrid(final BoundingBox boundingBox, final double gridCellWidth,
    final double gridCellHeight) {
    final double minX = boundingBox.getMinX();
    final double minY = boundingBox.getMinY();
    final double maxX = boundingBox.getMaxX();
    final double maxY = boundingBox.getMaxY();

    final double x = Math.floor(minX / gridCellWidth) * gridCellWidth;
    final double y = Math.floor(minY / gridCellHeight) * gridCellHeight;
    final double x2 = Math.ceil(maxX / gridCellWidth) * gridCellWidth;
    final double y2 = Math.ceil(maxY / gridCellHeight) * gridCellHeight;
    final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
    final int width = (int)((x2 - x) / gridCellWidth);
    final int height = (int)((y2 - y) / gridCellHeight);
    return new BitGrid(geometryFactory, x, y, width, height, gridCellWidth, gridCellHeight);
  }

  private final long[] words;

  public BitGrid(final GeometryFactory geometryFactory, final BoundingBox boundingBox,
    final int gridWidth, final int gridHeight, final double gridCellWidth,
    final double gridCellHeight, final long[] flags) {
    super(geometryFactory, boundingBox, gridWidth, gridHeight, gridCellWidth, gridCellHeight);
    this.words = flags;
  }

  public BitGrid(final GeometryFactory geometryFactory, final BoundingBox boundingBox,
    final int gridWidth, final int gridHeight, final double gridCellSize, final long[] cells) {
    this(geometryFactory, boundingBox, gridWidth, gridHeight, gridCellSize, gridCellSize, cells);
  }

  public BitGrid(final GeometryFactory geometryFactory, final double x, final double y,
    final int gridWidth, final int gridHeight, final double gridCellSize) {
    this(geometryFactory, x, y, gridWidth, gridHeight, gridCellSize, gridCellSize);
  }

  public BitGrid(final GeometryFactory geometryFactory, final double x, final double y,
    final int gridWidth, final int gridHeight, final double gridCellWidth,
    final double gridCellHeight) {
    super(geometryFactory, x, y, gridWidth, gridHeight, gridCellWidth, gridCellHeight);
    final int flagCount = (int)Math.ceil(gridWidth * gridHeight / 64.0);
    this.words = new long[flagCount];
  }

  @Override
  public void clear() {
    super.clear();
    Arrays.fill(this.words, 0);
  }

  public void clearFlag(final double x, final double y) {
    final int gridX = getGridCellX(x);
    final int gridY = getGridCellY(y);
    clearFlag(gridX, gridY);
  }

  public void clearFlag(final int gridX, final int gridY) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
      final int index = gridY * width + gridX;
      final int wordIndex = index >> 6;
      final int bitIndex = index & BIT_INDEX_MASK;
      this.words[wordIndex] &= ~(1L << bitIndex);
    }
  }

  public void clearFlagFast(final int gridX, final int gridY) {
    final int width = getGridWidth();
    final int index = gridY * width + gridX;
    final int wordIndex = index >> 6;
    final int bitIndex = index & BIT_INDEX_MASK;
    this.words[wordIndex] &= ~(1L << bitIndex);
  }

  public boolean getFlagFast(final int gridX, final int gridY) {
    final int index = gridY * this.gridWidth + gridX;
    final int wordIndex = index >> 6;
    final int bitIndex = index & BIT_INDEX_MASK;
    final long word = this.words[wordIndex];
    final long flag = word & 1L << bitIndex;
    return flag != 0;
  }

  @Override
  public Polygonal getNotNullPolygonal() {
    final GeometryFactory geometryFactory = getGeometryFactory().convertAxisCount(2);
    if (this.words.length > 0) {
      final BoundingBox boundingBox = getBoundingBox();
      final double minX = boundingBox.getMinX();
      final double minY = boundingBox.getMinY();
      final LinkedList<Polygon> notNullPolygons = new LinkedList<>();
      final double gridCellWidth = getGridCellWidth();
      final double gridCellHeight = getGridCellHeight();
      final int gridHeight = getGridHeight();
      final int gridWidth = getGridWidth();
      double nextY = minY;
      int wordIndex = 0;
      long word = this.words[wordIndex];
      int bitIndex = 0;
      for (int gridY = 0; gridY < gridHeight; gridY++) {
        final double y = nextY;
        nextY = minY + (gridY + 1) * gridCellHeight;
        int firstNotNullGridX = -1;
        boolean lastWasNotNull = false;
        for (int gridX = 0; gridX < gridWidth; gridX++) {
          if (bitIndex == 64) {
            wordIndex++;
            word = this.words[wordIndex];
            bitIndex = 0;
          }
          final long flagValue = word & 1L << bitIndex;

          final boolean flag = flagValue != 0;
          if (flag) {
            if (!lastWasNotNull) {
              firstNotNullGridX = gridX;
            }
            lastWasNotNull = true;
          } else {
            if (lastWasNotNull) {
              final double x1 = minX + firstNotNullGridX * gridCellWidth;
              final double x2 = minX + gridX * gridCellWidth;

              Grid.addRectangle(geometryFactory, notNullPolygons, nextY, y, x1, x2);
              lastWasNotNull = false;
              firstNotNullGridX = -1;
            }
          }
          bitIndex++;
        }
        if (lastWasNotNull) {
          final double x1 = minX + firstNotNullGridX * gridCellWidth;
          final double x2 = minX + gridWidth * gridCellWidth;
          Grid.addRectangle(geometryFactory, notNullPolygons, x1, y, x2, nextY);
        }
      }
      final Polygonal notNullPolygonal = geometryFactory.union(notNullPolygons);
      return (Polygonal)DouglasPeuckerSimplifier.simplify(notNullPolygonal, 0);
    } else {
      return geometryFactory.polygon();
    }
  }

  @Override
  public double getValueFast(final int gridX, final int gridY) {
    if (getFlagFast(gridX, gridY)) {
      return 1;
    } else {
      return 0;
    }
  }

  @Override
  public boolean hasValue(final int gridX, final int gridY) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
      final int index = gridY * width + gridX;
      final int wordIndex = index >> 6;
      final int bitIndex = index & BIT_INDEX_MASK;
      final long word = this.words[wordIndex];
      final long flag = word & 1L << bitIndex;
      return flag != 0;
    } else {
      return false;
    }
  }

  public void setFlag(final double x, final double y) {
    final int gridX = getGridCellX(x);
    final int gridY = getGridCellY(y);
    setFlag(gridX, gridY);
  }

  public void setFlag(final double x, final double y, final boolean flag) {
    final int gridX = getGridCellX(x);
    final int gridY = getGridCellY(y);
    setFlag(gridX, gridY, flag);
  }

  public void setFlag(final int gridX, final int gridY) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
      final int index = gridY * width + gridX;
      final int wordIndex = index >> 6;
      final int bitIndex = index & BIT_INDEX_MASK;
      this.words[wordIndex] |= 1L << bitIndex;

    }
  }

  public void setFlag(final int gridX, final int gridY, final boolean flag) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
      final int index = gridY * width + gridX;
      final int wordIndex = index >> 6;
      final int bitIndex = index & BIT_INDEX_MASK;
      if (flag) {
        this.words[wordIndex] |= 1L << bitIndex;
      } else {
        this.words[wordIndex] &= ~(1L << bitIndex);
      }
    }
  }

  public void setFlagFast(final int gridX, final int gridY) {
    final int width = getGridWidth();
    final int index = gridY * width + gridX;
    final int wordIndex = index >> 6;
    final int bitIndex = index & BIT_INDEX_MASK;
    this.words[wordIndex] |= 1L << bitIndex;
  }

  public void setFlagFast(final int gridX, final int gridY, final boolean flag) {
    final int width = getGridWidth();
    final int index = gridY * width + gridX;
    final int wordIndex = index >> 6;
    final int bitIndex = index & BIT_INDEX_MASK;
    if (flag) {
      this.words[wordIndex] |= 1L << bitIndex;
    } else {
      this.words[wordIndex] &= ~(1L << bitIndex);
    }
  }

  public void setXFlags(final int minGridX, final int maxGridX, final int gridY) {
    for (int gridX = minGridX; gridX < maxGridX; gridX++) {
      setFlag(gridX, gridY);
    }
  }

  public void setYFlags(final int gridX, final int minGridY, final int maxGridY) {
    for (int gridY = minGridY; gridY < maxGridY; gridY++) {
      setFlag(gridX, gridY);
    }
  }
}
