package com.revolsys.util;

import java.util.List;

import org.jeometry.common.exception.Exceptions;

public class IntPair implements Cloneable, Comparable<IntPair> {

  public static void add(final List<IntPair> list, final int value1, final int value2) {
    list.add(new IntPair(value1, value2));
  }

  private int value1;

  private int value2;

  public IntPair() {
  }

  public IntPair(final int value1, final int value2) {
    this.value1 = value1;
    this.value2 = value2;
  }

  @Override
  public IntPair clone() {
    try {
      return (IntPair)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public int compareTo(final IntPair other) {
    final int otherValue1 = other.getValue1();
    if (this.value1 == otherValue1) {
      final int otherValue2 = other.getValue2();
      if (this.value2 == otherValue2) {
        return 0;
      } else if (this.value2 < otherValue2) {
        return -1;
      } else {
        return 1;
      }
    } else if (this.value1 < otherValue1) {
      return -1;
    } else {
      return 1;
    }
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    } else if (obj == null) {
      return false;
    } else if (getClass() != obj.getClass()) {
      return false;
    } else {
      final IntPair other = (IntPair)obj;
      if (this.value1 != other.value1) {
        return false;
      } else if (this.value2 != other.value2) {
        return false;
      }
      return true;
    }
  }

  public int getValue1() {
    return this.value1;
  }

  public int getValue2() {
    return this.value2;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.value1;
    result = prime * result + this.value2;
    return result;
  }

  public void setValue1(final int value1) {
    this.value1 = value1;
  }

  public void setValue2(final int value2) {
    this.value2 = value2;
  }

  public void setValues(final int value1, final int value2) {
    this.value1 = value1;
    this.value2 = value2;
  }

  @Override
  public String toString() {
    return this.value1 + ", " + this.value2;
  }
}
