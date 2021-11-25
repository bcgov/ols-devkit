package com.revolsys.record.io.format.json;

import java.io.IOException;
import java.io.Writer;

import com.revolsys.io.FileUtil;

public class JsonStringEncodingWriter extends Writer {

  private Writer out;

  public JsonStringEncodingWriter(final Writer out) {
    this.out = out;
  }

  @Override
  public void close() {
    FileUtil.closeSilent(this.out);
    this.out = null;
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

  @Override
  public void write(final char[] chars) throws IOException {
    final int length = chars.length;
    write(chars, 0, length);
  }

  @Override
  public void write(final char[] chars, int startIndex, final int length) throws IOException {
    final Writer out = this.out;
    int count = 0;
    final int endIndex = startIndex + length;
    for (int i = 0; i < endIndex; i++) {
      final char c = chars[i];
      if (c < JsonWriterUtil.CHARACTER_ESCAPE_END) {
        out.write(chars, startIndex, count);
        final String escape = JsonWriterUtil.CHARACTER_ESCAPE[c];
        out.write(escape);
        startIndex = i + 1;
        count = 0;
      } else if (c == '"') {
        out.write(chars, startIndex, count);
        out.write('\\');
        out.write('"');
        startIndex = i + 1;
        count = 0;
      } else if (c == '\\') {
        out.write(chars, startIndex, count);
        out.write('\\');
        out.write('\\');
        startIndex = i + 1;
        count = 0;
      } else {
        if (count == 1024) {
          out.write(chars, startIndex, count);
          startIndex = i;
          count = 0;
        }
        count++;
      }
    }
    out.write(chars, startIndex, count);
  }

  @Override
  public void write(final int c) throws IOException {
    final Writer out = this.out;
    if (c < JsonWriterUtil.CHARACTER_ESCAPE_END) {
      out.write(JsonWriterUtil.CHARACTER_ESCAPE[c]);
    } else if (c == '"') {
      out.write("\\\"");
    } else if (c == '\\') {
      out.write("\\\\");
    } else {
      out.write(c);
    }
  }

  @Override
  public void write(final String string) throws IOException {
    final int length = string.length();
    write(string, 0, length);
  }

  @Override
  public void write(final String string, int startIndex, final int length) throws IOException {
    final Writer out = this.out;
    int count = 0;
    final int endIndex = startIndex + length;
    for (int i = 0; i < endIndex; i++) {
      final char c = string.charAt(i);
      if (c < JsonWriterUtil.CHARACTER_ESCAPE_END) {
        out.write(string, startIndex, count);
        final String escape = JsonWriterUtil.CHARACTER_ESCAPE[c];
        out.write(escape);
        startIndex = i + 1;
        count = 0;
      } else if (c == '"') {
        out.write(string, startIndex, count);
        out.write('\\');
        out.write('"');
        startIndex = i + 1;
        count = 0;
      } else if (c == '\\') {
        out.write(string, startIndex, count);
        out.write('\\');
        out.write('\\');
        startIndex = i + 1;
        count = 0;
      } else {
        if (count == 1024) {
          out.write(string, startIndex, count);
          startIndex = i;
          count = 0;
        }
        count++;
      }
    }
    out.write(string, startIndex, count);
  }
}
