package com.revolsys.geometry.operation.simple;

import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.operation.valid.VertexError;

public class DuplicateVertexError extends VertexError {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public DuplicateVertexError(final Vertex vertex) {
    super("Duplicate Vertex", vertex);
  }
}
