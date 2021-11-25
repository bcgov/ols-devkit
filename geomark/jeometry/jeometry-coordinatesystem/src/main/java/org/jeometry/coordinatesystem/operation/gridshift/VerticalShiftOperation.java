package org.jeometry.coordinatesystem.operation.gridshift;

import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

public interface VerticalShiftOperation extends CoordinatesOperation {

  @Override
  default void perform(final CoordinatesOperationPoint point) {
    verticalShift(point);
  }

  boolean verticalShift(CoordinatesOperationPoint point);
}
