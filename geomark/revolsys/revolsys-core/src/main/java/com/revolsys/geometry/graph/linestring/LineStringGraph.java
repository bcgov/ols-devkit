package com.revolsys.geometry.graph.linestring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.comparator.CollectionComparator;
import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.graph.comparator.EdgeAttributeValueComparator;
import com.revolsys.geometry.graph.comparator.NodeDistanceComparator;
import com.revolsys.geometry.graph.filter.EdgeLineSegmentDistanceFilter;
import com.revolsys.geometry.graph.filter.EdgeObjectFilter;
import com.revolsys.geometry.graph.filter.NodeCoordinatesFilter;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.model.coordinates.comparator.PointDistanceComparator;
import com.revolsys.geometry.model.coordinates.filter.CrossingLineSegmentFilter;
import com.revolsys.geometry.model.coordinates.filter.PointOnLineSegment;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDoubleGF;

public class LineStringGraph extends Graph<LineSegment> {
  private static final String INDEX = "INDEX";

  private static final CollectionComparator<Integer> INDEX_COMPARATOR = new CollectionComparator<>();

  public static Edge<LineSegment> getFirstEdge(final Collection<Edge<LineSegment>> edges) {
    final Iterator<Edge<LineSegment>> iterator = edges.iterator();
    if (iterator.hasNext()) {
      Edge<LineSegment> edge = iterator.next();
      List<Integer> index = edge.getProperty(INDEX);
      while (iterator.hasNext()) {
        final Edge<LineSegment> edge2 = iterator.next();
        final List<Integer> index2 = edge2.getProperty(INDEX);
        if (INDEX_COMPARATOR.compare(index, index2) > 0) {
          edge = edge2;
          index = index2;
        }
      }
      return edge;
    } else {
      return null;
    }
  }

  private BoundingBox envelope;

  private Point fromPoint;

  private GeometryFactory geometryFactory;

  private LineString points;

  public LineStringGraph(final GeometryFactory geometryFactory, final LineString line) {
    super(false);
    setGeometryFactory(geometryFactory);
    setLineString(line);
  }

  public LineStringGraph(final LineString line) {
    this(line.getGeometryFactory(), line);
  }

  @Override
  protected LineSegment clone(final LineSegment object, final LineString line) {
    return new LineSegmentDoubleGF(line);
  }

  @Override
  public LineString getEdgeLine(final int edgeId) {
    final LineSegment segment = getEdgeObject(edgeId);
    if (segment == null) {
      return null;
    } else {
      return segment;
    }
  }

  public LineString getLine() {
    final Set<Edge<LineSegment>> processedEdges = new HashSet<>();
    final List<Point> newPoints = new ArrayList<>();
    Node<LineSegment> previousNode = findNode(this.fromPoint);
    newPoints.add(previousNode);
    do {
      final List<Edge<LineSegment>> outEdges = previousNode.getOutEdges();
      outEdges.removeAll(processedEdges);
      if (outEdges.isEmpty()) {
        previousNode = null;
      } else {
        final Edge<LineSegment> edge = getFirstEdge(outEdges);
        processedEdges.add(edge);
        final Node<LineSegment> nextNode = edge.getToNode();

        newPoints.add(nextNode);

        previousNode = nextNode;
      }
    } while (previousNode != null && !previousNode.equals(2, this.fromPoint));
    return this.geometryFactory.lineString(newPoints);
  }

  public List<LineString> getLines() {
    removeDuplicateEdges();
    final EdgeAttributeValueComparator<LineSegment> comparator = new EdgeAttributeValueComparator<>(
      "INDEX");
    final List<LineString> lines = new ArrayList<>();
    final List<Point> points = new ArrayList<>();
    final Consumer<Edge<LineSegment>> action = new Consumer<>() {
      private Node<LineSegment> previousNode = null;

      @Override
      public void accept(final Edge<LineSegment> edge) {
        final LineSegment lineSegment = edge.getObject();
        if (lineSegment.getLength() > 0) {
          final Node<LineSegment> fromNode = edge.getFromNode();
          final Node<LineSegment> toNode = edge.getToNode();
          if (this.previousNode == null) {
            points.add(lineSegment.getPoint(0));
            points.add(lineSegment.getPoint(1));
          } else if (fromNode == this.previousNode) {
            if (edge.getLength() > 0) {
              points.add(toNode);
            }
          } else {
            if (points.size() > 1) {
              final LineString line = LineStringGraph.this.geometryFactory.lineString(points);
              lines.add(line);
            }
            points.clear();
            ;
            points.add(lineSegment.getPoint(0));
            points.add(lineSegment.getPoint(1));
          }
          if (points.size() > 1) {
            final int toDegree = toNode.getDegree();
            if (toDegree != 2) {
              final LineString line = LineStringGraph.this.geometryFactory.lineString(points);
              lines.add(line);
              points.clear();
              ;
              points.add(toNode);
            }
          }
          this.previousNode = toNode;
        }
      }
    };
    forEachEdge(comparator, action);
    if (points.size() > 1) {
      final LineString line = this.geometryFactory.lineString(points);
      lines.add(line);
    }
    return lines;
  }

