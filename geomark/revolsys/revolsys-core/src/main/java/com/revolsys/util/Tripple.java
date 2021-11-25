package com.revolsys.util;

public class Tripple<A, B, C> extends Pair<A, B> {
  private C value3;

  public Tripple() {
    super();
  }

  public Tripple(final A value1, final B value2, final C value3) {
    super(value1, value2);
    this.value3 = value3;
  }

  @Override
  public boolean equals(final Object obj) {
    if (super.equals(obj)) {
      @SuppressWarnings("rawtypes")
      final Tripple other = (Tripple)obj;
      if (this.value3 == null) {
        if (other.value3 != null) {
          return false;
        }
      } else if (!this.value3.equals(other.value3)) {
        return false;
      }

      return true;
    } else {
      return false;
    }
  }

  public C getValue3() {
    return this.value3;
  }

  public void setValue3(final C value3) {
    this.value3 = value3;
  }

  @Override
  public String toString() {
    return super.toString() + ", " + this.value3;
  }
}
