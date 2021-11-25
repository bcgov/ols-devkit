package com.revolsys.jdbc.io;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import org.apache.commons.dbcp2.BasicDataSource;

public class DataSourceImpl extends BasicDataSource {

  @Override
  public synchronized Connection getConnection() throws SQLException {
    try {
      return super.getConnection();
    } catch (final Exception e) {
      final Throwable cause = e.getCause();
      if (cause instanceof NoSuchElementException
        && "Timeout waiting for idle object".equals(cause.getMessage())) {
        // Retry once on timeout
        return super.getConnection();
      } else {
        throw e;
      }
    }
  }

  @Override
  public synchronized Connection getConnection(final String user, final String pass)
    throws SQLException {
    return super.getConnection(user, pass);
  }
}
