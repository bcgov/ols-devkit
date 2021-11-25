package com.revolsys.record.io.format.json;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.record.io.format.json.JsonParser.EventType;

public class JsonMapIterator implements Iterator<JsonObject>, Closeable {
  /** The current record. */
  private JsonObject currentObject;

  /** Flag indicating if there are more records to be read. */
  private boolean hasNext = false;

  private final JsonParser parser;

  public JsonMapIterator(final Reader in) throws IOException {
    this(in, false);
  }

  public JsonMapIterator(final Reader in, final boolean single) throws IOException {
    this.parser = new JsonParser(in);
    if (single) {
      this.hasNext = true;
      readNextRecord();
    } else if (this.parser.hasNext()) {
      EventType event = this.parser.next();
      if (event == EventType.startDocument) {
        if (this.parser.hasNext()) {
          event = this.parser.next();
          if (event == EventType.startObject) {
            this.parser.getString();
            if (this.parser.hasNext()) {
              event = this.parser.next();
              if (event == EventType.colon) {
                if (this.parser.hasNext()) {
                  event = this.parser.next();
                  if (event == EventType.startArray) {
                    this.hasNext = true;
                    readNextRecord();
                  }
                }
              }
            }
          } else if (event == EventType.startArray) {
            this.hasNext = true;
            readNextRecord();
          }
        }
      }
    }
    if (!this.hasNext) {
      close();
    }
  }

  @Override
  public void close() {
    this.parser.close();
  }

  /**
   * Returns <tt>true</tt> if the iteration has more elements.
   *
   * @return <tt>true</tt> if the iterator has more elements.
   */
  @Override
  public boolean hasNext() {
    return this.hasNext;
  }

  /**
   * Return the next record from the iterator.
   *
   * @return The record
   */
  @Override
  public JsonObject next() {
    if (!this.hasNext) {
      throw new NoSuchElementException("No more elements");
    } else {
      final JsonObject object = this.currentObject;
      readNextRecord();
      return object;
    }
  }

  /**
   * Reads the next line from the buffer and converts to a string array.
   *
   * @return a string array with each comma-separated element as a separate
   *         entry.
   * @throws IOException if bad things happen during the read
   */
  private JsonObject readNextRecord() {
    if (this.hasNext && this.parser.hasNext()) {
      final EventType event = this.parser.next();
      if (event == EventType.endArray || event == EventType.endDocument) {
        this.hasNext = false;
        close();
        return null;
      } else {
        this.currentObject = this.parser.getMap();
        return this.currentObject;
      }
    } else {
      this.hasNext = false;
      close();
      return null;
    }
  }

  /**
   * Removing items from the iterator is not supported.
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
