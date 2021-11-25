package com.revolsys.util;

import java.util.List;

import com.revolsys.collection.list.Lists;

public enum Parity {
  CONTINUOUS("Continuous"), EVEN("Even"), ODD("Odd");

  public static List<Parity> VALUES = Lists.newArray(EVEN, ODD);

  public static Parity getParity(final String code) {
    if (Property.hasValue(code)) {
      switch (Character.toUpperCase(code.charAt(0))) {
        case 'E':
          return Parity.EVEN;
        case 'O':
          return Parity.ODD;
        case 'C':
          return Parity.CONTINUOUS;
        default:
          return null;
      }
    }
    return null;
  }

  public static boolean isEven(final Parity parity) {
    return parity == EVEN;
  }

  public static boolean isOdd(final Parity parity) {
    return parity == ODD;
  }

  public static Parity opposite(final Parity parity) {
    if (parity == null) {
      return null;
    } else {
      return parity.opposite();
    }
  }

  private char letter;

  private String name;

  private Parity(final String name) {
    this.name = name;
    this.letter = name.charAt(0);
  }

  public char getLetter() {
    return this.letter;
  }

  public String getName() {
    return this.name;
  }

  public boolean isEven() {
    return this == EVEN;
  }

  public boolean isOdd() {
    return this == ODD;
  }

  public Parity opposite() {
    if (this == EVEN) {
      return ODD;
    } else {
      return EVEN;
    }
  }

  @Override
  public String toString() {
    return this.name;
  }
}
