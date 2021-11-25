package com.revolsys.record.io.format.json;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.number.Doubles;
import org.jeometry.common.number.Numbers;

import com.revolsys.collection.list.Lists;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.record.Record;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Property;

public final class JsonWriter implements BaseCloseable {

  private int depth = 0;

  private boolean indent;

  private Writer out;

  private boolean startAttribute;

  private final JsonStringEncodingWriter encodingOut;

  public JsonWriter(final Writer out) {
    this(out, true);
  }

  public JsonWriter(final Writer out, final boolean indent) {
    this.out = out;
    this.indent = indent;
    this.encodingOut = new JsonStringEncodingWriter(out);

  }

  public void charSequence(final CharSequence string) {
    try {
      JsonWriterUtil.charSequence(this.out, string);
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public void close() {
    FileUtil.closeSilent(this.out);
    this.out = null;
  }

  public void endAttribute() {
    try {
      this.out.write(",");
      newLine();
      this.startAttribute = false;
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  public void endList() {
    try {
      this.depth--;
      newLine();
      indent();
      this.out.write("]");
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  public void endObject() {
    try {
      this.depth--;
      newLine();
      indent();
      this.out.write("}");
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  public void flush() {
    try {
      this.out.flush();
    } catch (final Exception e) {
    }
  }

  public void indent() {
    try {
      if (this.indent) {
        for (int i = 0; i < this.depth; i++) {
          this.out.write("  ");
        }
      }
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  public void label(final String key) {
    try {
      indent();
      value(key);
      this.out.write(": ");
      this.startAttribute = true;
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  public void labelValue(final String key, final Object value) {
    label(key);
    value(value);
  }

  public void list(final Iterable<?> values) throws IOException {
    startList();
    final Iterator<?> iterator = values.iterator();

    if (iterator.hasNext()) {
      if (this.indent) {
        {
          final Object value = iterator.next();
          indent();
          value(value);
        }
        while (iterator.hasNext()) {
          endAttribute();
          indent();
          final Object value = iterator.next();
          value(value);
        }
      } else {
        {
          final Object value = iterator.next();
          value(value);
        }
        while (iterator.hasNext()) {
          endAttribute();
          final Object value = iterator.next();
          value(value);
        }
      }
    }
    endList();
  }

  public void list(final Object... values) throws IOException {
    startList();
    final int size = values.length;

    if (size > 0) {
      if (this.indent) {
        {
          final Object value = values[0];
          indent();
          value(value);
        }
        for (int index = 1; index < size; index++) {
          endAttribute();
          indent();
          final Object value = values[index];
          value(value);
        }
      } else {
        {
          final Object value = values[0];
          value(value);
        }
        for (int index = 1; index < size; index++) {
          endAttribute();
          final Object value = values[index];
          value(value);
        }
      }
    }
    endList();
  }

  public void newLine() {
    try {
      if (this.indent) {
        this.out.write('\n');
      }
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  public void newLineForce() {
    try {
      this.out.write('\n');
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  public void print(final char value) {
    try {
      this.out.write(value);
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  public void print(final Object value) {
    if (value != null) {
      try {
        this.out.write(value.toString());
      } catch (final Exception e) {
        throw Exceptions.wrap(e);
      }
    }
  }

  public void setIndent(final boolean indent) {
    this.indent = indent;
  }

  public void startList() {
    final boolean indent = true;
    startList(indent);
  }

  public void startList(final boolean indent) {
    try {
      if (indent && !this.startAttribute) {
        indent();
      }
      this.out.write('[');
      newLine();
      this.depth++;
      this.startAttribute = false;
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  public void startObject() {
    try {
      // if (!this.startAttribute) {
      // indent();
      // }
      this.out.write('{');
      newLine();
      this.depth++;
      this.startAttribute = false;
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  public void string(final String string) throws IOException {
    final Writer out = this.out;
    out.write('"');
    this.encodingOut.write(string);
    out.write('"');
  }

  public void value(final DataType dataType, final Object value) throws IOException {
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

  @SuppressWarnings("unchecked")
  public void value(final Object value) {
    try {
      if (value == null) {
        this.out.write("null");
      } else if (value instanceof Boolean) {
        if ((Boolean)value) {
          this.out.write("true");
        } else {
          this.out.write("false");
        }
      } else if (value instanceof Number) {
        final Number number = (Number)value;
        final double doubleValue = number.doubleValue();
        if (Double.isInfinite(doubleValue) || Double.isNaN(doubleValue)) {
          this.out.write("null");
        } else {
          this.out.write(Doubles.toString(doubleValue));
        }
      } else if (value instanceof MapSerializer) {
        final JsonObject map = ((MapSerializer)value).toMap();
        write(map);
      } else if (value instanceof Collection) {
        final Collection<? extends Object> list = (Collection<? extends Object>)value;
        write(list);
      } else if (value instanceof Map) {
        final Map<String, ? extends Object> map = (Map<String, ? extends Object>)value;
        write(map);
      } else if (value instanceof CharSequence) {
        final CharSequence string = (CharSequence)value;
        this.out.write('"');
        charSequence(string);
        this.out.write('"');
      } else if (value.getClass().isArray()) {
        final List<? extends Object> list = Lists.arrayToList(value);
        write(list);
      } else if (value instanceof Jsonable) {
        final JsonType json = ((Jsonable)value).toJson();
        value(json);
      } else {
        value(DataTypes.toString(value));
      }
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  public void write(final Collection<? extends Object> values) throws IOException {
    startList();
    int i = 0;
    final int size = values.size();
    final Iterator<? extends Object> iterator = values.iterator();
    if (this.indent) {
      while (i < size - 1) {
        final Object value = iterator.next();
        indent();
        value(value);
        endAttribute();
        i++;
      }
      if (iterator.hasNext()) {
        indent();
        final Object value = iterator.next();
        value(value);
      }
    } else {
      while (i < size - 1) {
        final Object value = iterator.next();
        value(value);
        endAttribute();
        i++;
      }
      if (iterator.hasNext()) {
        final Object value = iterator.next();
        value(value);
      }
    }
    endList();
  }

  public <K, V> void write(final Map<K, V> values) {
    startObject();
    if (values != null) {
      final Set<Entry<K, V>> entrySet = values.entrySet();
      int i = 0;
      final int size = values.size();
      final Iterator<Entry<K, V>> iterator = entrySet.iterator();
      while (i < size - 1) {
        final Entry<K, V> entry = iterator.next();
        final K key = entry.getKey();
        final Object value = entry.getValue();
        label(key.toString());
        value(value);
        endAttribute();
        i++;
      }
      if (iterator.hasNext()) {
        final Entry<K, V> entry = iterator.next();
        final K key = entry.getKey();
        final Object value = entry.getValue();
        label(key.toString());
        value(value);
      }
    }
    endObject();
  }

  public void writeRecord(final Record record) {
    try {
      final Writer out = this.out;
      startObject();
      boolean hasValue = false;
      final RecordDefinition recordDefinition = record.getRecordDefinition();
      final List<FieldDefinition> fieldDefinitions = recordDefinition.getFieldDefinitions();

      for (final FieldDefinition field : fieldDefinitions) {
        final int fieldIndex = field.getIndex();
        final Object value = record.getValue(fieldIndex);
        if (Property.hasValue(value)) {
          if (hasValue) {
            out.write(",\n");
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
}
