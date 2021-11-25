package com.revolsys.record.io.format.shp;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.function.Function3;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.geometry.model.impl.LineStringDouble;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.io.endian.EndianOutput;

public final class ShapefileGeometryHandler {

  public static final ShapefileGeometryHandler SHP_INSTANCE = new ShapefileGeometryHandler(true);

  public final Map<String, Function3<GeometryFactory, ByteBuffer, Integer, Geometry>> readFunctionByGeometryType = new LinkedHashMap<>();

  public final Map<String, BiConsumer<EndianOutput, Geometry>> writeFunctionByGeometryType = new LinkedHashMap<>();

  private final boolean shpFile;

  private final boolean writeLength;

  public ShapefileGeometryHandler(final boolean shpFile) {
    this.shpFile = shpFile;
    this.writeLength = shpFile;
    {
      final String typeName = "POINT";
      this.readFunctionByGeometryType.put(typeName + "FALSE" + "FALSE", this::readPoint);
      this.readFunctionByGeometryType.put(typeName + "TRUE" + "FALSE", this::readPointZ);
      this.readFunctionByGeometryType.put(typeName + "FALSE" + "TRUE", this::readPointM);
      this.readFunctionByGeometryType.put(typeName + "TRUE" + "TRUE", this::readPointZM);
    }
    {
      final String typeName = "MULTIPOINT";
      this.readFunctionByGeometryType.put(typeName + "FALSE" + "FALSE", this::readMultipoint);
      this.readFunctionByGeometryType.put(typeName + "TRUE" + "FALSE", this::readMultipointZ);
      this.readFunctionByGeometryType.put(typeName + "FALSE" + "TRUE", this::readMultipointM);
      this.readFunctionByGeometryType.put(typeName + "TRUE" + "TRUE", this::readMultipointZM);
    }

    for (final String typeName : Arrays.asList("LINESTRING", "MULTILINESTRING")) {
      this.readFunctionByGeometryType.put(typeName + "FALSE" + "FALSE", this::readPolyline);
      this.readFunctionByGeometryType.put(typeName + "TRUE" + "FALSE", this::readPolylineZ);
      this.readFunctionByGeometryType.put(typeName + "FALSE" + "TRUE", this::readPolylineM);
      this.readFunctionByGeometryType.put(typeName + "TRUE" + "TRUE", this::readPolylineZM);
    }
    for (final String typeName : Arrays.asList("POLYGON", "MULTIPOLYGON")) {
      this.readFunctionByGeometryType.put(typeName + "FALSE" + "FALSE", this::readPolygon);
      this.readFunctionByGeometryType.put(typeName + "TRUE" + "FALSE", this::readPolygonZ);
      this.readFunctionByGeometryType.put(typeName + "FALSE" + "TRUE", this::readPolygonM);
      this.readFunctionByGeometryType.put(typeName + "TRUE" + "TRUE", this::readPolygonZM);
    }

    {
      final String typeName = "POINT";
      this.writeFunctionByGeometryType.put(typeName + "FALSE" + "FALSE", this::writePoint);
      this.writeFunctionByGeometryType.put(typeName + "TRUE" + "FALSE", this::writePointZ);
      this.writeFunctionByGeometryType.put(typeName + "FALSE" + "TRUE", this::writePointM);
      this.writeFunctionByGeometryType.put(typeName + "TRUE" + "TRUE", this::writePointZM);
    }
    {
      final String typeName = "MULTIPOINT";
      this.writeFunctionByGeometryType.put(typeName + "FALSE" + "FALSE", this::writeMultipoint);
      this.writeFunctionByGeometryType.put(typeName + "TRUE" + "FALSE", this::writeMultipointZ);
      this.writeFunctionByGeometryType.put(typeName + "FALSE" + "TRUE", this::writeMultipointM);
      this.writeFunctionByGeometryType.put(typeName + "TRUE" + "TRUE", this::writeMultipointZM);
    }

    for (final String typeName : Arrays.asList("LINESTRING", "MULTILINESTRING")) {
      this.writeFunctionByGeometryType.put(typeName + "FALSE" + "FALSE", this::writePolyline);
      this.writeFunctionByGeometryType.put(typeName + "TRUE" + "FALSE", this::writePolylineZ);
      this.writeFunctionByGeometryType.put(typeName + "FALSE" + "TRUE", this::writePolylineM);
      this.writeFunctionByGeometryType.put(typeName + "TRUE" + "TRUE", this::writePolylineZM);
    }
    for (final String typeName : Arrays.asList("POLYGON", "MULTIPOLYGON")) {
      this.writeFunctionByGeometryType.put(typeName + "FALSE" + "FALSE", this::writePolygon);
      this.writeFunctionByGeometryType.put(typeName + "TRUE" + "FALSE", this::writePolygonZ);
      this.writeFunctionByGeometryType.put(typeName + "FALSE" + "TRUE", this::writePolygonM);
      this.writeFunctionByGeometryType.put(typeName + "TRUE" + "TRUE", this::writePolygonZM);
    }
  }

  public Function3<GeometryFactory, ByteBuffer, Integer, Geometry> getReadFunction(
    String geometryTypeKey) {
    geometryTypeKey = geometryTypeKey.toUpperCase();
    final Function3<GeometryFactory, ByteBuffer, Integer, Geometry> function = this.readFunctionByGeometryType
      .get(geometryTypeKey);
    if (function == null) {
      throw new IllegalArgumentException("Cannot get Shape Reader for: " + geometryTypeKey);
    }
    return function;
  }

