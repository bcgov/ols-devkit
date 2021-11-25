package com.revolsys.record.io.format.kml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.revolsys.io.AbstractMapWriter;
import com.revolsys.io.FileUtil;

public class KmzMapWriter extends AbstractMapWriter {

  private KmlMapWriter kmlWriter;

  private final ZipOutputStream zipOut;

  public KmzMapWriter(final OutputStream out) {
    try {
      this.zipOut = new ZipOutputStream(out);
      final ZipEntry entry = new ZipEntry("doc.kml");
      this.zipOut.putNextEntry(entry);
      final java.io.Writer writer = FileUtil.newUtf8Writer(this.zipOut);
      this.kmlWriter = new KmlMapWriter(writer);
    } catch (final Throwable e) {
      throw new RuntimeException("Unable to create KMZ file ", e);
    }

  }

  @Override
  public void close() {
    try {
      try {
        this.kmlWriter.close();
      } finally {
        this.zipOut.close();
      }
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
  public void setProperty(final String name, final Object value) {
    this.kmlWriter.setProperty(name, value);
  }

  @Override
  public void write(final Map<String, ? extends Object> values) {
    this.kmlWriter.write(values);
  }

}
