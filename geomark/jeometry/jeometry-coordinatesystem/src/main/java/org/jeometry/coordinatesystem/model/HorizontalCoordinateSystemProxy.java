package org.jeometry.coordinatesystem.model;

import org.jeometry.coordinatesystem.model.datum.GeodeticDatum;
import org.jeometry.coordinatesystem.operation.gridshift.HorizontalShiftOperation;

public interface HorizontalCoordinateSystemProxy extends CoordinateSystemProxy {
  default void addGridShiftOperation(final HorizontalCoordinateSystemProxy coordinateSystem,
    final HorizontalShiftOperation operation) {
    final GeographicCoordinateSystem sourceCs = getHorizontalCoordinateSystem();
    final GeographicCoordinateSystem targetCs = coordinateSystem.getHorizontalCoordinateSystem();
    if (sourceCs == null) {
      throw new IllegalArgumentException(this + " does not have a coordinate system");
    } else if (targetCs == null) {
      throw new IllegalArgumentException(targetCs + " does not have a coordinate system");
    } else {
      sourceCs.addGridShiftOperation(targetCs, operation);
    }
  }

  default Ellipsoid getEllipsoid() {
    final GeodeticDatum geodeticDatum = getGeodeticDatum();
    if (geodeticDatum == null) {
      return null;
    } else {
      return geodeticDatum.getEllipsoid();
    }
  }

  default GeodeticDatum getGeodeticDatum() {
    final HorizontalCoordinateSystem coordinateSystem = getHorizontalCoordinateSystem();
    if (coordinateSystem != null) {
      return coordinateSystem.getGeodeticDatum();
    }
    return null;
  }

  <C extends CoordinateSystem> C getHorizontalCoordinateSystem();

  default int getHorizontalCoordinateSystemId() {
    final HorizontalCoordinateSystem coordinateSystem = getHorizontalCoordinateSystem();
    if (coordinateSystem == null) {
      return 0;
    } else {
      return coordinateSystem.getHorizontalCoordinateSystemId();
    }
  }

  default String getHorizontalCoordinateSystemName() {
    final HorizontalCoordinateSystem coordinateSystem = getHorizontalCoordinateSystem();
    if (coordinateSystem == null) {
      return "Unknown";
    } else {
      return coordinateSystem.getCoordinateSystemName();
    }
  }

  default void removeGridShiftOperation(final HorizontalCoordinateSystemProxy coordinateSystem,
    final HorizontalShiftOperation operation) {
    final GeographicCoordinateSystem sourceCs = getHorizontalCoordinateSystem();
    final GeographicCoordinateSystem targetCs = coordinateSystem.getHorizontalCoordinateSystem();
    if (sourceCs == null) {
      throw new IllegalArgumentException(this + " does not have a coordinate system");
    } else if (targetCs == null) {
      throw new IllegalArgumentException(targetCs + " does not have a coordinate system");
    } else {
      sourceCs.removeGridShiftOperation(targetCs, operation);
    }
  }
}