  public int getShapeType(final Geometry geometry) {
    if (geometry != null) {
      final GeometryFactory geometryFactory = geometry.getGeometryFactory();
      final DataType dataType = geometry.getDataType();
      return getShapeType(geometryFactory, dataType);
    }
    return ShapefileConstants.NULL_SHAPE;
  }

  public int getShapeType(final GeometryFactory geometryFactory, final DataType dataType) {
    final int axisCount = geometryFactory.getAxisCount();
    final boolean hasZ = axisCount > 2;
    final boolean hasM = axisCount > 3;

    if (GeometryDataTypes.POINT.equals(dataType)) {
      if (hasM) {
        return ShapefileConstants.POINT_ZM_SHAPE;
      } else if (hasZ) {
        if (this.shpFile) {
          return ShapefileConstants.POINT_ZM_SHAPE;
        } else {
          return ShapefileConstants.POINT_Z_SHAPE;
        }
      } else {
        return ShapefileConstants.POINT_SHAPE;
      }
    } else if (GeometryDataTypes.MULTI_POINT.equals(dataType)) {
      if (hasM) {
        return ShapefileConstants.MULTI_POINT_ZM_SHAPE;
      } else if (hasZ) {
        if (this.shpFile) {
          return ShapefileConstants.MULTI_POINT_ZM_SHAPE;
        } else {
          return ShapefileConstants.MULTI_POINT_Z_SHAPE;
        }
      } else {
        return ShapefileConstants.MULTI_POINT_SHAPE;
      }
    } else if (GeometryDataTypes.LINEAR_RING.equals(dataType)
      || GeometryDataTypes.LINE_STRING.equals(dataType)
      || GeometryDataTypes.MULTI_LINE_STRING.equals(dataType)) {
      if (hasM) {
        return ShapefileConstants.POLYLINE_ZM_SHAPE;
      } else if (hasZ) {
        if (this.shpFile) {
          return ShapefileConstants.POLYLINE_ZM_SHAPE;
        } else {
          return ShapefileConstants.POLYLINE_Z_SHAPE;
        }
      } else {
        return ShapefileConstants.POLYLINE_SHAPE;
      }
    } else if (GeometryDataTypes.POLYGON.equals(dataType)
      || GeometryDataTypes.MULTI_POLYGON.equals(dataType)) {
      if (hasM) {
        return ShapefileConstants.POLYGON_ZM_SHAPE;
      } else if (hasZ) {
        if (this.shpFile) {
          return ShapefileConstants.POLYGON_ZM_SHAPE;
        } else {
          return ShapefileConstants.POLYGON_Z_SHAPE;
        }
      } else {
        return ShapefileConstants.POLYGON_SHAPE;
      }
    } else {
      throw new IllegalArgumentException("Unsupported geometry type: " + dataType);
    }
  }

  public BiConsumer<EndianOutput, Geometry> getWriteFunction(String geometryTypeKey) {
    geometryTypeKey = geometryTypeKey.toUpperCase();
    final BiConsumer<EndianOutput, Geometry> function = this.writeFunctionByGeometryType
      .get(geometryTypeKey);
    if (function == null) {
      throw new IllegalArgumentException("Cannot get Shape Reader for: " + geometryTypeKey);
    }
    return function;
  }

  public boolean isShpFile() {
    return this.shpFile;
  }

  public List<double[]> newCoordinatesLists(final int[] partIndex, final int axisCount) {
    final List<double[]> parts = new ArrayList<>(partIndex.length);
    for (final int partNumPoints : partIndex) {
      final double[] coordinates = new double[partNumPoints * axisCount];
      parts.add(coordinates);
    }
    return parts;
  }

  public Geometry newPolygonGeometryFromParts(final GeometryFactory geometryFactory,
    final List<double[]> parts, final int axisCount) {
    final List<Polygon> polygons = new ArrayList<>();
    final List<LinearRing> currentParts = new ArrayList<>();
    for (final double[] coordinates : parts) {
      final LinearRing ring = geometryFactory.linearRing(axisCount, coordinates);
      final boolean ringClockwise = ring.isClockwise();
      if (ringClockwise) {
        if (!currentParts.isEmpty()) {
          final Polygon polygon = geometryFactory.polygon(currentParts);
          polygons.add(polygon);
          currentParts.clear();
        }
      }
      currentParts.add(ring);
    }
    if (!currentParts.isEmpty()) {
      final Polygon polygon = geometryFactory.polygon(currentParts);
      polygons.add(polygon);
    }
    if (polygons.size() == 1) {
      return polygons.get(0);
    } else {
      return geometryFactory.polygonal(polygons);
    }
  }

  public void readCoordinates(final ByteBuffer buffer, final int vertexCount, final int axisCount,
    final double[] coordinates, final int axisIndex) {
    for (int j = 0; j < vertexCount; j++) {
      double value = buffer.getDouble();
      ;
      if (value == -Double.MAX_VALUE) {
        value = Double.NaN;
      }
      coordinates[j * axisCount + axisIndex] = value;
    }
  }

  public void readCoordinates(final ByteBuffer buffer, final int[] partIndex,
    final List<double[]> coordinateLists, final int axisIndex, final int axisCount) {
    buffer.getDouble();
    buffer.getDouble();
    ;
    for (int i = 0; i < partIndex.length; i++) {
      final double[] coordinates = coordinateLists.get(i);
      final int vertexCount = coordinates.length / axisCount;
      readCoordinates(buffer, vertexCount, axisCount, coordinates, axisIndex);
    }
  }

  public int[] readIntArray(final ByteBuffer buffer, final int count) {
    final int[] values = new int[count];
    for (int i = 0; i < count; i++) {
      final int value = buffer.getInt();
      values[i] = value;
    }
    return values;
  }

