package org.jeometry.coordinatesystem.model;

public interface CoordinateSystemProxy {

  <C extends CoordinateSystem> C getCoordinateSystem();

  default int getCoordinateSystemId() {
    final CoordinateSystem coordinateSystem = getCoordinateSystem();
    if (coordinateSystem == null) {
      return 0;
    } else {
      return coordinateSystem.getCoordinateSystemId();
    }
  }

  default String getCoordinateSystemName() {
    final CoordinateSystem coordinateSystem = getCoordinateSystem();
    if (coordinateSystem == null) {
      return "Unknown";
    } else {
      return coordinateSystem.getCoordinateSystemName();
    }
  }
}
