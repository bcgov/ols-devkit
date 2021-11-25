package org.jeometry.coordinatesystem.model;

import java.io.Serializable;

public class BaseAuthority implements Serializable, Authority {
  /**
   *
   */
  private static final long serialVersionUID = 6255702398027894174L;

  private final String code;

  private final String name;

  public BaseAuthority(final String name, final int code) {
    this(name, String.valueOf(code));
  }

  public BaseAuthority(final String name, final String code) {
    this.name = name;
    this.code = code;
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof Authority) {
      final Authority authority = (Authority)object;
      if (!this.name.equals(authority.getName())) {
        return false;
      } else if (!this.code.equals(authority.getCode())) {
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
    return this.code;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.name.hashCode();
    result = prime * result + this.code.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return this.name + ":" + this.code;
  }
}
