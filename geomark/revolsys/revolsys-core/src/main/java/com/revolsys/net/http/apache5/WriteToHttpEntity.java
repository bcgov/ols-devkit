package com.revolsys.net.http.apache5;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;

public class WriteToHttpEntity extends AbstractHttpEntity {
  public static interface WriteTo {
    void writeTo(OutputStream out) throws IOException;
  }

  private long contentLength = -1;

  private final WriteTo action;

  public WriteToHttpEntity(final ContentType contentType, final String contentEncoding,
    final WriteTo action) {
    super(contentType, contentEncoding);
    this.action = action;
  }

  @Override
  public void close() throws IOException {
  }

  @Override
  public InputStream getContent() throws IOException, UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getContentLength() {
    return this.contentLength;
  }

  @Override
  public boolean isRepeatable() {
    return false;
  }

  @Override
  public boolean isStreaming() {
    return false;
  }

  public void setContentLength(final long contentLength) {
    this.contentLength = contentLength;
  }

  @Override
  public void writeTo(final OutputStream outStream) throws IOException {
    this.action.writeTo(outStream);
  }

}
