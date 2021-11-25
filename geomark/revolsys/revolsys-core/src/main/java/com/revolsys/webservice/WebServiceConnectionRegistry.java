package com.revolsys.webservice;

import java.nio.file.Path;

import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.MapEx;
import com.revolsys.connection.AbstractConnectionRegistry;
import com.revolsys.connection.ConnectionRegistry;
import com.revolsys.connection.ConnectionRegistryManager;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.spring.resource.Resource;

public class WebServiceConnectionRegistry extends AbstractConnectionRegistry<WebServiceConnection> {
  private static final ThreadLocal<WebServiceConnectionRegistry> threadRegistry = new ThreadLocal<>();

  public static WebServiceConnectionRegistry getForThread() {
    return WebServiceConnectionRegistry.threadRegistry.get();
  }

  public static WebServiceConnectionRegistry setForThread(
    final WebServiceConnectionRegistry registry) {
    final WebServiceConnectionRegistry oldValue = getForThread();
    WebServiceConnectionRegistry.threadRegistry.set(registry);
    return oldValue;
  }

  public WebServiceConnectionRegistry(
    final ConnectionRegistryManager<? extends ConnectionRegistry<WebServiceConnection>> connectionManager,
    final String name, final boolean visible, final boolean readOnly,
    final Resource directoryResource) {
    super(connectionManager, name, visible, readOnly, directoryResource, "webServiceConnection");
  }

  public WebServiceConnectionRegistry(final String name) {
    this(null, name, true);
  }

  public WebServiceConnectionRegistry(final String name, final Resource resource,
    final boolean readOnly) {
    this(null, name, true, readOnly, resource);
  }

  protected WebServiceConnectionRegistry(final WebServiceConnectionManager connectionManager,
    final String name, final boolean visible) {
    this(connectionManager, name, visible, false, null);
  }

  protected WebServiceConnectionRegistry(final WebServiceConnectionManager connectionManager,
    final String name, final Resource resource) {
    this(connectionManager, name, true, false, resource);
  }

  @Override
  public WebServiceConnection addConnection(final MapEx config) {
    getConnectionName(config, null, true);
    final WebServiceConnection connection = new WebServiceConnection(this, config);
    addConnection(connection);
    return connection;
  }

  public void addConnection(final String name, final WebService<?> webService) {
    final WebServiceConnection connection = new WebServiceConnection(this, name, webService);
    addConnection(connection);
  }

  public void addConnection(final WebServiceConnection connection) {
    final String name = connection.getName();
    addConnection(name, connection);
  }

  @Override
  public String getIconName() {
    return "folder:world";
  }

  @Override
  protected WebServiceConnection loadConnection(final Path connectionFile,
    final boolean importConnection) {
    final MapEx config = Json.toMap(connectionFile);
    final String name = getConnectionName(config, connectionFile, importConnection);
    try {
      final WebServiceConnection connection = new WebServiceConnection(this, config);
      if (!importConnection) {
        connection.setConnectionFile(connectionFile);
      }
      addConnection(name, connection);
      return connection;
    } catch (final Throwable e) {
      Logs.error(this, "Error creating web service from: " + connectionFile, e);
      return null;
    }
  }

}
