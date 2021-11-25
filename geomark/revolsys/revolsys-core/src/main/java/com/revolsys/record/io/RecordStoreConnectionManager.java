package com.revolsys.record.io;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.revolsys.connection.AbstractConnectionRegistryManager;
import com.revolsys.io.FileUtil;
import com.revolsys.io.file.Paths;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.BaseCloneable;
import com.revolsys.util.OS;
import com.revolsys.util.Property;

public class RecordStoreConnectionManager
  extends AbstractConnectionRegistryManager<RecordStoreConnectionRegistry, RecordStoreConnection> {

  private static final RecordStoreConnectionManager INSTANCE;

  // TODO make this garbage collectible with reference counting.
  private static Map<Map<String, Object>, RecordStore> recordStoreByConfig = new HashMap<>();

  private static Map<Map<String, Object>, AtomicInteger> recordStoreCounts = new HashMap<>();

  static {
    INSTANCE = new RecordStoreConnectionManager();
    final File recordStoresDirectory = OS
      .getApplicationDataDirectory("com.revolsys.gis/Record Stores");
    INSTANCE.addConnectionRegistry("User", new PathResource(recordStoresDirectory));
  }

  private static BiFunction<RecordStoreConnection, Throwable, Boolean> invalidRecordStoreFunction;

  private static Function<String, RecordStore> missingRecordStoreFunction;

  public static RecordStoreConnectionManager get() {
    return INSTANCE;
  }

  public static BiFunction<RecordStoreConnection, Throwable, Boolean> getInvalidRecordStoreFunction() {
    return RecordStoreConnectionManager.invalidRecordStoreFunction;
  }

  public static <V extends RecordStore> V getRecordStore(final File file) {
    final Map<String, String> connectionProperties = Collections.singletonMap("url",
      FileUtil.toUrlString(file));
    final Map<String, Object> config = Collections.<String, Object> singletonMap("connection",
      connectionProperties);
    return getRecordStore(config);
  }

  /**
   * Get an initialized record store.
   * @param connectionProperties
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T extends RecordStore> T getRecordStore(
    final Map<String, ? extends Object> config) {
    @SuppressWarnings("rawtypes")
    final Map<String, Object> configClone = (Map)BaseCloneable.clone(config);
    synchronized (recordStoreByConfig) {
      RecordStore recordStore = recordStoreByConfig.get(configClone);
      if (recordStore != null && recordStore.isClosed()) {
        recordStoreByConfig.remove(configClone);
        recordStoreCounts.remove(configClone);
        recordStore = null;
      }
      if (recordStore == null) {
        final Map<String, ? extends Object> connectionProperties = (Map<String, ? extends Object>)configClone
          .get("connection");
        final String name = (String)connectionProperties.get("name");
        if (Property.hasValue(name)) {
          recordStore = getRecordStore(name);
          if (recordStore == null) {
            // TODO give option to add
            return null;
          }
        } else {
          recordStore = RecordStore.newRecordStore(connectionProperties);
          if (recordStore == null) {
            return null;
          } else {
            recordStore.setProperties(config);
            recordStore.initialize();
          }
        }
        recordStoreByConfig.put(configClone, recordStore);
        recordStoreCounts.put(configClone, new AtomicInteger(1));
      } else {
        final AtomicInteger count = recordStoreCounts.get(configClone);
        count.incrementAndGet();
      }
      return (T)recordStore;
    }
  }

  public static <V extends RecordStore> V getRecordStore(final Path path) {
    final Map<String, String> connectionProperties = Collections.singletonMap("url",
      Paths.toUrlString(path));
    final Map<String, Object> config = Collections.<String, Object> singletonMap("connection",
      connectionProperties);
    return getRecordStore(config);
  }

  public static RecordStore getRecordStore(final String name) {
    final RecordStoreConnectionManager connectionManager = get();
    final List<RecordStoreConnectionRegistry> registries = new ArrayList<>();
    registries.addAll(connectionManager.getConnectionRegistries());
    final RecordStoreConnectionRegistry threadRegistry = RecordStoreConnectionRegistry
      .getForThread();
    if (threadRegistry != null) {
      registries.add(threadRegistry);
    }
    Collections.reverse(registries);
    for (final RecordStoreConnectionRegistry registry : registries) {
      final RecordStoreConnection recordStoreConnection = registry.getConnection(name);
      if (recordStoreConnection != null) {
        return recordStoreConnection.getRecordStore();
      }
    }
    if (missingRecordStoreFunction == null) {
      return null;
    } else {
      return missingRecordStoreFunction.apply(name);
    }
  }

  @SuppressWarnings("unchecked")
  public static void releaseRecordStore(final Map<String, ? extends Object> config) {
    @SuppressWarnings("rawtypes")
    final Map<String, Object> configClone = (Map)BaseCloneable.clone(config);
    synchronized (recordStoreByConfig) {
      final RecordStore recordStore = recordStoreByConfig.get(configClone);
      if (recordStore != null) {
        final AtomicInteger count = recordStoreCounts.get(configClone);
        if (count.decrementAndGet() == 0) {
          final Map<String, ? extends Object> connectionProperties = (Map<String, ? extends Object>)configClone
            .get("connection");
          final String name = (String)connectionProperties.get("name");
          if (!Property.hasValue(name)) {
            // TODO release for connections from connection registries
            recordStore.close();
          }
          recordStoreByConfig.remove(configClone);
          recordStoreCounts.remove(configClone);
        }
      }
    }
  }

  public static void setInvalidRecordStoreFunction(
    final BiFunction<RecordStoreConnection, Throwable, Boolean> invalidRecordStoreFunction) {
    RecordStoreConnectionManager.invalidRecordStoreFunction = invalidRecordStoreFunction;
  }

  public static void setMissingRecordStoreFunction(
    final Function<String, RecordStore> missingRecordStoreFunction) {
    RecordStoreConnectionManager.missingRecordStoreFunction = missingRecordStoreFunction;
  }

  public RecordStoreConnectionManager() {
    super("Record Stores");
  }

  public RecordStoreConnectionRegistry addConnectionRegistry(final String name,
    final boolean visible) {
    final RecordStoreConnectionRegistry registry = new RecordStoreConnectionRegistry(this, name,
      visible);
    addConnectionRegistry(registry);
    return registry;
  }

  public RecordStoreConnectionRegistry addConnectionRegistry(final String name,
    final Resource recordStoresDirectory) {
    final RecordStoreConnectionRegistry registry = new RecordStoreConnectionRegistry(this, name,
      recordStoresDirectory);
    addConnectionRegistry(registry);
    return registry;
  }

  @Override
  public String getIconName() {
    return "folder:database";
  }

}
