package org.jeometry.coordinatesystem.model.unit;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.jeometry.coordinatesystem.model.Authority;

public class DegreeSexagesimalDMS extends AngularUnit {

  private static NumberFormat getFormat() {
    return new DecimalFormat("#0.00000##########################");
  }

  public DegreeSexagesimalDMS(final String name, final AngularUnit baseUnit,
    final double conversionFactor, final Authority authority, final boolean deprecated) {
    super(name, baseUnit, conversionFactor, authority, deprecated);
  }

  @Override
  public double toDegrees(final double value) {
    final String string = getFormat().format(value);
    final int dotIndex = string.indexOf('.');

    final int degrees = Integer.parseInt(string.substring(0, dotIndex));
    final int minutes = Integer.parseInt(string.substring(dotIndex + 1, dotIndex + 3));
    final double seconds = Double.parseDouble(
      string.substring(dotIndex + 3, dotIndex + 5) + "." + string.substring(dotIndex + 5));
    double decimal;
    if (value < 0) {
      decimal = degrees - minutes / 60.0 - seconds / 3600.0;
    } else {
      decimal = degrees + minutes / 60.0 + seconds / 3600.0;
    }
    return decimal;
  }

  @Override
  public double toNormal(final double value) {
    final String string = getFormat().format(value);
    final int dotIndex = string.indexOf('.');

    final int degrees = Integer.parseInt(string.substring(0, dotIndex));
    final int minutes = Integer.parseInt(string.substring(dotIndex + 1, dotIndex + 3));
    final double seconds = Double.parseDouble(
      string.substring(dotIndex + 3, dotIndex + 5) + "." + string.substring(dotIndex + 5));
    double decimal;
    if (value < 0) {
      decimal = degrees - minutes / 60.0 - seconds / 3600.0;
    } else {
      decimal = degrees + minutes / 60.0 + seconds / 3600.0;
    }
    return decimal;
  }

}
