package org.jeometry.coordinatesystem.operation.gridshift;

import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

public interface HorizontalShiftOperation extends CoordinatesOperation {

  boolean horizontalShift(CoordinatesOperationPoint point);

  @Override
  default void perform(final CoordinatesOperationPoint point) {
    horizontalShift(point);
  }
}
