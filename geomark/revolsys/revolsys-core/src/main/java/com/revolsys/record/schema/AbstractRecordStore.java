package com.revolsys.record.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import org.jeometry.common.function.Consumer3;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.jdbc.io.RecordStoreIteratorFactory;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.io.RecordStoreConnection;
import com.revolsys.record.io.RecordStoreExtension;
import com.revolsys.record.property.RecordDefinitionProperty;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.util.Property;
import com.revolsys.util.UrlUtil;
import com.revolsys.util.count.CategoryLabelCountMap;

public abstract class AbstractRecordStore extends BaseObjectWithProperties implements RecordStore {
  private boolean closed = false;

  private Map<String, List<String>> codeTableFieldNames = new HashMap<>();

  private final Map<String, CodeTable> codeTableByFieldName = new HashMap<>();

  private List<RecordDefinitionProperty> commonRecordDefinitionProperties = new ArrayList<>();

  private Map<String, Object> connectionProperties = new HashMap<>();

  private RecordStoreConnection recordStoreConnection;

  private final Map<Class<?>, Consumer3<Query, StringBuilder, QueryValue>> sqlQueryAppenderByClass = new HashMap<>();

  private GeometryFactory geometryFactory;

  private final Map<PathName, Set<Consumer<RecordDefinition>>> recordDefinitionInitializersByTableName = new HashMap<>();

  private boolean createMissingRecordStore = false;

  private boolean createMissingTables = false;

  private RecordStoreIteratorFactory iteratorFactory = new RecordStoreIteratorFactory();

  private String label;

  private boolean loadFullSchema = true;

  private RecordFactory<Record> recordFactory;

  private final Set<RecordStoreExtension> recordStoreExtensions = new LinkedHashSet<>();

  private final RecordStoreSchema rootSchema;

  private CategoryLabelCountMap statistics;

  private final Map<String, Map<String, Object>> typeRecordDefinitionProperties = new HashMap<>();

  private boolean initialized = false;

  protected AbstractRecordStore() {
    this(ArrayRecord.FACTORY);
  }

  protected AbstractRecordStore(final RecordFactory<? extends Record> recordFactory) {
    setRecordFactory(recordFactory);
    this.rootSchema = newRootSchema();
  }

  @Override
  public void addCodeTable(final CodeTable codeTable) {
    final String idFieldName = codeTable.getIdFieldName();
    addCodeTable(idFieldName, codeTable);
    final List<String> fieldNameAliases = codeTable.getFieldNameAliases();
    for (final String alias : fieldNameAliases) {
      addCodeTable(alias, codeTable);
    }
    final String codeTableName = codeTable.getName();
    final List<String> fieldNames = this.codeTableFieldNames.get(codeTableName);
    if (fieldNames != null) {
      for (final String fieldName : fieldNames) {
        addCodeTable(fieldName, codeTable);
      }
    }
  }

  @Override
  public void addCodeTable(final String fieldName, final CodeTable codeTable) {
    if (fieldName != null && !fieldName.equalsIgnoreCase("ID")) {
      this.codeTableByFieldName.put(fieldName.toUpperCase(), codeTable);
      final RecordStoreSchema rootSchema = getRootSchema();
      addCodeTableFieldNames(rootSchema, codeTable, fieldName);
    }
  }

  protected void addCodeTableFieldNames(final RecordStoreSchema schema, final CodeTable codeTable,
    final String codeTableFieldName) {
    if (schema.isInitialized()) {
      for (final RecordStoreSchema childSchema : schema.getSchemas()) {
        addCodeTableFieldNames(childSchema, codeTable, codeTableFieldName);
      }
      for (final RecordDefinition recordDefinition : schema.getRecordDefinitions()) {
        for (final FieldDefinition field : recordDefinition.getFields()) {
          final String fieldName = field.getName();
          if (!recordDefinition.isIdField(fieldName) && fieldName.equals(codeTableFieldName)) {
            field.setCodeTable(codeTable);
          }
        }
      }
    }
  }

  @Override
  public AbstractRecordStore addRecordDefinitionInitializer(final PathName tableName,
    final Consumer<RecordDefinition> action) {
    Maps.addToSet(this.recordDefinitionInitializersByTableName, tableName, action);
    return this;
  }

  protected void addRecordDefinitionProperties(final RecordDefinitionImpl recordDefinition) {
    final String typePath = recordDefinition.getPath();
    for (final RecordDefinitionProperty property : this.commonRecordDefinitionProperties) {
      final RecordDefinitionProperty clonedProperty = property.clone();
      clonedProperty.setRecordDefinition(recordDefinition);
    }
    final Map<String, Object> properties = this.typeRecordDefinitionProperties.get(typePath);
    recordDefinition.setProperties(properties);
  }