  public Punctual readMultipoint(final GeometryFactory geometryFactory, final ByteBuffer buffer,
    final int recordLength) {
    buffer.getDouble();
    buffer.getDouble();
    buffer.getDouble();
    buffer.getDouble();
    final int vertexCount = buffer.getInt();
    final double[] coordinates = readXYCoordinates(buffer, vertexCount, 2);
    return geometryFactory.punctual(new LineStringDouble(2, coordinates));
  }

  public Punctual readMultipointM(final GeometryFactory geometryFactory, final ByteBuffer buffer,
    final int recordLength) {
    buffer.getDouble();
    buffer.getDouble();
    buffer.getDouble();
    buffer.getDouble();
    final int vertexCount = buffer.getInt();
    final int axisCount = 4;
    final double[] coordinates = readXYCoordinates(buffer, vertexCount, axisCount);
    buffer.getDouble();
    buffer.getDouble();
    setCoordinatesNaN(coordinates, vertexCount, axisCount, 2);
    readCoordinates(buffer, vertexCount, axisCount, coordinates, 3);
    return geometryFactory.punctual(new LineStringDouble(axisCount, coordinates));
  }

  public Punctual readMultipointZ(final GeometryFactory geometryFactory, final ByteBuffer buffer,
    final int recordLength) {
    buffer.getDouble();
    buffer.getDouble();
    buffer.getDouble();
    buffer.getDouble();
    final int vertexCount = buffer.getInt();
    final double[] coordinates = readXYCoordinates(buffer, vertexCount, 3);
    buffer.getDouble();
    buffer.getDouble();
    readCoordinates(buffer, vertexCount, 3, coordinates, 2);
    return geometryFactory.punctual(new LineStringDouble(3, coordinates));
  }

  public Punctual readMultipointZM(GeometryFactory geometryFactory, final ByteBuffer buffer,
    final int recordLength) {
    buffer.getDouble();
    buffer.getDouble();
    buffer.getDouble();
    buffer.getDouble();
    final int vertexCount = buffer.getInt();
    int axisCount;
    if (recordLength == 20 + 12 * vertexCount) {
      geometryFactory = geometryFactory.convertAxisCount(3);
      axisCount = 3;
    } else {
      axisCount = 4;
    }
    final double[] coordinates = readXYCoordinates(buffer, vertexCount, axisCount);
    buffer.getDouble();
    buffer.getDouble();
    ;
    readCoordinates(buffer, vertexCount, axisCount, coordinates, 2);
    if (axisCount == 4) {
      buffer.getDouble();
      buffer.getDouble();
      ;
      readCoordinates(buffer, vertexCount, axisCount, coordinates, 3);
    }
    return geometryFactory.punctual(axisCount, coordinates);
  }

  public int[] readPartIndex(final ByteBuffer buffer, final int numParts, final int vertexCount) {
    final int[] partIndex = new int[numParts];
    if (numParts > 0) {
      int startIndex = buffer.getInt();
      for (int i = 1; i < partIndex.length; i++) {
        final int index = buffer.getInt();
        partIndex[i - 1] = index - startIndex;
        startIndex = index;
      }
      partIndex[partIndex.length - 1] = vertexCount - startIndex;
    }
    return partIndex;
  }

  public Point readPoint(final GeometryFactory geometryFactory, final ByteBuffer buffer,
    final int recordLength) {
    final double[] coordinates = readXYCoordinates(buffer, 1, 2);
    return geometryFactory.point(coordinates);
  }

  public Point readPointM(final GeometryFactory geometryFactory, final ByteBuffer buffer,
    final int recordLength) {
    final double x = buffer.getDouble();
    final double y = buffer.getDouble();
    final double z = 0;
    final double m = buffer.getDouble();
    return geometryFactory.point(x, y, z, m);
  }

  public void readPoints(final ByteBuffer buffer, final int[] partIndex,
    final List<double[]> coordinateLists, final int axisCount) {
    for (int i = 0; i < partIndex.length; i++) {
      final int count = partIndex[i];
      final double[] coordinates = coordinateLists.get(i);
      readXYCoordinates(buffer, axisCount, count, coordinates);
    }
  }

  public Point readPointZ(final GeometryFactory geometryFactory, final ByteBuffer buffer,
    final int recordLength) {
    final double x = buffer.getDouble();
    final double y = buffer.getDouble();
    final double z = buffer.getDouble();
    return geometryFactory.point(x, y, z);
  }

  public Point readPointZM(final GeometryFactory geometryFactory, final ByteBuffer buffer,
    final int recordLength) {
    final double x = buffer.getDouble();
    final double y = buffer.getDouble();
    final double z = buffer.getDouble();
    if (recordLength == 14) {
      return geometryFactory.convertAxisCount(3).point(x, y, z);
    } else {
      final double m = buffer.getDouble();
      return geometryFactory.point(x, y, z, m);
    }
  }

  public Geometry readPolygon(final GeometryFactory geometryFactory, final ByteBuffer buffer,
    final int recordLength) {
    buffer.getDouble();
    buffer.getDouble();
    buffer.getDouble();
    buffer.getDouble();
    final int numParts = buffer.getInt();
    final int vertexCount = buffer.getInt();
    final int[] partIndex = readPartIndex(buffer, numParts, vertexCount);

    final List<double[]> parts = newCoordinatesLists(partIndex, 2);

    readPoints(buffer, partIndex, parts, 2);

    return newPolygonGeometryFromParts(geometryFactory, parts, 2);

  }

