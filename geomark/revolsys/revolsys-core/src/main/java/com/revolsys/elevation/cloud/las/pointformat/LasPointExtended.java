package com.revolsys.elevation.cloud.las.pointformat;

public interface LasPointExtended extends LasPointGpsTime {
  boolean isOverlap();

  void setOverlap(boolean overlap);
}
