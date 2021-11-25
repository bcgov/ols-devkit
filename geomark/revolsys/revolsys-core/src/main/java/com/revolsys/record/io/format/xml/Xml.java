package com.revolsys.record.io.format.xml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.map.IteratorMapReader;
import com.revolsys.io.map.MapReader;
import com.revolsys.io.map.MapReaderFactory;
import com.revolsys.io.map.MapWriter;
import com.revolsys.io.map.MapWriterFactory;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.spring.resource.Resource;

public class Xml extends AbstractIoFactory
  implements RecordWriterFactory, MapReaderFactory, MapWriterFactory {
  public static MapEx toMap(final Resource resource) {
    final XmlMapIterator iterator = new XmlMapIterator(resource, true);
    try {
      if (iterator.hasNext()) {
        return iterator.next();
      } else {
        return null;
      }
    } finally {
      iterator.close();
    }
  }

  public Xml() {
    super("XML");
    addMediaTypeAndFileExtension("text/xml", "xml");
  }

  @Override
  public boolean isReadFromZipFileSupported() {
    return true;
  }

  @Override
  public MapReader newMapReader(final Resource resource) {
    final XmlMapIterator iterator = new XmlMapIterator(resource);
    return new IteratorMapReader(iterator);
  }

  @Override
  public MapWriter newMapWriter(final Writer out) {
    return new XmlMapWriter(out);
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinitionProxy recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset);
    return new XmlRecordWriter(recordDefinition, writer);
  }
}
