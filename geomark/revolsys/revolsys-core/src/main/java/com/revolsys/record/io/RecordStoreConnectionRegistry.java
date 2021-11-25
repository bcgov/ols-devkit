package com.revolsys.record.io;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.connection.AbstractConnectionRegistry;
import com.revolsys.connection.ConnectionRegistry;
import com.revolsys.connection.ConnectionRegistryManager;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.spring.resource.Resource;

public class RecordStoreConnectionRegistry
  extends AbstractConnectionRegistry<RecordStoreConnection> {

  private static final ThreadLocal<RecordStoreConnectionRegistry> threadRegistry = new ThreadLocal<>();

  public static RecordStoreConnectionRegistry getForThread() {
    return RecordStoreConnectionRegistry.threadRegistry.get();
  }

  public static RecordStoreConnectionRegistry setForThread(
    final RecordStoreConnectionRegistry registry) {
    final RecordStoreConnectionRegistry oldValue = getForThread();
    RecordStoreConnectionRegistry.threadRegistry.set(registry);
    return oldValue;
  }

  public RecordStoreConnectionRegistry(
    final ConnectionRegistryManager<? extends ConnectionRegistry<RecordStoreConnection>> connectionManager,
    final String name, final boolean visible, final boolean readOnly,
    final Resource directoryResource) {
    super(connectionManager, name, visible, readOnly, directoryResource, "recordStoreConnection");
  }

  protected RecordStoreConnectionRegistry(final RecordStoreConnectionManager connectionManager,
    final String name, final boolean visible) {
    this(connectionManager, name, visible, false, null);
  }

  protected RecordStoreConnectionRegistry(final RecordStoreConnectionManager connectionManager,
    final String name, final Resource resource) {
    this(connectionManager, name, true, false, resource);
  }

  public RecordStoreConnectionRegistry(final String name) {
    this(null, name, true);
  }

  public RecordStoreConnectionRegistry(final String name, final Resource resource,
    final boolean readOnly) {
    this(null, name, true, readOnly, resource);
  }

  @Override
  public RecordStoreConnection addConnection(final MapEx config) {
    final RecordStoreConnection connection = new RecordStoreConnection(this, null, config);
    addConnection(connection);
    return connection;
  }

  public void addConnection(final RecordStoreConnection connection) {
    addConnection(connection.getName(), connection);
  }

  public void addConnection(final String name, final RecordStore recordStore) {
    final RecordStoreConnection connection = new RecordStoreConnection(this, name, recordStore);
    addConnection(connection);
  }

  @Override
  protected RecordStoreConnection loadConnection(final Path connectionFile,
    final boolean importConnection) {
    final MapEx config = Json.toMap(connectionFile);
    final String name = getConnectionName(config, connectionFile, importConnection);
    try {
      @SuppressWarnings({
        "unchecked", "rawtypes"
      })
      final Map<String, Object> connectionProperties = Maps.get((Map)config, "connection",
        Collections.<String, Object> emptyMap());
      if (connectionProperties.isEmpty()) {
        Logs.error(this,
          "Record store must include a 'connection' map property: " + connectionFile);
        return null;
      } else {
        final RecordStoreConnection connection = new RecordStoreConnection(this,
          connectionFile.toString(), config);
        if (!importConnection) {
          connection.setConnectionFile(connectionFile);
        }
        addConnection(name, connection);
        return connection;
      }
    } catch (final Throwable e) {
      Logs.error(this, "Error creating record store from: " + connectionFile, e);
      return null;
    }
  }
}
