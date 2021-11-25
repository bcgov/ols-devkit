package com.revolsys.record.query;

import java.io.IOException;

import org.jeometry.common.exception.Exceptions;

public class FromAlias implements From {
  private final From from;

  private final String alias;

  public FromAlias(final From from, final String alias) {
    this.from = from;
    this.alias = alias;
  }

  @Override
  public void appendFrom(final Appendable sql) {
    this.from.appendFrom(sql);
  }

  @Override
  public void appendFromWithAlias(final Appendable sql) {
    try {
      appendFrom(sql);
      sql.append(" ");
      sql.append(this.alias);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }
}
