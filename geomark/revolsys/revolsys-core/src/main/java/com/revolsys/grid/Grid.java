package com.revolsys.grid;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

import org.jeometry.common.awt.WebColors;
import org.jeometry.common.function.Consumer3Double;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.simplify.DouglasPeuckerSimplifier;
import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.spring.resource.Resource;

/**
 * <p>A grid is a two dimensional array of values. The value could represent and double value and
 * could be an elevation a DEM or shade of gray in an image.</p>
 */
public interface Grid extends ObjectWithProperties, BoundingBoxProxy {

  String GEOMETRY_FACTORY = "geometryFactory";

  int NULL_COLOUR = WebColors.colorToRGB(0, 0, 0, 0);

  static void addRectangle(final GeometryFactory geometryFactory,
    final LinkedList<Polygon> polygons, final double minX, final double minY, final double maxX,
    final double maxY) {
    Polygon notNullRectangle = geometryFactory.newRectangleCorners(minX, minY, maxX, maxY);
    for (final Iterator<Polygon> iterator = polygons.iterator(); iterator.hasNext();) {
      final Polygon polygon = iterator.next();
      if (polygon.intersectsBbox(notNullRectangle.getBoundingBox())) {
        final Geometry union = notNullRectangle.union(polygon);
        if (union instanceof Polygon) {
          notNullRectangle = (Polygon)union;
          iterator.remove();
        }
      }
    }
    polygons.add(notNullRectangle);
  }

  public static double bilinearInterpolation(final double q11, final double q12, final double q21,
    final double q22, final double x1, final double x2, final double y1, final double y2,
    final double x, final double y) {
    final double x2x1 = x2 - x1;
    final double y2y1 = y2 - y1;
    final double x2x = x2 - x;
    final double y2y = y2 - y;
    final double yy1 = y - y1;
    final double xx1 = x - x1;
    return 1.0 / (x2x1 * y2y1)
      * (q11 * x2x * y2y + q21 * xx1 * y2y + q12 * x2x * yy1 + q22 * xx1 * yy1);
  }

  /**
   *
   *
   * @param a
   * @param b
   * @param c
   * @param d
   * @param t The va
   * @return
   */
  public static double cubicInterpolate(final double a, final double b, final double c,
    final double d, final double t) {
    return b + 0.5 * t * (c - a + t * (2 * a - 5 * b + 4 * c - d + t * (3 * (b - c) + d - a)));
  }

  static int getGridCellX(final double minX, final double gridCellSize, final double x) {
    final double deltaX = x - minX;
    final double cellDiv = deltaX / gridCellSize;
    final int gridX = (int)Math.floor(cellDiv);
    return gridX;
  }

  static int getGridCellY(final double minY, final double gridCellSize, final double y) {
    final double deltaY = y - minY;
    final double cellDiv = deltaY / gridCellSize;
    final int gridY = (int)Math.floor(cellDiv);
    return gridY;
  }

  void clear();

  default Grid copyGrid(final BoundingBoxProxy boundingBox) {
    final BoundingBox bbox = convertBoundingBox(boundingBox);
    final double gridCellWidth = getGridCellWidth();
    final double gridCellHeight = getGridCellHeight();
    final double minX = Math.floor(bbox.getMinX() / gridCellWidth) * gridCellWidth;
    final double maxX = Math.floor(bbox.getMaxX() / gridCellWidth) * gridCellWidth;
    final double minY = Math.floor(bbox.getMinY() / gridCellHeight) * gridCellHeight;
    final double maxY = Math.floor(bbox.getMaxY() / gridCellHeight) * gridCellHeight;
    final int width = (int)((maxX - minX) / gridCellWidth);
    final int height = (int)((maxY - minY) / gridCellHeight);
    final GeometryFactory geometryFactory = getGeometryFactory();
    final Grid newGrid = newGrid(geometryFactory, minX, minY, width, height, gridCellWidth,
      gridCellHeight);
    for (int gridY = 0; gridY < height; gridY++) {
      final double y = minY + gridY * gridCellHeight;
      for (int gridX = 0; gridX < height; gridX++) {
        final double x = minX + gridX * gridCellWidth;
        final double elevation = getValue(x, y);
        newGrid.setValue(gridX, gridY, elevation);
      }
    }
    return newGrid;
  }

