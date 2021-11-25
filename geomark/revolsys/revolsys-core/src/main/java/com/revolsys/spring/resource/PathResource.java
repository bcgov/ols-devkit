/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.revolsys.spring.resource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import org.jeometry.common.exception.Exceptions;
import org.springframework.core.io.WritableResource;
import org.springframework.util.Assert;

import com.revolsys.io.PathUtil;
import com.revolsys.io.channels.Channels;

/**
 * {@link Resource} implementation for {@code com.revolsys.nio.file.Path} handles.
 * Supports resolution as File, and also as URL.
 * Implements the extended {@link WritableResource} interface.
 *
 * @author Philippe Marschall
 * @since 4.0
 * @see java.nio.file.Path
 */
public class PathResource extends AbstractResource implements WritableResource {
  private final Path path;

  public PathResource(final File file) {
    this(file.toPath());
  }

  /**
   * @param path a PathUtil handle
   */
  public PathResource(final Path path) {
    Assert.notNull(path, "Path must not be null");
    this.path = path.normalize();
  }

  /**
   * @param path a PathUtil handle
   */
  public PathResource(final Resource parent, final Path path) {
    super(parent);
    Assert.notNull(parent, "Parent must not be null");
    Assert.notNull(path, "Path must not be null");
    this.path = path.normalize();
  }

  /**
   * Construct a new new PathResource from a PathUtil handle.
   * @param path a path
   * @see com.revolsys.io.file.com.revolsys.nio.file.Paths#getPath(String, String...)
   */
  public PathResource(final String path) {
    Assert.notNull(path, "Path must not be null");
    this.path = Paths.get(path).normalize();
  }

  /**
   * Construct a new new PathResource from a PathUtil handle.
   * @see com.revolsys.io.file.com.revolsys.nio.file.Paths#get(URI)
   * @param uri a path URI
   */
  public PathResource(final URI uri) {
    Assert.notNull(uri, "URI must not be null");
    this.path = Paths.get(uri).normalize();
  }

  /**
   * This implementation returns the underlying File's length.
   */
  @Override
  public long contentLength() throws IOException {
    return Files.size(this.path);
  }

  @Override
  public boolean copyFrom(final InputStream in) {
    final File file = getFile();
    final File parent = file.getParentFile();
    if (!parent.exists()) {
      parent.mkdirs();
    }
    return super.copyFrom(in);
  }

