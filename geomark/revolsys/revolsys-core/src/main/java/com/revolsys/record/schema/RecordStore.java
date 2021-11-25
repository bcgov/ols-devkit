package com.revolsys.record.schema;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.date.Dates;
import org.jeometry.common.io.PathName;
import org.jeometry.common.io.PathNameProxy;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.transaction.PlatformTransactionManager;

import com.revolsys.collection.ListResultPager;
import com.revolsys.collection.ResultPager;
import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.jdbc.io.RecordStoreIteratorFactory;
import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.RecordState;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.io.ListRecordReader;
import com.revolsys.record.io.RecordIterator;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordStoreConnection;
import com.revolsys.record.io.RecordStoreFactory;
import com.revolsys.record.io.RecordStoreQueryReader;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;
import com.revolsys.transaction.Transactionable;
import com.revolsys.util.Property;
import com.revolsys.util.count.CategoryLabelCountMap;
import com.revolsys.util.count.LabelCountMap;
import com.revolsys.util.count.LabelCounters;

public interface RecordStore extends GeometryFactoryProxy, RecordDefinitionFactory, Transactionable,
  BaseCloseable, ObjectWithProperties {

  static boolean isRecordStore(final Path path) {
    for (final RecordStoreFactory recordStoreFactory : IoFactory
      .factories(RecordStoreFactory.class)) {
      if (recordStoreFactory.canOpenPath(path)) {
        return true;
      }
    }
    return false;
  }

  static <T extends RecordStore> T newRecordStore(final File file) {
    return newRecordStore(FileUtil.toUrlString(file));
  }

  static <T extends RecordStore> T newRecordStore(final File directory,
    final String fileExtension) {
    if (!directory.exists()) {
      throw new IllegalArgumentException("Directory does not exist: " + directory);
    } else if (!directory.isDirectory()) {
      throw new IllegalArgumentException("File is not a directory: " + directory);
    } else {
      final String url = FileUtil.toUrlString(directory) + "?format=" + fileExtension;
      return newRecordStore(url);
    }
  }

  /**
   * Construct a new initialized record store.
   * @param connectionProperties
   * @return
   */
  @SuppressWarnings("unchecked")
  static <T extends RecordStore> T newRecordStore(
    final Map<String, ? extends Object> connectionProperties) {
    final String url = (String)connectionProperties.get("url");
    final RecordStoreFactory factory = recordStoreFactory(url);
    if (factory == null) {
      throw new IllegalArgumentException("Record Store Factory not found for " + url);
    } else {
      return (T)factory.newRecordStore(connectionProperties);
    }
  }

  static <T extends RecordStore> T newRecordStore(final Path file) {
    return newRecordStore(file.toUri().toString());
  }

  @SuppressWarnings("unchecked")
  static <T extends RecordStore> T newRecordStore(final String url) {
    final RecordStoreFactory factory = recordStoreFactory(url);
    if (factory == null) {
      throw new IllegalArgumentException("Record Store Factory not found for " + url);
    } else {
      final Map<String, Object> connectionProperties = new HashMap<>();
      connectionProperties.put("url", url);
      return (T)factory.newRecordStore(connectionProperties);
    }
  }

  @SuppressWarnings("unchecked")
  static <T extends RecordStore> T newRecordStore(final String url, final String user,
    final String password) {
    final RecordStoreFactory factory = recordStoreFactory(url);
    if (factory == null) {
      throw new IllegalArgumentException("Record Store Factory not found for " + url);
    } else {
      final Map<String, Object> connectionProperties = new HashMap<>();
      connectionProperties.put("url", url);
      connectionProperties.put("user", user);
      connectionProperties.put("password", password);
      return (T)factory.newRecordStore(connectionProperties);
    }
  }

  @SuppressWarnings("unchecked")
  static <T extends RecordStore> T newRecordStoreInitialized(
    final Map<String, ? extends Object> config) {
    final Map<String, Object> connectionProperties = (Map<String, Object>)config.get("connection");
    if (Property.isEmpty(connectionProperties)) {
      throw new IllegalArgumentException(
        "Record store must include a 'connection' map property: " + config);
    } else {
      long startTime = System.currentTimeMillis();
      final RecordStore recordStore = RecordStore.newRecordStore(connectionProperties);
      Dates.debugEllapsedTime(RecordStore.class, "new", startTime);
      recordStore.setProperties(config);
      Dates.debugEllapsedTime(RecordStore.class, "setProperties", startTime);
      recordStore.initialize();
      startTime = Dates.debugEllapsedTime(RecordStore.class, "init", startTime);
      return (T)recordStore;
    }
  }

  static RecordStoreFactory recordStoreFactory(final String url) {
    if (url == null) {
      throw new IllegalArgumentException("The url parameter must be specified");
    } else {
      final List<RecordStoreFactory> factories = IoFactory.factories(RecordStoreFactory.class);
      for (final RecordStoreFactory factory : factories) {
        final boolean canOpenUrl = factory.canOpenUrl(url);
        if (canOpenUrl) {
          return factory;
        }
      }
      return null;
    }
  }

  static Class<?> recordStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    final String url = (String)connectionProperties.get("url");
    final RecordStoreFactory factory = recordStoreFactory(url);
    if (factory == null) {
      throw new IllegalArgumentException("Data Source Factory not found for " + url);
    } else {
      return factory.getRecordStoreInterfaceClass(connectionProperties);
    }
  }

  static void setConnectionProperties(final RecordStore recordStore,
    final Map<String, Object> properties) {
    if (recordStore != null) {
      final DirectFieldAccessor dataSourceBean = new DirectFieldAccessor(recordStore);
      for (final Entry<String, Object> property : properties.entrySet()) {
        final String name = property.getKey();
        final Object value = property.getValue();
        try {
          dataSourceBean.setPropertyValue(name, value);
        } catch (final Throwable e) {
        }
      }
    }
  }

  void addCodeTable(CodeTable codeTable);

  void addCodeTable(String fieldName, CodeTable codeTable);

  default void addCodeTables(final Collection<CodeTable> codeTables) {
    for (final CodeTable codeTable : codeTables) {
      addCodeTable(codeTable);
    }
  }

  RecordStore addRecordDefinitionInitializer(PathName tableName, Consumer<RecordDefinition> action);

  default void addStatistic(final String statisticName, final Record object) {
    final CategoryLabelCountMap statistics = getStatistics();
    if (statistics != null) {
      statistics.addCount(statisticName, object);
    }
  }

  default void addStatistic(final String statisticName, final String typePath, final int count) {
    final CategoryLabelCountMap statistics = getStatistics();
    if (statistics != null) {
      statistics.addCount(statisticName, typePath, count);
    }
  }

  default void appendQueryValue(final Query query, final StringBuilder sql,
    final QueryValue queryValue) {
    queryValue.appendDefaultSql(query, this, sql);
  }

  default void appendSelect(final Query query, final Appendable sql, final QueryValue queryValue) {
    queryValue.appendDefaultSelect(query, this, sql);
  }

  @Override
  void close();

  default boolean deleteRecord(final PathName typePath, final Identifier identifier) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition != null) {
      final String idFieldName = recordDefinition.getIdFieldName();
      if (idFieldName != null) {
        final Query query = Query.equal(recordDefinition, idFieldName, identifier);
        if (deleteRecords(query) == 1) {
          return true;
        }
      }
    }
    return false;
  }

  default boolean deleteRecord(final PathName typePath, final Object identifier) {
    final Identifier id = Identifier.newIdentifier(identifier);
    return deleteRecord(typePath, id);
  }

  default boolean deleteRecord(final Record record) {
    throw new UnsupportedOperationException("Delete not supported");
  }

  default int deleteRecords(final Iterable<? extends Record> records) {
    int count = 0;
    for (final Record record : records) {
      if (deleteRecord(record)) {
        count++;
      }
    }
    return count;
  }

  default int deleteRecords(final Query query) {
    int count = 0;
    try (
      final RecordReader reader = getRecords(query)) {
      for (final Record record : reader) {
        if (deleteRecord(record)) {
          count++;
        }
      }
    }
    return count;
  }

  default RecordDefinition findRecordDefinition(final PathName typePath) {
    final PathName schemaName = typePath.getParent();
    final RecordStoreSchema schema = getSchema(schemaName);
    if (schema == null) {
      return null;
    } else {
      return schema.findRecordDefinition(typePath);
    }
  }

  default <CT extends CodeTable> CT getCodeTable(final PathName typePath) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getCodeTable();
    }
  }

  default <V extends CodeTable> V getCodeTable(final String typePath) {
    final PathName pathName = PathName.newPathName(typePath);
    return getCodeTable(pathName);
  }

  CodeTable getCodeTableByFieldName(CharSequence fieldName);

  Map<String, CodeTable> getCodeTableByFieldNameMap();

  MapEx getConnectionProperties();

  String getConnectionTitle();

  RecordStoreIteratorFactory getIteratorFactory();

  String getLabel();

  default Record getRecord(final PathName typePath, final Condition condition) {
    return getRecord(typePath, condition, null);
  }

  default Record getRecord(final PathName typePath, final Condition condition,
    final LockMode lockMode) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    final Query query = new Query(recordDefinition)//
      .setWhereCondition(condition)
      .setLockMode(lockMode);
    return getRecord(query);
  }

  default Record getRecord(final PathName typePath, final Identifier id) {
    final Query query = newGetRecordQuery(typePath, id);
    if (query == null) {
      return null;
    } else {
      final RecordReader records = getRecords(query);
      return records.getFirst();
    }
  }

  default Record getRecord(final PathName typePath, final Object... id) {
    final Identifier identifier = Identifier.newIdentifier(id);
    return getRecord(typePath, identifier);
  }

  default Record getRecord(final Query query) {
    Record firstRecord = null;
    try (
      RecordReader records = getRecords(query)) {
      for (final Record record : records) {
        if (firstRecord == null) {
          firstRecord = record;
        } else {
          throw new IllegalArgumentException("Query matched multiple objects\n" + query);
        }
      }
    }
    return firstRecord;
  }

  int getRecordCount(Query query);

  default <RD extends RecordDefinition> RD getRecordDefinition(final PathName path) {
    if (path == null) {
      return null;
    } else {
      final PathName schemaPath = path.getParent();
      final RecordStoreSchema schema = getSchema(schemaPath);
      if (schema == null) {
        return null;
      } else {
        return schema.getRecordDefinition(path);
      }
    }
  }

  default RecordDefinition getRecordDefinition(final PathNameProxy path) {
    if (path == null) {
      return null;
    } else {
      final PathName pathName = path.getPathName();
      return getRecordDefinition(pathName);
    }
  }

  default <RD extends RecordDefinition> RD getRecordDefinition(
    final RecordDefinition objectRecordDefinition) {
    final String typePath = objectRecordDefinition.getPath();
    return getRecordDefinition(typePath);
  }

  default <RD extends RecordDefinition> RD getRecordDefinition(
    final RecordDefinitionProxy recordDefinition) {
    if (recordDefinition != null) {
      final RecordDefinition rd = recordDefinition.getRecordDefinition();
      return getRecordDefinition(rd);
    }
    return null;
  }

  @Override
  default <RD extends RecordDefinition> RD getRecordDefinition(final String path) {
    final PathName pathName = PathName.newPathName(path);
    return getRecordDefinition(pathName);
  }

  default List<RecordDefinition> getRecordDefinitions(final PathName path) {
    final RecordStoreSchema schema = getSchema(path);
    if (schema == null) {
      return Collections.emptyList();
    } else {
      return schema.getRecordDefinitions();
    }
  }

  RecordFactory<Record> getRecordFactory();

  default Record getRecordLocked(final PathName typePath, final LockMode lockMode,
    final Identifier id) {
    final Query query = newGetRecordQuery(typePath, id);
    if (query == null) {
      return null;
    } else {
      query.setLockMode(lockMode);
      final RecordReader records = getRecords(query);
      return records.getFirst();
    }
  }

  default RecordReader getRecords(final Collection<Query> queries) {
    final RecordStoreQueryReader reader = newRecordReader();
    for (final Query query : queries) {
      if (query != null) {
        reader.addQuery(query);
      }
    }
    return reader;
  }

  default RecordReader getRecords(final PathName path) {
    final RecordStoreSchemaElement element = getRootSchema().getElement(path);
    if (element instanceof RecordDefinition) {
      final RecordDefinition recordDefinition = (RecordDefinition)element;
      final Query query = new Query(recordDefinition);
      return getRecords(query);
    } else if (element instanceof RecordStoreSchema) {
      final RecordStoreSchema schema = (RecordStoreSchema)element;
      final List<Query> queries = new ArrayList<>();
      for (final RecordDefinition recordDefinition : schema.getRecordDefinitions()) {
        final Query query = new Query(recordDefinition);
        queries.add(query);
      }
      return getRecords(queries);
    } else {
      return new ListRecordReader(null, Collections.emptyList());
    }
  }

  default RecordReader getRecords(final Query query) {
    final RecordStoreQueryReader reader = newRecordReader();
    reader.addQuery(query);
    return reader;
  }

  RecordStoreConnection getRecordStoreConnection();

  String getRecordStoreType();

  <RSS extends RecordStoreSchema> RSS getRootSchema();

  default <RSS extends RecordStoreSchema> RSS getSchema(final PathName pathName) {
    final RecordStoreSchema rootSchema = getRootSchema();
    return rootSchema.getSchema(pathName);
  }

  default <RSS extends RecordStoreSchema> RSS getSchema(final String path) {
    return getSchema(PathName.newPathName(path));
  }

  CategoryLabelCountMap getStatistics();

  default LabelCounters getStatistics(final String name) {
    final CategoryLabelCountMap statistics = getStatistics();
    if (statistics == null) {
      return null;
    } else {
      return statistics.getLabelCountMap(name);
    }
  }

  @Override
  default PlatformTransactionManager getTransactionManager() {
    return null;
  }

  String getUrl();

  String getUsername();

  default boolean hasSchema(final PathName schemaName) {
    return getSchema(schemaName) != null;
  }

  void initialize();

  void initializeRecordDefinition(RecordDefinition recordDefinition);

  default Record insertRecord(final PathName pathName, final Object... values) {
    final RecordDefinition recordDefinition = getRecordDefinition(pathName);
    final Record record = new ArrayRecord(recordDefinition, values);
    insertRecord(record);
    return record;
  }

  default void insertRecord(final Record record) {
    throw new UnsupportedOperationException("Insert not supported");
  }

  default void insertRecords(final Iterable<? extends Record> records) {
    for (final Record record : records) {
      insertRecord(record);
    }
  }

  default boolean isClosed() {
    return false;
  }

  default boolean isEditable(final PathName typePath) {
    return false;
  }

  boolean isLoadFullSchema();

  default Query newGetRecordQuery(final PathName typePath, final Identifier id) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition == null || id == null) {
      return null;
    } else {
      final List<String> idFieldNames = recordDefinition.getIdFieldNames();
      if (idFieldNames.isEmpty()) {
        throw new IllegalArgumentException(typePath + " does not have a primary key");
      } else if (id.getValueCount() != idFieldNames.size()) {
        throw new IllegalArgumentException(
          id + " not a valid id for " + typePath + " requires " + idFieldNames);
      } else {
        final Query query = new Query(recordDefinition);
        for (int i = 0; i < idFieldNames.size(); i++) {
          final String name = idFieldNames.get(i);
          final Object value = id.getValue(i);
          final FieldDefinition field = recordDefinition.getField(name);
          query.and(Q.equal(field, value));
        }
        return query;
      }
    }
  }

  default RecordIterator newIterator(final Query query, Map<String, Object> properties) {
    if (properties == null) {
      properties = Collections.emptyMap();
    }
    if (query == null) {
      return null;
    } else {
      final RecordDefinition recordDefinition = query.getRecordDefinition();
      if (recordDefinition != null) {
        final RecordStoreIteratorFactory recordStoreIteratorFactory = recordDefinition
          .getProperty("recordStoreIteratorFactory");
        if (recordStoreIteratorFactory != null) {
          final RecordIterator iterator = recordStoreIteratorFactory.newIterator(this, query,
            properties);
          if (iterator != null) {
            return iterator;
          }
        }
      }
      final RecordStoreIteratorFactory iteratorFactory = getIteratorFactory();
      return iteratorFactory.newIterator(this, query, properties);
    }
  }

  default Identifier newPrimaryIdentifier(final PathName typePath) {
    return null;
  }

  default Query newQuery(final PathName pathName) {
    final RecordDefinition recordDefinition = getRecordDefinition(pathName);
    if (recordDefinition == null) {
      throw new IllegalArgumentException("Cannot find table: " + pathName);
    } else {
      return new Query(recordDefinition);
    }
  }

  default Query newQuery(final String typeName) {
    final PathName typePath = PathName.newPathName(typeName);
    return new Query(typePath);
  }

  default Query newQuery(final String typePath, final String whereClause,
    final BoundingBox boundingBox) {
    throw new UnsupportedOperationException();
  }

  default Record newRecord(final PathName typePath) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition == null) {
      return null;
    } else {
      return newRecord(recordDefinition);
    }
  }

  default Record newRecord(final PathName typePath, final Map<String, ? extends Object> values) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition == null) {
      throw new IllegalArgumentException("Cannot find table " + typePath + " for " + this);
    } else {
      final Record record = newRecord(recordDefinition);
      if (record != null) {
        record.setValues(values);
        final String idFieldName = recordDefinition.getIdFieldName();
        if (Property.hasValue(idFieldName)) {
          if (values.get(idFieldName) == null) {
            final Identifier id = newPrimaryIdentifier(typePath);
            record.setIdentifier(id);
          }
        }
      }
      return record;
    }
  }

  default <R extends Record> R newRecord(final PathName typePath,
    final RecordFactory<R> recordFactory) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition == null) {
      return null;
    } else {
      return newRecord(recordDefinition, recordFactory);
    }
  }

  default Record newRecord(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final RecordDefinition recordStoreRecordDefinition = getRecordDefinition(recordDefinition);
    final RecordFactory<Record> recordFactory = getRecordFactory();
    if (recordStoreRecordDefinition == null || recordFactory == null) {
      return null;
    } else {
      final Record copy = recordFactory.newRecord(recordStoreRecordDefinition);
      copy.setValuesClone(record);
      copy.setIdentifier(null);
      return copy;
    }
  }

  default Record newRecord(final RecordDefinition objectRecordDefinition) {
    final RecordFactory<Record> recordFactory = getRecordFactory();
    return newRecord(objectRecordDefinition, recordFactory);
  }

  default Record newRecord(RecordDefinition recordDefinition,
    final Map<String, ? extends Object> values) {
    final PathName typePath = recordDefinition.getPathName();
    recordDefinition = getRecordDefinition(recordDefinition);
    if (recordDefinition == null) {
      throw new IllegalArgumentException("Cannot find table " + typePath + " for " + this);
    } else {
      final Record record = newRecord(recordDefinition);
      if (record != null) {
        record.setValues(values);
        final String idFieldName = recordDefinition.getIdFieldName();
        if (Property.hasValue(idFieldName)) {
          if (values.get(idFieldName) == null) {
            final Identifier id = newPrimaryIdentifier(typePath);
            record.setIdentifier(id);
          }
        }
      }
      return record;
    }
  }

  default <R extends Record> R newRecord(final RecordDefinition objectRecordDefinition,
    final RecordFactory<R> recordFactory) {
    final RecordDefinition recordDefinition = getRecordDefinition(objectRecordDefinition);
    if (recordDefinition == null || recordFactory == null) {
      return null;
    } else {
      return recordFactory.newRecord(recordDefinition);
    }
  }

  default RecordStoreQueryReader newRecordReader() {
    return new RecordStoreQueryReader(this);
  }

  default Record newRecordWithIdentifier(final RecordDefinition recordDefinition) {
    final Record record = newRecord(recordDefinition);
    if (record != null) {
      final String idFieldName = recordDefinition.getIdFieldName();
      if (Property.hasValue(idFieldName)) {
        final PathName typePath = recordDefinition.getPathName();
        final Identifier id = newPrimaryIdentifier(typePath);
        record.setIdentifier(id);
      }
    }
    return record;
  }

  default RecordWriter newRecordWriter() {
    return newRecordWriter(false);
  }

  RecordWriter newRecordWriter(final boolean throwExceptions);

  default RecordWriter newRecordWriter(final PathName pathName) {
    final RecordDefinition recordDefinition = getRecordDefinition(pathName);
    return newRecordWriter(recordDefinition);
  }

  default RecordWriter newRecordWriter(final RecordDefinitionProxy recordDefinition) {
    return newRecordWriter();
  }

  default ResultPager<Record> page(final Query query) {
    final RecordReader results = getRecords(query);
    final List<Record> list = results.toList();
    final RecordDefinition recordDefinition = query.getRecordDefinition();
    return new ListResultPager<>(recordDefinition, list);
  }

  default void refreshCodeTable(final PathName pathName) {
    final CodeTable codeTable = getCodeTable(pathName);
    if (codeTable != null) {
      codeTable.refresh();
    }
  }

  void setLabel(String label);

  void setLoadFullSchema(boolean loadFullSchema);

  void setRecordFactory(RecordFactory<? extends Record> recordFactory);

  void setRecordStoreConnection(RecordStoreConnection connection);

  default void setStatistics(final String name, final LabelCountMap labelCountMap) {
    final CategoryLabelCountMap categoryLabelCountMap = getStatistics();
    if (categoryLabelCountMap != null) {
      categoryLabelCountMap.setLabelCounters(name, labelCountMap);
    }
  }

  default void updateRecord(final Record record) {
    write(record, null);
  }

  default void updateRecords(final Iterable<? extends Record> records) {
    writeAll(records, null);
  }

  default void write(final Record record, final RecordState state) {
    try (
      Transaction transaction = newTransaction(com.revolsys.transaction.Propagation.REQUIRED)) {
      // It's important to have this in an inner try. Otherwise the exceptions
      // won't get caught on closing the writer and the transaction won't get
      // rolled back.
      try (
        RecordWriter writer = newRecordWriter(true)) {
        write(writer, record, state);
      } catch (final RuntimeException e) {
        transaction.setRollbackOnly();
        throw e;
      } catch (final Error e) {
        transaction.setRollbackOnly();
        throw e;
      }
    }
  }

  default Record write(final RecordWriter writer, Record record, final RecordState state) {
    if (state == RecordState.NEW) {
      if (record.getState() != state) {
        record = newRecord(record);
      }
    } else if (state != null) {
      record.setState(state);
    }
    writer.write(record);
    return record;
  }

  default int writeAll(final Iterable<? extends Record> records, final RecordState state) {
    int count = 0;
    try (
      Transaction transaction = newTransaction(Propagation.REQUIRED)) {
      // It's important to have this in an inner try. Otherwise the exceptions
      // won't get caught on closing the writer and the transaction won't get
      // rolled back.
      try (
        final RecordWriter writer = newRecordWriter(true)) {
        for (final Record record : records) {
          write(writer, record, state);
          count++;
        }
      } catch (final RuntimeException e) {
        transaction.setRollbackOnly();
        throw e;
      } catch (final Error e) {
        transaction.setRollbackOnly();
        throw e;
      }
    }
    return count;
  }
}
