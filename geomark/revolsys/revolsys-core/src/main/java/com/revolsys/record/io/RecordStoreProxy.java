package com.revolsys.record.io;

import com.revolsys.record.schema.RecordStore;

public interface RecordStoreProxy {
  <RS extends RecordStore> RS getRecordStore();
}
