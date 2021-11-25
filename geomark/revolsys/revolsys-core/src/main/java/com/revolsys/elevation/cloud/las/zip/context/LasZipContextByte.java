package com.revolsys.elevation.cloud.las.zip.context;

import com.revolsys.collection.ArrayUtil;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingCodec;
import com.revolsys.math.arithmeticcoding.ArithmeticDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticEncoder;
import com.revolsys.math.arithmeticcoding.ArithmeticModel;

public class LasZipContextByte {

  public boolean unused;

  public byte[] lastItem;

  private final ArithmeticModel[] m_bytes;

  public LasZipContextByte(final ArithmeticCodingCodec codec, final int size) {
    this.m_bytes = new ArithmeticModel[size];
    ArrayUtil.fill(this.m_bytes, () -> codec.createSymbolModelInit(256));
    this.lastItem = new byte[size];
  }

  public void initPoint(final ArithmeticCodingCodec codec, final LasPoint point) {
    this.unused = false;
    // TODO set item
  }

  public void read(final ArithmeticDecoder decoder, final LasPoint point) {
  }

  public void write(final ArithmeticEncoder encoder, final LasPoint point) {

  }

}
