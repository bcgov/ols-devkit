package com.revolsys.geometry.graph;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.PreDestroy;

import com.revolsys.collection.bplus.BPlusTreeMap;
import com.revolsys.collection.map.IntHashMap;
import com.revolsys.collection.map.MapEx;
import com.revolsys.comparator.ComparatorProxy;
import com.revolsys.geometry.graph.attribute.NodeProperties;
import com.revolsys.geometry.graph.comparator.NodeDistanceComparator;
import com.revolsys.geometry.graph.event.EdgeEvent;
import com.revolsys.geometry.graph.event.EdgeEventListener;
import com.revolsys.geometry.graph.event.EdgeEventListenerList;
import com.revolsys.geometry.graph.event.NodeEvent;
import com.revolsys.geometry.graph.event.NodeEventListener;
import com.revolsys.geometry.graph.event.NodeEventListenerList;
import com.revolsys.geometry.graph.filter.IsPointOnLineEdgeFilter;
import com.revolsys.geometry.graph.visitor.NodeWithinBoundingBoxVisitor;
import com.revolsys.geometry.index.IdObjectIndex;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.model.coordinates.comparator.PointComparators;
import com.revolsys.geometry.model.coordinates.comparator.PointDistanceComparator;
import com.revolsys.geometry.model.impl.LineStringDouble;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.impl.PointDoubleXYZ;
import com.revolsys.io.page.PageValueManager;
import com.revolsys.io.page.SerializablePageValueManager;
import com.revolsys.predicate.PredicateProxy;
import com.revolsys.predicate.Predicates;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.record.Record;
import com.revolsys.util.ExitLoopException;
import com.revolsys.visitor.CreateListVisitor;

public class Graph<T> extends BaseObjectWithProperties implements GeometryFactoryProxy {

  private static final AtomicInteger GRAPH_IDS = new AtomicInteger();

  private static Map<Integer, WeakReference<Graph<?>>> graphs = new HashMap<>();

  @SuppressWarnings("unchecked")
  public static <V> Graph<V> getGraph(final int id) {
    final WeakReference<Graph<?>> reference = graphs.get(id);
    if (reference == null) {
      return null;
    } else {
      final Graph<?> graph = reference.get();
      if (graph == null) {
        graphs.remove(id);
        return null;
      } else {
        return (Graph<V>)graph;
      }
    }
  }

  private final Map<Edge<T>, Integer> edgeIds = new TreeMap<>();

  private IdObjectIndex<Edge<T>> edgeIndex;

  private Map<Integer, LineString> edgeLinesById = new IntHashMap<>();

  private final EdgeEventListenerList<T> edgeListeners = new EdgeEventListenerList<>();

  private Map<Integer, T> edgeObjectsById = new IntHashMap<>();

  private Map<Integer, MapEx> edgePropertiesById = new IntHashMap<>();

  private Map<Integer, Edge<T>> edgesById = new IntHashMap<>();

  private GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;

  private final int id = GRAPH_IDS.incrementAndGet();

  private boolean inMemory = true;

  private int maxEdgesInMemory = Integer.MAX_VALUE;

  private int nextEdgeId;

  private int nextNodeId;

  private IdObjectIndex<Node<T>> nodeIndex;

  private final NodeEventListenerList<T> nodeListeners = new NodeEventListenerList<>();

  private Map<Integer, MapEx> nodePropertiesById = new IntHashMap<>();

  private Map<Integer, Node<T>> nodesById = new IntHashMap<>();

  private Map<Point, Integer> nodesIdsByPoint = new TreeMap<>(PointComparators.leftLowest());

  private GeometryFactory precisionModel = GeometryFactory.DEFAULT_3D;

  private final PointDoubleXY findPoint = new PointDoubleXY();

  public Graph() {
    this(true);
  }

  protected Graph(final boolean storeLines) {
    graphs.put(this.id, new WeakReference<Graph<?>>(this));
    if (!storeLines) {
      this.edgeLinesById = null;
    }
  }

  public void add(final NodeEventListener<T> listener) {
    this.nodeListeners.add(listener);
  }

  protected Edge<T> addEdge(final T object, final double fromX, final double fromY,
    final double toX, final double toY) {
    return addEdge(object, null, fromX, fromY, toX, toY);
  }

  public Edge<T> addEdge(final T object, final LineString line) {
    final double fromX = line.getX(0);
    final double fromY = line.getY(0);
    final int lastVertexIndex = line.getVertexCount() - 1;
    final double toX = line.getX(lastVertexIndex);
    final double toY = line.getY(lastVertexIndex);
    return addEdge(object, line, fromX, fromY, toX, toY);
  }

  /**
   * Actually add the edge.
   *
   * @param object
   * @param line
   * @param from
   * @param to
   * @return
   */
  protected Edge<T> addEdge(final T object, final LineString line, final double fromX,
    final double fromY, final double toX, final double toY) {
    if (this.inMemory && getEdgeCount() >= this.maxEdgesInMemory) {
      this.edgePropertiesById = BPlusTreeMap.newIntSeralizableTempDisk(this.edgePropertiesById);
      // TODO edgIds
      // TODO edgeIndex
      this.edgeLinesById = BPlusTreeMap.newIntSeralizableTempDisk(this.edgeLinesById);
      this.edgeObjectsById = BPlusTreeMap.newIntSeralizableTempDisk(this.edgeObjectsById);
      this.edgesById = BPlusTreeMap.newIntSeralizableTempDisk(this.edgesById);

      // TODO nodeIndex
      this.nodePropertiesById = BPlusTreeMap.newIntSeralizableTempDisk(this.nodePropertiesById);
      this.nodesById = BPlusTreeMap.newIntSeralizableTempDisk(this.nodesById);
      this.nodesIdsByPoint = BPlusTreeMap.newTempDisk(this.nodesIdsByPoint,
        new SerializablePageValueManager<Point>(), PageValueManager.INT);
      this.inMemory = false;
    }
    final Node<T> fromNode = getNode(fromX, fromY);
    final Node<T> toNode = getNode(toX, toY);
    final int edgeId = ++this.nextEdgeId;
    final Edge<T> edge = new Edge<>(edgeId, this, fromNode, toNode);
    if (this.edgeLinesById != null) {
      this.edgeLinesById.put(edgeId, line);
    }
    this.edgeObjectsById.put(edgeId, object);
    this.edgesById.put(edgeId, edge);
    this.edgeIds.put(edge, edgeId);
    if (this.edgeIndex != null) {
      this.edgeIndex.add(edge);
    }
    this.edgeListeners.edgeEvent(edge, null, EdgeEvent.EDGE_ADDED, null);
    return edge;
  }

