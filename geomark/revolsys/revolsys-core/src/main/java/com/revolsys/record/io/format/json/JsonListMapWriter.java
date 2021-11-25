package com.revolsys.record.io.format.json;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.AbstractMapWriter;
import com.revolsys.io.FileUtil;

public class JsonListMapWriter extends AbstractMapWriter {

  /** The writer */
  private Writer out;

  boolean written = false;

  public JsonListMapWriter(final Writer out) {
    this.out = out;
    try {
      this.out.write('[');
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  /**
   * Closes the underlying reader.
   */
  @Override
  public void close() {
    if (this.out != null) {
      try {
        this.out.write("\n]\n");
      } catch (final IOException e) {
      } finally {
        FileUtil.closeSilent(this.out);
        this.out = null;
      }
    }
  }

  @Override
  public void flush() {
    try {
      this.out.flush();
    } catch (final IOException e) {
    }
  }

  @Override
  public void write(final Map<String, ? extends Object> values) {
    try {
      if (this.written) {
        this.out.write(',');
      } else {
        this.written = true;
      }
      this.out.write('\n');
      JsonWriterUtil.write(this.out, values, null, isWriteNulls());
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }
}
