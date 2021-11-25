package com.revolsys.record.io.format.json;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.number.Numbers;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.record.Record;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;

public class JsonRecordWriter extends AbstractRecordWriter {

  private int depth = 0;

  private Writer out;

  private boolean singleObject;

  private boolean startAttribute;

  private boolean written;

  private final JsonStringEncodingWriter encodingOut;

  private String itemsPropertyName = "items";

  private JsonObject header;

  private JsonObject footer;

  public JsonRecordWriter(final RecordDefinitionProxy recordDefinition, final Writer out) {
    super(recordDefinition);
    this.out = out;
    this.encodingOut = new JsonStringEncodingWriter(out);
  }

  @Override
  public void close() {
    try {
      final Writer out = this.out;
      if (out != null) {
        if (this.singleObject) {
          if (!this.written) {
            out.write("{}\n");
          }
        } else {
          if (!this.written) {
            writeHeader();
          }
          out.write("\n]");
          writeExtraValues(this.footer, true);

          final MapEx extraProperties = getProperty("extraProperties", MapEx.EMPTY);
          for (final Entry<String, Object> entry : extraProperties.entrySet()) {
            final String name = entry.getKey();
            final Object value = entry.getValue();
            this.out.write(",\n");
            label(name);
            value(null, value);
          }
          out.write("}\n");
        }
        final String callback = getProperty(IoConstants.JSONP_PROPERTY);
        if (callback != null) {
          out.write(");\n");
        }
      }
    } catch (final IOException e) {
    } finally {
      FileUtil.closeSilent(this.out);
      this.out = null;
    }
  }

  private void endList() throws IOException {
    final Writer out = this.out;
    this.depth--;
    out.write('\n');
    indent();
    out.write(']');
  }

  private void endObject() throws IOException {
    final Writer out = this.out;
    this.depth--;
    out.write('\n');
    indent();
    out.write('}');
  }

  @Override
  public void flush() {
    try {
      final Writer out = this.out;
      if (out != null) {
        out.flush();
      }
    } catch (final IOException e) {
    }
  }

  public JsonObject getHeader() {
    return this.header;
  }

  public String getItemsPropertyName() {
    return this.itemsPropertyName;
  }

  private void indent() throws IOException {
    final Writer out = this.out;
    if (isIndent()) {
      for (int i = 0; i < this.depth; i++) {
        out.write(' ');
      }
    }
  }

  private void label(final String key) throws IOException {
    final Writer out = this.out;
    indent();
    out.write('"');
    this.encodingOut.write(key);
    out.write('"');
    out.write(':');
    if (isIndent()) {
      out.write(' ');
    }
    this.startAttribute = true;
  }

  private void list(final List<? extends Object> values) throws IOException {
    startList();
    int i = 0;
    final int size = values.size();
    final Iterator<? extends Object> iterator = values.iterator();
    while (i < size - 1) {
      final Object value = iterator.next();
      value(null, value);
      this.out.write(",\n");
      this.startAttribute = false;
      i++;
    }
    if (iterator.hasNext()) {
      final Object value = iterator.next();
      value(null, value);
    }
    endList();
  }

  public void setFooter(final JsonObject footer) {
    this.footer = footer;
  }

  public void setHeader(final JsonObject header) {
    this.header = header;
  }

  public void setItemsPropertyName(final String itemsPropertyName) {
    this.itemsPropertyName = itemsPropertyName;
  }

  private void startList() throws IOException {
    final Writer out = this.out;
    if (!this.startAttribute) {
      indent();
    }
    out.write("[\n");
    this.depth++;
    this.startAttribute = false;
  }

  private void startObject() throws IOException {
    final Writer out = this.out;
    if (!this.startAttribute) {
      indent();
    }
    out.write("{\n");
    this.depth++;
    this.startAttribute = false;
  }

  private void string(final String string) throws IOException {
    final Writer out = this.out;
    out.write('"');
    this.encodingOut.write(string);
    out.write('"');
  }

  @Override
  public String toString() {
    return getPathName().toString();
  }

  @SuppressWarnings("unchecked")
  private void value(final DataType dataType, final Object value) throws IOException {
    final Writer out = this.out;
    if (value == null) {
      out.write("null");
    } else if (value instanceof Boolean) {
      if ((Boolean)value) {
        out.write("true");
      } else {
        out.write("false");
      }
    } else if (value instanceof Number) {
      out.write(Numbers.toString((Number)value));
    } else if (value instanceof List) {
      final List<? extends Object> list = (List<? extends Object>)value;
      list(list);
    } else if (value instanceof Map) {
      final Map<String, ? extends Object> map = (Map<String, ? extends Object>)value;
      write(map);
    } else if (value instanceof CharSequence) {
      final CharSequence string = (CharSequence)value;
      string(string.toString());
    } else if (dataType == null) {
      string(value.toString());
    } else {
      final String string = dataType.toString(value);
      string(string);
    }
  }

  @Override
  public void write(final Map<String, ? extends Object> values) {
    try {
      startObject();
      boolean first = true;
      for (final Entry<String, ? extends Object> entry : values.entrySet()) {
        final String key = entry.getKey();
        final Object value = entry.getValue();
        if (value != null) {
          if (!first) {
            this.out.write(",\n");
          }
          label(key);
          value(null, value);
          first = false;
        }
      }
      endObject();
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public void write(final Record record) {
    try {
      final Writer out = this.out;
      if (this.written) {
        out.write(",\n");
      } else {
        writeHeader();
      }
      startObject();
      boolean hasValue = false;
      RecordDefinition recordDefinition = getRecordDefinition();
      if (recordDefinition == null) {
        recordDefinition = record.getRecordDefinition();
      }
      final List<FieldDefinition> fieldDefinitions = recordDefinition.getFieldDefinitions();

      for (final FieldDefinition field : fieldDefinitions) {
        final int fieldIndex = field.getIndex();
        final Object value;
        if (isWriteCodeValues()) {
          value = record.getCodeValue(fieldIndex);
        } else {
          value = record.getValue(fieldIndex);
        }
        if (isValueWritable(value)) {
          if (hasValue) {
            this.out.write(",\n");
          } else {
            hasValue = true;
          }
          final String name = field.getName();
          label(name);

          final DataType dataType = field.getDataType();
          value(dataType, value);
        }
      }
      endObject();
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private boolean writeExtraValues(final JsonObject values, boolean hasPrevious)
    throws IOException {
    final Writer out = this.out;
    if (values != null) {
      for (final String name : values.keySet()) {
        if (hasPrevious) {
          out.write(",\n");
        } else {
          hasPrevious = true;
        }
        final Object value = values.getValue(name);
        label(name);

        final DataType dataType = DataTypes.getDataType(value);
        value(dataType, value);
      }
    }
    return hasPrevious;
  }

  private void writeHeader() throws IOException {
    final Writer out = this.out;
    final String callback = getProperty(IoConstants.JSONP_PROPERTY);
    if (callback != null) {
      out.write(callback);
      out.write('(');
    }
    this.singleObject = Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY));
    if (!this.singleObject) {
      out.write("{");
      if (writeExtraValues(this.header, false)) {
        out.write(",\n");
      }
      out.write('"');
      out.write(this.itemsPropertyName);
      out.write("\": [\n");
    }
    this.written = true;
  }
}