  protected Edge<T> addEdge(final T object, final Point from, final Point to) {
    final double fromX = from.getX();
    final double fromY = from.getY();
    final double toX = to.getX();
    final double toY = to.getY();
    return addEdge(object, null, fromX, fromY, toX, toY);
  }

  public void addEdgeListener(final EdgeEventListener<T> listener) {
    this.edgeListeners.add(listener);
  }

  public void clear() {
    if (this.edgeIds != null) {
      this.edgeIds.clear();
    }
    if (this.edgePropertiesById != null) {
      this.edgePropertiesById.clear();
    }
    this.edgeIds.clear();
    // TODO edgeIndex
    if (this.edgeLinesById != null) {
      this.edgeLinesById.clear();
    }

    if (this.edgeObjectsById != null) {
      this.edgeObjectsById.clear();
    }
    if (this.edgesById != null) {
      this.edgesById.clear();
    }

    this.nodeIndex.clear();
    if (this.nodePropertiesById != null) {
      this.nodePropertiesById.clear();
    }
    this.nodesIdsByPoint.clear();
  }

  /**
   * Clone the object, setting the line property to the new value.
   *
   * @param object The object to clone.
   * @param line The line.
   * @return The new object.
   */
  @SuppressWarnings("unchecked")
  protected T clone(final T object, final LineString line) {
    if (object != null) {
      try {
        final Class<? extends Object> clazz = object.getClass();
        final Method method = clazz.getMethod("clone", new Class[0]);
        return (T)method.invoke(object, new Object[0]);
      } catch (final Throwable e) {
        throw new IllegalArgumentException("Cannot clone", e);
      }
    } else {
      return null;
    }
  }

  @Override
  @PreDestroy
  public void close() {
    if (this.edgeIds != null) {
      this.edgeIds.clear();
    }
    if (this.edgeIndex != null) {
      this.edgeIndex.clear();
    }
    if (this.edgeLinesById != null) {
      this.edgeLinesById.clear();
    }
    if (this.edgeObjectsById != null) {
      this.edgeObjectsById.clear();
    }
    if (this.edgePropertiesById != null) {
      this.edgePropertiesById.clear();
    }
    if (this.edgesById != null) {
      this.edgesById.clear();
    }

    if (this.nodeIndex != null) {
      this.nodeIndex.clear();
    }
    if (this.nodePropertiesById != null) {
      this.nodePropertiesById.clear();
    }
    if (this.nodesById != null) {
      this.nodesById.clear();
    }
    if (this.nodesIdsByPoint != null) {
      this.nodesIdsByPoint.clear();
    }
  }

  public boolean contains(final Edge<T> edge) {
    if (edge.getGraph() == this) {
      final int id = edge.getId();
      return this.edgesById.containsKey(id);
    }
    return false;
  }

  public void deleteEdges(final Predicate<Edge<T>> filter) {
    forEachEdge(filter, (edge) -> remove(edge));
  }

  public Iterable<Edge<T>> edges() {
    return getEdges();
  }

  protected void evict(final Edge<T> edge) {
    // TODO
  }

  protected void evict(final Node<T> node) {
    // TODO
  }

  public Node<T> findNode(final double x, final double y) {
    synchronized (this.findPoint) {
      this.findPoint.setX(x);
      this.findPoint.setY(y);
      return findNode(this.findPoint);
    }

  }

  /**
   * Find the node by point coordinates returning the node if it exists, null
   * otherwise.
   *
   * @param point The point coordinates to find the node for.
   * @return The nod or null if not found.
   */
  public Node<T> findNode(final Point point) {
    final Integer nodeId = this.nodesIdsByPoint.get(point);
    if (nodeId == null) {
      return null;
    } else {
      return getNode(nodeId);
    }
  }

  public List<Node<T>> findNodes(BoundingBox boundingBox) {
    boundingBox = boundingBox.bboxEdit(editor -> editor.setGeometryFactory(getGeometryFactory()));
    return NodeWithinBoundingBoxVisitor.getNodes(this, boundingBox);
  }

  /**
   * Find all the nodes <= the distance of the edge, that are "on" the line.
   *
   * @param edge The edge.
   * @param distance The distance.
   * @return The nodes
   * @see IsPointOnLineEdgeFilter
   */
  public List<Node<T>> findNodes(final Edge<T> edge, final double distance) {
    final IsPointOnLineEdgeFilter<T> filter = new IsPointOnLineEdgeFilter<>(edge, distance);

    final Node<T> fromNode = edge.getFromNode();

    final Comparator<Node<T>> comparator = new NodeDistanceComparator<>(fromNode);

    final BoundingBox boundingBox = filter.getEnvelope();
    return getNodes(boundingBox, filter, comparator);

  }

  public List<Node<T>> findNodesOfDegree(final int degree) {
    final List<Node<T>> nodesFound = new ArrayList<>();
    for (final Node<T> node : getNodes()) {
      if (node.getDegree() == degree) {
        nodesFound.add(node);
      }
    }
    return nodesFound;
  }

  public void forEachEdge(final BoundingBoxProxy boundingBoxProxy, final Consumer<Edge<T>> action) {
    final IdObjectIndex<Edge<T>> edgeIndex = getEdgeIndex();
    edgeIndex.forEach(boundingBoxProxy.getBoundingBox(), action);
  }

