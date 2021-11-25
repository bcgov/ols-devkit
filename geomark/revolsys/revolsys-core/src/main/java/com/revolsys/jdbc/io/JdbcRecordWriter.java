package com.revolsys.jdbc.io;

import java.sql.BatchUpdateException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;
import org.springframework.dao.DataAccessException;

import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.jdbc.JdbcConnection;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.property.GlobalIdProperty;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.transaction.Transaction;
import com.revolsys.util.LongCounter;
import com.revolsys.util.count.CategoryLabelCountMap;

public class JdbcRecordWriter extends AbstractRecordWriter {

  protected JdbcConnection connection;

  private boolean flushBetweenTypes = false;

  private String label;

  private JdbcRecordDefinition lastRecordDefinition;

  private boolean quoteColumnNames = true;

  private AbstractJdbcRecordStore recordStore;

  private String sqlPrefix;

  private String sqlSuffix;

  private final int batchSize;

  private CategoryLabelCountMap statistics;

  private boolean throwExceptions = false;

  private Map<JdbcRecordDefinition, JdbcRecordWriterTypeData> typeDeleteData = new HashMap<>();

  private Map<JdbcRecordDefinition, JdbcRecordWriterTypeData> typeInsertData = new HashMap<>();

  private Map<JdbcRecordDefinition, JdbcRecordWriterTypeData> typeInsertSequenceData = new HashMap<>();

  private Map<JdbcRecordDefinition, JdbcRecordWriterTypeData> typeInsertRowIdData = new HashMap<>();

  private Map<JdbcRecordDefinition, JdbcRecordWriterTypeData> typeUpdateData = new HashMap<>();

  private final Map<JdbcRecordDefinition, LongCounter> typeCountMap = new HashMap<>();

  public JdbcRecordWriter(final AbstractJdbcRecordStore recordStore,
    final RecordDefinitionProxy recordDefinition, final CategoryLabelCountMap statistics,
    final int batchSize) {
    super(recordDefinition);
    Transaction.assertInTransaction();
    this.recordStore = recordStore;
    this.statistics = statistics;
    this.connection = recordStore.getJdbcConnection();
    final DataSource dataSource = this.connection.getDataSource();
    if (dataSource != null) {
      try {
        this.connection.setAutoCommit(false);
      } catch (final SQLException e) {
        throw new RuntimeException("Unable to create connection", e);
      }
    }
    this.batchSize = batchSize;
    if (statistics != null) {
      statistics.connect();
    }
  }

  public JdbcRecordWriter(final AbstractJdbcRecordStore recordStore,
    final RecordDefinitionProxy recordDefinition, final int batchSize) {
    this(recordStore, recordDefinition, recordStore.getStatistics(), batchSize);
  }

  public void appendIdEquals(final StringBuilder sqlBuffer, final List<FieldDefinition> idFields) {
    boolean first = true;
    for (final FieldDefinition idField : idFields) {
      if (first) {
        first = false;
      } else {
        sqlBuffer.append(" AND ");
      }
      idField.appendColumnName(sqlBuffer, this.quoteColumnNames);
      sqlBuffer.append(" = ");
      ((JdbcFieldDefinition)idField).addStatementPlaceHolder(sqlBuffer);
    }
  }

  @Override
  @PreDestroy
  public void close() {
    flush();
    closeDo();
    if (this.statistics != null) {
      this.statistics.disconnect();
    }
  }

  private void close(final Map<JdbcRecordDefinition, JdbcRecordWriterTypeData> typeDataMap) {
    for (final JdbcRecordWriterTypeData typeData : typeDataMap.values()) {
      try {
        typeData.close();
      } catch (final DataAccessException e) {
        if (this.throwExceptions) {
          throw e;
        } else {
          Logs.error(this, "Error commiting records", e);
        }
      } finally {
        JdbcUtils.close(typeData.getStatement());
      }
    }
  }

