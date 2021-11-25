package com.revolsys.record.io.format.json;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.number.Doubles;
import org.jeometry.common.number.Numbers;

import com.revolsys.collection.list.Lists;
import com.revolsys.io.StringPrinter;
import com.revolsys.io.map.MapSerializer;

public final class JsonWriterUtil {
  public static final String[] CHARACTER_ESCAPE;

  public static char CHARACTER_ESCAPE_END = ' ';
  static {
    CHARACTER_ESCAPE = new String[32];
    for (int i = 0; i < CHARACTER_ESCAPE_END; i++) {
      CHARACTER_ESCAPE[i] = String.format("\\u%04x", i);
    }
    CHARACTER_ESCAPE['\b'] = "\\b";
    CHARACTER_ESCAPE['\f'] = "\\f";
    CHARACTER_ESCAPE['\n'] = "\\n";
    CHARACTER_ESCAPE['\r'] = "\\r";
    CHARACTER_ESCAPE['\t'] = "\\t";
  }

  public static void charSequence(final Appendable out, final CharSequence string)
    throws IOException {
    for (int i = 0; i < string.length(); i++) {
      final char c = string.charAt(i);
      if (c < CHARACTER_ESCAPE_END) {
        out.append(CHARACTER_ESCAPE[c]);
      } else if (c == '"') {
        out.append("\\\"");
      } else if (c == '\\') {
        out.append("\\\\");
      } else {
        out.append(c);
      }
    }
  }

  public static void endAttribute(final Writer out, final String indent) throws IOException {
    out.write(',');
    newLine(out, indent);
  }

  public static void endList(final Writer out) throws IOException {
    out.write(']');
  }

  public static void endObject(final Writer out) throws IOException {
    out.write('}');
  }

  public static void label(final Writer out, final String key, final String indent)
    throws IOException {
    writeIndent(out, indent);
    out.write('"');
    charSequence(out, key);
    out.write('"');

    out.write(":");
  }

  public static void newLine(final Writer out, final String indent) throws IOException {
    if (indent != null) {
      out.write('\n');
    }
  }

  public static void startList(final Writer out, final String indent) throws IOException {
    out.write('[');
    newLine(out, indent);
  }

  public static void startObject(final Writer out, final String indent) throws IOException {
    out.write('{');
    newLine(out, indent);
  }

  public static void write(final Writer out, final Collection<? extends Object> values,
    final String indent, final boolean writeNulls) throws IOException {
    startList(out, indent);
    String newIndent = indent;
    if (newIndent != null) {
      newIndent += "  ";
    }
    if (values != null) {
      int i = 0;
      final int size = values.size();
      final Iterator<? extends Object> iterator = values.iterator();
      while (i < size - 1) {
        writeIndent(out, newIndent);
        final Object value = iterator.next();
        write(out, value, newIndent, writeNulls);
        endAttribute(out, indent);
        i++;
      }
      if (iterator.hasNext()) {
        writeIndent(out, newIndent);
        final Object value = iterator.next();
        write(out, value, newIndent, writeNulls);
        newLine(out, indent);
      }
    }
    writeIndent(out, indent);
    endList(out);
  }

  public static void write(final Writer out, final Map<String, ? extends Object> values,
    final String indent, final boolean writeNulls) throws IOException {

    startObject(out, indent);
    if (values != null) {
      String newIndent = indent;
      if (newIndent != null) {
        newIndent += "  ";
      }
      final Set<String> fields = values.keySet();
      boolean hasValue = false;
      for (final String key : fields) {
        if (hasValue) {
          endAttribute(out, indent);
        } else {
          hasValue = true;
        }
        final Object value = values.get(key);
        label(out, key, newIndent);
        write(out, value, newIndent, writeNulls);
      }
      if (hasValue) {
        newLine(out, newIndent);
      }
    }
    writeIndent(out, indent);
    endObject(out);
  }

