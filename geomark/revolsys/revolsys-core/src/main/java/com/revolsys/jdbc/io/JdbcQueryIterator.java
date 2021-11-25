package com.revolsys.jdbc.io;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.io.PathName;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.UncategorizedSQLException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.io.FileUtil;
import com.revolsys.jdbc.JdbcConnection;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.RecordState;
import com.revolsys.record.io.RecordIterator;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.query.ColumnIndexes;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.transaction.Transaction;
import com.revolsys.util.Booleans;
import com.revolsys.util.count.LabelCounters;

public class JdbcQueryIterator extends AbstractIterator<Record>
  implements RecordReader, RecordIterator {
  public static Record getNextRecord(final JdbcRecordStore recordStore,
    final RecordDefinition recordDefinition, final List<QueryValue> expressions,
    final RecordFactory<Record> recordFactory, final ResultSet resultSet,
    final boolean internStrings) {
    final Record record = recordFactory.newRecord(recordDefinition);
    if (record != null) {
      record.setState(RecordState.INITIALIZING);
      final ColumnIndexes indexes = new ColumnIndexes();
      int fieldIndex = 0;
      for (final QueryValue expression : expressions) {
        try {
          final Object value = expression.getValueFromResultSet(recordDefinition, resultSet,
            indexes, internStrings);
          record.setValue(fieldIndex, value);
          fieldIndex++;
        } catch (final SQLException e) {
          throw new RuntimeException(
            "Unable to get value " + indexes.columnIndex + " from result set", e);
        }
      }
      record.setState(RecordState.PERSISTED);
      recordStore.addStatistic("query", record);
    }
    return record;
  }

  private boolean autoCommit;

  private boolean internStrings;

  private JdbcConnection connection;

  private final int currentQueryIndex = -1;

  private final int fetchSize = 10;

  private List<QueryValue> selectExpressions = new ArrayList<>();

  private List<Query> queries;

  private Query query;

  private JdbcRecordDefinition recordDefinition;

  private RecordFactory<Record> recordFactory;

  private JdbcRecordStore recordStore;

  private ResultSet resultSet;

  private PreparedStatement statement;

  private LabelCounters labelCountMap;

  public JdbcQueryIterator(final JdbcRecordStore recordStore, final Query query,
    final Map<String, Object> properties) {
    super();
    Transaction.assertInTransaction();

    this.recordFactory = query.getRecordFactory();
    if (this.recordFactory == null) {
      this.recordFactory = recordStore.getRecordFactory();
    }
    this.recordStore = recordStore;
    this.query = query;
    this.labelCountMap = query.getStatistics();
    if (properties != null) {
      this.autoCommit = Booleans.getBoolean(properties.get("autoCommit"));
      this.internStrings = Booleans.getBoolean(properties.get("internStrings"));
      if (this.labelCountMap == null) {
        this.labelCountMap = (LabelCounters)properties.get(LabelCounters.class.getName());
      }
    }
  }

  @Override
  public synchronized void closeDo() {
    JdbcUtils.close(this.statement, this.resultSet);
    FileUtil.closeSilent(this.connection);
    this.selectExpressions = null;
    this.connection = null;
    this.recordFactory = null;
    this.recordStore = null;
    this.recordDefinition = null;
    this.queries = null;
    this.query = null;
    this.resultSet = null;
    this.statement = null;
    this.labelCountMap = null;
  }

  protected String getErrorMessage() {
    if (this.queries == null) {
      return null;
    } else {
      return this.queries.get(this.currentQueryIndex).getSql();
    }
  }

  @Override
  protected Record getNext() throws NoSuchElementException {
    try {
      if (this.resultSet != null && !this.query.isCancelled() && this.resultSet.next()) {
        final Record record = getNextRecord(this.recordStore, this.recordDefinition,
          this.selectExpressions, this.recordFactory, this.resultSet, this.internStrings);
        if (this.labelCountMap != null) {
          this.labelCountMap.addCount(record);
        }
        return record;
      } else {
        close();
        throw new NoSuchElementException();
      }
    } catch (final SQLException e) {
      final boolean cancelled = this.query.isCancelled();
      DataAccessException e2;
      if (cancelled) {
        e2 = null;
      } else {
        final JdbcConnection connection = this.connection;
        final String sql = getErrorMessage();
        if (connection == null) {
          e2 = new UncategorizedSQLException("Get Next", sql, e);
        } else {
          e2 = connection.getException("Get Next", sql, e);
        }
      }
      close();
      if (cancelled) {
        throw new NoSuchElementException();
      } else {
        if (e2 == null) {
          throw Exceptions.wrap(e);
        } else {
          throw e2;
        }
      }
    } catch (final RuntimeException e) {
      close();
      throw e;
    } catch (final Error e) {
      close();
      throw e;
    }
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    if (this.recordDefinition == null) {
      hasNext();
    }
    return this.recordDefinition;
  }

  @Override
  public JdbcRecordStore getRecordStore() {
    return this.recordStore;
  }

  protected ResultSet getResultSet() {
    final Query query = this.query;
    final PathName tableName = query.getTablePath();
    final RecordDefinition queryRecordDefinition = query.getRecordDefinition();
    if (queryRecordDefinition != null) {
      this.recordDefinition = this.recordStore.getRecordDefinition(queryRecordDefinition);
      if (this.recordDefinition != null) {
        query.setRecordDefinition(this.recordDefinition);
      }
    }
    if (this.recordDefinition == null) {
      if (tableName != null) {
        this.recordDefinition = this.recordStore.getRecordDefinition(tableName);
        if (this.recordDefinition != null) {
          query.setRecordDefinition(this.recordDefinition);
        }
      }
    }
    String dbTableName;
    if (this.recordDefinition == null) {
      final PathName pathName = PathName.newPathName(tableName);
      if (pathName == null) {
        dbTableName = null;
      } else {
        dbTableName = pathName.getName();
      }
    } else {
      dbTableName = this.recordDefinition.getDbTableName();
    }

    final String sql = getSql(query);
    try {
      this.statement = this.connection.prepareStatement(sql);
      this.statement.setFetchSize(this.fetchSize);

      this.resultSet = this.recordStore.getResultSet(this.statement, query);
      final ResultSetMetaData resultSetMetaData = this.resultSet.getMetaData();

      if (this.recordDefinition == null || !query.getJoins().isEmpty()
        || this.recordStore != this.recordDefinition.getRecordStore() || query.getSql() != null) {
        this.recordDefinition = this.recordStore.getRecordDefinition(tableName, resultSetMetaData,
          dbTableName);
        query.setRecordDefinition(this.recordDefinition);
      } else if (query.isCustomResult()) {
        this.recordDefinition = this.recordStore.getRecordDefinition(query, resultSetMetaData);
      }
      this.selectExpressions = query.getSelectExpressions();
      if (this.selectExpressions.isEmpty()) {
        this.selectExpressions = (List)this.recordDefinition.getFieldDefinitions();
      }

    } catch (final SQLException e) {
      JdbcUtils.close(this.statement, this.resultSet);
      throw this.connection.getException("Execute Query", sql, e);
    }
    return this.resultSet;
  }

  protected String getSql(final Query query) {
    return query.getSelectSql();
  }

  @Override
  protected void initDo() {
    this.connection = this.recordStore.getJdbcConnection(this.autoCommit);

    this.resultSet = getResultSet();
  }

  public boolean isAutoCommit() {
    return this.autoCommit;
  }

  public boolean isInternStrings() {
    return this.internStrings;
  }

  public void setAutoCommit(final boolean autoCommit) {
    this.autoCommit = autoCommit;
  }

  public void setInternStrings(final boolean internStrings) {
    this.internStrings = internStrings;
  }

  protected void setQuery(final Query query) {
    this.query = query;
  }

}
