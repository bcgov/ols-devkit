package com.revolsys.io;

import java.io.IOException;

public class StringPrinter {

  private final String string;

  public StringPrinter(final String string) {
    this.string = string;
  }

  public void write(final java.io.Writer out) {
    try {
      out.write(this.string);
    } catch (final IOException e) {
    }
  }
}
