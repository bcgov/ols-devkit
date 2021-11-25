package com.revolsys.geometry.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import com.revolsys.geometry.graph.filter.EdgeObjectFilter;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.record.Record;
import com.revolsys.record.Records;
import com.revolsys.record.filter.RecordGeometryFilter;
import com.revolsys.record.property.DirectionalFields;
import com.revolsys.record.schema.RecordDefinition;

public class RecordGraph extends Graph<Record> {

  public static <T extends Geometry> Predicate<Edge<Record>> getEdgeFilter(
    final Predicate<T> geometryFilter) {
    final Predicate<Record> recordFilter = new RecordGeometryFilter<>(geometryFilter);
    final EdgeObjectFilter<Record> edgeFilter = new EdgeObjectFilter<>(recordFilter);
    return edgeFilter;
  }

  public RecordGraph() {
    super(false);
  }

  public RecordGraph(final Iterable<? extends Record> records) {
    addEdges(records);
  }

  public Edge<Record> addEdge(final Record record) {
    final Geometry geometry = record.getGeometry();
    if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      return addEdge(record, line);
    } else if (geometry != null && geometry.isGeometryCollection()) {
      if (geometry.getGeometryCount() == 1) {
        final Geometry part = geometry.getGeometry(0);
        if (part instanceof LineString) {
          final LineString line = (LineString)part;
          return addEdge(record, line);
        }
      }
    }
    throw new IllegalArgumentException("Cannot add edge for a " + geometry.getGeometryType());
  }

  public List<Edge<Record>> addEdges(final Iterable<? extends Record> records) {
    final List<Edge<Record>> edges = new ArrayList<>();
    for (final Record record : records) {
      final Edge<Record> edge = addEdge(record);
      edges.add(edge);
    }
    return edges;
  }

  /**
   * Clone the record, setting the line property to the new value.
   *
   * @param record The record to clone.
   * @param line The line.
   * @return The new record.
   */
  @Override
  protected Record clone(final Record record, final LineString line) {
    if (record == null) {
      return null;
    } else {
      return Records.copy(record, line);
    }
  }

  public Edge<Record> getEdge(final Record record) {
    if (record != null) {
      final LineString line = record.getGeometry();
      return getEdge(record, line);
    }
    return null;
  }

  @Override
  public LineString getEdgeLine(final int edgeId) {
    final Record record = getEdgeObject(edgeId);
    if (record == null) {
      return null;
    } else {
      final LineString line = record.getGeometry();
      return line;
    }
  }

  @Override
  public Node<Record> getNode(final Point point) {
    return super.getNode(point);
  }

  public List<Record> getObjects(final Collection<Integer> edgeIds) {
    final List<Record> records = new ArrayList<>();
    for (final Integer edgeId : edgeIds) {
      final Edge<Record> edge = getEdge(edgeId);
      final Record record = edge.getObject();
      records.add(record);
    }
    return records;
  }

  /**
   * Get the type name for the edge.
   *
   * @param edge The edge.
   * @return The type name.
   */
  @Override
  public String getTypeName(final Edge<Record> edge) {
    final Record record = edge.getObject();
    if (record == null) {
      return null;
    } else {
      final RecordDefinition recordDefinition = record.getRecordDefinition();
      final String typePath = recordDefinition.getPath();
      return typePath;
    }
  }

  public boolean hasEdge(final Record record) {
    final LineString line = record.getGeometry();
    final Point fromPoint = line.getFromPoint();
    final Point toPoint = line.getToPoint();
    final Node<Record> fromNode = findNode(fromPoint);
    final Node<Record> toNode = findNode(toPoint);
    if (fromNode != null && toNode != null) {
      final Collection<Edge<Record>> edges = Node.getEdgesBetween(fromNode, toNode);
      for (final Edge<Record> edge : edges) {
        final LineString updateLine = edge.getLineString();
        if (updateLine.equals(line)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public Edge<Record> merge(final Node<Record> node, final Edge<Record> edge1,
    final Edge<Record> edge2) {
    final Record record1 = edge1.getObject();
    final Record record2 = edge2.getObject();
    final Record mergedObject = DirectionalFields.merge(node, record1, record2);
    final Edge<Record> mergedEdge = addEdge(mergedObject);
    remove(edge1);
    remove(edge2);
    return mergedEdge;
  }

  public List<Edge<Record>> splitEdges(final Point point) {
    return splitEdges(point, 0);
  }

  public List<Edge<Record>> splitEdges(final Point point, final double distance) {
    final List<Edge<Record>> edges = new ArrayList<>();
    for (final Edge<Record> edge : getEdges(point, distance)) {
      final LineString line = edge.getLineString();
      final List<Edge<Record>> splitEdges = edge.splitEdge(point);
      DirectionalFields.edgeSplitFieldValues(line, point, splitEdges);
      edges.addAll(splitEdges);
    }
    return edges;
  }
}
