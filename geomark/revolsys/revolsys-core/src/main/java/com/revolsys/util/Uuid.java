package com.revolsys.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.number.Longs;

public class Uuid {
  public static UuidBuilder builder() {
    return new UuidBuilder();
  }

  public static UuidBuilder builder(final String namespace) {
    return new UuidBuilder(namespace);
  }

  // use sha1 instead
  public static final UUID md5(final String namespace, final byte[] name) {
    return name(3, namespace, name);
  }

  // use sha1 instead
  public static final UUID md5(final String namespace, final Object name) {
    return name(3, namespace, name);
  }

  // use sha1 instead
  public static final UUID md5(final String namespace, final String name) {
    return name(3, namespace, name);
  }

  private static UUID name(final int type, final String namespace, final byte[] name) {
    try {
      final MessageDigest digester;
      if (type == 3) {
        digester = MessageDigest.getInstance("MD5");
      } else if (type == 5) {
        digester = MessageDigest.getInstance("SHA-1");
      } else {
        throw new IllegalArgumentException("Unknown namespace UUID type " + type);
      }
      if (namespace != null) {
        final byte[] bytes = namespace.getBytes(StandardCharsets.UTF_8);
        digester.update(bytes);
      }
      if (name != null) {
        digester.update(name);
      }
      final byte[] digest = digester.digest();
      return Uuid.toUuid(type, digest);
    } catch (final NoSuchAlgorithmException e) {
      return Exceptions.throwUncheckedException(e);
    }
  }

  private static UUID name(final int type, final String namespace, final Object name) {
    try {
      final MessageDigest digester;
      if (type == 3) {
        digester = MessageDigest.getInstance("MD5");
      } else if (type == 5) {
        digester = MessageDigest.getInstance("SHA-1");
      } else {
        throw new IllegalArgumentException("Unknown namespace UUID type " + type);
      }
      if (namespace != null) {
        final byte[] bytes = namespace.getBytes(StandardCharsets.UTF_8);
        digester.update(bytes);
      }
      if (name instanceof String) {
        final byte[] bytes = ((String)name).getBytes(StandardCharsets.UTF_8);
        digester.update(bytes);
      } else if (name != null) {
        final String string = DataTypes.toString(name);
        final byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        digester.update(bytes);
      }
      final byte[] digest = digester.digest();
      return Uuid.toUuid(type, digest);
    } catch (final NoSuchAlgorithmException e) {
      return Exceptions.throwUncheckedException(e);
    }
  }

  public static final UUID sha1(final String namespace, final byte[] name) {
    return name(5, namespace, name);
  }

  public static final UUID sha1(final String namespace, final Object name) {
    return name(5, namespace, name);
  }

  public static final UUID sha1(final String namespace, final String name) {
    return name(5, namespace, name);
  }

  public static UUID toUuid(final byte[] bytes) {
    final long l1 = Longs.toLong(bytes, 0);
    final long l2 = Longs.toLong(bytes, 8);
    return new UUID(l1, l2);
  }

  public static UUID toUuid(final int type, final byte[] bytes) {
    bytes[6] &= 0x0f; // clear version
    bytes[6] |= type << 4; // set to version
    bytes[8] &= 0x3f; // clear variant
    bytes[8] |= 0x80; // set to IETF variant
    return toUuid(bytes);
  }

}
