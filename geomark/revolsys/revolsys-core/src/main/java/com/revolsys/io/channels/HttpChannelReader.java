package com.revolsys.io.channels;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.jeometry.common.exception.Exceptions;

public class HttpChannelReader extends ChannelReader {

  private final URL url;

  public HttpChannelReader(final URL url) {
    super(new HttpSeekableByteChannel(url));
    this.url = url;
  }

  @Override
  public InputStream getInputStream(final long offset, final int size) {
    try {
      final URLConnection connection = this.url.openConnection();
      final String range = "bytes=" + offset + "-" + (offset + size);
      connection.addRequestProperty("Range", range);
      return connection.getInputStream();
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }
}
