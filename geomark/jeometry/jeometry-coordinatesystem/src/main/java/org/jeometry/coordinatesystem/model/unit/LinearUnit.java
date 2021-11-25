package org.jeometry.coordinatesystem.model.unit;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.jeometry.coordinatesystem.model.Authority;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;
import org.jeometry.coordinatesystem.util.Md5;

import tech.units.indriya.AbstractUnit;
import tech.units.indriya.unit.AlternateUnit;
import tech.units.indriya.unit.Units;

public class LinearUnit implements UnitOfMeasure {

  private static final Map<String, Unit<Length>> UNIT_BY_NAME = new HashMap<>();

  static {
    UNIT_BY_NAME.put("metre", Units.METRE);
    UNIT_BY_NAME.put("meter", Units.METRE);
    UNIT_BY_NAME.put("millimetre", CustomUnits.MILLIMETRE);
    UNIT_BY_NAME.put("centimetre", CustomUnits.CENTIMETRE);
    UNIT_BY_NAME.put("kilometre", CustomUnits.KILOMETRE);
    UNIT_BY_NAME.put("inch", CustomUnits.INCH);
    UNIT_BY_NAME.put("foot", CustomUnits.FOOT);
    UNIT_BY_NAME.put("yard", CustomUnits.YARD);
    UNIT_BY_NAME.put("us survey foot", CustomUnits.FOOT_SURVEY);
    UNIT_BY_NAME.put("foot_us", CustomUnits.FOOT_SURVEY);
    UNIT_BY_NAME.put("link_clarke", CustomUnits.CLARKES_LINK);
    UNIT_BY_NAME.put("foot_clarke", CustomUnits.CLARKES_FOOT);
    UNIT_BY_NAME.put("chain_clarke", CustomUnits.CLARKES_CHAIN);
    UNIT_BY_NAME.put("yard_clarke", CustomUnits.CLARKES_YARD);
    UNIT_BY_NAME.put("foot_sears", CustomUnits.SEARS_FOOT);
    UNIT_BY_NAME.put("chain_sears", CustomUnits.SEARS_CHAIN);
    UNIT_BY_NAME.put("yard_sears", CustomUnits.SEARS_YARD);
    UNIT_BY_NAME.put("chain_sears_1922_truncated", CustomUnits.SEARS_CHAIN_TRUNCATED);
    UNIT_BY_NAME.put("yard_indian", CustomUnits.INDIAN_YARD);
    UNIT_BY_NAME.put("chain_benoit_1895_b", CustomUnits.BRITISH_CHAIN_BENOIT_1895_B);
    UNIT_BY_NAME.put("yard_indian_1937", CustomUnits.INDIAN_YARD_1937);
    UNIT_BY_NAME.put("foot_british_1936", CustomUnits.BRITISH_FOOT_1936);
    UNIT_BY_NAME.put("foot_gold_coast", CustomUnits.GOLD_COAST_FOOT);
    UNIT_BY_NAME.put("50_kilometers", Units.METRE.multiply(50000));
    UNIT_BY_NAME.put("150_kilometers", Units.METRE.multiply(150000));
  }

  private final Authority authority;

  private final LinearUnit baseUnit;

  private final double conversionFactor;

  private final boolean deprecated;

  private final String name;

  private Unit<Length> unit;

  public CoordinatesOperation fromMetresOperation = this::fromMetres;

  public CoordinatesOperation toMetresOperation = this::toMetres;

  public LinearUnit(final String name, final double conversionFactor) {
    this(name, null, conversionFactor, null, false);
  }

  public LinearUnit(final String name, final double conversionFactor, final Authority authority) {
    this(name, null, conversionFactor, authority, false);
  }

