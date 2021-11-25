package ca.bc.gov.geomark.client.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class JsonParser implements Iterator<JsonParser.EventType> {
  public enum EventType {
    booleanValue, label, comma, endArray, endDocument, endObject, nullValue, number, startArray, startDocument, startObject, string, unknown
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

  private int currentCharacter;

  private EventType currentEvent = EventType.startDocument;

  private Object currentValue;

  private int depth;

  private EventType nextEvent = EventType.startDocument;

  private Object nextValue;

  private final Reader reader;

  public JsonParser(final Reader reader) {
    this.reader = new BufferedReader(reader, 10000);
    try {
      this.currentCharacter = this.reader.read();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void close() {
    try {
      this.reader.close();
    } catch (final IOException e) {
    }
  }

  public List<Object> getArray() {
    if (getEvent() == EventType.startArray || hasNext() && next() == EventType.startArray) {
      EventType event = getEvent();
      final List<Object> list = new ArrayList<>();
      do {
        final Object value = getValue();
        if (value instanceof EventType) {
          event = (EventType)value;
          if (event == EventType.comma) {
            throw new IllegalStateException("Missing value before ',' " + event);
          } else if (event == EventType.endArray) {
            if (!list.isEmpty()) {
              throw new IllegalStateException("Missing value after ',' and before ']' " + event);
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

  public EventType getEvent() {
    return this.currentEvent;
  }

  public String getLabel() {
    if (getEvent() == EventType.label || hasNext() && next() == EventType.label) {
      if (this.currentValue == null) {
        return null;
      } else {
        return ((String)this.currentValue).intern();
      }
    } else {
      throw new IllegalStateException("Expecting a label");
    }
  }

  public Map<String, Object> getMap() {
    if (getEvent() == EventType.startObject || hasNext() && next() == EventType.startObject) {
      EventType event = getEvent();
      final Map<String, Object> object = new LinkedHashMap<>();
      try {
        do {
          if (hasNext() && next() == EventType.label) {
            final String key = getLabel();
            if (hasNext()) {
              final Object value = getValue();
              if (value instanceof EventType) {
                throw new IllegalStateException("Exepecting a value, not: " + key + "=" + value);
              }
              if (key != null) {
                object.put(key, value);
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
        throw new IllegalStateException("Unexpected end of JSON" + object, e);
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
    return this.currentEvent != EventType.endDocument && this.currentEvent != EventType.unknown;
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
          skipWhitespace();
          if (this.currentCharacter == ':') {
            this.nextEvent = EventType.label;
            this.currentCharacter = this.reader.read();
          }
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

  public String skipToAttribute() {
    while (hasNext()) {
      final EventType eventType = next();
      if (eventType == EventType.label) {
        return getLabel();
      }
    }
    return null;
  }

  public boolean skipToAttribute(final String fieldName) {
    while (hasNext()) {
      final EventType eventType = next();
      if (eventType == EventType.label) {
        final String label = getLabel();
        if (label.equals(fieldName)) {
          if (hasNext()) {
            next();
            return true;
          } else {
            return false;
          }
        }
      } else if (eventType == EventType.unknown) {
        return false;
      }
    }
    return false;
  }

  public String skipToNextAttribute() {
    int objectCount = 0;
    while (hasNext()) {
      final EventType eventType = next();
      if (objectCount == 0 && eventType == EventType.label) {
        return getLabel();
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
      + Character.toString((char)this.currentCharacter);
  }
}
