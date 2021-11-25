package com.revolsys.elevation.gridded.usgsdem;

public class UsgsGriddedElevationModelColumn {
  private final int gridY;

  final int[] elevations;

  public UsgsGriddedElevationModelColumn(final int gridY, final int[] elevations) {
    this.gridY = gridY;
    this.elevations = elevations;
  }

  public int getElevationInt(final int gridY) {
    final int index = gridY - this.gridY;
    if (index >= 0 && index < this.elevations.length) {
      return this.elevations[index];
    } else {
      return UsgsGriddedElevationModel.NULL_VALUE;
    }
  }

  public boolean hasElevation(final int gridY) {
    final int index = gridY - this.gridY;
    if (index >= 0 && index < this.elevations.length) {
      return this.elevations[index] != UsgsGriddedElevationModel.NULL_VALUE;
    } else {
      return false;
    }
  }

  public void setValue(final int gridY2, final int elevation) {
    final int index = this.gridY - this.gridY;
    if (index >= 0 && index < this.elevations.length) {
      this.elevations[index] = elevation;
    }
  }
}