  public void forEachEdge(final BoundingBoxProxy boundingBox,
    final Predicate<? super Edge<T>> filter, final Consumer<Edge<T>> visitor) {
    final IdObjectIndex<Edge<T>> edgeIndex = getEdgeIndex();
    edgeIndex.forEach(boundingBox.getBoundingBox(), filter, visitor);
  }

  public void forEachEdge(final Comparator<Edge<T>> comparator, final Consumer<Edge<T>> action) {
    forEachEdge(null, action, comparator);
  }

  @SuppressWarnings("unchecked")
  public void forEachEdge(final Consumer<Edge<T>> action) {
    Predicate<Edge<T>> filter = null;
    if (action instanceof PredicateProxy) {
      filter = ((PredicateProxy<Edge<T>>)action).getPredicate();
    }
    Comparator<Edge<T>> comparator = null;
    if (action instanceof ComparatorProxy) {
      comparator = ((ComparatorProxy<Edge<T>>)action).getComparator();
    }
    forEachEdge(filter, action, comparator);
  }

  public void forEachEdge(final Predicate<Edge<T>> filter, final Consumer<Edge<T>> action) {
    forEachEdge(filter, action, null);
  }

  public void forEachEdge(final Predicate<Edge<T>> filter, final Consumer<Edge<T>> action,
    final Comparator<Edge<T>> comparator) {
    final LinkedList<Edge<T>> edges = new LinkedList<>(getEdges(filter));
    final EdgeEventListener<T> listener;

    if (comparator == null) {
      listener = (edgeEvent) -> {
        final Edge<T> edge = edgeEvent.getEdge();
        if (edgeEvent.isAddAction()) {
          if (filter == null || filter.test(edge)) {
            edges.addFirst(edge);
          }
        }
      };
    } else {
      Collections.sort(edges, comparator);
      listener = (edgeEvent) -> {
        final Edge<T> edge = edgeEvent.getEdge();
        final String eventAction = edgeEvent.getAction();
        if (eventAction.equals(EdgeEvent.EDGE_ADDED)) {
          if (filter == null || filter.test(edge)) {
            edges.addFirst(edge);
          }
          if (comparator != null) {
            Collections.sort(edges, comparator);
          }
        } else if (eventAction.equals(EdgeEvent.EDGE_REMOVED)) {
          if (comparator != null) {
            edges.remove(edge);
          }
        }
      };
    }

    this.edgeListeners.add(listener);
    try {
      while (!edges.isEmpty()) {
        final Edge<T> edge = edges.remove(0);
        if (!edge.isRemoved()) {
          action.accept(edge);
        }
      }
    } catch (final ExitLoopException e) {
    } finally {
      this.edgeListeners.remove(listener);
    }
  }

  public void forEachNode(final BoundingBoxProxy boundingBoxProxy, final Consumer<Node<T>> action) {
    final IdObjectIndex<Node<T>> nodeIndex = getNodeIndex();
    final BoundingBox boundingBox = boundingBoxProxy.getBoundingBox();
    nodeIndex.forEach(boundingBox, action);
  }

  @SuppressWarnings("unchecked")
  public void forEachNode(final Consumer<Node<T>> action) {
    Predicate<Node<T>> filter = null;
    if (action instanceof PredicateProxy) {
      filter = ((PredicateProxy<Node<T>>)action).getPredicate();
    }
    Comparator<Node<T>> comparator = null;
    if (action instanceof ComparatorProxy) {
      comparator = ((ComparatorProxy<Node<T>>)action).getComparator();
    }
    forEachNode(action, filter, comparator);
  }

  public void forEachNode(final Consumer<Node<T>> action, final Comparator<Node<T>> comparator) {
    forEachNode(action, null, comparator);
  }

  public void forEachNode(final Consumer<Node<T>> action, final Predicate<Node<T>> filter) {
    forEachNode(action, filter, null);
  }

  // TODO make this work with cached nodes
  public void forEachNode(final Consumer<Node<T>> action, final Predicate<Node<T>> filter,
    final Comparator<Node<T>> comparator) {
    final List<Node<T>> nodes = new LinkedList<>();
    if (filter == null) {
      nodes.addAll(getNodes());
    } else {
      for (final Node<T> node : getNodes()) {
        if (filter.test(node)) {
          nodes.add(node);
        }
      }
    }
    NodeEventListener<T> listener;
    if (comparator == null) {
      listener = (nodeEvent) -> {
        if (nodeEvent.isAddAction()) {
          final Node<T> node = nodeEvent.getNode();
          if (filter == null || filter.test(node)) {
            nodes.add(node);
          }
        }
      };
    } else {
      Collections.sort(nodes, comparator);
      listener = (nodeEvent) -> {
        final Node<T> node = nodeEvent.getNode();
        final String eventAction = nodeEvent.getAction();
        if (eventAction.equals(NodeEvent.NODE_ADDED)) {
          if (filter == null || filter.test(node)) {
            nodes.add(node);
          }
          if (comparator != null) {
            Collections.sort(nodes, comparator);
          }
        } else if (eventAction.equals(NodeEvent.NODE_REMOVED)) {
          nodes.remove(node);
        }
      };
    }
    this.nodeListeners.add(listener);

    try {
      while (!nodes.isEmpty()) {
        final Node<T> node = nodes.remove(0);
        if (!node.isRemoved()) {
          action.accept(node);
        }
      }
    } catch (final ExitLoopException e) {
    } finally {
      this.nodeListeners.remove(listener);
    }
  }

  public void forEachObject(final Consumer<T> action) {
    forEachEdge(edge -> action.accept(edge.getObject()));
  }

  @Override
  public int getAxisCount() {
    return this.geometryFactory.getAxisCount();
  }

