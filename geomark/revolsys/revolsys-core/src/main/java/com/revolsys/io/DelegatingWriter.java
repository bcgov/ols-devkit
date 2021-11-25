package com.revolsys.io;

import com.revolsys.collection.map.MapEx;

public class DelegatingWriter<T> extends AbstractWriter<T> {
  private Writer<T> writer;

  public DelegatingWriter() {
  }

  public DelegatingWriter(final Writer<T> writer) {
    this.writer = writer;
  }

  @Override
  public void close() {
    this.writer.close();
  }

  @Override
  public void flush() {
    this.writer.flush();
  }

  @Override
  public MapEx getProperties() {
    return this.writer.getProperties();
  }

  @Override
  public <C> C getProperty(final String name) {
    return (C)this.writer.getProperty(name);
  }

  public Writer<T> getWriter() {
    return this.writer;
  }

  @Override
  public void setProperty(final String name, final Object value) {
    this.writer.setProperty(name, value);
  }

  public void setWriter(final Writer<T> writer) {
    this.writer = writer;
  }

  @Override
  public void write(final T object) {
    this.writer.write(object);
  }
}
