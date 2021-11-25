package com.revolsys.record.schema;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.io.PathName;
import org.jeometry.common.io.PathNameProxy;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Query;
import com.revolsys.util.IconNameProxy;

public interface RecordDefinitionProxy extends PathNameProxy, IconNameProxy, GeometryFactoryProxy {
  default int getFieldCount() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return 0;
    } else {
      return recordDefinition.getFieldCount();
    }
  }

  default FieldDefinition getFieldDefinition(final CharSequence fieldName) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getField(fieldName);
    }
  }

  default FieldDefinition getFieldDefinition(final int fieldIndex) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getField(fieldIndex);
    }
  }

  default List<FieldDefinition> getFieldDefinitions() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return Collections.emptyList();
    } else {
      return recordDefinition.getFields();
    }
  }

  default int getFieldIndex(final CharSequence fieldName) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null || fieldName == null) {
      return -1;
    } else {
      return recordDefinition.getFieldIndex(fieldName.toString());
    }
  }

  default String getFieldName(final int fieldIndex) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getFieldName(fieldIndex);
    }
  }

  default List<String> getFieldNames() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return Collections.emptyList();
    } else {
      return recordDefinition.getFieldNames();
    }
  }

  default String getFieldTitle(final String name) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return name;
    } else {
      return recordDefinition.getFieldTitle(name);
    }
  }

  @Override
  default GeometryFactory getGeometryFactory() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return GeometryFactory.DEFAULT_3D;
    } else {
      return recordDefinition.getGeometryFactory();
    }
  }

  default FieldDefinition getGeometryField() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getGeometryField();
    }
  }

  default String getGeometryFieldName() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getGeometryFieldName();
    }
  }

  @Override
  default String getIconName() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return "table";
    } else {
      return recordDefinition.getIconName();
    }
  }

  default String getIdFieldName() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getIdFieldName();
    }
  }

  default List<String> getIdFieldNames() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return Collections.emptyList();
    } else {
      return recordDefinition.getIdFieldNames();
    }
  }

  @Override
  default PathName getPathName() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getPathName();
    }
  }

  default Record getRecord(final Condition condition) {
    return getRecord(condition, null);
  }

  default Record getRecord(final Condition condition, final LockMode lockMode) {
    final PathName pathName = getPathName();
    final RecordStore recordStore = getRecordStore();
    if (recordStore == null) {
      throw new IllegalStateException(String.format("%s doesn't have a record store", pathName));
    } else {
      final Query query = newQuery();
      if (condition != null) {
        query.and(condition);
      }
      if (lockMode != null) {
        query.setLockMode(lockMode);
      }
      return getRecord(query);
    }
  }

  default Record getRecord(final Identifier id) {
    final PathName pathName = getPathName();
    final RecordStore recordStore = getRecordStore();
    if (recordStore == null) {
      throw new IllegalStateException(String.format("%s doesn't have a record store", pathName));
    } else {
      return recordStore.getRecord(pathName, id);
    }
  }

  default Record getRecord(final Object id) {
    final PathName pathName = getPathName();
    final RecordStore recordStore = getRecordStore();
    if (recordStore == null) {
      throw new IllegalStateException(String.format("%s doesn't have a record store", pathName));
    } else {
      return recordStore.getRecord(pathName, id);
    }
  }

  default Record getRecord(final Query query) {
    final PathName pathName = getPathName();
    final RecordStore recordStore = getRecordStore();
    if (recordStore == null) {
      throw new IllegalStateException(String.format("%s doesn't have a record store", pathName));
    } else {
      return recordStore.getRecord(query);
    }
  }

  default Record getRecord(final String id) {
    final PathName pathName = getPathName();
    final RecordStore recordStore = getRecordStore();
    if (recordStore == null) {
      throw new IllegalStateException(String.format("%s doesn't have a record store", pathName));
    } else {
      return recordStore.getRecord(pathName, id);
    }
  }

  RecordDefinition getRecordDefinition();

  default <R extends Record> RecordFactory<R> getRecordFactory() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getRecordFactory();
    }
  }

  default <R extends RecordStore> R getRecordStore() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getRecordStore();
    }
  }

  /**
   * Checks to see if the definition for this record has a field with the
   * specified name.
   *
   * @param name The name of the field.
   * @return True if the record has a field with the specified name.
   */
  default boolean hasField(final CharSequence name) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return false;
    } else {
      return recordDefinition.hasField(name);
    }
  }

  default boolean hasIdField() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return false;
    } else {
      return recordDefinition.hasIdField();
    }
  }

  default boolean isGeometryField(final int fieldIndex) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return false;
    } else {
      return recordDefinition.getGeometryFieldIndexes().contains(fieldIndex);
    }
  }

  default boolean isGeometryField(final String fieldName) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (fieldName == null || recordDefinition == null) {
      return false;
    } else {
      return recordDefinition.getGeometryFieldNames().contains(fieldName);
    }
  }

  default boolean isIdField(final CharSequence fieldName) {
    return isIdField(fieldName.toString());
  }

  default boolean isIdField(final int fieldIndex) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return false;
    } else {
      return recordDefinition.isIdField(fieldIndex);
    }
  }

  default boolean isIdField(final String fieldName) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (fieldName == null || recordDefinition == null) {
      return false;
    } else {
      return recordDefinition.isIdField(fieldName);
    }
  }

  default Query newQuery() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return new Query(recordDefinition);
  }

  default RecordDefinition newRecordDefinition(final Collection<String> fieldNames) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return new RecordDefinitionBuilder(recordDefinition, fieldNames)//
        .getRecordDefinition()//
      ;
    }
  }

}
