package com.revolsys.net.urlcache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.CacheRequest;

import com.revolsys.io.FileUtil;

public class FileCacheRequest extends CacheRequest {

  private final File file;

  private final FileOutputStream out;

  public FileCacheRequest(final File file) throws FileNotFoundException {
    this.file = file;
    file.getParentFile().mkdirs();
    this.out = new FileOutputStream(file);
  }

  @Override
  public void abort() {
    FileUtil.closeSilent(this.out);
    this.file.delete();
  }

  @Override
  public OutputStream getBody() throws IOException {
    return this.out;
  }

}
