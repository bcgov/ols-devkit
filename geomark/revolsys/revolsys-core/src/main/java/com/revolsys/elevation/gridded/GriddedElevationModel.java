package com.revolsys.elevation.gridded;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import org.jeometry.common.function.BiFunctionDoubleDouble;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.esriascii.EsriAsciiGriddedElevation;
import com.revolsys.elevation.gridded.esrifloatgrid.EsriFloatGridGriddedElevation;
import com.revolsys.elevation.gridded.img.ImgGriddedElevation;
import com.revolsys.elevation.gridded.rasterizer.ColorGradientGriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.ColorGriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.HillShadeGriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.SlopeColorGradientGriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.gradient.GradientStop;
import com.revolsys.elevation.gridded.rasterizer.gradient.MultiStopLinearGradient;
import com.revolsys.elevation.gridded.scaledint.ScaledIntegerGriddedDigitalElevation;
import com.revolsys.elevation.gridded.scaledint.compressed.CompressedScaledIntegerGriddedDigitalElevation;
import com.revolsys.elevation.gridded.usgsdem.UsgsGriddedElevation;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.editor.GeometryEditor;
import com.revolsys.geometry.model.editor.LineStringEditor;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.grid.Grid;
import com.revolsys.grid.IntArrayScaleGrid;
import com.revolsys.io.IoFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.spring.resource.Resource;

public interface GriddedElevationModel extends Grid {
  String GEOMETRY_FACTORY = "geometryFactory";

  static boolean copyGriddedElevationModel(final Object source, final Path target) {
    final GriddedElevationModel griddedElevationModel = GriddedElevationModel
      .newGriddedElevationModel(source);
    if (griddedElevationModel == null) {
      return false;
    } else {
      griddedElevationModel.writeGriddedElevationModel(target);
      return true;
    }
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

  static GriddedElevationModel newGriddedElevationModel(final Object source) {
    final MapEx properties = MapEx.EMPTY;
    return newGriddedElevationModel(source, properties);
  }

  static GriddedElevationModel newGriddedElevationModel(final Object source,
    final GeometryFactory defaultGeometryFactory) {
    final MapEx properties = new LinkedHashMapEx("geometryFactory", defaultGeometryFactory);
    return newGriddedElevationModel(source, properties);
  }

  static GriddedElevationModel newGriddedElevationModel(final Object source,
    final MapEx properties) {
    final GriddedElevationModelReaderFactory factory = IoFactory
      .factory(GriddedElevationModelReaderFactory.class, source);
    if (factory == null) {
      return null;
    } else {
      final Resource resource = Resource.getResource(source);
      final GriddedElevationModel dem = factory.newGriddedElevationModel(resource, properties);
      return dem;
    }
  }

  public static void serviceInit() {
    IoFactoryRegistry.addFactory(new ScaledIntegerGriddedDigitalElevation());
    IoFactoryRegistry.addFactory(new CompressedScaledIntegerGriddedDigitalElevation());
    IoFactoryRegistry.addFactory(new EsriAsciiGriddedElevation());
    IoFactoryRegistry.addFactory(new EsriFloatGridGriddedElevation());
    IoFactoryRegistry.addFactory(new UsgsGriddedElevation());
    IoFactoryRegistry.addFactory(new ImgGriddedElevation());

    MapObjectFactoryRegistry.newFactory("gradientStop", GradientStop::new);
    MapObjectFactoryRegistry.newFactory("multiStopLinearGradient", MultiStopLinearGradient::new);

    // Rasterizers
    MapObjectFactoryRegistry.newFactory("colorGriddedElevationModelRasterizer",
      ColorGriddedElevationModelRasterizer::new);
    MapObjectFactoryRegistry.newFactory("colorGradientGriddedElevationModelRasterizer",
      ColorGradientGriddedElevationModelRasterizer::new);
    MapObjectFactoryRegistry.newFactory("hillShadeGriddedElevationModelRasterizer",
      HillShadeGriddedElevationModelRasterizer::new);
    MapObjectFactoryRegistry.newFactory("slopeColorGradientGriddedElevationModelRasterizer",
      SlopeColorGradientGriddedElevationModelRasterizer::new);
  }

  @SuppressWarnings("unchecked")
  static <G extends Geometry> G setGeometryElevations(final G geometry,
    final BiFunctionDoubleDouble getElevation) {
    if (geometry == null) {
      return null;
    } else {
      final GeometryEditor<?> editor = geometry.newGeometryEditor();
      editor.setAxisCount(3);
      for (final Vertex vertex : geometry.vertices()) {
        final double x = vertex.getX();
        final double y = vertex.getY();
        final double elevation = getElevation.apply(x, y);
        if (Double.isFinite(elevation)) {
          final int[] vertexId = vertex.getVertexId();
          editor.setZ(vertexId, elevation);
        }
      }
      return (G)editor.newGeometry();
    }
  }

  default void cancelChanges() {
  }

  @Override
  default GriddedElevationModel copyGrid(final BoundingBoxProxy boundingBox) {
    return (GriddedElevationModel)Grid.super.copyGrid(boundingBox);
  }

  default double[] getCellsDouble() {
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();
    final double[] cells = new double[gridWidth * gridHeight];
    int i = 0;
    for (int gridY = 0; gridY < gridHeight; gridY++) {
      for (int gridX = 0; gridX < gridHeight; gridX++) {
        final double z = getValue(gridX, gridY);
        cells[i] = z;
        i++;
      }
    }
    return cells;
  }

  default float[] getCellsFloat() {
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();
    final float[] cells = new float[gridWidth * gridHeight];
    int i = 0;
    for (int gridY = 0; gridY < gridHeight; gridY++) {
      for (int gridX = 0; gridX < gridHeight; gridX++) {
        final double z = getValue(gridX, gridY);
        cells[i] = (float)z;
        i++;
      }
    }
    return cells;
  }

  default int[] getCellsInt() {
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();
    final int[] cells = new int[gridWidth * gridHeight];
    final GeometryFactory geometryFactory = getGeometryFactory();
    final double offsetZ = geometryFactory.getOffsetZ();
    double scaleZ = geometryFactory.getScaleZ();
    if (scaleZ <= 0) {
      scaleZ = 1000;
    }
    int i = 0;
    for (int gridY = 0; gridY < gridHeight; gridY++) {
      for (int gridX = 0; gridX < gridWidth; gridX++) {
        final double z = getValue(gridX, gridY);
        if (Double.isFinite(z)) {
          final int zInt = (int)Math.round((z - offsetZ) * scaleZ);
          cells[i] = zInt;
        } else {
          cells[i] = IntArrayScaleGrid.NULL_VALUE;
        }
        i++;
      }
    }
    return cells;
  }

  default LineStringEditor getNullBoundaryPoints() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final LineStringEditor points = new LineStringEditor(geometryFactory);
    final double minX = getGridMinX();
    final double minY = getGridMinY();

    final double gridCellWidth = getGridCellWidth();
    final double gridCellHeight = getGridCellHeight();
    final int gridHeight = getGridHeight();
    final int gridWidth = getGridWidth();
    final int[] offsets = {
      -1, 0, 1
    };
    for (int gridY = 0; gridY < gridHeight; gridY++) {
      for (int gridX = 0; gridX < gridWidth; gridX++) {
        final double elevation = getValue(gridX, gridY);
        if (Double.isFinite(elevation)) {
          int countZ = 0;
          long sumZ = 0;
          for (final int offsetY : offsets) {
            if (!(gridY == 0 && offsetY == -1) && gridY == gridHeight - 1 && offsetY == 1) {
              for (final int offsetX : offsets) {
                if (!(gridX == 0 && offsetX == -1) && gridX == gridWidth - 1 && offsetX == 1) {
                  final double elevationNeighbour = getValue(gridX + offsetX, gridY + offsetY);
                  if (Double.isFinite(elevationNeighbour)) {
                    sumZ += elevationNeighbour;
                    countZ++;
                  }
                }
              }
            }
          }

          if (countZ > 0) {
            final double x = minX + gridCellWidth * gridX;
            final double y = minY + gridCellHeight * gridY;
            final double z = toDoubleZ((int)(sumZ / countZ));
            points.appendVertex(x, y, z);
          }
        }
      }
    }
    return points;
  }