  public double getClosestDistance(final Node<T> node, final double maxDistance) {
    final List<Node<T>> nodes = getNodes(node, maxDistance);
    double closestDistance = Double.MAX_VALUE;
    for (final Node<T> matchNode : nodes) {
      if (matchNode != node) {
        final double distance = node.distancePoint(matchNode);
        if (distance < closestDistance) {
          closestDistance = distance;
        }
      }
    }
    return closestDistance;
  }

  public Edge<T> getEdge(final int edgeId) {
    return this.edgesById.get(edgeId);
  }

  public Edge<T> getEdge(final T object, final LineString line) {
    if (object != null) {
      final Point fromPoint = line.getPoint(0);
      final Node<T> fromNode = findNode(fromPoint);
      if (fromNode != null) {
        for (final Edge<T> edge : fromNode.getEdges()) {
          if (edge.getObject() == object) {
            return edge;
          }
        }
      }
    }
    return null;
  }

  public int getEdgeCount() {
    return this.edgesById.size();
  }

  public Collection<Integer> getEdgeIds() {
    return this.edgesById.keySet();
  }

  public int[] getEdgeIds(final Collection<Edge<T>> edges) {
    final int[] edgeIds = new int[edges.size()];
    int i = 0;
    for (final Edge<T> edge : edges) {
      edgeIds[i] = edge.getId();
      i++;
    }
    return edgeIds;
  }

  public IdObjectIndex<Edge<T>> getEdgeIndex() {
    if (this.edgeIndex == null) {
      this.edgeIndex = new EdgeQuadTree<>(this);
    }
    return this.edgeIndex;
  }

  public LineString getEdgeLine(final int edgeId) {
    return this.edgeLinesById.get(edgeId);
  }

  public List<LineString> getEdgeLines() {
    final List<Integer> edgeIds = new ArrayList<>(this.edgesById.keySet());
    return new EdgeLineList(this, edgeIds);
  }

  @SuppressWarnings("unchecked")
  public T getEdgeObject(final int edgeId) {
    return this.edgeObjectsById.get(edgeId);
  }

  public List<T> getEdgeObjects() {
    final List<T> objects = new ArrayList<>();
    for (final Edge<T> edge : getEdges()) {
      final T object = edge.getObject();
      objects.add(object);
    }
    return objects;
  }

  @SuppressWarnings("unchecked")
  public <T2 extends T> List<T2> getEdgeObjects(final int... ids) {
    final List<T2> objects = new ArrayList<>();
    for (final int edgeId : ids) {
      final T object = getEdgeObject(edgeId);
      if (object != null) {
        objects.add((T2)object);
      }
    }
    return objects;
  }

  protected Map<Integer, MapEx> getEdgePropertiesById() {
    return this.edgePropertiesById;
  }

  public List<Edge<T>> getEdges() {
    final List<Integer> edgeIds = new ArrayList<>(this.edgesById.keySet());
    return new EdgeList<>(this, edgeIds);
  }

  public List<Edge<T>> getEdges(final BoundingBoxProxy boundingBoxProxy) {
    return BoundingBox.newArraySorted(this::forEachEdge, boundingBoxProxy);
  }

  public List<Edge<T>> getEdges(final BoundingBoxProxy boundingBoxProxy,
    final Predicate<? super Edge<T>> filter) {
    return BoundingBox.newArraySorted(this::forEachEdge, boundingBoxProxy, filter);
  }

  public List<Edge<T>> getEdges(final BoundingBoxProxy boundingBoxProxy,
    final Predicate<? super Edge<T>> filter, final Comparator<Edge<T>> comparator) {
    return BoundingBox.newArraySorted(this::forEachEdge, boundingBoxProxy, filter, comparator);

  }

  public List<Edge<T>> getEdges(final Geometry geometry, final double maxDistance) {
    if (geometry == null) {
      return Collections.emptyList();
    } else {
      final BoundingBox boundingBox = geometry.getBoundingBox() //
        .bboxEditor() //
        .expandDelta(maxDistance);
      final Predicate<Edge<T>> filter = (edge) -> {
        final LineString line = edge.getLineString();
        final double distance = line.distanceGeometry(geometry);
        if (distance <= maxDistance) {
          return true;
        } else {
          return false;
        }
      };
      return BoundingBox.newArraySorted(this::forEachEdge, boundingBox, filter);
    }

  }

  public List<Edge<T>> getEdges(final int... ids) {
    final List<Edge<T>> edges = new ArrayList<>();
    for (final int edgeId : ids) {
      final Edge<T> edge = getEdge(edgeId);
      if (edge != null) {
        edges.add(edge);
      }
    }
    return edges;
  }

  public List<Edge<T>> getEdges(final List<Integer> ids) {
    final List<Edge<T>> edges = new ArrayList<>();
    for (final int edgeId : ids) {
      final Edge<T> edge = getEdge(edgeId);
      if (edge != null) {
        edges.add(edge);
      }
    }
    return edges;
  }

  public List<Edge<T>> getEdges(final Point point, final double maxDistance) {
    if (point == null) {
      return Collections.emptyList();
    } else {
      final BoundingBox boundingBox = point.getBoundingBox() //
        .bboxEditor() //
        .expandDelta(maxDistance);
      final double x = point.getX();
      final double y = point.getY();
      final Predicate<Edge<T>> filter = (edge) -> {
        final LineString line = edge.getLineString();
        final double distance = line.distancePoint(x, y);
        if (distance <= maxDistance) {
          return true;
        } else {
          return false;
        }
      };
      return BoundingBox.newArraySorted(this::forEachEdge, boundingBox, filter);
    }

  }

  public List<Edge<T>> getEdges(final Predicate<Edge<T>> filter) {
    final List<Edge<T>> edges = new EdgeList<>(this);
    for (final Integer edgeId : getEdgeIds()) {
      final Edge<T> edge = getEdge(edgeId);
      if (Predicates.matches(filter, edge)) {
        edges.add(edge);
      }
    }
    return edges;
  }

