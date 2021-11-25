package com.revolsys.util;

import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.jeometry.common.exception.Exceptions;

public class PasswordUtil {
  private static final Pattern PATTERN = Pattern.compile("\\{(\\w+)\\}(.+)");

  public static String decrypt(final String encryptedString) {
    if (encryptedString == null) {
      return null;
    } else {
      final Matcher matcher = PATTERN.matcher(encryptedString);
      if (matcher.matches()) {
        final String algorithm = matcher.group(1);
        final String encryptedPassword = matcher.group(2);
        if (algorithm.equals("BASE64")) {
          return Base64Util.decodeToString(encryptedPassword);
        }
      }
      return encryptedString;
    }
  }

  public static byte[] decryptSqlDeveloper(final byte[] result) {
    final byte constant = result[0];
    if (constant != (byte)5) {
      throw new IllegalArgumentException();
    }

    final byte[] secretKey = new byte[8];
    System.arraycopy(result, 1, secretKey, 0, 8);

    final byte[] encryptedPassword = new byte[result.length - 9];
    System.arraycopy(result, 9, encryptedPassword, 0, encryptedPassword.length);

    final byte[] iv = new byte[8];
    Arrays.fill(iv, (byte)0);

    try {
      final Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
      final SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "DES");
      cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
      return cipher.doFinal(encryptedPassword);
    } catch (final Throwable e) {
      Exceptions.throwUncheckedException(e);
      return null;
    }
  }

  public static String decryptSqlDeveloper(final String encryptedPassword) {
    if (encryptedPassword.length() % 2 != 0) {
      throw new IllegalArgumentException(
        "Password must consist of hex pairs.  Length is odd (not even).");
    } else {

      final byte[] secret = new byte[encryptedPassword.length() / 2];
      for (int i = 0; i < encryptedPassword.length(); i += 2) {
        final String pair = encryptedPassword.substring(i, i + 2);
        secret[i / 2] = (byte)Integer.parseInt(pair, 16);
      }
      return new String(decryptSqlDeveloper(secret));
    }
  }

  public static byte[] encrypt(final byte[] data, final char[] password, final byte[] salt,
    final int noIterations) {
    try {
      final String method = "PBEWithMD5AndTripleDES";
      final SecretKeyFactory kf = SecretKeyFactory.getInstance(method);
      final PBEKeySpec keySpec = new PBEKeySpec(password);
      final SecretKey key = kf.generateSecret(keySpec);
      final Cipher ciph = Cipher.getInstance(method);
      final PBEParameterSpec params = new PBEParameterSpec(salt, noIterations);
      return ciph.doFinal(data);
    } catch (final Exception e) {
      throw new RuntimeException("Spurious encryption error");
    }
  }

  public static String encrypt(final String password) {
    if (Property.isEmpty(password)) {
      return null;
    } else {
      return "{BASE64}" + Base64.getEncoder().encodeToString(password.getBytes());
    }
  }
}