  default void forEachPoint(final Consumer3Double action) {
    final double gridCellWidth = getGridCellWidth();
    final double gridCellHeight = getGridCellHeight();
    final double minY = getGridMinY();
    final double minX = getGridMinX();
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();
    for (int gridY = 0; gridY < gridHeight; gridY++) {
      final double y = minY + gridY * gridCellHeight;
      for (int gridX = 0; gridX < gridWidth; gridX++) {
        final double x = minX + gridX * gridCellWidth;
        final double value = getValueFast(gridX, gridY);
        action.accept(x, y, value);
      }
    }
  }

  default void forEachPointFinite(final BoundingBox boundingBox, final Consumer<Point> action) {
    final GeometryFactory targetGeometryFactory = boundingBox.getGeometryFactory()
      .convertAxisCount(3);
    final GeometryFactory geometryFactory = getGeometryFactory();

    final CoordinatesOperation projection = geometryFactory
      .getCoordinatesOperation(targetGeometryFactory);

    final BoundingBox convertexBoundingBox = boundingBox.bboxToCs(geometryFactory);
    final double gridCellWidth = getGridCellWidth();
    final double gridCellHeight = getGridCellHeight();
    final double minY = getGridMinY();
    final double minX = getGridMinX();
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();

    int startGridX = (int)Math.floor((convertexBoundingBox.getMinX() - minX) / gridCellWidth);
    if (startGridX < 0) {
      startGridX = 0;
    }
    int endGridX = (int)Math.ceil((convertexBoundingBox.getMaxX() - minX) / gridCellWidth);
    if (endGridX > gridWidth) {
      endGridX = gridWidth;
    }

    int startGridY = (int)Math.floor((convertexBoundingBox.getMinY() - minY) / gridCellHeight);
    if (startGridY < 0) {
      startGridY = 0;
    }
    int endGridY = (int)Math.ceil((convertexBoundingBox.getMaxY() - minY) / gridCellHeight);
    if (endGridY > gridHeight) {
      endGridY = gridHeight;
    }

    if (projection == null) {
      for (int gridY = startGridY; gridY < endGridY; gridY++) {
        final double y = minY + gridY * gridCellHeight;
        for (int gridX = startGridX; gridX < endGridX; gridX++) {
          final double x = minX + gridX * gridCellWidth;
          final double value = getValueFast(gridX, gridY);
          if (Double.isFinite(value)) {
            if (boundingBox.bboxCovers(x, y)) {
              final Point point = targetGeometryFactory.point(x, y, value);
              action.accept(point);
            }
          }
        }
      }
    } else {
      final CoordinatesOperationPoint point = new CoordinatesOperationPoint();
      for (int gridY = startGridY; gridY < endGridY; gridY++) {
        final double y = minY + gridY * gridCellHeight;
        for (int gridX = startGridX; gridX < endGridX; gridX++) {
          final double x = minX + gridX * gridCellWidth;
          final double value = getValueFast(gridX, gridY);
          if (Double.isFinite(value)) {
            point.setPoint(x, y, value);
            projection.perform(point);
            final double targetX = point.x;
            final double targetY = point.y;
            final double targetZ = point.z;
            if (boundingBox.bboxCovers(targetX, targetY)) {
              final Point targetPoint = targetGeometryFactory.point(targetX, targetY, targetZ);
              action.accept(targetPoint);
            }
          }
        }
      }
    }
  }

