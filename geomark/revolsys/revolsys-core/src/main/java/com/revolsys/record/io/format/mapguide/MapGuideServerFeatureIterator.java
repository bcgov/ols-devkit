package com.revolsys.record.io.format.mapguide;

import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import org.jeometry.common.logging.Logs;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.RecordState;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.format.xml.StaxReader;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;

public class MapGuideServerFeatureIterator extends AbstractIterator<Record>
  implements RecordReader {
  private static final String FEATURE = "Feature";

  private static final String PROPERTY = "Property";

  private static final QName NAME = new QName("Name");

  private static final QName VALUE = new QName("Value");

  private StaxReader reader;

  private boolean closed;

  private RecordDefinition recordDefinition;

  private RecordFactory<?> recordFacory;

  private GeometryFactory geometryFactory;

  private int totalRecordCount = 0;

  private int currentRecordCount = 0;

  private Map<String, Object> queryParameters;

  private final int queryOffset;

  private final int queryLimit;

  private int serverLimit;

  private final boolean supportsPaging;

  private final MapGuideFeatureLayer layer;

  private final String geometryFieldName;

  public MapGuideServerFeatureIterator(final MapGuideFeatureLayer layer,
    final Map<String, Object> queryParameters, final int offset, final int limit,
    final RecordFactory<?> recordFactory) {
    this.layer = layer;
    this.queryParameters = queryParameters;
    this.queryOffset = offset;
    this.queryLimit = limit;
    this.serverLimit = layer.getMaxRecordCount();
    if (this.queryLimit < this.serverLimit) {
      this.serverLimit = this.queryLimit;
    }
    this.recordDefinition = layer.getRecordDefinition();
    if (recordFactory == null) {
      this.recordFacory = ArrayRecord.FACTORY;
    } else {
      this.recordFacory = recordFactory;
    }
    this.geometryFactory = this.recordDefinition.getGeometryFactory();
    this.geometryFieldName = this.recordDefinition.getGeometryFieldName();
    this.supportsPaging = false;
  }

  @Override
  protected void closeDo() {
    FileUtil.closeSilent(this.reader);
    this.reader = null;
    this.geometryFactory = null;
    this.queryParameters = null;
    this.recordDefinition = null;
    this.recordFacory = null;
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  @Override
  protected Record getNext() throws NoSuchElementException {
    if (this.closed) {
      throw new NoSuchElementException();
    } else {
      if (this.totalRecordCount < this.queryLimit) {
        StaxReader parser = this.reader;
        if (parser == null) {
          parser = newParser();
        }
        if (!parser.skipToStartElement(FEATURE)) {
          if (this.supportsPaging && this.currentRecordCount == this.serverLimit) {
            parser = newParser();
          }
          if (!parser.skipToStartElement(FEATURE)) {
            throw new NoSuchElementException();
          }
        }
      } else {
        throw new NoSuchElementException();
      }
      if (!this.closed) {
        try {
          final Record record = this.recordFacory.newRecord(this.recordDefinition);
          record.setState(RecordState.INITIALIZING);

          final int featureDepth = this.reader.getDepth();
          while (this.reader.skipToStartElement(featureDepth, PROPERTY)) {
            final int propertyDepth = this.reader.getDepth();
            String name = null;
            Object value = null;
            while (this.reader.skipToStartElements(propertyDepth, NAME, VALUE)) {
              if (this.reader.isStartElementLocalName(NAME)) {
                name = this.reader.getElementText();
              } else if (this.reader.isStartElementLocalName(VALUE)) {
                value = this.reader.getElementText();
              }
            }
            if (value != null && name.equals(this.geometryFieldName)) {
              value = this.geometryFactory.geometry(value.toString());
            }
            record.setValue(name, value);
          }

          record.setState(RecordState.PERSISTED);
          this.currentRecordCount++;
          this.totalRecordCount++;
          return record;
        } catch (final Throwable e) {
          if (!this.closed) {
            Logs.debug(this,
              "Error reading: " + this.layer.getResource(this.queryParameters).getUriString(), e);
          }
          close();
        }
      }
    }
    throw new NoSuchElementException();
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  protected StaxReader newParser() {
    if (this.closed) {
      throw new NoSuchElementException();
    } else {
      this.currentRecordCount = 0;
      if (this.supportsPaging) {
        this.queryParameters.put("resultOffset", this.queryOffset + this.totalRecordCount);
        if (this.serverLimit > 0) {
          this.queryParameters.put("resultRecordCount", this.serverLimit);
        }
      }
      final Resource resource = this.layer.getResource(this.queryParameters);
      this.reader = StaxReader.newXmlReader(resource);
      if (!this.reader.skipToStartElement("Features")) {
        throw new NoSuchElementException();
      }
      return this.reader;
    }
  }
}
