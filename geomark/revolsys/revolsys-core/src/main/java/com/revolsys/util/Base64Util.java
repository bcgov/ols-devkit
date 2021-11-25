package com.revolsys.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import com.revolsys.io.IgnoreCloseDelegatingInputStream;
import com.revolsys.io.IgnoreCloseDelegatingOutputStream;

public class Base64Util {

  public static String decodeToString(final String string) {
    final byte[] data = Base64.getDecoder().decode(string);
    return new String(data);
  }

  public static String encodeToString(final String string) {
    final byte[] bytes = string.getBytes();
    return Base64.getEncoder().encodeToString(bytes);
  }

  public static InputStream wrap(final InputStream in, final boolean close) {
    final Decoder decoder = Base64.getDecoder();
    return wrap(in, close, decoder);
  }

  public static InputStream wrap(InputStream in, final boolean close, final Decoder decoder) {
    if (!close) {
      in = new IgnoreCloseDelegatingInputStream(in);
    }
    return decoder.wrap(in);
  }

  public static OutputStream wrap(final OutputStream out, final boolean close) {
    final Encoder encoder = Base64.getEncoder();
    return wrap(out, close, encoder);
  }

  public static OutputStream wrap(OutputStream out, final boolean close, final Encoder encoder) {
    if (!close) {
      out = new IgnoreCloseDelegatingOutputStream(out);
    }
    return encoder.wrap(out);
  }

  public static InputStream wrapMime(final InputStream in, final boolean close) {
    final Decoder decoder = Base64.getMimeDecoder();
    return wrap(in, close, decoder);
  }

  public static OutputStream wrapMime(final OutputStream out, final boolean close) {
    final Encoder encoder = Base64.getMimeEncoder();
    return wrap(out, close, encoder);
  }
}