  default void forEachPointFinite(final Consumer3Double action) {
    final double gridCellWidth = getGridCellWidth();
    final double gridCellHeight = getGridCellHeight();
    final double minY = getGridMinY();
    final double minX = getGridMinX();
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();
    for (int gridY = 0; gridY < gridHeight; gridY++) {
      final double y = minY + gridY * gridCellHeight;
      for (int gridX = 0; gridX < gridWidth; gridX++) {
        final double x = minX + gridX * gridCellWidth;
        final double value = getValueFast(gridX, gridY);
        if (Double.isFinite(value)) {
          action.accept(x, y, value);
        }
      }
    }
  }

  default void forEachValueFinite(final DoubleConsumer action) {
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();
    for (int gridY = 0; gridY < gridHeight; gridY++) {
      for (int gridX = 0; gridX < gridWidth; gridX++) {
        final double value = getValueFast(gridX, gridY);
        if (Double.isFinite(value)) {
          action.accept(value);
        }
      }
    }
  }

  default double getAspectRatio() {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (width > 0 && height > 0) {
      return (double)width / height;
    } else {
      return 0;
    }
  }

  default int getColour(final int gridX, final int gridY) {
    throw new UnsupportedOperationException();
  }

  @Override
  default GeometryFactory getGeometryFactory() {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox.getGeometryFactory();
  }

  double getGridCellHeight();

  double getGridCellWidth();

  default int getGridCellX(final double x) {
    final double minX = getGridMinX();
    final double gridCellWidth = getGridCellWidth();
    final double deltaX = x - minX;
    final double cellDiv = deltaX / gridCellWidth;
    return (int)Math.floor(cellDiv);
  }

  default int getGridCellXRound(final double x) {
    final double minX = getGridMinX();
    final double gridCellWidth = getGridCellWidth();
    final double deltaX = x - minX;
    final double cellDiv = deltaX / gridCellWidth;
    return (int)Math.round(cellDiv);
  }

  default int getGridCellY(final double y) {
    final double minY = getGridMinY();
    final double gridCellHeight = getGridCellHeight();
    final double deltaY = y - minY;
    final double cellDiv = deltaY / gridCellHeight;
    return (int)Math.floor(cellDiv);
  }

  default int getGridCellYRound(final double y) {
    final double minY = getGridMinY();
    final double gridCellHeight = getGridCellHeight();
    final double deltaY = y - minY;
    final double cellDiv = deltaY / gridCellHeight;
    return (int)Math.round(cellDiv);
  }

  int getGridHeight();

  default double getGridMaxX() {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox.getMaxX();
  }

  default double getGridMaxY() {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox.getMaxY();
  }

  default double getGridMinX() {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox.getMinX();
  }

  default double getGridMinY() {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox.getMinY();
  }

  int getGridWidth();

  double getMaxValue();

  double getMinValue();

  default Polygonal getNotNullPolygonal() {
    final GeometryFactory geometryFactory = getGeometryFactory().convertAxisCount(2);
    final BoundingBox boundingBox = getBoundingBox();
    final double minX = boundingBox.getMinX();
    final double minY = boundingBox.getMinY();
    final LinkedList<Polygon> notNullPolygons = new LinkedList<>();
    final double gridCellWidth = getGridCellWidth();
    final double gridCellHeight = getGridCellHeight();
    final int gridHeight = getGridHeight();
    final int gridWidth = getGridWidth();
    double nextY = minY;
    for (int gridY = 0; gridY < gridHeight; gridY++) {
      final double y = nextY;
      nextY = minY + (gridY + 1) * gridCellHeight;
      int firstNotNullGridX = -1;
      boolean lastWasNotNull = false;
      for (int gridX = 0; gridX < gridWidth; gridX++) {
        final double value = getValue(gridX, gridY);
        if (Double.isFinite(value)) {
          if (!lastWasNotNull) {
            firstNotNullGridX = gridX;
          }
          lastWasNotNull = true;
        } else {
          if (lastWasNotNull) {
            final double x1 = minX + firstNotNullGridX * gridCellWidth;
            final double x2 = minX + gridX * gridCellWidth;

            Grid.addRectangle(geometryFactory, notNullPolygons, x1, y, x2, nextY);
            lastWasNotNull = false;
            firstNotNullGridX = -1;
          }
        }
      }
      if (lastWasNotNull) {
        final double x1 = minX + firstNotNullGridX * gridCellWidth;
        final double x2 = minX + gridWidth * gridCellWidth;
        Grid.addRectangle(geometryFactory, notNullPolygons, x1, y, x2, nextY);
      }
    }
    final Polygonal notNullPolygonal = geometryFactory.union(notNullPolygons);
    return (Polygonal)DouglasPeuckerSimplifier.simplify(notNullPolygonal, 0);
  }

