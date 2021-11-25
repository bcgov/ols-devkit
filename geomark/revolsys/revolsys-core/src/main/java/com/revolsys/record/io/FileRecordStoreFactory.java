package com.revolsys.record.io;

public interface FileRecordStoreFactory extends RecordStoreFactory {
  default boolean isDirectory() {
    return true;
  }
}
