package ca.bc.gov.geomark.client.util;

import java.io.UnsupportedEncodingException;

/**
 * Utilities for base-64 encoding.
 */
public class Base64 {
  /** The Base-64 alphabet. */
  private final static byte[] ALPHABET = {
    (byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F',
    (byte)'G', (byte)'H', (byte)'I', (byte)'J', (byte)'K', (byte)'L',
    (byte)'M', (byte)'N', (byte)'O', (byte)'P', (byte)'Q', (byte)'R',
    (byte)'S', (byte)'T', (byte)'U', (byte)'V', (byte)'W', (byte)'X',
    (byte)'Y', (byte)'Z', (byte)'a', (byte)'b', (byte)'c', (byte)'d',
    (byte)'e', (byte)'f', (byte)'g', (byte)'h', (byte)'i', (byte)'j',
    (byte)'k', (byte)'l', (byte)'m', (byte)'n', (byte)'o', (byte)'p',
    (byte)'q', (byte)'r', (byte)'s', (byte)'t', (byte)'u', (byte)'v',
    (byte)'w', (byte)'x', (byte)'y', (byte)'z', (byte)'0', (byte)'1',
    (byte)'2', (byte)'3', (byte)'4', (byte)'5', (byte)'6', (byte)'7',
    (byte)'8', (byte)'9', (byte)'+', (byte)'/'
  };

  /** The equals sign as a byte. */
  private final static byte EQUALS_SIGN = (byte)'=';

  /**
   * Encodes the bytes using base-64 encoding as a UTF-8 string.
   * 
   * @param source The source bytes.
   * @return The base-64 encoded string.
   */
  public static String encode(final byte[] source) {
    int len = source.length;
    final int len43 = len * 4 / 3;
    final byte[] outBuff = new byte[(len43) + ((len % 3) > 0 ? 4 : 0)];
    int index = 0;
    int encodedLength = 0;
    final int len2 = len - 2;
    for (; index < len2; index += 3, encodedLength += 4) {
      encode3to4(source, index, 3, outBuff, encodedLength);
    }

    if (index < len) {
      encode3to4(source, index, len - index, outBuff, encodedLength);
      encodedLength += 4;
    }

    try {
      return new String(outBuff, 0, encodedLength, "UTF-8");
    } catch (final UnsupportedEncodingException e) {
      throw new RuntimeException("Unable to get UTF-8 encoding", e);
    }
  }

  /**
   * Encode three bytes as four base-64 bytes.
   * 
   * @param source The source bytes.
   * @param srcOffset The offset into the source bytes.
   * @param numSigBytes The number of bytes to read from the source (max 3, min
   *          1).
   * @param destination The destination bytes.
   * @param destOffset The offset into the destination bytes.
   */
  private static void encode3to4(final byte[] source, final int srcOffset,
    final int numSigBytes, final byte[] destination, final int destOffset) {

    final int inBuff = (numSigBytes > 0 ? ((source[srcOffset] << 24) >>> 8) : 0)
      | (numSigBytes > 1 ? ((source[srcOffset + 1] << 24) >>> 16) : 0)
      | (numSigBytes > 2 ? ((source[srcOffset + 2] << 24) >>> 24) : 0);

    destination[destOffset] = ALPHABET[(inBuff >>> 18)];
    destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];

    if (numSigBytes > 1) {
      destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 0x3f];
    } else {
      destination[destOffset + 2] = EQUALS_SIGN;
    }
    if (numSigBytes > 2) {
      destination[destOffset + 3] = ALPHABET[(inBuff) & 0x3f];
    } else {
      destination[destOffset + 3] = EQUALS_SIGN;
    }

  }

}
