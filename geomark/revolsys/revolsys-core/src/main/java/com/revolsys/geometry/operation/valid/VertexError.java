package com.revolsys.geometry.operation.valid;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.vertex.Vertex;

public class VertexError extends AbstractGeometryValidationError {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final int[] vertexId;

  public VertexError(final String message, final Vertex vertex) {
    super(message, vertex.getGeometry());
    this.vertexId = vertex.getVertexId();
  }

  @Override
  public Point getErrorPoint() {
    return getVertex();
  }

  public Vertex getVertex() {
    final Geometry geometry = getGeometry();
    final Vertex vertex = geometry.getVertex(this.vertexId);
    return vertex;
  }

  public int[] getVertexId() {
    return this.vertexId;
  }
}
