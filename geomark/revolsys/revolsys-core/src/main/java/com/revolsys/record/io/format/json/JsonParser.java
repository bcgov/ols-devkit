package com.revolsys.record.io.format.json;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.jeometry.common.exception.WrappedException;
import org.jeometry.common.logging.Logs;
import org.jeometry.common.number.Doubles;
import org.jeometry.common.number.Integers;

import com.revolsys.io.FileUtil;
import com.revolsys.spring.resource.Resource;

public class JsonParser implements Iterator<JsonParser.EventType>, Closeable {
  public enum EventType {
    booleanValue, colon, comma, endArray, endDocument, endObject, nullValue, number, startArray, startDocument, startObject, string, unknown
  }

  public static Map<String, Object> getMap(final InputStream in) {
    if (in == null) {
      return null;
    } else {
      try (
        final JsonParser parser = new JsonParser(in)) {
        if (parser.next() == EventType.startDocument) {
          return parser.getMap();
        } else {
          return Collections.emptyMap();
        }
      }
    }
  }

  public static Map<String, Object> getMap(final Reader reader) {
    final JsonParser parser = new JsonParser(reader);
    try {
      if (parser.next() == EventType.startDocument) {
        return parser.getMap();
      } else {
        return Collections.emptyMap();
      }
    } finally {
      parser.close();
    }
  }

  public static JsonParser newParser(final Object source) {
    Runnable closeAction = null;
    Reader reader;
    if (source instanceof Clob) {
      try {
        final Clob clob = (Clob)source;
        reader = clob.getCharacterStream();

        closeAction = () -> {
          try {
            clob.free();
          } catch (final SQLException e) {
            throw new RuntimeException("Unable to free clob resources", e);
          }
        };
      } catch (final SQLException e) {
        throw new RuntimeException("Unable to read clob", e);
      }
    } else if (source instanceof Reader) {
      reader = (Reader)source;
    } else if (source instanceof CharSequence) {
      reader = new StringReader(source.toString());
    } else {
      try {
        final Resource resource = Resource.getResource(source);
        reader = resource.newBufferedReader();
      } catch (final WrappedException e) {
        reader = new StringReader(source.toString());
      }
    }
    final JsonParser parser = new JsonParser(reader);
    parser.closeAction = closeAction;
    return parser;
  }

  @SuppressWarnings("unchecked")
  public static <V> V read(final InputStream in) {
    return (V)read(FileUtil.newUtf8Reader(in));
  }

