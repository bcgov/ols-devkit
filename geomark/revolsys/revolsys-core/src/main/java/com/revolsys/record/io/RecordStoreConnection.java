package com.revolsys.record.io;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.collection.Parent;
import com.revolsys.collection.map.MapEx;
import com.revolsys.connection.AbstractConnection;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.record.schema.RecordStoreSchema;
import com.revolsys.record.schema.RecordStoreSchemaElement;
import com.revolsys.util.Property;

public class RecordStoreConnection
  extends AbstractConnection<RecordStoreConnection, RecordStoreConnectionRegistry>
  implements Parent<RecordStoreSchemaElement> {
  private RecordStore recordStore;

  private boolean savePassword;

  public RecordStoreConnection(final RecordStoreConnectionRegistry registry,
    final String resourceName, final Map<String, ? extends Object> config) {
    super(registry, resourceName, config);
    final String type = MapObjectFactory.getType(this);
    if (Property.isEmpty(type)) {
      setProperty(MapObjectFactory.TYPE, "recordStore");
    }
  }

  public RecordStoreConnection(final RecordStoreConnectionRegistry registry, final String name,
    final RecordStore recordStore) {
    super(registry, name);
    this.recordStore = recordStore;
  }

  @Override
  public void deleteConnection() {
    super.deleteConnection();
    this.recordStore = null;
  }

  @Override
  public List<RecordStoreSchemaElement> getChildren() {
    final RecordStore recordStore = getRecordStore();
    if (recordStore != null) {
      final RecordStoreSchema rootSchema = recordStore.getRootSchema();
      if (rootSchema != null) {
        return rootSchema.getElements();
      }
    }
    return Collections.emptyList();
  }

  @Override
  public String getIconName() {
    return "database";
  }

  public RecordStore getRecordStore() {
    synchronized (this) {
      if (this.recordStore == null || this.recordStore.isClosed()) {
        this.recordStore = null;
        final BiFunction<RecordStoreConnection, Throwable, Boolean> invalidRecordStoreFunction = RecordStoreConnectionManager
          .getInvalidRecordStoreFunction();
        Throwable savedException = null;
        do {
          try {
            this.recordStore = newRecordStore();
            this.recordStore.setRecordStoreConnection(this);
            return this.recordStore;
          } catch (final Throwable e) {
            savedException = e;
          }
        } while (invalidRecordStoreFunction != null
          && invalidRecordStoreFunction.apply(this, savedException));
        Exceptions.throwUncheckedException(savedException);
      }
    }
    return this.recordStore;
  }

  public boolean isSavePassword() {
    return this.savePassword;
  }

  public RecordStore newRecordStore() {
    final MapEx config = toMapInternal();
    return MapObjectFactory.toObject(config);
  }

  @Override
  public void refresh() {
    final RecordStore recordStore = getRecordStore();
    if (recordStore != null) {
      final RecordStoreSchema rootSchema = recordStore.getRootSchema();
      if (rootSchema != null) {
        rootSchema.refresh();
      }
    }
  }

  public void setSavePassword(final boolean savePassword) {
    this.savePassword = savePassword;
  }

  @SuppressWarnings("unchecked")
  @Override
  public JsonObject toMap() {
    final JsonObject map = toMapInternal();
    if (!isSavePassword()) {
      final Map<String, Object> connection = (Map<String, Object>)map.get("connection");
      if (connection != null) {
        connection.remove("password");
      }
    }
    return map;
  }

  protected JsonObject toMapInternal() {
    final JsonObject map = newTypeMap("recordStore");
    addAllToMap(map, getProperties());
    final String name = getName();
    map.put("name", name);
    final boolean savePassword = isSavePassword();
    map.put("savePassword", savePassword);
    return map;
  }
}
