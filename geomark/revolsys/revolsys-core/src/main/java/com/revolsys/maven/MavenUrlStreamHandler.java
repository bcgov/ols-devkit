package com.revolsys.maven;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import com.revolsys.spring.resource.Resource;

public class MavenUrlStreamHandler extends URLStreamHandler {

  private final MavenRepository mavenRepository;

  public MavenUrlStreamHandler(final MavenRepository mavenRepository) {
    this.mavenRepository = mavenRepository;
  }

  @Override
  protected URLConnection openConnection(final URL url) throws IOException {
    final String protocol = url.getProtocol();
    if (protocol.equals("jar")) {
      final String file = url.getFile();
      int separator = file.indexOf("!/");
      if (separator == -1) {
        throw new MalformedURLException("no !/ found in url spec:" + file);
      } else {

        final String subUrl = file.substring(0, separator++);
        if (subUrl.startsWith("mvn")) {
          final String mavenId = subUrl.substring(4);
          final Resource resource = this.mavenRepository.getResource(mavenId);
          final URL resourceUrl = resource.getURL();

          String entryName = "/";

          if (++separator != file.length()) {
            entryName = file.substring(separator - 1, file.length());
          }
          final String jarUrl = "jar:" + resourceUrl + "!" + entryName;
          return new URL(jarUrl).openConnection();
        }
      }
    } else if (protocol.equals("mvn")) {
      final String mavenId = url.getFile();
      final Resource resource = this.mavenRepository.getResource(mavenId);
      final URL resourceUrl = resource.getURL();
      return resourceUrl.openConnection();
    }
    return new URL(url.toString()).openConnection();
  }
}
