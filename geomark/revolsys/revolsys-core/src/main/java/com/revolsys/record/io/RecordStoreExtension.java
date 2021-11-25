package com.revolsys.record.io;

import java.util.Map;

import com.revolsys.record.schema.RecordStore;
import com.revolsys.record.schema.RecordStoreSchema;

public interface RecordStoreExtension {

  public abstract void initialize(RecordStore recordStore,
    Map<String, Object> connectionProperties);

  boolean isEnabled(RecordStore recordStore);

  public abstract void postProcess(RecordStoreSchema schema);

  public abstract void preProcess(RecordStoreSchema schema);
}
