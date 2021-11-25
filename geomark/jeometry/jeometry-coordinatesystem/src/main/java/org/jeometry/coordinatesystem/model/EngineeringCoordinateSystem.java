package org.jeometry.coordinatesystem.model;

import java.util.List;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.jeometry.coordinatesystem.model.datum.EngineeringDatum;
import org.jeometry.coordinatesystem.model.datum.GeodeticDatum;
import org.jeometry.coordinatesystem.model.unit.LinearUnit;
import org.jeometry.coordinatesystem.model.unit.UnitOfMeasure;

public class EngineeringCoordinateSystem extends AbstractHorizontalCoordinateSystem {
  private final UnitOfMeasure unit;

  private final EngineeringDatum engineeringDatum;

  public EngineeringCoordinateSystem(final int id, final String name,
    final EngineeringDatum engineeringDatum, final List<Axis> axis, final Area area,
    final boolean deprecated) {
    super(id, name, axis, area, deprecated);
    this.engineeringDatum = engineeringDatum;
    this.unit = axis.get(0).getUnit();
  }

  @Override
  public EngineeringCoordinateSystem clone() {
    try {
      return (EngineeringCoordinateSystem)super.clone();
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
    } else if (object instanceof EngineeringCoordinateSystem) {
      final EngineeringCoordinateSystem cs = (EngineeringCoordinateSystem)object;
      if (!equals(this.engineeringDatum, cs.engineeringDatum)) {
        return false;
      } else if (!equals(this.unit, cs.unit)) {
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
    if (coordinateSystem instanceof EngineeringCoordinateSystem) {
      final EngineeringCoordinateSystem engineeringCoordinateSystem = (EngineeringCoordinateSystem)coordinateSystem;
      return equalsExact(engineeringCoordinateSystem);
    }
    return false;
  }

  public boolean equalsExact(final EngineeringCoordinateSystem cs) {
    if (super.equalsExact(cs)) {
      if (!equals(this.unit, cs.unit)) {
        return false;
      } else if (!equals(this.engineeringDatum, cs.engineeringDatum)) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  @Override
  public CoordinateSystemType getCoordinateSystemType() {
    return CoordinateSystemType.ENGINEERING;
  }

  public EngineeringDatum getDatum() {
    return this.engineeringDatum;
  }

  @Override
  public GeodeticDatum getGeodeticDatum() {
    return null;
  }

  @Override
  public Unit<Length> getLengthUnit() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LinearUnit getLinearUnit() {
    if (this.unit instanceof LinearUnit) {
      return (LinearUnit)this.unit;
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <Q extends Quantity<Q>> Unit<Q> getUnit() {
    if (this.unit instanceof LinearUnit) {
      final LinearUnit linearUnit = (LinearUnit)this.unit;
      return (Unit<Q>)linearUnit.getUnit();
    } else {
      return null;
    }
  }

  @Override
  public String getUnitLabel() {
    return this.unit.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    if (this.engineeringDatum != null) {
      result = prime * result + this.engineeringDatum.hashCode();
    }
    return result;
  }

  @Override
  public boolean isSame(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem instanceof EngineeringCoordinateSystem) {
      return isSame((EngineeringCoordinateSystem)coordinateSystem);
    } else {
      return false;
    }
  }

  public boolean isSame(final EngineeringCoordinateSystem coordinateSystem) {
    if (this.engineeringDatum.isSame(coordinateSystem.engineeringDatum)) {
      if (this.unit.isSame(coordinateSystem.unit)) {
        return true;
      }
    }
    return false;
  }

}
