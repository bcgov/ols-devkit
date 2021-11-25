package com.revolsys.jdbc;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.ClientInfoStatus;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;

public class JdbcConnection implements Connection {
  private Connection connection;

  private DataSource dataSource;

  public JdbcConnection(final Connection connection, final DataSource dataSource) {
    this.dataSource = dataSource;
    if (connection == null) {
      if (dataSource == null) {
        this.connection = null;
      } else {
        this.connection = JdbcUtils.getConnection(dataSource);
      }
    } else {
      this.connection = connection;
    }
  }

  public JdbcConnection(final Connection connection, final DataSource dataSource,
    final boolean autoCommit) {
    this(connection, dataSource);
    if (dataSource != null) {
      try {
        this.connection.setAutoCommit(autoCommit);
      } catch (final SQLException e) {
        throw new RuntimeException("Unable to set auto commit");
      }
    }
  }

  public JdbcConnection(final DataSource dataSource) {
    this(null, dataSource);
  }

  public JdbcConnection(final DataSource dataSource, final boolean autoCommit) {
    this(null, dataSource, autoCommit);
  }

  @Override
  public void abort(final Executor executor) throws SQLException {
    getConnection().abort(executor);
  }

  @Override
  public void clearWarnings() throws SQLException {
    getConnection().clearWarnings();
  }

  @Override
  public void close() {
    if (this.connection != null) {
      JdbcUtils.release(this.connection, this.dataSource);
      this.connection = null;
      this.dataSource = null;
    }
  }

  @Override
  public void commit() throws SQLException {
    getConnection().commit();
  }

  @Override
  public Array createArrayOf(final String typeName, final Object[] elements) throws SQLException {
    return getConnection().createArrayOf(typeName, elements);
  }

  @Override
  public Blob createBlob() throws SQLException {
    return getConnection().createBlob();
  }

  @Override
  public Clob createClob() throws SQLException {
    return getConnection().createClob();
  }

  @Override
  public NClob createNClob() throws SQLException {
    return getConnection().createNClob();
  }

  @Override
  public SQLXML createSQLXML() throws SQLException {
    return getConnection().createSQLXML();
  }

  @Override
  public Statement createStatement() throws SQLException {
    return getConnection().createStatement();
  }

  @Override
  public Statement createStatement(final int resultSetType, final int resultSetConcurrency)
    throws SQLException {
    return getConnection().createStatement(resultSetType, resultSetConcurrency);
  }

  @Override
  public Statement createStatement(final int resultSetType, final int resultSetConcurrency,
    final int resultSetHoldability) throws SQLException {
    return createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  @Override
  public Struct createStruct(final String typeName, final Object[] attributes) throws SQLException {
    return getConnection().createStruct(typeName, attributes);
  }

  public int executeCall(final String sql, final Object... parameters) {
    try {
      final PreparedStatement statement = this.connection.prepareCall(sql);
      try {
        JdbcUtils.setParameters(statement, parameters);
        return statement.executeUpdate();
      } finally {
        JdbcUtils.close(statement);
      }
    } catch (final SQLException e) {
      throw getException("Update:\n" + sql, sql, e);
    }
  }

  public int executeUpdate(final String sql, final Object... parameters) {
    try {
      final PreparedStatement statement = this.connection.prepareStatement(sql);
      try {
        JdbcUtils.setParameters(statement, parameters);
        return statement.executeUpdate();
      } finally {
        JdbcUtils.close(statement);
      }
    } catch (final SQLException e) {
      throw getException("Update:\n" + sql, sql, e);
    }
  }

  @Override
  public boolean getAutoCommit() throws SQLException {
    return getConnection().getAutoCommit();
  }

  @Override
  public String getCatalog() throws SQLException {
    return getConnection().getCatalog();
  }

  @Override
  public Properties getClientInfo() throws SQLException {
    return getConnection().getClientInfo();
  }

  @Override
  public String getClientInfo(final String name) throws SQLException {
    return getConnection().getClientInfo(name);
  }

  public Connection getConnection() throws SQLException {
    if (this.connection == null) {
      throw new SQLException("connection is closed", "08003");
    } else {
      return this.connection;
    }
  }

  public DataSource getDataSource() {
    return this.dataSource;
  }

  public DataAccessException getException(final String task, final String sql,
    final SQLException e) {
    SQLExceptionTranslator exceptionTransaltor;
    if (this.dataSource == null) {
      exceptionTransaltor = new SQLStateSQLExceptionTranslator();
    } else {
      exceptionTransaltor = new SQLErrorCodeSQLExceptionTranslator(this.dataSource);
    }
    final DataAccessException translatedException = exceptionTransaltor.translate(task, sql, e);
    if (translatedException == null) {
      return new UncategorizedSQLException(task, sql, e);
    } else {
      return translatedException;
    }
  }

  @Override
  public int getHoldability() throws SQLException {
    return getConnection().getHoldability();
  }

  @Override
  public DatabaseMetaData getMetaData() throws SQLException {
    return getConnection().getMetaData();
  }

  @Override
  public int getNetworkTimeout() throws SQLException {
    return getConnection().getNetworkTimeout();
  }

  @Override
  public String getSchema() throws SQLException {
    return getConnection().getSchema();
  }

  @Override
  public int getTransactionIsolation() throws SQLException {
    return getConnection().getTransactionIsolation();
  }

  @Override
  public Map<String, Class<?>> getTypeMap() throws SQLException {
    return getConnection().getTypeMap();
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    return getConnection().getWarnings();
  }

  @Override
  public boolean isClosed() throws SQLException {
    final Connection connection = this.connection;
    return connection == null || connection.isClosed();
  }

  @Override
  public boolean isReadOnly() throws SQLException {
    return getConnection().isReadOnly();
  }

  @Override
  public boolean isValid(final int timeout) throws SQLException {
    return getConnection().isValid(timeout);
  }

  @Override
  public boolean isWrapperFor(final Class<?> clazz) throws SQLException {
    if (clazz.isAssignableFrom(getConnection().getClass())) {
      return true;
    } else {
      return getConnection().isWrapperFor(clazz);
    }
  }

  @Override
  public String nativeSQL(final String sql) throws SQLException {
    return getConnection().nativeSQL(sql);
  }

  @Override
  public CallableStatement prepareCall(final String sql) throws SQLException {
    return getConnection().prepareCall(sql);
  }

  @Override
  public CallableStatement prepareCall(final String sql, final int resultSetType,
    final int resultSetConcurrency) throws SQLException {
    return getConnection().prepareCall(sql, resultSetType, resultSetConcurrency);
  }

  @Override
  public CallableStatement prepareCall(final String sql, final int resultSetType,
    final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
    return getConnection().prepareCall(sql, resultSetType, resultSetConcurrency,
      resultSetHoldability);
  }

  @Override
  public PreparedStatement prepareStatement(final String sql) throws SQLException {
    return getConnection().prepareStatement(sql);
  }

  @Override
  public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys)
    throws SQLException {
    return getConnection().prepareStatement(sql, autoGeneratedKeys);
  }

