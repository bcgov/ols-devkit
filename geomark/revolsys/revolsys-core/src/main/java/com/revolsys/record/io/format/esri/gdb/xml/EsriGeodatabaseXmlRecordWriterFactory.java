package com.revolsys.record.io.format.esri.gdb.xml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.schema.RecordDefinitionProxy;

public class EsriGeodatabaseXmlRecordWriterFactory extends AbstractIoFactory
  implements RecordWriterFactory {
  public EsriGeodatabaseXmlRecordWriterFactory() {
    super(EsriGeodatabaseXmlConstants.FORMAT_DESCRIPTION);
    addMediaTypeAndFileExtension(EsriGeodatabaseXmlConstants.MEDIA_TYPE,
      EsriGeodatabaseXmlConstants.FILE_EXTENSION);
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinitionProxy recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset);
    return new EsriGeodatabaseXmlRecordWriter(recordDefinition, writer);
  }
}
