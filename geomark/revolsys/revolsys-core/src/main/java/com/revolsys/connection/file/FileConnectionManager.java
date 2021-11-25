package com.revolsys.connection.file;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.revolsys.beans.PropertyChangeSupport;
import com.revolsys.connection.ConnectionRegistryManager;
import com.revolsys.spring.resource.Resource;

public class FileConnectionManager extends FileSystem
  implements ConnectionRegistryManager<FolderConnectionRegistry>, URLStreamHandlerFactory {

  static FileConnectionManager instance;

  private static final FileSystem DEFAULT_FILE_SYSTEM = FileSystems.getDefault();

  public static FileConnectionManager get() {
    if (instance == null) {
      new FileConnectionFileSystemProvider();
    }
    return instance;
  }

  public static File getConnection(final String name) {
    final FileConnectionManager connectionManager = get();
    final List<FolderConnectionRegistry> registries = new ArrayList<>();
    registries.addAll(connectionManager.getConnectionRegistries());
    final FolderConnectionRegistry threadRegistry = FolderConnectionRegistry.getForThread();
    if (threadRegistry != null) {
      registries.add(threadRegistry);
    }
    Collections.reverse(registries);
    for (final FolderConnectionRegistry registry : registries) {
      final FolderConnection connection = registry.getConnection(name);
      if (connection != null) {
        return connection.getFile();
      }
    }
    return null;
  }

  private final FileConnectionFileSystemProvider provider;

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  private final List<FolderConnectionRegistry> registries = new ArrayList<>();

  public FileConnectionManager(final FileConnectionFileSystemProvider provider) {
    this.provider = provider;
  }

  @Override
  public void addConnectionRegistry(final FolderConnectionRegistry registry) {
    if (registry != null) {
      int index = -1;
      synchronized (this.registries) {
        if (!this.registries.contains(registry)) {
          index = this.registries.size();
          this.registries.add(registry);
          registry.setConnectionManager(this);
        }
      }
      if (index != -1) {
        index = getVisibleConnectionRegistries().indexOf(registry);
        if (index != -1) {
          this.propertyChangeSupport.fireIndexedPropertyChange("registries", index, null, registry);
          this.propertyChangeSupport.fireIndexedPropertyChange("children", index, null, registry);
        }
      }
    }
  }

  public synchronized FolderConnectionRegistry addConnectionRegistry(final String name) {
    final FolderConnectionRegistry registry = new FolderConnectionRegistry(this, name);
    addConnectionRegistry(registry);
    return registry;
  }

  public synchronized FolderConnectionRegistry addConnectionRegistry(final String name,
    final Resource resource) {
    final FolderConnectionRegistry registry = new FolderConnectionRegistry(this, name, resource,
      false);
    addConnectionRegistry(registry);
    return registry;
  }

  @Override
  public void close() throws IOException {
  }

  @Override
  public URLStreamHandler createURLStreamHandler(final String protocol) {
    return null;
  }

  protected FolderConnectionRegistry findConnectionRegistry(final String name) {
    for (final FolderConnectionRegistry registry : this.registries) {
      if (registry.getName().equals(name)) {
        return registry;
      }
    }
    return null;
  }

  @Override
  public List<FolderConnectionRegistry> getConnectionRegistries() {
    return new ArrayList<>(this.registries);
  }

  @Override
  public FolderConnectionRegistry getConnectionRegistry(final String name) {
    final FolderConnectionRegistry connectionRegistry = findConnectionRegistry(name);
    if (connectionRegistry == null) {
      return this.registries.get(0);
    }
    return connectionRegistry;
  }

  @Override
  public Iterable<FileStore> getFileStores() {
    return Collections.emptyList();
  }

  @Override
  public String getIconName() {
    return "folder:link";
  }

  @Override
  public String getName() {
    return "Folder Connections";
  }

  @Override
  public Path getPath(final String first, final String... more) {
    String path;
    if (more.length == 0) {
      path = first;
    } else {
      final StringBuilder sb = new StringBuilder();
      sb.append(first);
      for (final String segment : more) {
        if (segment.length() > 0) {
          if (sb.length() > 0) {
            sb.append('/');
          }
          sb.append(segment);
        }
      }
      path = sb.toString();
    }
    // return new FileConnectionPath(this, path);
    return null;
  }

  @Override
  public PathMatcher getPathMatcher(final String syntaxAndPattern) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return this.propertyChangeSupport;
  }

  @Override
  public Iterable<Path> getRootDirectories() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getSeparator() {
    return "/";
  }

  @Override
  public UserPrincipalLookupService getUserPrincipalLookupService() {
    return DEFAULT_FILE_SYSTEM.getUserPrincipalLookupService();
  }

  @Override
  public List<FolderConnectionRegistry> getVisibleConnectionRegistries() {
    final List<FolderConnectionRegistry> registries = new ArrayList<>();
    for (final FolderConnectionRegistry registry : this.registries) {
      if (registry != null && registry.isVisible()) {
        registries.add(registry);
      }
    }
    return registries;
  }

  @Override
  public boolean isOpen() {
    return true;
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public WatchService newWatchService() throws IOException {
    return DEFAULT_FILE_SYSTEM.newWatchService();
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    this.propertyChangeSupport.firePropertyChange(event);
  }

  @Override
  public FileSystemProvider provider() {
    return this.provider;
  }

  @Override
  public void removeConnectionRegistry(final FolderConnectionRegistry registry) {
    if (registry != null) {
      final int index;
      synchronized (this.registries) {
        index = this.registries.indexOf(registry);
        if (index != -1) {
          this.registries.remove(registry);
          registry.setConnectionManager(null);
          registry.getPropertyChangeSupport().removePropertyChangeListener(this);
        }
      }
      if (index != -1) {
        this.propertyChangeSupport.fireIndexedPropertyChange("registries", index, registry, null);
      }
    }
  }

  public void removeConnectionRegistry(final String name) {
    final FolderConnectionRegistry connectionRegistry = findConnectionRegistry(name);
    removeConnectionRegistry(connectionRegistry);
  }

  @Override
  public Set<String> supportedFileAttributeViews() {
    return DEFAULT_FILE_SYSTEM.supportedFileAttributeViews();
  }

  @Override
  public String toString() {
    return getName();
  }
}
