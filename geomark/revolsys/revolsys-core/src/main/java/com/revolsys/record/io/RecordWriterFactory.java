package com.revolsys.record.io;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.io.GeometryWriter;
import com.revolsys.geometry.io.GeometryWriterFactory;
import com.revolsys.io.FileIoFactory;
import com.revolsys.record.Records;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.spring.resource.Resource;

public interface RecordWriterFactory extends FileIoFactory, GeometryWriterFactory {

  @Override
  default GeometryWriter newGeometryWriter(final Resource resource, final MapEx properties) {
    final RecordDefinition recordDefinition = Records.newGeometryRecordDefinition();
    final RecordWriter recordWriter = newRecordWriter(recordDefinition, resource);
    recordWriter.setProperties(properties);
    return new RecordWriterGeometryWriter(recordWriter);
  }

  @Override
  default GeometryWriter newGeometryWriter(final String baseName, final OutputStream out,
    final Charset charset) {
    final RecordDefinition recordDefinition = Records.newGeometryRecordDefinition();
    final RecordWriter recordWriter = newRecordWriter(baseName, recordDefinition, out, charset);
    return new RecordWriterGeometryWriter(recordWriter);
  }

  /**
   * Construct a new writer to write to the specified resource.
   *
   * @param recordDefinition The recordDefinition for the type of data to write.
   * @param resource The resource to write to.
   * @return The writer.
   */
  default RecordWriter newRecordWriter(final RecordDefinitionProxy recordDefinition,
    final Resource resource) {
    final OutputStream out = resource.newBufferedOutputStream();
    final String baseName = resource.getBaseName();
    return newRecordWriter(baseName, recordDefinition, out);
  }

  default RecordWriter newRecordWriter(final String baseName,
    final RecordDefinitionProxy recordDefinition, final OutputStream outputStream) {
    return newRecordWriter(baseName, recordDefinition, outputStream, StandardCharsets.UTF_8);
  }

  RecordWriter newRecordWriter(String baseName, RecordDefinitionProxy recordDefinition,
    OutputStream outputStream, Charset charset);
}
