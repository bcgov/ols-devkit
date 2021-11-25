package com.revolsys.elevation.tin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.collection.list.Lists;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.IntArrayScaleGriddedElevationModel;
import com.revolsys.elevation.tin.compactbinary.ScaledIntegerTriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.tin.AsciiTin;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Triangle;
import com.revolsys.geometry.model.impl.PointDoubleXYZ;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDoubleGF;
import com.revolsys.io.IoFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.predicate.Predicates;
import com.revolsys.spring.resource.Resource;

public interface TriangulatedIrregularNetwork extends GeometryFactoryProxy {

  static final int[] OPPOSITE_INDEXES = {
    2, 1, 0
  };

  static final String GEOMETRY_FACTORY = "geometryFactory";

  static boolean forEachTriangle(final Object source, final MapEx properties,
    final TriangleConsumer action) {
    final TriangulatedIrregularNetworkReaderFactory factory = IoFactory
      .factory(TriangulatedIrregularNetworkReaderFactory.class, source);
    if (factory == null) {
      return false;
    } else {
      final Resource resource = factory.getZipResource(source);
      factory.forEachTriangle(resource, properties, action);
      return true;
    }
  }

  static boolean forEachTriangle(final Object source, final TriangleConsumer action) {
    return forEachTriangle(source, MapEx.EMPTY, action);
  }

  /**
   * Get the index of the corner or a triangle opposite corners i1 -> i2. i1 and
   * i2 must have different values in the range 0..2.
   *
   * @param i1
   * @param i2
   * @return
   */
  public static int getOtherIndex(final int i1, final int i2) {
    return OPPOSITE_INDEXES[i1 + i2 - 1];
  }

  static TriangulatedIrregularNetwork newTriangulatedIrregularNetwork(final Object source) {
    return newTriangulatedIrregularNetwork(source, MapEx.EMPTY);
  }

  static TriangulatedIrregularNetwork newTriangulatedIrregularNetwork(final Object source,
    final MapEx properties) {
    final TriangulatedIrregularNetworkReaderFactory factory = IoFactory
      .factory(TriangulatedIrregularNetworkReaderFactory.class, source);
    if (factory == null) {
      return null;
    } else {
      final Resource resource = factory.getZipResource(source);
      final TriangulatedIrregularNetwork tin = factory.newTriangulatedIrregularNetwork(resource,
        properties);
      return tin;
    }
  }

  public static void serviceInit() {
    IoFactoryRegistry.addFactory(new ScaledIntegerTriangulatedIrregularNetwork());
    IoFactoryRegistry.addFactory(new AsciiTin());
  }

  default void cancelChanges() {
  }

  default void forEachTriangle(final BoundingBox boundingBox,
    final Consumer<? super Triangle> action) {
    forEachTriangle(triangle -> {
      if (triangle.intersectsBbox(boundingBox)) {
        action.accept(triangle);
      }
    });
  }

  default void forEachTriangle(final BoundingBox boundingBox,
    final Predicate<? super Triangle> filter, final Consumer<? super Triangle> action) {
    final Consumer<? super Triangle> filteredAction = Predicates.newConsumer(filter, action);
    forEachTriangle(boundingBox, filteredAction);
  }

  void forEachTriangle(final Consumer<? super Triangle> action);

  default void forEachTriangle(final double x, final double y,
    final Consumer<? super Triangle> action) {
    forEachTriangle(triangle -> {
      if (triangle.intersects(x, y)) {
        action.accept(triangle);
      }
    });
  }

  default void forEachTriangle(final double x, final double y,
    final Predicate<? super Triangle> filter, final Consumer<? super Triangle> action) {
    final Consumer<? super Triangle> filteredAction = Predicates.newConsumer(filter, action);
    forEachTriangle(x, y, filteredAction);
  }

  default void forEachTriangle(final Predicate<? super Triangle> filter,
    final Consumer<? super Triangle> action) {
    final Consumer<? super Triangle> filteredAction = Predicates.newConsumer(filter, action);
    forEachTriangle(filteredAction);
  }

