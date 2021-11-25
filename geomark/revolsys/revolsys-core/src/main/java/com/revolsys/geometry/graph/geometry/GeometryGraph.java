package com.revolsys.geometry.graph.geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.graph.comparator.EdgeAttributeValueComparator;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.editor.BoundingBoxEditor;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDoubleGF;

public class GeometryGraph extends Graph<LineSegment> implements BoundingBoxProxy {

  private final BoundingBoxEditor boundingBox;

  private final List<Geometry> geometries = new ArrayList<>();

  private final List<Point> points = new ArrayList<>();

  private final List<Point> startPoints = new ArrayList<>();

  public GeometryGraph(final Geometry geometry) {
    this(geometry.getGeometryFactory());
    addGeometry(geometry);
  }

  public GeometryGraph(final GeometryFactory geometryFactory) {
    super(false);
    setGeometryFactory(geometryFactory);
    this.boundingBox = geometryFactory.bboxEditor();
  }

  public void addEdge(final Node<LineSegment> fromNode, final Node<LineSegment> toNode) {
    final LineSegment lineSegment = new LineSegmentDoubleGF(fromNode, toNode);
    addEdge(lineSegment, fromNode, toNode);
  }

  private void addEdges(final LineString points, final Map<String, Object> attributes) {
    this.startPoints.add(points.getPoint(0).newPoint2D());
    int index = 0;
    for (LineSegment lineSegment : points.segments()) {
      lineSegment = (LineSegment)lineSegment.clone();
      final double fromX = lineSegment.getX(0);
      final double fromY = lineSegment.getY(0);
      final double toX = lineSegment.getX(1);
      final double toY = lineSegment.getY(1);
      final Edge<LineSegment> edge = addEdge(lineSegment, fromX, fromY, toX, toY);
      attributes.put("segmentIndex", index++);
      edge.setProperties(attributes);
    }
  }

  public void addGeometry(Geometry geometry) {
    geometry = getGeometryFactory().geometry(geometry);
    final Map<String, Object> properties = new LinkedHashMap<>();

    final int geometryIndex = this.geometries.size();
    properties.put("geometryIndex", geometryIndex);
    this.geometries.add(geometry);
    for (int partIndex = 0; partIndex < geometry.getGeometryCount(); partIndex++) {
      properties.put("partIndex", partIndex);
      final Geometry part = geometry.getGeometry(partIndex);
      if (part instanceof Point) {
        final Point point = (Point)part;
        this.points.add(point);
      } else if (part instanceof LineString) {
        final LineString line = (LineString)part;
        final LineString points = line;
        properties.put("type", "LineString");
        addEdges(points, properties);
      } else if (part instanceof Polygon) {
        final Polygon polygon = (Polygon)part;
        int ringIndex = 0;
        for (final LinearRing ring : polygon.rings()) {
          properties.put("ringIndex", ringIndex++);
          if (ringIndex == 0) {
            properties.put("type", "PolygonShell");
          } else {
            properties.put("type", "PolygonHole");
          }
          addEdges(ring, properties);
        }
        properties.remove("ringIndex");
      }
    }

    this.boundingBox.addBbox(geometry);
  }

  @Override
  protected LineSegment clone(final LineSegment segment, final LineString line) {
    return new LineSegmentDoubleGF(line);
  }

  @Override
  public BoundingBoxEditor getBoundingBox() {
    return this.boundingBox;
  }

  @Override
  public LineString getEdgeLine(final int edgeId) {
    final LineSegment object = getEdgeObject(edgeId);
    if (object == null) {
      return null;
    } else {
      final LineString line = object;
      return line;
    }
  }

  /**
   * Only currently works for lines and points.
   *
   * @return
   */

  public Geometry getGeometry() {
    removeDuplicateLineEdges();
    final EdgeAttributeValueComparator<LineSegment> comparator = new EdgeAttributeValueComparator<>(
      "geometryIndex", "partIndex", "segmentIndex");
    final List<Geometry> geometries = new ArrayList<>(this.points);
    final GeometryFactory geometryFactory = getGeometryFactory();
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
              final LineString line = geometryFactory.lineString(points);
              geometries.add(line);
            }
            points.clear();
            ;
            points.add(lineSegment.getPoint(0));
            points.add(lineSegment.getPoint(1));
          }
          if (points.size() > 1) {
            final int toDegree = toNode.getDegree();
            if (toDegree != 2) {
              final LineString line = geometryFactory.lineString(points);
              geometries.add(line);
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
      final LineString line = geometryFactory.lineString(points);
      geometries.add(line);
    }
    return geometryFactory.geometry(geometries);
  }

  private boolean isLineString(final Edge<LineSegment> edge) {
    if ("LineString".equals(edge.getProperty("type"))) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isStartPoint(final Point coordinates) {
    return this.startPoints.contains(coordinates);
  }

  public void removeDuplicateLineEdges() {
    final Comparator<Edge<LineSegment>> comparator = new EdgeAttributeValueComparator<>(
      "geometryIndex", "partIndex", "segmentIndex");
    forEachEdge(comparator, (edge) -> {
      if (isLineString(edge)) {
        final Node<LineSegment> fromNode = edge.getFromNode();
        final Node<LineSegment> toNode = edge.getToNode();

        final Collection<Edge<LineSegment>> edges = fromNode.getEdgesTo(toNode);
        final int duplicateCount = edges.size();
        if (duplicateCount > 1) {
          edges.remove(edge);
          for (final Edge<LineSegment> removeEdge : edges) {
            if (isLineString(removeEdge)) {
              removeEdge.remove();
            }
          }
        }
      }
    });
  }

}
