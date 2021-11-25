package com.revolsys.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;

public class UuidBuilder {
  private MessageDigest digester;

  public UuidBuilder() {
    try {
      this.digester = MessageDigest.getInstance("SHA-1");
    } catch (final NoSuchAlgorithmException e) {
      Exceptions.throwUncheckedException(e);
    }
  }

  public UuidBuilder(final String namespace) {
    this();
    append(namespace);
  }

  public UuidBuilder append(final byte[] bytes) {
    if (bytes != null) {
      this.digester.update(bytes);
    }
    return this;
  }

  public UuidBuilder append(final Object value) {
    if (value instanceof String) {
      append((String)value);
    } else if (value != null) {
      final String string = DataTypes.toString(value);
      append(string);
    }
    return this;
  }

  public UuidBuilder append(final String string) {
    final byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
    return append(bytes);
  }

  public Identifier newStringIdentifier() {
    final String string = toString();
    return Identifier.newIdentifier(string);
  }

  public UUID newUuid() {
    final byte[] digest = this.digester.digest();
    return Uuid.toUuid(5, digest);
  }

  @Override
  public String toString() {
    final UUID uuid = newUuid();
    return uuid.toString();
  }
}
