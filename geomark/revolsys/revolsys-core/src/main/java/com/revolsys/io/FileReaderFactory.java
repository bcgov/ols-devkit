package com.revolsys.io;

import java.io.File;

public interface FileReaderFactory<T> {
  Reader<T> newReader(File file);
}
