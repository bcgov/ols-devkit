package com.revolsys.record.io;

import java.util.Map;

import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.record.schema.RecordStore;

public class RecordStoreRecordWriter extends AbstractRecordWriter {
  private final RecordStore recordStore;

  private final RecordWriter writer;

  public RecordStoreRecordWriter(final RecordStore recordStore,
    final RecordDefinitionProxy recordDefinition) {
    super(recordStore.getRecordDefinition(recordDefinition));
    this.recordStore = recordStore;
    if (getRecordDefinition() == null) {
      throw new IllegalArgumentException(
        "Cannot find recordDefinition=" + recordDefinition.getPathName() + " for " + recordStore);
    }
    this.writer = recordStore.newRecordWriter(recordDefinition);
  }

  @Override
  public void close() {
    try {
      this.writer.close();
    } finally {
      this.recordStore.close();
    }
  }

  @Override
  public void flush() {
    this.writer.flush();
  }

  protected RecordWriter getWriter() {
    return this.writer;
  }

  @Override
  public Record newRecord() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return this.recordStore.newRecord(recordDefinition);
  }

  @Override
  public Record newRecord(final Map<String, ? extends Object> values) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return this.recordStore.newRecord(recordDefinition, values);
  }

  @Override
  public void write(final Record record) {
    if (this.writer != null) {
      this.writer.write(record);
    }
  }
}
