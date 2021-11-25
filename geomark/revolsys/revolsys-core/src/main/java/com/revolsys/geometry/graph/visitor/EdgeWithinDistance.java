package com.revolsys.geometry.graph.visitor;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.visitor.CreateListVisitor;
import com.revolsys.visitor.DelegatingVisitor;

public class EdgeWithinDistance<T> extends DelegatingVisitor<Edge<T>>
  implements Predicate<Edge<T>> {
  public static <T> List<Edge<T>> edgesWithinDistance(final Graph<T> graph, final Geometry geometry,
    final double maxDistance) {
    if (geometry == null) {
      return Collections.emptyList();
    } else {
      final CreateListVisitor<Edge<T>> results = new CreateListVisitor<>();
      BoundingBox env = geometry.getBoundingBox();
      env = env.bboxNewExpandDelta(maxDistance);
      graph.getEdgeIndex().forEach(env, new EdgeWithinDistance<>(geometry, maxDistance, results));
      return results.getList();
    }
  }

  public static <T> List<Edge<T>> edgesWithinDistance(final Graph<T> graph, final Node<T> node,
    final double maxDistance) {
    final GeometryFactory geometryFactory = GeometryFactory.floating3d(0);
    final Point coordinate = node;
    final Geometry geometry = geometryFactory.point(coordinate);
    return edgesWithinDistance(graph, geometry, maxDistance);

  }

  public static <T> List<Edge<T>> edgesWithinDistance(final Graph<T> graph, final Point point,
    final double maxDistance) {
    final GeometryFactory geometryFactory = GeometryFactory.floating3d(0);
    final Geometry geometry = geometryFactory.point(point);
    return edgesWithinDistance(graph, geometry, maxDistance);

  }

  private final Geometry geometry;

  private final double maxDistance;

  public EdgeWithinDistance(final Geometry geometry, final double maxDistance) {
    this.geometry = geometry;
    this.maxDistance = maxDistance;
  }

  public EdgeWithinDistance(final Geometry geometry, final double maxDistance,
    final Consumer<Edge<T>> matchVisitor) {
    super(matchVisitor);
    this.geometry = geometry;
    this.maxDistance = maxDistance;
  }

  @Override
  public void accept(final Edge<T> edge) {
    if (test(edge)) {
      super.accept(edge);
    }
  }

  @Override
  public boolean test(final Edge<T> edge) {
    final LineString line = edge.getLineString();
    final double distance = line.distanceGeometry(this.geometry);
    if (distance <= this.maxDistance) {
      return true;
    } else {
      return false;
    }
  }
}
