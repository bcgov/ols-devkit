package com.revolsys.geometry.filter;

import java.util.function.Predicate;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Dimension;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.IntersectionMatrix;
import com.revolsys.geometry.model.LineString;

public class LinearIntersectionFilter implements Predicate<LineString> {

  private final BoundingBox envelope;

  private final LineString line;

  private final Geometry preparedLine;

  public LinearIntersectionFilter(final LineString line) {
    this.line = line;
    this.preparedLine = line.prepare();
    this.envelope = line.getBoundingBox();
  }

  @Override
  public boolean test(final LineString line) {
    final BoundingBox envelope = line.getBoundingBox();
    if (envelope.bboxIntersects(this.envelope)) {
      if (this.preparedLine.intersects(line)) {
        final IntersectionMatrix relate = this.line.relate(line);
        if (relate.isOverlaps(Dimension.L, Dimension.L) || relate.isContains()
          || relate.isWithin()) {
          return true;
        }
      }
    }
    return false;
  }
}
