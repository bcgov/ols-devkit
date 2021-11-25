package com.revolsys.record.io.format.shp;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.number.Doubles;

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
import com.revolsys.io.endian.EndianInput;
import com.revolsys.io.endian.EndianOutput;
import com.revolsys.util.JavaBeanUtil;

public final class ShapefileGeometryUtil {
  public static final Map<String, Method> GEOMETRY_TYPE_READ_METHOD_MAP = new LinkedHashMap<>();

  public static final Map<String, Method> GEOMETRY_TYPE_WRITE_METHOD_MAP = new LinkedHashMap<>();

  public static final ShapefileGeometryUtil SHP_INSTANCE = new ShapefileGeometryUtil(true);

  static {
    addReadWriteMethods("Point");
    addReadWriteMethods("Polygon");
    addReadWriteMethods("Polyline");
    addReadWriteMethods("Multipoint");

    for (final boolean z : Arrays.asList(false, true)) {
      for (final boolean m : Arrays.asList(false, true)) {
        final String hasZ = String.valueOf(z).toUpperCase();
        final String hasM = String.valueOf(m).toUpperCase();
        GEOMETRY_TYPE_READ_METHOD_MAP.put("LINESTRING" + hasZ + hasM,
          GEOMETRY_TYPE_READ_METHOD_MAP.get("POLYLINE" + hasZ + hasM));
        GEOMETRY_TYPE_WRITE_METHOD_MAP.put("LINESTRING" + hasZ + hasM,
          GEOMETRY_TYPE_WRITE_METHOD_MAP.get("POLYLINE" + hasZ + hasM));
        GEOMETRY_TYPE_READ_METHOD_MAP.put("MULTILINESTRING" + hasZ + hasM,
          GEOMETRY_TYPE_READ_METHOD_MAP.get("POLYLINE" + hasZ + hasM));
        GEOMETRY_TYPE_WRITE_METHOD_MAP.put("MULTILINESTRING" + hasZ + hasM,
          GEOMETRY_TYPE_WRITE_METHOD_MAP.get("POLYLINE" + hasZ + hasM));
        GEOMETRY_TYPE_READ_METHOD_MAP.put("MULTIPOLYGON" + hasZ + hasM,
          GEOMETRY_TYPE_READ_METHOD_MAP.get("POLYGON" + hasZ + hasM));
        GEOMETRY_TYPE_WRITE_METHOD_MAP.put("MULTIPOLYGON" + hasZ + hasM,
          GEOMETRY_TYPE_WRITE_METHOD_MAP.get("POLYGON" + hasZ + hasM));
      }
    }

  }

  private static void addMethod(final String action, final Map<String, Method> methodMap,
    final String geometryType, final boolean hasZ, final boolean hasM,
    final Class<?>... parameterTypes) {
    final String geometryTypeKey = (geometryType + hasZ + hasM).toUpperCase();
    String methodName = action + geometryType;
    if (hasZ) {
      methodName += "Z";
    }
    if (hasM) {
      methodName += "M";
    }
    final Method method = JavaBeanUtil.getMethod(ShapefileGeometryUtil.class, methodName,
      parameterTypes);
    methodMap.put(geometryTypeKey, method);
  }

  private static void addReadWriteMethods(final String geometryType) {
    addMethod("read", GEOMETRY_TYPE_READ_METHOD_MAP, geometryType, false, false,
      GeometryFactory.class, EndianInput.class, Integer.TYPE);
    addMethod("read", GEOMETRY_TYPE_READ_METHOD_MAP, geometryType, true, false,
      GeometryFactory.class, EndianInput.class, Integer.TYPE);
    addMethod("read", GEOMETRY_TYPE_READ_METHOD_MAP, geometryType, false, true,
      GeometryFactory.class, EndianInput.class, Integer.TYPE);
    addMethod("read", GEOMETRY_TYPE_READ_METHOD_MAP, geometryType, true, true,
      GeometryFactory.class, EndianInput.class, Integer.TYPE);

    addMethod("write", GEOMETRY_TYPE_WRITE_METHOD_MAP, geometryType, false, false,
      EndianOutput.class, Geometry.class);
    addMethod("write", GEOMETRY_TYPE_WRITE_METHOD_MAP, geometryType, true, false,
      EndianOutput.class, Geometry.class);
    addMethod("write", GEOMETRY_TYPE_WRITE_METHOD_MAP, geometryType, false, true,
      EndianOutput.class, Geometry.class);
    addMethod("write", GEOMETRY_TYPE_WRITE_METHOD_MAP, geometryType, true, true, EndianOutput.class,
      Geometry.class);
  }