  public Map<Edge<LineSegment>, List<Node<LineSegment>>> getPointsOnEdges(
    final Graph<LineSegment> graph1, final double tolerance) {
    final Map<Edge<LineSegment>, List<Node<LineSegment>>> pointsOnEdge1 = new HashMap<>();
    for (final Edge<LineSegment> edge : getEdges()) {
      final Node<LineSegment> fromNode = edge.getFromNode();
      final Node<LineSegment> toNode = edge.getToNode();
      final LineSegment lineSegment = edge.getObject();
      final PointOnLineSegment coordinatesFilter = new PointOnLineSegment(lineSegment, tolerance);
      final NodeCoordinatesFilter<LineSegment> nodeFilter = new NodeCoordinatesFilter<>(
        coordinatesFilter);
      final NodeDistanceComparator<LineSegment> comparator = new NodeDistanceComparator<>(fromNode);
      final List<Node<LineSegment>> nodes = graph1.getNodes(nodeFilter, comparator);
      for (final Iterator<Node<LineSegment>> iterator = nodes.iterator(); iterator.hasNext();) {
        final Node<LineSegment> node = iterator.next();
        if (node.equals(2, fromNode)) {
          iterator.remove();
        } else if (node.equals(2, toNode)) {
          iterator.remove();
        }
      }
      if (!nodes.isEmpty()) {
        pointsOnEdge1.put(edge, nodes);
      }
    }
    return pointsOnEdge1;
  }

  public Geometry getSelfIntersections() {
    final Set<Point> intersectionPoints = new HashSet<>();
    for (int i = 0; i < this.points.getVertexCount(); i++) {
      final Point point = this.points.getPoint(i);
      final Node<LineSegment> node = getNode(point);
      if (node.getDegree() > 2 || hasTouchingEdges(node)) {
        intersectionPoints.add(point);
      }
    }
    forEachEdge((edge1) -> {
      final LineSegment lineSegment1 = edge1.getObject();
      forEachEdge(edge1, (edge2) -> {
        if (edge1 != edge2) {
          final LineSegment lineSegment2 = edge2.getObject();
          final Geometry intersections = ((LineSegment)lineSegment1
            .convertGeometry(getGeometryFactory())).getIntersection(lineSegment2);
          for (final Point intersection : intersections.vertices()) {
            if (!lineSegment1.isEndPoint(intersection) && !lineSegment2.isEndPoint(intersection)) {
              intersectionPoints.add(intersection);
            }
          }
        }
      });
    });
    return this.geometryFactory.punctual(intersectionPoints);
  }

  /**
   * Get the z-value for the point if it is at a node or on an edge.
   *
   * @param point The point to get the z-value for.
   * @return The z-value or Double.NaN.
   */
  public double getZ(final Point point) {
    final Node<LineSegment> node = findNode(point);
    if (node == null) {
      final double maxDistance = this.geometryFactory.getScaleXY() / 1000;
      for (final Edge<LineSegment> edge : getEdges(point, maxDistance)) {
        final LineSegment segment = edge.getObject();
        if (segment.isPointOnLineMiddle(point, maxDistance)) {
          final double elevation = segment.getElevation(point);
          return this.geometryFactory.makeZPrecise(elevation);
        }
      }
      return Double.NaN;
    } else {
      return node.getZ();
    }
  }

