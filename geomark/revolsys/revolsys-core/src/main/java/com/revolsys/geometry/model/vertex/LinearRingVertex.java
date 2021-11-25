package com.revolsys.geometry.model.vertex;

import java.awt.geom.PathIterator;

import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;

public class LinearRingVertex extends LineStringVertex {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public LinearRingVertex(final LinearRing line, final int... vertexId) {
    super(line, vertexId);
  }

  public LinearRingVertex(final LinearRing line, final int vertexIndex) {
    super(line, vertexIndex);
  }

  @Override
  public int getAwtType() {

    final int vertexIndex = this.vertexIndex;
    if (vertexIndex == 0) {
      return PathIterator.SEG_MOVETO;
    } else {
      final LineString line = getLineString();
      if (vertexIndex == line.getLastVertexIndex()) {
        return PathIterator.SEG_CLOSE;
      } else {
        return PathIterator.SEG_LINETO;
      }
    }
  }
}
