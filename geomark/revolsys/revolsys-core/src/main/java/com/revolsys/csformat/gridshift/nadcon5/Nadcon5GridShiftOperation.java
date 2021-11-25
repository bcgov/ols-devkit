package com.revolsys.csformat.gridshift.nadcon5;

import java.util.ArrayList;
import java.util.List;

import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;
import org.jeometry.coordinatesystem.operation.gridshift.HorizontalShiftOperation;

public class Nadcon5GridShiftOperation implements HorizontalShiftOperation {

  private final String sourceDatumName;

  private final String targetDatumName;

  private final List<List<Nadcon5RegionDatumGrids>> REGION_GRIDS = new ArrayList<>();

  private final boolean[] REGION_INVERSE = new boolean[Nadcon5Region.REGIONS.size()];

  public Nadcon5GridShiftOperation(final String sourceDatumName, final String targetDatumName) {
    this.sourceDatumName = sourceDatumName;
    this.targetDatumName = targetDatumName;
    for (int regionIndex = 0; regionIndex < Nadcon5Region.REGIONS.size(); regionIndex++) {
      final Nadcon5Region region = Nadcon5Region.REGIONS.get(regionIndex);
      final int sourceDatumIndex = region.getDatumIndex(this.sourceDatumName);
      final int targetDatumIndex = region.getDatumIndex(this.targetDatumName);
      this.REGION_INVERSE[regionIndex] = sourceDatumIndex > targetDatumIndex;
      this.REGION_GRIDS.add(region.getGrids(sourceDatumIndex, targetDatumIndex));
    }
  }

  @Override
  public boolean horizontalShift(final CoordinatesOperationPoint point) {
    final double x = point.x;
    final double y = point.y;
    final double z = point.z;
    if (point.x < 0) {
      point.x += 360;
    }
    for (final Nadcon5Region region : Nadcon5Region.REGIONS) {
      if (region.covers(point)) {
        final int index = region.getIndex();
        final List<Nadcon5RegionDatumGrids> grids = this.REGION_GRIDS.get(index);
        final boolean inverse = this.REGION_INVERSE[index];
        if (inverse) {
          for (final Nadcon5RegionDatumGrids grid : grids) {
            if (!grid.shiftInverse(point)) {
              point.resetPoint(x, y, z);
              return false;
            }
          }
        } else {
          for (final Nadcon5RegionDatumGrids grid : grids) {
            if (!grid.shiftForward(point)) {
              point.resetPoint(x, y, z);
              return false;
            }
          }
        }
        if (point.x > 180) {
          point.x = point.x - 360;
        }
        return true;
      }
    }
    point.resetPoint(x, y, z);
    return false;
  }

  @Override
  public String toString() {
    return "Nadcon5: " + this.sourceDatumName + " -> " + this.targetDatumName;
  }
}
