package com.revolsys.jdbc.io;

import com.revolsys.record.io.RecordWriter;

public interface JdbcWriter extends RecordWriter {

  @Override
  public void flush();
}
