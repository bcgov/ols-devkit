package com.revolsys.jdbc.exception;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLErrorCodesFactory;

import com.revolsys.jdbc.io.DataSourceImpl;
import com.revolsys.log.LogbackUtil;
import com.revolsys.util.Property;

public class JdbcExceptionTranslator extends SQLErrorCodeSQLExceptionTranslator {
  private static final Map<String, BiFunction<String, SQLException, DataAccessException>> ERROR_CODE_TO_FUNCTION = new HashMap<>();

  static {
    LogbackUtil.setLevel(SQLErrorCodesFactory.class.getName(), "ERROR");
    ERROR_CODE_TO_FUNCTION.put("org.postgresql.Driver-28000",
      UsernameOrPasswordInvalidException::new);
    ERROR_CODE_TO_FUNCTION.put("org.postgresql.Driver-28P01",
      UsernameOrPasswordInvalidException::new);
    ERROR_CODE_TO_FUNCTION.put("org.postgresql.Driver-3D000", DatabaseNotFoundException::new);

    ERROR_CODE_TO_FUNCTION.put("oracle.jdbc.OracleDriver-66000", DatabaseNotFoundException::new);
    ERROR_CODE_TO_FUNCTION.put("oracle.jdbc.OracleDriver-08006", DatabaseNotFoundException::new);
    ERROR_CODE_TO_FUNCTION.put("oracle.jdbc.OracleDriver-72000",
      UsernameOrPasswordInvalidException::new);
  }

  private String driverClassName;

  public JdbcExceptionTranslator(final DataSource dataSource) {
    try {
      setDataSource(dataSource);
    } catch (final Exception e) {
    }
    if (dataSource instanceof DataSourceImpl) {
      final DataSourceImpl dataSourceImpl = (DataSourceImpl)dataSource;
      this.driverClassName = dataSourceImpl.getDriverClassName();
    }
  }

  @Override
  protected DataAccessException customTranslate(final String task, final String sql,
    final SQLException exception) {

    final String sqlState = exception.getSQLState();
    if (sqlState == null) {
      final Throwable cause = exception.getCause();
      if (cause instanceof SQLException) {
        return customTranslate(task, sqlState, (SQLException)cause);
      }
    }
    final BiFunction<String, SQLException, DataAccessException> function = ERROR_CODE_TO_FUNCTION
      .get(this.driverClassName + "-" + sqlState);
    if (function == null) {
      return null;
    } else {
      final StringBuilder message = new StringBuilder();
      if (Property.hasValue(task)) {
        message.append(task);
      }
      if (Property.hasValue(sql)) {
        if (message.length() > 0) {
          message.append("\n  ");
        }
        message.append(sql);
      }
      if (message.length() == 0) {

      }
      final DataAccessException newException = function.apply(message.toString(), exception);
      return newException;
    }
  }
}
