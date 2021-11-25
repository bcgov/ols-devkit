package com.revolsys.csformat.gridshift.nadcon5;

import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

public class Nadcon5RegionDatumGrids {

  private final Nadcon5FileGrid ehtAccuracies;

  private final Nadcon5FileGrid ehtShifts;

  private final Nadcon5FileGrid latAccuracies;

  private final Nadcon5FileGrid latShifts;

  private final Nadcon5FileGrid lonAccuracies;

  private final Nadcon5FileGrid lonShifts;

  private final String sourceDatumName;

  private final String targetDatumName;

  private final Nadcon5Region region;

  public Nadcon5RegionDatumGrids(final Nadcon5Region region, final String sourceDatumName,
    final String targetDatumName) {
    this.region = region;
    this.sourceDatumName = sourceDatumName;
    this.targetDatumName = sourceDatumName;
    this.lonAccuracies = new Nadcon5FileGrid(region, sourceDatumName, targetDatumName, "lon",
      "err");
    this.lonShifts = new Nadcon5FileGrid(region, sourceDatumName, targetDatumName, "lon", "trn");
    this.latAccuracies = new Nadcon5FileGrid(region, sourceDatumName, targetDatumName, "lat",
      "err");
    this.latShifts = new Nadcon5FileGrid(region, sourceDatumName, targetDatumName, "lat", "trn");
    this.ehtAccuracies = new Nadcon5FileGrid(region, sourceDatumName, targetDatumName, "eht",
      "err");
    this.ehtShifts = new Nadcon5FileGrid(region, sourceDatumName, targetDatumName, "eht", "trn");
  }

  public double getEhtAccuracy(final int fileIndex, final double lon, final double lat) {
    return Math.pow(this.ehtAccuracies.getValueBiquadratic(lon, lat), 2);
  }

  public double getEhtShift(final int fileIndex, final double lon, final double lat) {
    return this.ehtShifts.getValueBiquadratic(lon, lat);
  }

  public double getLatAccuracy(final int fileIndex, final double lon, final double lat) {
    return Math.pow(this.latAccuracies.getValueBiquadratic(lon, lat), 2);
  }

  public double getLatShift(final int fileIndex, final double lon, final double lat) {
    return this.latShifts.getValueBiquadratic(lon, lat) / 3600.0;
  }

  public double getLonAccuracy(final int fileIndex, final double lon, final double lat) {
    return Math.pow(this.lonAccuracies.getValueBiquadratic(lon, lat), 2);
  }

  public double getLonShift(final int fileIndex, final double lon, final double lat) {
    return this.lonShifts.getValueBiquadratic(lon, lat) / 3600.0;
  }

  public boolean shiftForward(final CoordinatesOperationPoint point) {
    final double x = point.x;
    final double y = point.y;
    final double z = point.z;
    final double xShift = this.lonShifts.getValueBiquadratic(x, y);
    final double yShift = this.latShifts.getValueBiquadratic(x, y);
    final double zShift = this.ehtShifts.getValueBiquadratic(x, y);
    if (Double.isFinite(xShift)) {
      point.x = x + xShift;
      point.y = y + yShift;
      point.z = z + zShift;
      if (this.region.covers(point)) {
        return true;
      }
    }
    return false;
  }

  public boolean shiftInverse(final CoordinatesOperationPoint point) {
    final double x = point.x;
    final double y = point.y;
    final double z = point.z;
    final double xShift = this.lonShifts.getValueBiquadratic(x, y);
    final double yShift = this.latShifts.getValueBiquadratic(x, y);
    final double zShift = this.ehtShifts.getValueBiquadratic(x, y);
    if (Double.isFinite(xShift)) {
      point.x = x - xShift;
      point.y = y - yShift;
      point.z = z - zShift;
      if (this.region.covers(point)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return this.region + " " + this.sourceDatumName + " -> " + this.targetDatumName;
  }
}
