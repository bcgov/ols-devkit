package com.revolsys.record.io;

import java.util.Iterator;

import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;

public interface RecordIterator extends Iterator<Record>, RecordReader {
  @Override
  RecordDefinition getRecordDefinition();
}
