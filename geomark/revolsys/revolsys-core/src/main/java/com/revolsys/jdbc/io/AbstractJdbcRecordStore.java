package com.revolsys.jdbc.io;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.revolsys.collection.ResultPager;
import com.revolsys.collection.map.Maps;
import com.revolsys.io.PathUtil;
import com.revolsys.jdbc.JdbcConnection;
import com.revolsys.jdbc.field.JdbcFieldAdder;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.jdbc.field.JdbcFieldFactory;
import com.revolsys.jdbc.field.JdbcFieldFactoryAdder;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.RecordState;
import com.revolsys.record.code.AbstractMultiValueCodeTable;
import com.revolsys.record.io.RecordIterator;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordStoreExtension;
import com.revolsys.record.io.RecordStoreQueryReader;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.property.GlobalIdProperty;
import com.revolsys.record.query.ColumnIndexes;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.TableReference;
import com.revolsys.record.schema.AbstractRecordStore;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.record.schema.RecordStoreSchema;
import com.revolsys.record.schema.RecordStoreSchemaElement;
import com.revolsys.transaction.Transaction;
import com.revolsys.transaction.TransactionOptions;
import com.revolsys.util.Booleans;
import com.revolsys.util.Property;

public abstract class AbstractJdbcRecordStore extends AbstractRecordStore
  implements JdbcRecordStore, RecordStoreExtension {
  public static final List<String> DEFAULT_PERMISSIONS = Arrays.asList("SELECT");

  public static final RecordIterator newJdbcIterator(final RecordStore recordStore,
    final Query query, final Map<String, Object> properties) {
    return new JdbcQueryIterator((AbstractJdbcRecordStore)recordStore, query, properties);
  }

  private final Set<String> allSchemaNames = new TreeSet<>();

  private boolean quoteNames = false;

  private int batchSize;

  private boolean blobAsString = false;

  private boolean clobAsString = false;

  private JdbcDatabaseFactory databaseFactory;

  private DataSource dataSource;

  private final Object exceptionWriterKey = new Object();

  private Set<String> excludeTablePaths = new HashSet<>();

  private List<String> excludeTablePatterns = new ArrayList<>();

  private final Map<String, JdbcFieldAdder> fieldDefinitionAdders = new HashMap<>();

  private boolean flushBetweenTypes;

  private boolean lobAsString = false;

  private String primaryKeySql;

  private String primaryKeyTableCondition;

  private String schemaPermissionsSql;

  private String schemaTablePermissionsSql;

  private final Map<String, String> sequenceTypeSqlMap = new HashMap<>();

  private String sqlPrefix;

  private String sqlSuffix;

  private DataSourceTransactionManager transactionManager;

  private boolean usesSchema = true;

  private final Object writerKey = new Object();

  private boolean useUpperCaseNames = true;

  public AbstractJdbcRecordStore() {
    this(ArrayRecord.FACTORY);
  }

  public AbstractJdbcRecordStore(final DataSource dataSource) {
    this();
    setDataSource(dataSource);
  }

  public AbstractJdbcRecordStore(final JdbcDatabaseFactory databaseFactory,
    final Map<String, ? extends Object> connectionProperties) {
    this(databaseFactory, ArrayRecord.FACTORY);
    setConnectionProperties(connectionProperties);
    final DataSource dataSource = databaseFactory.newDataSource(connectionProperties);
    setDataSource(dataSource);
  }

  public AbstractJdbcRecordStore(final JdbcDatabaseFactory databaseFactory,
    final RecordFactory<? extends Record> recordFactory) {
    this(recordFactory);
    this.databaseFactory = databaseFactory;
  }

  public AbstractJdbcRecordStore(final RecordFactory<? extends Record> recordFactory) {
    super(recordFactory);
    setIteratorFactory(new RecordStoreIteratorFactory(AbstractJdbcRecordStore::newJdbcIterator));
    addRecordStoreExtension(this);
  }

  protected void addAllSchemaNames(final String schemaName) {
    this.allSchemaNames.add(toUpperIfNeeded(schemaName));
  }

  public void addExcludeTablePaths(final String tableName) {
    addExcludeTablePaths(tableName);
  }

  public JdbcFieldDefinition addField(final JdbcRecordDefinition recordDefinition,
    final ResultSetMetaData resultSetMetaData, final int columnIndex) throws SQLException {
    final String columnName = resultSetMetaData.getColumnName(columnIndex);
    final String fieldName = toUpperIfNeeded(columnName);
    final String dataType = resultSetMetaData.getColumnTypeName(columnIndex);
    final int sqlType = resultSetMetaData.getColumnType(columnIndex);
    final int length = resultSetMetaData.getPrecision(columnIndex);
    final int scale = resultSetMetaData.getScale(columnIndex);
    return addField(recordDefinition, columnName, fieldName, dataType, sqlType, length, scale,
      false, null);
  }

  protected JdbcFieldDefinition addField(final JdbcRecordDefinition recordDefinition,
    final String dbColumnName, final String name, final String dataType, final int sqlType,
    final int length, final int scale, final boolean required, final String description) {
    final JdbcFieldAdder fieldAdder = getFieldAdder(dataType);
    return (JdbcFieldDefinition)fieldAdder.addField(this, recordDefinition, dbColumnName, name,
      dataType, sqlType, length, scale, required, description);
  }

  protected void addField(final ResultSetMetaData resultSetMetaData,
    final JdbcRecordDefinition recordDefinition, final String name, final int i,
    final String description) throws SQLException {
    final String dataType = resultSetMetaData.getColumnTypeName(i);
    final int sqlType = resultSetMetaData.getColumnType(i);
    final int length = resultSetMetaData.getPrecision(i);
    final int scale = resultSetMetaData.getScale(i);
    final boolean required = false;
    final String fieldName = toUpperIfNeeded(name);
    addField(recordDefinition, name, fieldName, dataType, sqlType, length, scale, required,
      description);
  }

  protected void addFieldAdder(final String sqlTypeName, final DataType dataType) {
    final JdbcFieldAdder adder = new JdbcFieldAdder(dataType);
    this.fieldDefinitionAdders.put(sqlTypeName, adder);
  }

  public void addFieldAdder(final String sqlTypeName, final JdbcFieldAdder adder) {
    this.fieldDefinitionAdders.put(sqlTypeName, adder);
  }

  protected void addFieldAdder(final String sqlTypeName, final JdbcFieldFactory fieldFactory) {
    final JdbcFieldFactoryAdder fieldAdder = new JdbcFieldFactoryAdder(fieldFactory);
    addFieldAdder(sqlTypeName, fieldAdder);
  }

  /**
   * Add a new field definition for record definitions that don't have a primary key.
   *
   * @param recordDefinition
   */
  protected void addRowIdFieldDefinition(final RecordDefinitionImpl recordDefinition) {
    final Object tableType = recordDefinition.getProperty("tableType");
    if ("TABLE".equals(tableType)) {
      final JdbcFieldDefinition idFieldDefinition = newRowIdFieldDefinition();
      if (idFieldDefinition != null) {
        recordDefinition.addField(idFieldDefinition);
        final String idFieldName = idFieldDefinition.getName();
        recordDefinition.setIdFieldName(idFieldName);
      }
    }
  }

  @Override
  @PreDestroy
  public synchronized void close() {
    try {
      super.close();
      if (this.databaseFactory != null && this.dataSource != null) {
        JdbcDatabaseFactory.closeDataSource(this.dataSource);
      }
    } finally {
      this.allSchemaNames.clear();
      this.fieldDefinitionAdders.clear();
      this.transactionManager = null;
      this.databaseFactory = null;
      this.dataSource = null;
      this.excludeTablePatterns.clear();
      this.sequenceTypeSqlMap.clear();
      this.sqlPrefix = null;
      this.sqlSuffix = null;
    }
  }

  @Override
  public boolean deleteRecord(final Record record) {
    final RecordState state = RecordState.DELETED;
    write(record, state);
    return true;
  }

  @Override
  public int deleteRecords(final Iterable<? extends Record> records) {
    return writeAll(records, RecordState.DELETED);
  }

  @Override
  public int deleteRecords(final Query query) {
    final PathName tablePath = query.getTablePath();
    RecordDefinition recordDefinition = query.getRecordDefinition();
    if (recordDefinition == null) {
      if (tablePath != null) {
        recordDefinition = getRecordDefinition(tablePath);
        query.setRecordDefinition(recordDefinition);
      }
    }
    final String sql = query.newDeleteSql();
    try (
      Transaction transaction = newTransaction(com.revolsys.transaction.Propagation.REQUIRED)) {
      // It's important to have this in an inner try. Otherwise the exceptions
      // won't get caught on closing the writer and the transaction won't get
      // rolled back.
      try (
        JdbcConnection connection = getJdbcConnection(isAutoCommit());
        final PreparedStatement statement = connection.prepareStatement(sql)) {

        setPreparedStatementParameters(statement, query);
        return statement.executeUpdate();
      } catch (final SQLException e) {
        transaction.setRollbackOnly();
        throw new RuntimeException("Unable to delete : " + sql, e);
      } catch (final RuntimeException e) {
        transaction.setRollbackOnly();
        throw e;
      } catch (final Error e) {
        transaction.setRollbackOnly();
        throw e;
      }
    }
  }

  public void executeUpdate(final PreparedStatement statement) throws SQLException {
    statement.executeUpdate();
  }

  public Set<String> getAllSchemaNames() {
    return this.allSchemaNames;
  }

  public int getBatchSize() {
    return this.batchSize;
  }

  public List<String> getColumnNames(final String typePath) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    return recordDefinition.getFieldNames();
  }

  protected Set<String> getDatabaseSchemaNames() {
    final Set<String> schemaNames = new TreeSet<>();
    try {
      try (
        final Connection connection = getJdbcConnection();
        final PreparedStatement statement = connection.prepareStatement(this.schemaPermissionsSql);
        final ResultSet resultSet = statement.executeQuery();) {
        while (resultSet.next()) {
          final String schemaName = resultSet.getString("SCHEMA_NAME");
          addAllSchemaNames(schemaName);
          if (!isSchemaExcluded(schemaName)) {
            schemaNames.add(schemaName);
          }
        }
      }
    } catch (final Throwable e) {
      Logs.error(this, "Unable to get schema and table permissions", e);
    }
    return schemaNames;
  }

  protected DataSource getDataSource() {
    return this.dataSource;
  }

  public Set<String> getExcludeTablePaths() {
    return this.excludeTablePaths;
  }

  public JdbcFieldDefinition getField(final String schemaName, final String tableName,
    final String columnName) {
    final String typePath = PathUtil.toPath(schemaName, tableName);
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition == null) {
      return null;
    } else {
      final FieldDefinition attribute = recordDefinition.getField(columnName);
      return (JdbcFieldDefinition)attribute;
    }
  }

  public JdbcFieldAdder getFieldAdder(final String dataType) {
    JdbcFieldAdder fieldAdder = this.fieldDefinitionAdders.get(dataType);
    if (fieldAdder == null) {
      fieldAdder = new JdbcFieldAdder(DataTypes.OBJECT);
    }
    return fieldAdder;
  }

  @Override
  public String getGeneratePrimaryKeySql(final JdbcRecordDefinition recordDefinition) {
    throw new UnsupportedOperationException(
      "Cannot create SQL to generate Primary Key for " + recordDefinition);
  }

  public String getIdFieldName(final String typePath) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getIdFieldName();
    }
  }

  @Override
  public JdbcConnection getJdbcConnection() {
    return new JdbcConnection(this.dataSource);
  }

  @Override
  public JdbcConnection getJdbcConnection(final boolean autoCommit) {
    return new JdbcConnection(this.dataSource, autoCommit);
  }

  protected Identifier getNextPrimaryKey(final String typePath) {
    return null;
  }

  @Override
  public int getRecordCount(Query query) {
    if (query == null) {
      return 0;
    } else {
      final TableReference table = query.getTable();
      query = query.clone(table, table);
      query.setSql(null);
      query.clearOrderBy();
      final String sql = "select count(mainquery.*) from (" + query.getSelectSql() + ") mainquery";
      try (
        Transaction transaction = newTransaction(TransactionOptions.REQUIRED);
        JdbcConnection connection = getJdbcConnection()) {
        try (
          final PreparedStatement statement = connection.prepareStatement(sql)) {
          setPreparedStatementParameters(statement, query);
          try (
            final ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
              final int rowCount = resultSet.getInt(1);
              return rowCount;
            } else {
              return 0;
            }
          }
        } catch (final SQLException e) {
          throw connection.getException("getRecordCount", sql, e);
        } catch (final IllegalArgumentException e) {
          Logs.error(this, "Cannot get row count: " + query, e);
          return 0;
        }
      }
    }
  }

  @Override
  public JdbcRecordDefinition getRecordDefinition(PathName typePath,
    final ResultSetMetaData resultSetMetaData, final String dbTableName) {
    if (Property.isEmpty(typePath)) {
      typePath = PathName.newPathName("/Record");
    }

    try {
      final PathName pathName = PathName.newPathName(typePath);
      final PathName schemaName = pathName.getParent();
      final JdbcRecordStoreSchema schema = getSchema(schemaName);
      final JdbcRecordDefinition resultRecordDefinition = newRecordDefinition(schema, pathName,
        dbTableName);

      final RecordDefinition recordDefinition = getRecordDefinition(typePath);
      for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
        final String columnName = resultSetMetaData.getColumnName(i);
        final String fieldName = toUpperIfNeeded(columnName);
        addField(resultSetMetaData, resultRecordDefinition, fieldName, i, null);
        if (recordDefinition != null && recordDefinition.isIdField(fieldName)) {
          resultRecordDefinition.setIdFieldIndex(i - 1);
        }

      }

      addRecordDefinitionProperties(resultRecordDefinition);

      return resultRecordDefinition;
    } catch (final SQLException e) {
      throw new IllegalArgumentException("Unable to load metadata for " + typePath);
    }
  }

  @Override
  public JdbcRecordDefinition getRecordDefinition(final Query query,
    final ResultSetMetaData resultSetMetaData) {
    PathName tablePath = query.getTablePath();
    if (Property.isEmpty(tablePath)) {
      tablePath = PathName.newPathName("/Record");
    }

    final RecordDefinition recordDefinition = query.getRecordDefinition();
    try {
      final PathName schemaName = tablePath.getParent();
      final JdbcRecordStoreSchema schema = getSchema(schemaName);
      final String dbTableName = recordDefinition.getDbTableName();
      final JdbcRecordDefinition resultRecordDefinition = newRecordDefinition(schema, tablePath,
        dbTableName);

      final ColumnIndexes columnIndexes = new ColumnIndexes();
      List<? extends QueryValue> selectExpressions = query.getSelectExpressions();
      if (selectExpressions.isEmpty()) {
        selectExpressions = recordDefinition.getFieldDefinitions();
      }
      for (final QueryValue expression : selectExpressions) {
        final FieldDefinition newField = expression.addField(this, resultRecordDefinition,
          resultSetMetaData, columnIndexes);
        if (expression instanceof JdbcFieldDefinition) {
          final JdbcFieldDefinition field = (JdbcFieldDefinition)expression;
          if (field.getRecordDefinition() == recordDefinition
            && recordDefinition.isIdField(field)) {
            final String name = newField.getName();
            resultRecordDefinition.setIdFieldName(name);
          }
        }
      }

      addRecordDefinitionProperties(resultRecordDefinition);

      return resultRecordDefinition;
    } catch (final SQLException e) {
      throw new IllegalArgumentException("Unable to load metadata for " + tablePath);
    }
  }

  @Override
  public RecordReader getRecords(final Query query) {
    return newIterator(query, null);
  }

  public String getSchemaTablePermissionsSql() {
    return this.schemaTablePermissionsSql;
  }

  protected String getSequenceName(final JdbcRecordDefinition recordDefinition) {
    return null;
  }

  public String getSqlPrefix() {
    return this.sqlPrefix;
  }

  public String getSqlSuffix() {
    return this.sqlSuffix;
  }

  @Override
  public PlatformTransactionManager getTransactionManager() {
    return this.transactionManager;
  }

  @Override
  public void initialize(final RecordStore recordStore,
    final Map<String, Object> connectionProperties) {
  }

  @Override
  public void initializeDo() {
    super.initializeDo();
    if (this.dataSource != null) {
      this.transactionManager = new DataSourceTransactionManager(this.dataSource);
    }
  }

  @Override
  protected void initializePost() {
    try (
      Transaction transaction = newTransaction(TransactionOptions.DEFAULT);
      JdbcConnection connection = getJdbcConnection()) {
      // Get a connection to test that the database works
    }
  }

  @Override
  public void insertRecord(final Record record) {
    write(record, RecordState.NEW);
  }

  @Override
  public void insertRecords(final Iterable<? extends Record> records) {
    writeAll(records, RecordState.NEW);
  }

  public boolean isAutoCommit() {
    boolean autoCommit = false;
    if (Booleans.getBoolean(getProperties().get("autoCommit"))) {
      autoCommit = true;
    }
    return autoCommit;
  }

  public boolean isBlobAsString() {
    return this.blobAsString;
  }

  public boolean isClobAsString() {
    return this.clobAsString;
  }

  @Override
  public boolean isEditable(final PathName typePath) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    return recordDefinition.getIdFieldIndex() != -1;
  }

  @Override
  public boolean isEnabled(final RecordStore recordStore) {
    return true;
  }

  protected boolean isExcluded(final String dbSchemaName, final String tableName) {
    String path = "/";
    if (dbSchemaName != null) {
      path += toUpperIfNeeded(dbSchemaName) + "/";
    }
    path += toUpperIfNeeded(tableName);
    path = path.replaceAll("/+", "/");
    if (this.excludeTablePaths.contains(path)) {
      return true;
    } else {
      for (final String pattern : this.excludeTablePatterns) {
        if (path.matches(pattern) || tableName.matches(pattern)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isFlushBetweenTypes() {
    return this.flushBetweenTypes;
  }

  @Override
  public boolean isIdFieldRowid(final RecordDefinition recordDefinition) {
    return false;
  }

  public boolean isLobAsString() {
    return this.lobAsString;
  }

  public boolean isQuoteNames() {
    return this.quoteNames;
  }

  public abstract boolean isSchemaExcluded(String schemaName);

  public boolean isUseUpperCaseNames() {
    return this.useUpperCaseNames;
  }

  protected synchronized Map<String, List<String>> loadIdFieldNames(final Connection connection,
    final String dbSchemaName) {
    final String schemaName = "/" + toUpperIfNeeded(dbSchemaName);
    final Map<String, List<String>> idFieldNames = new HashMap<>();
    if (Property.hasValue(this.primaryKeySql)) {
      try {
        try (
          final PreparedStatement statement = connection.prepareStatement(this.primaryKeySql);) {
          if (this.primaryKeySql.indexOf('?') != -1) {
            statement.setString(1, dbSchemaName);
          }
          try (
            final ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
              final String tableName = toUpperIfNeeded(rs.getString("TABLE_NAME"));
              final String idFieldName = rs.getString("COLUMN_NAME");
              if (Property.hasValue(dbSchemaName)) {
                Maps.addToList(idFieldNames, schemaName + "/" + tableName, idFieldName);
              } else {
                Maps.addToList(idFieldNames, "/" + tableName, idFieldName);
              }
            }
          }
        }
      } catch (final Throwable e) {
        throw new IllegalArgumentException("Unable to primary keys for schema " + dbSchemaName, e);
      }
    }
    return idFieldNames;
  }

  protected Map<PathName, JdbcRecordDefinition> loadRecordDefinitionsPermissions(
    final Connection connection, final JdbcRecordStoreSchema schema) {
    final PathName schemaPath = schema.getPathName();
    final String dbSchemaName = schema.getDbName();
    try (
      final PreparedStatement statement = connection
        .prepareStatement(this.schemaTablePermissionsSql)) {
      if (this.schemaTablePermissionsSql.indexOf('?') != -1) {
        statement.setString(1, dbSchemaName);
      }
      try (

        final ResultSet resultSet = statement.executeQuery()) {
        final Map<PathName, JdbcRecordDefinition> recordDefinitionMap = new TreeMap<>();
        while (resultSet.next()) {
          final String dbTableName = resultSet.getString("TABLE_NAME");
          if (!isExcluded(dbSchemaName, dbTableName)) {
            final String tableName = toUpperIfNeeded(dbTableName);
            final PathName pathName = schemaPath.newChild(tableName);

            JdbcRecordDefinition recordDefinition = recordDefinitionMap.get(pathName);
            Set<String> tablePermissions;
            if (recordDefinition == null) {
              recordDefinition = newRecordDefinition(schema, pathName, dbTableName);
              recordDefinitionMap.put(pathName, recordDefinition);

              tablePermissions = new LinkedHashSet<>();
              recordDefinition.setProperty("permissions", tablePermissions);

              final String description = resultSet.getString("REMARKS");
              recordDefinition.setDescription(description);

              try {
                final String tableType = resultSet.getString("TABLE_TYPE");
                recordDefinition.setProperty("tableType", tableType);
              } catch (final SQLException e) {
              }
            } else {
              tablePermissions = recordDefinition.getProperty("permissions");
            }
            final String privilege = resultSet.getString("PRIVILEGE");
            if ("ALL".equals(privilege)) {
              tablePermissions.add("SELECT");
              tablePermissions.add("INSERT");
              tablePermissions.add("UPDATE");
              tablePermissions.add("DELETE");
            } else {
              tablePermissions.add(privilege);
            }

          }
        }
        return recordDefinitionMap;
      }
    } catch (final Throwable e) {
      throw Exceptions.wrap("Unable to get schema and table permissions: " + dbSchemaName, e);
    }
  }

  protected Identifier newPrimaryIdentifier(final JdbcRecordDefinition recordDefinition) {
    final GlobalIdProperty globalIdProperty = GlobalIdProperty.getProperty(recordDefinition);
    if (globalIdProperty == null) {
      if (isIdFieldRowid(recordDefinition)) {
        return null;
      } else if (recordDefinition.hasIdField()) {
        final String sequenceName = getSequenceName(recordDefinition);
        return getNextPrimaryKey(sequenceName);
      } else {
        return null;
      }
    } else {
      return Identifier.newIdentifier(UUID.randomUUID().toString());
    }
  }

  @Override
  public Identifier newPrimaryIdentifier(final PathName typePath) {
    final JdbcRecordDefinition recordDefinition = getRecordDefinition(typePath);
    return newPrimaryIdentifier(recordDefinition);
  }

  protected JdbcRecordDefinition newRecordDefinition(final JdbcRecordStoreSchema schema,
    final PathName pathName, final String dbTableName) {
    return new JdbcRecordDefinition(schema, pathName, dbTableName);
  }

  protected RecordStoreQueryReader newRecordReader(final Query query) {
    final RecordStoreQueryReader reader = newRecordReader();
    reader.addQuery(query);
    return reader;
  }

  @Override
  public RecordWriter newRecordWriter(final boolean throwExceptions) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      Object writerKey;
      if (throwExceptions) {
        writerKey = this.exceptionWriterKey;
      } else {
        writerKey = this.writerKey;
      }
      JdbcWriterResourceHolder resourceHolder = (JdbcWriterResourceHolder)TransactionSynchronizationManager
        .getResource(writerKey);
      if (resourceHolder == null) {
        resourceHolder = new JdbcWriterResourceHolder();
        TransactionSynchronizationManager.bindResource(writerKey, resourceHolder);
      }
      final JdbcWriterWrapper writerWrapper = resourceHolder.getWriterWrapper(this, throwExceptions,
        this.batchSize);

      if (!resourceHolder.isSynchronizedWithTransaction()) {
        final JdbcWriterSynchronization synchronization = new JdbcWriterSynchronization(this,
          resourceHolder, writerKey);
        TransactionSynchronizationManager.registerSynchronization(synchronization);
        resourceHolder.setSynchronizedWithTransaction(true);
      }

      return writerWrapper;

    } else {
      return newRecordWriter(this.batchSize);
    }
  }

  protected JdbcRecordWriter newRecordWriter(final int batchSize) {
    return newRecordWriter(null, batchSize);
  }

  @Override
  public RecordWriter newRecordWriter(final RecordDefinitionProxy recordDefinition) {
    return newRecordWriter(recordDefinition, 0);
  }

  protected JdbcRecordWriter newRecordWriter(final RecordDefinitionProxy recordDefinition,
    final int batchSize) {
    final JdbcRecordWriter writer = new JdbcRecordWriter(this, recordDefinition, batchSize);
    writer.setSqlPrefix(this.sqlPrefix);
    writer.setSqlSuffix(this.sqlSuffix);
    writer.setLabel(getLabel());
    writer.setFlushBetweenTypes(this.flushBetweenTypes);
    writer.setQuoteColumnNames(false);
    return writer;
  }

  @Override
  protected RecordStoreSchema newRootSchema() {
    return new JdbcRecordStoreSchema(this);
  }

  /**
   * Create the field definition for the row identifier column for tables that don't have a primary key.
   * @return
   */
  protected JdbcFieldDefinition newRowIdFieldDefinition() {
    return null;
  }

  protected JdbcRecordStoreSchema newSchema(final JdbcRecordStoreSchema rootSchema,
    final String dbSchemaName, final PathName childSchemaPath) {
    return new JdbcRecordStoreSchema(rootSchema, childSchemaPath, dbSchemaName);
  }

  @Override
  public ResultPager<Record> page(final Query query) {
    return new JdbcQueryResultPager(this, getProperties(), query);
  }

  protected void postProcess(final JdbcRecordStoreSchema schema) {
  }

  @Override
  public final void postProcess(final RecordStoreSchema schema) {
    postProcess((JdbcRecordStoreSchema)schema);
  }

  protected void preProcess(final JdbcRecordStoreSchema schema) {
    for (final JdbcFieldAdder fieldDefinitionAdder : this.fieldDefinitionAdders.values()) {
      fieldDefinitionAdder.initialize(schema);
    }
  }

  @Override
  public final void preProcess(final RecordStoreSchema schema) {
    preProcess((JdbcRecordStoreSchema)schema);
  }

  @Override
  protected Map<PathName, ? extends RecordStoreSchemaElement> refreshSchemaElements(
    final RecordStoreSchema schema) {
    final JdbcRecordStoreSchema jdbcSchema = (JdbcRecordStoreSchema)schema;
    final JdbcRecordStoreSchema rootSchema = getRootSchema();
    final PathName schemaPath = jdbcSchema.getPathName();
    try (
      Transaction transaction = newTransaction(TransactionOptions.DEFAULT)) {
      if (jdbcSchema == rootSchema) {
        if (this.usesSchema) {
          final Map<PathName, RecordStoreSchemaElement> schemas = new TreeMap<>();
          final Set<String> databaseSchemaNames = getDatabaseSchemaNames();
          for (final String dbSchemaName : databaseSchemaNames) {
            final PathName childSchemaPath = schemaPath.newChild(toUpperIfNeeded(dbSchemaName));
            RecordStoreSchema childSchema = schema.getSchema(childSchemaPath);
            if (childSchema == null) {
              childSchema = newSchema(rootSchema, dbSchemaName, childSchemaPath);
            } else {
              if (childSchema.isInitialized()) {
                childSchema.refresh();
              }
            }
            schemas.put(childSchemaPath, childSchema);
          }
          return schemas;
        } else {
          return refreshSchemaElementsDo(jdbcSchema, schemaPath);
        }
      } else {
        return refreshSchemaElementsDo(jdbcSchema, schemaPath);
      }
    }
  }

  protected Map<PathName, ? extends RecordStoreSchemaElement> refreshSchemaElementsDo(
    final JdbcRecordStoreSchema schema, final PathName schemaPath) {
    final String dbSchemaName = schema.getDbName();

    final Map<PathName, RecordStoreSchemaElement> elementsByPath = new TreeMap<>();
    try {
      try (
        final Connection connection = getJdbcConnection()) {
        final DatabaseMetaData databaseMetaData = connection.getMetaData();
        final Map<PathName, JdbcRecordDefinition> recordDefinitionMap = loadRecordDefinitionsPermissions(
          connection, schema);

        final Map<String, List<String>> idFieldNameMap = loadIdFieldNames(connection, dbSchemaName);
        for (final JdbcRecordDefinition recordDefinition : recordDefinitionMap.values()) {
          final PathName typePath = recordDefinition.getPathName();
          final List<String> idFieldNames = idFieldNameMap.get(typePath.toString());
          if (Property.isEmpty(idFieldNames)) {
            addRowIdFieldDefinition(recordDefinition);

          }
          elementsByPath.put(typePath, recordDefinition);
        }
        try (
          final ResultSet columnsRs = databaseMetaData.getColumns(null, dbSchemaName, "%", "%")) {
          while (columnsRs.next()) {
            final String tableName = toUpperIfNeeded(columnsRs.getString("TABLE_NAME"));
            final PathName typePath = schemaPath.newChild(tableName);
            final JdbcRecordDefinition recordDefinition = recordDefinitionMap.get(typePath);
            if (recordDefinition != null) {
              final String dbColumnName = columnsRs.getString("COLUMN_NAME");
              final String name = toUpperIfNeeded(dbColumnName);
              final int sqlType = columnsRs.getInt("DATA_TYPE");
              final String dataType = columnsRs.getString("TYPE_NAME");
              final int length = columnsRs.getInt("COLUMN_SIZE");
              int scale = columnsRs.getInt("DECIMAL_DIGITS");
              if (columnsRs.wasNull()) {
                scale = -1;
              }
              final boolean required = !columnsRs.getString("IS_NULLABLE").equals("YES");
              final String description = columnsRs.getString("REMARKS");
              final JdbcFieldDefinition field = addField(recordDefinition, dbColumnName, name,
                dataType, sqlType, length, scale, required, description);
              final boolean generated = columnsRs.getString("IS_GENERATEDCOLUMN").equals("YES");
              field.setGenerated(generated);
            }
          }

          for (final RecordDefinitionImpl recordDefinition : recordDefinitionMap.values()) {
            final String typePath = recordDefinition.getPath();
            final List<String> idFieldNames = idFieldNameMap.get(typePath);
            if (!Property.isEmpty(idFieldNames)) {
              recordDefinition.setIdFieldNames(idFieldNames);
            }
          }

        }
      }
    } catch (final Throwable e) {
      throw new IllegalArgumentException("Unable to load metadata for schema " + dbSchemaName, e);
    }

    return elementsByPath;
  }

  public void setBatchSize(final int batchSize) {
    this.batchSize = batchSize;
  }

  public void setBlobAsString(final boolean blobAsString) {
    this.blobAsString = blobAsString;
  }

  public void setClobAsString(final boolean clobAsString) {
    this.clobAsString = clobAsString;
  }

  public void setCodeTables(final List<AbstractMultiValueCodeTable> codeTables) {
    for (final AbstractMultiValueCodeTable codeTable : codeTables) {
      addCodeTable(codeTable);
    }
  }

  public void setDataSource(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void setExcludeTablePaths(final Collection<String> excludeTablePaths) {
    this.excludeTablePaths = new HashSet<>(excludeTablePaths);
  }

  public void setExcludeTablePaths(final String... excludeTablePaths) {
    setExcludeTablePaths(Arrays.asList(excludeTablePaths));
  }

  public void setExcludeTablePatterns(final String... excludeTablePatterns) {
    this.excludeTablePatterns = new ArrayList<>(Arrays.asList(excludeTablePatterns));
  }

  public void setFlushBetweenTypes(final boolean flushBetweenTypes) {
    this.flushBetweenTypes = flushBetweenTypes;
  }

  public void setLobAsString(final boolean lobAsString) {
    this.lobAsString = lobAsString;
  }

  public void setPrimaryKeySql(final String primaryKeySql) {
    this.primaryKeySql = primaryKeySql;
  }

  public void setPrimaryKeyTableCondition(final String primaryKeyTableCondition) {
    this.primaryKeyTableCondition = primaryKeyTableCondition;
  }

  public void setQuoteNames(final boolean quoteNames) {
    this.quoteNames = quoteNames;
  }

  protected void setSchemaPermissionsSql(final String scehmaPermissionsSql) {
    this.schemaPermissionsSql = scehmaPermissionsSql;
  }

  public void setSchemaTablePermissionsSql(final String tablePermissionsSql) {
    this.schemaTablePermissionsSql = tablePermissionsSql;
  }

  public void setSqlPrefix(final String sqlPrefix) {
    this.sqlPrefix = sqlPrefix;
  }

  public void setSqlSuffix(final String sqlSuffix) {
    this.sqlSuffix = sqlSuffix;
  }

  protected void setUsesSchema(final boolean usesSchema) {
    this.usesSchema = usesSchema;
  }

  @Override
  public JdbcRecordStore setUseUpperCaseNames(final boolean useUpperCaseNames) {
    this.useUpperCaseNames = useUpperCaseNames;
    return this;
  }

  protected String toUpperIfNeeded(final String name) {
    String fieldName;
    if (isUseUpperCaseNames()) {
      fieldName = name.toUpperCase();
    } else {
      fieldName = name;
    }
    return fieldName;
  }

}
