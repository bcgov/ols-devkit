package com.revolsys.geometry.graph;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.graph.attribute.NodeProperties;
import com.revolsys.geometry.model.ClockDirection;
import com.revolsys.geometry.model.End;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.properties.ObjectPropertyProxy;
import com.revolsys.properties.ObjectWithProperties;

public class Node<T> extends PointDoubleXY implements ObjectWithProperties, Externalizable {
  private static final int[] EMPTY_IDS = new int[0];

  public static <V> Predicate<Node<V>> filterDegree(final int degree) {
    return (node) -> {
      return node.getDegree() == degree;
    };
  }

  public static <V> int getEdgeIndex(final List<Edge<V>> edges, final Edge<V> edge) {
    return edges.indexOf(edge);
  }

  public static <T> Set<Edge<T>> getEdgesBetween(final Node<T> node0, final Node<T> node1) {
    final Set<Edge<T>> commonEdges = new HashSet<>();
    if (node1 == null) {
      return commonEdges;
    } else if (node0 == node1) {
      for (final Edge<T> edge : node0.getEdges()) {
        if (edge.getFromNode() == edge.getToNode()) {
          commonEdges.add(edge);
        }
      }
    } else {
      final Collection<Edge<T>> edges0 = node0.getEdges();
      commonEdges.addAll(edges0);
      final Collection<Edge<T>> edges1 = node1.getEdges();
      commonEdges.retainAll(edges1);
    }
    return commonEdges;
  }

  public static <T> Collection<Edge<T>> getEdgesBetween(final String typePath, final Node<T> node0,
    final Node<T> node1) {
    final Collection<Edge<T>> edges = getEdgesBetween(node0, node1);
    for (final Iterator<Edge<T>> edgeIter = edges.iterator(); edgeIter.hasNext();) {
      final Edge<T> edge = edgeIter.next();
      if (!edge.getTypeName().equals(typePath)) {
        edgeIter.remove();
      }
    }
    return edges;
  }

  // TODO broken as it doesn't deal with in/out edge directions correctly or
  // with loops
  public static <V> Edge<V> getNextEdge(final List<Edge<V>> edges, final Edge<V> edge) {
    final int index = getEdgeIndex(edges, edge);
    final int nextIndex = (index + 1) % edges.size();
    return edges.get(nextIndex);
  }