  @Override
  public PreparedStatement prepareStatement(final String sql, final int resultSetType,
    final int resultSetConcurrency) throws SQLException {
    return getConnection().prepareStatement(sql, resultSetType, resultSetConcurrency);
  }

  @Override
  public PreparedStatement prepareStatement(final String sql, final int resultSetType,
    final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
    return prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  @Override
  public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes)
    throws SQLException {
    return getConnection().prepareStatement(sql, columnIndexes);
  }

  @Override
  public PreparedStatement prepareStatement(final String sql, final String[] columnNames)
    throws SQLException {
    return getConnection().prepareStatement(sql, columnNames);
  }

  @Override
  public void releaseSavepoint(final Savepoint savepoint) throws SQLException {
    getConnection().releaseSavepoint(savepoint);
  }

  @Override
  public void rollback() throws SQLException {
    getConnection().rollback();
  }

  @Override
  public void rollback(final Savepoint savepoint) throws SQLException {
    getConnection().rollback(savepoint);
  }

  @Override
  public void setAutoCommit(final boolean autoCommit) throws SQLException {
    getConnection().setAutoCommit(autoCommit);
  }

  @Override
  public void setCatalog(final String catalog) throws SQLException {
    getConnection().setCatalog(catalog);
  }

  @Override
  public void setClientInfo(final Properties properties) throws SQLClientInfoException {
    try {
      getConnection().setClientInfo(properties);
    } catch (final SQLException e) {
      final Map<String, ClientInfoStatus> map = Collections.emptyMap();
      throw new SQLClientInfoException(e.getMessage(), e.getSQLState(), map);
    }
  }

  @Override
  public void setClientInfo(final String name, final String value) throws SQLClientInfoException {
    try {
      getConnection().setClientInfo(name, value);
    } catch (final SQLException e) {
      final Map<String, ClientInfoStatus> map = Collections.emptyMap();
      throw new SQLClientInfoException(e.getMessage(), e.getSQLState(), map);
    }
  }

  @Override
  public void setHoldability(final int holdability) throws SQLException {
    getConnection().setHoldability(holdability);
  }

  @Override
  public void setNetworkTimeout(final Executor executor, final int milliseconds)
    throws SQLException {
    getConnection().setNetworkTimeout(executor, milliseconds);
  }

  @Override
  public void setReadOnly(final boolean readOnly) throws SQLException {
    getConnection().setReadOnly(readOnly);
  }

  public int setRole(final String roleName) {
    final String sql = "SET ROLE " + roleName;
    return executeUpdate(sql);
  }

  @Override
  public Savepoint setSavepoint() throws SQLException {
    return getConnection().setSavepoint();
  }

  @Override
  public Savepoint setSavepoint(final String name) throws SQLException {
    return getConnection().setSavepoint(name);
  }

  @Override
  public void setSchema(final String schema) throws SQLException {
    getConnection().setSchema(schema);
  }

  @Override
  public void setTransactionIsolation(final int level) throws SQLException {
    getConnection().setTransactionIsolation(level);
  }

  @Override
  public void setTypeMap(final Map<String, Class<?>> map) throws SQLException {
    getConnection().setTypeMap(map);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T unwrap(final Class<T> clazz) throws SQLException {
    final Connection connection = getConnection();
    if (clazz.isAssignableFrom(connection.getClass())) {
      return (T)connection;
    } else {
      return connection.unwrap(clazz);
    }
  }

}