  public Geometry readPolygonM(final GeometryFactory geometryFactory, final ByteBuffer buffer,
    final int recordLength) {
    buffer.getDouble();
    buffer.getDouble();
    buffer.getDouble();
    buffer.getDouble();
    final int partCount = buffer.getInt();
    final int vertexCount = buffer.getInt();
    final int axisCount = 4;
    final int[] partIndex = readPartIndex(buffer, partCount, vertexCount);

    final List<double[]> parts = newCoordinatesLists(partIndex, axisCount);
    readPoints(buffer, partIndex, parts, axisCount);
    readCoordinates(buffer, partIndex, parts, 3, axisCount);
    return newPolygonGeometryFromParts(geometryFactory, parts, axisCount);

  }

  public Geometry readPolygonZ(final GeometryFactory geometryFactory, final ByteBuffer buffer,
    final int recordLength) {
    buffer.getDouble();
    buffer.getDouble();
    buffer.getDouble();
    buffer.getDouble();
    final int numParts = buffer.getInt();
    final int vertexCount = buffer.getInt();
    final int axisCount = 3;
    final int[] partIndex = readPartIndex(buffer, numParts, vertexCount);

    final List<double[]> parts = newCoordinatesLists(partIndex, axisCount);
    readPoints(buffer, partIndex, parts, axisCount);
    readCoordinates(buffer, partIndex, parts, 2, axisCount);
    return newPolygonGeometryFromParts(geometryFactory, parts, axisCount);
  }

  public Geometry readPolygonZM(GeometryFactory geometryFactory, final ByteBuffer buffer,
    final int recordLength) {
    buffer.getDouble();
    buffer.getDouble();
    buffer.getDouble();
    buffer.getDouble();
    final int numParts = buffer.getInt();
    final int vertexCount = buffer.getInt();
    final int[] partIndex = readPartIndex(buffer, numParts, vertexCount);
    final int axisCount;
    if (recordLength == 22 + 8 + 2 * numParts + 12 * vertexCount) {
      axisCount = 3;
      geometryFactory = geometryFactory.convertAxisCount(3);
    } else {
      axisCount = 4;
    }
    final List<double[]> parts = newCoordinatesLists(partIndex, axisCount);
    readPoints(buffer, partIndex, parts, axisCount);
    readCoordinates(buffer, partIndex, parts, 2, axisCount);
    if (axisCount == 4) {
      readCoordinates(buffer, partIndex, parts, 3, axisCount);
    }
    return newPolygonGeometryFromParts(geometryFactory, parts, axisCount);
  }

  public Geometry readPolyline(final GeometryFactory geometryFactory, final ByteBuffer buffer,
    final int recordLength) {
    buffer.getDouble();
    buffer.getDouble();
    buffer.getDouble();
    buffer.getDouble();
    final int numParts = buffer.getInt();
    final int vertexCount = buffer.getInt();
    final int axisCount = 2;
    if (numParts == 1) {
      buffer.getInt();
      final double[] coordinates = readXYCoordinates(buffer, vertexCount, axisCount);

      return geometryFactory.lineString(2, coordinates);
    } else {
      final int[] partIndex = new int[numParts + 1];
      partIndex[numParts] = vertexCount;
      for (int i = 0; i < partIndex.length - 1; i++) {
        partIndex[i] = buffer.getInt();

      }
      final List<LineString> lines = new ArrayList<>();
      for (int i = 0; i < partIndex.length - 1; i++) {
        final int startIndex = partIndex[i];
        final int endIndex = partIndex[i + 1];
        final int numCoords = endIndex - startIndex;
        final double[] coordinates = readXYCoordinates(buffer, numCoords, axisCount);
        lines.add(geometryFactory.lineString(2, coordinates));
      }
      return geometryFactory.lineal(lines);
    }
  }

  public Geometry readPolylineM(final GeometryFactory geometryFactory, final ByteBuffer buffer,
    final int recordLength) {
    buffer.getDouble();
    buffer.getDouble();
    buffer.getDouble();
    buffer.getDouble();
    final int partCount = buffer.getInt();
    final int allVertexCount = buffer.getInt();
    final int axisCount = 4;
    if (partCount == 1) {
      buffer.getInt();
      final double[] coordinates = readXYCoordinates(buffer, allVertexCount, axisCount);
      buffer.getDouble();
      buffer.getDouble();
      setCoordinatesNaN(coordinates, allVertexCount, axisCount, 2);
      readCoordinates(buffer, allVertexCount, axisCount, coordinates, 3);
      return geometryFactory.lineString(axisCount, coordinates);
    } else {
      final int[] partIndex = new int[partCount + 1];
      partIndex[partCount] = allVertexCount;
      for (int i = 0; i < partIndex.length - 1; i++) {
        partIndex[i] = buffer.getInt();
      }
      final List<double[]> coordinatesList = new ArrayList<>();
      for (int i = 0; i < partIndex.length - 1; i++) {
        final int startIndex = partIndex[i];
        final int endIndex = partIndex[i + 1];
        final int vertexCount = endIndex - startIndex;
        final double[] coordinates = readXYCoordinates(buffer, vertexCount, axisCount);
        coordinatesList.add(coordinates);
      }
      buffer.getDouble();
      buffer.getDouble();
      for (int i = 0; i < partIndex.length - 1; i++) {
        final double[] coordinates = coordinatesList.get(i);
        final int vertexCount = coordinates.length / axisCount;
        readCoordinates(buffer, vertexCount, axisCount, coordinates, 3);
      }

      return geometryFactory.lineal(axisCount,
        coordinatesList.toArray(new double[coordinatesList.size()][]));
    }
  }

  public Geometry readPolylineZ(final GeometryFactory geometryFactory, final ByteBuffer buffer,
    final int recordLength) {
    buffer.getDouble();
    buffer.getDouble();
    buffer.getDouble();
    buffer.getDouble();
    final int numParts = buffer.getInt();
    final int vertexCount = buffer.getInt();
    return readPolylineZ(geometryFactory, buffer, numParts, vertexCount);
  }

