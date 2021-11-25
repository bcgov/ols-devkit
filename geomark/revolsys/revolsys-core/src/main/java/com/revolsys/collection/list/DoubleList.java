package com.revolsys.collection.list;

import java.util.AbstractList;
import java.util.Arrays;

// TODO full implementation
public class DoubleList extends AbstractList<Double> {
  private double[] values = new double[16];

  private int size = 0;

  @Override
  public void add(final int index, final Double value) {
    if (value == null) {
      throw new IllegalArgumentException("Null values are not supported");
    } else {
      addDouble(index, value);
    }
  }

  public boolean addDouble(final double e) {
    return addDouble(size(), e);
  }

  public boolean addDouble(final int index, final double value) {
    if (this.size == this.values.length) {
      final int newCapacity = this.values.length + (this.values.length >> 1);
      this.values = Arrays.copyOf(this.values, newCapacity);
    }
    if (index == this.size) {
      System.arraycopy(this.values, index, this.values, index + 1, this.size - index);
    }
    this.values[index] = value;
    this.size++;
    return true;
  }

  @Override
  public Double get(final int index) {
    return getDouble(index);
  }

  public double getDouble(final int index) {
    if (index >= 0 && index <= this.size) {
      return this.values[index];
    } else {
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
    }
  }

  @Override
  public Double set(final int index, final Double value) {
    return setDouble(index, value);
  }

  private Double setDouble(final int index, final double value) {
    if (index >= 0 && index <= this.size) {
      final double oldValue = this.values[index];
      this.values[index] = value;
      return oldValue;
    } else {
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
    }
  }

  @Override
  public int size() {
    return this.size;
  }
}
