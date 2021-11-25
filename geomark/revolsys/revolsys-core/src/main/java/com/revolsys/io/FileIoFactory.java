package com.revolsys.io;

public interface FileIoFactory extends IoFactory {
  default boolean isBinary() {
    return false;
  }

  default boolean isCustomFieldsSupported() {
    return true;
  }

  default boolean isGeometrySupported() {
    return true;
  }

  default boolean isSingleFile() {
    return true;
  }
}