  Resource getResource();

  default double getScaleX() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final double scaleX = geometryFactory.getScaleX();
    return scaleX;
  }

  default double getScaleY() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final double scaleY = geometryFactory.getScaleY();
    return scaleY;
  }

  default double getScaleZ() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final double scaleX = geometryFactory.getScaleX();
    return scaleX;
  }

  /**
   * <p>Get the value at the given coordinates by rounding down to the grid cell.</p>
   *
   * <code>
   * gridX = floor(x - minX / gridCellSize);
   * gridY = floor(y - minY / gridCellSize);
   * value = getValue(gridX, gridY)
   * </code>
   *
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @return The value.
   * @see #getValue(int, int)
   */
  default double getValue(final double x, final double y) {
    final int gridX = getGridCellX(x);
    final int gridY = getGridCellY(y);
    return getValue(gridX, gridY);
  }

  default double getValue(int gridX, int gridY) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (gridX < 0 || gridY < 0) {
      return Double.NaN;
    } else {
      if (gridX >= width) {
        if (gridX == width) {
          gridX--;
        } else {
          return Double.NaN;
        }
      }
      if (gridY >= height) {
        if (gridY == height) {
          gridY--;
        } else {
          return Double.NaN;
        }
      }
      return getValueFast(gridX, gridY);
    }
  }

  default double getValue(Point point) {
    point = convertGeometry(point);
    final double x = point.getX();
    final double y = point.getY();
    return getValue(x, y);
  }

  /**
   * Get the elevation using <a href="https://en.wikipedia.org/wiki/Bicubic_interpolation">Bicubic interpolation</a> using the 4x4 grid cells -1, 0, 1, 2.
   *
   * @param x The x-coordinate.
   * @param y The y-coordinate.
   * @return The interpolated elevation (z-coordinate).
   */
  default double getValueBicubic(final double x, final double y) {
    final double gridCellWidth = getGridCellWidth();
    final double gridCellHeight = getGridCellHeight();

    final double xGrid = (x - getGridMinX()) / gridCellWidth;
    final int gridX = (int)Math.floor(xGrid);
    final double xPercent = xGrid - gridX;

    final double yGrid = (y - getGridMinY()) / gridCellHeight;
    final int gridY = (int)Math.floor(yGrid);
    final double yPercent = yGrid - gridY;

    final double z1 = getValueCubic(gridX, gridY - 1, xPercent);
    final double z2 = getValueCubic(gridX, gridY, xPercent);
    final double z3 = getValueCubic(gridX, gridY + 1, xPercent);
    final double z4 = getValueCubic(gridX, gridY + 2, xPercent);

    return cubicInterpolate(z1, z2, z3, z4, yPercent);
  }

  /**
   * Get the elevation of the point location using <a href="https://en.wikipedia.org/wiki/Bilinear_interpolation">Bilinear Interpolation</a> using the 2x2 grid cells 0, 1.
   *
   * @param x The x-coordinate.
   * @param y The y-coordinate.
   * @return The interpolated elevation (z-coordinate).
   */
  default double getValueBilinear(final double x, final double y) {
    final double gridCellWidth = getGridCellWidth();
    final double gridCellHeight = getGridCellHeight();
    final double minX = getGridMinX();
    final double xGrid = (x - minX) / gridCellWidth;
    final int gridX = (int)Math.floor(xGrid);
    final double minY = getGridMinY();
    final double yGrid = (y - minY) / gridCellHeight;
    final int gridY = (int)Math.floor(yGrid);
    final double z11 = getValue(gridX, gridY);
    double z21 = getValue(gridX + 1, gridY);
    if (!Double.isFinite(z21)) {
      z21 = z11;
    }
    final double z12 = getValue(gridX, gridY + 1);
    if (!Double.isFinite(z12)) {
      z21 = z11;
    }
    double z22 = getValue(gridX + 1, gridY + 1);
    if (!Double.isFinite(z22)) {
      z22 = z21;
    }
    // Calculation is simplified as only the percent is required.
    final double xPercent = xGrid - gridX;
    final double yPercent = yGrid - gridY;
    final double x2x = 1 - xPercent;
    final double y2y = 1 - yPercent;

    return z11 * x2x * y2y + z21 * xPercent * y2y + z12 * x2x * yPercent
      + z22 * xPercent * yPercent;
    // MathUtil.bilinearInterpolation(double, double, double, double, double,
    // double, double,
    // double, double, double)
  }

  default double getValueCubic(final int gridX, final int gridY, final double xPercent) {
    final double z1 = getValue(gridX - 1, gridY);
    final double z2 = getValue(gridX, gridY);
    final double z3 = getValue(gridX + 1, gridY);
    final double z4 = getValue(gridX + 2, gridY);
    return cubicInterpolate(z1, z2, z3, z4, xPercent);
  }

  double getValueFast(int gridX, int gridY);

  /**
   * <p>Get the elevation at the given coordinates by rounding to the nearest grid cell.</p>
   *
   * <code>
   * gridX = round(x - minX / gridCellSize);
   * gridY = round(y - minY / gridCellSize);
   * value = getValue(gridX, gridY)
   * </code>
   *
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @return The value.
   * @see #getValue(int, int)
   */
  default double getValueNearestNeighbour(final double x, final double y) {
    final int gridX = getGridCellXRound(x);
    final int gridY = getGridCellYRound(y);
    return getValue(gridX, gridY);
  }

  default double getX(final int i) {
    final double minX = getGridMinX();
    final double gridCellWidth = getGridCellWidth();
    return minX + i * gridCellWidth;
  }

  default double getY(final int i) {
    final double maxY = getGridMinY();
    final double gridCellHeight = getGridCellHeight();
    return maxY + i * gridCellHeight;
  }

  default boolean hasValue(final double x, final double y) {
    final int gridX = getGridCellX(x);
    final int gridY = getGridCellY(y);
    return hasValue(gridX, gridY);
  }

  default boolean hasValue(final int gridX, final int gridY) {
    final double elevation = getValue(gridX, gridY);
    return Double.isFinite(elevation);
  }

  default boolean hasValueFast(final int gridX, final int gridY) {
    final double elevation = getValue(gridX, gridY);
    return Double.isFinite(elevation);
  }

  boolean isEmpty();

  default boolean isNull(final double x, final double y) {
    final int i = getGridCellX(x);
    final int j = getGridCellY(y);
    return isNull(i, j);
  }

  default boolean isNull(final int x, final int y) {
    final double elevation = getValue(x, y);
    return Double.isNaN(elevation);
  }

  default BitGrid newBitGrid() {
    final BoundingBox boundingBox = getBoundingBox();
    final double gridCellWidth = getGridCellWidth();
    final double gridCellHeight = getGridCellHeight();
    final BitGrid bitGrid = BitGrid.newBitGrid(boundingBox, gridCellWidth, gridCellHeight);
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();
    for (int gridY = 0; gridY < gridHeight; gridY++) {
      for (int gridX = 0; gridX < gridWidth; gridX++) {
        if (hasValue(gridX, gridY)) {
          bitGrid.setFlag(gridX, gridY);
        }
      }
    }
    return bitGrid;
  }

  default Grid newGrid(final BoundingBox boundingBox, final double gridCellSize) {
    final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
    final int minX = (int)boundingBox.getMinX();
    final int minY = (int)boundingBox.getMinY();
    final double width = boundingBox.getWidth();
    final double height = boundingBox.getHeight();

    final int modelWidth = (int)Math.ceil(width / gridCellSize);
    final int modelHeight = (int)Math.ceil(height / gridCellSize);
    final Grid grid = newGrid(geometryFactory, minX, minY, modelWidth, modelHeight, gridCellSize);
    final int maxX = (int)(minX + modelWidth * gridCellSize);
    final int maxY = (int)(minY + modelHeight * gridCellSize);
    for (double y = minY; y < maxY; y += gridCellSize) {
      for (double x = minX; x < maxX; x += gridCellSize) {
        setValue(grid, x, y);
      }
    }
    return grid;
  }

  default Grid newGrid(final double x, final double y, final int width, final int height) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final double gridCellWidth = getGridCellWidth();
    final double gridCellHeight = getGridCellHeight();
    return newGrid(geometryFactory, x, y, width, height, gridCellWidth, gridCellHeight);
  }

  default Grid newGrid(final GeometryFactory geometryFactory, final double x, final double y,
    final int width, final int height, final double gridCellSize) {
    return newGrid(geometryFactory, x, y, width, height, gridCellSize, gridCellSize);
  }

  default Grid newGrid(final GeometryFactory geometryFactory, final double x, final double y,
    final int width, final int height, final double gridCellWidth, final double gridCellHeight) {
    return new IntArrayScaleGrid(geometryFactory, x, y, width, height, gridCellWidth,
      gridCellHeight);
  }

  default Grid resample(final int newGridCellSize) {
    return resample(newGridCellSize, newGridCellSize);
  }

  default Grid resample(final int newGridCellWidth, final int newGridCellHeight) {
    final int tileX = (int)getGridMinX();
    final int tileY = (int)getGridMinY();
    final double gridCellWidth = getGridCellWidth();
    final double gridCellHeight = getGridCellHeight();
    final double cellRatioX = gridCellWidth / newGridCellWidth;
    final double cellRatioY = gridCellHeight / newGridCellHeight;
    final int stepX = (int)Math.round(1 / cellRatioX);
    final int stepY = (int)Math.round(1 / cellRatioY);
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();

    final int newGridWidth = (int)Math.round(gridWidth * cellRatioX);
    final int newGridHeight = (int)Math.round(gridHeight * cellRatioY);

    final GeometryFactory geometryFactory = getGeometryFactory();
    final Grid newDem = new IntArrayScaleGrid(geometryFactory, tileX, tileY, newGridWidth,
      newGridHeight, newGridCellWidth, newGridCellHeight);

    int newGridY = 0;
    for (int gridYMin = 0; gridYMin < gridHeight; gridYMin += stepY) {
      final int gridYMax = gridYMin + stepY;
      int newGridX = 0;
      for (int gridXMin = 0; gridXMin < gridWidth; gridXMin += stepX) {
        final int gridXMax = gridXMin + stepX;
        int count = 0;
        double sum = 0;
        for (int gridY = gridYMin; gridY < gridYMax; gridY++) {
          for (int gridX = gridXMin; gridX < gridXMax; gridX++) {
            final double elevation = getValue(gridX, gridY);
            if (Double.isFinite(elevation)) {
              count++;
              sum += elevation;
            }
          }
        }
        if (count > 0) {
          final double elevation = geometryFactory.makeZPrecise(sum / count);
          newDem.setValue(newGridX, newGridY, elevation);
        }
        newGridX++;
      }
      newGridY++;
    }
    return newDem;
  }

  void setBoundingBox(BoundingBox boundingBox);

  default void setValue(final double x, final double y, final double elevation) {
    final int gridX = getGridCellX(x);
    final int gridY = getGridCellY(y);
    setValue(gridX, gridY, elevation);
  }

  default void setValue(final Grid grid, final double x, final double y) {
    final int gridX = getGridCellX(x);
    final int gridY = getGridCellY(y);
    final double elevation = grid.getValue(x, y);
    setValue(gridX, gridY, elevation);
  }

  default void setValue(final int gridX, final int gridY, final double elevation) {
    throw new UnsupportedOperationException("Value model is readonly");
  }

  default void setValue(final int gridX, final int gridY, final Grid grid, final double x,
    final double y) {
    final double elevation = grid.getValue(x, y);
    // if (Double.isFinite(elevation)) {
    setValue(gridX, gridY, elevation);
    // }
  }

  default void setValueNull(final double x, final double y) {
    final int gridX = getGridCellX(x);
    final int gridY = getGridCellY(y);
    setValueNull(gridX, gridY);
  }

  default void setValueNull(final int gridX, final int gridY) {
    setValue(gridX, gridY, Double.NaN);
  }

  default void setValues(final Geometry geometry) {
    if (geometry != null) {
      geometry.forEachVertex(getGeometryFactory(), point -> {
        final double x = point.x;
        final double y = point.y;
        final double value = point.z;
        setValue(x, y, value);
      });
    }
  }

  default void setValues(final Grid grid) {
    final double gridCellWidth = getGridCellWidth();
    final double gridCellHeight = getGridCellHeight();
    if (grid.getGridCellWidth() == gridCellWidth && grid.getGridCellHeight() == gridCellHeight) {
      final int gridWidth = getGridWidth();
      final int gridHeight = getGridHeight();

      final double minX1 = grid.getGridMinX();
      final double minY1 = grid.getGridMinY();

      int startX = getGridCellX(minX1);
      int endX = startX + grid.getGridWidth();
      if (startX < 0) {
        startX = 0;
      }
      if (endX > gridWidth) {
        endX = gridWidth;
      }
      int startY = getGridCellY(minY1);
      int endY = startY + grid.getGridHeight();
      if (startY < 0) {
        startY = 0;
      }
      if (endY > gridHeight) {
        endY = gridHeight;
      }
      final int minX = (int)(getGridMinX() + startX * gridCellWidth);
      final int minY = (int)(getGridMinY() + startY * gridCellHeight);

      double y = minY;
      for (int gridY = startY; gridY < endY; gridY++) {
        double x = minX;
        for (int gridX = startX; gridX < endX; gridX++) {
          setValue(gridX, gridY, grid, x, y);
          x += gridCellWidth;
        }
        y += gridCellHeight;
      }
    } else {
      throw new IllegalArgumentException(
        "gridCellWidth " + grid.getGridCellWidth() + " != " + gridCellWidth + " or "
          + "gridCellHeight " + grid.getGridCellHeight() + " != " + gridCellHeight);
    }
  }

  void setValuesForTriangle(final double x1, final double y1, final double z1, final double x2,
    final double y2, final double z2, final double x3, final double y3, final double z3);

  default void setValuesNull(final Grid grid) {
    final double minX1 = grid.getGridMinX();
    final double minY1 = grid.getGridMinY();

    int startX = getGridCellX(minX1);
    if (startX < 0) {
      startX = 0;
    }
    int startY = getGridCellY(minY1);
    if (startY < 0) {
      startY = 0;
    }

    final double gridCellWidth = getGridCellWidth();
    final double gridCellHeight = getGridCellHeight();
    final int minX = (int)(getGridMinX() + startX * gridCellWidth);
    final int minY = (int)(getGridMinY() + startY * gridCellHeight);

    double y = minY;
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();
    for (int gridY = startY; gridY < gridHeight; gridY++) {
      double x = minX;
      for (int gridX = startX; gridX < gridWidth; gridX++) {
        final Grid grid1 = grid;
        final double elevation = grid1.getValue(x, y);
        if (Double.isFinite(elevation)) {
          setValueNull(gridX, gridY);
        }
        x += gridCellWidth;
      }
      y += gridCellHeight;
    }
  }

  default void setValuesNullFast(final Iterable<? extends Point> points) {
    for (final Point point : points) {
      final double x = point.getX();
      final double y = point.getY();
      final int gridX = getGridCellX(x);
      final int gridY = getGridCellY(y);
      setValueNull(gridX, gridY);
    }
  }

  void updateValues();
}