  public LinearUnit(final String name, final LinearUnit baseUnit, final double conversionFactor,
    final Authority authority, final boolean deprecated) {
    this.name = name;
    this.baseUnit = baseUnit;
    this.conversionFactor = conversionFactor;
    this.authority = authority;
    this.deprecated = deprecated;
    this.unit = UNIT_BY_NAME.get(name.toLowerCase());
    if (this.unit == null) {
      if (baseUnit == null) {
        if (conversionFactor == 1) {
          this.unit = new AlternateUnit<>(AbstractUnit.ONE, name);
        } else {
          System.err.println("Invalid conversion factor for " + name);
        }
      } else if (Double.isFinite(conversionFactor)) {
        this.unit = baseUnit.getUnit().multiply(conversionFactor);
      } else {
        this.unit = baseUnit.getUnit();
      }
    }
  }

  public void addFromMetresOperation(final List<CoordinatesOperation> operations) {
    operations.add(this.fromMetresOperation);
  }

  public void addToMetresOperation(final List<CoordinatesOperation> operations) {
    operations.add(this.toMetresOperation);
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof LinearUnit) {
      final LinearUnit unit = (LinearUnit)object;
      if (this.name == null && unit.name != null || !this.name.equals(unit.name)) {
        return false;
      } else if (Math.abs(this.conversionFactor - unit.conversionFactor) > 1.0e-10) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  public void fromMetres(final CoordinatesOperationPoint point) {
    point.x = fromMetres(point.x);
    point.y = fromMetres(point.y);
  }

  public double fromMetres(final double value) {
    final double baseValue;
    if (Double.isFinite(this.conversionFactor)) {
      baseValue = value / this.conversionFactor;
    } else {
      baseValue = value;
    }
    if (this.baseUnit == null) {
      return baseValue;
    } else {
      return this.baseUnit.fromMetres(baseValue);
    }
  }

  @Override
  public Authority getAuthority() {
    return this.authority;
  }

  public LinearUnit getBaseUnit() {
    return this.baseUnit;
  }

  public double getConversionFactor() {
    return this.conversionFactor;
  }

  public String getLabel() {
    String unitLabel = this.unit.getSymbol();
    if (unitLabel == null) {
      unitLabel = this.unit.getName();
    }
    return unitLabel;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public UnitOfMeasureType getType() {
    return UnitOfMeasureType.LINEAR;
  }

  public Unit<Length> getUnit() {
    return this.unit;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.name.hashCode();
    final long temp = Double.doubleToLongBits(this.conversionFactor);
    result = prime * result + (int)(temp ^ temp >>> 32);
    return result;
  }

  public boolean isDeprecated() {
    return this.deprecated;
  }

  @Override
  public double toBase(final double value) {
    final double baseValue;
    if (Double.isFinite(this.conversionFactor)) {
      baseValue = value * this.conversionFactor;
    } else {
      baseValue = value;
    }
    if (this.baseUnit == null) {
      return baseValue;
    } else {
      return this.baseUnit.toBase(baseValue);
    }
  }

  public void toMetres(final CoordinatesOperationPoint point) {
    point.x = toMetres(point.x);
    point.y = toMetres(point.y);
  }

  public double toMetres(final double value) {
    final double baseValue;
    if (Double.isFinite(this.conversionFactor)) {
      baseValue = value * this.conversionFactor;
    } else {
      baseValue = value;
    }
    if (this.baseUnit == null) {
      return baseValue;
    } else {
      return this.baseUnit.toMetres(baseValue);
    }
  }

  @Override
  public double toNormal(final double value) {
    final double baseValue;
    if (Double.isFinite(this.conversionFactor)) {
      baseValue = value * this.conversionFactor;
    } else {
      baseValue = value;
    }
    if (this.baseUnit == null) {
      return Math.round(value * 1.0e12) / 1.0e12;
    } else {
      return this.baseUnit.toBase(baseValue);
    }
  }

  @Override
  public String toString() {
    return this.name;
  }

  public void updateDigest(final MessageDigest digest) {
    if ("Meter".equals(this.name)) {
      Md5.update(digest, "metre");
    } else {
      Md5.update(digest, this.name.toLowerCase());
    }
  }
}
