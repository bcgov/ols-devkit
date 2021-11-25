package com.revolsys.connection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.jeometry.common.logging.Logs;

import com.revolsys.beans.PropertyChangeSupport;
import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.io.FileUtil;
import com.revolsys.io.file.Paths;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;

public abstract class AbstractConnectionRegistry<C extends Connection>
  implements ConnectionRegistry<C>, PropertyChangeListener, PropertyChangeSupportProxy {

  private ConnectionRegistryManager<ConnectionRegistry<C>> connectionManager;

  private final Map<String, String> connectionNames = new TreeMap<>();

  private final Map<String, C> connections = new TreeMap<>();

  private Path directory;

  private final String fileExtension;

  private final String name;

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  private boolean readOnly;

  private boolean visible = true;

  public AbstractConnectionRegistry(
    final ConnectionRegistryManager<? extends ConnectionRegistry<C>> connectionManager,
    final String name, final boolean visible, final boolean readOnly,
    final Resource directoryResource, final String fileExtension) {
    this.name = name;
    this.fileExtension = fileExtension;
    setConnectionManager(connectionManager);
    setVisible(visible);
    setReadOnly(readOnly);
    setDirectory(directoryResource);
    init();
  }

  protected synchronized void addConnection(final String name, final C connection) {
    if (connection != null && name != null) {
      final String lowerName = name.toLowerCase();
      final C existingConnection = this.connections.get(lowerName);
      removeConnection(existingConnection);
      this.connectionNames.put(lowerName, name);
      this.connections.put(lowerName, connection);
      if (connection instanceof PropertyChangeSupportProxy) {
        final PropertyChangeSupportProxy proxy = (PropertyChangeSupportProxy)connection;
        final PropertyChangeSupport propertyChangeSupport = proxy.getPropertyChangeSupport();
        if (propertyChangeSupport != null) {
          propertyChangeSupport.addPropertyChangeListener(this);
        }
      }
      final int index = getConnectionIndex(name);
      this.propertyChangeSupport.fireIndexedPropertyChange("connections", index, null, connection);
      this.propertyChangeSupport.fireIndexedPropertyChange("children", index, null, connection);
    }
  }

  public void clear() {
    this.directory = null;
    init();
  }

  public void clear(final Resource directory, final boolean readOnly) {
    this.readOnly = readOnly;
    setDirectory(directory);
    init();
  }

  @Override
  public C getConnection(final String connectionName) {
    if (Property.hasValue(connectionName)) {
      return this.connections.get(connectionName.toLowerCase());
    } else {
      return null;
    }
  }

  protected Path getConnectionFile(final Connection connection, final boolean useOriginalFile) {
    Path connectionFile = null;
    if (useOriginalFile) {
      connectionFile = connection.getConnectionFile();
    }
    if (connectionFile == null) {
      final String connectionName = connection.getName();
      connectionFile = getConnectionFile(connectionName);
    }
    return connectionFile;
  }

  protected Path getConnectionFile(final String name) {
    if (Property.hasValue(name)) {
      if (!Paths.exists(this.directory)) {
        if (isReadOnly()) {
          return null;
        } else {
          try {
            Paths.createDirectories(this.directory);
          } catch (final Throwable e) {
            return null;
          }
        }
      }
      final String fileName = FileUtil.toSafeName(name) + "." + this.fileExtension;
      final Path file = this.directory.resolve(fileName);
      return file;
    } else {
      return null;
    }
  }

  protected int getConnectionIndex(final String name) {
    final String lowerName = name.toLowerCase();
    final int index = new ArrayList<>(this.connectionNames.keySet()).indexOf(lowerName);
    return index;
  }

  @Override
  public ConnectionRegistryManager<ConnectionRegistry<C>> getConnectionManager() {
    return this.connectionManager;
  }

  public synchronized String getConnectionName(final C connection) {
    for (final Entry<String, C> entry : this.connections.entrySet()) {
      if (entry.getValue() == connection) {
        final String lowerName = entry.getKey();
        return this.connectionNames.get(lowerName);
      }
    }
    return null;
  }

  protected String getConnectionName(final MapEx config, final Path connectionFile,
    final boolean requireUniqueNames) {
    String name = config.getString("name");
    if (connectionFile != null && !Property.hasValue(name)) {
      name = Paths.getBaseName(connectionFile);
    }
    if (requireUniqueNames) {
      name = getUniqueName(name);
    }
    config.put("name", name);
    return name;
  }

  @Override
  public List<String> getConnectionNames() {
    final List<String> names = new ArrayList<>(this.connectionNames.values());
    return names;
  }

  @Override
  public List<C> getConnections() {
    return new ArrayList<>(this.connections.values());
  }

  public Path getDirectory() {
    return this.directory;
  }

  @Override
  public String getFileExtension() {
    return this.fileExtension;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return this.propertyChangeSupport;
  }

  protected String getUniqueName(String name) {
    int i = 1;
    String newName = name;
    while (getConnection(newName) != null) {
      newName = name + i;
      i++;
    }
    name = newName;
    return name;
  }

  @Override
  public void importConnection(final Path file) {
    if (file != null && Files.isRegularFile(file)) {
      loadConnection(file, true);
    }
  }

  private synchronized void init() {
    this.connectionNames.clear();
    this.connections.clear();
    initDo();
    final List<C> connections = getConnections();
    this.propertyChangeSupport.firePropertyChange("connections", null, connections);
    this.propertyChangeSupport.firePropertyChange("children", null, connections);
  }

  protected void initDo() {
    if (this.directory != null && Files.isDirectory(this.directory)) {
      for (final Path connectionFile : Paths.getChildPaths(this.directory, this.fileExtension,
        "rgobject")) {
        try {
          loadConnection(connectionFile, false);
        } catch (final Exception e) {
          Logs.error(this, "Error loading connection file:" + connectionFile, e);
        }
      }
    }
  }

  @Override
  public boolean isReadOnly() {
    return this.readOnly;
  }

  @Override
  public boolean isVisible() {
    return this.visible;
  }

  protected abstract C loadConnection(final Path connectionFile, boolean importConnection);

  @Override
  public C newConnection(final Map<String, ? extends Object> connectionParameters) {
    final String name = Maps.getString(connectionParameters, "name");
    final Path file = getConnectionFile(name);
    if (file != null && (!Paths.exists(file) || Files.isReadable(file))) {
      Json.writeMap(connectionParameters, file, true);
      return loadConnection(file, false);
    }
    return null;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    this.propertyChangeSupport.firePropertyChange(event);
  }

  public void remove() {
    if (this.connectionManager != null) {
      this.connectionManager.removeConnectionRegistry(this);
      this.connectionManager = null;
    }
  }

  @Override
  public boolean removeConnection(final Connection connection) {
    if (connection == null) {
      return false;
    } else {
      final String name = connection.getName();
      return removeConnection(name, connection);
    }
  }

  public boolean removeConnection(final String name) {
    final C connection = getConnection(name);
    return removeConnection(connection);
  }

  protected synchronized boolean removeConnection(final String name, final Connection connection) {
    if (connection != null && name != null) {
      final String lowerName = name.toLowerCase();
      final C existingConnection = this.connections.get(lowerName);
      if (existingConnection == connection) {
        final int index = getConnectionIndex(name);
        this.connectionNames.remove(lowerName);
        this.connections.remove(lowerName);
        if (connection instanceof PropertyChangeSupportProxy) {
          final PropertyChangeSupportProxy proxy = (PropertyChangeSupportProxy)connection;
          final PropertyChangeSupport propertyChangeSupport = proxy.getPropertyChangeSupport();
          if (propertyChangeSupport != null) {
            propertyChangeSupport.removePropertyChangeListener(this);
          }
        }
        this.propertyChangeSupport.fireIndexedPropertyChange("connections", index, connection,
          null);
        this.propertyChangeSupport.fireIndexedPropertyChange("children", index, connection, null);
        if (this.directory != null && !this.readOnly) {
          final Path file = existingConnection.getConnectionFile();
          Paths.deleteDirectories(file);
        }
        return true;
      }
    }
    return false;
  }

  public void save() {
    saveDo(true);
  }

  public void saveAs(final Resource directory) {
    this.readOnly = false;
    setDirectory(directory);
    saveDo(false);
  }

  public void saveAs(final Resource parentDirectory, final String directoryName) {
    final Resource connectionsDirectory = parentDirectory.newChildResource(directoryName);
    saveAs(connectionsDirectory);
  }

  private void saveDo(final boolean useOriginalFile) {
    for (final Connection connection : this.connections.values()) {
      final Path connectionFile = getConnectionFile(connection, useOriginalFile);
      final String name = connection.getName();
      if (Property.hasValue(name)) {
        connection.writeToFile(connectionFile);
      } else {
        throw new IllegalArgumentException("Connection must have a name");
      }
    }
  }

  @Override
  public void setConnectionManager(
    final ConnectionRegistryManager<? extends ConnectionRegistry<C>> connectionManager) {
    if (this.connectionManager != connectionManager) {
      if (this.connectionManager != null) {
        this.propertyChangeSupport.removePropertyChangeListener(connectionManager);
      }
      this.connectionManager = (ConnectionRegistryManager)connectionManager;
      if (connectionManager != null) {
        this.propertyChangeSupport.addPropertyChangeListener(connectionManager);
      }
    }
  }

  protected void setDirectory(final Resource directoryResource) {
    if (directoryResource instanceof PathResource) {
      final PathResource pathResource = (PathResource)directoryResource;
      final Path directory = pathResource.getPath();
      boolean readOnly = isReadOnly();
      if (!readOnly) {
        if (directoryResource.exists()) {
          readOnly = !Files.isWritable(directory);
        } else if (Paths.deleteDirectories(directory)) {
          readOnly = false;
        } else {
          readOnly = true;
        }
      }
      this.readOnly = readOnly;
      this.directory = directory;
    } else {
      setReadOnly(true);
      this.directory = null;
    }
  }

  public void setReadOnly(final boolean readOnly) {
    if (this.isReadOnly() && !readOnly) {
      throw new IllegalArgumentException("Cannot make a read only registry not read only");
    }
    this.readOnly = readOnly;
  }

  public void setVisible(final boolean visible) {
    this.visible = visible;
  }

  @Override
  public String toString() {
    return getName();
  }
}
