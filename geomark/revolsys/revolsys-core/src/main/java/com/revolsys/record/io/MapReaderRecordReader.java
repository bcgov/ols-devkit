package com.revolsys.record.io;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jeometry.common.data.type.DataType;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.io.AbstractReader;
import com.revolsys.io.map.MapReader;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.FieldValueInvalidException;
import com.revolsys.record.Record;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;

public class MapReaderRecordReader extends AbstractReader<Record>
  implements RecordReader, Iterator<Record> {

  private Iterator<MapEx> mapIterator;

  private final MapReader mapReader;

  private boolean open;

  private final RecordDefinition recordDefinition;

  public MapReaderRecordReader(final RecordDefinition recordDefinition, final MapReader mapReader) {
    this.recordDefinition = recordDefinition;
    this.mapReader = mapReader;
  }

  @Override
  public void close() {
    this.mapReader.close();
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
    return this.mapIterator.hasNext();
  }

  @Override
  public Iterator<Record> iterator() {
    return this;
  }

  @Override
  public Record next() {
    if (hasNext()) {
      final MapEx source = this.mapIterator.next();
      final RecordDefinition recordDefinition = this.recordDefinition;
      final Record target = new ArrayRecord(recordDefinition);
      for (final String fieldName : source.keySet()) {
        final FieldDefinition field = recordDefinition.getField(fieldName);
        if (field != null) {
          final String name = field.getName();
          final Object value = source.get(fieldName);
          if (value != null) {
            final DataType dataType = recordDefinition.getFieldType(name);
            final Object convertedValue;
            try {
              convertedValue = dataType.toObject(value);
            } catch (final Throwable e) {
              throw new FieldValueInvalidException(name, value, e);
            }
            target.setValue(name, convertedValue);
          }
        }
      }
      if (recordDefinition.hasGeometryField()) {
        if (target.getGeometry() == null) {
          if (source instanceof Record) {
            final Geometry geometry = ((Record)source).getGeometry();
            target.setGeometryValue(geometry);
          }
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
    this.mapIterator = this.mapReader.iterator();
  }

  @Override
  public void remove() {
    this.mapIterator.remove();
  }
}
