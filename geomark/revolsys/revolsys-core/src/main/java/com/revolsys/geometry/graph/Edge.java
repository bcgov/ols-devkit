package com.revolsys.geometry.graph;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.DelegatingLineString;
import com.revolsys.geometry.model.Direction;
import com.revolsys.geometry.model.End;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.util.LineStringUtil;
import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.util.Property;

public class Edge<T> implements DelegatingLineString, ObjectWithProperties, Externalizable {

  public static <T> void addEdgeToEdgesByLine(final Map<LineString, Set<Edge<T>>> lineEdgeMap,
    final Edge<T> edge) {
    final LineString line = edge.getLineString();
    for (final Entry<LineString, Set<Edge<T>>> entry : lineEdgeMap.entrySet()) {
      final LineString keyLine = entry.getKey();
      if (LineStringUtil.equalsIgnoreDirection2d(line, keyLine)) {
        final Set<Edge<T>> edges = entry.getValue();
        edges.add(edge);
        return;
      }
    }
    final HashSet<Edge<T>> edges = new HashSet<>();
    edges.add(edge);
    lineEdgeMap.put(line, edges);
  }

  public static <T> void addEdgeToEdgesByLine(final Node<T> node,
    final Map<LineString, Set<Edge<T>>> lineEdgeMap, final Edge<T> edge) {
    LineString line = edge.getLineString();
    for (final Entry<LineString, Set<Edge<T>>> entry : lineEdgeMap.entrySet()) {
      final LineString keyLine = entry.getKey();
      if (LineStringUtil.equalsIgnoreDirection2d(line, keyLine)) {
        final Set<Edge<T>> edges = entry.getValue();
        edges.add(edge);
        return;
      }
    }
    final HashSet<Edge<T>> edges = new HashSet<>();
    if (edge.getEnd(node).isFrom()) {
      line = line.reverse();
    }
    edges.add(edge);
    lineEdgeMap.put(line, edges);
  }

  public static <T> Set<Edge<T>> getEdges(final Collection<Edge<T>> edges, final LineString line) {
    final Set<Edge<T>> newEdges = new LinkedHashSet<>();
    for (final Edge<T> edge : edges) {
      if (LineStringUtil.equalsIgnoreDirection2d(line, edge.getLineString())) {
        newEdges.add(edge);
      }
    }
    return newEdges;
  }

  public static <T> List<Edge<T>> getEdges(final List<Edge<T>> edges,
    final Predicate<Edge<T>> filter) {
    final List<Edge<T>> filteredEdges = new ArrayList<>();
    for (final Edge<T> edge : edges) {
      if (filter.test(edge)) {
        filteredEdges.add(edge);
      }
    }
    return filteredEdges;
  }

  public static <T> Set<Edge<T>> getEdges(final Map<LineString, Set<Edge<T>>> lineEdgeMap,
    final LineString line) {
    for (final Entry<LineString, Set<Edge<T>>> entry : lineEdgeMap.entrySet()) {
      final LineString keyLine = entry.getKey();
      if (LineStringUtil.equalsIgnoreDirection2d(line, keyLine)) {
        final Set<Edge<T>> edges = entry.getValue();
        return edges;
      }
    }
    return null;
  }

  public static <T> Map<LineString, Set<Edge<T>>> getEdgesByLine(final List<Edge<T>> edges) {
    final Map<LineString, Set<Edge<T>>> edgesByLine = new HashMap<>();
    for (final Edge<T> edge : edges) {
      addEdgeToEdgesByLine(edgesByLine, edge);
    }
    return edgesByLine;
  }

  public static <T> Map<LineString, Set<Edge<T>>> getEdgesByLine(final Node<T> node,
    final List<Edge<T>> edges) {
    final Map<LineString, Set<Edge<T>>> edgesByLine = new HashMap<>();
    for (final Edge<T> edge : edges) {
      addEdgeToEdgesByLine(node, edgesByLine, edge);
    }
    return edgesByLine;
  }

  public static <T> List<Edge<T>> getEdgesMatchingObjectFilter(final List<Edge<T>> edges,
    final Predicate<T> filter) {
    final List<Edge<T>> filteredEdges = new ArrayList<>();
    for (final Edge<T> edge : edges) {
      if (!edge.isRemoved()) {
        final T object = edge.getObject();
        if (filter.test(object)) {
          filteredEdges.add(edge);
        }
      }
    }
    return filteredEdges;
  }

  /**
   * Get the list of objects from the collection of edges.
   *
   * @param <T> The type of the objects.
   * @param edges The collection of edges.
   * @return The collection of edges.
   */
  public static <T> List<T> getObjects(final Collection<Edge<T>> edges) {
    final List<T> objects = new ArrayList<>();
    for (final Edge<T> edge : edges) {
      final T object = edge.getObject();
      objects.add(object);
    }
    return objects;
  }

