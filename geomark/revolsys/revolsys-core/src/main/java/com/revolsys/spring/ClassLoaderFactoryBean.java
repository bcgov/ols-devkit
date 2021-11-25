package com.revolsys.spring;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.revolsys.io.FileUtil;
import com.revolsys.io.filter.DirectoryFilenameFilter;
import com.revolsys.io.filter.ExtensionFilenameFilter;

public class ClassLoaderFactoryBean extends AbstractFactoryBean<ClassLoader> {

  private static final FilenameFilter JAR_FILTER = new ExtensionFilenameFilter("jar", "zip");

  public static void addJars(final Collection<URL> urls, final File directory) {
    if (directory.exists() && directory.isDirectory()) {
      final File[] libFiles = directory.listFiles(JAR_FILTER);
      for (final File libFile : libFiles) {
        urls.add(FileUtil.toUrl(libFile));
      }
      final File[] subDirs = directory.listFiles(new DirectoryFilenameFilter());
      for (final File subDir : subDirs) {
        addJars(urls, subDir);
      }
    }
  }

  public static URLClassLoader newClassLoader(final ClassLoader parentClassLoader,
    final Collection<URL> urls) {
    URL[] urlArray = new URL[urls.size()];
    urlArray = urls.toArray(urlArray);
    return new URLClassLoader(urlArray, parentClassLoader);
  }

  public static URLClassLoader newClassLoader(final ClassLoader parentClassLoader,
    final File file) {
    final Collection<URL> urls = new LinkedHashSet<>();
    if (file.isDirectory()) {
      addJars(urls, file);
    } else if (JAR_FILTER.accept(file.getParentFile(), FileUtil.getFileName(file))) {
      urls.add(FileUtil.toUrl(file));
    }
    return newClassLoader(parentClassLoader, urls);
  }

  private Collection<File> libDirectories = new LinkedHashSet<>();

  private final Collection<URL> mergedUrls = new LinkedHashSet<>();

  private Collection<URL> urls = new LinkedHashSet<>();

  @Override
  protected ClassLoader createInstance() throws Exception {
    final Class<? extends ClassLoaderFactoryBean> clazz = getClass();
    final ClassLoader parentClassLoader = clazz.getClassLoader();
    final URLClassLoader classLoader = newClassLoader(parentClassLoader, this.mergedUrls);
    return classLoader;
  }

  public Collection<File> getLibDirectories() {
    return this.libDirectories;
  }

  @Override
  public Class<ClassLoader> getObjectType() {
    return ClassLoader.class;
  }

  public Collection<URL> getUrls() {
    return this.urls;
  }

  public void setLibDirectories(final Collection<File> libDirectories) {
    this.libDirectories = libDirectories;
    for (final File directory : libDirectories) {
      addJars(this.mergedUrls, directory);
    }
  }

  public void setUrls(final Collection<URL> urls) {
    this.urls = urls;
    this.mergedUrls.addAll(urls);
  }
}
