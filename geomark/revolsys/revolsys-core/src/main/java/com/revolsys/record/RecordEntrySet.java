package com.revolsys.record;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map.Entry;

public class RecordEntrySet extends AbstractSet<Entry<String, Object>> {
  private final Record record;

  public RecordEntrySet(final Record record) {
    this.record = record;
  }

  @Override
  public Iterator<Entry<String, Object>> iterator() {
    return new RecordEntrySetIterator(this.record);
  }

  @Override
  public int size() {
    return this.record.getRecordDefinition().getFieldCount();
  }
}
