package com.revolsys.connection.file;

import java.nio.file.Files;
import java.nio.file.Path;

import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.MapEx;
import com.revolsys.connection.AbstractConnectionRegistry;
import com.revolsys.connection.ConnectionRegistry;
import com.revolsys.connection.ConnectionRegistryManager;
import com.revolsys.io.file.Paths;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.spring.resource.Resource;

public class FolderConnectionRegistry extends AbstractConnectionRegistry<FolderConnection> {
  private static final ThreadLocal<FolderConnectionRegistry> threadRegistry = new ThreadLocal<>();

  public static FolderConnectionRegistry getForThread() {
    return FolderConnectionRegistry.threadRegistry.get();
  }

  public static FolderConnectionRegistry setForThread(final FolderConnectionRegistry registry) {
    final FolderConnectionRegistry oldValue = getForThread();
    FolderConnectionRegistry.threadRegistry.set(registry);
    return oldValue;
  }

  public FolderConnectionRegistry(
    final ConnectionRegistryManager<? extends ConnectionRegistry<FolderConnection>> connectionManager,
    final String name, final boolean visible, final boolean readOnly,
    final Resource directoryResource) {
    super(connectionManager, name, visible, readOnly, directoryResource, "folderConnection");
  }

  public FolderConnectionRegistry(final FileConnectionManager connectionManager,
    final String name) {
    this(connectionManager, name, true, false, null);
  }

  public FolderConnectionRegistry(final FileConnectionManager connectionManager, final String name,
    final Resource directory, final boolean readOnly) {
    this(connectionManager, name, true, readOnly, directory);
  }

  public FolderConnectionRegistry(final String name) {
    this(null, name, true, false, null);
  }

  public FolderConnectionRegistry(final String name, final Resource resource,
    final boolean readOnly) {
    this(null, name, resource, readOnly);
  }

  public void addConnection(final FolderConnection connection) {
    final String name = connection.getName();
    addConnection(name, connection);
    if (!isReadOnly()) {
      final Path file = getConnectionFile(connection, true);
      if (file != null && (!Paths.exists(file) || Files.isWritable(file))) {
        connection.writeToFile(file);
      }
    }
  }

  @Override
  public FolderConnection addConnection(final MapEx config) {
    try {
      final String name = getConnectionName(config, null, true);
      final String fileName = (String)config.get("file");
      final Path file = Paths.getPath(fileName);
      final FolderConnection connection = new FolderConnection(this, name, file);
      addConnection(connection);
      return connection;
    } catch (final Exception e) {
      Logs.error(this, "Error creating folder connection from: " + config, e);
      return null;
    }
  }

  public FolderConnection addConnection(String name, final Path file) {
    name = getUniqueName(name);
    final FolderConnection connection = new FolderConnection(this, name, file);
    addConnection(connection);
    return connection;
  }

  @Override
  public String getIconName() {
    return "folder:link";
  }

  @Override
  protected FolderConnection loadConnection(final Path connectionFile,
    final boolean importConnection) {
    try {
      final MapEx config = Json.toMap(connectionFile);
      final String name = getConnectionName(config, connectionFile, importConnection);
      final String fileName = (String)config.get("file");
      final Path file = Paths.getPath(fileName);
      final FolderConnection connection = new FolderConnection(this, name, file);
      if (!importConnection) {
        connection.setConnectionFile(connectionFile);
      }
      addConnection(connection);
      return connection;
    } catch (final Throwable e) {
      Logs.error(this, "Error creating folder connection from: " + connectionFile, e);
      return null;
    }
  }
}
