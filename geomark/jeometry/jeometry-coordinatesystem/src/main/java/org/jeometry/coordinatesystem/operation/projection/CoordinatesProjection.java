package org.jeometry.coordinatesystem.operation.projection;

import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

public interface CoordinatesProjection {
  CoordinatesOperation getInverseOperation();

  CoordinatesOperation getProjectOperation();

  void inverse(CoordinatesOperationPoint point);

  void project(CoordinatesOperationPoint point);
}
