package com.revolsys.record.io.format.kml;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinitionProxy;

public class KmzRecordWriter extends AbstractRecordWriter {

  private final KmlRecordWriter kmlWriter;

  private final ZipOutputStream zipOut;

  public KmzRecordWriter(final RecordDefinitionProxy recordDefinition, final OutputStream out,
    final Charset charset) {
    super(recordDefinition);
    try {
      final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(out);
      this.zipOut = new ZipOutputStream(bufferedOutputStream);
      final ZipEntry entry = new ZipEntry("doc.kml");
      this.zipOut.putNextEntry(entry);
      final OutputStreamWriter writer = FileUtil.newUtf8Writer(this.zipOut);
      this.kmlWriter = new KmlRecordWriter(recordDefinition, writer);
    } catch (final Throwable e) {
      throw new RuntimeException("Unable to create KMZ file", e);
    }

  }

  @Override
  public void close() {
    try {
      this.kmlWriter.close();
      this.zipOut.close();
    } catch (final IOException e) {
    }
  }

  @Override
  public void flush() {
    try {
      this.kmlWriter.flush();
      this.zipOut.flush();
    } catch (final IOException e) {
      throw new RuntimeException("Unable to flush: ", e);
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.kmlWriter.getGeometryFactory();
  }

  @Override
  public void open() {
    this.kmlWriter.open();
  }

  @Override
  public void setProperty(final String name, final Object value) {
    super.setProperty(name, value);
    this.kmlWriter.setProperty(name, value);
  }

  @Override
  public String toString() {
    return "KMZ Writer";
  }

  @Override
  public void write(final Record object) {
    this.kmlWriter.write(object);
  }

}
