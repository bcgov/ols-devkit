package com.revolsys.record.query;

import java.io.IOException;

import org.jeometry.common.exception.Exceptions;

public class FromSql implements From {

  private final String from;

  public FromSql(final String from) {
    this.from = from;
  }

  @Override
  public void appendFrom(final Appendable sql) {
    try {
      sql.append('(');
      sql.append(this.from);
      sql.append(')');
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }
}
