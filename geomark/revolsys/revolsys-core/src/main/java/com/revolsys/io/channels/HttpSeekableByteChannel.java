package com.revolsys.io.channels;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;

public class HttpSeekableByteChannel implements SeekableByteChannel {

  private final URL url;

  private ReadableByteChannel channel;

  private long position;

  private long length;

  public HttpSeekableByteChannel(final URL url) {
    this.url = url;
  }

  @Override
  public void close() throws IOException {
    this.channel.close();
    this.channel = null;
  }

  private ReadableByteChannel getChannel() throws IOException {
    if (this.channel == null) {
      final URLConnection connection = this.url.openConnection();
      if (this.position > 0) {
        final String range = "bytes=" + this.position + "-";
        connection.addRequestProperty("Range", range);
      }
      this.channel = Channels.newChannel(connection.getInputStream());
      final String contentRange = connection.getHeaderField("Content-Range");
      if (contentRange == null) {
        final String contentLength = connection.getHeaderField("Content-Length");
        this.length = Long.parseLong(contentLength);
      } else {
        this.length = Long.parseLong(contentRange.substring(contentRange.indexOf('/') + 1));
      }
    }
    return this.channel;
  }

  @Override
  public boolean isOpen() {
    return this.channel != null && this.channel.isOpen();
  }

  @Override
  public long position() throws IOException {
    return this.position;
  }

  @Override
  public SeekableByteChannel position(final long position) throws IOException {
    if (position != this.position) {
      if (this.channel != null) {
        this.channel.close();
        this.channel = null;
      }
    }
    this.position = position;
    return this;
  }

  @Override
  public int read(final ByteBuffer buffer) throws IOException {
    final ReadableByteChannel channel = getChannel();
    final int value = channel.read(buffer);
    if (value != -1) {
      this.position += value;
    }
    return value;
  }

  @Override
  public long size() throws IOException {
    return this.length;
  }

  @Override
  public SeekableByteChannel truncate(final long size) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int write(final ByteBuffer buffer) throws IOException {
    throw new UnsupportedOperationException();
  }

}