  public Geometry readPolylineZ(final GeometryFactory geometryFactory, final ByteBuffer buffer,
    final int partCount, final int allVertexCount) {
    final int axisCount = 3;
    if (partCount == 1) {
      buffer.getInt();
      final double[] coordinates = readXYCoordinates(buffer, allVertexCount, axisCount);
      buffer.getDouble();
      buffer.getDouble();
      readCoordinates(buffer, allVertexCount, axisCount, coordinates, 2);
      return geometryFactory.lineString(axisCount, coordinates);
    } else {
      final int[] partIndex = new int[partCount + 1];
      partIndex[partCount] = allVertexCount;
      for (int i = 0; i < partCount; i++) {
        partIndex[i] = buffer.getInt();
      }
      final double[][] linesCoordinates = new double[partCount][];
      for (int i = 0; i < partCount; i++) {
        final int startIndex = partIndex[i];
        final int endIndex = partIndex[i + 1];
        final int vertexCount = endIndex - startIndex;
        linesCoordinates[i] = readXYCoordinates(buffer, vertexCount, axisCount);
      }
      buffer.getDouble();
      buffer.getDouble();
      for (int i = 0; i < partCount; i++) {
        final double[] coordinates = linesCoordinates[i];
        final int vertexCount = coordinates.length / axisCount;
        readCoordinates(buffer, vertexCount, axisCount, coordinates, 2);
      }
      return geometryFactory.lineal(axisCount, linesCoordinates);
    }
  }

  public Geometry readPolylineZM(GeometryFactory geometryFactory, final ByteBuffer buffer,
    final int recordLength) {
    buffer.getDouble();
    buffer.getDouble();
    buffer.getDouble();
    buffer.getDouble();
    final int geometryCount = buffer.getInt();
    final int vertexCount = buffer.getInt();
    if (22 + geometryCount * 2 + vertexCount * 12 == recordLength) {
      geometryFactory = geometryFactory.convertAxisCount(3);
      return readPolylineZ(geometryFactory, buffer, geometryCount, vertexCount);
    } else {
      final int axisCount = 4;
      if (geometryCount == 1) {
        buffer.getInt();
        final double[] coordinates = readXYCoordinates(buffer, vertexCount, axisCount);
        buffer.getDouble();
        buffer.getDouble();
        readCoordinates(buffer, vertexCount, axisCount, coordinates, 2);
        buffer.getDouble();
        buffer.getDouble();
        readCoordinates(buffer, vertexCount, axisCount, coordinates, 3);
        return geometryFactory.lineString(axisCount, coordinates);
      } else {
        final int[] partIndex = new int[geometryCount + 1];
        partIndex[geometryCount] = vertexCount;
        for (int i = 0; i < partIndex.length - 1; i++) {
          partIndex[i] = buffer.getInt();
        }
        final List<double[]> coordinatesList = new ArrayList<>();
        for (int i = 0; i < partIndex.length - 1; i++) {
          final int startIndex = partIndex[i];
          final int endIndex = partIndex[i + 1];
          final int numCoords = endIndex - startIndex;
          final double[] coordinates = readXYCoordinates(buffer, numCoords, axisCount);
          coordinatesList.add(coordinates);
        }
        buffer.getDouble();
        buffer.getDouble();
        for (int i = 0; i < partIndex.length - 1; i++) {
          final double[] coordinates = coordinatesList.get(i);
          readCoordinates(buffer, coordinates.length / 4, axisCount, coordinates, 2);
        }
        buffer.getDouble();
        buffer.getDouble();
        for (int i = 0; i < partIndex.length - 1; i++) {
          final double[] coordinates = coordinatesList.get(i);
          readCoordinates(buffer, coordinates.length / 4, axisCount, coordinates, 3);
        }
        final List<LineString> lines = new ArrayList<>();
        for (final double[] coordinates : coordinatesList) {
          lines.add(geometryFactory.lineString(axisCount, coordinates));
        }
        return geometryFactory.lineal(lines);
      }
    }
  }

  public double[] readXYCoordinates(final ByteBuffer buffer, final int vertexCount,
    final int axisCount) {
    final double[] coordinates = new double[vertexCount * axisCount];
    readXYCoordinates(buffer, axisCount, vertexCount, coordinates);
    return coordinates;
  }

  public void readXYCoordinates(final ByteBuffer buffer, final int axisCount, final int vertexCount,
    final double[] coordinates) {
    for (int j = 0; j < vertexCount; j++) {
      final double x = buffer.getDouble();
      final double y = buffer.getDouble();
      coordinates[j * axisCount] = x;
      coordinates[j * axisCount + 1] = y;
    }
  }

  public void setCoordinatesNaN(final ByteBuffer buffer, final int[] partIndex,
    final List<double[]> coordinateLists, final int axisIndex, final int axisCount) {
    buffer.getDouble();
    buffer.getDouble();
    for (int i = 0; i < partIndex.length; i++) {
      final double[] coordinates = coordinateLists.get(i);
      final int vertexCount = coordinates.length / axisCount;
      setCoordinatesNaN(coordinates, vertexCount, axisCount, axisIndex);
    }
  }

  public void setCoordinatesNaN(final double[] coordinates, final int vertexCount,
    final int axisCount, final int axisIndex) {
    for (int j = 0; j < vertexCount; j++) {
      coordinates[j * axisCount + axisIndex] = Double.NaN;
    }
  }

