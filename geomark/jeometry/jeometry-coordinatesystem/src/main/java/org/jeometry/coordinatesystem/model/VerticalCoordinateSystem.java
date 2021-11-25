package org.jeometry.coordinatesystem.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.jeometry.coordinatesystem.model.datum.VerticalDatum;
import org.jeometry.coordinatesystem.model.unit.LinearUnit;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;

public class VerticalCoordinateSystem extends AbstractCoordinateSystem {

  private static int getId(final Authority authority) {
    if (authority == null) {
      return 0;
    } else {
      return authority.getId();
    }
  }

  private final LinearUnit linearUnit;

  private final VerticalDatum verticalDatum;

  private final Map<ParameterName, ParameterValue> parameterValues;

  public VerticalCoordinateSystem(final Authority authority, final String name,
    final VerticalDatum verticalDatum, final Map<ParameterName, ParameterValue> parameterValues,
    final LinearUnit linearUnit, final List<Axis> axis) {
    this(authority, name, verticalDatum, parameterValues, linearUnit, axis, null, false);
  }

  private VerticalCoordinateSystem(final Authority authority, final String name,
    final VerticalDatum verticalDatum, final Map<ParameterName, ParameterValue> parameterValues,
    final LinearUnit linearUnit, final List<Axis> axis, final Area area, final boolean deprecated) {
    super(getId(authority), name, axis, area, deprecated, authority);
    this.verticalDatum = verticalDatum;
    this.parameterValues = parameterValues;
    if (axis.size() > 0) {
      this.linearUnit = (LinearUnit)axis.get(0).getUnit();
    } else {
      this.linearUnit = linearUnit;
    }
  }

  public VerticalCoordinateSystem(final int id, final String name,
    final VerticalDatum verticalDatum, final List<Axis> axis, final Area area,
    final boolean deprecated) {
    super(id, name, axis, area, deprecated);
    this.verticalDatum = verticalDatum;
    if (axis.size() > 0) {
      this.linearUnit = (LinearUnit)axis.get(0).getUnit();
    } else {
      this.linearUnit = null;
    }
    this.parameterValues = Collections.emptyMap();
  }

  @Override
  public GeographicCoordinateSystem clone() {
    try {
      return (GeographicCoordinateSystem)super.clone();
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
    } else if (object instanceof VerticalCoordinateSystem) {
      final VerticalCoordinateSystem cs = (VerticalCoordinateSystem)object;
      if (!equals(this.verticalDatum, cs.verticalDatum)) {
        return false;
      } else if (!equals(this.linearUnit, cs.linearUnit)) {
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
    if (coordinateSystem instanceof VerticalCoordinateSystem) {
      final VerticalCoordinateSystem verticalCoordinateSystem = (VerticalCoordinateSystem)coordinateSystem;
      return equalsExact(verticalCoordinateSystem);
    }
    return false;
  }

  public boolean equalsExact(final VerticalCoordinateSystem cs) {
    if (super.equalsExact(cs)) {
      if (!equals(this.linearUnit, cs.linearUnit)) {
        return false;
      } else if (!equals(this.verticalDatum, cs.verticalDatum)) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  @Override
  public CoordinatesOperation getCoordinatesOperation(final CoordinateSystem coordinateSystem) {
    return null;
  }

  @Override
  public CoordinateSystemType getCoordinateSystemType() {
    return CoordinateSystemType.VERTICAL;
  }

  public VerticalDatum getDatum() {
    return this.verticalDatum;
  }

  @Override
  public int getHorizontalCoordinateSystemId() {
    return 0;
  }

  @Override
  public Unit<Length> getLengthUnit() {
    return this.linearUnit.getUnit();
  }

  @Override
  public LinearUnit getLinearUnit() {
    return this.linearUnit;
  }

  public Map<ParameterName, ParameterValue> getParameterValues() {
    return this.parameterValues;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Unit<Length> getUnit() {
    return this.linearUnit.getUnit();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    if (this.verticalDatum != null) {
      result = prime * result + this.verticalDatum.hashCode();
    }
    return result;
  }

  @Override
  public boolean isSame(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem instanceof VerticalCoordinateSystem) {
      return isSame((VerticalCoordinateSystem)coordinateSystem);
    } else {
      return false;
    }
  }

  public boolean isSame(final VerticalCoordinateSystem coordinateSystem) {
    if (this.verticalDatum.isSame(coordinateSystem.verticalDatum)) {
      if (this.linearUnit.isSame(coordinateSystem.linearUnit)) {
        return this.parameterValues.equals(coordinateSystem.parameterValues);
      }
    }
    return false;
  }

}
