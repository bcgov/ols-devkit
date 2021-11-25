package org.jeometry.coordinatesystem.operation;

import org.jeometry.common.function.BiConsumerDouble;

public interface CoordinatesOperation {

  void perform(CoordinatesOperationPoint point);

  default void perform2d(final CoordinatesOperationPoint point, final double x, final double y,
    final BiConsumerDouble action) {
    point.setPoint(x, y);
    perform(point);
    point.apply2d(action);
  }
}
