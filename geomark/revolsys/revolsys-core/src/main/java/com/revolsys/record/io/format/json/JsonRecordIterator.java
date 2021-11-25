package com.revolsys.record.io.format.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.io.FileUtil;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.schema.RecordDefinition;

public class JsonRecordIterator extends AbstractIterator<Record> implements RecordReader {

  private JsonMapIterator iterator;

  private RecordDefinition recordDefinition;

  public JsonRecordIterator(final RecordDefinition recordDefinition, final InputStream in) {
    this(recordDefinition, FileUtil.newUtf8Reader(in));
  }

  public JsonRecordIterator(final RecordDefinition recordDefinition, final Reader in) {
    this(recordDefinition, in, false);
  }

  public JsonRecordIterator(final RecordDefinition recordDefinition, final Reader in,
    final boolean single) {
    this.recordDefinition = recordDefinition;
    try {
      this.iterator = new JsonMapIterator(in, single);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Cannot open " + in, e);
    }
  }

  @Override
  protected void closeDo() {
    FileUtil.closeSilent(this.iterator);
    this.iterator = null;
    this.recordDefinition = null;
  }

  @Override
  protected Record getNext() throws NoSuchElementException {
    if (this.iterator.hasNext()) {
      final Map<String, Object> map = this.iterator.next();
      return new ArrayRecord(this.recordDefinition, map);
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }
}
