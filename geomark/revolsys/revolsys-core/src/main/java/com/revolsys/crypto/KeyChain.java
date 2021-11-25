package com.revolsys.crypto;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStoreException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.jeometry.common.logging.Logs;

public class KeyChain {
  private static final String SECRET_KEY_ALGORITHM = "JCEKS";

  private static final String KEYSTORE_TYPE = "JCEKS";

  private KeyStore store;

  private final char[] password;

  private final Path path;

  private final PasswordProtection passwordProtection;

  public KeyChain(final Path path, final String password) {
    this.path = path;
    this.password = password.toCharArray();
    this.passwordProtection = new PasswordProtection(this.password);
    try {
      this.store = KeyStore.getInstance(KEYSTORE_TYPE);
      if (Files.exists(path)) {
        final InputStream inputStream = Files.newInputStream(path);
        this.store.load(inputStream, this.password);
      } else {
        this.store.load(null, null);
      }
    } catch (final Throwable e) {
      Logs.error(this, "Cannot open keychain " + path, e);
    }
  }

  public String getPassword(final String keyName) {
    try {
      final SecretKeyEntry entry = (SecretKeyEntry)this.store.getEntry(keyName,
        this.passwordProtection);
      final SecretKey secretKey = entry.getSecretKey();
      final byte[] passwordBytes = secretKey.getEncoded();
      return new String(passwordBytes, StandardCharsets.UTF_8);
    } catch (final Throwable e) {
      Logs.error(this, "Cannot get password for " + keyName + ": " + this.path, e);
    }
    return null;
  }

  public void removePassword(final String keyName) {
    try {
      this.store.deleteEntry(keyName);
    } catch (final KeyStoreException e) {
      Logs.error(this, "Cannot remove password for " + keyName + ": " + this.path, e);
    }
  }

  private void save() {
    try {
      final OutputStream outputStream = Files.newOutputStream(this.path);
      this.store.store(outputStream, this.password);
    } catch (final Throwable e) {
      Logs.error(this, "Cannot save keychain " + this.path, e);
    }
  }

  public void setPassword(final String keyName, final String password) {
    final String pass = new String(password);
    final byte[] passwordBytes = pass.getBytes(StandardCharsets.UTF_8);
    final SecretKeySpec passwordKey = new SecretKeySpec(passwordBytes, SECRET_KEY_ALGORITHM);
    final SecretKeyEntry entry = new SecretKeyEntry(passwordKey);
    try {
      this.store.setEntry(keyName, entry, this.passwordProtection);
      save();
    } catch (final Throwable e) {
      Logs.error(this, "Cannot set password for " + keyName + ": " + this.path, e);
    }
  }
}
