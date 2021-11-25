package com.revolsys.geometry.algorithm.linematch;

import java.util.function.Predicate;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.model.BoundingBox;

public class LineMatchEdgeFilter implements Predicate<Edge<LineSegmentMatch>> {

  public static double getDistance(final Edge<LineSegmentMatch> edge,
    final Node<LineSegmentMatch> node, final double tolerance) {
    final double distance = edge.distancePoint(node);
    if (distance == 0) {
      return 0;
    } else if (distance < tolerance) {
      if (distance == node.distancePoint(edge.getFromNode())) {
        return Double.MAX_VALUE;
      } else if (distance == node.distancePoint(edge.getToNode())) {
        return Double.MAX_VALUE;
      } else {
        return distance;
      }
    }
    return distance;
  }

  public static boolean isEitherOppositeNodesWithinDistance(final Edge<LineSegmentMatch> edge1,
    final Edge<LineSegmentMatch> edge2, final Node<LineSegmentMatch> fromNode2,
    final double tolerance) {
    if (isOppositeNodeWithinDistance(edge1, edge2, fromNode2, tolerance)) {
      return true;
    } else if (isOppositeNodeWithinDistance(edge2, edge1, fromNode2, tolerance)) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean isOppositeNodeWithinDistance(final Edge<LineSegmentMatch> edge1,
    final Edge<LineSegmentMatch> edge2, final Node<LineSegmentMatch> node, final double tolerance) {
    final Node<LineSegmentMatch> oppositeNode = edge1.getOppositeNode(node);
    final double oppositeNodeEdge2Distance = edge2.distancePoint(oppositeNode);
    if (oppositeNodeEdge2Distance < tolerance) {
      if (oppositeNodeEdge2Distance == edge1.getLength()) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  private final Edge<LineSegmentMatch> edge;

  private final LineSegmentMatch edgeMatch;

  private final BoundingBox envelope;

  private final Node<LineSegmentMatch> fromNode;

  private final int index;

  private final double tolerance;

  private final Node<LineSegmentMatch> toNode;

  public LineMatchEdgeFilter(final Edge<LineSegmentMatch> edge, final int index,
    final double tolerance) {
    this.edge = edge;
    this.index = index;
    this.tolerance = tolerance;

    this.envelope = edge.getBoundingBox() //
      .bboxEditor() //
      .expandDelta(tolerance);

    this.edgeMatch = edge.getObject();
    this.fromNode = edge.getFromNode();
    this.toNode = edge.getToNode();
  }

  private boolean checkTolerance(final double from1Distance, final double to1Distance,
    final double from2Distance, final double to2Distance) {
    if (from1Distance < this.tolerance) {
      if (to1Distance < this.tolerance || from2Distance < this.tolerance
        || to2Distance < this.tolerance) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  public com.revolsys.geometry.model.BoundingBox getEnvelope() {
    return this.envelope;
  }

  @Override
  public boolean test(final Edge<LineSegmentMatch> edge2) {
    if (edge2.getBoundingBox().bboxIntersects(this.envelope)) {
      final LineSegmentMatch edgeMatch2 = edge2.getObject();
      if (!edgeMatch2.hasSegment(this.index)) {
        final Node<LineSegmentMatch> fromNode2 = edge2.getFromNode();
        final Node<LineSegmentMatch> toNode2 = edge2.getToNode();
        if (this.edge.hasNode(fromNode2)) {
          return isEitherOppositeNodesWithinDistance(this.edge, edge2, fromNode2, this.tolerance);
        } else if (this.edge.hasNode(toNode2)) {
          return isEitherOppositeNodesWithinDistance(this.edge, edge2, toNode2, this.tolerance);
        } else if (this.edge.distanceLine(edge2) < this.tolerance) {
          final double edge2FromNodeDistance = getDistance(edge2, this.fromNode, this.tolerance);
          final double edge2ToNodeDistance = getDistance(edge2, this.toNode, this.tolerance);

          final double edgeFromNode2Distance = getDistance(this.edge, fromNode2, this.tolerance);
          final double edgeToNode2Distance = getDistance(this.edge, toNode2, this.tolerance);

          if (checkTolerance(edge2FromNodeDistance, edge2ToNodeDistance, edgeFromNode2Distance,
            edgeToNode2Distance)) {
            return true;
          } else if (checkTolerance(edge2ToNodeDistance, edge2FromNodeDistance,
            edgeFromNode2Distance, edgeToNode2Distance)) {
            return true;
          } else if (checkTolerance(edgeFromNode2Distance, edgeToNode2Distance,
            edge2FromNodeDistance, edge2ToNodeDistance)) {
            return true;
          } else if (checkTolerance(edgeToNode2Distance, edgeFromNode2Distance,
            edge2FromNodeDistance, edge2ToNodeDistance)) {
            return true;
          } else {
            return false;
          }
        }
      }
    }
    return false;
  }
}