  public boolean hasTouchingEdges(final Node<LineSegment> node) {
    final GeometryFactory precisionModel = getPrecisionModel();
    final List<Edge<LineSegment>> edges = getEdges(node, precisionModel.getScaleXY());
    for (final Edge<LineSegment> edge : edges) {
      final Point lineStart = edge.getFromNode();
      final Point lineEnd = edge.getToNode();
      if (LineSegmentUtil.isPointOnLineMiddle(precisionModel, lineStart, lineEnd, node)) {
        return true;
      }
    }
    return false;
  }

  public boolean intersects(final LineString line) {
    BoundingBox envelope = line.getBoundingBox();
    final double scaleXY = this.geometryFactory.getScaleXY();
    double maxDistance = 0;
    if (scaleXY > 0) {
      maxDistance = 1 / scaleXY;
    }
    envelope = envelope //
      .bboxEditor() //
      .expandDelta(maxDistance);
    if (envelope.bboxIntersects(this.envelope)) {
      final LineString points = line;
      final int numPoints = points.getVertexCount();
      final Point fromPoint = points.getPoint(0);
      final Point toPoint = points.getPoint(numPoints - 1);

      Point previousPoint = fromPoint;
      for (int i = 1; i < numPoints; i++) {
        final Point nextPoint = points.getPoint(i);
        final LineSegment line1 = new LineSegmentDoubleGF(previousPoint, nextPoint);
        final List<Edge<LineSegment>> edges = EdgeLineSegmentDistanceFilter.getEdges(this, line1,
          maxDistance);
        for (final Edge<LineSegment> edge2 : edges) {
          final LineSegment line2 = edge2.getObject();
          final Geometry intersections = line1.getIntersection(line2);
          for (final Point intersection : intersections.vertices()) {
            if (intersection.equals(fromPoint) || intersection.equals(toPoint)) {
              // Point intersection, make sure it's not at the start
              final Node<LineSegment> node = findNode(intersection);
              final int degree = node.getDegree();
              if (node.equals(2, this.fromPoint)) {
                if (degree > 2) {
                  // Intersection not at the start/end of the other line, taking
                  // into account loops
                  return true;
                }
              } else if (degree > 1) {
                // Intersection not at the start/end of the other line
                return true;
              }
            } else {
              // Intersection not at the start/end of the line
              return true;
            }
          }
          for (final Point point : line1.vertices()) {
            if (line2.distancePoint(point) < maxDistance) {

              if (point.equals(fromPoint) || point.equals(toPoint)) {
                // Point intersection, make sure it's not at the start
                for (final Node<LineSegment> node : getNodes(point, maxDistance)) {
                  final int degree = node.getDegree();
                  if (node.equals(2, this.fromPoint)) {
                    if (degree > 2) {
                      // Intersection not at the start/end of the other line,
                      // taking
                      // into account loops
                      return true;
                    }
                  } else if (degree > 1) {
                    // Intersection not at the start/end of the other line
                    return true;
                  }
                }
              } else {
                // Intersection not at the start/end of the line
                return true;
              }
            }
          }

        }
        previousPoint = nextPoint;
      }
    }
    return false;
  }

  public boolean isSimple() {
    for (final Node<LineSegment> node : getNodes()) {
      if (node.getDegree() > 2) {
        return false;
      }
    }

    for (final Edge<LineSegment> edge : getEdges()) {
      final LineSegment line = edge.getObject();
      final EdgeObjectFilter<LineSegment> filter = new EdgeObjectFilter<>(
        new LineSegmentIntersectingFilter(line));
      final List<Edge<LineSegment>> edges = getEdges(line, filter);
      for (final Edge<LineSegment> edge2 : edges) {
        final LineSegment line2 = edge2.getObject();
        final Geometry intersections = line.getIntersection(line2);
        if (intersections instanceof LineSegment) {
          return false;
        } else if (intersections instanceof Point) {
          if (edge.getCommonNodes(edge2).isEmpty()) {
            return false;
          }
        }
      }
    }
    return true;
  }

  @Override
  public void nodeMoved(final Node<LineSegment> node, final Node<LineSegment> newNode) {
    if (this.fromPoint.equals(2, node)) {
      this.fromPoint = newNode.newPoint2D();
    }
  }

  private void removeDuplicateEdges() {
    final Comparator<Edge<LineSegment>> comparator = new EdgeAttributeValueComparator<>(INDEX);
    forEachEdge(comparator, (edge) -> {
      final Node<LineSegment> fromNode = edge.getFromNode();
      final Node<LineSegment> toNode = edge.getToNode();

      final Collection<Edge<LineSegment>> edges = fromNode.getEdgesTo(toNode);
      final int duplicateCount = edges.size();
      if (duplicateCount > 1) {
        edges.remove(edge);
        Edge.remove(edges);
      }
    });
  }

