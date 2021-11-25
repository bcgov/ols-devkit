package org.jeometry.coordinatesystem.model;

public class AxisName {
  private final int id;

  private final String name;

  public AxisName(final int id, final String name) {
    this.id = id;
    this.name = name;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof AxisName) {
      final AxisName axisName = (AxisName)obj;
      return axisName.id == this.id;
    } else {
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  public <C> C getCode() {
    return (C)(Integer)this.id;
  }

  public String getDescription() {
    return this.name;
  }

  public int getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public int hashCode() {
    return this.id;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
