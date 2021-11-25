
package com.revolsys.spring.resource;

import java.io.InputStream;
import java.net.URL;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

public class ClassPathResource extends AbstractResource {

  public static ClassPathResource newInPackage(final Class<?> clazz, final String name) {
    return new ClassPathResource(name, clazz);
  }

  private ClassLoader classLoader;

  private Class<?> clazz;

  private final String path;

  public ClassPathResource(final String path) {
    this(path, (ClassLoader)null);
  }

  public ClassPathResource(final String path, final Class<?> clazz) {
    Assert.notNull(path, "Path must not be null");
    this.path = StringUtils.cleanPath(path);
    this.clazz = clazz;
  }

  public ClassPathResource(final String path, final ClassLoader classLoader) {
    Assert.notNull(path, "Path must not be null");
    String pathToUse = StringUtils.cleanPath(path);
    if (pathToUse.startsWith("/")) {
      pathToUse = pathToUse.substring(1);
    }
    this.path = pathToUse;
    this.classLoader = classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader();
  }

  protected ClassPathResource(final String path, final ClassLoader classLoader,
    final Class<?> clazz) {
    this.path = StringUtils.cleanPath(path);
    this.classLoader = classLoader;
    this.clazz = clazz;
  }

  @Override
  public Resource createRelative(final String relativePath) {
    final String pathToUse = StringUtils.applyRelativePath(this.path, relativePath);
    return new ClassPathResource(pathToUse, this.classLoader, this.clazz);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof ClassPathResource) {
      final ClassPathResource otherRes = (ClassPathResource)obj;
      return this.path.equals(otherRes.path)
        && ObjectUtils.nullSafeEquals(this.classLoader, otherRes.classLoader)
        && ObjectUtils.nullSafeEquals(this.clazz, otherRes.clazz);
    }
    return false;
  }

  @Override
  public boolean exists() {
    try {
      getURL();
      return true;
    } catch (final Throwable t) {
      return false;
    }
  }

  public final ClassLoader getClassLoader() {
    return this.clazz != null ? this.clazz.getClassLoader() : this.classLoader;
  }

  public final String getClassPath() {
    return this.path;
  }

  @Override
  public String getDescription() {
    final StringBuilder builder = new StringBuilder();
    String pathToUse = this.path;
    if (this.clazz != null && !pathToUse.startsWith("/")) {
      builder.append(ClassUtils.classPackageAsResourcePath(this.clazz));
      builder.append('/');
    }
    if (pathToUse.startsWith("/")) {
      pathToUse = pathToUse.substring(1);
    }
    builder.append(pathToUse);
    return builder.toString();
  }

  @Override
  public String getFilename() {
    return StringUtils.getFilename(this.path);
  }

  @Override
  public InputStream getInputStream() {
    InputStream is;
    if (this.clazz != null) {
      is = this.clazz.getResourceAsStream(this.path);
    } else if (this.classLoader != null) {
      is = this.classLoader.getResourceAsStream(this.path);
    } else {
      is = ClassLoader.getSystemResourceAsStream(this.path);
    }
    if (is == null) {
      throw new NoSuchResourceException(this);
    }
    return is;
  }

  @Override
  public URL getURL() {
    URL url;
    if (this.clazz != null) {
      url = this.clazz.getResource(this.path);
    } else if (this.classLoader != null) {
      url = this.classLoader.getResource(this.path);
    } else {
      url = ClassLoader.getSystemResource(this.path);
    }
    if (url == null) {
      throw new NoSuchResourceException(this);
    }
    return url;
  }

  @Override
  public int hashCode() {
    return this.path.hashCode();
  }

}
