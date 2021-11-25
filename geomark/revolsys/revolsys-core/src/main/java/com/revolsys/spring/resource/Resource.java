package com.revolsys.spring.resource;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.io.FileProxy;
import org.jeometry.common.net.UrlProxy;

import com.revolsys.collection.list.Lists;
import com.revolsys.io.FileNames;
import com.revolsys.io.FileUtil;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.io.channels.DataReader;
import com.revolsys.io.file.Paths;
import com.revolsys.predicate.Predicates;
import com.revolsys.util.Property;

public interface Resource extends org.springframework.core.io.Resource, FileProxy, UrlProxy {
  String CLASSPATH_URL_PREFIX = "classpath:";

  ThreadLocal<Resource> BASE_RESOURCE = new ThreadLocal<>();

  static boolean exists(final Resource resource) {
    if (resource == null) {
      return false;
    } else {
      return resource.exists();
    }
  }

  static Resource getBaseResource() {
    final Resource baseResource = Resource.BASE_RESOURCE.get();
    if (baseResource == null) {
      return new PathResource(FileUtil.getCurrentDirectory());
    } else {
      return baseResource;
    }
  }

  static Resource getBaseResource(final String childPath) {
    final Resource baseResource = getBaseResource();
    return baseResource.newChildResource(childPath);
  }

  static File getFileOrCreateTempFile(final Resource resource) {
    try {
      if (resource instanceof PathResource) {
        return resource.getFile();
      } else {
        final String filename = resource.getFilename();
        String baseName = FileUtil.getBaseName(filename);
        final String fileExtension = FileNames.getFileNameExtension(filename);
        if (baseName.length() < 3) {
          baseName += "___";
        }
        return File.createTempFile(baseName, fileExtension);
      }
    } catch (final IOException e) {
      throw new RuntimeException("Unable to get file for " + resource, e);
    }
  }

  static File getOrDownloadFile(final Resource resource) {
    if (resource == null) {
      return null;
    } else {
      return resource.getOrDownloadFile();
    }
  }

  static Resource getResource(final Object source) {
    if (source == null) {
      return null;
    } else if (source instanceof Resource) {
      return (Resource)source;
    } else if (source instanceof Path) {
      return new PathResource((Path)source);
    } else if (source instanceof File) {
      return new PathResource((File)source);
    } else if (source instanceof URL) {
      return new UrlResource((URL)source);
    } else if (source instanceof URI) {
      return new UrlResource((URI)source);
    } else if (source instanceof CharSequence) {
      return getResource(source.toString());
    } else if (source instanceof InputStream) {
      return new InputStreamResource((InputStream)source);
    } else if (source instanceof OutputStream) {
      return new OutputStreamResource("", (OutputStream)source);
    } else if (source instanceof org.springframework.core.io.Resource) {
      if (source instanceof org.springframework.core.io.ClassPathResource) {
        final org.springframework.core.io.ClassPathResource springResource = (org.springframework.core.io.ClassPathResource)source;
        return new ClassPathResource(springResource.getPath(), springResource.getClassLoader());
      } else if (source instanceof org.springframework.core.io.FileSystemResource) {
        final org.springframework.core.io.FileSystemResource springResource = (org.springframework.core.io.FileSystemResource)source;
        return new PathResource(springResource.getFile());
      } else if (source instanceof org.springframework.core.io.PathResource) {
        final org.springframework.core.io.PathResource springResource = (org.springframework.core.io.PathResource)source;
        return new PathResource(springResource.getPath());
      } else if (source instanceof org.springframework.core.io.UrlResource) {
        final org.springframework.core.io.UrlResource springResource = (org.springframework.core.io.UrlResource)source;
        try {
          return new UrlResource(springResource.getURL());
        } catch (final Exception e) {
          throw Exceptions.wrap(e);
        }
      }
    }

    throw new IllegalArgumentException(source.getClass() + " is not supported");
  }

