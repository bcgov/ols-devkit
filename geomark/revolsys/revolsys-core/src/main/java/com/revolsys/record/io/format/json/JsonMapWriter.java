package com.revolsys.record.io.format.json;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.AbstractMapWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;

public class JsonMapWriter extends AbstractMapWriter {
  private final boolean indent;

  private boolean listRoot;

  /** The writer */
  private Writer out;

  private boolean singleObject;

  private boolean written = false;

  public JsonMapWriter(final Writer out) {
    this(out, true);
  }

  public JsonMapWriter(final Writer out, final boolean indent) {
    this.out = out;
    this.indent = indent;
  }

  /**
   * Closes the underlying reader.
   */
  @Override
  public void close() {
    if (this.out != null) {
      try {
        if (!this.written) {
          writeHeader();
        }
        if (!this.singleObject) {
          newLine();
          if (this.listRoot) {
            this.out.write("]");
          } else {
            this.out.write("]}");
          }
        }
        final String callback = getProperty(IoConstants.JSONP_PROPERTY);
        if (callback != null) {
          this.out.write(");");
        }
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

  public boolean isListRoot() {
    return this.listRoot;
  }

  private void newLine() throws IOException {
    if (this.indent) {
      this.out.write('\n');
    }
  }

  public void setListRoot(final boolean listRoot) {
    this.listRoot = listRoot;
  }

  public void setSingleObject(final boolean singleObject) {
    setProperty(IoConstants.SINGLE_OBJECT_PROPERTY, singleObject);
  }

  @Override
  public void write(final Map<String, ? extends Object> values) {
    try {
      if (this.written) {
        this.out.write(',');
        newLine();
      } else {
        writeHeader();
      }
      String indentString = null;
      if (this.indent) {
        if (this.singleObject) {
          indentString = "";
        } else {
          indentString = "  ";
          this.out.write(indentString);
        }
      }
      JsonWriterUtil.write(this.out, values, indentString, isWriteNulls());
      newLine();
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private void writeHeader() throws IOException {
    final String callback = getProperty(IoConstants.JSONP_PROPERTY);
    if (callback != null) {
      this.out.write(callback);
      this.out.write('(');
    }
    this.listRoot = Boolean.TRUE.equals(getProperty(IoConstants.JSON_LIST_ROOT_PROPERTY));
    this.singleObject = Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY));

    if (!this.singleObject) {
      if (this.listRoot) {
        this.out.write('[');
        newLine();
      } else {
        this.out.write("{\"items\": [");
        newLine();
      }
    }
    this.written = true;
  }
}
