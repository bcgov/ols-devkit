package com.revolsys.elevation.cloud.las.zip;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteOrder;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.channels.DataReader;
import com.revolsys.math.arithmeticcoding.ArithmeticDecoder;

public class ArithmeticDecoderByteArray extends ArithmeticDecoder {

  private int size;

  private boolean enabled = true;

  public ArithmeticDecoderByteArray() {
  }

  public boolean isEnabled() {
    return this.enabled;
  }

  public boolean readBytes(final DataReader reader) {
    if (this.size <= 0) {
      return false;
    } else if (this.enabled) {
      if (this.size > 0) {
        final byte[] bytes = new byte[this.size];
        reader.getBytes(bytes);
        final InputStream in = new ByteArrayInputStream(bytes);
        final DataReader newReader = new ChannelReader(in);
        newReader.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        init(newReader, true);
      }
      return true;
    } else {
      reader.skipBytes(this.size);
      return false;
    }
  }

  public int readSize(final DataReader in) {
    this.size = in.getInt();
    return this.size;
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public void setEnabled(final int flags, final int flagMask) {
    this.enabled = (flags & flagMask) != 0;
  }

}
