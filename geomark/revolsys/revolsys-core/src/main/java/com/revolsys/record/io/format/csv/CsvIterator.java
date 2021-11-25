package com.revolsys.record.io.format.csv;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.FileUtil;

public class CsvIterator implements Iterator<List<String>>, Iterable<List<String>> {
  private static final int BUFFER_SIZE = 8192;

  private final char fieldSeparator;

  private final char[] buffer = new char[BUFFER_SIZE];

  /** The current record. */
  private List<String> currentRecord;

  /** Flag indicating if there are more records to be read. */
  private boolean hasNext = true;

  /** The reader to */
  private final Reader in;

  private int index = 0;

  private int readCount;

  private final StringBuilder sb = new StringBuilder();

  /**
   * Constructs CSVReader with supplied separator and quote char.
   *
   * @param reader The reader to the CSV file.
   * @throws IOException
   */
  public CsvIterator(final Reader in) {
    this(in, Csv.FIELD_SEPARATOR);
  }

  public CsvIterator(final Reader in, final char fieldSeparator) {
    this.in = in;
    this.fieldSeparator = fieldSeparator;
    readNextRecord();
  }

  /**
   * Closes the underlying reader.
   *
   * @throws IOException if the close fails
   */
  public void close() {
    FileUtil.closeSilent(this.in);
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

  @Override
  public Iterator<List<String>> iterator() {
    return this;
  }

  /**
   * Return the next record from the iterator.
   *
   * @return The record
   */
  @Override
  public List<String> next() {
    if (!this.hasNext) {
      throw new NoSuchElementException("No more elements");
    } else {
      final List<String> object = this.currentRecord;
      readNextRecord();
      return object;
    }
  }

  private List<String> parseRecord() throws IOException {
    final StringBuilder sb = this.sb;
    final Reader in = this.in;
    sb.delete(0, sb.length());
    final List<String> fields = new ArrayList<>();
    boolean inQuotes = false;
    boolean hadQuotes = false;
    while (this.readCount != -1) {
      if (this.index >= this.readCount) {
        this.index = 0;
        this.readCount = in.read(this.buffer, 0, BUFFER_SIZE);
        if (this.readCount < 0) {
          if (fields.isEmpty()) {
            this.hasNext = false;
            return null;
          } else {
            return fields;
          }
        }
      }
      final char c = this.buffer[this.index++];
      switch (c) {
        case '"':
          hadQuotes = true;
          final char nextChar = previewNextChar();
          if (inQuotes && nextChar == '"') {
            sb.append('"');
            this.index++;
          } else {
            inQuotes = !inQuotes;
            if (sb.length() > 0 && !(nextChar == this.fieldSeparator || nextChar == '\r'
              || nextChar == '\n' || nextChar == 0)) {
              sb.append(c);
            }
          }
        break;
        case '\r':
          if (inQuotes) {
            sb.append(c);
          } else if (previewNextChar() == '\n') {
          } else {
            if (hadQuotes || sb.length() > 0) {
              fields.add(sb.toString());
              sb.delete(0, sb.length());
            } else {
              fields.add(null);
            }
            return fields;
          }
        break;
        case '\n':
          if (previewNextChar() == '\r') {
            this.index++;
          }
          if (inQuotes) {
            sb.append(c);
          } else {
            if (hadQuotes || sb.length() > 0) {
              fields.add(sb.toString());
              sb.delete(0, sb.length());
            } else {
              fields.add(null);
            }
            return fields;
          }
        break;
        case 65279: // Byte Order Mark
        break;
        default:
          if (c == this.fieldSeparator) {
            if (inQuotes) {
              sb.append(c);
            } else {
              if (hadQuotes || sb.length() > 0) {
                fields.add(sb.toString());
                sb.delete(0, sb.length());
              } else {
                fields.add(null);
              }
              hadQuotes = false;
            }
          } else {
            sb.append(c);
          }
        break;
      }
    }
    this.hasNext = false;
    return null;
  }

  private char previewNextChar() throws IOException {
    if (this.index >= this.readCount) {
      this.index = 0;
      this.readCount = this.in.read(this.buffer, 0, BUFFER_SIZE);
      if (this.readCount < 0) {
        return 0;
      }
    }
    return this.buffer[this.index];
  }

  /**
   * Reads the next line from the buffer and converts to a string array.
   *
   * @return a string array with each comma-separated element as a separate
   *         entry.
   * @throws IOException if bad things happen during the read
   */
  private List<String> readNextRecord() {
    if (this.hasNext) {
      try {
        this.currentRecord = parseRecord();
      } catch (final IOException e) {
        Exceptions.throwUncheckedException(e);
      }
      return this.currentRecord;
    } else {
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
