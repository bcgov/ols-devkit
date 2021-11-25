package com.revolsys.geometry.graph.linemerge;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;

public class LineStringsGraph extends Graph<LineString> {

  public void addEdge(LineString line) {
    if (!line.isEmpty()) {
      line = line.removeDuplicatePoints();
      final int vertexCount = line.getVertexCount();
      if (vertexCount > 1) {
        addEdge(line, line);
      }
    }
  }

  public void removeEdge(final LineString line) {
    final Point point = line.getFromPoint();
    final Node<LineString> node = findNode(point);
    if (node != null) {
      for (final Edge<LineString> edge : Lists.toArray(node.getOutEdges())) {
        final LineString edgeLine = edge.getLineString();
        if (line.equals(edgeLine)) {
          remove(edge);
        }
      }
    }
  }

  public void removeEdges(final Iterable<LineString> lines) {
    for (final LineString line : lines) {
      removeEdge(line);
    }
  }
}
