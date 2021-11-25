package com.revolsys.geometry.model.vertex;

import java.util.Comparator;

public class VertexIndexComparator implements Comparator<Vertex> {
  @Override
  public int compare(final Vertex vertex1, final Vertex vertex2) {
    final int[] vertexId1 = vertex1.getVertexId();
    final int[] vertexId2 = vertex2.getVertexId();
    for (int i = 0; i < Math.max(vertexId1.length, vertexId2.length); i++) {
      if (i >= vertexId1.length) {
        return -1;
      } else if (i >= vertexId2.length) {
        return 1;
      } else {
        final int value1 = vertexId1[i];
        final int value2 = vertexId2[i];
        if (value1 < value2) {
          return -1;
        } else if (value1 > value2) {
          return 1;
        }
      }
    }
    return 0;
  }
}