  protected synchronized void closeDo() {
    if (this.recordStore != null) {
      try {
        close(this.typeInsertData);

        close(this.typeInsertData);

        close(this.typeInsertRowIdData);

        close(this.typeUpdateData);

        close(this.typeDeleteData);

        if (this.statistics != null) {
          this.statistics.disconnect();
          this.statistics = null;
        }
      } finally {
        this.typeInsertData = null;
        this.typeInsertSequenceData = null;
        this.typeInsertRowIdData = null;
        this.typeUpdateData = null;
        this.typeDeleteData = null;
        this.recordStore = null;
        if (this.connection != null) {
          final DataSource dataSource = this.connection.getDataSource();
          try {
            if (dataSource != null && !Transaction.isHasCurrentTransaction()) {
              this.connection.commit();
            }
          } catch (final SQLException e) {
            throw new RuntimeException("Failed to commit data:", e);
          } finally {
            FileUtil.closeSilent(this.connection);
            this.connection = null;
          }
        }
      }
    }
  }

  public synchronized void commit() {
    flush();
    JdbcUtils.commit(this.connection);
  }

  private void deleteRecord(final JdbcRecordDefinition recordDefinition, final Record record)
    throws SQLException {
    flushIfRequired(recordDefinition);
    JdbcRecordWriterTypeData data = this.typeDeleteData.get(recordDefinition);
    if (data == null) {
      final String sql = getDeleteSql(recordDefinition);
      try {
        final PreparedStatement statement = this.connection.prepareStatement(sql);
        data = new JdbcRecordWriterTypeData(this, recordDefinition, sql, statement, false);
        this.typeDeleteData.put(recordDefinition, data);
      } catch (final SQLException e) {
        this.connection.getException("Prepare Delete SQL", sql, e);
      }
    }
    final PreparedStatement statement = data.getStatement();
    setIdEqualsValues(statement, 1, recordDefinition, record);
    data.executeUpdate();
    this.recordStore.addStatistic("Delete", record);
  }

  @Override
  public synchronized void flush() {
    flush(this.typeInsertData);

    flush(this.typeInsertSequenceData);

    flush(this.typeInsertRowIdData);

    flush(this.typeUpdateData);

    flush(this.typeDeleteData);
  }

  private void flush(final Map<JdbcRecordDefinition, JdbcRecordWriterTypeData> typeDataMap) {
    if (typeDataMap != null) {
      for (final JdbcRecordWriterTypeData data : typeDataMap.values()) {
        try {
          data.flush();
        } catch (final DataAccessException e) {
          if (this.throwExceptions) {
            throw e;
          } else {
            Logs.error(this, "Error writing to database", e);
          }
        }
      }
    }
  }

  private void flushIfRequired(final JdbcRecordDefinition recordDefinition) {
    if (this.flushBetweenTypes && recordDefinition != this.lastRecordDefinition) {
      flush();
      this.lastRecordDefinition = recordDefinition;
    }
  }

  public int getBatchSize() {
    return this.batchSize;
  }

  protected synchronized LongCounter getCounter(final JdbcRecordDefinition recordDefinition) {
    LongCounter counter = this.typeCountMap.get(recordDefinition);
    if (counter == null) {
      counter = new LongCounter(recordDefinition.getName());
      this.typeCountMap.put(recordDefinition, counter);
    }
    return counter;
  }

  private String getDeleteSql(final JdbcRecordDefinition recordDefinition) {
    final List<FieldDefinition> idFields = recordDefinition.getIdFields();
    if (idFields.isEmpty()) {
      throw new RuntimeException("No primary key found for: " + recordDefinition);
    } else {
      final String tableName = recordDefinition.getDbTableQualifiedName();
      final StringBuilder sqlBuffer = new StringBuilder();
      if (this.sqlPrefix != null) {
        sqlBuffer.append(this.sqlPrefix);
      }
      sqlBuffer.append("delete ");
      sqlBuffer.append(" from ");
      sqlBuffer.append(tableName);
      sqlBuffer.append(" where ");
      appendIdEquals(sqlBuffer, idFields);
      sqlBuffer.append(" ");
      if (this.sqlSuffix != null) {
        sqlBuffer.append(this.sqlSuffix);
      }
      return sqlBuffer.toString();
    }
  }