  default void forEachTriangle(final TriangleConsumer action) {
    forEachTriangle(action::acceptTriangle);
  }

  void forEachVertex(Consumer<Point> action);

  BoundingBox getBoundingBox();

  default double getElevation(final double x, final double y) {
    final List<Triangle> triangles = getTriangles(x, y);
    for (final Triangle triangle : triangles) {
      return triangle.getElevation(x, y);
    }
    return Double.NaN;
  }

  default LineString getElevation(final LineString line) {
    final GeometryFactory geometryFactory = line.getGeometryFactory();
    final int vertexCount = line.getVertexCount();
    final int axisCount = line.getAxisCount();
    final double[] newCoordinates = new double[vertexCount * axisCount];

    boolean modified = false;
    int i = 0;
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        double value = line.getCoordinate(vertexIndex, axisIndex);
        if (axisIndex == 2) {
          final double newZ = getElevation(line.getPoint(vertexIndex));
          if (!Double.isNaN(newZ)) {
            if (value != newZ) {
              value = newZ;
              modified = true;
            }
          }
        }
        newCoordinates[i] = value;
        i++;
      }
    }
    if (modified) {
      return geometryFactory.lineString(axisCount, newCoordinates);
    } else {
      return line;
    }
  }

  default double getElevation(Point point) {
    point = convertGeometry(point);
    final List<Triangle> triangles = getTriangles(point);
    for (final Triangle triangle : triangles) {
      final Point t0 = triangle.getP0();
      if (t0.equals(point)) {
        return t0.getZ();
      }
      final Point t1 = triangle.getP1();
      if (t1.equals(point)) {
        return t1.getZ();
      }
      final Point t2 = triangle.getP2();
      if (t2.equals(point)) {
        return t2.getZ();
      }
      Point closestCorner = t0;
      LineSegment oppositeEdge = new LineSegmentDoubleGF(t1, t2);
      double closestDistance = point.distancePoint(closestCorner);
      final double t1Distance = point.distancePoint(t1);
      if (closestDistance > t1Distance) {
        closestCorner = t1;
        oppositeEdge = new LineSegmentDoubleGF(t2, t0);
        closestDistance = t1Distance;
      }
      if (closestDistance > point.distancePoint(t2)) {
        closestCorner = t2;
        oppositeEdge = new LineSegmentDoubleGF(t0, t1);
      }
      LineSegment segment = new LineSegmentDoubleGF(closestCorner, point).extend(0,
        t0.distancePoint(t1) + t1.distancePoint(t2) + t0.distancePoint(t2));
      final Geometry intersectCoordinates = oppositeEdge.getIntersection(segment);
      if (intersectCoordinates.getVertexCount() > 0) {
        final Point intersectPoint = intersectCoordinates.getVertex(0);
        final double z = oppositeEdge.getElevation(intersectPoint);
        if (!Double.isNaN(z)) {
          final double x = intersectPoint.getX();
          final double y = intersectPoint.getY();
          final Point end = new PointDoubleXYZ(x, y, z);
          segment = new LineSegmentDoubleGF(t0, end);
          return segment.getElevation(point);
        }
      }
    }
    return Double.NaN;
  }

  default Resource getResource() {
    return null;
  }

  int getTriangleCount();

  default List<Triangle> getTriangles() {
    final Consumer<Consumer<Triangle>> action = this::forEachTriangle;
    return Lists.newArray(action);
  }

  default List<Triangle> getTriangles(BoundingBox boundingBox) {
    boundingBox = boundingBox.bboxEdit(editor -> editor.setGeometryFactory(getGeometryFactory()));
    final List<Triangle> triangles = new ArrayList<>();
    forEachTriangle(boundingBox, triangles::add);
    return triangles;
  }

  default List<Triangle> getTriangles(final double x, final double y) {
    final List<Triangle> triangles = new ArrayList<>();
    final Predicate<Triangle> filter = triangle -> triangle.containsPoint(x, y);
    forEachTriangle(x, y, filter, triangles::add);
    return triangles;
  }

  default List<Triangle> getTriangles(final LineSegment segment) {
    final BoundingBox boundingBox = segment.getBoundingBox();
    final List<Triangle> triangles = new ArrayList<>();
    forEachTriangle(boundingBox, triangles::add);
    return triangles;
  }

  default List<Triangle> getTriangles(final Point point) {
    final List<Triangle> triangles = new ArrayList<>();
    final Predicate<Triangle> filter = (triangle) -> {
      return triangle.containsPoint(point);
    };
    final BoundingBox boundingBox = point.getBoundingBox();
    forEachTriangle(boundingBox, filter, triangles::add);
    return triangles;
  }

  int getVertexCount();

  default List<Point> getVertices() {
    return Lists.newArray(this::forEachVertex);
  }

  default GriddedElevationModel newGriddedElevationModel(final double minX, final double minY,
    final double maxX, final double maxY, final int gridCellSize, final double scaleFactor) {
    final int minXInt = (int)Math.floor(minX / gridCellSize) * gridCellSize;
    final int minYInt = (int)Math.floor(minY / gridCellSize) * gridCellSize;
    final int maxXInt = (int)Math.ceil(maxX / gridCellSize) * gridCellSize;
    final int maxYInt = (int)Math.ceil(maxY / gridCellSize) * gridCellSize;

    final int width = maxXInt - minXInt;
    final int height = maxYInt - minYInt;

    final int gridWidth = width / gridCellSize;
    final int gridHeight = height / gridCellSize;

    final GeometryFactory geometryFactory = getGeometryFactory()//
      .convertAxisCountAndScales(3, scaleFactor, scaleFactor, scaleFactor);
    final IntArrayScaleGriddedElevationModel elevationModel = new IntArrayScaleGriddedElevationModel(
      geometryFactory, minXInt, minYInt, gridWidth, gridHeight, gridCellSize);

    forEachTriangle(elevationModel::setElevationsForTriangle);
    return elevationModel;
  }

  /**
   * Create a new {@link GriddedElevationModel} using this TIN's bounding box with the specified
   * grid cell size and a 1mm precision model.
   *
   * @param gridCellSize
   * @return
   */
  default GriddedElevationModel newGriddedElevationModel(final int gridCellSize) {
    return newGriddedElevationModel(gridCellSize, 1000.0);
  }

  default GriddedElevationModel newGriddedElevationModel(final int gridCellSize,
    final double scaleFactor) {
    final BoundingBox boundingBox = getBoundingBox();
    final double minX = boundingBox.getMinX();
    final double minY = boundingBox.getMinY();
    final double maxX = boundingBox.getMaxX();
    final double maxY = boundingBox.getMaxY();
    return newGriddedElevationModel(minX, minY, maxX, maxY, gridCellSize, scaleFactor);
  }

  default boolean writeTriangulatedIrregularNetwork() {
    return writeTriangulatedIrregularNetwork(MapEx.EMPTY);
  }

  default boolean writeTriangulatedIrregularNetwork(final MapEx properties) {
    final Resource resource = getResource();
    if (resource == null) {
      return false;
    } else {
      writeTriangulatedIrregularNetwork(resource, properties);
      return true;
    }
  }

  default void writeTriangulatedIrregularNetwork(final Object target) {
    writeTriangulatedIrregularNetwork(target, MapEx.EMPTY);
  }

  default void writeTriangulatedIrregularNetwork(final Object target, final MapEx properties) {
    try (
      TriangulatedIrregularNetworkWriter writer = TriangulatedIrregularNetworkWriter
        .newTriangulatedIrregularNetworkWriter(target, properties)) {
      if (writer == null) {
        throw new IllegalArgumentException(
          "No triangulated irregular network writer exists for " + target);
      }
      writer.write(this);
    }
  }
}
