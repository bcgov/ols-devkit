package com.revolsys.jdbc.io;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.jeometry.common.function.Function3;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;

public abstract class AbstractJdbcDatabaseFactory implements JdbcDatabaseFactory {

  protected final Map<String, Function3<String, String, SQLException, DataAccessException>> sqlStateExceptionFactories = new HashMap<>();

  public AbstractJdbcDatabaseFactory() {
    addSqlStateExceptionFactories(BadSqlGrammarException::new, //
      "07", // Dynamic SQL error
      "21", // Cardinality violation
      "2A", // Syntax error direct SQL
      "37", // Syntax error dynamic SQL
      "42", // General SQL syntax error
      "65" // Oracle: unknown identifier
    );

    addSqlStateExceptionMessageFactories(DataIntegrityViolationException::new, //
      "01", // Data truncation
      "02", // No data found
      "22", // Value out of range
      "23", // Integrity constraint violation
      "27", // Triggered data change violation
      "44" // With check violation
    );

    addSqlStateExceptionMessageFactories(DataAccessResourceFailureException::new, //
      "08", // Connection exception
      "53", // PostgreSQL: insufficient resources (e.g. disk full)
      "54", // PostgreSQL: program limit exceeded (e.g. statement too complex)
      "57", // DB2: out-of-memory exception / database not started
      "58" // DB2: unexpected system error
    );

    addSqlStateExceptionMessageFactories(TransientDataAccessResourceException::new, //
      "JW", // Sybase: internal I/O error
      "JZ", // Sybase: unexpected I/O error
      "S1" // DB2: communication failure
    );

    addSqlStateExceptionMessageFactories(ConcurrencyFailureException::new, //
      "40", // Transaction rollback
      "61" // Oracle: deadlock
    );
  }

  protected void addSqlStateExceptionFactories(
    final Function3<String, String, SQLException, DataAccessException> factory,
    final String... sqlStates) {
    for (final String sqlState : sqlStates) {
      this.sqlStateExceptionFactories.put(sqlState, factory);
    }
  }

  protected void addSqlStateExceptionMessageFactories(
    final BiFunction<String, SQLException, DataAccessException> constructor,
    final String... sqlStates) {
    final Function3<String, String, SQLException, DataAccessException> factory = newFactoryMessage(
      constructor);
    addSqlStateExceptionFactories(factory, sqlStates);
  }

  protected final Function3<String, String, SQLException, DataAccessException> newFactoryMessage(
    final BiFunction<String, SQLException, DataAccessException> function) {
    return (task, sql, exception) -> {
      final String message = newMessage(task, sql, exception);
      return function.apply(message, exception);
    };

  }

  @Override
  public DataAccessException translateException(final String task, final String sql,
    final SQLException exception) {
    return translateSqlStateException(task, sql, exception);
  }

  public DataAccessException translateSqlStateException(final String task, final String sql,
    final SQLException exception) {
    final String sqlState = exception.getSQLState();
    Function3<String, String, SQLException, DataAccessException> factory = this.sqlStateExceptionFactories
      .get(sqlState);
    if (factory == null) {
      factory = this.sqlStateExceptionFactories.get(sqlState.substring(0, 2));
      if (factory == null) {
        return new UncategorizedSQLException(task, sql, exception);
      }
    }
    return factory.apply(task, sql, exception);
  }
}
