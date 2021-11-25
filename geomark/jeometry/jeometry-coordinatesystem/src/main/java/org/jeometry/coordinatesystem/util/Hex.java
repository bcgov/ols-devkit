package org.jeometry.coordinatesystem.util;

import java.security.MessageDigest;

public class Hex {
  public static byte[] toDecimalBytes(final String hexString) {
    final byte[] bytes = new byte[hexString.length() / 2];
    for (int i = 0; i < bytes.length; i++) {
      final int index = i * 2;
      final String hexChar = hexString.substring(index, index + 2);
      final int character = Integer.parseInt(hexChar, 16);
      bytes[i] = (byte)character;
    }
    return bytes;
  }

  public static String toHex(final byte[] bytes) {
    final StringBuilder hexString = new StringBuilder();
    for (final byte c : bytes) {
      final String hexChar = Integer.toString((c & 0xff) + 0x100, 16).substring(1);
      hexString.append(hexChar);
    }
    return hexString.toString();
  }

  public static String toHex(final MessageDigest messageDigest) {
    final byte[] digest = messageDigest.digest();
    return toHex(digest);
  }

  public static String toHex(final String string) {
    final StringBuilder hexString = new StringBuilder();
    for (final char c : string.toCharArray()) {
      final String hexChar = Integer.toHexString(c);
      hexString.append(hexChar);
    }
    return hexString.toString();
  }
}
