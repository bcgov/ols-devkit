package com.revolsys.record;

public enum RecordState {
  INITIALIZING, //
  NEW, //
  PERSISTED, //
  MODIFIED, //
  DELETED;

  public boolean isDeleted() {
    return this == DELETED;
  }

  public boolean isInitializing() {
    return this == INITIALIZING;
  }

  public boolean isModified() {
    return this == MODIFIED;
  }

  public boolean isNew() {
    return this == NEW;
  }

  public boolean isPersisted() {
    return this == PERSISTED;
  }
}
