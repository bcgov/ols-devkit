package com.revolsys.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LazyHttpPostOutputStream extends OutputStream {
  private HttpURLConnection connection;

  private final String contentType;

  private InputStream in;

  private OutputStream out;

  private final String url;

  public LazyHttpPostOutputStream(final String url, final String contentType) {
    this.url = url;
    this.contentType = contentType;
  }

  @Override
  public void close() throws IOException {
    this.out.flush();
    this.out.close();
    this.in = this.connection.getInputStream();
    if (this.connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
      throw new RuntimeException("Result data not accepted by server "
        + this.connection.getResponseCode() + " " + this.connection.getResponseMessage());
    }
    this.in.close();
  }

  private void init() throws IOException {
    this.connection = (HttpURLConnection)new URL(this.url).openConnection();
    this.connection.setRequestMethod("POST");
    this.connection.setRequestProperty("Content-Type", this.contentType);
    this.connection.setChunkedStreamingMode(4096);
    this.connection.setDoOutput(true);
    this.connection.setDoInput(true);
    this.out = this.connection.getOutputStream();

  }

  @Override
  public void write(final byte[] b) throws IOException {
    if (this.out == null) {
      init();
    }
    this.out.write(b);
  }

  @Override
  public void write(final byte[] b, final int off, final int len) throws IOException {
    if (this.out == null) {
      init();
    }
    this.out.write(b, off, len);
  }

  @Override
  public void write(final int b) throws IOException {
    if (this.out == null) {
      init();
    }
    this.out.write(b);
  }
}