  public List<Edge<T>> getEdges(final Predicate<Edge<T>> filter,
    final Comparator<Edge<T>> comparator) {
    final List<Edge<T>> targetEdges = getEdges(filter);
    if (comparator != null) {
      Collections.sort(targetEdges, comparator);
    }
    return targetEdges;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public int getId() {
    return this.id;
  }

  public int getMaxEdgesInMemory() {
    return this.maxEdgesInMemory;
  }

  /**
   * Get the node by point coordinates, creating one if it did not exist.
   *
   * @param point The point coordinates to get the node for.
   * @return The node.
   */
  public Node<T> getNode(final double x, final double y) {
    final PointDoubleXY point = new PointDoubleXY(x, y);
    Node<T> node = findNode(point);
    if (node == null) {
      final int nodeId = ++this.nextNodeId;
      node = new Node<>(nodeId, this, x, y);
      this.nodesIdsByPoint.put(point, nodeId);
      this.nodesById.put(nodeId, node);
      if (this.nodeIndex != null) {
        this.nodeIndex.add(node);
      }
      this.nodeListeners.nodeEvent(node, null, null, NodeEvent.NODE_ADDED, null);
    }
    return node;
  }

  public Node<T> getNode(final int nodeId) {
    return this.nodesById.get(nodeId);
  }

  /**
   * Get the node by point coordinates, creating one if it did not exist.
   *
   * @param point The point coordinates to get the node for.
   * @return The node.
   */
  public Node<T> getNode(final Point point) {
    Node<T> node = findNode(point);
    if (node == null) {
      final int nodeId = ++this.nextNodeId;
      node = new Node<>(nodeId, this, point.getX(), point.getY());
      this.nodesIdsByPoint.put(node.newPoint2D(), nodeId);
      this.nodesById.put(nodeId, node);
      if (this.nodeIndex != null) {
        this.nodeIndex.add(node);
      }
      this.nodeListeners.nodeEvent(node, null, null, NodeEvent.NODE_ADDED, null);
    }
    return node;
  }

  public int getNodeCount() {
    return this.nodesById.size();
  }

  public Collection<Integer> getNodeIds() {
    return this.nodesById.keySet();
  }

  public IdObjectIndex<Node<T>> getNodeIndex() {
    if (this.nodeIndex == null) {
      this.nodeIndex = new NodeQuadTree<>(this);
    }
    return this.nodeIndex;
  }

  protected Map<Integer, MapEx> getNodePropertiesById() {
    return this.nodePropertiesById;
  }

  public List<Node<T>> getNodes() {
    final List<Integer> nodeIds = new ArrayList<>(this.nodesIdsByPoint.values());
    final NodeList<T> nodeList = new NodeList<>(this, nodeIds);
    nodeList.sort(PointComparators.leftLowest());
    return nodeList;
  }

  public List<Node<T>> getNodes(final BoundingBox boundingBox, final Predicate<Node<T>> filter) {
    return getNodes(boundingBox, filter, null);
  }

  public List<Node<T>> getNodes(final BoundingBox boundingBox, final Predicate<Node<T>> filter,
    final Comparator<Node<T>> comparator) {
    final CreateListVisitor<Node<T>> results = new CreateListVisitor<>(filter);
    final IdObjectIndex<Node<T>> nodeIndex = getNodeIndex();
    nodeIndex.forEach(boundingBox, results);
    final List<Node<T>> nodes = results.getList();
    if (comparator == null) {
      Collections.sort(nodes);
    } else {
      Collections.sort(nodes, comparator);
    }
    return nodes;

  }

  public List<Node<T>> getNodes(final Comparator<Node<T>> comparator) {
    final List<Node<T>> targetNodes = getNodes();
    if (comparator != null) {
      Collections.sort(targetNodes, comparator);
    }
    return targetNodes;
  }

  /**
   * Find the nodes <= the distance of the specified geometry.
   *
   * @param geometry The geometry.
   * @param maxDistance The maximum distance.
   * @return The list of nodes.
   */
  public List<Node<T>> getNodes(final Geometry geometry, final double maxDistance) {
    final BoundingBox boundingBox = geometry.getBoundingBox() //
      .bboxEditor() //
      .expandDelta(maxDistance);
    final IdObjectIndex<Node<T>> nodeIndex = getNodeIndex();
    final Predicate<? super Node<T>> filter = (node) -> {
      final double distance = geometry.distancePoint(node);
      return distance <= maxDistance;
    };
    return BoundingBox.newArraySorted(nodeIndex::forEach, boundingBox, filter);
  }

  public List<Node<T>> getNodes(final List<Integer> nodeIds) {
    final List<Node<T>> nodes = new ArrayList<>();
    for (final int nodeId : nodeIds) {
      final Node<T> node = getNode(nodeId);
      if (node != null) {
        nodes.add(node);
      }
    }
    return nodes;

  }

  public List<Node<T>> getNodes(final Predicate<Node<T>> filter) {
    if (filter == null) {
      return getNodes();
    } else {
      final List<Node<T>> filteredNodes = Predicates.filter(getNodes(), filter);
      return filteredNodes;
    }
  }

  public List<Node<T>> getNodes(final Predicate<Node<T>> filter,
    final Comparator<Node<T>> comparator) {
    final List<Node<T>> targetNodes = getNodes(filter);
    if (comparator != null) {
      Collections.sort(targetNodes, comparator);
    }
    return targetNodes;
  }

  public List<Node<T>> getNodes(final Predicate<Node<T>> filter, final Geometry geometry,
    final double maxDistance) {
    final BoundingBox boundingBox = geometry.getBoundingBox() //
      .bboxEditor() //
      .expandDelta(maxDistance);
    final Predicate<Node<T>> distanceFilter = (node) -> {
      return filter.test(node) && node.distanceGeometry(geometry) <= maxDistance;
    };
    return getNodes(boundingBox, distanceFilter, null);
  }

  public List<T> getObjects() {
    final List<T> objects = new ArrayList<>();
    for (final Edge<T> edge : getEdges()) {
      if (edge != null && !edge.isRemoved()) {
        final T object = edge.getObject();
        objects.add(object);
      }
    }
    return objects;
  }

  public GeometryFactory getPrecisionModel() {
    return this.precisionModel;
  }

  /**
   * Get the type name for the edge.
   *
   * @param edge The edge.
   * @return The type name.
   */
  public String getTypeName(final Edge<T> edge) {
    final Object object = edge.getObject();
    if (object == null) {
      return null;
    } else {
      final String className = edge.getClass().getName();
      return className;
    }
  }

  public boolean hasEdge(final Edge<T> edge) {
    final Node<T> fromNode = edge.getFromNode();
    final Node<T> toNode = edge.getToNode();
    return hasEdgeBetween(fromNode, toNode);
  }

  public boolean hasEdgeBetween(final Point fromPoint, final Point toPoint) {
    final Node<T> fromNode = findNode(fromPoint);
    if (fromNode == null) {
      return false;
    } else {
      final Node<T> toNode = findNode(toPoint);
      if (toNode == null) {
        return false;
      } else {
        return fromNode.hasEdgeTo(toNode);
      }
    }
  }

  public boolean isEmpty() {
    return this.edgesById.isEmpty();
  }

  /**
   * Merge the two edges at the node.
   *
   * @param node
   * @param edge1
   * @param edge2
   */
  public Edge<T> merge(final Node<T> node, final Edge<T> edge1, final Edge<T> edge2) {
    if (edge1 != edge2 && edge1.hasNode(node) && edge2.hasNode(node)) {
      final Map<String, Object> attributes1 = edge1.getProperties();
      final Map<String, Object> attributes2 = edge2.getProperties();
      final T object1 = edge1.getObject();
      final LineString line1 = edge1.getLineString();
      final LineString line2 = edge2.getLineString();

      final LineString newLine = line1.merge(node, line2);

      final T mergedObject = clone(object1, newLine);
      final Edge<T> newEdge = addEdge(mergedObject, newLine);
      newEdge.setProperties(attributes2);
      newEdge.setProperties(attributes1);
      remove(edge1);
      remove(edge2);
      return newEdge;
    } else {
      return null;
    }

  }

  /**
   * Merge the two edges into a single edge, removing the old edges and node if
   * required from the graph and adding a new edge to the graph.
   *
   * @param reversedEdges The list of edges that need to be reversed.
   * @param node The node to remove.
   * @param edge1 The first edge to merge.
   * @param edge2 The second edge to merge.
   * @return The new edge.
   */
  public Edge<T> mergeEdges(final Edge<T> edge1, final Edge<T> edge2) {
    final LineString line1 = edge1.getLineString();
    final LineString line2 = edge2.getLineString();

    final LineString newLine = line1.merge(line2);
    final Edge<T> newEdge = replaceEdge(edge1, newLine);
    remove(edge2);
    return newEdge;
  }

  public void moveNode(final String typePath, final Node<Record> fromNode,
    final Node<Record> toNode, final Point newPoint) {
    if (!fromNode.isRemoved() && !toNode.isRemoved()) {
      if (!fromNode.equals(toNode)) {
        final List<Edge<Record>> edges = NodeProperties.getEdgesByType(fromNode, typePath);

        for (final Edge<Record> edge : edges) {
          if (!edge.isRemoved()) {
            final LineString line = edge.getLineString();
            LineString newLine;
            if (line.getPoint().equals(fromNode)) {
              newLine = line.subLine(newPoint, 1, line.getVertexCount() - 1, null);
            } else {
              newLine = line.subLine(null, 0, line.getVertexCount() - 1, newPoint);
            }
            final Graph<Record> graph = edge.getGraph();
            graph.replaceEdge(edge, newLine);
          }
        }
      }
    }
  }

  public boolean moveNodesToMidpoint(final String typePath, final Node<Record> node1,
    final Node<Record> node2) {
    final Point point1 = node1.get3dCoordinates(typePath);
    final Point point2 = node2.get3dCoordinates(typePath);

    final Graph<Record> graph = node1.getGraph();
    final Point midPoint = LineSegmentUtil.midPoint(GeometryFactory.fixed3d(0, 1000.0, 1000.0, 1.0),
      node2, node1);
    final double x = midPoint.getX();
    final double y = midPoint.getY();
    final double z1 = point1.getZ();
    final double z2 = point2.getZ();
    double z;
    if (z1 == 0 || !Double.isFinite(z1)) {
      z = z2;
    } else if (z2 == 0 || !Double.isFinite(z1)) {
      z = z1;
    } else {
      z = Double.NaN;
    }
    final Point newPoint = new PointDoubleXYZ(x, y, z);
    final Node<Record> newNode = graph.getNode(midPoint);
    if (!Node.hasEdgesBetween(typePath, node1, newNode)
      && !Node.hasEdgesBetween(typePath, node2, newNode)) {
      if (node1.equals(2, newNode)) {
        moveNode(typePath, node2, node1, newPoint);
      } else if (node2.equals(2, newNode)) {
        moveNode(typePath, node1, node2, newPoint);
      } else {
        moveNode(typePath, node1, newNode, newPoint);
        moveNode(typePath, node2, newNode, newPoint);
      }
      return true;
    } else {
      return false;
    }
  }

  public boolean movePointsWithinTolerance(final Map<Point, Point> movedNodes,
    final double maxDistance, final Node<T> node1) {
    final Graph<T> graph1 = node1.getGraph();
    List<Node<T>> nodes2 = getNodes(node1, maxDistance);
    if (nodes2.isEmpty()) {
      nodes2 = getNodes(node1, maxDistance * 2);
      if (nodes2.size() == 1) {
        final Node<T> node2 = nodes2.get(0);
        if (graph1.findNode(node2) == null) {
          final List<Edge<T>> inEdges = node2.getInEdges();
          final List<Edge<T>> outEdges = node2.getOutEdges();
          if (inEdges.size() == 1 && outEdges.size() == 1) {
            final Edge<T> inEdge = inEdges.get(0);
            if (inEdge.distancePoint(node1) < maxDistance) {
              moveToMidpoint(movedNodes, graph1, node1, node2);
              return true;
            }
            final Edge<T> outEdge = outEdges.get(0);
            if (outEdge.distancePoint(node1) < maxDistance) {
              moveToMidpoint(movedNodes, graph1, node1, node2);
              return true;
            }
          }
        }
      }
    } else if (nodes2.size() == 1) {
      final Node<T> node2 = nodes2.get(0);
      if (graph1.findNode(node2) == null) {
        moveToMidpoint(movedNodes, graph1, node1, node2);
      }
    }
    return true;
  }

  public void moveToMidpoint(final Map<Point, Point> movedNodes, final Graph<T> graph1,
    final Node<T> node1, final Node<T> node2) {
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

  public Edge<T> newEdge(final GeometryFactory geometryFactory, final T object,
    final LineString points) {
    final LineString newLine = geometryFactory.lineString(points);
    final T newObject = clone(object, newLine);
    final Edge<T> newEdge = addEdge(newObject, newLine);
    return newEdge;
  }

  public void nodeMoved(final Node<T> node, final Node<T> newNode) {
  }

  public Iterable<Node<T>> nodes() {
    return getNodes();
  }

  public void queryEdges(final EdgeVisitor<T> visitor) {
    final BoundingBox env = visitor.getEnvelope();
    final IdObjectIndex<Edge<T>> index = getEdgeIndex();
    index.forEach(env, visitor);
  }

  public void queryEdges(final EdgeVisitor<T> visitor, final Consumer<Edge<T>> matchVisitor) {
    visitor.setAction(matchVisitor);
    queryEdges(visitor);
  }

  public void remove(final Edge<T> edge) {
    if (!edge.isRemoved()) {
      this.edgeListeners.edgeEvent(edge, null, EdgeEvent.EDGE_REMOVED, null);
      final int edgeId = edge.getId();
      this.edgeIds.remove(edge);
      this.edgesById.remove(edgeId);
      this.edgePropertiesById.remove(edgeId);
      if (this.edgeLinesById != null) {
        this.edgeLinesById.remove(edgeId);
      }
      this.edgeObjectsById.remove(edgeId);
      if (this.edgeIndex != null) {
        this.edgeIndex.remove(edge);
      }
      edge.removeInternal();
    }
  }

  public void remove(final EdgeEventListener<T> listener) {
    this.edgeListeners.remove(listener);
  }

  public void remove(final Node<T> node) {
    if (!node.isRemoved()) {
      final ArrayList<Edge<T>> edges = new ArrayList<>(node.getEdges());
      for (final Edge<T> edge : edges) {
        remove(edge);
      }
      this.nodeListeners.nodeEvent(node, null, null, NodeEvent.NODE_REMOVED, null);
      final int nodeId = node.getId();
      this.nodesById.remove(nodeId);
      this.nodePropertiesById.remove(nodeId);
      this.nodesIdsByPoint.remove(node);
      if (this.nodeIndex != null) {
        this.nodeIndex.remove(node);
      }

      node.remove();
    }
  }

  public void remove(final NodeEventListener<T> listener) {
    this.nodeListeners.remove(listener);
  }

  public List<Edge<T>> replaceEdge(final Edge<T> edge, final Geometry lines) {
    if (!edge.isRemoved()) {
      final List<Edge<T>> edges = new ArrayList<>();
      final T object = edge.getObject();
      remove(edge);
      for (int i = 0; i < lines.getGeometryCount(); i++) {
        final LineString line = (LineString)lines.getGeometry(i);
        final T newObject = clone(object, line);
        final Edge<T> newEdge = addEdge(newObject, line);
        edges.add(newEdge);
      }
      return edges;
    } else {
      return Collections.emptyList();
    }
  }

  public Edge<T> replaceEdge(final Edge<T> edge, final LineString line) {
    if (!edge.isRemoved()) {
      final T object = edge.getObject();
      final T newObject = clone(object, line);
      final Edge<T> newEdge = addEdge(newObject, line);
      remove(edge);
      return newEdge;
    } else {
      return null;
    }
  }

  public List<Edge<T>> replaceEdge(final Edge<T> edge, final List<LineString> lines) {
    if (!edge.isRemoved()) {
      final List<Edge<T>> edges = new ArrayList<>();
      final T object = edge.getObject();
      remove(edge);
      for (final LineString line : lines) {
        final T newObject = clone(object, line);
        final Edge<T> newEdge = addEdge(newObject, line);
        edges.add(newEdge);
      }
      return edges;
    } else {
      return Collections.emptyList();
    }
  }

  public void setEdgeObject(final int edgeId, final T object) {
    this.edgeObjectsById.put(edgeId, object);
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    setPrecisionModel(geometryFactory);
  }

  public void setMaxEdgesInMemory(final int maxEdgesInMemory) {
    this.maxEdgesInMemory = maxEdgesInMemory;
  }

  public void setPrecisionModel(final GeometryFactory precisionModel) {
    this.precisionModel = precisionModel;
  }

  public <V extends Point> List<Edge<T>> splitEdge(final Edge<T> edge, final Collection<V> nodes) {
    return splitEdge(edge, nodes, 0.0);
  }

  public <V extends Point> List<Edge<T>> splitEdge(final Edge<T> edge,
    final Collection<V> splitPoints, final double maxDistance) {
    final Collection<V> nodes = new ArrayList<>(splitPoints);
    if (edge.isRemoved()) {
      return Collections.emptyList();
    } else {
      final LineString line = edge.getLineString();
      final LineString points = line;
      final Set<Integer> splitVertices = new TreeSet<>();
      final Set<Integer> splitIndexes = new TreeSet<>();

      for (final Iterator<V> nodeIter = nodes.iterator(); nodeIter.hasNext();) {
        final Point node = nodeIter.next();
        final double distance = points.distanceVertex(0, node);
        if (distance < maxDistance) {
          nodeIter.remove();
        }
      }
      final Map<Point, Double> nodeDistanceMap = new HashMap<>();
      final Map<Point, Integer> nodeSegment = new HashMap<>();

      for (int i = 1; i < points.getVertexCount() && !nodes.isEmpty(); i++) {
        for (final Iterator<V> nodeIter = nodes.iterator(); nodeIter.hasNext();) {
          final Point node = nodeIter.next();
          final double nodeDistance = points.distanceVertex(i, node);
          if (nodeDistance < maxDistance) {
            if (i < points.getVertexCount() - 1) {
              splitVertices.add(i);
              splitIndexes.add(i);
            }
            nodeDistanceMap.remove(node);
            nodeSegment.remove(node);
            nodeIter.remove();
          } else {
            final int segmentIndex = i - 1;
            final double x = node.getX();
            final double y = node.getY();
            final double x1 = points.getX(segmentIndex);
            final double y1 = points.getY(segmentIndex);
            final double x2 = points.getX(i);
            final double y2 = points.getY(i);
            final double segmentDistance = LineSegmentUtil.distanceLinePoint(x1, y1, x2, y2, x, y);
            if (segmentDistance == 0) {
              nodeDistanceMap.put(node, segmentDistance);
              nodeSegment.put(node, segmentIndex);
              nodeIter.remove();
            } else {
              final double projectionFactor = LineSegmentUtil.projectionFactor(x1, y1, x2, y2, x,
                y);
              if (projectionFactor >= 0.0 && projectionFactor <= 1.0) {
                final Double closestDistance = nodeDistanceMap.get(node);
                if (closestDistance == null) {
                  nodeSegment.put(node, segmentIndex);
                  nodeDistanceMap.put(node, segmentDistance);
                } else if (closestDistance.compareTo(segmentDistance) > 0) {
                  nodeSegment.put(node, segmentIndex);
                  nodeDistanceMap.put(node, segmentDistance);
                }
              }
            }
          }
        }
      }
      final T object = edge.getObject();
      final GeometryFactory geometryFactory = line.getGeometryFactory();
      final Map<Integer, Set<Point>> segmentSplitNodes = new TreeMap<>();
      for (final Entry<Point, Integer> entry : nodeSegment.entrySet()) {
        final Point node = entry.getKey();
        final Integer index = entry.getValue();
        Set<Point> splitNodes = segmentSplitNodes.get(index);
        if (splitNodes == null) {
          final double x = points.getX(index);
          final double y = points.getY(index);
          splitNodes = new TreeSet<>(new PointDistanceComparator(x, y));
          segmentSplitNodes.put(index, splitNodes);
          splitIndexes.add(index);
        }
        splitNodes.add(node);
        nodes.remove(node);
      }
      if (nodes.isEmpty()) {
        final List<LineString> newLines = new ArrayList<>();
        int startIndex = 0;
        Point startPoint = null;
        for (final Integer index : splitIndexes) {
          if (splitVertices.contains(index)) {
            final LineString newPoints = points.subLine(startPoint, startIndex,
              index - startIndex + 1, null);
            newLines.add(newPoints);
            startPoint = null;
            startIndex = index;
          }
          final Set<Point> splitNodes = segmentSplitNodes.get(index);
          if (splitNodes != null) {
            for (final Point splitPoint : splitNodes) {
              final Node<T> node = getNode(splitPoint);
              final String typePath = edge.getTypeName();
              Point point = splitPoint;
              double splitPointZ = splitPoint.getZ();
              if (splitPointZ == 0 || Double.isNaN(splitPointZ)) {
                if (splitPoint instanceof Node<?>) {
                  final Node<?> splitNode = (Node<?>)splitPoint;
                  point = splitNode.get3dCoordinates(typePath);
                  splitPointZ = point.getZ();
                }
                if (splitPointZ == 0 || Double.isNaN(splitPointZ)) {
                  point = node.get3dCoordinates(typePath);
                }
                if (splitPointZ == 0 || Double.isNaN(splitPointZ)) {
                  final Point p1 = points.getPoint(index);
                  final Point p2 = points.getPoint(index + 1);
                  final double z = LineSegmentUtil.getElevation(p1, p2, point);
                  point = new PointDoubleXYZ(point.getX(), point.getY(), z);
                }
              }

              final LineString newPoints;
              if (startIndex > index) {
                newPoints = new LineStringDouble(points.getAxisCount(), startPoint, point);
              } else {
                newPoints = points.subLine(startPoint, startIndex, index - startIndex + 1, point);
              }
              newLines.add(newPoints);
              startPoint = point;
              startIndex = index + 1;
            }
          }
        }
        final LineString newPoints = points.subLine(startPoint, startIndex, points.getVertexCount(),
          null);
        newLines.add(newPoints);

        if (newLines.size() > 1) {
          final List<Edge<T>> newEdges = new ArrayList<>();
          for (final LineString edgePoints : newLines) {
            final Edge<T> newEdge = newEdge(geometryFactory, object, edgePoints);
            newEdges.add(newEdge);
          }
          edge.remove();
          return newEdges;
        } else {
          return Collections.singletonList(edge);
        }
      } else {
        return Collections.singletonList(edge);
      }
    }
  }

  public List<Edge<T>> splitEdge(final Edge<T> edge, final Point point) {
    if (!edge.isRemoved()) {
      final LineString line = edge.getLineString();
      final List<LineString> lines = line.split(point);
      if (lines.size() == 1) {
        return Collections.singletonList(edge);
      } else {
        final List<Edge<T>> newEdges = replaceEdge(edge, lines);
        return newEdges;
      }
    } else {
      return Collections.emptyList();
    }
  }

  public List<Edge<T>> splitEdge(final Edge<T> edge, final Point... nodes) {
    return splitEdge(edge, Arrays.asList(nodes));
  }

}
