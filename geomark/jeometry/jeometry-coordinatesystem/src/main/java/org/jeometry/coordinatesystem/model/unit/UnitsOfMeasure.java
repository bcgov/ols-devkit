package org.jeometry.coordinatesystem.model.unit;

import org.jeometry.coordinatesystem.model.systems.EpsgCoordinateSystems;

public interface UnitsOfMeasure {
  static Degree DEGREE = EpsgCoordinateSystems.getUnit(9102);

  static Metre METRE = EpsgCoordinateSystems.getUnit(9001);
}
