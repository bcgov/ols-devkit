package com.revolsys.record.io.format.directory;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordIterator;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;

public class RecordReaderQueryIterator extends AbstractIterator<Record> implements RecordIterator {

  private Iterator<Record> iterator;

  private final RecordReader reader;

  private final Condition whereCondition;

  public RecordReaderQueryIterator(final RecordReader reader, final Query query) {
    this.reader = reader;
    this.whereCondition = query.getWhereCondition();
  }

  @Override
  protected void closeDo() {
    this.reader.close();
  }

  @Override
  protected Record getNext() throws NoSuchElementException {
    while (true) {
      final Record record = this.iterator.next();
      if (this.whereCondition.test(record)) {
        return record;
      }
    }
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.reader.getRecordDefinition();
  }

  @Override
  public synchronized void init() {
    this.reader.open();
    this.iterator = this.reader.iterator();
  }
}