  public static <T> boolean hasEdgesBetween(final String typePath, final Node<T> node0,
    final Node<T> node1) {
    if (node1 == null) {
      return false;
    }
    if (node0 == node1) {
      for (final Edge<T> edge : node0.getEdges()) {
        if (edge.getTypeName().equals(typePath)) {
          if (edge.getFromNode() == edge.getToNode()) {
            return true;
          }
        }
      }
    } else {
      for (final Edge<T> edge : node0.getEdges()) {
        if (edge.getTypeName().equals(typePath)) {
          if (edge.hasNode(node1)) {
            return true;
          }
        }
      }
      for (final Edge<T> edge : node1.getEdges()) {
        if (edge.getTypeName().equals(typePath)) {
          if (edge.hasNode(node0)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private Graph<T> graph;

  private int id;

  private int[] inEdgeIds = EMPTY_IDS;

  private int[] outEdgeIds = EMPTY_IDS;

  public Node() {
  }

  protected Node(final int nodeId, final Graph<T> graph, final double x, final double y) {
    super(x, y);
    this.id = nodeId;
    this.graph = graph;
  }

  private int[] addEdge(final int[] oldEdgeIds, final Edge<T> edge) {
    final Graph<T> graph = getGraph();
    final List<Edge<T>> edges = graph.getEdges(oldEdgeIds);
    edges.add(edge);
    final EdgeToAngleComparator<T> comparator = EdgeToAngleComparator.get();
    Collections.sort(edges, comparator);
    return graph.getEdgeIds(edges);
  }

  protected void addInEdge(final Edge<T> edge) {
    this.inEdgeIds = addEdge(this.inEdgeIds, edge);
    updateAttributes();
  }

  protected void addOutEdge(final Edge<T> edge) {
    this.outEdgeIds = addEdge(this.outEdgeIds, edge);
    updateAttributes();
  }

  @Override
  public void clearProperties() {
    if (this.graph != null) {
      final Map<Integer, MapEx> propertiesById = this.graph.getNodePropertiesById();
      propertiesById.remove(this.id);
    }
  }

  public int compareTo(final Node<T> node) {
    return compareTo((Point)node);
  }

  public boolean containsEdge(final Edge<T> edge) {
    if (edge != null) {
      final Graph<T> graph = getGraph();
      if (graph == edge.getGraph()) {
        for (final int edgeId : this.inEdgeIds) {
          if (graph.getEdge(edgeId) == edge) {
            return true;
          }
        }
        for (final int edgeId : this.outEdgeIds) {
          if (graph.getEdge(edgeId) == edge) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof Node<?>) {
      final Node<?> node = (Node<?>)other;
      return node.id == this.id && node.graph == this.graph;
    } else {
      return super.equals(other);
    }
  }

  public boolean equalsCoordinate(final double x, final double y) {
    return this.getX() == x && this.getY() == y;
  }

  @Override
  protected void finalize() throws Throwable {
    if (this.graph != null) {
      this.graph.evict(this);
    }
    super.finalize();
  }

  public void forEachEdge(final Consumer<Edge<T>> action) {
    final Graph<T> graph = this.graph;
    for (final int edgeId : this.inEdgeIds) {
      final Edge<T> edge = graph.getEdge(edgeId);
      action.accept(edge);
    }
    for (final int edgeId : this.outEdgeIds) {
      final Edge<T> edge = graph.getEdge(edgeId);
      action.accept(edge);
    }
  }

  public void forEachInEdge(final Consumer<Edge<T>> action) {
    final Graph<T> graph = this.graph;
    for (final int edgeId : this.inEdgeIds) {
      final Edge<T> edge = graph.getEdge(edgeId);
      action.accept(edge);
    }
  }

  public void forEachObject(final Consumer<T> action) {
    forEachEdge((e) -> {
      final T object = e.getObject();
      action.accept(object);
    });
  }

  public void forEachOutEdge(final Consumer<Edge<T>> action) {
    final Graph<T> graph = this.graph;
    for (final int edgeId : this.outEdgeIds) {
      final Edge<T> edge = graph.getEdge(edgeId);
      action.accept(edge);
    }
  }

  public Point get3dCoordinates(final String typePath) {
    if (!isRemoved()) {

      final List<Edge<T>> edges = NodeProperties.getEdgesByType(this, typePath);
      if (!edges.isEmpty()) {
        Point coordinates = null;
        for (final Edge<T> edge : edges) {
          final LineString line = edge.getLineString();
          final LineString points = line;
          Point point = null;
          if (edge.getFromNode() == this) {
            point = points.getPoint(0);
          } else if (edge.getToNode() == this) {
            point = points.getPoint(points.getVertexCount() - 1);
          }
          if (point != null) {
            final double z = point.getZ();
            if (z == 0 || java.lang.Double.isNaN(z)) {
              coordinates = point;
            } else {
              return point;
            }
          }
        }
        return coordinates;
      }
    }
    return this;

  }

  @Override
  public double getCoordinate(final int index) {
    switch (index) {
      case 0:
        return this.getX();
      case 1:
        return this.getY();

      default:
        return java.lang.Double.NaN;
    }
  }

  public int getDegree() {
    return this.inEdgeIds.length + this.outEdgeIds.length;
  }

  public Edge<T> getEdge(final int i) {
    final List<Edge<T>> edges = getEdges();
    return edges.get(i);
  }

  public int getEdgeCount() {
    return getDegree();
  }

  public int getEdgeIndex(final Edge<T> edge) {
    final List<Edge<T>> edges = getEdges();
    return getEdgeIndex(edges, edge);
  }

  public List<Edge<T>> getEdges() {
    final ArrayList<Edge<T>> edges = new ArrayList<>();
    final List<Edge<T>> inEdges = getInEdges();
    final Iterator<Edge<T>> inIterator = inEdges.iterator();
    final List<Edge<T>> outEdges = getOutEdges();
    final Iterator<Edge<T>> outIterator = outEdges.iterator();
    if (!inIterator.hasNext()) {
      edges.addAll(outEdges);
    } else if (!outIterator.hasNext()) {
      edges.addAll(inEdges);
    } else {
      Edge<T> inEdge = inIterator.next();
      final double inAngle = inEdge.getToAngle();
      Edge<T> outEdge = outIterator.next();
      final double outAngle = outEdge.getFromAngle();
      do {
        boolean nextIn = false;
        boolean nextOut = false;
        if (inEdge == null) {
          edges.add(outEdge);
          nextOut = true;
        } else if (outEdge == null) {
          edges.add(inEdge);
          nextIn = true;
        } else {
          if (outAngle <= inAngle) {
            edges.add(outEdge);
            nextOut = true;
          } else {
            edges.add(inEdge);
            nextIn = true;
          }
        }
        if (nextOut) {
          if (outIterator.hasNext()) {
            outEdge = outIterator.next();
          } else {
            outEdge = null;
          }
        }
        if (nextIn) {
          if (inIterator.hasNext()) {
            inEdge = inIterator.next();
          } else {
            inEdge = null;
          }
        }
      } while (inEdge != null || outEdge != null);
    }
    return edges;
  }

  public Map<Double, List<Edge<T>>> getEdgesByAngle() {
    final Map<Double, List<Edge<T>>> edgesByAngle = new TreeMap<>();
    forEachOutEdge((edge) -> {
      final double angle = edge.getFromAngle();
      Maps.addToList(edgesByAngle, angle, edge);
    });
    forEachInEdge((edge) -> {
      final double angle = edge.getToAngle();
      Maps.addToList(edgesByAngle, angle, edge);
    });

    return edgesByAngle;
  }

  public Set<Edge<T>> getEdgesTo(final Node<T> node) {
    return getEdgesBetween(this, node);
  }

  /**
   * Get all the edges from a node which do not have an attribute with the
   * specified name.
   *
   * @param node The node to get the edges for.
   * @param fieldName The attribute name.
   * @return The list of edges without the attribute.
   */
  public List<Edge<T>> getEdgesWithoutAttribute(final String fieldName) {
    final List<Edge<T>> edges = new ArrayList<>();
    for (final Edge<T> edge : getEdges()) {
      if (edge.getProperty(fieldName) == null) {
        edges.add(edge);
      }
    }
    return edges;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.graph.getGeometryFactory();
  }

  public Graph<T> getGraph() {
    return this.graph;
  }

  public int getId() {
    return this.id;
  }

  public Edge<T> getInEdge(final int i) {
    final int edgeId = this.inEdgeIds[i];
    return this.graph.getEdge(edgeId);
  }

  public int getInEdgeCount() {
    return this.inEdgeIds.length;
  }

  public int getInEdgeIndex(final Edge<T> edge) {
    return getInEdges().indexOf(edge);
  }

  public List<Edge<T>> getInEdges() {
    final Graph<T> graph = getGraph();
    return graph.getEdges(this.inEdgeIds);
  }

  public <T2 extends T> List<T2> getInObjects() {
    if (this.inEdgeIds.length == 0) {
      return Collections.emptyList();
    } else {
      final Graph<T> graph = getGraph();
      return graph.getEdgeObjects(this.inEdgeIds);
    }
  }

  public Edge<T> getNextEdge(final Edge<T> edge) {
    final List<Edge<T>> edges = getEdges();
    return getNextEdge(edges, edge);
  }

  public Edge<T> getNextEdge(final Edge<T> edge, final ClockDirection direction) {
    final List<Edge<T>> edges = getEdges();
    // TODO verify edge angles
    final int index = getEdgeIndex(edges, edge);
    int step = 0;
    if (direction.isCounterClockwise()) {
      step = -1;
    } else {
      step = 1;
    }
    final int nextIndex = (index + step) % edges.size();
    return edges.get(nextIndex);
  }

  public Edge<T> getNextInEdge(final Edge<T> edge) {
    final int index = getInEdgeIndex(edge);
    final int nextIndex = (index + 1) % this.inEdgeIds.length;
    final Graph<T> graph = getGraph();
    return graph.getEdge(this.inEdgeIds[nextIndex]);
  }

  public Edge<T> getNextOutEdge(final Edge<T> edge) {
    final int index = getOutEdgeIndex(edge);
    final int nextIndex = (index + 1) % this.outEdgeIds.length;
    final Graph<T> graph = getGraph();
    return graph.getEdge(this.outEdgeIds[nextIndex]);
  }

  public Edge<T> getOutEdge(final int i) {
    final int edgeId = this.outEdgeIds[i];
    return this.graph.getEdge(edgeId);
  }

  public int getOutEdgeCount() {
    return this.outEdgeIds.length;
  }

  public int getOutEdgeIndex(final Edge<T> edge) {
    return getOutEdges().indexOf(edge);
  }

  public List<Edge<T>> getOutEdges() {
    final Graph<T> graph = getGraph();
    return graph.getEdges(this.outEdgeIds);
  }

  public List<Edge<T>> getOutEdgesTo(final Node<T> node) {
    final List<Edge<T>> edges = new ArrayList<>();
    for (final Edge<T> edge : getOutEdges()) {
      if (edge.getToNode() == node) {
        edges.add(edge);
      }
    }
    return edges;
  }

  public <T2 extends T> List<T2> getOutObjects() {
    if (this.outEdgeIds.length == 0) {
      return Collections.emptyList();
    } else {
      final Graph<T> graph = getGraph();
      return graph.getEdgeObjects(this.outEdgeIds);
    }
  }

  @Override
  public Point getPoint() {
    final Graph<T> graph = getGraph();
    final GeometryFactory geometryFactory = graph.getGeometryFactory();
    return geometryFactory.point(this);
  }

  @Override
  public MapEx getProperties() {
    if (this.graph == null) {
      return MapEx.EMPTY;
    } else {
      final Map<Integer, MapEx> propertiesById = this.graph.getNodePropertiesById();
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
      final Map<Integer, MapEx> propertiesById = this.graph.getNodePropertiesById();
      final Map<String, Object> properties = propertiesById.get(this.id);
      return ObjectWithProperties.getProperty(this, properties, name);
    }
    return null;
  }

  public boolean hasEdge(final Edge<T> edge) {
    if (edge.getGraph() == getGraph()) {
      final int edgeId = edge.getId();
      for (final int inEdgeId : this.inEdgeIds) {
        if (inEdgeId == edgeId) {
          return true;
        }
      }
      for (final int inEdgeId : this.outEdgeIds) {
        if (inEdgeId == edgeId) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean hasEdges() {
    if (isRemoved()) {
      return false;
    } else {
      return !getEdges().isEmpty();
    }
  }

  public boolean hasEdgeTo(final Node<T> node) {
    if (node == this) {
      return false;
    } else {
      for (final Edge<T> edge : getEdges()) {
        if (edge.hasNode(node)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Integer.hashCode(this.id);
  }

  public boolean isRemoved() {
    return this.graph == null;
  }

  public void moveNode(final double x, final double y) {
    moveNode(new PointDoubleXY(x, y));
  }

  public boolean moveNode(final Point point) {
    if (isRemoved()) {
      return false;
    } else {
      final Node<T> newNode = this.graph.getNode(point);
      if (equals(newNode)) {
        return false;
      } else {
        this.graph.nodeMoved(this, newNode);
        final Set<Edge<T>> edges = new HashSet<>(getInEdges());
        edges.addAll(getOutEdges());
        for (final Edge<T> edge : edges) {
          if (!edge.isRemoved()) {
            final LineString line = edge.getLineString();
            LineString newLine;
            final int vertexCount = line.getVertexCount();
            final End end = edge.getEnd(this);
            if (end.isFrom()) {
              if (line.equalsVertex(vertexCount - 1, getX(), getY())) {
                // LOOPS
                newLine = line.subLine(newNode, 1, vertexCount - 2, newNode);
              } else {
                newLine = line.subLine(newNode, 1, vertexCount - 1, null);
              }
            } else if (end.isTo()) {
              newLine = line.subLine(null, 0, vertexCount - 1, newNode);
            } else {
              return false;
            }
            this.graph.replaceEdge(edge, newLine);
          }
        }
        return true;
      }
    }
  }

  @Override
  public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    final int graphId = in.readInt();
    this.graph = Graph.getGraph(graphId);
    this.id = in.readInt();
    this.inEdgeIds = (int[])in.readObject();
    this.outEdgeIds = (int[])in.readObject();
    this.x = in.readDouble();
    this.y = in.readDouble();
  }

  void remove() {
    this.graph = null;
    this.inEdgeIds = null;
    this.outEdgeIds = null;
  }

  public void remove(final Edge<T> edge) {
    if (!isRemoved()) {
      this.outEdgeIds = removeEdge(this.outEdgeIds, edge);
      this.inEdgeIds = removeEdge(this.inEdgeIds, edge);
      if (this.inEdgeIds.length == 0 && this.outEdgeIds.length == 0) {
        this.graph.remove(this);
      } else {
        updateAttributes();
      }
    }
  }

  public int[] removeEdge(final int[] oldEdgeIds, final Edge<T> edge) {
    final Graph<T> graph = getGraph();
    final List<Edge<T>> edges = graph.getEdges(oldEdgeIds);
    edges.remove(edge);
    return graph.getEdgeIds(edges);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Node: ");
    sb.append(' ');
    if (isRemoved()) {
      sb.insert(0, "Removed");
    } else {
      sb.append(this.id);
      sb.append('{');
      sb.append(Arrays.toString(this.inEdgeIds));
      sb.append(',');
      sb.append(Arrays.toString(this.outEdgeIds));
      sb.append("}\tPOINT(");
      sb.append(getX());
      sb.append(" ");
      sb.append(getY());
      sb.append(")");
    }
    return sb.toString();

  }

  private void updateAttributes() {
    for (final Object attribute : getProperties().values()) {
      if (attribute instanceof ObjectPropertyProxy) {
        @SuppressWarnings("unchecked")
        final ObjectPropertyProxy<Object, Node<T>> proxy = (ObjectPropertyProxy<Object, Node<T>>)attribute;
        proxy.clearValue();
      }
    }
  }

  @Override
  public void writeExternal(final ObjectOutput out) throws IOException {
    final int graphId = this.graph.getId();
    out.writeInt(graphId);
    out.writeInt(this.id);
    out.writeObject(this.inEdgeIds);
    out.writeObject(this.outEdgeIds);
    out.writeDouble(this.getX());
    out.writeDouble(this.getY());
  }
}
