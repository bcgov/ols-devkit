package com.revolsys.io.channels;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.revolsys.io.BaseCloseable;

public class InputStreamDataReader extends AbstractDataReader implements BaseCloseable {

  private final InputStream in;

  private final byte[] bytes;

  public InputStreamDataReader() {
    this((InputStream)null);
  }

  public InputStreamDataReader(final InputStream in) {
    this(in, new byte[8192]);
  }

  public InputStreamDataReader(final InputStream in, final byte[] bytes) {
    super(ByteBuffer.wrap(bytes), false);
    this.in = in;
    this.bytes = bytes;
  }

  @Override
  protected int readInternal(final ByteBuffer buffer) throws IOException {
    final int readCount = this.in.read(this.bytes);
    buffer.put(this.bytes, 0, readCount);
    return readCount;
  }

}
