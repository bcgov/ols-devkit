package com.revolsys.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.revolsys.properties.BaseObjectWithProperties;

public abstract class AbstractWriter<T> extends BaseObjectWithProperties implements Writer<T> {

  public static void close(final Collection<? extends Writer<?>> writers) {
    final List<RuntimeException> exceptions = new ArrayList<>();
    for (final Writer<?> writer : writers) {
      if (writer != null) {
        try {
          writer.close();
        } catch (final RuntimeException e) {
          exceptions.add(e);
        }
      }
    }
    if (!exceptions.isEmpty()) {
      throw exceptions.get(0);
    }
  }

  public static void close(final Writer<?>... writers) {
    close(Arrays.asList(writers));
  }

  public static <V> Writer<V> close(final Writer<V> writer) {
    if (writer != null) {
      writer.close();
    }
    return null;
  }

  @Override
  public void close() {
  }

  @Override
  public void flush() {
  }

  @Override
  public void open() {
  }
}
