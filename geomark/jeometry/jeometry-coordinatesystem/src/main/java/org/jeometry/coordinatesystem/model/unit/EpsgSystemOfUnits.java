package org.jeometry.coordinatesystem.model.unit;

import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import tech.units.indriya.AbstractSystemOfUnits;

public class EpsgSystemOfUnits extends AbstractSystemOfUnits {
  public EpsgSystemOfUnits() {
  }

  public void addUnit(final UnitOfMeasure unitOfMeasure, final String name, final String symbol) {
    if (unitOfMeasure instanceof AngularUnit) {
      final AngularUnit angularUnit = (AngularUnit)unitOfMeasure;
      final Unit<Angle> unit = angularUnit.getUnit();
      Helper.addUnit(this.units, unit, name, symbol);
    } else if (unitOfMeasure instanceof LinearUnit) {
      final LinearUnit linearUnit = (LinearUnit)unitOfMeasure;
      final Unit<Length> unit = linearUnit.getUnit();
      Helper.addUnit(this.units, unit, name, symbol);
    }

  }

  @Override
  public String getName() {
    return "EPSG Units";
  }
}
