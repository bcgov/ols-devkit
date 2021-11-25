package com.revolsys.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

public class ManifestUtil {

  public static String getImplementationVersion(final String implementationTitle) {
    return getMainAttributeByImplementationTitle(implementationTitle, Name.IMPLEMENTATION_VERSION,
      "0.0.0");
  }

  public static String getMainAttributeByImplementationTitle(final String implementationTitle,
    final Name name, final String defaultValue) {
    final Manifest manifest = getManifestByImplementationTitle(implementationTitle);
    if (manifest != null) {
      return manifest.getMainAttributes().getValue(name);
    } else {
      return defaultValue;
    }
  }

  public static String getMainAttributeByImplementationTitle(final String implementationTitle,
    final String name, final String defaultValue) {
    final Manifest manifest = getManifestByImplementationTitle(implementationTitle);
    if (manifest != null) {
      return manifest.getMainAttributes().getValue(name);
    } else {
      return defaultValue;
    }
  }

  public static Manifest getManifestByImplementationTitle(final String implementationTitle) {
    try {
      final Enumeration<URL> resources = Thread.currentThread()
        .getContextClassLoader()
        .getResources("META-INF/MANIFEST.MF");
      while (resources.hasMoreElements()) {
        final URL url = resources.nextElement();

        try (
          final InputStream in = url.openStream()) {
          final Manifest manifest = new Manifest(in);
          final Attributes attrs = manifest.getMainAttributes();
          final String title = attrs.getValue(Attributes.Name.IMPLEMENTATION_TITLE);
          if (implementationTitle.equals(title)) {
            return manifest;
          }
        }
      }
    } catch (final IOException e) {
    }
    return null;
  }

  public static String getScmCommit(final String implementationTitle) {
    return getMainAttributeByImplementationTitle(implementationTitle, new Name("SCM-Commit"),
      "HEAD");
  }
}
