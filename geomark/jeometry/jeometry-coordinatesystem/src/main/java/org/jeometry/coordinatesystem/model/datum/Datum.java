package org.jeometry.coordinatesystem.model.datum;

import org.jeometry.coordinatesystem.model.Area;
import org.jeometry.coordinatesystem.model.Authority;
import org.jeometry.coordinatesystem.util.Equals;

public class Datum {
  private final Authority authority;

  private final boolean deprecated;

  private final String name;

  private final Area area;

  public Datum(final Authority authority, final String name, final Area area,
    final boolean deprecated) {
    this.authority = authority;
    this.name = name;
    this.area = area;
    this.deprecated = deprecated;
  }

  public boolean equals(final Datum object) {
    if (object instanceof Datum) {
      final Datum datum = object;
      if (Equals.equals(this.authority, datum.authority)) {
        return true;
      } else if (this.name == null) {
        if (datum.name != null) {
          return false;
        }
      } else {
        return true;
      }
    }
    return false;
  }

  public boolean equalsExact(final Datum datum) {
    if (!Equals.equals(this.authority, datum.authority)) {
      return false;
    } else if (this.deprecated != datum.deprecated) {
      return false;
    } else if (!Equals.equals(this.name, datum.name)) {
      return false;
    } else {
      return true;
    }
  }

  public Area getArea() {
    return this.area;
  }

  public Authority getAuthority() {
    return this.authority;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public int hashCode() {
    return this.authority.hashCode();
  }

  public boolean isDeprecated() {
    return this.deprecated;
  }

  public boolean isSame(final Datum datum) {
    if (datum == null) {
      return false;
    } else {
      return this.name.equalsIgnoreCase(datum.name);
    }
  }

  @Override
  public String toString() {
    return this.name;
  }
}
