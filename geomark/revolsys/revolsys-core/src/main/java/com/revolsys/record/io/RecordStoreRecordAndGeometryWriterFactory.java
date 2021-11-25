package com.revolsys.record.io;

import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.spring.resource.Resource;

public class RecordStoreRecordAndGeometryWriterFactory extends AbstractIoFactory
  implements RecordWriterFactory {

  public RecordStoreRecordAndGeometryWriterFactory(final String name, final String mediaType,
    final boolean geometrySupported, final boolean customAttributionSupported,
    final Iterable<String> fileExtensions) {
    super(name);
    for (final String fileExtension : fileExtensions) {
      addMediaTypeAndFileExtension(mediaType, fileExtension);
    }
  }

  public RecordStoreRecordAndGeometryWriterFactory(final String name, final String mediaType,
    final boolean geometrySupported, final boolean customAttributionSupported,
    final String... fileExtensions) {
    this(name, mediaType, geometrySupported, customAttributionSupported,
      Arrays.asList(fileExtensions));
  }

  @Override
  public RecordWriter newRecordWriter(final RecordDefinitionProxy recordDefinition,
    final Resource resource) {
    final File file = resource.getFile();
    final RecordStore recordStore = RecordStore.newRecordStore(file);
    if (recordStore == null) {
      return null;
    } else {
      recordStore.initialize();
      return new RecordStoreRecordWriter(recordStore, recordDefinition);
    }
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinitionProxy recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    throw new UnsupportedOperationException("Writing to a stream not currently supported");
  }
}