  @Override
  public boolean copyFrom(final Resource source) {
    if (source == null) {
      return false;
    } else {
      try (
        ReadableByteChannel in = source.newReadableByteChannel()) {
        if (in == null) {
          return false;
        } else {
          try (
            FileChannel out = newWritableByteChannel()) {
            final long size;
            if (in instanceof FileChannel) {
              size = ((FileChannel)in).size();
            } else {
              size = source.contentLength();
            }
            Channels.copy(in, out, size);
            return true;
          } catch (final IOException e) {
            throw Exceptions.wrap(e);
          }
        }
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    }
  }

  @Override
  public boolean copyTo(final Resource target) {
    if (target == null) {
      return false;
    } else {
      try (
        WritableByteChannel out = target.newWritableByteChannel()) {
        return copyTo(out);
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    }
  }

  public boolean copyTo(final WritableByteChannel out) {
    if (out == null) {
      return false;
    } else {
      try (
        FileChannel in = newReadableByteChannel()) {
        if (in == null) {
          return false;
        } else {
          Channels.copy(in, out);
          return true;
        }
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    }
  }

  @Override
  public boolean createParentDirectories() {
    com.revolsys.io.file.Paths.createParentDirectories(this.path);
    return true;
  }

  /**
   * This implementation creates a FileResource, applying the given path
   * relative to the path of the underlying file of this resource descriptor.
   * @see java.nio.file.Path#resolve(String)
   */
  @Override
  public Resource createRelative(final String relativePath) {
    final Path childPath = this.path.resolve(relativePath);
    return new PathResource(childPath);
  }

  @Override
  public boolean delete() {
    try {
      return Files.deleteIfExists(this.path);
    } catch (final DirectoryNotEmptyException e) {
      return false;
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  public boolean deleteDirectory() {
    return com.revolsys.io.file.Paths.deleteDirectories(this.path);
  }

  /**
   * This implementation compares the underlying PathUtil references.
   */
  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof PathResource && this.path.equals(((PathResource)obj).path);
  }

  /**
   * This implementation returns whether the underlying file exists.
   * @see PathResource#exists()
   */
  @Override
  public boolean exists() {
    return Files.exists(this.path, com.revolsys.io.file.Paths.LINK_OPTIONS_NONE);
  }

  @Override
  public String getDescription() {
    return this.path.toAbsolutePath().toString();
  }

  /**
   * This implementation returns the underlying File reference.
   */
  @Override
  public File getFile() {
    return this.path.toFile();
  }

  /**
   * This implementation returns the name of the file.
   * @see java.nio.file.Path#getFileName()
   */
  @Override
  public String getFilename() {
    return this.path.getFileName().toString();
  }

  /**
   * This implementation opens a InputStream for the underlying file.
   * @see com.revolsys.nio.file.spi.FileSystemProvider#newInputStream(PathUtil, OpenOption...)
   */
  @Override
  public InputStream getInputStream() {
    try {
      return Files.newInputStream(this.path, com.revolsys.io.file.Paths.OPEN_OPTIONS_NONE);
    } catch (final FileSystemException e) {
      throw Exceptions.wrap("Error opening file: " + getPath(), e);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public Instant getLastModifiedInstant() {
    final Path path = getPath();
    try {
      return Files.getLastModifiedTime(path).toInstant();
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public OutputStream getOutputStream() {
    try {
      final Path path = this.path;
      return Files.newOutputStream(path, com.revolsys.io.file.Paths.OPEN_OPTIONS_NONE);
    } catch (final FileSystemException e) {
      throw Exceptions.wrap("Error opening file: " + getPath(), e);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public Resource getParent() {
    Resource parent = super.getParent();
    if (parent == null) {
      final Path parentPath = this.path.getParent();
      if (parentPath == null) {
        return null;
      } else {
        parent = new PathResource(parentPath);
        setParent(parent);
      }
    }
    return parent;
  }

  /**
   * Return the file path for this resource.
   */
  @Override
  public final Path getPath() {
    return this.path;
  }

  /**
   * This implementation returns a URI for the underlying file.
   * @see java.nio.file.Path#toUri()
   */
  @Override
  public URI getURI() throws IOException {
    return this.path.toUri();
  }

  /**
   * This implementation returns a URL for the underlying file.
   * @see java.nio.file.Path#toUri()
   * @see java.net.URI#toURL()
   */
  @Override
  public URL getURL() {
    try {
      return this.path.toUri().toURL();
    } catch (final MalformedURLException e) {
      throw Exceptions.wrap(e);
    }
  }

  /**
   * This implementation returns the hash code of the underlying PathUtil reference.
   */
  @Override
  public int hashCode() {
    return this.path.hashCode();
  }

  @Override
  public boolean isFile() {
    return true;
  }

  /**
   * This implementation checks whether the underlying file is marked as readable
   * (and corresponds to an actual file with content, not to a directory).
   * @see com.revolsys.nio.file.Files#isReadable(PathUtil)
   * @see com.revolsys.nio.file.Files#isDirectory(PathUtil, com.revolsys.nio.file.LinkOption...)
   */
  @Override
  public boolean isReadable() {
    return Files.isReadable(this.path) && !Files.isDirectory(this.path);
  }

  @Override
  public boolean isWritable() {
    return Files.isWritable(this.path) && !Files.isDirectory(this.path);
  }

  /**
   * This implementation returns the underlying File's timestamp.
   * @see com.revolsys.nio.file.Files#getLastModifiedTime(PathUtil, com.revolsys.nio.file.LinkOption...)
   */
  @Override
  public long lastModified() throws IOException {
    // we can not use the super class method since it uses conversion to a File
    // and
    // only Paths on the default file system can be converted to a File
    return Files.getLastModifiedTime(this.path).toMillis();
  }

  @Override
  public OutputStream newOutputStream() {
    return getOutputStream();
  }

  // implementation of WritableResource

  @Override
  public OutputStream newOutputStreamAppend() {
    try {
      final Path path = this.path;
      return Files.newOutputStream(path, com.revolsys.io.file.Paths.OPEN_OPTIONS_APPEND);
    } catch (final FileSystemException e) {
      throw Exceptions.wrap("Error opening file: " + getPath(), e);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public FileChannel newReadableByteChannel() {
    try {
      return FileChannel.open(this.path, com.revolsys.io.file.Paths.OPEN_OPTIONS_READ_SET,
        com.revolsys.io.file.Paths.FILE_ATTRIBUTES_NONE);
    } catch (final NoSuchFileException e) {
      return null;
    } catch (final FileSystemException e) {
      return null;
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public FileChannel newWritableByteChannel() {
    try {
      try {
        return FileChannel.open(this.path, com.revolsys.io.file.Paths.OPEN_OPTIONS_WRITE_SET,
          com.revolsys.io.file.Paths.FILE_ATTRIBUTES_NONE);
      } catch (final NoSuchFileException e) {
        if (com.revolsys.io.file.Paths.createParentDirectories(this.path) == null) {
          return null;
        } else {
          return FileChannel.open(this.path, com.revolsys.io.file.Paths.OPEN_OPTIONS_WRITE_SET,
            com.revolsys.io.file.Paths.FILE_ATTRIBUTES_NONE);
        }
      }
    } catch (final FileSystemException e) {
      throw Exceptions.wrap("Error opening file: " + getPath(), e);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public Writer newWriter() {
    return newWriter(StandardCharsets.UTF_8);
  }

  @Override
  public Writer newWriter(final Charset charset) {
    try {
      final File file = getFile();
      return new FileWriter(file, charset);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

}
