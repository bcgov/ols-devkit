package com.revolsys.record.comparator;

import java.util.Comparator;

import com.revolsys.record.Record;

public class RecordComparators {
  public static <R extends Record> Comparator<R> fieldName(final String fieldName) {
    return (record1, record2) -> {
      return record1.compareValue(record2, fieldName);
    };
  }
}
