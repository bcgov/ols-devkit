package com.revolsys.transaction;

import java.util.Iterator;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.schema.RecordDefinition;

public class TransactionRecordReader implements RecordReader {

  private final RecordReader reader;

  private final Transaction transaction;

  public TransactionRecordReader(final RecordReader reader, final Transaction transaction) {
    this.reader = reader;
    this.transaction = transaction;
  }

  @Override
  public void close() {
    try {
      RecordReader.super.close();
    } finally {
      this.transaction.close();
    }
  }

  @Override
  public MapEx getProperties() {
    return this.reader.getProperties();
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.reader.getRecordDefinition();
  }

  @Override
  public boolean isCancelled() {
    return this.reader.isCancelled();
  }

  @Override
  public Iterator<Record> iterator() {
    return this.reader.iterator();
  }

  @Override
  public String toString() {
    return this.reader.toString();
  }
}
