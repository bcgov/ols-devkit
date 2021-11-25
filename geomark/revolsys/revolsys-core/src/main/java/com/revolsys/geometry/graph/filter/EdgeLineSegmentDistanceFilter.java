package com.revolsys.geometry.graph.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.index.IdObjectIndex;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDoubleXYEditor;

public class EdgeLineSegmentDistanceFilter extends LineSegmentDoubleXYEditor
  implements Predicate<Edge<LineSegment>> {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public static List<Edge<LineSegment>> getEdges(final Graph<LineSegment> graph,
    final LineSegment lineSegment, final double maxDistance) {
    final List<Edge<LineSegment>> results = new ArrayList<>();
    final BoundingBox boundingBox = lineSegment.bboxNewExpandDelta(maxDistance);
    final IdObjectIndex<Edge<LineSegment>> edgeIndex = graph.getEdgeIndex();
    final EdgeLineSegmentDistanceFilter filter = new EdgeLineSegmentDistanceFilter(lineSegment,
      maxDistance);
    edgeIndex.forEach(boundingBox, filter, results::add);
    return results;
  }

  private final double maxDistance;

  public EdgeLineSegmentDistanceFilter(final double maxDistance) {
    super();
    this.maxDistance = maxDistance;
  }

  public EdgeLineSegmentDistanceFilter(final LineSegment lineSegment, final double maxDistance) {
    super(lineSegment);
    this.maxDistance = maxDistance;
  }

  @Override
  public boolean test(final Edge<LineSegment> edge) {
    final LineSegment lineSegment = edge.getObject();
    final double distance = this.distance(lineSegment);
    if (distance <= this.maxDistance) {
      return true;
    } else {
      return false;
    }
  }
}
