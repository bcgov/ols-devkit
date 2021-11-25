package com.revolsys.geometry.graph.visitor;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.graph.event.NodeEventListener;
import com.revolsys.geometry.index.IdObjectIndex;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.util.LineStringUtil;

public class SplitCrossingEdgesVisitor<T> extends AbstractEdgeListenerVisitor<T> {

  public static final String CROSSING_EDGES = "Crossing edges";

  private final Graph<T> graph;

  private final SplitEdgesCloseToNodeVisitor<T> splitEdgesCloseToNodeVisitor;

  public SplitCrossingEdgesVisitor(final Graph<T> graph) {
    this.graph = graph;
    this.splitEdgesCloseToNodeVisitor = new SplitEdgesCloseToNodeVisitor<>(graph, CROSSING_EDGES,
      1);
  }

  @Override
  public void accept(final Edge<T> edge) {
    final IdObjectIndex<Edge<T>> edgeIndex = this.graph.getEdgeIndex();
    final LineString line = edge.getLineString();
    final List<Edge<T>> crossings = queryCrosses(edgeIndex, line);
    crossings.remove(edge);

    for (final Edge<T> crossEdge : crossings) {
      if (!crossEdge.isRemoved()) {
        final LineString crossLine = crossEdge.getLineString();
        final Point intersection = LineStringUtil.getCrossingIntersection(line, crossLine);
        if (intersection != null) {
          final Point point = this.graph.getPrecisionModel().getPreciseCoordinates(intersection);
          final Node<T> node = this.graph.getNode(point);
          this.splitEdgesCloseToNodeVisitor.accept(node);
        }
      }
    }
  }

  @Override
  public void addNodeListener(final NodeEventListener<T> listener) {
    this.splitEdgesCloseToNodeVisitor.addNodeListener(listener);
  }

  public Collection<Edge<T>> getNewEdges() {
    return this.splitEdgesCloseToNodeVisitor.getNewEdges();
  }

  public Collection<T> getSplitObjects() {
    return this.splitEdgesCloseToNodeVisitor.getSplitObjects();
  }

  public List<Edge<T>> queryCrosses(final IdObjectIndex<Edge<T>> edgeIndex, final LineString line) {
    final Geometry preparedLine = line.prepare();
    final BoundingBox envelope = line.getBoundingBox();
    final List<Edge<T>> edges = edgeIndex.query(envelope);
    // TODO change to use an visitor
    for (final Iterator<Edge<T>> iterator = edges.iterator(); iterator.hasNext();) {
      final Edge<T> edge = iterator.next();
      final LineString matchLine = edge.getLineString();
      if (!preparedLine.crosses(matchLine)) {
        iterator.remove();
      }
    }
    return edges;
  }

  public void setNewEdges(final Collection<Edge<T>> newEdges) {
    this.splitEdgesCloseToNodeVisitor.setNewEdges(newEdges);
  }

  public void setSplitObjects(final Collection<T> splitObjects) {
    this.splitEdgesCloseToNodeVisitor.setSplitObjects(splitObjects);
  }
}
