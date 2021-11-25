package com.revolsys.net.protocol.folderconnection;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import com.revolsys.io.FileUtil;

public class FolderConnectionURLStreamHandler extends URLStreamHandler {

  public static final FolderConnectionURLStreamHandler INSTANCE = new FolderConnectionURLStreamHandler();

  @Override
  protected URLConnection openConnection(final URL url) throws IOException {
    final File file = FileUtil.getFile(url);
    if (file == null) {
      return null;
    } else {
      final URL fileUrl = FileUtil.toUrl(file);
      return fileUrl.openConnection();
    }
  }
}
