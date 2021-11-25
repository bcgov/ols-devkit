/*
 * Copyright (c) 1996, 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.revolsys.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Lowercase version of buffered writer.
 *
 * @see BufferedWriter
 * @see OutputStreamWriter
 * @see java.nio.file.Files#newBufferedWriter
 */

public class LowercaseWriter extends Writer {

  private static int DEFAULT_BUFFER_SIZE = 8192;

  private Writer out;

  private char cb[];

  private final int nChars;

  private int nextChar;

  public LowercaseWriter(final OutputStream out) {
    this(new OutputStreamWriter(out));
  }

  /**
   * Creates a buffered character-output stream that uses a default-sized
   * output buffer.
   *
   * @param  out  A Writer
   */
  public LowercaseWriter(final Writer out) {
    this(out, DEFAULT_BUFFER_SIZE);
  }

  /**
   * Creates a new buffered character-output stream that uses an output
   * buffer of the given size.
   *
   * @param  out  A Writer
   * @param  sz   Output-buffer size, a positive integer
   *
   * @exception  IllegalArgumentException  If {@code sz <= 0}
   */
  public LowercaseWriter(final Writer out, final int sz) {
    super(out);
    if (sz <= 0) {
      throw new IllegalArgumentException("Buffer size <= 0");
    }
    this.out = out;
    this.cb = new char[sz];
    this.nChars = sz;
    this.nextChar = 0;
  }

  @Override
  public void close() throws IOException {
    synchronized (this.lock) {
      if (this.out == null) {
        return;
      }
      try (
        Writer w = this.out) {
        flushBuffer();
      } finally {
        this.out = null;
        this.cb = null;
      }
    }
  }

  /** Checks to make sure that the stream has not been closed */
  private void ensureOpen() throws IOException {
    if (this.out == null) {
      throw new IOException("Stream closed");
    }
  }

  /**
   * Flushes the stream.
   *
   * @exception  IOException  If an I/O error occurs
   */
  @Override
  public void flush() throws IOException {
    synchronized (this.lock) {
      flushBuffer();
      this.out.flush();
    }
  }

  /**
   * Flushes the output buffer to the underlying character stream, without
   * flushing the stream itself.  This method is non-private only so that it
   * may be invoked by PrintStream.
   */
  void flushBuffer() throws IOException {
    synchronized (this.lock) {
      ensureOpen();
      final int charCount = this.nextChar;
      if (charCount > 0) {
        final char[] chars = this.cb;
        for (int i = 0; i < charCount; i++) {
          chars[i] = Character.toLowerCase(chars[i]);
        }
        this.out.write(chars, 0, charCount);
        this.nextChar = 0;
      }
    }
  }

  /**
   * Our own little min method, to avoid loading java.lang.Math if we've run
   * out of file descriptors and we're trying to print a stack trace.
   */
  private int min(final int a, final int b) {
    if (a < b) {
      return a;
    }
    return b;
  }

  /**
   * Writes a line separator.  The line separator string is defined by the
   * system property {@code line.separator}, and is not necessarily a single
   * newline ('\n') character.
   *
   * @exception  IOException  If an I/O error occurs
   */
  public void newLine() throws IOException {
    write(System.lineSeparator());
  }

  /**
   * Writes a portion of an array of characters.
   *
   * <p> Ordinarily this method stores characters from the given array into
   * this stream's buffer, flushing the buffer to the underlying stream as
   * needed.  If the requested length is at least as large as the buffer,
   * however, then this method will flush the buffer and write the characters
   * directly to the underlying stream.  Thus redundant
   * {@code BufferedWriter}s will not copy data unnecessarily.
   *
   * @param  cbuf  A character array
   * @param  off   Offset from which to start reading characters
   * @param  len   Number of characters to write
   *
   * @throws  IndexOutOfBoundsException
   *          If {@code off} is negative, or {@code len} is negative,
   *          or {@code off + len} is negative or greater than the length
   *          of the given array
   *
   * @throws  IOException  If an I/O error occurs
   */
  @Override
  public void write(final char cbuf[], final int off, final int len) throws IOException {
    synchronized (this.lock) {
      ensureOpen();
      if (off < 0 || off > cbuf.length || len < 0 || off + len > cbuf.length || off + len < 0) {
        throw new IndexOutOfBoundsException();
      } else if (len == 0) {
        return;
      }

      if (len >= this.nChars) {
        /*
         * If the request length exceeds the size of the output buffer, flush
         * the buffer and then write the data directly. In this way buffered
         * streams will cascade harmlessly.
         */
        flushBuffer();
        this.out.write(cbuf, off, len);
        return;
      }

      int b = off;
      final int t = off + len;
      while (b < t) {
        final int d = min(this.nChars - this.nextChar, t - b);
        System.arraycopy(cbuf, b, this.cb, this.nextChar, d);
        b += d;
        this.nextChar += d;
        if (this.nextChar >= this.nChars) {
          flushBuffer();
        }
      }
    }
  }

  /**
   * Writes a single character.
   *
   * @exception  IOException  If an I/O error occurs
   */
  @Override
  public void write(final int c) throws IOException {
    synchronized (this.lock) {
      ensureOpen();
      if (this.nextChar >= this.nChars) {
        flushBuffer();
      }
      this.cb[this.nextChar++] = (char)c;
    }
  }

  /**
   * Writes a portion of a String.
   *
   * @implSpec
   * While the specification of this method in the
   * {@linkplain java.io.Writer#write(java.lang.String,int,int) superclass}
   * recommends that an {@link IndexOutOfBoundsException} be thrown
   * if {@code len} is negative or {@code off + len} is negative,
   * the implementation in this class does not throw such an exception in
   * these cases but instead simply writes no characters.
   *
   * @param  s     String to be written
   * @param  off   Offset from which to start reading characters
   * @param  len   Number of characters to be written
   *
   * @throws  IndexOutOfBoundsException
   *          If {@code off} is negative,
   *          or {@code off + len} is greater than the length
   *          of the given string
   *
   * @throws  IOException  If an I/O error occurs
   */
  @Override
  public void write(final String s, final int off, final int len) throws IOException {
    synchronized (this.lock) {
      ensureOpen();

      int b = off;
      final int t = off + len;
      while (b < t) {
        final int d = min(this.nChars - this.nextChar, t - b);
        s.getChars(b, b + d, this.cb, this.nextChar);
        b += d;
        this.nextChar += d;
        if (this.nextChar >= this.nChars) {
          flushBuffer();
        }
      }
    }
  }
}
