package com.revolsys.geometry.operation.valid;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.segment.Segment;

public class SegmentError extends AbstractGeometryValidationError {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final int[] segmentId;

  public SegmentError(final String message, final Segment segment) {
    super(message, segment.getGeometry());
    this.segmentId = segment.getSegmentId();
  }

  @Override
  public Geometry getErrorGeometry() {
    return getSegment();
  }

  @Override
  public Point getErrorPoint() {
    return getSegment().getFromPoint();
  }

  public Segment getSegment() {
    final Geometry geometry = getGeometry();
    final Segment segment = geometry.getSegment(this.segmentId);
    return segment;
  }

  public int[] getSegmentId() {
    return this.segmentId;
  }
}
