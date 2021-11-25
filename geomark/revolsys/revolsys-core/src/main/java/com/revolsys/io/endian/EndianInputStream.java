package com.revolsys.io.endian;

import java.io.DataInputStream;
import java.io.InputStream;

public class EndianInputStream extends DataInputStream implements EndianInput {
  public EndianInputStream(final InputStream in) {
    super(in);
  }
}
