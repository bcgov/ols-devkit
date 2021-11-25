package com.revolsys.record.io.format.tcx;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

import javax.xml.stream.XMLStreamException;

import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.format.gpx.GpxIterator;
import com.revolsys.record.io.format.xml.StaxReader;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;

public class TcxIterator extends BaseObjectWithProperties
  implements Iterator<Record>, RecordReader {
  private String baseName;

  private Record currentRecord;

  private File file;

  private boolean hasNext = true;

  private final StaxReader in;

  private boolean loadNextObject = true;

  private final Queue<Record> records = new LinkedList<>();

  private final String schemaName = TcxConstants._NS_URI;

  private String typePath;

  public TcxIterator(final File file) throws IOException, XMLStreamException {
    this(new FileReader(file));
  }

  public TcxIterator(final Reader in) throws IOException, XMLStreamException {
    this(StaxReader.newXmlReader(in));
  }

  public TcxIterator(final Reader in, final RecordFactory recordFactory, final String path) {
    this(StaxReader.newXmlReader(in));
    this.typePath = path;
  }

  public TcxIterator(final Resource resource, final RecordFactory recordFactory, final String path)
    throws IOException {
    this(StaxReader.newXmlReader(resource));
    this.typePath = path;
    this.baseName = resource.getBaseName();
  }

  public TcxIterator(final StaxReader in) {
    this.in = in;
    // try {
    // in.skipToStartElement(in);
    // // skipMetaData();
    // } catch (final XMLStreamException e) {
    // throw new RuntimeException(e.getMessage(), e);
    // }
  }

  @Override
  public void close() {
    if (this.in != null) {
      this.in.close();
    }
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return GpxIterator.GPX_TYPE;
  }

  public String getSchemaName() {
    return this.schemaName;
  }

  @Override
  public boolean hasNext() {
    if (!this.hasNext) {
      return false;
    } else if (this.loadNextObject) {
      return loadNextRecord();
    } else {
      return true;
    }
  }

  @Override
  public Iterator<Record> iterator() {
    return this;
  }

  protected boolean loadNextRecord() {
    // try {
    do {
      // this.currentRecord = parseRecord();
    } while (this.currentRecord != null && this.typePath != null
      && !this.currentRecord.getRecordDefinition().getPath().equals(this.typePath));
    this.loadNextObject = false;
    if (this.currentRecord == null) {
      close();
      this.hasNext = false;
    }
    return this.hasNext;
    // } catch (final XMLStreamException e) {
    // throw new RuntimeException(e.getMessage(), e);
    // }
  }

  @Override
  public Record next() {
    if (hasNext()) {
      this.loadNextObject = true;
      return this.currentRecord;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return this.file.getAbsolutePath();
  }

}