  public static Method getReadMethod(String geometryTypeKey) {
    geometryTypeKey = geometryTypeKey.toUpperCase();
    final Method method = GEOMETRY_TYPE_READ_METHOD_MAP.get(geometryTypeKey);
    if (method == null) {
      throw new IllegalArgumentException("Cannot get Shape Reader for: " + geometryTypeKey);
    }
    return method;
  }

  public static Method getWriteMethod(final GeometryFactory geometryFactory,
    final DataType dataType) {
    final int axisCount = geometryFactory.getAxisCount();
    final boolean hasZ = axisCount > 2;
    final boolean hasM = axisCount > 3;
    final String geometryType = dataType.toString();
    final String geometryTypeKey = geometryType.toUpperCase() + hasZ + hasM;
    return getWriteMethod(geometryTypeKey);
  }

  public static Method getWriteMethod(String geometryTypeKey) {
    geometryTypeKey = geometryTypeKey.toUpperCase();
    final Method method = GEOMETRY_TYPE_WRITE_METHOD_MAP.get(geometryTypeKey);
    if (method == null) {
      throw new IllegalArgumentException("Cannot get Shape Writer for: " + geometryTypeKey);
    }
    return method;
  }

  private final boolean shpFile;

  private final boolean writeLength;

  public ShapefileGeometryUtil(final boolean shpFile) {
    this.shpFile = shpFile;
    this.writeLength = shpFile;
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

  @SuppressWarnings("unchecked")
  public <V extends Geometry> V read(final Method method, final GeometryFactory geometryFactory,
    final EndianInput in, final int recordLength) {
    return (V)JavaBeanUtil.method(method, this, geometryFactory, in, recordLength);
  }

  public void readCoordinates(final EndianInput in, final int vertexCount, final int axisCount,
    final double[] coordinates, final int axisIndex) throws IOException {
    for (int j = 0; j < vertexCount; j++) {
      double value = in.readLEDouble();
      if (value == -Double.MAX_VALUE) {
        value = Double.NaN;
      }
      coordinates[j * axisCount + axisIndex] = value;
    }
  }

  public void readCoordinates(final EndianInput in, final int[] partIndex,
    final List<double[]> coordinateLists, final int axisIndex, final int axisCount)
    throws IOException {
    in.skipBytes(2 * Doubles.BYTES_IN_DOUBLE);
    for (int i = 0; i < partIndex.length; i++) {
      final double[] coordinates = coordinateLists.get(i);
      final int vertexCount = coordinates.length / axisCount;
      readCoordinates(in, vertexCount, axisCount, coordinates, axisIndex);
    }
  }

  public int[] readIntArray(final EndianInput in, final int count) throws IOException {
    final int[] values = new int[count];
    for (int i = 0; i < count; i++) {
      final int value = in.readLEInt();
      values[i] = value;
    }
    return values;
  }

  public Punctual readMultipoint(final GeometryFactory geometryFactory, final EndianInput in,
    final int recordLength) throws IOException {
    in.skipBytes(4 * Doubles.BYTES_IN_DOUBLE);
    final int vertexCount = in.readLEInt();
    final double[] coordinates = readXYCoordinates(in, vertexCount, 2);
    return geometryFactory.punctual(new LineStringDouble(2, coordinates));
  }

  public Punctual readMultipointM(final GeometryFactory geometryFactory, final EndianInput in,
    final int recordLength) throws IOException {
    in.skipBytes(4 * Doubles.BYTES_IN_DOUBLE);
    final int vertexCount = in.readLEInt();
    final int axisCount = 4;
    final double[] coordinates = readXYCoordinates(in, vertexCount, axisCount);
    in.skipBytes(2 * Doubles.BYTES_IN_DOUBLE);
    setCoordinatesNaN(coordinates, vertexCount, axisCount, 2);
    readCoordinates(in, vertexCount, axisCount, coordinates, 3);
    return geometryFactory.punctual(new LineStringDouble(axisCount, coordinates));
  }

  public Punctual readMultipointZ(final GeometryFactory geometryFactory, final EndianInput in,
    final int recordLength) throws IOException {
    in.skipBytes(4 * Doubles.BYTES_IN_DOUBLE);
    final int vertexCount = in.readLEInt();
    final double[] coordinates = readXYCoordinates(in, vertexCount, 3);
    in.skipBytes(2 * Doubles.BYTES_IN_DOUBLE);
    readCoordinates(in, vertexCount, 3, coordinates, 2);
    return geometryFactory.punctual(new LineStringDouble(3, coordinates));
  }

  public Punctual readMultipointZM(GeometryFactory geometryFactory, final EndianInput in,
    final int recordLength) throws IOException {
    in.skipBytes(4 * Doubles.BYTES_IN_DOUBLE);
    final int vertexCount = in.readLEInt();
    int axisCount;
    if (40 + 24 * vertexCount == recordLength * 2) {
      geometryFactory = geometryFactory.convertAxisCount(3);
      axisCount = 3;
    } else {
      axisCount = 4;
    }
    final double[] coordinates = readXYCoordinates(in, vertexCount, axisCount);
    in.skipBytes(2 * Doubles.BYTES_IN_DOUBLE);
    readCoordinates(in, vertexCount, axisCount, coordinates, 2);
    if (axisCount == 4) {
      in.skipBytes(2 * Doubles.BYTES_IN_DOUBLE);
      readCoordinates(in, vertexCount, axisCount, coordinates, 3);
    }
    return geometryFactory.punctual(axisCount, coordinates);
  }

  public int[] readPartIndex(final EndianInput in, final int numParts, final int vertexCount)
    throws IOException {
    final int[] partIndex = new int[numParts];
    if (numParts > 0) {
      int startIndex = in.readLEInt();
      for (int i = 1; i < partIndex.length; i++) {
        final int index = in.readLEInt();
        partIndex[i - 1] = index - startIndex;
        startIndex = index;
      }
      partIndex[partIndex.length - 1] = vertexCount - startIndex;
    }
    return partIndex;
  }

  public Point readPoint(final GeometryFactory geometryFactory, final EndianInput in,
    final int recordLength) throws IOException {
    final double[] coordinates = readXYCoordinates(in, 1, 2);
    return geometryFactory.point(coordinates);
  }

  public Point readPointM(final GeometryFactory geometryFactory, final EndianInput in,
    final int recordLength) throws IOException {
    final double x = in.readLEDouble();
    final double y = in.readLEDouble();
    final double z = 0;
    final double m = in.readLEDouble();
    return geometryFactory.point(x, y, z, m);
  }

  public void readPoints(final EndianInput in, final int[] partIndex,
    final List<double[]> coordinateLists, final int axisCount) throws IOException {
    for (int i = 0; i < partIndex.length; i++) {
      final int count = partIndex[i];
      final double[] coordinates = coordinateLists.get(i);
      readXYCoordinates(in, axisCount, count, coordinates);
    }
  }

  public Point readPointZ(final GeometryFactory geometryFactory, final EndianInput in,
    final int recordLength) throws IOException {
    final double x = in.readLEDouble();
    final double y = in.readLEDouble();
    final double z = in.readLEDouble();
    return geometryFactory.point(x, y, z);
  }

  public Point readPointZM(final GeometryFactory geometryFactory, final EndianInput in,
    final int recordLength) throws IOException {
    final double x = in.readLEDouble();
    final double y = in.readLEDouble();
    final double z = in.readLEDouble();
    if (recordLength * 2 == 28) {
      return geometryFactory.convertAxisCount(3).point(x, y, z);
    } else {
      final double m = in.readLEDouble();
      return geometryFactory.point(x, y, z, m);
    }
  }

  public Geometry readPolygon(final GeometryFactory geometryFactory, final EndianInput in,
    final int recordLength) throws IOException {
    in.skipBytes(4 * Doubles.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int vertexCount = in.readLEInt();
    final int[] partIndex = readPartIndex(in, numParts, vertexCount);

    final List<double[]> parts = newCoordinatesLists(partIndex, 2);

    readPoints(in, partIndex, parts, 2);

    return newPolygonGeometryFromParts(geometryFactory, parts, 2);

  }

  public Geometry readPolygonM(final GeometryFactory geometryFactory, final EndianInput in,
    final int recordLength) throws IOException {
    in.skipBytes(4 * Doubles.BYTES_IN_DOUBLE);
    final int partCount = in.readLEInt();
    final int vertexCount = in.readLEInt();
    final int axisCount = 4;
    final int[] partIndex = readPartIndex(in, partCount, vertexCount);

    final List<double[]> parts = newCoordinatesLists(partIndex, axisCount);
    readPoints(in, partIndex, parts, axisCount);
    readCoordinates(in, partIndex, parts, 3, axisCount);
    return newPolygonGeometryFromParts(geometryFactory, parts, axisCount);

  }

  public Geometry readPolygonZ(final GeometryFactory geometryFactory, final EndianInput in,
    final int recordLength) throws IOException {
    in.skipBytes(4 * Doubles.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int vertexCount = in.readLEInt();
    final int axisCount = 3;
    final int[] partIndex = readPartIndex(in, numParts, vertexCount);

    final List<double[]> parts = newCoordinatesLists(partIndex, axisCount);
    readPoints(in, partIndex, parts, axisCount);
    readCoordinates(in, partIndex, parts, 2, axisCount);
    return newPolygonGeometryFromParts(geometryFactory, parts, axisCount);
  }

  public Geometry readPolygonZM(GeometryFactory geometryFactory, final EndianInput in,
    final int recordLength) throws IOException {
    in.skipBytes(4 * Doubles.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int vertexCount = in.readLEInt();
    final int[] partIndex = readPartIndex(in, numParts, vertexCount);
    final int axisCount;
    if (44 + 16 + 4 * numParts + 24 * vertexCount == recordLength * 2) {
      axisCount = 3;
      geometryFactory = geometryFactory.convertAxisCount(3);
    } else {
      axisCount = 4;
    }
    final List<double[]> parts = newCoordinatesLists(partIndex, axisCount);
    readPoints(in, partIndex, parts, axisCount);
    readCoordinates(in, partIndex, parts, 2, axisCount);
    if (axisCount == 4) {
      readCoordinates(in, partIndex, parts, 3, axisCount);
    }
    return newPolygonGeometryFromParts(geometryFactory, parts, axisCount);
  }

  public Geometry readPolyline(final GeometryFactory geometryFactory, final EndianInput in,
    final int recordLength) throws IOException {
    in.skipBytes(4 * Doubles.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int vertexCount = in.readLEInt();
    final int axisCount = 2;
    if (numParts == 1) {
      in.readLEInt();
      final double[] coordinates = readXYCoordinates(in, vertexCount, axisCount);

      return geometryFactory.lineString(2, coordinates);
    } else {
      final int[] partIndex = new int[numParts + 1];
      partIndex[numParts] = vertexCount;
      for (int i = 0; i < partIndex.length - 1; i++) {
        partIndex[i] = in.readLEInt();

      }
      final List<LineString> lines = new ArrayList<>();
      for (int i = 0; i < partIndex.length - 1; i++) {
        final int startIndex = partIndex[i];
        final int endIndex = partIndex[i + 1];
        final int numCoords = endIndex - startIndex;
        final double[] coordinates = readXYCoordinates(in, numCoords, axisCount);
        lines.add(geometryFactory.lineString(2, coordinates));
      }
      return geometryFactory.lineal(lines);
    }
  }

  public Geometry readPolylineM(final GeometryFactory geometryFactory, final EndianInput in,
    final int recordLength) throws IOException {
    in.skipBytes(4 * Doubles.BYTES_IN_DOUBLE);
    final int partCount = in.readLEInt();
    final int allVertexCount = in.readLEInt();
    final int axisCount = 4;
    if (partCount == 1) {
      in.readLEInt();
      final double[] coordinates = readXYCoordinates(in, allVertexCount, axisCount);
      in.skipBytes(2 * Doubles.BYTES_IN_DOUBLE);
      setCoordinatesNaN(coordinates, allVertexCount, axisCount, 2);
      readCoordinates(in, allVertexCount, axisCount, coordinates, 3);
      return geometryFactory.lineString(axisCount, coordinates);
    } else {
      final int[] partIndex = new int[partCount + 1];
      partIndex[partCount] = allVertexCount;
      for (int i = 0; i < partIndex.length - 1; i++) {
        partIndex[i] = in.readLEInt();
      }
      final List<double[]> coordinatesList = new ArrayList<>();
      for (int i = 0; i < partIndex.length - 1; i++) {
        final int startIndex = partIndex[i];
        final int endIndex = partIndex[i + 1];
        final int vertexCount = endIndex - startIndex;
        final double[] coordinates = readXYCoordinates(in, vertexCount, axisCount);
        coordinatesList.add(coordinates);
      }
      in.skipBytes(2 * Doubles.BYTES_IN_DOUBLE);
      for (int i = 0; i < partIndex.length - 1; i++) {
        final double[] coordinates = coordinatesList.get(i);
        final int vertexCount = coordinates.length / axisCount;
        readCoordinates(in, vertexCount, axisCount, coordinates, 3);
      }

      return geometryFactory.lineal(axisCount,
        coordinatesList.toArray(new double[coordinatesList.size()][]));
    }
  }

  public Geometry readPolylineZ(final GeometryFactory geometryFactory, final EndianInput in,
    final int recordLength) throws IOException {
    in.skipBytes(4 * Doubles.BYTES_IN_DOUBLE);
    final int numParts = in.readLEInt();
    final int vertexCount = in.readLEInt();
    final int axisCount = 3;
    return readPolylineZ(geometryFactory, in, numParts, vertexCount, axisCount);
  }

  public Geometry readPolylineZ(final GeometryFactory geometryFactory, final EndianInput in,
    final int partCount, final int allVertexCount, final int axisCount) throws IOException {
    if (partCount == 1) {
      in.readLEInt();
      final double[] coordinates = readXYCoordinates(in, allVertexCount, axisCount);
      in.skipBytes(2 * Doubles.BYTES_IN_DOUBLE);
      readCoordinates(in, allVertexCount, axisCount, coordinates, 2);
      return geometryFactory.lineString(axisCount, coordinates);
    } else {
      final int[] partIndex = new int[partCount + 1];
      partIndex[partCount] = allVertexCount;
      for (int i = 0; i < partCount; i++) {
        partIndex[i] = in.readLEInt();
      }
      final double[][] linesCoordinates = new double[partCount][];
      for (int i = 0; i < partCount; i++) {
        final int startIndex = partIndex[i];
        final int endIndex = partIndex[i + 1];
        final int vertexCount = endIndex - startIndex;
        linesCoordinates[i] = readXYCoordinates(in, vertexCount, axisCount);
      }
      in.skipBytes(2 * Doubles.BYTES_IN_DOUBLE);
      for (int i = 0; i < partCount; i++) {
        final double[] coordinates = linesCoordinates[i];
        final int vertexCount = coordinates.length / axisCount;
        readCoordinates(in, vertexCount, axisCount, coordinates, 2);
      }
      return geometryFactory.lineal(axisCount, linesCoordinates);
    }
  }

  public Geometry readPolylineZM(GeometryFactory geometryFactory, final EndianInput in,
    final int recordLength) throws IOException {
    in.skipBytes(4 * Doubles.BYTES_IN_DOUBLE);
    final int geometryCount = in.readLEInt();
    final int vertexCount = in.readLEInt();
    if (44 + 16 + 4 * geometryCount + 24 * vertexCount == recordLength * 2) {
      geometryFactory = geometryFactory.convertAxisCount(3);
      return readPolylineZ(geometryFactory, in, geometryCount, vertexCount, 3);
    } else {
      final int axisCount = 4;
      if (geometryCount == 1) {
        in.readLEInt();
        final double[] coordinates = readXYCoordinates(in, vertexCount, axisCount);
        in.skipBytes(2 * Doubles.BYTES_IN_DOUBLE);
        readCoordinates(in, vertexCount, axisCount, coordinates, 2);
        in.skipBytes(2 * Doubles.BYTES_IN_DOUBLE);
        readCoordinates(in, vertexCount, axisCount, coordinates, 3);
        return geometryFactory.lineString(axisCount, coordinates);
      } else {
        final int[] partIndex = new int[geometryCount + 1];
        partIndex[geometryCount] = vertexCount;
        for (int i = 0; i < partIndex.length - 1; i++) {
          partIndex[i] = in.readLEInt();
        }
        final List<double[]> coordinatesList = new ArrayList<>();
        for (int i = 0; i < partIndex.length - 1; i++) {
          final int startIndex = partIndex[i];
          final int endIndex = partIndex[i + 1];
          final int numCoords = endIndex - startIndex;
          final double[] coordinates = readXYCoordinates(in, numCoords, axisCount);
          coordinatesList.add(coordinates);
        }
        in.skipBytes(2 * Doubles.BYTES_IN_DOUBLE);
        for (int i = 0; i < partIndex.length - 1; i++) {
          final double[] coordinates = coordinatesList.get(i);
          readCoordinates(in, coordinates.length / 4, axisCount, coordinates, 2);
        }
        in.skipBytes(2 * Doubles.BYTES_IN_DOUBLE);
        for (int i = 0; i < partIndex.length - 1; i++) {
          final double[] coordinates = coordinatesList.get(i);
          readCoordinates(in, coordinates.length / 4, axisCount, coordinates, 3);
        }
        final List<LineString> lines = new ArrayList<>();
        for (final double[] coordinates : coordinatesList) {
          lines.add(geometryFactory.lineString(axisCount, coordinates));
        }
        return geometryFactory.lineal(lines);
      }
    }
  }

  public double[] readXYCoordinates(final EndianInput in, final int vertexCount,
    final int axisCount) throws IOException {
    final double[] coordinates = new double[vertexCount * axisCount];
    readXYCoordinates(in, axisCount, vertexCount, coordinates);
    return coordinates;
  }

  public void readXYCoordinates(final EndianInput in, final int axisCount, final int vertexCount,
    final double[] coordinates) throws IOException {
    for (int j = 0; j < vertexCount; j++) {
      final double x = in.readLEDouble();
      final double y = in.readLEDouble();
      coordinates[j * axisCount] = x;
      coordinates[j * axisCount + 1] = y;
    }
  }

  public void setCoordinatesNaN(final double[] coordinates, final int vertexCount,
    final int axisCount, final int axisIndex) {
    for (int j = 0; j < vertexCount; j++) {
      coordinates[j * axisCount + axisIndex] = Double.NaN;
    }
  }

  public void setCoordinatesNaN(final EndianInput in, final int[] partIndex,
    final List<double[]> coordinateLists, final int axisIndex, final int axisCount)
    throws IOException {
    in.skipBytes(2 * Doubles.BYTES_IN_DOUBLE);
    for (int i = 0; i < partIndex.length; i++) {
      final double[] coordinates = coordinateLists.get(i);
      final int vertexCount = coordinates.length / axisCount;
      setCoordinatesNaN(coordinates, vertexCount, axisCount, axisIndex);
    }
  }

  public void write(final Method method, final EndianOutput out, final Geometry geometry) {
    JavaBeanUtil.method(method, this, out, geometry);
  }

  public void writeEnvelope(final EndianOutput out, final BoundingBox envelope) throws IOException {
    out.writeLEDouble(envelope.getMinX());
    out.writeLEDouble(envelope.getMinY());
    out.writeLEDouble(envelope.getMaxX());
    out.writeLEDouble(envelope.getMaxY());
  }

  public void writeMCoordinates(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writeMCoordinatesRange(out, geometry);
    if (geometry.getAxisCount() >= 4) {
      for (final Vertex vertex : geometry.vertices()) {
        final double m = vertex.getM();
        if (Double.isNaN(m)) {
          out.writeLEDouble(0);
        } else {
          out.writeLEDouble(m);
        }
      }
    } else {
      for (int i = 0; i < geometry.getVertexCount(); i++) {
        out.writeLEDouble(0);
      }
    }
  }

  public void writeMCoordinates(final EndianOutput out, final LineString coordinates)
    throws IOException {
    if (coordinates.getAxisCount() >= 4) {
      for (int i = 0; i < coordinates.getVertexCount(); i++) {
        final double m = coordinates.getM(i);
        if (!Double.isNaN(m)) {
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

  public void writeMCoordinates(final EndianOutput out, final List<LineString> coordinatesList)
    throws IOException {
    writeMCoordinatesRange(out, coordinatesList);
    for (final LineString coordinates : coordinatesList) {
      writeMCoordinates(out, coordinates);
    }
  }

  public void writeMCoordinatesRange(final EndianOutput out, final Geometry geometry)
    throws IOException {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    final double minM = boundingBox.getMin(3);
    final double maxM = boundingBox.getMax(3);
    if (Double.isNaN(minM) || Double.isNaN(maxM)) {
      out.writeLEDouble(0);
      out.writeLEDouble(0);
    } else {
      out.writeLEDouble(minM);
      out.writeLEDouble(maxM);
    }
  }

  public void writeMCoordinatesRange(final EndianOutput out, final List<LineString> coordinatesList)
    throws IOException {
    double minM = Double.MAX_VALUE;
    double maxM = -Double.MAX_VALUE;
    for (final LineString ring : coordinatesList) {
      for (int i = 0; i < ring.getVertexCount(); i++) {
        double m = ring.getCoordinate(i, 2);
        if (Double.isNaN(m)) {
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

  public void writeMultipoint(final EndianOutput out, final Geometry geometry) throws IOException {
    writeMultipoint(out, geometry, ShapefileConstants.MULTI_POINT_SHAPE, 8);
  }

  private void writeMultipoint(final EndianOutput out, final Geometry geometry, final int shapeType,
    final int wordsPerPoint) throws IOException {
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

  public void writeMultipointM(final EndianOutput out, final Geometry geometry) throws IOException {
    writeMultipoint(out, geometry, ShapefileConstants.MULTI_POINT_M_SHAPE, 12);
    writeMCoordinates(out, geometry);
  }

  public void writeMultipointZ(final EndianOutput out, final Geometry geometry) throws IOException {
    int shapeType;
    if (this.shpFile) {
      shapeType = ShapefileConstants.MULTI_POINT_ZM_SHAPE;
    } else {
      shapeType = ShapefileConstants.MULTI_POINT_Z_SHAPE;
    }

    writeMultipoint(out, geometry, shapeType, 12);
    writeZCoordinates(out, geometry);
  }

  public void writeMultipointZM(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writeMultipoint(out, geometry, ShapefileConstants.MULTI_POINT_ZM_SHAPE, 16);
    writeZCoordinates(out, geometry);
    writeMCoordinates(out, geometry);
  }

  public void writePoint(final EndianOutput out, final Geometry geometry) throws IOException {
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

  public void writePointM(final EndianOutput out, final Geometry geometry) throws IOException {
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
      if (Double.isNaN(m)) {
        out.writeLEDouble(0);
      } else {
        out.writeLEDouble(m);
      }
    } else {
      throw new IllegalArgumentException(
        "Expecting " + Point.class + " geometry got " + geometry.getClass());
    }
  }

  public void writePointZ(final EndianOutput out, final Geometry geometry) throws IOException {
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
      if (Double.isNaN(z)) {
        out.writeLEDouble(0);
      } else {
        out.writeLEDouble(z);
      }
    } else {
      throw new IllegalArgumentException(
        "Expecting " + Point.class + " geometry got " + geometry.getClass());
    }
  }

  public void writePointZM(final EndianOutput out, final Geometry geometry) throws IOException {
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
      if (Double.isNaN(z)) {
        out.writeLEDouble(0);
      } else {
        out.writeLEDouble(z);
      }
      final double m = point.getM();
      if (Double.isNaN(m)) {
        out.writeLEDouble(0);
      } else {
        out.writeLEDouble(m);
      }
    } else {
      throw new IllegalArgumentException(
        "Expecting " + Point.class + " geometry got " + geometry.getClass());
    }
  }

  public void writePolygon(final EndianOutput out, final Geometry geometry) throws IOException {
    writePolygon(out, geometry, ShapefileConstants.POLYGON_SHAPE, 0, 8);
  }

  private List<LineString> writePolygon(final EndianOutput out, final Geometry geometry,
    final int shapeType, final int headerOverhead, final int wordsPerPoint) throws IOException {

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

  public void writePolygonM(final EndianOutput out, final Geometry geometry) throws IOException {
    final List<LineString> rings = writePolygon(out, geometry, ShapefileConstants.POLYGON_M_SHAPE,
      8, 12);
    writeMCoordinates(out, rings);
  }

  public void writePolygonZ(final EndianOutput out, final Geometry geometry) throws IOException {
    int shapeType;
    if (this.shpFile) {
      shapeType = ShapefileConstants.POLYGON_ZM_SHAPE;
    } else {
      shapeType = ShapefileConstants.POLYGON_Z_SHAPE;
    }
    final List<LineString> rings = writePolygon(out, geometry, shapeType, 8, 12);
    writeZCoordinates(out, rings);
  }

  public void writePolygonZM(final EndianOutput out, final Geometry geometry) throws IOException {
    final List<LineString> rings = writePolygon(out, geometry, ShapefileConstants.POLYGON_ZM_SHAPE,
      16, 16);
    writeZCoordinates(out, rings);
    writeMCoordinates(out, rings);
  }

  public void writePolyline(final EndianOutput out, final Geometry geometry) throws IOException {
    writePolyline(out, geometry, ShapefileConstants.POLYLINE_SHAPE, 8);
  }

  private void writePolyline(final EndianOutput out, final Geometry geometry, final int shapeType,
    final int wordsPerPoint) throws IOException {
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

  public void writePolylineM(final EndianOutput out, final Geometry geometry) throws IOException {
    writePolyline(out, geometry, ShapefileConstants.POLYLINE_M_SHAPE, 12);
    writeMCoordinates(out, geometry);
  }

  public void writePolylinePartIndexes(final EndianOutput out, final Geometry geometry)
    throws IOException {
    int partIndex = 0;
    for (int i = 0; i < geometry.getGeometryCount(); i++) {
      final LineString line = (LineString)geometry.getGeometry(i);
      out.writeLEInt(partIndex);
      partIndex += line.getVertexCount();
    }
  }

  public void writePolylineZ(final EndianOutput out, final Geometry geometry) throws IOException {
    int shapeType;
    if (this.shpFile) {
      shapeType = ShapefileConstants.POLYLINE_ZM_SHAPE;
    } else {
      shapeType = ShapefileConstants.POLYLINE_Z_SHAPE;
    }
    writePolyline(out, geometry, shapeType, 12);
    writeZCoordinates(out, geometry);
  }

  public void writePolylineZM(final EndianOutput out, final Geometry geometry) throws IOException {
    writePolyline(out, geometry, ShapefileConstants.POLYLINE_ZM_SHAPE, 16);
    writeZCoordinates(out, geometry);
    writeMCoordinates(out, geometry);
  }

  public void writeXy(final EndianOutput out, final double value, final char axisName)
    throws IOException {
    if (Double.isNaN(value)) {
      throw new IllegalArgumentException(axisName + " coordinate value cannot be NaN");
    } else if (Double.isInfinite(value)) {
      throw new IllegalArgumentException(axisName + " coordinate cannot be infinite");
    } else {
      out.writeLEDouble(value);
    }
  }

  private void writeXy(final EndianOutput out, final LineString coordinates, final int index)
    throws IOException {
    writeXy(out, coordinates.getX(index), 'X');
    writeXy(out, coordinates.getY(index), 'Y');
  }

  private void writeXy(final EndianOutput out, final Point point) throws IOException {
    final double x = point.getX();
    final double y = point.getY();
    writeXy(out, x, 'X');
    writeXy(out, y, 'Y');
  }

  public void writeXYCoordinates(final EndianOutput out, final Geometry geometry)
    throws IOException {
    for (final Vertex vertex : geometry.vertices()) {
      writeXy(out, vertex);
    }
  }

  public void writeXYCoordinates(final EndianOutput out, final LineString coordinates)
    throws IOException {
    for (int i = 0; i < coordinates.getVertexCount(); i++) {
      writeXy(out, coordinates, i);
    }
  }

  public void writeZCoordinates(final EndianOutput out, final Geometry geometry)
    throws IOException {
    writeZCoordinatesRange(out, geometry);
    if (geometry.getAxisCount() >= 3) {
      for (final Vertex vertex : geometry.vertices()) {
        final double z = vertex.getZ();
        if (Double.isNaN(z)) {
          out.writeLEDouble(0);
        } else {
          out.writeLEDouble(z);
        }
      }
    } else {
      for (int i = 0; i < geometry.getVertexCount(); i++) {
        out.writeLEDouble(0);
      }
    }
  }

  public void writeZCoordinates(final EndianOutput out, final LineString coordinates)
    throws IOException {
    if (coordinates.getAxisCount() >= 3) {
      for (int i = 0; i < coordinates.getVertexCount(); i++) {
        final double z = coordinates.getZ(i);
        if (Double.isNaN(z)) {
          out.writeLEDouble(0);
        } else {
          out.writeLEDouble(z);
        }
      }
    } else {
      for (int i = 0; i < coordinates.getVertexCount(); i++) {
        out.writeLEDouble(0);
      }
    }
  }

  public void writeZCoordinates(final EndianOutput out, final List<LineString> coordinatesList)
    throws IOException {
    writeZCoordinatesRange(out, coordinatesList);
    for (final LineString coordinates : coordinatesList) {
      writeZCoordinates(out, coordinates);
    }
  }

  public void writeZCoordinatesRange(final EndianOutput out, final Geometry geometry)
    throws IOException {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    final double min = boundingBox.getMin(2);
    final double max = boundingBox.getMax(2);
    if (Double.isNaN(min) || Double.isNaN(max)) {
      out.writeLEDouble(0);
      out.writeLEDouble(0);
    } else {
      out.writeLEDouble(min);
      out.writeLEDouble(max);
    }
  }

  public void writeZCoordinatesRange(final EndianOutput out, final List<LineString> coordinatesList)
    throws IOException {
    double minZ = Double.MAX_VALUE;
    double maxZ = -Double.MAX_VALUE;
    for (final LineString ring : coordinatesList) {
      for (int i = 0; i < ring.getVertexCount(); i++) {
        double z = ring.getCoordinate(i, 2);
        if (Double.isNaN(z)) {
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
