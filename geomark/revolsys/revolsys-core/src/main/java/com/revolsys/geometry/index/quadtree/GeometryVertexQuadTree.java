package com.revolsys.geometry.index.quadtree;

import org.jeometry.common.number.Doubles;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.util.RectangleUtil;
import com.revolsys.util.Property;

public class GeometryVertexQuadTree extends IdObjectQuadTree<Vertex> {

  private static final long serialVersionUID = 1L;

  public static GeometryVertexQuadTree get(final Geometry geometry) {
    if (Property.hasValue(geometry)) {
      try {
        return new GeometryVertexQuadTree(geometry);
      } catch (final Error e) {
        System.out.println(geometry);
        throw e;
      }
    } else {
      return null;
    }
  }

  private final Geometry geometry;

  public GeometryVertexQuadTree(final Geometry geometry) {
    super(geometry.getGeometryFactory());
    this.geometry = geometry;
    if (geometry != null) {
      setGeometryFactory(geometry.getGeometryFactory());
      for (final Vertex vertex : geometry.vertices()) {
        final double x = vertex.getX();
        final double y = vertex.getY();
        insertItem(x, y, vertex);
      }
    }
  }

  @Override
  protected Object getId(final Vertex vertex) {
    return vertex.getVertexId();
  }

  @Override
  protected Vertex getItem(final Object id) {
    final int[] vertexId = (int[])id;
    return this.geometry.getVertex(vertexId);
  }

  @Override
  protected boolean intersectsBounds(final Object id, final double x, final double y) {
    final Vertex vertex = getItem(id);
    if (vertex == null) {
      return false;
    } else {
      if (Doubles.equal(x, vertex.getX())) {
        if (Doubles.equal(y, vertex.getY())) {
          return true;
        }
      }
      return false;
    }
  }

  @Override
  protected boolean intersectsBounds(final Object id, final double minX, final double minY,
    final double maxX, final double maxY) {
    final Vertex vertex = getItem(id);
    if (vertex == null) {
      return false;
    } else {
      final double x = vertex.getX();
      final double y = vertex.getY();
      return RectangleUtil.intersectsPoint(minX, minY, maxX, maxY, x, y);
    }
  }
}