  /**
   * Get the map of type name to list of edges.
   *
   * @param <T> The type of object stored in the edge.
   * @param edges The list of edges.
   * @return The map of type name to list of edges.
   */
  public static <T> Map<String, List<Edge<T>>> getTypeNameEdgesMap(final List<Edge<T>> edges) {
    final Map<String, List<Edge<T>>> edgesByTypeName = new HashMap<>();
    for (final Edge<T> edge : edges) {
      final String typePath = edge.getTypeName();
      List<Edge<T>> typeEdges = edgesByTypeName.get(typePath);
      if (typeEdges == null) {
        typeEdges = new ArrayList<>();
        edgesByTypeName.put(typePath, typeEdges);
      }
      typeEdges.add(edge);
    }
    return edgesByTypeName;
  }

  public static <T> boolean hasEdgeMatchingObjectFilter(final List<Edge<T>> edges,
    final Predicate<T> filter) {
    for (final Edge<T> edge : edges) {
      final T object = edge.getObject();
      if (filter.test(object)) {
        return true;
      }
    }
    return false;
  }

  public static <T> void remove(final Collection<Edge<T>> edges) {
    for (final Edge<T> edge : edges) {
      edge.remove();
    }
  }

  public static <T> void setEdgesAttribute(final List<Edge<T>> edges, final String fieldName,
    final Object value) {
    for (final Edge<T> edge : edges) {
      edge.setProperty(fieldName, value);
    }
  }

  private int fromNodeId;

  /** The graph the edge is part of. */
  private Graph<T> graph;

  private int id;

  private int toNodeId;

  public Edge() {
  }

  public Edge(final int id, final Graph<T> graph, final Node<T> fromNode, final Node<T> toNode) {
    this.id = id;
    this.graph = graph;
    this.fromNodeId = fromNode.getId();
    this.toNodeId = toNode.getId();
    fromNode.addOutEdge(this);
    toNode.addInEdge(this);
  }

