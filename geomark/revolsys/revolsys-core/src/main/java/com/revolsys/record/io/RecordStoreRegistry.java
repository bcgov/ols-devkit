package com.revolsys.record.io;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.record.schema.RecordStore;

public class RecordStoreRegistry {
  private Map<String, RecordStore> recordStores = new HashMap<>();

  public void addRecordStore(final String name, final RecordStore recordStore) {
    this.recordStores.put(name, recordStore);
  }

  public RecordStore getRecordStore(final String name) {
    return this.recordStores.get(name);
  }

  public Map<String, RecordStore> getRecordStores() {
    return Collections.unmodifiableMap(this.recordStores);
  }

  public void setRecordStores(final Map<String, RecordStore> recordStores) {
    this.recordStores = recordStores;
  }

}
