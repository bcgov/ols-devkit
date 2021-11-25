package org.jeometry.coordinatesystem.model;

import java.io.Serializable;

import org.jeometry.coordinatesystem.model.systems.EpsgCoordinateSystems;
import org.jeometry.coordinatesystem.model.unit.UnitOfMeasure;

public class Axis implements Serializable {
  private static final long serialVersionUID = 5463484439488623454L;

  private final String abbreviation;

  private final AxisName axisName;

  private final String orientation;

  private final UnitOfMeasure unit;

  public Axis(final AxisName axisName, final String orientation, final String abbreviation,
    final UnitOfMeasure unit) {
    this.axisName = axisName;
    this.orientation = orientation;
    this.abbreviation = abbreviation;
    this.unit = unit;
  }

  public Axis(final String name, final String direction) {
    this(EpsgCoordinateSystems.getAxisName(name), direction, null, null);
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof Axis) {
      final Axis axis = (Axis)object;
      if (!this.axisName.equals(axis.axisName)) {
        return false;
      } else if (!this.orientation.equals(axis.orientation)) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  public String getAbbreviation() {
    return this.abbreviation;
  }

  public AxisName getAxisName() {
    return this.axisName;
  }

  public String getName() {
    return this.axisName.getName();
  }

  public String getOrientation() {
    return this.orientation;
  }

  public UnitOfMeasure getUnit() {
    return this.unit;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.axisName.hashCode();
    result = prime * result + this.orientation.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return getName();
  }
}
