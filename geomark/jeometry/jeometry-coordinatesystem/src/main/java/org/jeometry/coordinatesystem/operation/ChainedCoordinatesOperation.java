package org.jeometry.coordinatesystem.operation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChainedCoordinatesOperation implements CoordinatesOperation {
  private final List<CoordinatesOperation> operations;

  public ChainedCoordinatesOperation(final CoordinatesOperation... operations) {
    this(Arrays.asList(operations));
  }

  public ChainedCoordinatesOperation(final List<CoordinatesOperation> operations) {
    this.operations = new ArrayList<>(operations);
  }

  public void addOperation(final CoordinatesOperation operation) {
    this.operations.add(operation);
  }

  @Override
  public void perform(final CoordinatesOperationPoint point) {
    for (final CoordinatesOperation operation : this.operations) {
      operation.perform(point);
    }
  }

  @Override
  public String toString() {
    return this.operations.toString();
  }
}