  public void addRecordStoreExtension(final RecordStoreExtension extension) {
    if (extension != null) {
      try {
        final Map<String, Object> connectionProperties = getConnectionProperties();
        extension.initialize(this, connectionProperties);
        this.recordStoreExtensions.add(extension);
      } catch (final Throwable e) {
        Logs.error(extension.getClass(), "Unable to initialize", e);
      }
    }
  }

  protected void addSqlQueryAppender(final Class<?> clazz,
    final Consumer3<Query, StringBuilder, QueryValue> appender) {
    this.sqlQueryAppenderByClass.put(clazz, appender);
  }

  @Override
  public void appendQueryValue(final Query query, final StringBuilder sql,
    final QueryValue queryValue) {
    final Class<?> valueClass = queryValue.getClass();
    final Consumer3<Query, StringBuilder, QueryValue> appender = this.sqlQueryAppenderByClass
      .get(valueClass);
    if (appender == null) {
      queryValue.appendDefaultSql(query, this, sql);
    } else {
      appender.accept(query, sql, queryValue);
    }
  }

  @Override
  public void close() {
    this.closed = true;
    try {
      super.close();
      if (this.statistics != null) {
        this.statistics.disconnect();
      }
      getRootSchema().close();
    } finally {
      this.codeTableFieldNames.clear();
      this.codeTableByFieldName.clear();
      this.commonRecordDefinitionProperties.clear();
      this.connectionProperties.clear();
      this.recordFactory = null;
      this.recordStoreExtensions.clear();
      try {
        throw new RuntimeException("Closed:" + this);
      } catch (final Exception e) {
        Logs.error(this, e);
      }
      this.iteratorFactory = null;
      this.label = "deleted";
      if (this.statistics != null) {
        this.statistics.clear();
      }
      this.typeRecordDefinitionProperties.clear();
    }
  }

  protected RecordDefinition createRecordDefinitionDo(final RecordDefinition recordDefinition) {
    throw new UnsupportedOperationException(
      "Creating new record definitions (tables) is not supported");
  }

  @Override
  public CodeTable getCodeTableByFieldName(final CharSequence fieldName) {
    if (fieldName == null) {
      return null;
    } else {
      final CodeTable codeTable = this.codeTableByFieldName.get(fieldName.toString().toUpperCase());
      return codeTable;
    }
  }

  @Override
  public Map<String, CodeTable> getCodeTableByFieldNameMap() {
    return new HashMap<>(this.codeTableByFieldName);
  }

  public Map<String, List<String>> getCodeTableColumNames() {
    return this.codeTableFieldNames;
  }

  @Override
  public MapEx getConnectionProperties() {
    return Maps.newLinkedHashEx(this.connectionProperties);
  }

