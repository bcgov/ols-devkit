package com.revolsys.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.jeometry.common.logging.Logs;

/**
 * The ZipWriter is a wrapper for another writer which has been configured to
 * write files to the tempDirectory. When this writer closes it will output a
 * zip file of the tempDirectory contents to the output stream and then delete
 * the temporary directory.
 *
 * @author Paul Austin
 * @param <T> The type of data to write.
 */
public class ZipWriter<T> extends DelegatingWriter<T> {

  private final OutputStream out;

  private final File tempDirectory;

  public ZipWriter(final File tempDirectory, final Writer<T> writer, final OutputStream out) {
    super(writer);
    this.tempDirectory = tempDirectory;
    this.out = out;
  }

  @Override
  public void close() {
    try {
      super.close();
    } finally {
      try {
        ZipUtil.zipDirectory(this.tempDirectory, this.out);
      } catch (final IOException e) {
        throw new RuntimeException("Unable to compress files", e);
      } finally {
        if (!FileUtil.deleteDirectory(this.tempDirectory)) {
          Logs.error(this, "Unable to delete:" + this.tempDirectory);
        }
      }
    }
  }
}
