package com.revolsys.geometry.graph;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.visitor.DelegatingVisitor;

public abstract class EdgeVisitor<T> extends DelegatingVisitor<Edge<T>> {
  public abstract BoundingBox getEnvelope();
}
