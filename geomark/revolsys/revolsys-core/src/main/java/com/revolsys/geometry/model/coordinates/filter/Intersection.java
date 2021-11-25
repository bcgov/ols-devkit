package com.revolsys.geometry.model.coordinates.filter;

import java.util.function.Predicate;

import com.revolsys.geometry.graph.linestring.LineStringGraph;
import com.revolsys.geometry.model.LineString;

public class Intersection implements Predicate<LineString> {

  private final LineStringGraph graph;

  private final LineString line;

  public Intersection(final LineString line) {
    this.line = line;
    this.graph = new LineStringGraph(line);
  }

  public LineString getLine() {
    return this.line;
  }

  @Override
  public boolean test(final LineString line) {
    return this.graph.intersects(line);
  }
}
