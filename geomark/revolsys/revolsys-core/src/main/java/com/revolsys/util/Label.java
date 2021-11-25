package com.revolsys.util;

/**
 * A label contains a string but is not equal to another label with the same string.
 *
 */
public class Label {

  private final String label;

  public Label(final String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return this.label;
  }
}
