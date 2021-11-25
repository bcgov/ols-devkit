package com.revolsys.record.io.format.saif;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.ZipFile;

import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.format.saif.util.OsnConverter;
import com.revolsys.record.io.format.saif.util.OsnConverterRegistry;
import com.revolsys.record.io.format.saif.util.OsnIterator;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionFactory;
import com.revolsys.record.schema.RecordDefinitionImpl;

public class OsnReader implements Iterator<Record>, Closeable {
  private final OsnConverterRegistry converters;

  private File directory;

  private boolean endOfFile = false;

  private RecordFactory factory;

  private final String fileName;

  private boolean nextChecked = false;

  private OsnIterator osnIterator;

  private final RecordDefinitionFactory recordDefinitionFactory;

  private ZipFile zipFile;

  public OsnReader(final RecordDefinitionFactory recordDefinitionFactory, final File directory,
    final String fileName, final int srid) throws IOException {
    this.recordDefinitionFactory = recordDefinitionFactory;
    this.directory = directory;
    this.fileName = fileName;
    this.converters = new OsnConverterRegistry(srid);
    open();
  }

  public OsnReader(final RecordDefinitionFactory recordDefinitionFactory, final ZipFile zipFile,
    final String fileName, final int srid) throws IOException {
    this.recordDefinitionFactory = recordDefinitionFactory;
    this.fileName = fileName;
    this.zipFile = zipFile;
    this.converters = new OsnConverterRegistry(srid);
    open();
  }

  /**
   * Get an attribute definition from the iterator.
   *
   * @param record
   * @param typePath TODO
   * @param iterator The OsnIterator.
   * @return The attribute definition.
   * @throws IOException If an I/O error occurs.
   */
  private void addField(final Record record) {
    if (this.osnIterator.getEventType() != OsnIterator.START_ATTRIBUTE) {
      if (this.osnIterator.next() != OsnIterator.START_ATTRIBUTE) {
        this.osnIterator.throwParseError("Excepecting an attribute name");
      }
    }
    final String name = this.osnIterator.getStringValue();
    final Object value = getExpression();
    record.setValue(name, value);
  }

  @Override
  public void close() {
    try {
      this.osnIterator.close();
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Get the value of an expression from the iterator.
   *
   * @return The value of the expression.
   * @throws IOException If an I/O error occurs.
   */
  private Object getExpression() {
    final Object eventType = this.osnIterator.next();
    if (eventType == OsnIterator.START_DEFINITION) {
      return getRecord();
    } else if (eventType == OsnIterator.START_SET) {
      final Set<Object> set = new LinkedHashSet<>();
      processCollection(set, OsnIterator.END_SET);
      return set;
    } else if (eventType == OsnIterator.START_LIST) {
      final List<Object> list = new ArrayList<>();
      processCollection(list, OsnIterator.END_LIST);
      return list;
    } else if (eventType == OsnIterator.TEXT_VALUE) {
      return this.osnIterator.getValue();
    } else if (eventType == OsnIterator.NUMERIC_VALUE) {
      return this.osnIterator.getValue();
    } else if (eventType == OsnIterator.BOOLEAN_VALUE) {
      return this.osnIterator.getValue();
    } else if (eventType == OsnIterator.ENUM_TAG) {
      return this.osnIterator.getValue();
    } else if (eventType == OsnIterator.UNKNOWN) {
      this.osnIterator.throwParseError("Expected an expression");
    }
    return null;
  }

  /**
   * @return the factory
   */
  public RecordFactory getFactory() {
    return this.factory;
  }

  /**
   * @return the fileName
   */
  public String getFileName() {
    return this.fileName;
  }

  private Object getRecord() {
    final String typePath = this.osnIterator.getPathValue();
    final OsnConverter converter = this.converters.getConverter(typePath);
    if (converter != null) {
      return converter.read(this.osnIterator);
    } else {
      final RecordDefinition type = this.recordDefinitionFactory.getRecordDefinition(typePath);
      final Record record = this.factory.newRecord(type);
      while (this.osnIterator.next() != OsnIterator.END_OBJECT) {
        addField(record);
      }
      return record;
    }
  }

  public RecordDefinitionFactory getRecordDefinitionFactory() {
    return this.recordDefinitionFactory;
  }

  @Override
  public boolean hasNext() {
    if (this.nextChecked) {
      return true;
    } else if (this.endOfFile) {
      return false;
    } else {
      if (this.osnIterator.hasNext()) {
        Object eventType = this.osnIterator.getEventType();
        if (eventType != OsnIterator.START_DEFINITION) {
          eventType = this.osnIterator.next();
        }
        if (eventType == OsnIterator.START_DEFINITION) {
          this.nextChecked = true;
          return true;
        } else if (eventType != OsnIterator.END_DOCUMENT && eventType != OsnIterator.END_SET) {
          this.osnIterator.throwParseError("Excepecting start of an object");
        }
      }
    }
    this.endOfFile = true;
    return false;
  }

  @Override
  public Record next() {
    if (hasNext()) {
      this.nextChecked = false;
      return (Record)getRecord();
    } else {
      throw new NoSuchElementException();
    }
  }

  public void open() {
    try {
      if (this.directory != null) {
        this.osnIterator = new OsnIterator(this.directory, this.fileName);
      } else {
        this.osnIterator = new OsnIterator(this.zipFile, this.fileName);
      }
      skipToFirstRecord();
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Process a collection, loading all the values into the collection.
   *
   * @param collection The collection to save the objects to.
   * @param endEventType The event type indicating the end of a collection.
   * @throws IOException If an I/O error occurs.
   */
  private void processCollection(final Collection<Object> collection, final Object endEventType) {
    while (this.osnIterator.getEventType() != endEventType) {
      final Object value = getExpression();
      if (value != null || this.osnIterator.getEventType() == OsnIterator.NULL_VALUE) {
        collection.add(value);
      }
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Removing SAIF objects is not supported");
  }

  /**
   * @param factory the factory to set
   */
  public void setFactory(final RecordFactory factory) {
    this.factory = factory;
  }

  /**
   * Skip all objects and attributes until the first object in the collection.
   *
   * @return True if an object was found.
   * @throws IOException If an I/O error occurs.
   */
  private boolean skipToFirstRecord() throws IOException {
    if (this.osnIterator.next() == OsnIterator.START_DEFINITION) {
      final String typePath = this.osnIterator.getPathValue();
      final RecordDefinitionImpl type = (RecordDefinitionImpl)this.recordDefinitionFactory
        .getRecordDefinition(typePath);
      final RecordDefinition spatialDataSetType = this.recordDefinitionFactory
        .getRecordDefinition("/SpatialDataSet");
      if (type != null && type.isInstanceOf(spatialDataSetType)) {
        final String oiName = this.osnIterator.nextFieldName();

        if (oiName != null && oiName.equals("objectIdentifier")) {
          this.osnIterator.nextStringValue();
          final String fieldName = this.osnIterator.nextFieldName();
          if (fieldName != null
            && (fieldName.equals("geoComponents") || fieldName.equals("annotationComponents"))) {
            if (this.osnIterator.next() == OsnIterator.START_SET) {
              return true;
            } else {
              this.osnIterator.throwParseError("Expecting a set of objects");
            }
          } else {
            this.osnIterator.throwParseError("Excepecting the 'geoComponents' attribute");
          }

        } else {
          this.osnIterator.throwParseError("Expecting the 'objectIdentifier' attribute");
        }
      } else {
        return true;
      }
    } else {
      this.osnIterator.throwParseError("Expecting a start of an object definition");
    }
    return false;
  }

  @Override
  public String toString() {
    return this.fileName;
  }
}
