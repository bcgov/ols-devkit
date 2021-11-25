package com.revolsys.io.page;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import com.revolsys.util.JavaBeanUtil;

public class MethodPageValueManager<T> implements PageValueManager<T> {

  public static byte[] getByteBytes(final Page page) {
    return page.readBytes(1);
  }

  public static Byte getByteValue(final byte[] bytes) {
    return bytes[0];
  }

  public static byte[] getDoubleBytes(final Page page) {
    return page.readBytes(8);
  }

  public static Double getDoubleValue(final byte[] bytes) {
    final long l = getLongValue(bytes);
    return Double.longBitsToDouble(l);
  }

  public static byte[] getFloatBytes(final Page page) {
    return page.readBytes(4);
  }

  public static Float getFloatValue(final byte[] bytes) {
    final int i = getIntValue(bytes);
    return Float.intBitsToFloat(i);
  }

  public static byte[] getIntBytes(final Page page) {
    return page.readBytes(4);
  }

  public static Integer getIntValue(final byte[] bytes) {
    final int b1 = bytes[0] & 0xFF;
    final int b2 = bytes[1] & 0xFF;
    final int b3 = bytes[2] & 0xFF;
    final int b4 = bytes[3] & 0xFF;
    return (b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0);
  }

  public static byte[] getLongBytes(final Page page) {
    return page.readBytes(8);
  }

  public static Long getLongValue(final byte[] bytes) {
    final int b1 = bytes[0] & 0xFF;
    final int b2 = bytes[1] & 0xFF;
    final int b3 = bytes[2] & 0xFF;
    final int b4 = bytes[3] & 0xFF;
    final int b5 = bytes[4] & 0xFF;
    final int b6 = bytes[5] & 0xFF;
    final int b7 = bytes[6] & 0xFF;
    final int b8 = bytes[7] & 0xFF;
    return ((long)b1 << 56) + ((long)(b2 & 255) << 48) + ((long)(b3 & 255) << 40)
      + ((long)(b4 & 255) << 32) + ((long)(b5 & 255) << 24) + ((b6 & 255) << 16) + ((b7 & 255) << 8)
      + ((b8 & 255) << 0);
  }

  public static byte[] getShortBytes(final Page page) {
    return page.readBytes(2);
  }

  public static Short getShortValue(final byte[] bytes) {
    final int b1 = bytes[0] & 0xFF;
    final int b2 = bytes[1] & 0xFF;
    return (short)((b1 << 8) + (b2 << 0));
  }

  public static byte[] getStringBytes(final Page page) {
    final byte[] bytes = getIntBytes(page);
    final int size = getIntValue(bytes);
    final byte[] stringBytes = new byte[size + 4];
    System.arraycopy(bytes, 0, stringBytes, 0, 4);
    page.readBytes(stringBytes, 4, size);
    return stringBytes;
  }

  public static String getStringValue(final byte[] bytes) {
    final int size = getIntValue(bytes);
    final byte[] stringBytes = new byte[size];
    System.arraycopy(bytes, 4, stringBytes, 0, size);
    return new String(stringBytes, StandardCharsets.UTF_8);
  }

  public static byte[] getValueByteBytes(final Byte b) {
    return new byte[] {
      b
    };
  }

  public static byte[] getValueDoubleBytes(final Double d) {
    final long l = Double.doubleToLongBits(d);
    return getValueLongBytes(l);
  }

  public static byte[] getValueFloatBytes(final Float f) {
    final int i = Float.floatToIntBits(f);
    return getValueIntBytes(i);
  }

  public static byte[] getValueIntBytes(final Integer i) {
    final byte b1 = (byte)(i >>> 24 & 0xFF);
    final byte b2 = (byte)(i >>> 16 & 0xFF);
    final byte b3 = (byte)(i >>> 8 & 0xFF);
    final byte b4 = (byte)(i >>> 0 & 0xFF);
    return new byte[] {
      b1, b2, b3, b4
    };
  }

  public static byte[] getValueLongBytes(final Long l) {
    final byte b1 = (byte)(l >>> 56);
    final byte b2 = (byte)(l >>> 48);
    final byte b3 = (byte)(l >>> 40);
    final byte b4 = (byte)(l >>> 32);
    final byte b5 = (byte)(l >>> 24);
    final byte b6 = (byte)(l >>> 16);
    final byte b7 = (byte)(l >>> 8);
    final byte b8 = (byte)(l >>> 0);
    return new byte[] {
      b1, b2, b3, b4, b5, b6, b7, b8
    };
  }

  public static byte[] getValueShortBytes(final Short s) {
    final byte b1 = (byte)(s >>> 8 & 0xFF);
    final byte b2 = (byte)(s >>> 0 & 0xFF);
    return new byte[] {
      b1, b2,
    };
  }

  public static byte[] getValueStringBytes(final String s) {
    final byte[] stringBytes = s.getBytes(StandardCharsets.UTF_8);
    final int size = stringBytes.length;
    final byte[] sizeBytes = getValueIntBytes(size);
    final byte[] bytes = new byte[stringBytes.length + sizeBytes.length];
    System.arraycopy(sizeBytes, 0, bytes, 0, sizeBytes.length);
    System.arraycopy(stringBytes, 0, bytes, sizeBytes.length, stringBytes.length);
    return bytes;
  }

  private Method byteArrayWriteMethod;

  private Method getBytesMethod;

  private Method getValueMethod;

  private String typePath;

  protected MethodPageValueManager() {
  }

  protected MethodPageValueManager(final String typePath) {
    this(typePath, "getValue" + typePath + "Bytes", "get" + typePath + "Value",
      "get" + typePath + "Bytes");
  }

  protected MethodPageValueManager(final String typePath, final String byteArrayWriteMethodName,
    final String byteArrayReadMethodName, final String pageReadMethodName) {
    for (final Method method : getClass().getMethods()) {
      if (method.getName().equals(byteArrayWriteMethodName)) {
        this.byteArrayWriteMethod = method;
      }
      if (method.getName().equals(byteArrayReadMethodName)) {
        this.getValueMethod = method;
      }
      if (method.getName().equals(pageReadMethodName)) {
        this.getBytesMethod = method;
      }
    }
    this.typePath = typePath;

  }

  @Override
  public void disposeBytes(final byte[] bytes) {
  }

  @Override
  public byte[] getBytes(final Page page) {
    return (byte[])JavaBeanUtil.method(this.getBytesMethod, getClass(), page);
  }

  @Override
  public byte[] getBytes(final T value) {
    return JavaBeanUtil.method(this.byteArrayWriteMethod, getClass(), value);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V extends T> V getValue(final byte[] bytes) {
    return (V)JavaBeanUtil.method(this.getValueMethod, getClass(), bytes);
  }

  @Override
  public <V extends T> V readFromPage(final Page page) {
    final byte[] bytes = getBytes(page);
    return getValue(bytes);
  }

  @Override
  public String toString() {
    return this.typePath;
  }
}