  public void writeEnvelope(final EndianOutput out, final BoundingBox envelope) {
    out.writeLEDouble(envelope.getMinX());
    out.writeLEDouble(envelope.getMinY());
    out.writeLEDouble(envelope.getMaxX());
    out.writeLEDouble(envelope.getMaxY());
  }

  public void writeMCoordinates(final EndianOutput out, final Geometry geometry) {
    writeMCoordinatesRange(out, geometry);
    if (geometry.getAxisCount() >= 4) {
      for (final Vertex vertex : geometry.vertices()) {
        final double m = vertex.getM();
        if (Double.isFinite(m)) {
          out.writeLEDouble(m);
        } else {
          out.writeLEDouble(0);
        }
      }
    } else {
      for (int i = 0; i < geometry.getVertexCount(); i++) {
        out.writeLEDouble(0);
      }
    }
  }

  public void writeMCoordinates(final EndianOutput out, final LineString coordinates) {
    if (coordinates.getAxisCount() >= 4) {
      for (int i = 0; i < coordinates.getVertexCount(); i++) {
        final double m = coordinates.getM(i);
        if (Double.isFinite(m)) {
          out.writeLEDouble(m);
        } else {
          out.writeLEDouble(0);
        }
      }
    } else {
      for (int i = 0; i < coordinates.getVertexCount(); i++) {
        out.writeLEDouble(0);
      }
    }
  }

  public void writeMCoordinates(final EndianOutput out, final List<LineString> coordinatesList) {
    writeMCoordinatesRange(out, coordinatesList);
    for (final LineString coordinates : coordinatesList) {
      writeMCoordinates(out, coordinates);
    }
  }

