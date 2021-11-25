package com.revolsys.record.io.format.kml;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.spring.resource.Resource;

public class KmzGeometryIterator extends AbstractIterator<Geometry> implements GeometryReader {

  private KmlGeometryReader kmlIterator;

  private ZipInputStream zipIn;

  public KmzGeometryIterator(final Resource resource, final MapEx properties) {
    try {
      final InputStream in = resource.getInputStream();
      this.zipIn = new ZipInputStream(in);
    } catch (final Throwable e) {
      throw new RuntimeException("Unable to reade KMZ file", e);
    }
    setProperties(properties);
  }

  @Override
  protected void closeDo() {
    FileUtil.closeSilent(this.kmlIterator, this.zipIn);
    this.kmlIterator = null;
    this.zipIn = null;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.kmlIterator.getGeometryFactory();
  }

  @Override
  protected Geometry getNext() {
    if (this.kmlIterator == null) {
      throw new NoSuchElementException();
    } else {
      return this.kmlIterator.getNext();
    }
  }

  @Override
  protected void initDo() {
    try {
      for (ZipEntry entry = this.zipIn.getNextEntry(); entry != null; entry = this.zipIn
        .getNextEntry()) {
        final String name = entry.getName();
        final String extension = FileUtil.getFileNameExtension(name);
        if ("kml".equals(extension)) {
          this.kmlIterator = new KmlGeometryReader(this.zipIn);
          return;
        }
      }
    } catch (final IOException e) {
      throw new RuntimeException("Unable to read KML file inside KMZ file", e);
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
