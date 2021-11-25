package com.revolsys.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DelegatingInputStream extends InputStream {

  private final InputStream in;

  public DelegatingInputStream(final InputStream in) {
    this.in = in;
  }

  @Override
  public int available() throws IOException {
    return this.in.available();
  }

  @Override
  public void close() throws IOException {
    this.in.close();
  }

  @Override
  public synchronized void mark(final int readlimit) {
    this.in.mark(readlimit);
  }

  @Override
  public boolean markSupported() {
    return this.in.markSupported();
  }

  @Override
  public int read() throws IOException {
    return this.in.read();
  }

  @Override
  public int read(final byte[] b) throws IOException {
    return this.in.read(b);
  }

  @Override
  public int read(final byte[] b, final int off, final int len) throws IOException {
    return this.in.read(b, off, len);
  }

  @Override
  public byte[] readAllBytes() throws IOException {
    return this.in.readAllBytes();
  }

  @Override
  public int readNBytes(final byte[] b, final int off, final int len) throws IOException {
    return this.in.readNBytes(b, off, len);
  }

  @Override
  public byte[] readNBytes(final int len) throws IOException {
    return this.in.readNBytes(len);
  }

  @Override
  public synchronized void reset() throws IOException {
    this.in.reset();
  }

  @Override
  public long skip(final long n) throws IOException {
    return this.in.skip(n);
  }

  @Override
  public long transferTo(final OutputStream out) throws IOException {
    return this.in.transferTo(out);
  }

}
