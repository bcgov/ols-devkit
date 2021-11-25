package com.revolsys.geometry.graph.filter;

import java.util.function.Predicate;

import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;

public class NodeGeometryIntersectionFilter<T> implements Predicate<Node<T>> {

  private GeometryFactory geometryFactory;

  private Geometry preparedGeometry;

  public NodeGeometryIntersectionFilter() {
  }

  public NodeGeometryIntersectionFilter(final Geometry geometry) {
    setGeometry(geometry);
  }

  public void setGeometry(final Geometry geometry) {
    this.preparedGeometry = geometry.prepare();
    this.geometryFactory = geometry.getGeometryFactory();
  }

  @Override
  public boolean test(final Node<T> node) {
    final boolean intersects = this.preparedGeometry.intersects(node);
    return intersects;
  }
}
