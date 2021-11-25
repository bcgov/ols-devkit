package com.revolsys.record.io.format.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.AbstractMapWriter;
import com.revolsys.io.FileUtil;

public class CsvMapWriter extends AbstractMapWriter {
  private List<String> fieldNames;

  private final char fieldSeparator;

  /** The writer */
  private Writer out;

  private final boolean useQuotes;

  private String newLine = "\n";

  public CsvMapWriter(final File file) throws FileNotFoundException {
    this(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
  }

  public CsvMapWriter(final Writer out) {
    this(out, Csv.FIELD_SEPARATOR, true);
  }

  public CsvMapWriter(final Writer out, final char fieldSeparator, final boolean useQuotes) {
    this.out = new BufferedWriter(out);
    this.fieldSeparator = fieldSeparator;
    this.useQuotes = useQuotes;
  }

  /**
   * Closes the underlying reader.
   */
  @Override
  public void close() {
    flush();
    FileUtil.closeSilent(this.out);
    this.out = null;
  }

  @Override
  public void flush() {
    if (this.out != null) {
      try {
        this.out.flush();
      } catch (final IOException e) {
      }
    }
  }

  public List<String> getFieldNames() {
    return this.fieldNames;
  }

  public String getNewLine() {
    return this.newLine;
  }

  public void setFieldNames(final Collection<String> fieldNames) {
    assert this.fieldNames == null;
    this.fieldNames = new ArrayList<>(fieldNames);
    write(fieldNames);
  }

  public void setNewLine(final String newLine) {
    this.newLine = newLine;
  }

  public void write(final Collection<? extends Object> values) {
    write(values.toArray());
  }

  @Override
  public void write(final Map<String, ? extends Object> values) {
    final List<Object> fieldValues = new ArrayList<>();
    if (this.fieldNames == null) {
      setFieldNames(values.keySet());
    }
    for (final String fieldName : this.fieldNames) {
      final Object value = values.get(fieldName);
      fieldValues.add(value);
    }
    write(fieldValues);
  }

  public void write(final Object... values) {
    try {
      for (int i = 0; i < values.length; i++) {
        if (i > 0) {
          this.out.write(this.fieldSeparator);
        }
        final Object value = values[i];
        if (value != null) {
          final String string = DataTypes.toString(value);
          if (this.useQuotes) {
            this.out.write('"');
            for (int j = 0; j < string.length(); j++) {
              final char c = string.charAt(j);
              if (c == '"') {
                this.out.write('"');
              }
              this.out.write(c);
            }
            this.out.write('"');
          } else {
            this.out.write(string, 0, string.length());
          }
        }
      }
      this.out.write(this.newLine);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }
}
