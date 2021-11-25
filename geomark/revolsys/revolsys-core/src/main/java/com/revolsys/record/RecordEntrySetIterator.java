package com.revolsys.record;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import com.revolsys.record.schema.RecordDefinition;

public class RecordEntrySetIterator
  implements Iterator<Entry<String, Object>>, Entry<String, Object> {
  private int index = -1;

  private final Record record;

  private final int size;

  private final RecordDefinition recordDefinition;

  public RecordEntrySetIterator(final Record record) {
    this.record = record;
    this.recordDefinition = record.getRecordDefinition();
    this.size = this.recordDefinition.getFieldCount();
  }

  @Override
  public String getKey() {
    return this.recordDefinition.getFieldName(this.index);
  }

  @Override
  public Object getValue() {
    return this.record.getValue(this.index);
  }

  @Override
  public boolean hasNext() {
    if (this.index + 1 < this.size) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public Entry<String, Object> next() {
    this.index++;
    if (this.index < this.size) {
      return this;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public Object setValue(final Object value) {
    return this.record.setValue(this.index, value);
  }

  @Override
  public String toString() {
    if (this.index < 0 || this.index >= this.size) {
      return "Invalid";
    } else {
      return getKey() + "=" + getValue();
    }
  }
}