  @SuppressWarnings("unchecked")
  public static <V> V read(final JsonParser parser) {
    if (parser.hasNext()) {
      final EventType event = parser.next();
      if (event == EventType.startDocument) {
        final V value = (V)parser.getValue();
        if (parser.hasNext() && parser.next() != EventType.endDocument) {
          throw new IllegalStateException("Extra content at end of file: " + parser);
        }
        return value;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static <V> V read(final Object source) {
    try (
      final JsonParser parser = newParser(source)) {
      final V value = (V)read(parser);
      return value;
    }
  }

  @SuppressWarnings("unchecked")
  public static <V> V read(final Reader in) {
    try (
      final JsonParser parser = new JsonParser(in)) {
      if (parser.hasNext()) {
        final EventType event = parser.next();
        if (event == EventType.startDocument) {
          final V value = (V)parser.getValue();
          if (parser.hasNext() && parser.next() != EventType.endDocument) {
            throw new IllegalStateException("Extra content at end of file: " + parser);
          }
          return value;
        }
      }
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public static <V> V read(final String in) {
    return (V)read(new StringReader(in));
  }

  private int currentCharacter;

  private EventType currentEvent = EventType.startDocument;

  private Object currentValue;

  private int depth;

  private EventType nextEvent = EventType.startDocument;

  private Object nextValue;

  private final Reader reader;

  private Runnable closeAction;

  public JsonParser(final InputStream in) {
    this(FileUtil.newUtf8Reader(in));
  }

  public JsonParser(final Reader reader) {
    this.reader = new BufferedReader(reader, 10000);
    try {
      this.currentCharacter = this.reader.read();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public JsonParser(final Resource resource) {
    this(resource.newBufferedReader());
  }

  @Override
  public void close() {
    FileUtil.closeSilent(this.reader);
    if (this.closeAction != null) {
      this.closeAction.run();
    }
  }

  public JsonList getArray() {
    if (getEvent() == EventType.startArray || hasNext() && next() == EventType.startArray) {
      EventType event = getEvent();
      final JsonList list = JsonList.array();
      do {
        final Object value = getValue();
        if (value instanceof EventType) {
          event = (EventType)value;
          if (event == EventType.comma) {
            throw new IllegalStateException(
              "Missing value before ',' " + FileUtil.getString(this.reader, 80));
          } else if (event == EventType.endArray) {
            if (!list.isEmpty()) {
              throw new IllegalStateException(
                "Missing value after ',' and before ']' " + FileUtil.getString(this.reader, 80));
            }
          }
        } else {
          list.add(value);
          event = next();
        }
      } while (event == EventType.comma);
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not '" + this + ']');
      }
      return list;
    } else {
      throw new IllegalStateException("Exepecting start array, not: " + this);
    }

  }

  @SuppressWarnings("unchecked")
  public <T> T getCurrentValue() {
    return (T)this.currentValue;
  }

  public int getDepth() {
    return this.depth;
  }

  public double[] getDoubleArray() {
    if (getEvent() == EventType.startArray || hasNext() && next() == EventType.startArray) {
      EventType event = getEvent();
      final List<Number> list = new ArrayList<>();
      do {
        final Object value = getValue();
        if (value instanceof EventType) {
          event = (EventType)value;
        } else if (value instanceof Number) {
          list.add((Number)value);
          event = next();
        } else {
          throw new IllegalArgumentException("Expecting number, not: " + value);
        }
      } while (event == EventType.comma);
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not: " + event);
      }

      return Doubles.toDoubleArray(list);
    } else if (getEvent() == EventType.nullValue) {
      return null;
    } else {
      throw new IllegalStateException("Exepecting start array, not: " + getEvent());
    }
  }

  public EventType getEvent() {
    return this.currentEvent;
  }

  public int[] getIntArray() {
    if (getEvent() == EventType.startArray || hasNext() && next() == EventType.startArray) {
      EventType event = getEvent();
      final List<Number> list = new ArrayList<>();
      do {
        final Object value = getValue();
        if (value instanceof EventType) {
          event = (EventType)value;
        } else if (value instanceof Number) {
          list.add((Number)value);
          event = next();
        } else {
          throw new IllegalArgumentException("Expecting number, not: " + value);
        }
      } while (event == EventType.comma);
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not: " + event);
      }

      return Integers.toIntArray(list);
    } else if (getEvent() == EventType.nullValue) {
      return null;
    } else {
      throw new IllegalStateException("Exepecting start array, not: " + getEvent());
    }
  }

  public JsonObject getMap() {
    if (getEvent() == EventType.startObject || hasNext() && next() == EventType.startObject) {
      EventType event = getEvent();
      final JsonObject object = new JsonObjectHash();
      try {
        do {
          if (hasNext() && next() == EventType.string) {
            final String key = getStringIntern();
            if (hasNext()) {
              if (next() == EventType.colon) {
                if (hasNext()) {
                  final Object value = getValue();
                  if (value instanceof EventType) {
                    throw new IllegalStateException(
                      "Exepecting a value, not: " + key + "=" + value);
                  }
                  if (key != null) {
                    object.put(key, value);
                  }
                }
              }
            }
            event = next();
          } else {
            event = getEvent();
          }
        } while (event == EventType.comma);
        if (event != EventType.endObject) {
          throw new IllegalStateException("Exepecting end object, not:" + event);
        }
      } catch (final NoSuchElementException e) {
        Logs.warn(this, "Unexpected end of JSON" + object, e);
      }
      return object;
    } else {
      throw new IllegalStateException("Exepecting end object, not:" + getEvent());
    }

  }

  public String getString() {
    if (getEvent() == EventType.string || hasNext() && next() == EventType.string) {
      return getCurrentValue();
    } else {
      throw new IllegalStateException("Expecting a string");
    }
  }

  public String getStringIntern() {
    final String string = getString();
    if (string == null) {
      return null;
    } else {
      return string.intern();
    }
  }

  public Object getValue() {
    // TODO empty array
    if (hasNext()) {
      final EventType event = next();
      if (event == EventType.startArray) {
        return getArray();
      } else if (event == EventType.startObject) {
        return this.getMap();
      } else if (event == EventType.booleanValue) {
        return getCurrentValue();
      } else if (event == EventType.nullValue) {
        return getCurrentValue();
      } else if (event == EventType.string) {
        return getCurrentValue();
      } else if (event == EventType.number) {
        return getCurrentValue();
      } else {
        return event;
      }
    } else {
      throw new IllegalStateException("Expecting a value not EOF");
    }
  }

  @Override
  public boolean hasNext() {
    return this.currentEvent != EventType.endDocument;
  }

  public boolean isEvent(final EventType eventType) {
    return this.currentEvent == eventType;
  }

  public boolean isEvent(final EventType... eventTypes) {
    for (final EventType eventType : eventTypes) {
      if (this.currentEvent == eventType) {
        return true;
      }
    }
    return false;
  }

  private void moveNext() {
    this.nextValue = null;
    try {
      skipWhitespace();
      switch (this.currentCharacter) {
        case ',':
          this.nextEvent = EventType.comma;
          this.currentCharacter = this.reader.read();
        break;
        case ':':
          this.nextEvent = EventType.colon;
          this.currentCharacter = this.reader.read();
        break;
        case '{':
          this.nextEvent = EventType.startObject;
          this.currentCharacter = this.reader.read();
          this.depth++;
        break;
        case '}':
          this.nextEvent = EventType.endObject;
          this.currentCharacter = this.reader.read();
          this.depth--;
        break;
        case '[':
          this.nextEvent = EventType.startArray;
          this.currentCharacter = this.reader.read();
        break;
        case ']':
          this.nextEvent = EventType.endArray;
          this.currentCharacter = this.reader.read();
        break;
        case 't':
          for (int i = 0; i < 3; i++) {
            this.currentCharacter = this.reader.read();
          }
          this.nextEvent = EventType.booleanValue;
          this.nextValue = Boolean.TRUE;
          this.currentCharacter = this.reader.read();
        break;
        case 'f':
          for (int i = 0; i < 4; i++) {
            this.currentCharacter = this.reader.read();
          }
          this.nextEvent = EventType.booleanValue;
          this.nextValue = Boolean.FALSE;
          this.currentCharacter = this.reader.read();
        break;
        case 'n':
          for (int i = 0; i < 3; i++) {
            this.currentCharacter = this.reader.read();
          }
          this.nextEvent = EventType.nullValue;
          this.nextValue = null;
          this.currentCharacter = this.reader.read();
        break;
        case '"':
          this.nextEvent = EventType.string;

          processString();
          this.currentCharacter = this.reader.read();
        break;
        case '-':
          this.nextEvent = EventType.number;

          processNumber();
        break;
        case -1:
          this.nextEvent = EventType.endDocument;
        break;
        default:
          if (this.currentCharacter >= '0' && this.currentCharacter <= '9') {
            this.nextEvent = EventType.number;
            processNumber();
          } else {
            this.nextEvent = EventType.unknown;
          }
        break;
      }
    } catch (final IOException e) {
      this.nextEvent = EventType.endDocument;
    }
  }

  @Override
  public EventType next() {
    if (hasNext()) {
      this.currentValue = this.nextValue;
      this.currentEvent = this.nextEvent;
      moveNext();
      return this.currentEvent;
    } else {
      throw new NoSuchElementException("End of JSON");
    }
  }

  private void processNumber() throws IOException {
    final StringBuilder text = new StringBuilder();
    if (this.currentCharacter == '-') {
      text.append((char)this.currentCharacter);
      this.currentCharacter = this.reader.read();
    }
    while (this.currentCharacter >= '0' && this.currentCharacter <= '9') {
      text.append((char)this.currentCharacter);
      this.currentCharacter = this.reader.read();
    }

    if (this.currentCharacter == '.') {
      text.append((char)this.currentCharacter);
      this.currentCharacter = this.reader.read();
      while (this.currentCharacter >= '0' && this.currentCharacter <= '9') {
        text.append((char)this.currentCharacter);
        this.currentCharacter = this.reader.read();
      }
    }

    if (this.currentCharacter == 'e' || this.currentCharacter == 'E') {
      text.append((char)this.currentCharacter);
      this.currentCharacter = this.reader.read();
      if (this.currentCharacter == '-' || this.currentCharacter == '+') {
        text.append((char)this.currentCharacter);
        this.currentCharacter = this.reader.read();
      }
      while (this.currentCharacter >= '0' && this.currentCharacter <= '9') {
        text.append((char)this.currentCharacter);
        this.currentCharacter = this.reader.read();
      }
    }
    this.nextValue = new BigDecimal(text.toString());
  }

  private void processString() throws IOException {
    final StringBuilder text = new StringBuilder();
    this.currentCharacter = this.reader.read();
    while (this.currentCharacter != '"' && this.currentCharacter != -1) {
      if (this.currentCharacter == '\\') {
        this.currentCharacter = this.reader.read();
        switch (this.currentCharacter) {
          case -1:
          break;
          case 'b':
            text.setLength(text.length() - 1);
          break;
          case '"':
            text.append('"');
          break;
          case '/':
            text.append('/');
          break;
          case '\\':
            text.append('\\');
          break;
          case 'f':
            text.append('\f');
          case 'n':
            text.append('\n');
          break;
          case 'r':
            text.append('\r');
          break;
          case 't':
            text.append('\t');
          break;
          case 'u':
            final char[] buf = new char[4];
            final int readCount = this.reader.read(buf);
            final String unicodeText = String.valueOf(buf, 0, readCount);
            if (readCount == 4) {
              try {
                final int unicode = Integer.parseInt(unicodeText, 16);
                text.append((char)unicode);
              } catch (final NumberFormatException e) {
                throw e;
              }
            } else {
              throw new IllegalStateException("Unicode escape not correct " + unicodeText);
            }
          break;
          default:
            throw new IllegalStateException(
              "Invalid escape character: \\" + (char)this.currentCharacter);
        }
      } else {
        text.append((char)this.currentCharacter);
      }
      this.currentCharacter = this.reader.read();
    }
    this.nextValue = text.toString();
  }

  @Override
  public void remove() {
  }

  /** Skip to next attribute in any object.*/
  public String skipToAttribute() {
    while (hasNext()) {
      final EventType eventType = next();
      if (eventType == EventType.string) {
        final String key = getStringIntern();
        if (hasNext() && next() == EventType.colon) {
          return key;
        }
      }
    }
    return null;
  }

  /**
   * Skip through the document until the specified object attribute name is
   * found.
   *
   * @param parser The parser.
   * @param fieldName The name of the attribute to skip through.
   */
  public boolean skipToAttribute(final String fieldName) {
    while (hasNext()) {
      final EventType eventType = next();
      if (eventType == EventType.string) {
        final String key = getStringIntern();
        if (key.equals(fieldName)) {
          if (hasNext() && next() == EventType.colon) {
            if (hasNext()) {
              next();
              return true;
            } else {
              return false;
            }
          }
        }
      } else if (eventType == EventType.unknown) {
        return false;
      }
    }
    return false;
  }

  /** Skip to next attribute in the same object.*/
  public String skipToNextAttribute() {
    int objectCount = 0;
    while (hasNext()) {
      final EventType eventType = next();
      if (objectCount == 0 && eventType == EventType.string) {
        final String key = getStringIntern();
        if (hasNext() && next() == EventType.colon) {
          return key;
        }
      } else if (eventType == EventType.startObject) {
        objectCount++;
      } else if (eventType == EventType.endObject) {
        if (objectCount == 0) {
          return null;
        } else {
          objectCount--;
        }
      }
    }
    return null;
  }

  public boolean skipToNextObjectInArray() {
    if (isEvent(EventType.startArray, EventType.comma)) {
      if (hasNext()) {
        final EventType eventType = next();
        if (eventType == EventType.startObject) {
          return true;
        } else if (eventType == EventType.endArray) {
          return false;
        } else {
          throw new IllegalArgumentException("Unexpected element: " + this);
        }
      }
    }
    return false;
  }

  private void skipWhitespace() throws IOException {
    while (Character.isWhitespace(this.currentCharacter)) {
      this.currentCharacter = this.reader.read();
    }
  }

  @Override
  public String toString() {
    return this.currentEvent + " : " + this.currentValue + " "
      + Character.toString((char)this.currentCharacter) + FileUtil.getString(this.reader, 80);
  }
}
