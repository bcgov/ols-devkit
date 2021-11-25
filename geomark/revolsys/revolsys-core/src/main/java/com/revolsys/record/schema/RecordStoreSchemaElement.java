package com.revolsys.record.schema;

import org.jeometry.common.io.PathName;
import org.jeometry.common.io.PathNameProxy;

import com.revolsys.collection.NameProxy;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.util.IconNameProxy;

public interface RecordStoreSchemaElement extends GeometryFactoryProxy, ObjectWithProperties,
  PathNameProxy, Comparable<RecordStoreSchemaElement>, NameProxy, IconNameProxy {

  default boolean equalPath(final PathName path) {
    return getPathName().equals(path);
  }

  default boolean equalsRecordStore(final RecordStore recordStore) {
    return getRecordStore() == recordStore;
  }

  /**
   * Get the path of the object type. Names are described using a path (e.g.
   * /SCHEMA/TABLE).
   *
   * @return The name.
   */
  String getPath();

  <R extends RecordStore> R getRecordStore();

  <RSS extends RecordStoreSchema> RSS getSchema();

  default boolean isClosed() {
    final RecordStoreSchema schema = getSchema();
    if (schema == null) {
      return true;
    } else {
      return false;
    }
  }

}
