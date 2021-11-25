package com.revolsys.webservice;

import java.util.Collections;
import java.util.List;

import org.jeometry.common.io.PathName;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.spring.resource.UrlResource;

public interface WebServiceFeatureLayer extends RecordDefinitionProxy, WebServiceResource {
  default BoundingBox getBoundingBox() {
    return BoundingBox.empty();
  }

  @Override
  default String getIconName() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return "table";
    } else {
      return recordDefinition.getIconName();
    }
  }

  default int getMaxRecordCount() {
    return Integer.MAX_VALUE;
  }

  @Override
  default String getName() {
    final PathName pathName = getPathName();
    return pathName.getName();
  }

  @Override
  default PathName getPathName() {
    return WebServiceResource.super.getPathName();
  }

  @Override
  default Record getRecord(final Query query) {
    Record firstRecord = null;
    try (
      RecordReader records = getRecordReader(query)) {
      for (final Record record : records) {
        if (firstRecord == null) {
          firstRecord = record;
        } else {
          throw new IllegalArgumentException("Query matched multiple objects\n" + query);
        }
      }
    }
    return firstRecord;
  }

  default int getRecordCount(final BoundingBox boundingBox) {
    return 0;
  }

  default int getRecordCount(final Query query) {
    return 0;
  }

  default RecordReader getRecordReader(final BoundingBox boundingBox) {
    return getRecordReader(ArrayRecord.FACTORY, boundingBox);
  }

  RecordReader getRecordReader(final Query query);

  <V extends Record> RecordReader getRecordReader(final RecordFactory<V> recordFactory,
    final BoundingBox boundingBox);

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  default <V extends Record> List<V> getRecords(final Query query) {
    try (
      RecordReader reader = getRecordReader(query)) {
      if (reader == null) {
        return Collections.emptyList();
      } else {
        return (List)reader.toList();
      }
    }
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  default <V extends Record> List<V> getRecords(final RecordFactory<V> recordFactory,
    final BoundingBox boundingBox) {
    try (
      RecordReader reader = getRecordReader(recordFactory, boundingBox)) {
      if (reader == null) {
        return Collections.emptyList();
      } else {
        return (List)reader.toList();
      }
    }
  }

  @Override
  default UrlResource getServiceUrl() {
    return null;
  }

}
