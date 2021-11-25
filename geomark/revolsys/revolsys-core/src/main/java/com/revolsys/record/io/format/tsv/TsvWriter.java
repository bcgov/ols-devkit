package com.revolsys.record.io.format.tsv;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.BaseCloseable;
import com.revolsys.io.FileUtil;

public class TsvWriter implements BaseCloseable {
  /** The writer */
  private final Writer out;

  private String newLine = "\n";

  private boolean useQuotes = true;

  protected TsvWriter(final Writer out) {
    this.out = out;
  }

  @Override
  public void close() {
    flush();
    FileUtil.closeSilent(this.out);
  }

  public void flush() {
    try {
      this.out.flush();
    } catch (final IOException e) {
    }
  }

  public String getNewLine() {
    return this.newLine;
  }

  public boolean isUseQuotes() {
    return this.useQuotes;
  }

  public void setNewLine(final String newLine) {
    this.newLine = newLine;
  }

  public void setUseQuotes(final boolean useQuotes) {
    this.useQuotes = useQuotes;
  }

  public void write(final Collection<? extends Object> values) {
    write(values.toArray());
  }

  public void write(final Object... values) {
    try {
      for (int i = 0; i < values.length; i++) {
        final Object value = values[i];
        if (value != null) {
          final String string = DataTypes.toString(value);
          final boolean useQuotes = this.useQuotes || string.indexOf('\t') != -1;
          if (useQuotes) {
            this.out.write('"');
          }
          for (int j = 0; j < string.length(); j++) {
            final char c = string.charAt(j);
            if (useQuotes && c == '"') {
              this.out.write('"');
            }
            this.out.write(c);
          }
          if (useQuotes) {
            this.out.write('"');
          }
        }
        if (i < values.length - 1) {
          this.out.write('\t');
        }
      }
      this.out.write(this.newLine);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }
}
