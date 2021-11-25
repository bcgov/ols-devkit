package com.revolsys.io.map;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import com.revolsys.io.FileIoFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.spring.resource.Resource;

public interface MapWriterFactory extends FileIoFactory {
  default MapWriter newMapWriter(final Object source) {
    final Resource resource = Resource.getResource(source);
    final Writer writer = resource.newWriter();
    return newMapWriter(writer);
  }

  default MapWriter newMapWriter(final OutputStream out) {
    final Writer writer = FileUtil.newUtf8Writer(out);
    return newMapWriter(writer);
  }

  default MapWriter newMapWriter(final OutputStream out, final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(out, charset);
    return newMapWriter(writer);
  }

  MapWriter newMapWriter(final Writer out);
}
