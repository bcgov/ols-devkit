package com.revolsys.util;

public class Pair<A, B> {
  public static <A, B> Pair<A, B> newPair(final A value1, final B value2) {
    return new Pair<>(value1, value2);
  }

  private A value1;

  private B value2;

  public Pair() {
  }

  public Pair(final A value1, final B value2) {
    this.value1 = value1;
    this.value2 = value2;
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
      @SuppressWarnings("rawtypes")
      final Pair other = (Pair)obj;
      if (this.value1 == null) {
        if (other.value1 != null) {
          return false;
        }
      } else if (!this.value1.equals(other.value1)) {
        return false;
      }
      if (this.value2 == null) {
        if (other.value2 != null) {
          return false;
        }
      } else if (!this.value2.equals(other.value2)) {
        return false;
      }
      return true;
    }
  }

  public A getValue1() {
    return this.value1;
  }

  public B getValue2() {
    return this.value2;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (this.value1 == null ? 0 : this.value1.hashCode());
    result = prime * result + (this.value2 == null ? 0 : this.value2.hashCode());
    return result;
  }

  public boolean isEmpty() {
    return this.value1 == null && this.value2 == null;
  }

  public void setValue1(final A value1) {
    this.value1 = value1;
  }

  public void setValue2(final B value2) {
    this.value2 = value2;
  }

  @Override
  public String toString() {
    return this.value1 + ", " + this.value2;
  }
}
