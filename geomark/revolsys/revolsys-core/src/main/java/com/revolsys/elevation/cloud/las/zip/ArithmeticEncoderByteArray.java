package com.revolsys.elevation.cloud.las.zip;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.math.arithmeticcoding.ArithmeticEncoder;

public class ArithmeticEncoderByteArray extends ArithmeticEncoder {

  private final ByteArrayOutputStream out = new ByteArrayOutputStream();

  public ArithmeticEncoderByteArray() {
    setWriter(new ChannelWriter(this.out));
  }

  public int getByteCount() {
    return this.out.size();
  }

  public byte[] getBytes() {
    return this.out.toByteArray();
  }

  @Override
  public void init() {
    super.init();
    this.out.reset();
  }

  public int writeBytes(final ChannelWriter writer) {
    final byte[] bytes = getBytes();
    writer.putBytes(bytes);
    return bytes.length;
  }

  public int writeBytes(final ChannelWriter writer, final boolean enabled) {
    if (enabled) {
      final byte[] bytes = getBytes();
      writer.putBytes(bytes);
      return bytes.length;
    } else {
      return 0;
    }
  }

  public int writeSize(final ChannelWriter out, final boolean enabled) {
    int size;
    if (enabled) {
      done();
      size = this.out.size();
    } else {
      size = 0;
    }
    out.putInt(size);
    return size;
  }
}
