package com.revolsys.net.urlcache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CacheResponse;
import java.util.List;
import java.util.Map;

public class FileCacheResponse extends CacheResponse {

  private final File file;

  private final Map<String, List<String>> headers;

  public FileCacheResponse(final File file, final Map<String, List<String>> headers) {
    this.file = file;
    this.headers = headers;
  }

  @Override
  public InputStream getBody() throws IOException {
    return new FileInputStream(this.file);
  }

  @Override
  public Map<String, List<String>> getHeaders() throws IOException {
    return this.headers;
  }

}
