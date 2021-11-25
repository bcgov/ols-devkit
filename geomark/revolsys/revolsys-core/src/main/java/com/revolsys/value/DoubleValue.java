package com.revolsys.value;

public class DoubleValue implements ValueHolder<Double> {

  public double value;

  public double addValue(final double value) {
    this.value += value;
    return value;
  }

  @Override
  public Double getValue() {
    return this.value;
  }

  public double setValue(final double value) {
    this.value = value;
    return value;
  }

  @Override
  public Double setValue(final Double value) {
    return value;
  }
}
