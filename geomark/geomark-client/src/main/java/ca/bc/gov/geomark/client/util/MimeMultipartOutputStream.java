package ca.bc.gov.geomark.client.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

public class MimeMultipartOutputStream implements Closeable {

  private static final byte[] DASH = {
    '-', '-'
  };

  private static final byte[] CRLF = {
    13, 10
  };

  private final OutputStream out;

  private final byte[] boundaryBytes;

  public MimeMultipartOutputStream(final OutputStream out, final String boundary) {
    this.out = out;
    this.boundaryBytes = boundary.getBytes();
  }

  @Override
  public void close() {
    try {
      this.out.write(DASH);
      this.out.write(this.boundaryBytes);
      this.out.write(DASH);
      this.out.write(CRLF);
      this.out.flush();
      this.out.close();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void write(final Object value) throws IOException {
    write(value.toString());
  }

  private void write(final String string) throws IOException {
    this.out.write(string.getBytes("UTF-8"));
  }

  private void writePart(final String name, final Object value) {
    if (value instanceof InputStream) {
      final InputStream in = (InputStream)value;
      writePart("application/octet-stream", name, in);
    } else {
      writePart("text/plain", name, value);
    }
  }

  public void writePart(final String mediaType, final String name, final InputStream in) {
    try {
      writePartHeader(mediaType, name, "upload");
      final byte[] buffer = new byte[4906];
      int count;
      while ((count = in.read(buffer)) > -1) {
        this.out.write(buffer, 0, count);
      }

      writePartFooter();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    } finally {
      try {
        in.close();
      } catch (final IOException e) {
      }
    }
  }

  public void writePart(final String mediaType, final String name, final Object value) {
    if (value != null) {
      try {
        writePartHeader(mediaType, name, null);
        write(value);
        writePartFooter();
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void writePartFooter() throws IOException {
    this.out.write(CRLF);
  }

  private void writePartHeader(final String mediaType, final String name, final String filename)
    throws IOException {
    this.out.write(DASH);
    this.out.write(this.boundaryBytes);
    this.out.write(CRLF);
    write("Content-Disposition: form-data; name=\"");
    write(name);
    write("\"");
    if (filename != null) {
      write("; filename=\"");
      write(filename);
      write("\"");
      this.out.write(CRLF);
      write("Content-Transfer-Encoding: binary");
    }
    this.out.write(CRLF);
    write("Content-Type: ");
    write(mediaType);
    if (filename == null) {
      write("; charset=UTF-8");
    }
    this.out.write(CRLF);
    this.out.write(CRLF);
  }

  public void writeParts(final Map<String, Object> parameters) {
    for (final Entry<String, Object> parameter : parameters.entrySet()) {
      final String name = parameter.getKey();
      final Object value = parameter.getValue();
      if (value instanceof Collection) {
        @SuppressWarnings("rawtypes")
        final Collection collection = (Collection)value;
        for (final Object object : collection) {
          writePart(name, object);
        }
      } else {
        writePart(name, value);
      }
    }
  }
}
