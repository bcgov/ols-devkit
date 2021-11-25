package com.revolsys.io.page;

public interface PageValueManager<T> {
  PageValueManager<Byte> BYTE = new MethodPageValueManager<>("Byte");

  PageValueManager<Double> DOUBLE = new MethodPageValueManager<>("Double");

  PageValueManager<Float> FLOAT = new MethodPageValueManager<>("Float");

  PageValueManager<Integer> INT = new MethodPageValueManager<>("Int");

  PageValueManager<Long> LONG = new MethodPageValueManager<>("Long");

  PageValueManager<Short> SHORT = new MethodPageValueManager<>("Short");

  PageValueManager<String> STRING = new MethodPageValueManager<>("String");

  void disposeBytes(final byte[] bytes);

  byte[] getBytes(final Page page);

  byte[] getBytes(final T value);

  <V extends T> V getValue(final byte[] bytes);

  <V extends T> V readFromPage(final Page page);
}