  @Override
  public void clearProperties() {
    if (this.graph != null) {
      final Map<Integer, MapEx> propertiesById = this.graph.getEdgePropertiesById();
      propertiesById.remove(this.id);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Edge<T> clone() {
    try {
      return (Edge<T>)super.clone();
    } catch (final CloneNotSupportedException e) {
      return null;
    }
  }

  @Override
  public int compareTo(final Object other) {
    if (other instanceof Edge<?>) {
      final Edge<?> edge = (Edge<?>)other;

      if (this == edge) {
        return 0;
      } else if (isRemoved()) {
        return 1;
      } else if (edge.isRemoved()) {
        return -1;
      } else {
        final Node<?> otherFromNode = edge.getFromNode();
        final Node<?> fromNode = getFromNode();
        final int fromCompare = fromNode.compareTo(otherFromNode.getX(), otherFromNode.getY());
        if (fromCompare == 0) {
          final Node<?> otherToNode = edge.getToNode();
          final Node<T> toNode = getToNode();
          final int toCompare = toNode.compareTo(otherToNode.getX(), otherToNode.getY());
          if (toCompare == 0) {
            final double otherLength = edge.getLength();
            final double length = getLength();
            final int lengthCompare = Double.compare(length, otherLength);
            if (lengthCompare == 0) {
              final String name = toSuperString();
              final String otherName = edge.toSuperString();
              final int nameCompare = name.compareTo(otherName);
              if (nameCompare == 0) {
                return ((Integer)this.id).compareTo(edge.id);
              } else {
                return nameCompare;
              }
            }
            return lengthCompare;
          } else {
            return toCompare;
          }
        } else {
          return fromCompare;
        }
      }
    } else if (other instanceof LineString) {
      final LineString otherLine = (LineString)other;
      final LineString line = getLineString();
      return line.compareTo(otherLine);
    }
    return 1;
  }

  public boolean equalId(final Edge<T> edge) {
    if (edge != null) {
      if (this.graph == edge.getGraph()) {
        return this.id == edge.getId();
      }
    }
    return false;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof Edge<?>) {
      final Edge<?> edge = (Edge<?>)obj;
      return edge.id == this.id && edge.graph == this.graph;
    } else {
      return false;
    }
  }

  @Override
  protected void finalize() throws Throwable {
    if (this.graph != null) {
      this.graph.evict(this);
    }
    super.finalize();
  }

  public double getAngle(final Node<T> node) {
    if (node.getGraph() == this.graph) {
      final int nodeId = node.getId();
      if (nodeId == this.fromNodeId) {
        return getFromAngle();
      } else if (nodeId == this.toNodeId) {
        return getToAngle();

      }
    }
    return Double.NaN;
  }

  @Override
  public int getAxisCount() {
    return this.graph.getAxisCount();
  }

  public Collection<Node<T>> getCommonNodes(final Edge<T> edge) {
    final Collection<Node<T>> nodes1 = getNodes();
    final Collection<Node<T>> nodes2 = edge.getNodes();
    nodes1.retainAll(nodes2);
    return nodes1;
  }

  public Direction getDirection(final Point point) {
    if (getFromPoint().equals(point)) {
      return Direction.FORWARDS;
    } else if (getToPoint().equals(point)) {
      return Direction.BACKWARDS;
    } else {
      return Direction.NONE;
    }
  }

  @SuppressWarnings("unchecked")
  public <T2 extends T> T2 getEdgeObject() {
    return (T2)this.graph.getEdgeObject(this.id);
  }

  public List<Edge<T>> getEdgesToNextJunctionNode(final Node<T> node) {
    final List<Edge<T>> edges = new ArrayList<>();
    edges.add(this);
    Edge<T> currentEdge = this;
    Node<T> currentNode = getOppositeNode(node);
    while (currentNode.getDegree() == 2) {
      currentEdge = currentNode.getNextEdge(currentEdge);
      final Node<T> nextNode = currentEdge.getOppositeNode(currentNode);
      if (nextNode != currentNode) {
        currentNode = nextNode;
        edges.add(currentEdge);
      } else {
        return edges;
      }
    }
    return edges;
  }

  public End getEnd(final Node<T> node) {
    if (getGraph() == node.getGraph()) {
      if (isLoop()) {
        throw new IllegalArgumentException(
          "Cannot get the node end for a loop, without specifying the angle");
      } else {
        final int nodeId = node.getId();
        if (this.fromNodeId == nodeId) {
          return End.FROM;
        } else if (this.toNodeId == nodeId) {
          return End.TO;
        }
      }
    }
    return null;
  }

  public End getEnd(final Node<T> node, final double angle) {
    if (getGraph() == node.getGraph()) {
      if (isLoop()) {
        final int nodeId = node.getId();
        if (this.fromNodeId == nodeId) {
          if (getFromAngle() == angle) {
            return End.FROM;
          } else {
            return End.TO;
          }
        }
      } else {
        return getEnd(node);
      }
    }
    return null;
  }

  /**
   * Get the direction of the edge from the specified node. If the node is at
   * the start of the edge then return {@link End#FROM}. If the node is at the end of the
   * edge return {@link End#TO}. Otherwise return null if it's not at the node.
   *
   * @param node The node to test the direction from.
   * @return True if the node is at the start of the edge.
   */
  @Override
  public End getEnd(final Point point) {
    if (Property.hasValue(point)) {
      if (point.equals(getFromNode())) {
        return End.FROM;
      } else if (point.equals(getToNode())) {
        return End.TO;
      }
    }
    return End.NONE;
  }

  public double getFromAngle() {
    final LineString line = getLineString();
    return line.angleForwards(0);
  }

  public Node<T> getFromNode() {
    return this.graph.getNode(this.fromNodeId);
  }

  public Graph<T> getGraph() {
    return this.graph;
  }

  public int getId() {
    return this.id;
  }

  @Override
  public LineString getLineString() {
    return this.graph.getEdgeLine(this.id);
  }

  public Node<T> getNextJunctionNode(final Node<T> node) {
    Edge<T> currentEdge = this;
    Node<T> currentNode = getOppositeNode(node);
    while (currentNode.getDegree() == 2) {
      currentEdge = currentNode.getNextEdge(currentEdge);
      final Node<T> nextNode = currentEdge.getOppositeNode(currentNode);
      if (nextNode != currentNode) {
        currentNode = nextNode;
      } else {
        return currentNode;
      }
    }
    return currentNode;
  }

  public Node<T> getNode(final boolean from) {
    if (from) {
      return getFromNode();
    } else {
      return getToNode();
    }
  }

  public Collection<Node<T>> getNodes() {
    final LinkedHashSet<Node<T>> nodes = new LinkedHashSet<>();
    nodes.add(getFromNode());
    nodes.add(getToNode());
    return nodes;
  }

  public T getObject() {
    return this.graph.getEdgeObject(this.id);
  }

  public Node<T> getOppositeNode(final Node<T> node) {
    if (node.getGraph() == node.getGraph()) {
      final int nodeId = node.getId();
      if (this.fromNodeId == nodeId) {
        return getToNode();
      } else if (this.toNodeId == nodeId) {
        return getFromNode();
      }
    }
    return null;
  }

  @Override
  public MapEx getProperties() {
    if (this.graph == null) {
      return MapEx.EMPTY;
    } else {
      final Map<Integer, MapEx> propertiesById = this.graph.getEdgePropertiesById();
      MapEx properties = propertiesById.get(this.id);
      if (properties == null) {
        properties = new LinkedHashMapEx();
        propertiesById.put(this.id, properties);
      }
      return properties;
    }
  }

  @Override
  public <V> V getProperty(final String name) {
    if (this.graph != null) {
      final Map<Integer, MapEx> propertiesById = this.graph.getEdgePropertiesById();
      final Map<String, Object> properties = propertiesById.get(this.id);
      return ObjectWithProperties.getProperty(this, properties, name);
    }
    return null;
  }

  public double getToAngle() {
    final LineString line = getLineString();
    if (line == null) {
      return Double.NaN;
    } else {
      return line.angleBackwards(line.getLastSegmentIndex());
    }
  }

  public Node<T> getToNode() {
    return this.graph.getNode(this.toNodeId);
  }

  public String getTypeName() {
    return this.graph.getTypeName(this);
  }

  @Override
  public int hashCode() {
    return Integer.hashCode(this.id);
  }

  public boolean hasNode(final Node<T> node) {
    if (node != null && node.getGraph() == this.graph) {
      final int nodeId = node.getId();
      if (this.fromNodeId == nodeId) {
        return true;
      } else if (this.toNodeId == nodeId) {
        return true;
      }
    }
    return false;
  }

  public boolean hasNode(final Point point) {
    final Node<T> node = this.graph.findNode(point);
    if (node == null) {
      return false;
    } else {
      return hasNode(node);
    }
  }

  public boolean isLoop() {
    return this.fromNodeId == this.toNodeId;
  }

  public boolean isRemoved() {
    return this.graph == null;
  }

  @Override
  public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    final int graphId = in.readInt();
    this.graph = Graph.getGraph(graphId);
    this.id = in.readInt();
    this.fromNodeId = in.readInt();
    this.toNodeId = in.readInt();
  }