  @Override
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    setPrecisionModel(geometryFactory);
  }

  private void setLineString(final LineString lineString) {
    this.points = lineString;
    int index = 0;
    for (final LineSegment lineSegment : lineString.segments()) {
      final double fromX = lineSegment.getX(0);
      final double fromY = lineSegment.getY(0);
      final double toX = lineSegment.getX(1);
      final double toY = lineSegment.getY(1);
      final Edge<LineSegment> edge = addEdge((LineSegment)lineSegment.clone(), fromX, fromY, toX,
        toY);

      edge.setProperty(INDEX, Arrays.asList(index++));
    }
    this.fromPoint = lineString.getPoint(0).newPoint2D();
    this.envelope = lineString.getBoundingBox();
  }

  public void splitCrossingEdges() {
    final Comparator<Edge<LineSegment>> comparator = new EdgeAttributeValueComparator<>(INDEX);
    forEachEdge(comparator, (edge) -> {
      final LineSegment line1 = edge.getObject();
      final Predicate<LineSegment> lineFilter = new CrossingLineSegmentFilter(line1);
      final Predicate<Edge<LineSegment>> filter = new EdgeObjectFilter<>(lineFilter);
      final List<Edge<LineSegment>> edges = getEdges(line1, filter);

      if (!edges.isEmpty()) {
        final List<Point> points = new ArrayList<>();
        for (final Edge<LineSegment> edge2 : edges) {
          final LineSegment line2 = edge2.getObject();
          final Geometry intersections = line1.getIntersection(line2);
          if (intersections instanceof Point) {
            final Point intersection = (Point)intersections;
            points.add(intersection);
            edge2.split(intersection);
          }
        }
        if (!points.isEmpty()) {
          edge.splitEdge(points);
        }
      }
    });
  }

  @Override
  public <V extends Point> List<Edge<LineSegment>> splitEdge(final Edge<LineSegment> edge,
    final Collection<V> nodes) {
    final List<Edge<LineSegment>> newEdges = new ArrayList<>();
    if (!edge.isRemoved()) {
      final Node<LineSegment> fromNode = edge.getFromNode();
      final Node<LineSegment> toNode = edge.getToNode();
      final PointDistanceComparator comparator = new PointDistanceComparator(fromNode.getX(),
        toNode.getY());
      final Set<Point> newPoints = new TreeSet<>(comparator);
      for (final Point point : nodes) {
        newPoints.add(point);
      }
      newPoints.add(toNode);

      final List<Integer> index = edge.getProperty(INDEX);
      int i = 0;
      Point previousPoint = fromNode;
      for (final Point point : newPoints) {
        final LineSegment lineSegment = new LineSegmentDoubleGF(previousPoint, point);
        final Edge<LineSegment> newEdge = addEdge(lineSegment, previousPoint, point);
        final List<Integer> newIndecies = new ArrayList<>(index);
        newIndecies.add(i++);
        newEdge.setProperty(INDEX, newIndecies);
        newEdges.add(newEdge);
        previousPoint = point;
      }

      remove(edge);
    }
    return newEdges;
  }

  public <V extends Point> void splitEdges(final Map<Edge<LineSegment>, List<V>> pointsOnEdge1) {
    for (final Entry<Edge<LineSegment>, List<V>> entry : pointsOnEdge1.entrySet()) {
      final Edge<LineSegment> edge = entry.getKey();
      final List<V> nodes = entry.getValue();
      splitEdge(edge, nodes);
    }
  }

  public void splitEdgesCloseToNodes() {
    double distance = 0;
    final double scaleXY = this.geometryFactory.getScaleXY();
    if (scaleXY > 0) {
      distance = 1 / scaleXY;
    }
    for (final Node<LineSegment> node : getNodes()) {
      final List<Edge<LineSegment>> edges = getEdges(node, distance);
      edges.removeAll(node.getEdges());
      if (!edges.isEmpty()) {
        for (final Edge<LineSegment> edge : edges) {
          final LineSegment line = edge.getObject();
          if (line.isPointOnLineMiddle(node, distance)) {
            edge.split(node);
          }
        }
      }
    }
  }

}
