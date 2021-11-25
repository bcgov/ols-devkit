package com.revolsys.geometry.dissolve;

import com.revolsys.geometry.edgegraph.EdgeGraph;
import com.revolsys.geometry.edgegraph.HalfEdge;
import com.revolsys.geometry.model.Point;

/**
 * A graph containing {@link DissolveHalfEdge}s.
 *
 * @author Martin Davis
 *
 */
class DissolveEdgeGraph extends EdgeGraph {
  @Override
  protected HalfEdge newHalfEdge(final Point p0) {
    return new DissolveHalfEdge(p0);
  }

}
