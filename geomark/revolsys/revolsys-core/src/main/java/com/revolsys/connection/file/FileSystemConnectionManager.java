package com.revolsys.connection.file;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class FileSystemConnectionManager {
  private static FileSystemConnectionManager INSTANCE = new FileSystemConnectionManager();

  public static FileSystemConnectionManager get() {
    return INSTANCE;
  }

  public List<File> getFileSystems() {
    return Arrays.asList(File.listRoots());
  }

  @Override
  public String toString() {
    return "File Systems";
  }
}
