package com.revolsys.geometry.geoid;

public abstract class AbstractGeoidModel implements GeoidModel {

  private final String geoidName;

  public AbstractGeoidModel(final String geoidName) {
    this.geoidName = geoidName;
  }

  @Override
  public String getGeoidName() {
    return this.geoidName;
  }

  @Override
  public String toString() {
    return this.geoidName.toString();
  }
}
