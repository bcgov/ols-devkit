package com.revolsys.net.urlcache;

import java.io.File;
import java.io.IOException;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.ResponseCache;
import java.net.URI;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

import com.revolsys.io.BaseCloseable;
import com.revolsys.io.FileUtil;
import com.revolsys.value.ThreadBooleanValue;

public class FileResponseCache extends ResponseCache {
  private static final ThreadBooleanValue enabled = new ThreadBooleanValue(true);

  public static BaseCloseable disable() {
    return enabled.closeable(false);
  }

  private final File directory;

  public FileResponseCache() {
    this(System.getProperty("java.io.tmpdir"));
  }

  public FileResponseCache(final File directory) {
    if (!directory.exists()) {
      directory.mkdirs();
    }
    this.directory = directory;
  }

  public FileResponseCache(final String directory) {
    this(new File(directory));
  }

  @Override
  public CacheResponse get(final URI uri, final String method,
    final Map<String, List<String>> headers) throws IOException {
    if (enabled.isTrue()) {
      if (headers.isEmpty() && method.equals("GET")) {
        final File file = toFile(uri);
        if (file != null && file.exists()) {
          return new FileCacheResponse(file, headers);
        }
      }
    }
    return null;
  }

  @SuppressWarnings("deprecation")
  @Override
  public CacheRequest put(final URI uri, final URLConnection connection) throws IOException {
    if (enabled.isTrue()) {
      final File file = toFile(uri);
      if (file != null) {
        long lastModified = 0;
        String dateString = connection.getHeaderField("last-modified");
        if (dateString != null) {
          try {
            if (dateString.indexOf("GMT") == -1) {
              dateString = dateString + " GMT";
            }
            lastModified = Date.parse(dateString);
            file.setLastModified(lastModified);
          } catch (final Exception e) {
          }
        }
        return new FileCacheRequest(file);
      }
    }
    return null;
  }

  private File toFile(final URI uri) {
    final String scheme = uri.getScheme();
    if (scheme.equals("http") || scheme.equals("https")) {
      File file = new File(this.directory, scheme);
      final String host = uri.getHost();
      file = new File(file, host);
      final int port = uri.getPort();
      if (port != -1) {
        file = new File(file, String.valueOf(port));
      }
      String extension = null;
      String fileName = null;
      final String path = uri.getPath();
      if (path != null) {
        file = new File(file, path);
        if (!path.endsWith("/")) {
          extension = FileUtil.getFileNameExtension(file);
          if (extension.length() > 0) {
            fileName = FileUtil.getFileNamePrefix(file);
          } else {
            fileName = FileUtil.getFileName(file);
          }
          file = file.getParentFile();
        }
      }
      if (fileName == null) {
        final CRC32 crc32 = new CRC32();
        crc32.update(uri.toString().getBytes());
        fileName = String.valueOf(crc32.getValue());
      }
      final String query = uri.getQuery();
      if (query != null) {
        final CRC32 crc32 = new CRC32();
        crc32.update(query.getBytes());
        fileName += "-q" + crc32.getValue();
      }
      if (extension.length() > 0) {
        fileName = fileName + "." + extension;
      }
      return new File(file, fileName);
    }
    return null;
  }

}