  static Resource getResource(final String location) {
    if (Property.hasValue(location)) {
      if (location.charAt(0) == '/' || location.length() > 1 && location.charAt(1) == ':'
        || location.indexOf(':') == -1) {
        return new PathResource(location);
      } else if (location.startsWith(CLASSPATH_URL_PREFIX)) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final String path = location.substring(CLASSPATH_URL_PREFIX.length());
        return new ClassPathResource(path, classLoader);
      } else {
        final UrlResource urlResource = new UrlResource(location);
        if ("file".equals(urlResource.getProtocol())) {
          final URI uri = urlResource.getUri();
          return new PathResource(uri);
        }
        return urlResource;
      }
    }
    return null;
  }

  static Resource newResource(final ZipFile zipFile, final ZipEntry zipEntry) {
    try {
      final InputStream inputStream = zipFile.getInputStream(zipEntry);
      return new InputStreamResource(inputStream);
    } catch (final IOException e) {
      throw Exceptions.wrap("Cannot open " + zipFile + "!" + zipEntry, e);
    }
  }

  static Resource setBaseResource(final Resource baseResource) {
    final Resource oldResource = Resource.BASE_RESOURCE.get();
    Resource.BASE_RESOURCE.set(baseResource);
    return oldResource;
  }

  default String contentsAsString() {
    final Reader reader = newReader();
    return FileUtil.getString(reader);
  }

  default boolean copyFrom(final InputStream in) {
    if (in == null) {
      return false;
    } else {
      try (
        final InputStream in2 = in;
        final OutputStream out = newBufferedOutputStream();) {
        if (out == null) {
          return false;
        } else {
          FileUtil.copy(in2, out);
          return true;
        }
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    }
  }

  default boolean copyFrom(final Object source) {
    final Resource resource = getResource(source);
    return copyFrom(resource);

  }

  default boolean copyFrom(final Resource source) {
    if (source == null) {
      return false;
    } else {
      try (
        final InputStream in = source.newBufferedInputStream()) {
        if (in == null) {
          return false;
        } else {
          return copyFrom(in);
        }
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    }
  }

  default void copyTo(final OutputStream out) {
    try (
      final OutputStream out2 = out;
      final InputStream in = newBufferedInputStream();) {
      FileUtil.copy(in, out2);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  default boolean copyTo(final Resource target) {
    if (target == null) {
      return false;
    } else {
      return target.copyFrom(this);
    }
  }

  default boolean createParentDirectories() {
    return false;
  }

  default Resource createRelative(final Object relativePath) {
    final String path = DataTypes.toString(relativePath);
    return createRelative(path);
  }

  @Override
  Resource createRelative(String relativePath);

  default boolean delete() {
    return false;
  }

  default String getBaseName() {
    final String filename = getFilename();
    return FileNames.getBaseName(filename);
  }

  default List<String> getChildFileNames() {
    if (isFile()) {
      final File file = getFile();
      final List<String> childFileNames = Lists.newArray(file.list());
      Collections.sort(childFileNames);
      return childFileNames;
    } else {
      return Collections.emptyList();
    }
  }

  default List<String> getChildFileNames(final Predicate<String> filter) {
    final List<String> fileNames = getChildFileNames();
    return Predicates.filter(fileNames, filter);
  }

  default List<Resource> getChildren() {
    final List<Resource> children = new ArrayList<>();
    for (final String fileName : getChildFileNames()) {
      final Resource newChildResource = newChildResource(fileName);
      children.add(newChildResource);
    }
    return children;
  }

  default List<Resource> getChildren(final Predicate<String> filter) {
    final List<Resource> children = new ArrayList<>();
    for (final String fileName : getChildFileNames(filter)) {
      final Resource newChildResource = newChildResource(fileName);
      children.add(newChildResource);
    }
    return children;
  }

  @Override
  File getFile();

  default String getFileNameExtension() {
    final String filename = getFilename();
    return FileNames.getFileNameExtension(filename);
  }

  @Override
  InputStream getInputStream();

  default long getLastModified() {
    try {
      return lastModified();
    } catch (final IOException e) {
      return Long.MAX_VALUE;
    }
  }

  default java.sql.Date getLastModifiedDate() {
    long lastModified = getLastModified();
    if (lastModified == Long.MAX_VALUE) {
      lastModified = 0;
    }
    return new java.sql.Date(lastModified);
  }

  default Date getLastModifiedDateTime() {
    final long lastModified = getLastModified();
    if (lastModified == Long.MAX_VALUE) {
      return new Date(0);
    } else {
      return new Date(lastModified);
    }
  }

  default Instant getLastModifiedInstant() {
    final long lastModified = getLastModified();
    if (lastModified == Long.MAX_VALUE) {
      return Instant.MAX;
    } else {
      return Instant.ofEpochMilli(lastModified);
    }
  }

  @Override
  default File getOrDownloadFile() {
    try {
      return getFile();
    } catch (final Throwable e) {
      if (exists()) {
        String baseName = getBaseName();
        if (baseName.length() < 3) {
          baseName += "xxx";
        }
        String fileNameExtension = getFileNameExtension();
        if (fileNameExtension.length() < 3) {
          fileNameExtension += "xxx";
        }
        final File file = FileUtil.newTempFile(baseName, "." + fileNameExtension);
        try (
          InputStream inputStream = getInputStream()) {
          FileUtil.copy(inputStream, file);
        } catch (final IOException e1) {
          throw Exceptions.wrap("Error downloading: " + this, e1);
        }
        return file;
      } else {
        throw new IllegalArgumentException("Cannot get File for resource " + this, e);
      }
    }
  }

  default <R> R getOrDownloadFile(final Function<File, R> factory) {
    final File file = getOrDownloadFile();
    if (file == null) {
      return null;
    } else {
      return factory.apply(file);
    }
  }

  default Path getOrDownloadPath() {
    try {
      return getPath();
    } catch (final Throwable e) {
      if (exists()) {
        String baseName = getBaseName();
        if (baseName.length() < 3) {
          baseName += "xxx";
        }
        String fileNameExtension = getFileNameExtension();
        if (fileNameExtension.length() < 3) {
          fileNameExtension += "xxx";
        }
        Path path;
        try {
          path = Files.createTempFile(baseName, "." + fileNameExtension);
          try (
            InputStream inputStream = getInputStream()) {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
          } catch (final IOException e1) {
            throw Exceptions.wrap("Error downloading: " + this, e1);
          }
        } catch (final IOException e1) {
          throw Exceptions.wrap("Error downloading: " + this, e1);
        }
        return path;
      } else {
        throw new IllegalArgumentException("Cannot get File for resource " + this, e);
      }
    }
  }

  default Resource getParent() {
    return null;
  }

  default Path getPath() {
    final File file = getFile();
    if (file == null) {
      return null;
    } else {
      return file.toPath();
    }
  }

  @Override
  default URI getUri() {
    try {
      return getURI();
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  default String getUriString() {
    final URI uri = getUri();
    if (uri == null) {
      return null;
    } else {
      return uri.toString();
    }
  }

  @Override
  default URL getUrl() {
    return getURL();
  }

  @Override
  URL getURL();

  @Override
  default boolean isFile() {
    try {
      getFile();
      return true;
    } catch (final Throwable e) {
      return false;
    }
  }

  default InputStream newBufferedInputStream() {
    final InputStream in = newInputStream();
    return new BufferedInputStream(in);
  }

  default <IS, IS2 extends IS> IS newBufferedInputStream(final Function<InputStream, IS2> factory) {
    final InputStream out = newBufferedInputStream();
    return factory.apply(out);
  }

  default OutputStream newBufferedOutputStream() {
    final OutputStream out = newOutputStream();
    return new BufferedOutputStream(out);
  }

  default <OS extends OutputStream> OS newBufferedOutputStream(
    final Function<OutputStream, OS> factory) {
    final OutputStream out = newBufferedOutputStream();
    return factory.apply(out);
  }

  default BufferedReader newBufferedReader() {
    final Reader in = newReader();
    return new BufferedReader(in);
  }

  default DataReader newChannelReader() {
    return newChannelReader(8192, ByteOrder.BIG_ENDIAN);
  }

  default ChannelReader newChannelReader(final ByteBuffer byteBuffer) {
    final ReadableByteChannel in = newReadableByteChannel();
    if (in == null) {
      return null;
    } else {
      return new ChannelReader(in, byteBuffer);
    }
  }

  default DataReader newChannelReader(final int capacity) {
    return newChannelReader(capacity, ByteOrder.BIG_ENDIAN);
  }

  default DataReader newChannelReader(final int capacity, final ByteOrder byteOrder) {
    final ReadableByteChannel in = newReadableByteChannel();
    if (in == null) {
      return null;
    } else {
      return new ChannelReader(in, capacity, byteOrder);
    }
  }

  default ChannelWriter newChannelWriter() {
    return newChannelWriter(8192, ByteOrder.BIG_ENDIAN);
  }

  default ChannelWriter newChannelWriter(final ByteBuffer buffer) {
    final WritableByteChannel in = newWritableByteChannel();
    return new ChannelWriter(in, true, buffer);
  }

  default ChannelWriter newChannelWriter(final int capacity) {
    return newChannelWriter(capacity, ByteOrder.BIG_ENDIAN);
  }

  default ChannelWriter newChannelWriter(final int capacity, final ByteOrder byteOrder) {
    final WritableByteChannel in = newWritableByteChannel();
    return new ChannelWriter(in, true, capacity, byteOrder);
  }

  default Resource newChildResource(final CharSequence childPath) {
    return createRelative(childPath.toString());
  }

  default InputStream newInputStream() {
    return getInputStream();
  }

  default OutputStream newOutputStream() {
    try {
      final URL url = getURL();
      final String protocol = url.getProtocol();
      if (protocol.equals("file") || protocol.equals("folderconnection")) {
        final File file = getFile();
        final File parentFile = file.getParentFile();
        if (parentFile != null) {
          parentFile.mkdirs();
        }
        return new FileOutputStream(file);
      } else {
        final URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        return connection.getOutputStream();
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  default OutputStream newOutputStreamAppend() {
    throw new UnsupportedOperationException("Cannot created appended output stream for:" + this);
  }

  default PrintWriter newPrintWriter() {
    final Writer writer = newWriter();
    return new PrintWriter(writer);
  }

  default ReadableByteChannel newReadableByteChannel() {
    final InputStream in = newInputStream();
    if (in == null) {
      return null;
    } else {
      return Channels.newChannel(in);
    }
  }

  default Reader newReader() {
    final InputStream in = getInputStream();
    return FileUtil.newUtf8Reader(in);
  }

  default Reader newReader(final Charset charset) {
    final InputStream in = getInputStream();
    return new InputStreamReader(in, charset);
  }

  default Resource newResourceAddExtension(final String extension) {
    final String fileName = getFilename();
    final String newFileName = fileName + "." + extension;
    final Resource parent = getParent();
    if (parent == null) {
      return null;
    } else {
      return parent.newChildResource(newFileName);
    }
  }

  default Resource newResourceChangeExtension(final String extension) {
    if (extension.equals(getFileNameExtension())) {
      return this;
    } else {
      final String baseName = getBaseName();
      final String newFileName = baseName + "." + extension;
      final Resource parent = getParent();
      if (parent == null) {
        return null;
      } else {
        return parent.newChildResource(newFileName);
      }
    }
  }

  default WritableByteChannel newWritableByteChannel() {
    final OutputStream out = newOutputStream();
    return Channels.newChannel(out);
  }

  default Writer newWriter() {
    final OutputStream stream = newOutputStream();
    return FileUtil.newUtf8Writer(stream);
  }

  default Writer newWriter(final Charset charset) {
    final OutputStream stream = newOutputStream();
    return new OutputStreamWriter(stream, charset);
  }

  default Writer newWriterAppend() {
    final OutputStream stream = newOutputStreamAppend();
    return FileUtil.newUtf8Writer(stream);
  }

  default Path toPath() {
    if (isFile()) {
      final Path path = getFile().toPath();
      if (Paths.exists(path)) {
        try {
          return path.toRealPath();
        } catch (final IOException e) {
          return path;
        }
      } else {
        return path;
      }
    } else {
      return null;
    }
  }
}
