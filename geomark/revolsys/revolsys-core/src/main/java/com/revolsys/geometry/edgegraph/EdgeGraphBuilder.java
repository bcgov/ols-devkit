package com.revolsys.geometry.edgegraph;

import java.util.Collection;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;

/**
 * Builds an edge graph from geometries containing edges.
 *
 * @author mdavis
 *
 */
public class EdgeGraphBuilder {
  public static EdgeGraph build(final Collection geoms) {
    final EdgeGraphBuilder builder = new EdgeGraphBuilder();
    builder.add(geoms);
    return builder.getGraph();
  }

  private final EdgeGraph graph = new EdgeGraph();

  public EdgeGraphBuilder() {

  }

  /**
   * Adds the edges in a collection of {@link Geometry}s to the graph.
   * May be called multiple times.
   * Any dimension of Geometry may be added.
   *
   * @param geometries the geometries to be added
   */
  public void add(final Collection geometries) {
    for (final Object element : geometries) {
      final Geometry geometry = (Geometry)element;
      add(geometry);
    }
  }

  /**
   * Adds the edges of a Geometry to the graph.
   * May be called multiple times.
   * Any dimension of Geometry may be added; the constituent edges are
   * extracted.
   *
   * @param geometry geometry to be added
   */
  public void add(final Geometry geometry) {
    for (final LineString line : geometry.getGeometryComponents(LineString.class)) {
      add(line);
    }
  }

  private void add(final LineString lineString) {
    final LineString seq = lineString;
    for (int i = 1; i < seq.getVertexCount(); i++) {
      this.graph.addEdge(seq.getPoint(i - 1), seq.getPoint(i));
    }
  }

  public EdgeGraph getGraph() {
    return this.graph;
  }

}
