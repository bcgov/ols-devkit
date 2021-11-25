package com.revolsys.connection;

import java.nio.file.Path;
import java.util.Map;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.io.FileUtil;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonObjectHash;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;

public abstract class AbstractConnection<C extends Connection, R extends ConnectionRegistry<C>>
  extends BaseObjectWithProperties implements Connection {
  private JsonObject config = new JsonObjectHash();

  private String name;

  private R registry;

  private Path connectionFile;

  public AbstractConnection(final R registry, final String name) {
    this.registry = registry;
    this.name = name;
  }

  public AbstractConnection(final R registry, final String resourceName,
    final Map<String, ? extends Object> config) {
    this.config.putAll(config);
    this.registry = registry;
    setProperties(config);
    if (!Property.hasValue(this.name)) {
      this.name = FileUtil.getBaseName(resourceName);
    }
  }

  @Override
  public void deleteConnection() {
    if (this.registry != null) {
      this.registry.removeConnection(this);
    }
    this.config = null;
    this.name = null;
    this.registry = null;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof AbstractConnection) {
      final AbstractConnection<?, ?> connection = (AbstractConnection<?, ?>)obj;
      if (this.registry == connection.getRegistry()) {
        if (DataType.equal(this.name, connection.getName())) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean equalsConfig(final Connection connection) {
    if (connection != null && getClass().isAssignableFrom(connection.getClass())) {
      final MapEx config = connection.getConfig();
      return DataType.equal(this.config, config);
    }
    return false;
  }

  @Override
  public MapEx getConfig() {
    final MapEx config = new LinkedHashMapEx(this.config);
    config.putAll(getProperties());
    return config;
  }

  @Override
  public Path getConnectionFile() {
    return this.connectionFile;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public R getRegistry() {
    return this.registry;
  }

  @Override
  public int hashCode() {
    if (this.name == null) {
      return 0;
    } else {
      return this.name.hashCode();
    }
  }

  @Override
  public boolean isReadOnly() {
    if (this.registry == null) {
      return true;
    } else {
      return this.registry.isReadOnly();
    }
  }

  public void setConnectionFile(final Path connectionFile) {
    this.connectionFile = connectionFile;
  }

  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public JsonObject toMap() {
    return this.config;
  }

  @Override
  public String toString() {
    return this.name;
  }

  @Override
  public void writeToFile(final Object target) {
    final Resource resource = Resource.getResource(target);
    try {
      this.connectionFile = resource.getPath();
    } catch (final Throwable e) {
      Logs.debug(this, "Error writing:" + resource, e);
    }
    Connection.super.writeToFile(resource);
  }
}
