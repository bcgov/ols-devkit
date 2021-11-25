package com.revolsys.record.io.format.csv;

import java.io.IOException;
import java.io.Writer;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.wkt.EWktWriter;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.spring.resource.Resource;

public class CsvRecordWriter extends AbstractRecordWriter {
  private boolean ewkt;

  private final char fieldSeparator;

  /** The writer */
  private Writer out;

  private boolean useQuotes;

  private boolean paused = false;

  private String newLine = "\n";

  private int maxFieldLength = Integer.MAX_VALUE;

  public CsvRecordWriter(final RecordDefinitionProxy recordDefinition, final Object target,
    final char fieldSeparator, final boolean useQuotes, final boolean ewkt) {
    this(recordDefinition, Resource.getResource(target), fieldSeparator, useQuotes, ewkt);
  }

  public CsvRecordWriter(final RecordDefinitionProxy recordDefinition, final Resource resource,
    final char fieldSeparator, final boolean useQuotes, final boolean ewkt) {
    this(recordDefinition, resource.newWriter(), fieldSeparator, useQuotes, ewkt);
    setResource(resource);
    recordDefinition.writePrjFile(resource);
  }

  public CsvRecordWriter(final RecordDefinitionProxy recordDefinition, final Writer out,
    final char fieldSeparator, final boolean useQuotes, final boolean ewkt) {
    super(recordDefinition);
    try {
      this.out = out;
      this.fieldSeparator = fieldSeparator;
      this.useQuotes = useQuotes;
      this.ewkt = ewkt;
      for (int i = 0; i < recordDefinition.getFieldCount(); i++) {
        if (i > 0) {
          this.out.write(fieldSeparator);
        }
        final String name = recordDefinition.getFieldName(i);
        string(name);
      }
      this.out.write(this.newLine);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  /**
   * Closes the underlying reader.
   */
  @Override
  public synchronized void close() {
    final Writer out = this.out;
    if (out != null) {
      try {
        out.flush();
      } catch (final IOException e) {
      }
      try {
        out.close();
      } catch (final IOException e) {
      }
      this.out = null;
    }
  }

  @Override
  public synchronized void flush() {
    if (this.out != null) {
      try {
        this.out.flush();
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    }

  }

  public String getNewLine() {
    return this.newLine;
  }

  public void pause() {
    final Resource resource = getResource();
    if (resource == null) {
      throw new IllegalStateException("Cannot pause without a resource");
    }
    if (!this.paused) {
      final Writer out = this.out;
      if (out != null) {
        this.paused = true;
        flush();
        try {
          out.flush();
        } catch (final IOException e) {
        }
        try {
          out.close();
        } catch (final IOException e) {
        }
        this.out = null;
      }
    }
  }

  public void setEwkt(final boolean ewkt) {
    this.ewkt = ewkt;
  }

  public void setMaxFieldLength(final int maxFieldLength) {
    this.maxFieldLength = maxFieldLength;
  }

  public void setNewLine(final String newLine) {
    this.newLine = newLine;
  }

  public void setUseQuotes(final boolean useQuotes) {
    this.useQuotes = useQuotes;
  }

  private void string(final Object value) throws IOException {
    final Writer out = this.out;
    if (out != null) {
      final String string = value.toString();
      int length = string.length();
      if (length > this.maxFieldLength) {
        length = this.maxFieldLength;
      }
      if (this.useQuotes) {
        out.write('"');
        for (int i = 0; i < length; i++) {
          final char c = string.charAt(i);
          if (c == '"') {
            out.write('"');
          }
          out.write(c);
        }
        out.write('"');
      } else {
        out.write(string, 0, length);
      }
    }
  }

  @Override
  public void write(final Record record) {
    Writer out = this.out;
    if (this.paused) {
      this.paused = false;
      final Resource resource = getResource();
      out = this.out = resource.newWriterAppend();
    }
    if (out != null) {
      try {
        final char fieldSeparator = this.fieldSeparator;
        boolean first = true;
        for (final FieldDefinition field : getFieldDefinitions()) {
          if (first) {
            first = false;
          } else {
            out.write(fieldSeparator);
          }
          final String fieldName = field.getName();
          final Object value;
          if (isWriteCodeValues()) {
            value = record.getCodeValue(fieldName);
          } else {
            value = record.getValue(fieldName);
          }
          if (value instanceof Geometry) {
            final Geometry geometry = (Geometry)value;
            if (this.useQuotes) {
              out.write('"');
              EWktWriter.write(out, geometry, this.ewkt);
              out.write('"');
            } else {
              EWktWriter.write(out, geometry, this.ewkt);
            }
          } else if (value != null) {
            final DataType dataType = field.getDataType();
            final String stringValue = dataType.toString(value);
            if (this.useQuotes && dataType.isRequiresQuotes()) {
              string(stringValue);
            } else {
              int length = stringValue.length();
              if (length > this.maxFieldLength) {
                length = this.maxFieldLength;
              }
              out.write(stringValue, 0, length);
            }
          }
        }
        out.write(this.newLine);
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    }
  }

}