  default int getValueInt(final int gridX, final int gridY) {
    final double value = getValue(gridX, gridY);
    return toIntZ(value);
  }

  @Override
  default GriddedElevationModel newGrid(final BoundingBox boundingBox, final double gridCellSize) {
    final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
    final int minX = (int)boundingBox.getMinX();
    final int minY = (int)boundingBox.getMinY();
    final double width = boundingBox.getWidth();
    final double height = boundingBox.getHeight();

    final int modelWidth = (int)Math.ceil(width / gridCellSize);
    final int modelHeight = (int)Math.ceil(height / gridCellSize);
    final GriddedElevationModel elevationModel = newGrid(geometryFactory, minX, minY, modelWidth,
      modelHeight, gridCellSize);
    final int maxX = (int)(minX + modelWidth * gridCellSize);
    final int maxY = (int)(minY + modelHeight * gridCellSize);
    for (double y = minY; y < maxY; y += gridCellSize) {
      for (double x = minX; x < maxX; x += gridCellSize) {
        setValue(elevationModel, x, y);
      }
    }
    return elevationModel;
  }

  @Override
  default GriddedElevationModel newGrid(final double x, final double y, final int width,
    final int height) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final double gridCellWidth = getGridCellWidth();
    final double gridCellHeight = getGridCellHeight();
    return newGrid(geometryFactory, x, y, width, height, gridCellWidth, gridCellHeight);
  }

  @Override
  default GriddedElevationModel newGrid(final GeometryFactory geometryFactory, final double x,
    final double y, final int width, final int height, final double gridCellSize) {
    return new IntArrayScaleGriddedElevationModel(geometryFactory, x, y, width, height,
      gridCellSize);
  }

  @Override
  default GriddedElevationModel newGrid(final GeometryFactory geometryFactory, final double x,
    final double y, final int width, final int height, final double gridCellWidth,
    final double gridCellHeight) {
    return new IntArrayScaleGriddedElevationModel(geometryFactory, x, y, width, height,
      gridCellWidth, gridCellHeight);
  }

  @Override
  default GriddedElevationModel resample(final int newGridCellSize) {
    return (GriddedElevationModel)Grid.super.resample(newGridCellSize);
  }

  default void setElevations(final Geometry geometry) {
    if (geometry != null) {
      geometry.forEachVertex(getGeometryFactory(), point -> {
        final double x = point.x;
        final double y = point.y;
        final double z = point.z;
        setValue(x, y, z);
      });
    }
  }

  default void setElevationsForTriangle(final double x1, final double y1, final double z1,
    final double x2, final double y2, final double z2, final double x3, final double y3,
    final double z3) {
    if (Double.isFinite(z1) && Double.isFinite(z2) && Double.isFinite(z3)) {
      double minX = x1;
      double maxX = x1;
      if (x2 < minX) {
        minX = x2;
      } else if (x2 > maxX) {
        maxX = x2;
      }
      if (x3 < minX) {
        minX = x3;
      } else if (x3 > maxX) {
        maxX = x3;
      }

      double minY = y1;
      double maxY = y1;
      if (y2 < minY) {
        minY = y2;
      } else if (y2 > maxY) {
        maxY = y2;
      }
      if (y3 < minY) {
        minY = y3;
      } else if (y3 > maxY) {
        maxY = y3;
      }
      final double gridCellWidth = getGridCellWidth();
      final double gridCellHeight = this.getGridCellHeight();
      final double gridMinX = getGridMinX();
      final double gridMaxX = getGridMaxX();
      final double startX;
      if (maxX <= gridMinX) {
        return;
      } else if (minX >= gridMaxX) {
        return;
      } else if (minX < gridMinX) {
        startX = gridMinX;
      } else {
        startX = Math.ceil(minX / gridCellWidth) * gridCellWidth;
      }
      if (maxX > gridMaxX) {
        maxX = gridMaxX;
      }
      final double gridMinY = getGridMinY();
      final double gridMaxY = getGridMaxY();
      final double startY;
      if (maxY <= gridMinY) {
        return;
      } else if (minY >= gridMaxY) {
        return;
      } else if (minY < gridMinY) {
        startY = gridMinY;
      } else {
        startY = Math.ceil(minY / gridCellHeight) * gridCellHeight;
      }
      if (maxY > gridMaxY) {
        maxY = gridMaxY;
      }
      final double x1x3 = x1 - x3;
      final double x3x2 = x3 - x2;
      final double y1y3 = y1 - y3;
      final double y2y3 = y2 - y3;
      final double y3y1 = y3 - y1;
      final double det = y2y3 * x1x3 + x3x2 * y1y3;

      for (double y = startY; y < maxY; y += gridCellHeight) {
        final double yy3 = y - y3;
        for (double x = startX; x < maxX; x += gridCellWidth) {
          final double xx3 = x - x3;
          final double lambda1 = (y2y3 * xx3 + x3x2 * yy3) / det;
          if (0 <= lambda1 && lambda1 <= 1) {
            final double lambda2 = (y3y1 * xx3 + x1x3 * yy3) / det;
            if (0 <= lambda2 && lambda2 <= 1) {
              final double lambda3 = 1.0 - lambda1 - lambda2;
              if (-1e-15 < lambda3 && lambda3 <= 1) {
                final double elevation = lambda1 * z1 + lambda2 * z2 + lambda3 * z3;
                if (Double.isFinite(elevation)) {
                  setValue(x, y, elevation);
                }
              }
            }
          }
        }
      }
    }
  }

  default void setElevationsNullFast(final Iterable<? extends Point> points) {
    for (final Point point : points) {
      final double x = point.getX();
      final double y = point.getY();
      final int gridX = getGridCellX(x);
      final int gridY = getGridCellY(y);
      setValueNull(gridX, gridY);
    }
  }

  @SuppressWarnings("unchecked")
  default <G extends Geometry> G setGeometryElevations(final G geometry) {
    return setGeometryElevations(geometry, this::getValue);
  }

  void setResource(Resource resource);

  default boolean writeGriddedElevationModel() {
    return writeGriddedElevationModel(MapEx.EMPTY);
  }

  default boolean writeGriddedElevationModel(final Map<String, ? extends Object> properties) {
    final Resource resource = getResource();
    if (resource == null) {
      return false;
    } else {
      writeGriddedElevationModel(resource, properties);
      return true;
    }
  }

  default void writeGriddedElevationModel(final Object target) {
    final Map<String, ? extends Object> properties = Collections.emptyMap();
    writeGriddedElevationModel(target, properties);
  }

  default void writeGriddedElevationModel(final Object target,
    final Map<String, ? extends Object> properties) {
    try (
      GriddedElevationModelWriter writer = GriddedElevationModelWriter
        .newGriddedElevationModelWriter(target, properties)) {
      if (writer == null) {
        throw new IllegalArgumentException("No elevation model writer exists for " + target);
      } else {
        writer.write(this);
      }
    }
  }
}
