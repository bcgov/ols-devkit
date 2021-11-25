package org.jeometry.coordinatesystem.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5 {

  public static MessageDigest getMessageDigest() {
    try {
      return MessageDigest.getInstance("MD5");
    } catch (final NoSuchAlgorithmException e) {
      throw new RuntimeException("MD5 Digest not found", e);
    }
  }

  public static DigestInputStream inputStream(final InputStream in) {
    final MessageDigest messageDigest = getMessageDigest();
    return new DigestInputStream(in, messageDigest);

  }

  public static byte[] md5(final byte[] data) {
    final MessageDigest messageDigest = getMessageDigest();
    return messageDigest.digest(data);
  }

  public static byte[] md5(final InputStream data) throws IOException {
    final MessageDigest digest = getMessageDigest();
    final int bufferSize = 1024;
    final byte[] buffer = new byte[bufferSize];
    int read = data.read(buffer, 0, bufferSize);

    while (read > -1) {
      digest.update(buffer, 0, read);
      read = data.read(buffer, 0, bufferSize);
    }

    return digest.digest();
  }

  public static byte[] md5(final String data) {
    return md5(data.getBytes(StandardCharsets.UTF_8));
  }

  public static String md5Hex(final byte[] data) {
    final byte[] md5 = md5(data);
    return Hex.toHex(md5);
  }

  public static String md5Hex(final InputStream data) throws IOException {
    final byte[] md5 = md5(data);
    return Hex.toHex(md5);
  }

  public static String md5Hex(final String data) {
    final byte[] md5 = md5(data);
    return Hex.toHex(md5);
  }

  public static void update(final MessageDigest digest, final double value) {
    final long l = Double.doubleToLongBits(value);
    final String data = Long.toString(l);
    update(digest, data);
  }

  public static void update(final MessageDigest digest, final String data) {
    final byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
    digest.update(bytes);
  }
}