  public void remove() {
    if (this.graph != null) {
      this.graph.remove(this);
    }
  }

  void removeInternal() {
    final Node<T> fromNode = this.graph.getNode(this.fromNodeId);
    if (fromNode != null) {
      fromNode.remove(this);
    }
    final Node<T> toNode = this.graph.getNode(this.toNodeId);
    if (toNode != null) {
      toNode.remove(this);
    }
    this.graph = null;
  }

  public List<Edge<T>> replace(final LineString... lines) {
    return replace(Arrays.asList(lines));
  }

  public List<Edge<T>> replace(final List<LineString> lines) {
    if (isRemoved()) {
      return Collections.emptyList();
    } else {
      final Graph<T> graph = getGraph();
      return graph.replaceEdge(this, lines);
    }
  }

  public void setObject(final T object) {
    this.graph.setEdgeObject(this.id, object);
  }

  public <V extends Point> List<Edge<T>> splitEdge(final Collection<V> splitPoints) {
    return this.graph.splitEdge(this, splitPoints);
  }

  public <V extends Point> List<Edge<T>> splitEdge(final Collection<V> points,
    final double maxDistance) {
    return this.graph.splitEdge(this, points, maxDistance);
  }

  public List<Edge<T>> splitEdge(final List<Point> points) {
    return this.graph.splitEdge(this, points);
  }

  public List<Edge<T>> splitEdge(final Point... points) {
    return splitEdge(Arrays.asList(points));
  }

  public List<Edge<T>> splitEdge(final Point point) {
    return this.graph.splitEdge(this, point);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(' ');
    if (isRemoved()) {
      return "Removed Edge";
    } else {
      final String typeName = getTypeName();
      if (typeName != null) {
        sb.append(typeName.toString());
      }
      sb.append(this.id);
      sb.append('{');
      sb.append(this.fromNodeId);
      sb.append(',');
      sb.append(this.toNodeId);
      sb.append("}\tLINESTRING(");
      final Node<T> fromNode = getFromNode();
      sb.append(fromNode.getX());
      sb.append(" ");
      sb.append(fromNode.getY());
      sb.append(",");
      final Node<T> toNode = getToNode();
      sb.append(toNode.getX());
      sb.append(" ");
      sb.append(toNode.getY());
      sb.append(")\n");
      sb.append(getObject());
    }
    return sb.toString();
  }

  private String toSuperString() {
    return super.toString();
  }

  public boolean touches(final Edge<T> edge) {
    final Collection<Node<T>> nodes1 = getCommonNodes(edge);
    return !nodes1.isEmpty();
  }

  @Override
  public void writeExternal(final ObjectOutput out) throws IOException {
    final int graphId = this.graph.getId();
    out.writeInt(graphId);
    out.writeInt(this.id);
    out.writeInt(this.fromNodeId);
    out.writeInt(this.toNodeId);

  }
}
