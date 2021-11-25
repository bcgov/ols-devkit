package com.revolsys.io;

public class CloseableWrapper implements BaseCloseable {

  private BaseCloseable closeable;

  public CloseableWrapper(final BaseCloseable closeable) {
    this.closeable = closeable;
  }

  @Override
  public synchronized void close() {
    if (this.closeable != null) {
      this.closeable.close();
      this.closeable = null;
    }
  }

  @Override
  public String toString() {
    final BaseCloseable closeable = this.closeable;
    if (closeable == null) {
      return super.toString();
    } else {
      return closeable.toString();
    }
  }
}
