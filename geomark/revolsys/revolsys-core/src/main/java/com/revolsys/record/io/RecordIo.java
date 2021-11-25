package com.revolsys.record.io;

import com.revolsys.io.Writer;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;

public interface RecordIo {

  static void copyRecords(final Iterable<? extends Record> reader, final Writer<Record> writer) {
    if (reader != null && writer != null) {
      for (final Record record : reader) {
        writer.write(record);
      }
    }
  }

  static void copyRecords(final Object source, final Object target) {
    try (
      RecordReader reader = RecordReader.newRecordReader(source)) {
      if (reader == null) {
        throw new IllegalArgumentException("Unable to read " + source);
      } else {
        copyRecords(reader, target);
      }
    }

  }

  static void copyRecords(final Object source, final Writer<Record> writer) {
    try (
      RecordReader reader = RecordReader.newRecordReader(source)) {
      if (reader == null) {
        throw new IllegalArgumentException("Unable to read " + source);
      } else {
        copyRecords((Iterable<Record>)reader, writer);
      }
    }

  }

  static void copyRecords(final RecordDefinition recordDefinition,
    final Iterable<? extends Record> records, final Object target) {
    if (target != null) {
      final Resource resource = Resource.getResource(target);
      if (resource instanceof PathResource) {
        final PathResource pathResource = (PathResource)resource;
        pathResource.deleteDirectory();
      }
      try (
        RecordWriter writer = RecordWriter.newRecordWriter(recordDefinition, resource)) {
        if (writer == null) {
          throw new IllegalArgumentException("Unable to create writer " + target);
        } else {
          copyRecords(records, writer);
        }
      }
    }
  }

  static void copyRecords(final RecordReader reader, final Object target) {
    if (reader != null) {
      final RecordDefinition recordDefinition = reader.getRecordDefinition();
      copyRecords(recordDefinition, reader, target);
    }
  }
}
