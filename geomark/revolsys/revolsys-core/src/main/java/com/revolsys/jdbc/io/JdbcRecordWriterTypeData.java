package com.revolsys.jdbc.io;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jeometry.common.logging.Logs;

import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.util.LongCounter;

public class JdbcRecordWriterTypeData {

  private int batchCount = 0;

  private boolean closed = false;

  private final String sql;

  private final PreparedStatement statement;

  private final List<Record> records = new ArrayList<>();

  private final JdbcRecordDefinition recordDefinition;

  private final boolean hasGeneratedKeys;

  private final AbstractJdbcRecordStore recordStore;

  private final LongCounter counter;

  private final int batchSize;

  private final JdbcRecordWriter writer;

  public JdbcRecordWriterTypeData(final JdbcRecordWriter writer,
    final JdbcRecordDefinition recordDefinition, final String sql,
    final PreparedStatement statement, final boolean hasGeneratedKeys) {
    this.writer = writer;
    this.recordStore = writer.getRecordStore();
    this.batchSize = writer.getBatchSize();
    this.counter = writer.getCounter(recordDefinition);
    this.recordDefinition = recordDefinition;
    this.sql = sql;
    this.statement = statement;
    this.hasGeneratedKeys = hasGeneratedKeys;
  }

  public synchronized int addCount() {
    this.batchCount++;
    if (this.batchCount >= this.batchSize) {
      processCurrentBatch();
    }
    return this.batchCount;
  }

  public void clear() {
    this.batchCount = 0;
    this.records.clear();
  }

  public synchronized void close() {
    if (!this.closed) {
      this.closed = true;
      try {
        processCurrentBatch();

      } finally {
        try {
          if (!this.statement.isClosed()) {
            this.statement.close();
          }
        } catch (final SQLException e) {
          throw this.writer.connection.getException("Process Batch", this.sql, e);
        }
      }
    }
  }

  protected void executeUpdate() throws SQLException {
    if (this.batchSize > 1) {
      this.statement.addBatch();
      addCount();
    } else {
      this.recordStore.executeUpdate(this.statement);
    }
  }

  public void flush() {
    processCurrentBatch();
  }

  public int getBatchCount() {
    return this.batchCount;
  }

  public JdbcRecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public List<Record> getRecords() {
    return this.records;
  }

  public String getSql() {
    return this.sql;
  }

  public PreparedStatement getStatement() {
    return this.statement;
  }

  public void insertRecord(final Record record) throws SQLException {
    if (this.batchSize > 1) {
      this.statement.addBatch();
      if (this.hasGeneratedKeys) {
        this.records.add(record);
      }
      addCount();
    } else {
      try {
        this.recordStore.executeUpdate(this.statement);
      } catch (final SQLException e) {
        throw e;
      }
      try (
        final ResultSet generatedKeyResultSet = this.statement.getGeneratedKeys()) {
        if (generatedKeyResultSet.next()) {
          setGeneratedValues(generatedKeyResultSet, record);
        }
      }
    }
  }

  public boolean isHasGeneratedKeys() {
    return this.hasGeneratedKeys;
  }

  private void processCurrentBatch() {
    if (this.batchCount > 0) {
      try {
        this.counter.add(this.batchCount);
        this.recordStore.execteBatch(this.statement);
        if (this.hasGeneratedKeys && !this.records.isEmpty()) {
          try (
            final ResultSet generatedKeyResultSet = this.statement.getGeneratedKeys()) {
            int recordIndex = 0;
            while (generatedKeyResultSet.next() && recordIndex < this.records.size()) {
              final Record record = this.records.get(recordIndex++);
              setGeneratedValues(generatedKeyResultSet, record);
            }
          }
        }
      } catch (final SQLException e) {
        throw this.writer.connection.getException("Process Batch", this.sql, e);
      } catch (final RuntimeException e) {
        Logs.error(this, this.sql, e);
        throw e;
      } finally {
        clear();
      }
    }
  }

  protected void setGeneratedValues(final ResultSet rs, final Record record) throws SQLException {
    final RecordState recordState = record.setState(RecordState.INITIALIZING);
    try {
      final ResultSetMetaData metaData = rs.getMetaData();
      for (int i = 1; i <= metaData.getColumnCount(); i++) {
        final String name = metaData.getColumnName(i);
        final Object value = rs.getObject(i);
        record.setValue(name, value);
      }
    } finally {
      record.setState(recordState);
    }
  }
}
