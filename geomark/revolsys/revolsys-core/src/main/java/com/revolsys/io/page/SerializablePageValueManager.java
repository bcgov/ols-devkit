package com.revolsys.io.page;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jeometry.common.exception.Exceptions;

public class SerializablePageValueManager<T> implements PageValueManager<T> {
  @Override
  public void disposeBytes(final byte[] bytes) {
  }

  @Override
  public byte[] getBytes(final Page page) {
    final byte[] bytes = MethodPageValueManager.getIntBytes(page);
    final int size = MethodPageValueManager.getIntValue(bytes);
    final byte[] valueBytes = new byte[size + 4];
    System.arraycopy(bytes, 0, valueBytes, 0, 4);
    page.readBytes(valueBytes, 4, size);
    return null;
  }

  @Override
  public byte[] getBytes(final T value) {
    try {
      final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
      final ObjectOutputStream out = new ObjectOutputStream(bOut);
      out.writeObject(value);
      out.close();
      final byte[] valueBytes = bOut.toByteArray();

      final int size = valueBytes.length;
      final byte[] sizeBytes = MethodPageValueManager.getValueIntBytes(size);
      final byte[] bytes = new byte[valueBytes.length + sizeBytes.length];
      System.arraycopy(sizeBytes, 0, bytes, 0, sizeBytes.length);
      System.arraycopy(valueBytes, 0, bytes, sizeBytes.length, valueBytes.length);
      return bytes;
    } catch (final Exception e) {
      return (byte[])Exceptions.throwUncheckedException(e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V extends T> V getValue(final byte[] bytes) {
    try {
      final ByteArrayInputStream bIn = new ByteArrayInputStream(bytes, 4, bytes.length - 4);
      final ObjectInputStream in = new ObjectInputStream(bIn);
      return (V)in.readObject();
    } catch (final Exception e) {
      return (V)Exceptions.throwUncheckedException(e);
    }
  }

  @Override
  public <V extends T> V readFromPage(final Page page) {
    final byte[] bytes = getBytes(page);
    return getValue(bytes);
  }

}