  @Override
  public String getConnectionTitle() {
    final RecordStoreConnection recordStoreConnection = getRecordStoreConnection();
    if (recordStoreConnection == null) {
      final String url = getUrl();
      if (url == null) {
        return null;
      } else {
        return UrlUtil.getFileName(url);
      }
    } else {
      return recordStoreConnection.getName();
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public RecordStoreIteratorFactory getIteratorFactory() {
    return this.iteratorFactory;
  }

  @Override
  public String getLabel() {
    return this.label;
  }

  @Override
  public <RD extends RecordDefinition> RD getRecordDefinition(
    final RecordDefinition recordDefinition) {
    final RD rd = RecordStore.super.getRecordDefinition(recordDefinition);
    if (rd == null && recordDefinition != null && isCreateMissingTables()) {
      return (RD)createRecordDefinitionDo(recordDefinition);
    }
    return rd;
  }

  @Override
  public RecordFactory<Record> getRecordFactory() {
    return this.recordFactory;
  }

  @Override
  public RecordStoreConnection getRecordStoreConnection() {
    return this.recordStoreConnection;
  }

  public Collection<RecordStoreExtension> getRecordStoreExtensions() {
    return this.recordStoreExtensions;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <RSS extends RecordStoreSchema> RSS getRootSchema() {
    return (RSS)this.rootSchema;
  }

  @Override
  public CategoryLabelCountMap getStatistics() {
    return this.statistics;
  }

  @Override
  public String getUrl() {
    return (String)this.connectionProperties.get("url");
  }

  @Override
  public String getUsername() {
    return (String)this.connectionProperties.get("user");
  }

  @Override
  public final void initialize() {
    synchronized (this) {
      if (!this.initialized) {
        this.initialized = true;
        initializeDo();
        initializePost();
      }
    }
  }

  protected void initializeDo() {
    final CategoryLabelCountMap statistics = getStatistics();
    if (statistics != null) {
      statistics.connect();
    }
  }

  protected void initializePost() {
  }

  @Override
  public void initializeRecordDefinition(final RecordDefinition recordDefinition) {
    final PathName pathName = recordDefinition.getPathName();
    for (final Consumer<RecordDefinition> action : this.recordDefinitionInitializersByTableName
      .getOrDefault(pathName, Collections.emptySet())) {
      action.accept(recordDefinition);
    }
  }

  protected void initRecordDefinition(final RecordDefinition recordDefinition) {
    for (final FieldDefinition field : recordDefinition.getFields()) {
      final String fieldName = field.getName();
      if (!recordDefinition.isIdField(fieldName)) {
        final CodeTable codeTable = getCodeTableByFieldName(fieldName);
        if (codeTable != null) {
          if (field.getCodeTable() != codeTable) {
            field.setCodeTable(codeTable);
          }
        }
      }
    }
  }

  @Override
  public boolean isClosed() {
    return this.closed;
  }

  public boolean isCreateMissingRecordStore() {
    return this.createMissingRecordStore;
  }

  public boolean isCreateMissingTables() {
    return this.createMissingTables;
  }

  public boolean isInitialized() {
    return this.initialized;
  }

  @Override
  public boolean isLoadFullSchema() {
    return this.loadFullSchema;
  }

  protected RecordStoreSchema newRootSchema() {
    return new RecordStoreSchema(this);
  }

  protected RecordDefinition refreshRecordDefinition(final RecordStoreSchema schema,
    final PathName typePath) {
    return null;
  }

  protected void refreshSchema() {
    getRootSchema().refresh();
  }

  protected void refreshSchema(final PathName schemaName) {
    final RecordStoreSchema schema = getSchema(schemaName);
    if (schema != null) {
      schema.refresh();
    }
  }

  protected Map<PathName, ? extends RecordStoreSchemaElement> refreshSchemaElements(
    final RecordStoreSchema schema) {
    return Collections.emptyMap();
  }

  public void setCodeTableColumNames(final Map<String, List<String>> domainColumNames) {
    this.codeTableFieldNames = domainColumNames;
  }

  public void setCommonRecordDefinitionProperties(
    final List<RecordDefinitionProperty> commonRecordDefinitionProperties) {
    this.commonRecordDefinitionProperties = commonRecordDefinitionProperties;
  }

  protected void setConnectionProperties(final Map<String, ? extends Object> connectionProperties) {
    this.connectionProperties = Maps.newHash(connectionProperties);
  }

  public void setCreateMissingRecordStore(final boolean createMissingRecordStore) {
    this.createMissingRecordStore = createMissingRecordStore;
  }

  public void setCreateMissingTables(final boolean createMissingTables) {
    this.createMissingTables = createMissingTables;
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public void setIteratorFactory(final RecordStoreIteratorFactory iteratorFactory) {
    this.iteratorFactory = iteratorFactory;
  }

  @Override
  public void setLabel(final String label) {
    this.label = label;
    final CategoryLabelCountMap statistics = getStatistics();
    if (statistics != null) {
      statistics.setPrefix(label);
    }
  }

  @Override
  public void setLoadFullSchema(final boolean loadFullSchema) {
    this.loadFullSchema = loadFullSchema;
  }

  public void setLogCounts(final boolean logCounts) {
    if (logCounts) {
      if (this.statistics == null) {
        this.statistics = new CategoryLabelCountMap();
      }
    } else {
      this.statistics = null;
    }
  }

  @Override
  public void setRecordFactory(final RecordFactory<? extends Record> recordFactory) {
    this.recordFactory = (RecordFactory<Record>)recordFactory;
  }

  @Override
  public void setRecordStoreConnection(final RecordStoreConnection recordStoreConnection) {
    this.recordStoreConnection = recordStoreConnection;
  }

  public void setTypeRecordDefinitionProperties(
    final Map<String, List<RecordDefinitionProperty>> typeRecordDefinitionProperties) {
    for (final Entry<String, List<RecordDefinitionProperty>> typeProperties : typeRecordDefinitionProperties
      .entrySet()) {
      final String typePath = typeProperties.getKey();
      Map<String, Object> currentProperties = this.typeRecordDefinitionProperties.get(typePath);
      if (currentProperties == null) {
        currentProperties = new LinkedHashMap<>();
        this.typeRecordDefinitionProperties.put(typePath, currentProperties);
      }
      final List<RecordDefinitionProperty> properties = typeProperties.getValue();
      for (final RecordDefinitionProperty property : properties) {
        final String name = property.getPropertyName();
        currentProperties.put(name, property);
      }
    }
  }

  @Override
  public String toString() {
    if (Property.hasValue(this.label)) {
      return this.label;
    } else {
      return super.toString();
    }
  }
}
