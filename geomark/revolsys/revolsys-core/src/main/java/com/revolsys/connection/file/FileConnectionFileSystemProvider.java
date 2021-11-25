package com.revolsys.connection.file;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;

import com.revolsys.spring.resource.PathResource;
import com.revolsys.util.OS;

public class FileConnectionFileSystemProvider extends FileSystemProvider {
  private static final FileSystemProvider DEFAULT_PROVIDER = FileSystems.getDefault().provider();

  private final FileConnectionManager fileSystem;

  public FileConnectionFileSystemProvider() {
    this.fileSystem = new FileConnectionManager(this);
    final File directory = OS.getApplicationDataDirectory("com.revolsys.gis/Folder Connections");
    this.fileSystem.addConnectionRegistry("User", new PathResource(directory));
    FileConnectionManager.instance = this.fileSystem;
  }

  @Override
  public void checkAccess(final Path path, final AccessMode... modes) throws IOException {
    final Path filePath = getFilePath(path);
    DEFAULT_PROVIDER.checkAccess(filePath, modes);
  }

  private void checkUri(final URI uri) {
    final String scheme = getScheme();
    if (!uri.getScheme().equalsIgnoreCase(scheme)) {
      throw new IllegalArgumentException("URI scheme must be " + scheme);
    }
    if (uri.getAuthority() != null) {
      throw new IllegalArgumentException("URI Authority not allowed");
    }
    if (!"/".equals(uri.getPath())) {
      throw new IllegalArgumentException("URI Path must be '/'");
    }
    if (uri.getQuery() != null) {
      throw new IllegalArgumentException("URI Query not allowed");
    }
    if (uri.getFragment() != null) {
      throw new IllegalArgumentException("URI Fragment not allowed");
    }
  }

  @Override
  public void copy(final Path source, final Path target, final CopyOption... options)
    throws IOException {
    final Path sourcePath = getFilePath(source);
    final Path targetPath = getFilePath(target);
    Files.copy(sourcePath, targetPath, options);
  }

  @Override
  public void createDirectory(final Path dir, final FileAttribute<?>... attrs) throws IOException {
    final Path filePath = getFilePath(dir);
    Files.createDirectory(filePath, attrs);
  }

  @Override
  public void delete(final Path path) throws IOException {
    final Path filePath = getFilePath(path);
    Files.delete(filePath);
  }

  @Override
  public <V extends FileAttributeView> V getFileAttributeView(final Path path, final Class<V> type,
    final LinkOption... options) {
    final Path filePath = getFilePath(path);
    return Files.getFileAttributeView(filePath, type, options);
  }

  private Path getFilePath(final Path path) {
    // if (path instanceof FileConnectionPath) {
    // final FileConnectionPath fileConnectionPath = (FileConnectionPath)path;
    // return fileConnectionPath.toFilePath();
    // } else {
    return path;
    // }
  }

  @Override
  public FileStore getFileStore(final Path path) throws IOException {
    final Path filePath = getFilePath(path);
    return Files.getFileStore(filePath);
  }

  @Override
  public FileSystem getFileSystem(final URI uri) {
    checkUri(uri);
    return this.fileSystem;
  }

  @Override
  public Path getPath(final URI uri) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getScheme() {
    return "fileconnection";
  }

  @Override
  public boolean isHidden(final Path path) throws IOException {
    final Path filePath = getFilePath(path);
    return Files.isHidden(filePath);
  }

  @Override
  public boolean isSameFile(final Path path, final Path path2) throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void move(final Path source, final Path target, final CopyOption... options)
    throws IOException {
    final Path sourcePath = getFilePath(source);
    final Path targetPath = getFilePath(target);
    Files.copy(sourcePath, targetPath, options);
  }

  @Override
  public SeekableByteChannel newByteChannel(final Path path,
    final Set<? extends OpenOption> options, final FileAttribute<?>... attrs) throws IOException {
    final Path filePath = getFilePath(path);
    return Files.newByteChannel(filePath, options, attrs);
  }

  @Override
  public DirectoryStream<Path> newDirectoryStream(final Path path,
    final Filter<? super Path> filter) throws IOException {
    final Path filePath = getFilePath(path);
    return Files.newDirectoryStream(filePath);
  }

  @Override
  public FileSystem newFileSystem(final URI uri, final Map<String, ?> env) throws IOException {
    checkUri(uri);
    throw new FileSystemAlreadyExistsException();
  }

  @Override
  public <A extends BasicFileAttributes> A readAttributes(final Path path, final Class<A> type,
    final LinkOption... options) throws IOException {
    final Path filePath = getFilePath(path);
    return readAttributes(filePath, type, options);
  }

  @Override
  public Map<String, Object> readAttributes(final Path path, final String attributes,
    final LinkOption... options) throws IOException {
    final Path filePath = getFilePath(path);
    return readAttributes(filePath, attributes, options);
  }

  @Override
  public void setAttribute(final Path path, final String attribute, final Object value,
    final LinkOption... options) throws IOException {
    final Path filePath = getFilePath(path);
    Files.setAttribute(filePath, attribute, value, options);
  }
}
