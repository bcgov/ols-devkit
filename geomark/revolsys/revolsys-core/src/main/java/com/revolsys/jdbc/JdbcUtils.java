package com.revolsys.jdbc;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.ObjectName;
import javax.sql.DataSource;

import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.io.PathUtil;
import com.revolsys.jdbc.exception.JdbcExceptionTranslator;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.jdbc.field.JdbcFieldDefinitions;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.TableReference;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public final class JdbcUtils {

  public static void addOrderBy(final Query query, final StringBuilder sql,
    final TableReference table, final Map<QueryValue, Boolean> orderBy) {
    if (!orderBy.isEmpty()) {
      sql.append(" ORDER BY ");
      appendOrderByFields(query, sql, table, orderBy);
    }
  }

  public static StringBuilder appendOrderByFields(final Query query, final StringBuilder sql,
    final TableReference table, final Map<QueryValue, Boolean> orderBy) {
    boolean first = true;
    for (final Entry<QueryValue, Boolean> entry : orderBy.entrySet()) {
      if (first) {
        first = false;
      } else {
        sql.append(", ");
      }

      final QueryValue field = entry.getKey();
      table.appendQueryValue(query, sql, field);

      final Boolean ascending = entry.getValue();
      if (!ascending) {
        sql.append(" DESC");
      }
    }
    return sql;
  }

  public static void appendQueryValue(final StringBuilder sql, final Query query,
    final QueryValue queryValue) {
    final RecordDefinition recordDefinition = query.getRecordDefinition();
    if (recordDefinition == null) {
      queryValue.appendSql(query, null, sql);
    } else {
      final RecordStore recordStore = recordDefinition.getRecordStore();
      queryValue.appendSql(query, recordStore, sql);
    }
  }

  public static void appendWhere(final StringBuilder sql, final Query query) {
    final Condition where = query.getWhereCondition();
    if (!where.isEmpty()) {
      sql.append(" WHERE ");
      appendQueryValue(sql, query, where);
    }
  }

  public static String cleanObjectName(final String objectName) {
    return objectName.replaceAll("[^a-zA-Z\\._]", "");
  }

  public static void close(final ResultSet resultSet) {
    if (resultSet != null) {
      try {
        resultSet.close();
      } catch (final SQLException e) {
        Logs.debug(JdbcUtils.class, "SQL error closing result set", e);
      } catch (final Throwable e) {
        Logs.debug(JdbcUtils.class, "Unknown error closing result set", e);
      }
    }
  }

  public static void close(final Statement statement) {
    if (statement != null) {
      try {
        statement.close();
      } catch (final SQLException e) {
        Logs.debug(JdbcUtils.class, "SQL error closing statement", e);
      } catch (final Throwable e) {
        Logs.debug(JdbcUtils.class, "Unknown error closing statement", e);
      }
    }
  }

  public static void close(final Statement statement, final ResultSet resultSet) {
    close(resultSet);
    close(statement);
  }

  public static void commit(final Connection connection) {
    try {
      connection.commit();
    } catch (final SQLException e) {
    }
  }

  public static void delete(final Connection connection, final String tableName,
    final String idColumn, final Object id) {

    final String sql = "DELETE FROM " + cleanObjectName(tableName) + " WHERE "
      + cleanObjectName(idColumn) + " = ?";
    try {
      final PreparedStatement statement = connection.prepareStatement(sql);
      try {
        setValue(statement, 1, id);
        statement.executeQuery();
      } catch (final SQLException e) {
        Logs.error(JdbcUtils.class, "Unable to delete:" + sql, e);
        throw new RuntimeException("Unable to delete:" + sql, e);
      } finally {
        close(statement);
        connection.commit();
      }
    } catch (final SQLException e) {
      Logs.error(JdbcUtils.class, "Invalid table name or id column: " + sql, e);
      throw new IllegalArgumentException("Invalid table name or id column: " + sql);
    }

  }

  public static int executeUpdate(final Connection connection, final String sql,
    final Object... parameters) throws SQLException {
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      setParameters(statement, parameters);
      return statement.executeUpdate();
    } finally {
      close(statement);
    }
  }

  public static int executeUpdate(final DataSource dataSource, final String sql,
    final Object... parameters) {
    final Connection connection = getConnection(dataSource);
    try {
      return executeUpdate(connection, sql, parameters);
    } catch (final SQLException e) {
      throw getException(dataSource, "Update", sql, e);
    } finally {
      release(connection, dataSource);
    }
  }

  public static BigDecimal[] getBigDecimalArray(final ResultSet resultSet, final int index)
    throws SQLException {
    final Array array = resultSet.getArray(index);
    return (BigDecimal[])array.getArray();
  }

  public static Connection getConnection(final DataSource dataSource) {
    try {
      Connection connection = DataSourceUtils.doGetConnection(dataSource);
      if (connection.isClosed()) {
        DataSourceUtils.doReleaseConnection(connection, dataSource);
        connection = DataSourceUtils.doGetConnection(dataSource);
      }

      return connection;
    } catch (final SQLException e) {
      throw getException(dataSource, "Get Connection", null, e);
    }
  }

  public static String getDeleteSql(final Query query) {
    final PathName tablePath = query.getTablePath();
    final String dbTableName = getQualifiedTableName(tablePath);

    final StringBuilder sql = new StringBuilder();
    sql.append("DELETE FROM ");
    sql.append(dbTableName);
    sql.append(" T ");
    appendWhere(sql, query);
    return sql.toString();
  }

  public static RuntimeException getException(final DataSource dataSource, final String task,
    final String sql, final SQLException e) {
    SQLExceptionTranslator translator;
    if (dataSource == null) {
      translator = new SQLStateSQLExceptionTranslator();
    } else {
      translator = new JdbcExceptionTranslator(dataSource);
    }
    final DataAccessException exception = translator.translate(task, sql, e);
    if (exception == null) {
      return Exceptions.wrap(Strings.toString("\n", task, sql), e);
    }
    return exception;
  }

  public static String getProductName(final DataSource dataSource) {
    if (dataSource == null) {
      return null;
    } else {
      final Connection connection = getConnection(dataSource);
      try {
        if (connection == null) {
          if (dataSource.getClass().getName().toLowerCase().contains("oracle")) {
            return "Oracle";
          } else if (dataSource.getClass().getName().toLowerCase().contains("postgres")) {
            return "PostgreSQL";
          } else {
            return null;
          }
        } else {
          final DatabaseMetaData metaData = connection.getMetaData();
          return metaData.getDatabaseProductName();
        }
      } catch (final SQLException e) {
        throw new IllegalArgumentException("Unable to get database product name", e);
      } finally {
        release(connection, dataSource);
      }
    }
  }

  public static String getQualifiedTableName(final PathName pathName) {
    if (Property.hasValue(pathName)) {
      final String path = pathName.toString();
      return getQualifiedTableName(path);
    } else {
      return null;
    }
  }

  public static String getQualifiedTableName(final String typePath) {
    if (Property.hasValue(typePath)) {
      final String tableName = typePath.replaceAll("^/+", "");
      return tableName.replaceAll("/", ".");
    } else {
      return null;
    }
  }

  public static String getSchemaName(final String typePath) {
    if (Property.hasValue(typePath)) {
      final String path = PathUtil.getPath(typePath);
      return path.replaceAll("(^/|/$)", "");
    } else {
      return "";
    }
  }

  public static String getTableName(final String typePath) {
    return PathUtil.getName(typePath);
  }

  public static void lockTable(final Connection connection, final String tableName)
    throws SQLException {
    final String sql = "LOCK TABLE " + tableName + " IN SHARE MODE";
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      statement.execute();
    } finally {
      close(statement);
    }
  }

  public static MapEx readMap(final ResultSet rs) throws SQLException {
    final MapEx values = new LinkedHashMapEx();
    final ResultSetMetaData metaData = rs.getMetaData();
    for (int i = 1; i <= metaData.getColumnCount(); i++) {
      final String name = metaData.getColumnName(i);
      final Object value = rs.getObject(i);
      values.put(name, value);
    }
    return values;
  }

  public static void release(final Connection connection, final DataSource dataSource) {
    if (dataSource != null && connection != null) {
      DataSourceUtils.releaseConnection(connection, dataSource);
    }
  }

  public static Date selectDate(final Connection connection, final String sql,
    final Object... parameters) throws SQLException {
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      setParameters(statement, parameters);
      final ResultSet resultSet = statement.executeQuery();
      try {
        if (resultSet.next()) {
          return resultSet.getDate(1);
        } else {
          throw new IllegalArgumentException("Value not found");
        }
      } finally {
        close(resultSet);
      }
    } finally {
      close(statement);
    }
  }

  public static Date selectDate(final DataSource dataSource, final Connection connection,
    final String sql, final Object... parameters) throws SQLException {
    if (dataSource == null) {
      return selectDate(connection, sql, parameters);
    } else {
      return selectDate(dataSource, sql, parameters);
    }
  }

  public static Date selectDate(final DataSource dataSource, final String sql,
    final Object... parameters) throws SQLException {
    final Connection connection = getConnection(dataSource);
    try {
      return selectDate(connection, sql, parameters);
    } finally {
      release(connection, dataSource);
    }
  }

  public static int selectInt(final Connection connection, final String sql,
    final Object... parameters) {
    return selectInt(null, connection, sql, parameters);
  }

  public static int selectInt(final DataSource dataSource, Connection connection, final String sql,
    final Object... parameters) {
    if (dataSource != null) {
      connection = getConnection(dataSource);
    }
    try {
      final PreparedStatement statement = connection.prepareStatement(sql);
      try {
        setParameters(statement, parameters);
        final ResultSet resultSet = statement.executeQuery();
        try {
          if (resultSet.next()) {
            return resultSet.getInt(1);
          } else {
            throw new IllegalArgumentException("Value not found");
          }
        } finally {
          close(resultSet);
        }

      } finally {
        close(statement);
      }
    } catch (final SQLException e) {
      throw getException(dataSource, "selectInt", sql, e);
    } finally {
      if (dataSource != null) {
        release(connection, dataSource);
      }
    }
  }

  public static int selectInt(final DataSource dataSource, final String sql,
    final Object... parameters) {
    return selectInt(dataSource, null, sql, parameters);

  }

  public static <T> List<T> selectList(final Connection connection, final String sql,
    final int columnIndex, final Object... parameters) throws SQLException {
    final List<T> results = new ArrayList<>();
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      setParameters(statement, parameters);
      final ResultSet resultSet = statement.executeQuery();
      try {
        while (resultSet.next()) {
          @SuppressWarnings("unchecked")
          final T value = (T)resultSet.getObject(columnIndex);
          results.add(value);
        }
        return results;
      } finally {
        close(resultSet);
      }
    } finally {
      close(statement);
    }
  }

  public static long selectLong(final Connection connection, final String sql,
    final Object... parameters) throws SQLException {
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      setParameters(statement, parameters);
      final ResultSet resultSet = statement.executeQuery();
      try {
        if (resultSet.next()) {
          return resultSet.getLong(1);
        } else {
          throw new IllegalArgumentException("Value not found");
        }
      } finally {
        close(resultSet);
      }
    } finally {
      close(statement);
    }
  }

  public static long selectLong(final DataSource dataSource, final Connection connection,
    final String sql, final Object... parameters) throws SQLException {
    if (dataSource == null) {
      return selectLong(connection, sql, parameters);
    } else {
      return selectLong(dataSource, sql, parameters);
    }
  }

  public static long selectLong(final DataSource dataSource, final String sql,
    final Object... parameters) throws SQLException {
    final Connection connection = getConnection(dataSource);
    try {
      return selectLong(connection, sql, parameters);
    } finally {
      release(connection, dataSource);
    }
  }

  public static MapEx selectMap(final Connection connection, final String sql,
    final Object... parameters) throws SQLException {
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      setParameters(statement, parameters);
      final ResultSet resultSet = statement.executeQuery();
      try {
        if (resultSet.next()) {
          return readMap(resultSet);
        } else {
          throw new IllegalArgumentException(
            "Value not found for " + sql + " " + Arrays.asList(parameters));
        }
      } finally {
        close(resultSet);
      }
    } finally {
      close(statement);
    }
  }

  public static MapEx selectMap(final DataSource dataSource, final String sql,
    final Object... parameters) throws SQLException {
    final Connection connection = getConnection(dataSource);
    try {
      return selectMap(connection, sql, parameters);
    } finally {
      release(connection, dataSource);
    }
  }

  public static String selectString(final Connection connection, final String sql,
    final Object... parameters) throws SQLException {
    try (
      final PreparedStatement statement = connection.prepareStatement(sql)) {
      setParameters(statement, parameters);
      try (
        final ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          return resultSet.getString(1);
        } else {
          throw new IllegalArgumentException("Value not found");
        }
      }
    }
  }

  public static String selectString(final DataSource dataSource, final Connection connection,
    final String sql, final Object... parameters) throws SQLException {
    if (dataSource == null) {
      return selectString(connection, sql, parameters);
    } else {
      return selectString(dataSource, sql, parameters);
    }
  }

  public static String selectString(final DataSource dataSource, final String sql,
    final Object... parameters) throws SQLException {
    final Connection connection = getConnection(dataSource);
    try {
      return selectString(connection, sql, parameters);
    } finally {
      release(connection, dataSource);
    }
  }

  public static void setParameters(final PreparedStatement statement, final Object... parameters)
    throws SQLException {
    int index = 1;
    for (final Object parameter : parameters) {
      index = setValue(statement, index, parameter);
    }
  }

  public static int setValue(final PreparedStatement statement, final int index, final Object value)
    throws SQLException {
    final JdbcFieldDefinition fieldDefinition = JdbcFieldDefinitions.newFieldDefinition(value);
    return fieldDefinition.setPreparedStatementValue(statement, index, value);
  }

  public static Struct struct(final Connection connection, final String type, final Object... args)
    throws SQLException {
    return connection.createStruct(type, args);
  }

  public static void unregisterDrivers(final ClassLoader classLoader) {
    try {
      final Enumeration<Driver> drivers = DriverManager.getDrivers();
      while (drivers.hasMoreElements()) {
        final Driver driver = drivers.nextElement();
        try {
          final Class<? extends Driver> driverClass = driver.getClass();
          if (driverClass.getClassLoader() == classLoader) {
            DriverManager.deregisterDriver(driver);
          }

        } catch (final Throwable e) {
          Logs.error(JdbcUtils.class, "Unable to unregister driver: " + driver, e);
        }
      }
    } catch (final Throwable e) {
      Logs.error(JdbcUtils.class, "Unable to unregister drivers", e);
    }
    try {
      // Cleanup Oracle MBean
      final ObjectName objectname = new ObjectName("com.oracle.jdbc:type=diagnosability,name="
        + classLoader.getClass().getName() + "@" + Integer.toHexString(classLoader.hashCode()));
      ManagementFactory.getPlatformMBeanServer().unregisterMBean(objectname);
    } catch (final Throwable e) {
      Logs.error(JdbcUtils.class, "Unable to remove Oracle MBean", e);
    }
  }

  private JdbcUtils() {

  }
}
