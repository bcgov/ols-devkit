package com.revolsys.util;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.coordinatesystem.model.unit.CustomUnits;

import tech.units.indriya.quantity.Quantities;

public interface QuantityType {
  static <Q extends Quantity<Q>> double convertValue(final Unit<Q> fromUnit, final double value,
    final Unit<Q> toUnit) {
    return Quantities.getQuantity(value, fromUnit).to(toUnit).getValue().doubleValue();
  }

  static <Q extends Quantity<Q>> double doubleValue(final Quantity<Q> value, final Unit<Q> unit) {
    return value.to(unit).getValue().doubleValue();
  }

  static Quantity<?> newQuantity(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Quantity) {
      return (Quantity<?>)value;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return Quantities.getQuantity(number.doubleValue(), CustomUnits.PIXEL);
    } else {
      final String string = DataTypes.toString(value);
      return newQuantity(string);
    }
  }

  static Quantity<?> newQuantity(final String string) {
    if (Property.hasValue(string)) {
      final Quantity<?> measure = Quantities.getQuantity(string);
      final Number value = measure.getValue();
      final Unit<?> unit = measure.getUnit();
      return Quantities.getQuantity(value.doubleValue(), unit);
    } else {
      return null;
    }
  }

  static String toString(final Object value) {
    if (value == null) {
      return null;
    } else {
      final Quantity<?> measure = newQuantity(value);
      final double doubleValue = measure.getValue().doubleValue();
      if (Double.isInfinite(doubleValue) || Double.isNaN(doubleValue)) {
        return String.valueOf(doubleValue) + " " + measure.getUnit();
      } else {
        return measure.toString();
      }
    }
  }
}
