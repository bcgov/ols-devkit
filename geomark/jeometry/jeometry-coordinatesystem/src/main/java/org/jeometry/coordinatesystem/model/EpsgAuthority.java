package org.jeometry.coordinatesystem.model;

import java.io.Serializable;

public class EpsgAuthority implements Authority, Serializable {
  private static final long serialVersionUID = 6255702398027894174L;

  private final int id;

  public EpsgAuthority(final int id) {
    this.id = id;
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof EpsgAuthority) {
      final EpsgAuthority authority = (EpsgAuthority)object;
      return this.id == authority.id;
    } else if (object instanceof Authority) {
      final Authority authority = (Authority)object;
      if (!getName().equals(authority.getName())) {
        return false;
      } else if (!getCode().equals(authority.getCode())) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  @Override
  public String getCode() {
    return String.valueOf(this.id);
  }

  @Override
  public int getId() {
    return this.id;
  }

  @Override
  public String getName() {
    return "EPSG";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + getName().hashCode();
    result = prime * result + this.id;
    return result;
  }

  @Override
  public String toString() {
    return getName() + ":" + this.id;
  }
}
