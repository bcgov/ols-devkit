package com.revolsys.record.query;

public class ColumnIndexes {

  public int columnIndex = 0;

  public int incrementAndGet() {
    return ++this.columnIndex;
  }

  @Override
  public String toString() {
    return Integer.toString(this.columnIndex);
  }
}