  private String getInsertSql(final JdbcRecordDefinition recordDefinition,
    final boolean generatePrimaryKey) {
    final JdbcRecordStore recordStore = this.recordStore;
    final String tableName = recordDefinition.getDbTableQualifiedName();
    final boolean hasRowIdField = recordStore.isIdFieldRowid(recordDefinition);
    final StringBuilder sqlBuffer = new StringBuilder();
    if (this.sqlPrefix != null) {
      sqlBuffer.append(this.sqlPrefix);
    }
    sqlBuffer.append("insert ");

    sqlBuffer.append(" into ");
    sqlBuffer.append(tableName);
    sqlBuffer.append(" (");
    boolean first = true;
    for (final FieldDefinition fieldDefinition : recordDefinition.getFields()) {
      final JdbcFieldDefinition jdbcField = (JdbcFieldDefinition)fieldDefinition;
      if (!jdbcField.isGenerated()) {
        if (!(hasRowIdField && fieldDefinition.isIdField())) {
          if (first) {
            first = false;
          } else {
            sqlBuffer.append(',');
          }
          fieldDefinition.appendColumnName(sqlBuffer, this.quoteColumnNames);
        }
      }
    }

    sqlBuffer.append(") VALUES (");
    first = true;
    for (final FieldDefinition fieldDefinition : recordDefinition.getFields()) {
      final JdbcFieldDefinition jdbcField = (JdbcFieldDefinition)fieldDefinition;
      if (!jdbcField.isGenerated()) {
        final boolean idField = fieldDefinition.isIdField();
        if (!(hasRowIdField && idField)) {
          if (first) {
            first = false;
          } else {
            sqlBuffer.append(',');
          }
          if (idField && generatePrimaryKey) {
            final String primaryKeySql = recordStore.getGeneratePrimaryKeySql(recordDefinition);
            sqlBuffer.append(primaryKeySql);
          } else {
            jdbcField.addInsertStatementPlaceHolder(sqlBuffer, generatePrimaryKey);
          }
        }
      }
    }
    sqlBuffer.append(")");
    if (this.sqlSuffix != null) {
      sqlBuffer.append(this.sqlSuffix);
    }
    return sqlBuffer.toString();
  }

  public String getLabel() {
    return this.label;
  }

  private JdbcRecordDefinition getRecordDefinition(final PathName typePath) {
    if (this.recordStore == null) {
      return null;
    } else {
      final JdbcRecordDefinition recordDefinition = this.recordStore.getRecordDefinition(typePath);
      return recordDefinition;
    }
  }