  public void writeMCoordinatesRange(final EndianOutput out, final Geometry geometry) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    final double minM = boundingBox.getMin(3);
    final double maxM = boundingBox.getMax(3);
    if (Double.isFinite(minM) && Double.isFinite(maxM)) {
      out.writeLEDouble(minM);
      out.writeLEDouble(maxM);
    } else {
      out.writeLEDouble(0);
      out.writeLEDouble(0);
    }
  }

  public void writeMCoordinatesRange(final EndianOutput out,
    final List<LineString> coordinatesList) {
    double minM = Double.MAX_VALUE;
    double maxM = -Double.MAX_VALUE;
    for (final LineString ring : coordinatesList) {
      for (int i = 0; i < ring.getVertexCount(); i++) {
        double m = ring.getCoordinate(i, 2);
        if (!Double.isFinite(m)) {
          m = 0;
        }
        minM = Math.min(m, minM);
        maxM = Math.max(m, maxM);
      }
    }
    if (minM == Double.MAX_VALUE && maxM == -Double.MAX_VALUE) {
      out.writeLEDouble(0);
      out.writeLEDouble(0);
    } else {
      out.writeLEDouble(minM);
      out.writeLEDouble(maxM);
    }
  }

  public void writeMultipoint(final EndianOutput out, final Geometry geometry) {
    writeMultipoint(out, geometry, ShapefileConstants.MULTI_POINT_SHAPE, 8);
  }

  private void writeMultipoint(final EndianOutput out, final Geometry geometry, final int shapeType,
    final int wordsPerPoint) {
    if (geometry instanceof Punctual) {
      final int vertexCount = geometry.getVertexCount();
      if (this.writeLength) {
        final int recordLength = 20 + wordsPerPoint * vertexCount;
        // (BYTES_IN_INT + 4 * BYTES_IN_DOUBLE + BYTES_IN_INT +
        // (vertexCount * 2 * BYTES_IN_DOUBLE)) / BYTES_IN_SHORT;
        out.writeInt(recordLength);
      }
      out.writeLEInt(shapeType);
      final BoundingBox envelope = geometry.getBoundingBox();
      writeEnvelope(out, envelope);
      out.writeLEInt(vertexCount);
      writeXYCoordinates(out, geometry);
    } else {
      throw new IllegalArgumentException(
        "Expecting Punctual geometry got " + geometry.getGeometryType());
    }
  }

  public void writeMultipointM(final EndianOutput out, final Geometry geometry) {
    writeMultipoint(out, geometry, ShapefileConstants.MULTI_POINT_M_SHAPE, 12);
    writeMCoordinates(out, geometry);
  }

  public void writeMultipointZ(final EndianOutput out, final Geometry geometry) {
    int shapeType;
    if (this.shpFile) {
      shapeType = ShapefileConstants.MULTI_POINT_ZM_SHAPE;
    } else {
      shapeType = ShapefileConstants.MULTI_POINT_Z_SHAPE;
    }

    writeMultipoint(out, geometry, shapeType, 12);
    writeZCoordinates(out, geometry);
  }

  public void writeMultipointZM(final EndianOutput out, final Geometry geometry) {
    writeMultipoint(out, geometry, ShapefileConstants.MULTI_POINT_ZM_SHAPE, 16);
    writeZCoordinates(out, geometry);
    writeMCoordinates(out, geometry);
  }

  public void writePoint(final EndianOutput out, final Geometry geometry) {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      if (this.writeLength) {
        final int recordLength = 10;
        // (BYTES_IN_INT + 2 * BYTES_IN_DOUBLE) / BYTES_IN_SHORT;
        out.writeInt(recordLength);
      }
      out.writeLEInt(ShapefileConstants.POINT_SHAPE);
      writeXy(out, point);
    } else {
      throw new IllegalArgumentException(
        "Expecting " + Point.class + " geometry got " + geometry.getClass());
    }
  }

  public void writePointM(final EndianOutput out, final Geometry geometry) {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      if (this.writeLength) {
        final int recordLength = 14;
        // (BYTES_IN_INT + 3 * BYTES_IN_DOUBLE) / BYTES_IN_SHORT;
        out.writeInt(recordLength);
      }
      out.writeLEInt(ShapefileConstants.POINT_M_SHAPE);
      writeXy(out, point);

      final double m = point.getM();
      if (Double.isFinite(m)) {
        out.writeLEDouble(m);
      } else {
        out.writeLEDouble(0);
      }
    } else {
      throw new IllegalArgumentException(
        "Expecting " + Point.class + " geometry got " + geometry.getClass());
    }
  }

  public void writePointZ(final EndianOutput out, final Geometry geometry) {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      if (this.writeLength) {
        final int recordLength = 14;
        // (BYTES_IN_INT + 3 * BYTES_IN_DOUBLE) / BYTES_IN_SHORT;
        out.writeInt(recordLength);
      }
      if (this.shpFile) {
        out.writeLEInt(ShapefileConstants.POINT_ZM_SHAPE);
      } else {
        out.writeLEInt(ShapefileConstants.POINT_Z_SHAPE);
      }
      writeXy(out, point);
      final double z = point.getZ();
      if (Double.isFinite(z)) {
        out.writeLEDouble(z);
      } else {
        out.writeLEDouble(0);
      }
    } else {
      throw new IllegalArgumentException(
        "Expecting " + Point.class + " geometry got " + geometry.getClass());
    }
  }

  public void writePointZM(final EndianOutput out, final Geometry geometry) {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      if (this.writeLength) {
        final int recordLength = 18;
        // (BYTES_IN_INT + 4 * BYTES_IN_DOUBLE) / BYTES_IN_SHORT;
        out.writeInt(recordLength);
      }
      out.writeLEInt(ShapefileConstants.POINT_ZM_SHAPE);
      writeXy(out, point);
      final double z = point.getZ();
      if (Double.isFinite(z)) {
        out.writeLEDouble(z);
      } else {
        out.writeLEDouble(0);
      }
      final double m = point.getM();
      if (Double.isFinite(m)) {
        out.writeLEDouble(m);
      } else {
        out.writeLEDouble(0);
      }
    } else {
      throw new IllegalArgumentException(
        "Expecting " + Point.class + " geometry got " + geometry.getClass());
    }
  }

  public void writePolygon(final EndianOutput out, final Geometry geometry) {
    writePolygon(out, geometry, ShapefileConstants.POLYGON_SHAPE, 0, 8);
  }

  private List<LineString> writePolygon(final EndianOutput out, final Geometry geometry,
    final int shapeType, final int headerOverhead, final int wordsPerPoint) {

    int vertexCount = 0;

    final List<LineString> rings = new ArrayList<>();
    for (int i = 0; i < geometry.getGeometryCount(); i++) {
      final Geometry part = geometry.getGeometry(i);
      if (part instanceof Polygon) {
        final Polygon polygon = (Polygon)part;
        LineString shell = polygon.getShell();
        shell = shell.toClockwise();
        rings.add(shell);
        vertexCount += shell.getVertexCount();
        final int numHoles = polygon.getHoleCount();
        for (int j = 0; j < numHoles; j++) {
          LineString hole = polygon.getHole(j);
          hole = hole.toCounterClockwise();
          rings.add(hole);
          vertexCount += hole.getVertexCount();
        }
      } else {
        throw new IllegalArgumentException(
          "Expecting " + Polygon.class + " geometry got " + part.getClass());
      }
    }
    final int numParts = rings.size();

    if (this.writeLength) {
      final int recordLength = 22 + headerOverhead + 2 * numParts + wordsPerPoint * vertexCount;

      out.writeInt(recordLength);
    }
    out.writeLEInt(shapeType);
    final BoundingBox envelope = geometry.getBoundingBox();
    writeEnvelope(out, envelope);
    out.writeLEInt(numParts);
    out.writeLEInt(vertexCount);

    int partIndex = 0;
    for (final LineString ring : rings) {
      out.writeLEInt(partIndex);
      partIndex += ring.getVertexCount();
    }

    for (final LineString ring : rings) {
      writeXYCoordinates(out, ring);
    }
    return rings;
  }

  public void writePolygonM(final EndianOutput out, final Geometry geometry) {
    final List<LineString> rings = writePolygon(out, geometry, ShapefileConstants.POLYGON_M_SHAPE,
      8, 12);
    writeMCoordinates(out, rings);
  }

  public void writePolygonZ(final EndianOutput out, final Geometry geometry) {
    int shapeType;
    if (this.shpFile) {
      shapeType = ShapefileConstants.POLYGON_ZM_SHAPE;
    } else {
      shapeType = ShapefileConstants.POLYGON_Z_SHAPE;
    }
    final List<LineString> rings = writePolygon(out, geometry, shapeType, 8, 12);
    writeZCoordinates(out, rings);
  }

  public void writePolygonZM(final EndianOutput out, final Geometry geometry) {
    final List<LineString> rings = writePolygon(out, geometry, ShapefileConstants.POLYGON_ZM_SHAPE,
      16, 16);
    writeZCoordinates(out, rings);
    writeMCoordinates(out, rings);
  }

  public void writePolyline(final EndianOutput out, final Geometry geometry) {
    writePolyline(out, geometry, ShapefileConstants.POLYLINE_SHAPE, 8);
  }

  private void writePolyline(final EndianOutput out, final Geometry geometry, final int shapeType,
    final int wordsPerPoint) {
    if (geometry instanceof Lineal) {
      final int numCoordinates = geometry.getVertexCount();
      final int numGeometries = geometry.getGeometryCount();
      final BoundingBox envelope = geometry.getBoundingBox();

      if (this.writeLength) {
        // final int recordLength = ((3 + numGeometries) * BYTES_IN_INT + (4 + 2
        // * numCoordinates)
        // * BYTES_IN_DOUBLE) / 2;
        final int recordLength = 22 + numGeometries * 2 + numCoordinates * wordsPerPoint;
        out.writeInt(recordLength);
      }
      out.writeLEInt(shapeType);
      writeEnvelope(out, envelope);
      out.writeLEInt(numGeometries);
      out.writeLEInt(numCoordinates);
      writePolylinePartIndexes(out, geometry);
      writeXYCoordinates(out, geometry);
    } else {
      throw new IllegalArgumentException(
        "Expecting Lineal geometry got " + geometry.getGeometryType() + "\n" + geometry);
    }
  }

  public void writePolylineM(final EndianOutput out, final Geometry geometry) {
    writePolyline(out, geometry, ShapefileConstants.POLYLINE_M_SHAPE, 12);
    writeMCoordinates(out, geometry);
  }

  public void writePolylinePartIndexes(final EndianOutput out, final Geometry geometry) {
    int partIndex = 0;
    for (int i = 0; i < geometry.getGeometryCount(); i++) {
      final LineString line = (LineString)geometry.getGeometry(i);
      out.writeLEInt(partIndex);
      partIndex += line.getVertexCount();
    }
  }

  public void writePolylineZ(final EndianOutput out, final Geometry geometry) {
    int shapeType;
    if (this.shpFile) {
      shapeType = ShapefileConstants.POLYLINE_ZM_SHAPE;
    } else {
      shapeType = ShapefileConstants.POLYLINE_Z_SHAPE;
    }
    writePolyline(out, geometry, shapeType, 12);
    writeZCoordinates(out, geometry);
  }

  public void writePolylineZM(final EndianOutput out, final Geometry geometry) {
    writePolyline(out, geometry, ShapefileConstants.POLYLINE_ZM_SHAPE, 16);
    writeZCoordinates(out, geometry);
    writeMCoordinates(out, geometry);
  }

  public void writeXy(final EndianOutput out, final double value, final char axisName) {
    if (Double.isNaN(value)) {
      throw new IllegalArgumentException(axisName + " coordinate value cannot be NaN");
    } else if (Double.isInfinite(value)) {
      throw new IllegalArgumentException(axisName + " coordinate cannot be infinite");
    } else {
      out.writeLEDouble(value);
    }
  }

  private void writeXy(final EndianOutput out, final LineString coordinates, final int index) {
    writeXy(out, coordinates.getX(index), 'X');
    writeXy(out, coordinates.getY(index), 'Y');
  }

  private void writeXy(final EndianOutput out, final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    writeXy(out, x, 'X');
    writeXy(out, y, 'Y');
  }

  public void writeXYCoordinates(final EndianOutput out, final Geometry geometry) {
    for (final Vertex vertex : geometry.vertices()) {
      writeXy(out, vertex);
    }
  }

  public void writeXYCoordinates(final EndianOutput out, final LineString coordinates) {
    for (int i = 0; i < coordinates.getVertexCount(); i++) {
      writeXy(out, coordinates, i);
    }
  }

  public void writeZCoordinates(final EndianOutput out, final Geometry geometry) {
    writeZCoordinatesRange(out, geometry);
    if (geometry.getAxisCount() >= 3) {
      for (final Vertex vertex : geometry.vertices()) {
        final double z = vertex.getZ();
        if (Double.isFinite(z)) {
          out.writeLEDouble(z);
        } else {
          out.writeLEDouble(0);
        }
      }
    } else {
      for (int i = 0; i < geometry.getVertexCount(); i++) {
        out.writeLEDouble(0);
      }
    }
  }

  public void writeZCoordinates(final EndianOutput out, final LineString coordinates) {
    if (coordinates.getAxisCount() >= 3) {
      for (int i = 0; i < coordinates.getVertexCount(); i++) {
        final double z = coordinates.getZ(i);
        if (Double.isFinite(z)) {
          out.writeLEDouble(z);
        } else {
          out.writeLEDouble(0);
        }
      }
    } else {
      for (int i = 0; i < coordinates.getVertexCount(); i++) {
        out.writeLEDouble(0);
      }
    }
  }

  public void writeZCoordinates(final EndianOutput out, final List<LineString> coordinatesList) {
    writeZCoordinatesRange(out, coordinatesList);
    for (final LineString coordinates : coordinatesList) {
      writeZCoordinates(out, coordinates);
    }
  }

  public void writeZCoordinatesRange(final EndianOutput out, final Geometry geometry) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    final double min = boundingBox.getMin(2);
    final double max = boundingBox.getMax(2);
    if (Double.isFinite(min) && Double.isFinite(max)) {
      out.writeLEDouble(min);
      out.writeLEDouble(max);
    } else {
      out.writeLEDouble(0);
      out.writeLEDouble(0);
    }
  }

  public void writeZCoordinatesRange(final EndianOutput out,
    final List<LineString> coordinatesList) {
    double minZ = Double.MAX_VALUE;
    double maxZ = -Double.MAX_VALUE;
    for (final LineString ring : coordinatesList) {
      for (int i = 0; i < ring.getVertexCount(); i++) {
        double z = ring.getCoordinate(i, 2);
        if (!Double.isFinite(z)) {
          z = 0;
        }
        minZ = Math.min(z, minZ);
        maxZ = Math.max(z, maxZ);
      }
    }
    if (minZ == Double.MAX_VALUE || maxZ == -Double.MAX_VALUE) {
      out.writeLEDouble(0);
      out.writeLEDouble(0);
    } else {
      out.writeLEDouble(minZ);
      out.writeLEDouble(maxZ);
    }
  }

}
