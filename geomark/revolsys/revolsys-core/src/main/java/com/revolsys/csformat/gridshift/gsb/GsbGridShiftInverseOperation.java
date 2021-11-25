package com.revolsys.csformat.gridshift.gsb;

import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;
import org.jeometry.coordinatesystem.operation.gridshift.HorizontalShiftOperation;

public class GsbGridShiftInverseOperation implements HorizontalShiftOperation {

  private final GsbGridShiftFile file;

  public GsbGridShiftInverseOperation(final GsbGridShiftFile file) {
    this.file = file;
  }

  @Override
  public boolean horizontalShift(final CoordinatesOperationPoint point) {
    final GsbGridShiftFile file = this.file;
    final double lon = point.x;
    final double lat = point.y;
    final double lonPositiveWestSeconds = -lon * 3600;
    final double latSeconds = lat * 3600;
    double lonShift = 0;
    double latShift = 0;
    for (int i = 0; i < 4; i++) {
      final double forwardLonPositiveWestSeconds = lonPositiveWestSeconds - lonShift;
      final double forwardLatSeconds = latSeconds - latShift;
      final GsbGridShiftGrid gsbGridShiftGrid = file.getGrid(forwardLonPositiveWestSeconds,
        forwardLatSeconds);
      if (gsbGridShiftGrid == null) {
        if (i == 0) {
          return false;
        } else {
          return true;
        }
      } else {
        lonShift = gsbGridShiftGrid.getLonShift(forwardLonPositiveWestSeconds, forwardLatSeconds);
        latShift = gsbGridShiftGrid.getLatShift(forwardLonPositiveWestSeconds, forwardLatSeconds);
      }
    }
    point.x = -(lonPositiveWestSeconds - lonShift) / 3600;
    point.y = (latSeconds - latShift) / 3600;
    return true;
  }

  @Override
  public String toString() {
    return this.file.toString();
  }
}
