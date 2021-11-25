package com.revolsys.io;

import java.io.IOException;

public interface RandomAccess {
  long length() throws IOException;

  void seek(long index) throws IOException;

  void setLength(long length) throws IOException;
}