  @SuppressWarnings("unchecked")
  public static void write(final Writer out, final Object value, final String indent,
    final boolean writeNulls) throws IOException {
    if (value == null) {
      out.write("null");
    } else if (value instanceof StringPrinter) {
      final StringPrinter printer = (StringPrinter)value;
      printer.write(out);
    } else if (value instanceof Boolean) {
      if ((Boolean)value) {
        out.write("true");
      } else {
        out.write("false");
      }
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      String string = Numbers.toString(number);
      if ("NaN".equals(string)) {
        string = "null";
      } else if ("Infinity".equals(string)) {
        string = Doubles.MAX_DOUBLE_STRING;
      } else if ("-Infinity".equals(string)) {
        string = Doubles.MIN_DOUBLE_STRING;
      }
      out.write(string);
    } else if (value instanceof Collection) {
      final Collection<? extends Object> list = (Collection<? extends Object>)value;
      write(out, list, indent, writeNulls);
    } else if (value instanceof Map) {
      final Map<String, ? extends Object> map = (Map<String, ? extends Object>)value;
      write(out, map, indent, false);
    } else if (value instanceof CharSequence) {
      final CharSequence string = (CharSequence)value;
      out.write('"');
      charSequence(out, string);
      out.write('"');
    } else if (value.getClass().isArray()) {
      final List<? extends Object> list = Lists.arrayToList(value);
      write(out, list, indent, writeNulls);
    } else {
      write(out, DataTypes.toString(value), indent, writeNulls);
    }

  }

  protected static void writeIndent(final Writer out, final String indent) throws IOException {
    if (indent != null) {
      out.write(indent);
    }
  }

  private JsonWriterUtil() {
  }

  public static void appendList(final Appendable appendable,
    final Collection<? extends Object> values) throws IOException {
    appendable.append('[');
    boolean first = true;
    for (final Object value : values) {
      if (first) {
        first = false;
      } else {
        appendable.append(',');
      }
      JsonWriterUtil.appendValue(appendable, value);
    }
    appendable.append(']');
  }

  public static <K, V> void appendMap(final Appendable appendable, final Map<K, V> map)
    throws IOException {
    appendable.append('{');
    boolean first = true;
    for (final K key : map.keySet()) {
      if (first) {
        first = false;
      } else {
        appendable.append(',');
      }
      final V value = map.get(key);
      appendable.append('"');
      charSequence(appendable, key.toString());
      appendable.append("\":");
      JsonWriterUtil.appendValue(appendable, value);
    }
    appendable.append('}');
  }

  public static void appendText(final Appendable appendable, final Object value) {
    try {
      final String string = DataTypes.toString(value);
      if (string == null) {
        appendable.append("null");
      } else {
        appendable.append('"');
      }
      appendValue(appendable, string);
      appendable.append('"');
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  @SuppressWarnings("unchecked")
  public static void appendValue(final Appendable appendable, final Object value) {
    try {
      if (value == null) {
        appendable.append("null");
      } else if (value instanceof Boolean) {
        if ((Boolean)value) {
          appendable.append("true");
        } else {
          appendable.append("false");
        }
      } else if (value instanceof Number) {
        final Number number = (Number)value;
        final double doubleValue = number.doubleValue();
        if (Double.isInfinite(doubleValue) || Double.isNaN(doubleValue)) {
          appendable.append("null");
        } else {
          appendable.append(Doubles.toString(doubleValue));
        }
      } else if (value instanceof JsonType) {
        final JsonType jsonType = (JsonType)value;
        jsonType.appendJson(appendable);
      } else if (value instanceof Jsonable) {
        final JsonType json = ((MapSerializer)value).asJson();
        if (json != null) {
          json.appendJson(appendable);
        }
      } else if (value instanceof Collection) {
        final Collection<? extends Object> list = (Collection<? extends Object>)value;
        appendList(appendable, list);
      } else if (value instanceof Map) {
        final Map<Object, Object> map = (Map<Object, Object>)value;
        appendMap(appendable, map);
      } else if (value instanceof CharSequence) {
        final CharSequence string = (CharSequence)value;
        appendable.append('"');
        charSequence(appendable, string);
        appendable.append('"');
      } else if (value.getClass().isArray()) {
        final List<? extends Object> list = Lists.arrayToList(value);
        appendList(appendable, list);
      } else {
        appendText(appendable, value);
      }
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

}
