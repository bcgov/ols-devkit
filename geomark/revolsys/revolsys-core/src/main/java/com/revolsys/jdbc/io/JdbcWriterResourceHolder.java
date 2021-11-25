package com.revolsys.jdbc.io;

import org.springframework.transaction.support.ResourceHolderSupport;

public class JdbcWriterResourceHolder extends ResourceHolderSupport {
  private JdbcRecordWriter writer;

  public JdbcWriterResourceHolder() {
  }

  protected void close() {
    if (this.writer != null) {
      this.writer.close();
      this.writer = null;
    }
  }

  public JdbcRecordWriter getWriter() {
    return this.writer;
  }

  public JdbcWriterWrapper getWriterWrapper(final AbstractJdbcRecordStore recordStore,
    final boolean throwExceptions, final int batchSize) {
    requested();
    if (this.writer == null) {
      this.writer = recordStore.newRecordWriter(batchSize);
      this.writer.setThrowExceptions(throwExceptions);
    }
    return new JdbcWriterWrapper(this.writer);
  }

  public boolean hasWriter() {
    return this.writer != null;
  }

  @Override
  public void released() {
    super.released();
    if (!isOpen()) {
      close();
    }
  }

  public void setWriter(final JdbcRecordWriter writer) {
    this.writer = writer;
  }

  public boolean writerEquals(final JdbcRecordWriter writer) {
    return this.writer == writer;
  }
}
