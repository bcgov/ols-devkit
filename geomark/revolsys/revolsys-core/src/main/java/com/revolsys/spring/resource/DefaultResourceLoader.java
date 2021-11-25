package com.revolsys.spring.resource;

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.core.io.ContextResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

public class DefaultResourceLoader implements ResourceLoader {
  protected static class ClassPathContextResource extends ClassPathResource
    implements ContextResource {

    public ClassPathContextResource(final String path, final ClassLoader classLoader) {
      super(path, classLoader);
    }

    @Override
    public Resource createRelative(final String relativePath) {
      final String pathToUse = StringUtils.applyRelativePath(getClassPath(), relativePath);
      return new ClassPathContextResource(pathToUse, getClassLoader());
    }

    @Override
    public String getPathWithinContext() {
      return getClassPath();
    }
  }

  private ClassLoader classLoader;

  public DefaultResourceLoader() {
    this.classLoader = ClassUtils.getDefaultClassLoader();
  }

  public DefaultResourceLoader(final ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  @Override
  public ClassLoader getClassLoader() {
    return this.classLoader != null ? this.classLoader : ClassUtils.getDefaultClassLoader();
  }

  @Override
  public Resource getResource(final String location) {
    Assert.notNull(location, "Location must not be null");
    if (location.startsWith("/")) {
      return getResourceByPath(location);
    } else if (location.startsWith(CLASSPATH_URL_PREFIX)) {
      return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()),
        getClassLoader());
    } else {
      try {
        // Try to parse the location as a URL...
        final URL url = new URL(location);
        return new UrlResource(url);
      } catch (final MalformedURLException ex) {
        // No URL -> resolve as resource path.
        return getResourceByPath(location);
      }
    }
  }

  protected Resource getResourceByPath(final String path) {
    return new ClassPathContextResource(path, getClassLoader());
  }

  public void setClassLoader(final ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

}