  private JdbcRecordDefinition getRecordDefinition(final Record record) {
    return getRecordDefinition(record.getPathName());
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends RecordStore> R getRecordStore() {
    return (R)this.recordStore;
  }

  public String getSqlPrefix() {
    return this.sqlPrefix;
  }

  public String getSqlSuffix() {
    return this.sqlSuffix;
  }

  private String getUpdateSql(final JdbcRecordDefinition recordDefinition) {
    final List<FieldDefinition> idFields = recordDefinition.getIdFields();
    if (idFields.isEmpty()) {
      throw new RuntimeException("No primary key found for: " + recordDefinition);
    } else {
      final String tableName = recordDefinition.getDbTableQualifiedName();
      final StringBuilder sqlBuffer = new StringBuilder();
      if (this.sqlPrefix != null) {
        sqlBuffer.append(this.sqlPrefix);
      }
      sqlBuffer.append("update ");

      sqlBuffer.append(tableName);
      sqlBuffer.append(" set ");
      boolean first = true;
      for (final FieldDefinition fieldDefinition : recordDefinition.getFields()) {
        if (!idFields.contains(fieldDefinition)) {
          final JdbcFieldDefinition jdbcFieldDefinition = (JdbcFieldDefinition)fieldDefinition;
          if (!jdbcFieldDefinition.isGenerated()) {
            if (first) {
              first = false;
            } else {
              sqlBuffer.append(", ");
            }
            jdbcFieldDefinition.appendColumnName(sqlBuffer, this.quoteColumnNames);
            sqlBuffer.append(" = ");
            jdbcFieldDefinition.addInsertStatementPlaceHolder(sqlBuffer, false);
          }
        }
      }
      sqlBuffer.append(" where ");
      appendIdEquals(sqlBuffer, idFields);

      sqlBuffer.append(" ");
      if (this.sqlSuffix != null) {
        sqlBuffer.append(this.sqlSuffix);
      }
      return sqlBuffer.toString();

    }
  }

  protected void insert(final JdbcRecordDefinition recordDefinition, final Record record)
    throws SQLException {
    flushIfRequired(recordDefinition);

    GlobalIdProperty.setIdentifier(record);

    final boolean hasId = recordDefinition.hasIdField();
    if (this.recordStore.isIdFieldRowid(recordDefinition)) {
      insertRowId(record, recordDefinition);
    } else if (!hasId) {
      insert(record, recordDefinition);
    } else {
      boolean hasIdValue = true;
      for (final String idFieldName : recordDefinition.getIdFieldNames()) {
        if (!record.hasValue(idFieldName)) {
          hasIdValue = false;
        }
      }
      if (hasIdValue) {
        insert(record, recordDefinition);
      } else {
        insertRecordSequence(record, recordDefinition);
      }
    }
    record.setState(RecordState.PERSISTED);
    this.recordStore.addStatistic("Insert", record);
  }

  protected void insert(final Record record, final JdbcRecordDefinition recordDefinition)
    throws SQLException {
    final JdbcRecordWriterTypeData data = insertStatementGet(recordDefinition, this.typeInsertData,
      false, recordDefinition.isHasGeneratedFields());
    if (data != null) {
      final PreparedStatement statement = data.getStatement();
      int parameterIndex = 1;
      for (final FieldDefinition fieldDefinition : recordDefinition.getFields()) {
        final JdbcFieldDefinition jdbcField = (JdbcFieldDefinition)fieldDefinition;
        if (!jdbcField.isGenerated()) {
          parameterIndex = jdbcField.setInsertPreparedStatementValue(statement, parameterIndex,
            record);
        }
      }
      data.insertRecord(record);
    }
  }

  private void insertRecordSequence(final Record record,
    final JdbcRecordDefinition recordDefinition) throws SQLException {
    final JdbcRecordWriterTypeData data = insertStatementGet(recordDefinition,
      this.typeInsertSequenceData, true, true);
    if (data != null) {
      final PreparedStatement statement = data.getStatement();
      insertSetValuesNonId(record, recordDefinition, statement);
      data.insertRecord(record);
    }
  }

  private void insertRowId(final Record record, final JdbcRecordDefinition recordDefinition)
    throws SQLException {
    final JdbcRecordWriterTypeData data = insertStatementGet(recordDefinition,
      this.typeInsertRowIdData, false, true);
    if (data != null) {
      final PreparedStatement statement = data.getStatement();
      insertSetValuesNonId(record, recordDefinition, statement);
      data.insertRecord(record);
    }
  }

  private void insertSetValuesNonId(final Record record,
    final JdbcRecordDefinition recordDefinition, final PreparedStatement statement)
    throws SQLException {
    int parameterIndex = 1;
    for (final FieldDefinition fieldDefinition : recordDefinition.getFields()) {
      if (!fieldDefinition.isIdField()) {
        parameterIndex = ((JdbcFieldDefinition)fieldDefinition)
          .setInsertPreparedStatementValue(statement, parameterIndex, record);
      }
    }
  }

  private JdbcRecordWriterTypeData insertStatementGet(final JdbcRecordDefinition recordDefinition,
    final Map<JdbcRecordDefinition, JdbcRecordWriterTypeData> typeDataMap,
    final boolean generatePrimaryKey, final boolean returnGeneratedKeys) {

    JdbcRecordWriterTypeData data = typeDataMap.get(recordDefinition);
    if (data == null) {
      final String sql = getInsertSql(recordDefinition, generatePrimaryKey);
      try {
        PreparedStatement statement;
        if (returnGeneratedKeys) {
          statement = this.recordStore.insertStatementPrepareRowId(this.connection,
            recordDefinition, sql);
        } else {
          statement = this.connection.prepareStatement(sql);
        }
        data = new JdbcRecordWriterTypeData(this, recordDefinition, sql, statement,
          returnGeneratedKeys);
        typeDataMap.put(recordDefinition, data);
      } catch (final SQLException e) {
        throw this.connection.getException("Prepare Insert SQL", sql, e);
      }
    }
    return data;
  }

  public boolean isFlushBetweenTypes() {
    return this.flushBetweenTypes;
  }

  public boolean isQuoteColumnNames() {
    return this.quoteColumnNames;
  }

  public boolean isThrowExceptions() {
    return this.throwExceptions;
  }

  public void setFlushBetweenTypes(final boolean flushBetweenTypes) {
    this.flushBetweenTypes = flushBetweenTypes;
  }

  private int setIdEqualsValues(final PreparedStatement statement, int parameterIndex,
    final JdbcRecordDefinition recordDefinition, final Record record) throws SQLException {
    for (final FieldDefinition idField : recordDefinition.getIdFields()) {
      final Object value = record.getValue(idField);
      parameterIndex = ((JdbcFieldDefinition)idField).setPreparedStatementValue(statement,
        parameterIndex++, value);
    }
    return parameterIndex;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  public void setQuoteColumnNames(final boolean quoteColumnNames) {
    this.quoteColumnNames = quoteColumnNames;
  }

  public void setSqlPrefix(final String sqlPrefix) {
    this.sqlPrefix = sqlPrefix;
  }

  public void setSqlSuffix(final String sqlSuffix) {
    this.sqlSuffix = sqlSuffix;
  }

  public void setThrowExceptions(final boolean throwExceptions) {
    this.throwExceptions = throwExceptions;
  }

  @Override
  public String toString() {
    if (this.recordStore == null) {
      return super.toString();
    } else {
      return this.recordStore.toString() + " writer";
    }
  }

  private void updateRecord(final JdbcRecordDefinition recordDefinition, final Record record)
    throws SQLException {
    flushIfRequired(recordDefinition);
    JdbcRecordWriterTypeData data = this.typeUpdateData.get(recordDefinition);
    if (data == null) {
      final String sql = getUpdateSql(recordDefinition);
      try {
        final PreparedStatement statement = this.connection.prepareStatement(sql);
        data = new JdbcRecordWriterTypeData(this, recordDefinition, sql, statement, false);
        this.typeUpdateData.put(recordDefinition, data);
      } catch (final SQLException e) {
        this.connection.getException("Prepare Update SQL", sql, e);
      }
    }
    final PreparedStatement statement = data.getStatement();
    int parameterIndex = 1;
    for (final FieldDefinition fieldDefinition : recordDefinition.getFields()) {
      if (!fieldDefinition.isIdField()) {
        final JdbcFieldDefinition jdbcFieldDefinition = (JdbcFieldDefinition)fieldDefinition;
        if (!jdbcFieldDefinition.isGenerated()) {
          parameterIndex = jdbcFieldDefinition.setInsertPreparedStatementValue(statement,
            parameterIndex, record);
        }
      }
    }
    parameterIndex = setIdEqualsValues(statement, parameterIndex, recordDefinition, record);
    data.executeUpdate();
    this.recordStore.addStatistic("Update", record);
  }

  @Override
  public synchronized void write(final Record record) {
    try {
      final JdbcRecordDefinition recordDefinition = getRecordDefinition(record);
      final RecordState state = record.getState();
      if (record.getRecordStore() != this.recordStore) {
        if (state != RecordState.DELETED) {
          insert(recordDefinition, record);
        }
      } else {
        switch (state) {
          case NEW:
            insert(recordDefinition, record);
          break;
          case MODIFIED:
            updateRecord(recordDefinition, record);
          break;
          case PERSISTED:
          // No action required
          break;
          case DELETED:
            deleteRecord(recordDefinition, record);
          break;
          default:
            throw new IllegalStateException("State not known");
        }
      }
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Error e) {
      throw e;
    } catch (final BatchUpdateException e) {
      for (SQLException e1 = e.getNextException(); e1 != null; e1 = e1.getNextException()) {
        Logs.error(this, "Unable to write", e1);
      }
      throw new RuntimeException("Unable to write\n" + record, e);
    } catch (final Exception e) {
      throw new RuntimeException("Unable to write\n" + record, e);
    }
  }
}
