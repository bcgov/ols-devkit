package com.revolsys.geometry.operation.simple;

import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.operation.valid.SegmentError;

public class SelfOverlapSegmentError extends SegmentError {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public SelfOverlapSegmentError(final Segment segment) {
    super("Self Overlap at Segment", segment);
  }
}
