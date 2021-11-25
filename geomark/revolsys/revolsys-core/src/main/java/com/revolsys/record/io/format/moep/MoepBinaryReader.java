package com.revolsys.record.io.format.moep;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.AbstractReader;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;

public class MoepBinaryReader extends AbstractReader<Record> implements RecordReader {

  private MoepBinaryIterator iterator;

  /**
   * Construct a new MoepBinaryReader.
   *
   * @param moepDirectoryReader
   * @param file the file.
   * @param factory The factory used to create Record instances.
   */
  public MoepBinaryReader(final MoepDirectoryReader moepDirectoryReader, final Resource resource,
    final RecordFactory factory) {
    final InputStream in = resource.getInputStream();
    this.iterator = new MoepBinaryIterator(moepDirectoryReader, resource.getBaseName(), in,
      factory);
  }

  /**
   * Construct a new MoepBinaryReader.
   *
   * @param url The url to the file.
   * @param factory The factory used to create Record instances.
   */
  public MoepBinaryReader(final URL url, final RecordFactory factory) {
    try {
      final InputStream in = url.openStream();
      final String path = url.getPath();
      String fileName = path;
      final int slashIndex = fileName.lastIndexOf('/');
      if (slashIndex != -1) {
        fileName = fileName.substring(slashIndex + 1);
      }
      this.iterator = new MoepBinaryIterator(null, fileName, in, factory);
    } catch (final IOException e) {
    }
  }

  @Override
  public void close() {
    this.iterator.close();
  }

  @Override
  public MapEx getProperties() {
    return this.iterator.getProperties();
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return MoepConstants.RECORD_DEFINITION;
  }

  /**
   * Get the iterator for the MOEP file.
   *
   * @return The iterator.
   */
  @Override
  public Iterator iterator() {
    return this.iterator;
  }

  @Override
  public void open() {
    this.iterator.hasNext();
  }

}
