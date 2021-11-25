package org.jeometry.common.io;

import java.io.File;

public interface FileProxy {

  File getFile();

  default File getOrDownloadFile() {
    return getFile();
  }
}
