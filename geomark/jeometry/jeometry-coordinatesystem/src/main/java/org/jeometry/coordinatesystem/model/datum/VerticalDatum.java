package org.jeometry.coordinatesystem.model.datum;

import org.jeometry.coordinatesystem.model.Area;
import org.jeometry.coordinatesystem.model.Authority;

public class VerticalDatum extends Datum {
  private int datumType;

  public VerticalDatum(final Authority authority, final String name, final Area area,
    final boolean deprecated) {
    super(authority, name, area, deprecated);
  }

  public VerticalDatum(final Authority authority, final String name, final int datumType) {
    super(authority, name, null, false);
    this.datumType = datumType;
  }

  public int getDatumType() {
    return this.datumType;
  }

  @Override
  public boolean isSame(final Datum datum) {
    if (datum instanceof VerticalDatum) {
      return isSame((VerticalDatum)datum);
    }
    return false;
  }

  public boolean isSame(final VerticalDatum verticalDatum) {
    if (verticalDatum == null) {
      return false;
    } else {
      if (this.datumType == verticalDatum.datumType) {
        return super.isSame(verticalDatum);
      }
      return false;
    }
  }
}
