package com.revolsys.jdbc.exception;

import java.sql.SQLException;

import org.springframework.jdbc.CannotGetJdbcConnectionException;

public class DatabaseNotFoundException extends CannotGetJdbcConnectionException {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public DatabaseNotFoundException(final String message, final SQLException exception) {
    super(message, exception);
  }
}
