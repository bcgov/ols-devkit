package com.revolsys.io;

import java.io.IOException;
import java.io.InputStream;

public class MultipleInputStream extends InputStream {

  private InputStream[] streams;

  private int inIndex = 0;

  private InputStream currentIn;

  public MultipleInputStream(InputStream... ins) {
    if (ins == null || ins.length == 0) {
      ins = new InputStream[0];
      this.currentIn = null;
    } else {
      this.streams = ins;
      this.currentIn = ins[0];
    }
  }

  @Override
  public void close() throws IOException {
    if (this.currentIn != null) {
      this.currentIn.close();
    }
  }

  @Override
  public int read() throws IOException {
    if (this.currentIn == null) {
      return -1;
    } else {
      int value = this.currentIn.read();
      while (value == -1) {
        this.currentIn.close();
        try {
          this.currentIn = this.streams[++this.inIndex];
          value = this.currentIn.read();
        } catch (final ArrayIndexOutOfBoundsException e) {
          this.currentIn = null;
          return -1;
        }
      }

      return value;
    }
  }

}
