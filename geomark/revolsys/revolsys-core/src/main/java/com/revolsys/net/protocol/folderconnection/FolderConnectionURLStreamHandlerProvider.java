package com.revolsys.net.protocol.folderconnection;

import java.net.URLStreamHandler;
import java.net.spi.URLStreamHandlerProvider;

public class FolderConnectionURLStreamHandlerProvider extends URLStreamHandlerProvider {

  @Override
  public URLStreamHandler createURLStreamHandler(final String protocol) {
    if ("folderconnection".equals(protocol)) {
      return FolderConnectionURLStreamHandler.INSTANCE;
    } else {
      return null;
    }
  }

}
