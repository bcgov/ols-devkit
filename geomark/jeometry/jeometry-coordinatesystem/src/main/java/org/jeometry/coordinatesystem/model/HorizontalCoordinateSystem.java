package org.jeometry.coordinatesystem.model;

import org.jeometry.coordinatesystem.model.systems.EpsgCoordinateSystems;

public interface HorizontalCoordinateSystem extends CoordinateSystem {

  @SuppressWarnings("unchecked")
  default <C extends CoordinateSystem> C getCompound(
    final VerticalCoordinateSystem verticalCoordinateSystem) {
    if (verticalCoordinateSystem == null) {
      return (C)this;
    } else {
      final CompoundCoordinateSystem compoundCoordinateSystem = new CompoundCoordinateSystem(this,
        verticalCoordinateSystem);
      return (C)EpsgCoordinateSystems.getCoordinateSystem(compoundCoordinateSystem);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  default <C extends CoordinateSystem> C getHorizontalCoordinateSystem() {
    return (C)this;
  }

  String getUnitLabel();
}
