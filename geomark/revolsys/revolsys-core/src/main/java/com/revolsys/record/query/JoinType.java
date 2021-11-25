package com.revolsys.record.query;

import org.jeometry.common.io.PathName;

import com.revolsys.record.schema.RecordDefinitionProxy;

public enum JoinType {

  CROSS_JOIN, INNER_JOIN, LEFT_OUTER_JOIN, RIGHT_OUTER_JOIN, FULL_OUTER_JOIN;

  static JoinType JOIN = INNER_JOIN;

  private String sql;

  private JoinType() {
    this.sql = name().replace('_', ' ');
  }

  public Join build(final PathName tablePath) {
    return new Join(this).tablePath(tablePath);
  }

  public Join build(final RecordDefinitionProxy recordDefinition) {
    return new Join(this).recordDefinition(recordDefinition);
  }

  public Join build(final TableReference table) {
    return new Join(this).table(table);
  }

  public String getSql() {
    return this.sql;
  }

  @Override
  public String toString() {
    return this.sql;
  }
}
