package org.jeometry.coordinatesystem.model;

import java.util.ArrayList;
import java.util.List;

import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;
import org.jeometry.coordinatesystem.operation.gridshift.HorizontalShiftOperation;

public class GeographicCoordinateSystemGridShiftOperation implements CoordinatesOperation {
  private final GeographicCoordinateSystem sourceCoordinateSystem;

  private final GeographicCoordinateSystem targetCoordinateSystem;

  private final List<HorizontalShiftOperation> operations = new ArrayList<>();

  public GeographicCoordinateSystemGridShiftOperation(
    final GeographicCoordinateSystem sourceCoordinateSystem,
    final GeographicCoordinateSystem targetCoordinateSystem) {
    this.sourceCoordinateSystem = sourceCoordinateSystem;
    this.targetCoordinateSystem = targetCoordinateSystem;
  }

  public synchronized void addOperation(final HorizontalShiftOperation operation) {
    if (!this.operations.contains(operation)) {
      this.operations.add(operation);
    }
  }

  @Override
  public void perform(final CoordinatesOperationPoint point) {
    for (final HorizontalShiftOperation operation : this.operations) {
      if (operation.horizontalShift(point)) {
        return;
      }
    }
  }

  public void removeOperation(final HorizontalShiftOperation operation) {
    this.operations.remove(operation);
  }

  @Override
  public String toString() {
    return this.sourceCoordinateSystem + " -> " + this.targetCoordinateSystem;
  }

}
