package com.revolsys.record.io;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jeometry.common.data.type.DataType;

import com.revolsys.io.AbstractReader;
import com.revolsys.io.Reader;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;

public class RecordDefinitionConvertRecordReader extends AbstractReader<Record>
  implements RecordReader, Iterator<Record> {

  private Iterator<Record> iterator;

  private boolean open;

  private final Reader<Record> reader;

  private final RecordDefinition recordDefinition;

  public RecordDefinitionConvertRecordReader(final RecordDefinition recordDefinition,
    final Reader<Record> reader) {
    this.recordDefinition = recordDefinition;
    this.reader = reader;
  }

  @Override
  public void close() {
    this.reader.close();
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  @Override
  public boolean hasNext() {
    if (!this.open) {
      open();
    }
    return this.iterator.hasNext();
  }

  @Override
  public Iterator<Record> iterator() {
    return this;
  }

  @Override
  public Record next() {
    if (hasNext()) {
      final Record source = this.iterator.next();
      final Record target = new ArrayRecord(this.recordDefinition);
      for (final FieldDefinition attribute : this.recordDefinition.getFields()) {
        final String name = attribute.getName();
        final Object value = source.getValue(name);
        if (value != null) {
          final DataType dataType = this.recordDefinition.getFieldType(name);
          final Object convertedValue = dataType.toObject(value);
          target.setValue(name, convertedValue);
        }
      }
      return target;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void open() {
    this.open = true;
    this.iterator = this.reader.iterator();
  }

  @Override
  public void remove() {
    this.iterator.remove();
  }
}
