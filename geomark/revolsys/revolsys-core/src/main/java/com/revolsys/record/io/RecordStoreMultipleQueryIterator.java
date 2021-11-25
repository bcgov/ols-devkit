package com.revolsys.record.io;

import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.iterator.AbstractMultipleIterator;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;

public class RecordStoreMultipleQueryIterator extends AbstractMultipleIterator<Record>
  implements RecordIterator {

  private int queryIndex = 0;

  private RecordStoreQueryReader reader;

  public RecordStoreMultipleQueryIterator(final RecordStoreQueryReader reader) {
    this.reader = reader;
  }

  @Override
  public void closeDo() {
    super.closeDo();
    this.reader = null;
  }

  @Override
  public AbstractIterator<Record> getNextIterator() throws NoSuchElementException {
    if (this.reader == null) {
      throw new NoSuchElementException();
    } else {
      final AbstractIterator<Record> iterator = (AbstractIterator<Record>)this.reader
        .newQueryIterator(this.queryIndex);
      this.queryIndex++;
      return iterator;
    }
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    try {
      final AbstractIterator<Record> iterator = getIterator();
      if (iterator instanceof RecordReader) {
        final RecordReader reader = (RecordReader)iterator;
        return reader.getRecordDefinition();
      }
    } catch (final NoSuchElementException e) {
    }
    return null;
  }

}
