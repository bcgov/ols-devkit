package com.revolsys.geometry.model.coordinates.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeometry.common.number.Doubles;

import com.revolsys.geometry.algorithm.RobustDeterminant;
import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.graph.linestring.LineStringGraph;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.model.impl.LineStringDouble;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.vertex.Vertex;

public class CoordinatesListUtil {
  public static final String COORDINATE_DISTANCE = "coordinateDistance";

  public static final String COORDINATE_INDEX = "coordinateIndex";

  public static final String SEGMENT_DISTANCE = "segmentDistance";

  public static final String SEGMENT_INDEX = "segmentIndex";

  public static int append(final int axisCount, final LineString source, final int sourceIndex,
    final double[] targetCoordinates, final int targetIndex, final int vertexCount) {
    int coordIndex = targetIndex;
    double previousX;
    double previousY;
    if (targetIndex == 0) {
      previousX = Double.NaN;
      previousY = Double.NaN;
    } else {
      previousX = targetCoordinates[(targetIndex - 1) * axisCount];
      previousY = targetCoordinates[(targetIndex - 1) * axisCount + 1];
    }
    int coordinateIndex = coordIndex * axisCount;
    for (int i = 0; i < vertexCount; i++) {
      final int sourceVertexIndex = sourceIndex + i;
      final double x = source.getX(sourceVertexIndex);
      final double y = source.getY(sourceVertexIndex);
      if (x != previousX || y != previousY) {
        targetCoordinates[coordinateIndex++] = x;
        targetCoordinates[coordinateIndex++] = y;
        for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
          final double coordinate = source.getCoordinate(sourceVertexIndex, axisIndex);
          targetCoordinates[coordinateIndex++] = coordinate;
        }
        coordIndex++;
      }
      previousX = x;
      previousY = y;
    }
    return coordIndex;
  }

  public static int appendReverse(final int axisCount, final LineString source,
    final int sourceStartIndex, final double[] targetCoordinates, final int targetStartIndex,
    final int vertexCount) {
    int coordIndex = targetStartIndex;
    final int sourceVertexCount = source.getVertexCount();
    double previousX;
    double previousY;
    if (targetStartIndex == 0) {
      previousX = Double.NaN;
      previousY = Double.NaN;
    } else {
      previousX = targetCoordinates[(targetStartIndex - 1) * axisCount];
      previousY = targetCoordinates[(targetStartIndex - 1) * axisCount + 1];
    }
    int coordinateIndex = coordIndex * axisCount;
    int sourceIndex = sourceVertexCount - 1 - sourceStartIndex;
    for (int i = 0; i < vertexCount; i++) {
      final double x = source.getX(sourceIndex);
      final double y = source.getY(sourceIndex);
      if (x != previousX || y != previousY) {
        targetCoordinates[coordinateIndex++] = x;
        targetCoordinates[coordinateIndex++] = y;
        for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
          final double coordinate = source.getCoordinate(sourceIndex, axisIndex);
          targetCoordinates[coordinateIndex++] = coordinate;
        }
        coordIndex++;
      }
      sourceIndex--;
      previousX = x;
      previousY = y;
    }
    return coordIndex;
  }

  /**
   * <p>
   * Check within a given tolerance that the LINESTRING defined by points2 is
   * contained within the points1.
   * </p>
   * <p>
   * The algorithm is as follows:
   * <ol>
   * <li>Find all coordinates from points2 that are within the tolerance from
   * the line segments of points1.</li>
   * <li>Find all coordinates from points1 that are within the tolerance from
   * the line segments of points2.</li>
   * <li>Split all the line sgements of points1 that were matched in step 1.</li>
   * <li>Split all the line sgements of points2 that were matched in step 2.</li>
   * <li>Line is contained if all line segments from point2 have matching lines
   * in points1.</li>
   * </ol>
   *
   * @param points1
   * @param points2
   * @param tolerance
   * @return
   */
  public static boolean containsWithinTolerance(final LineString points1, final LineString points2,
    final double tolerance) {

    final LineStringGraph graph1 = new LineStringGraph(points1);
    final LineStringGraph graph2 = new LineStringGraph(points2);
    graph1.forEachNode((node) -> movePointsWithinTolerance(null, graph2, tolerance, node));
    graph1.forEachNode((node) -> movePointsWithinTolerance(null, graph1, tolerance, node));

    final Map<Edge<LineSegment>, List<Node<LineSegment>>> pointsOnEdge1 = graph1
      .getPointsOnEdges(graph2, tolerance);
    final Map<Edge<LineSegment>, List<Node<LineSegment>>> pointsOnEdge2 = graph2
      .getPointsOnEdges(graph1, tolerance);
    graph1.splitEdges(pointsOnEdge1);
    graph2.splitEdges(pointsOnEdge2);
    for (final Edge<LineSegment> edge : graph2.getEdges()) {
      final Node<LineSegment> fromNode = edge.getFromNode();
      final Node<LineSegment> toNode = edge.getToNode();
      if (!graph1.hasEdgeBetween(fromNode, toNode)) {
        return false;
      }
    }
    return true;
  }

  public static boolean containsXy(final double[] coordinates, final int vertexCount,
    final int axisCount, final double x, final double y) {
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      final double x1 = coordinates[vertexIndex * axisCount];
      if (Doubles.equal(x, x1)) {
        final double y1 = coordinates[vertexIndex * axisCount + 1];
        if (Doubles.equal(y, y1)) {
          return true;
        }
      }
    }
    return false;
  }

  public static Point[] getPointArray(final Geometry geometry) {
    return getPointArray(geometry, geometry.getVertexCount());
  }

  public static Point[] getPointArray(final Geometry geometry, final int vertexCount) {
    final Point[] points = new Point[vertexCount];
    int i = 0;
    for (final Vertex vertex : geometry.vertices()) {
      if (i > vertexCount) {
        break;
      }
      points[i++] = vertex.newPoint();
    }
    return points;
  }

  public static List<LineString> intersection(final GeometryFactory geometryFactory,
    final LineString points1, final LineString points2, final double maxDistance) {

    final LineStringGraph graph1 = new LineStringGraph(points1);
    graph1.setPrecisionModel(geometryFactory);
    final LineStringGraph graph2 = new LineStringGraph(points2);
    graph2.setPrecisionModel(geometryFactory);
    final Map<Point, Point> movedNodes = new HashMap<>();
    graph1.forEachNode((node) -> movePointsWithinTolerance(movedNodes, graph2, maxDistance, node));
    graph2.forEachNode((node) -> movePointsWithinTolerance(movedNodes, graph1, maxDistance, node));

    final Map<Edge<LineSegment>, List<Node<LineSegment>>> pointsOnEdge1 = graph1
      .getPointsOnEdges(graph2, maxDistance);
    final Map<Edge<LineSegment>, List<Node<LineSegment>>> pointsOnEdge2 = graph2
      .getPointsOnEdges(graph1, maxDistance);
    graph1.splitEdges(pointsOnEdge1);
    graph2.splitEdges(pointsOnEdge2);
    Point startPoint = points1.getPoint(0);
    if (movedNodes.containsKey(startPoint)) {
      startPoint = movedNodes.get(startPoint);
    }
    Point endPoint = points1.getPoint(points1.getVertexCount() - 1);
    if (movedNodes.containsKey(endPoint)) {
      endPoint = movedNodes.get(endPoint);
    }
    final List<LineString> intersections = new ArrayList<>();
    final List<Point> currentCoordinates = new ArrayList<>();
    Node<LineSegment> previousNode = graph1.getNode(startPoint);
    do {
      final List<Edge<LineSegment>> outEdges = previousNode.getOutEdges();
      if (outEdges.isEmpty()) {
        previousNode = null;
      } else if (outEdges.size() > 1) {
        throw new IllegalArgumentException("Cannot handle overlaps\n" + points1 + "\n " + points2);
      } else {
        final Edge<LineSegment> edge = outEdges.get(0);
        final LineSegment line = edge.getObject();
        final Node<LineSegment> nextNode = edge.getToNode();
        if (graph2.hasEdgeBetween(previousNode, nextNode)) {
          if (currentCoordinates.size() == 0) {
            currentCoordinates.add(line.getPoint(0));
          }
          currentCoordinates.add(line.getPoint(1));
        } else {
          if (currentCoordinates.size() > 0) {
            final LineString points = new LineStringDouble(points1.getAxisCount(),
              currentCoordinates);
            intersections.add(points);
            currentCoordinates.clear();
          }
        }
        previousNode = nextNode;
      }

    } while (previousNode != null && !endPoint.equals(2, startPoint));
    if (currentCoordinates.size() > 0) {
      final LineString points = new LineStringDouble(points1.getAxisCount(), currentCoordinates);
      intersections.add(points);
    }
    return intersections;
  }

  /**
   * Only move the node if there is one of them
   *
   * @param graph2
   * @param maxDistance
   * @param node1
   * @return
   */
  public static <T> boolean movePointsWithinTolerance(final Map<Point, Point> movedNodes,
    final Graph<T> graph2, final double maxDistance, final Node<T> node1) {
    final Graph<T> graph1 = node1.getGraph();
    final List<Node<T>> nodes2 = graph2.getNodes(node1, maxDistance);
    if (nodes2.size() == 1) {
      final Node<T> node2 = nodes2.get(0);
      if (graph1.findNode(node2) == null) {
        final GeometryFactory precisionModel = graph1.getPrecisionModel();
        final Point midPoint = LineSegmentUtil.midPoint(precisionModel, node1, node2);
        if (!node1.equals(2, midPoint)) {
          if (movedNodes != null) {
            movedNodes.put(node1.newPoint2D(), midPoint);
          }
          node1.moveNode(midPoint);
        }
        if (!node2.equals(2, midPoint)) {
          if (movedNodes != null) {
            movedNodes.put(node2.newPoint2D(), midPoint);
          }
          node2.moveNode(midPoint);
        }
      }
    }
    return true;
  }

  /**
   * Returns the index of the direction of the point <code>x,y</code> relative to
   * a vector specified by <code>(x1,y1)->(x2,y2)</code>.
   *
   * @param p1 the origin point of the vector
   * @param p2 the final point of the vector
   * @param q the point to compute the direction to
   * @return 1 if q is counter-clockwise (left) from p1-p2
   * @return -1 if q is clockwise (right) from p1-p2
   * @return 0 if q is collinear with p1-p2
   */
  public static int orientationIndex(final double x1, final double y1, final double x2,
    final double y2, final double x, final double y) {
    // travelling along p1->p2, turn counter clockwise to get to q return 1,
    // travelling along p1->p2, turn clockwise to get to q return -1,
    // p1, p2 and q are colinear return 0.
    final double dx1 = x2 - x1;
    final double dy1 = y2 - y1;
    final double dx2 = x - x2;
    final double dy2 = y - y2;
    return RobustDeterminant.signOfDet2x2(dx1, dy1, dx2, dy2);
  }

  public static int orientationIndex(final LineString ring, final int index1, final int index2,
    final int index) {
    return orientationIndex(ring.getX(index1), ring.getY(index1), ring.getX(index2),
      ring.getY(index2), ring.getX(index), ring.getY(index));
  }

  public static LineString removeRepeatedPoints(final LineString points) {
    final int axisCount = points.getAxisCount();
    final List<Double> coordinates = new ArrayList<>();
    double x = points.getX(0);
    double y = points.getY(0);
    coordinates.add(x);
    coordinates.add(y);
    for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
      coordinates.add(points.getCoordinate(0, axisIndex));
    }
    for (int i = 0; i < points.getVertexCount(); i++) {
      final double x1 = points.getX(i);
      final double y1 = points.getY(i);
      if (x != x1 || y != y1) {
        coordinates.add(x1);
        coordinates.add(y1);
        for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
          coordinates.add(points.getCoordinate(i, axisIndex));
        }
        x = x1;
        y = y1;
      }
    }
    return new LineStringDouble(axisCount, coordinates);
  }

  public static void setCoordinates(final double[] coordinates, final int axisCount, final int i,
    final double... point) {
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double value;
      if (axisIndex < point.length) {
        value = point[axisIndex];
      } else {
        value = Double.NaN;
      }
      coordinates[i * axisCount + axisIndex] = value;
    }
  }

  public static void setCoordinates(final double[] coordinates, final int axisCount, final int i,
    final LineString line, final int j) {
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double value = line.getCoordinate(j, axisIndex);
      coordinates[i * axisCount + axisIndex] = value;
    }
  }

  public static void setCoordinates(final double[] coordinates, final int axisCount,
    final int targetVertexIndex, final LineString line, final int sourceVertexIndex,
    final int vertexCount) {
    for (int i = 0; i < vertexCount; i++) {
      setCoordinates(coordinates, axisCount, targetVertexIndex + i, line, sourceVertexIndex + i);
    }
  }

  public static void setCoordinates(final double[] coordinates, final int axisCount, final int i,
    final Point point) {
    if (point != null && !point.isEmpty()) {
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        final double value = point.getCoordinate(axisIndex);
        coordinates[i * axisCount + axisIndex] = value;
      }
    }
  }

  public static void setCoordinates(final GeometryFactory geometryFactory,
    final double[] coordinates, final int axisCount, final int vertexIndex, Point point) {
    if (geometryFactory != null) {
      point = point.convertGeometry(geometryFactory, axisCount);
    }
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      double value = point.getCoordinate(axisIndex);
      value = geometryFactory.makePrecise(axisIndex, value);
      coordinates[vertexIndex * axisCount + axisIndex] = value;
    }
  }

  public static void switchCoordinates(final double[] coordinates, final int axisCount,
    final int vertexIndex1, final int vertexIndex2) {
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double value1 = coordinates[vertexIndex1 * axisCount + axisIndex];
      final double value2 = coordinates[vertexIndex2 * axisCount + axisIndex];
      coordinates[vertexIndex1 * axisCount + axisIndex] = value2;
      coordinates[vertexIndex2 * axisCount + axisIndex] = value1;
    }

  }

}
