package org.jeometry.common.net;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public interface UrlProxy {

  default URI getUri() {
    final URL url = getUrl();
    try {
      return url.toURI();
    } catch (final URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  URL getUrl();

  default URL getUrl(final Path path) {
    String childPath;
    if (path.getNameCount() == 0) {
      final String fileName = path.toString();
      if (fileName.endsWith("\\") || fileName.endsWith("\\")) {
        childPath = fileName.substring(0, fileName.length() - 1);
      } else {
        childPath = fileName;
      }
    } else {
      final Path fileNamePath = path.getFileName();
      final String fileName = fileNamePath.toString();
      if (fileName.endsWith("\\") || fileName.endsWith("/")) {
        childPath = fileName.substring(0, fileName.length() - 1);
      } else {
        childPath = fileName;
      }
    }
    if (Files.isDirectory(path)) {
      childPath += "/";
    }
    return getUrl(childPath);
  }

  default URL getUrl(final String child) {
    final URL parentUrl = getUrl();
    if (parentUrl == null) {
      return null;
    } else {
      try {
        // final String encodedChild = percentEncode(child);
        final StringBuilder newUrl = new StringBuilder(parentUrl.toExternalForm());
        final String ref = parentUrl.getRef();
        if (ref != null) {
          newUrl.setLength(newUrl.length() - ref.length() - 1);
        }
        final String query = parentUrl.getQuery();
        if (query != null) {
          newUrl.setLength(newUrl.length() - query.length() - 1);
        }
        if (newUrl.charAt(newUrl.length() - 1) != '/') {
          newUrl.append('/');
        }
        newUrl.append(child);
        if (query != null) {
          newUrl.append('?');
          newUrl.append(query);
        }
        if (ref != null) {
          newUrl.append('#');
          newUrl.append(ref);
        }
        return new URL(newUrl.toString());
      } catch (final MalformedURLException e) {
        throw new IllegalArgumentException(
          "Cannot create child URL for " + parentUrl + " + " + child);
      }
    }
  }
}
