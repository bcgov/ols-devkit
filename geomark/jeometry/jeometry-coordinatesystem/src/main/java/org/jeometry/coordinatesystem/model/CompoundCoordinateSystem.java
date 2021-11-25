package org.jeometry.coordinatesystem.model;

import java.util.ArrayList;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.jeometry.coordinatesystem.model.unit.LinearUnit;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;

public class CompoundCoordinateSystem extends AbstractCoordinateSystem {

  private static List<Axis> getAxis(final CoordinateSystem horizontalCoordinateSystem,
    final VerticalCoordinateSystem verticalCoordinateSystem) {
    final List<Axis> axis = new ArrayList<>();
    axis.addAll(horizontalCoordinateSystem.getAxis());
    axis.addAll(verticalCoordinateSystem.getAxis());
    return axis;
  }

  private final HorizontalCoordinateSystem horizontalCoordinateSystem;

  private final VerticalCoordinateSystem verticalCoordinateSystem;

  public CompoundCoordinateSystem(final HorizontalCoordinateSystem horizontalCoordinateSystem,
    final VerticalCoordinateSystem verticalCoordinateSystem) {
    this(0,
      horizontalCoordinateSystem.getCoordinateSystemName() + " + "
        + verticalCoordinateSystem.getCoordinateSystemName(),
      horizontalCoordinateSystem, verticalCoordinateSystem, null, false);
  }

  public CompoundCoordinateSystem(final int id, final String name,
    final HorizontalCoordinateSystem horizontalCoordinateSystem,
    final VerticalCoordinateSystem verticalCoordinateSystem, final Area area,
    final boolean deprecated) {
    super(id, name, getAxis(horizontalCoordinateSystem, verticalCoordinateSystem), area,
      deprecated);
    this.horizontalCoordinateSystem = horizontalCoordinateSystem;
    this.verticalCoordinateSystem = verticalCoordinateSystem;
  }

  @Override
  public CompoundCoordinateSystem clone() {
    try {
      return (CompoundCoordinateSystem)super.clone();
    } catch (final Exception e) {
      return null;
    }
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof CompoundCoordinateSystem) {
      final CompoundCoordinateSystem cs = (CompoundCoordinateSystem)object;
      if (!equals(this.horizontalCoordinateSystem, cs.horizontalCoordinateSystem)) {
        return false;
      } else if (!equals(this.verticalCoordinateSystem, cs.verticalCoordinateSystem)) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  public boolean equalsExact(final CompoundCoordinateSystem cs) {
    if (super.equalsExact(cs)) {
      if (!equals(this.horizontalCoordinateSystem, cs.horizontalCoordinateSystem)) {
        return false;
      } else if (!equals(this.verticalCoordinateSystem, cs.verticalCoordinateSystem)) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  @Override
  public boolean equalsExact(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem instanceof CompoundCoordinateSystem) {
      final CompoundCoordinateSystem compoundCoordinateSystem = (CompoundCoordinateSystem)coordinateSystem;
      return equalsExact(compoundCoordinateSystem);
    }
    return false;
  }

  @Override
  public CoordinatesOperation getCoordinatesOperation(final CoordinateSystem coordinateSystem) {
    return null;
  }

  @Override
  public CoordinateSystemType getCoordinateSystemType() {
    return CoordinateSystemType.COMPOUND;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <C extends CoordinateSystem> C getHorizontalCoordinateSystem() {
    return (C)this.horizontalCoordinateSystem;
  }

  @Override
  public int getHorizontalCoordinateSystemId() {
    if (this.horizontalCoordinateSystem == null) {
      return 0;
    } else {
      return this.horizontalCoordinateSystem.getHorizontalCoordinateSystemId();
    }
  }

  @Override
  public Unit<Length> getLengthUnit() {
    return this.horizontalCoordinateSystem.getLengthUnit();
  }

  @Override
  public LinearUnit getLinearUnit() {
    return this.horizontalCoordinateSystem.getLinearUnit();
  }

  @Override
  public <Q extends Quantity<Q>> Unit<Q> getUnit() {
    return this.horizontalCoordinateSystem.getUnit();
  }

  public VerticalCoordinateSystem getVerticalCoordinateSystem() {
    return this.verticalCoordinateSystem;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    if (this.horizontalCoordinateSystem != null) {
      result = prime * result + this.horizontalCoordinateSystem.hashCode();
    }
    if (this.verticalCoordinateSystem != null) {
      result = prime * result + this.verticalCoordinateSystem.hashCode();
    }
    return result;
  }

  public boolean isSame(final CompoundCoordinateSystem coordinateSystem) {
    if (coordinateSystem == null) {
      return false;
    } else {
      if (this.horizontalCoordinateSystem == null) {
        if (coordinateSystem.horizontalCoordinateSystem != null) {
          return false;
        }
      } else if (!this.horizontalCoordinateSystem
        .isSame(coordinateSystem.horizontalCoordinateSystem)) {
        return false;
      }
      if (this.verticalCoordinateSystem == null) {
        if (coordinateSystem.verticalCoordinateSystem != null) {
          return false;
        }
      } else if (!this.verticalCoordinateSystem.isSame(coordinateSystem.verticalCoordinateSystem)) {
        return false;
      }
      return true;
    }
  }

  @Override
  public boolean isSame(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem instanceof CompoundCoordinateSystem) {
      return isSame((CompoundCoordinateSystem)coordinateSystem);
    } else {
      return false;
    }
  }

}
