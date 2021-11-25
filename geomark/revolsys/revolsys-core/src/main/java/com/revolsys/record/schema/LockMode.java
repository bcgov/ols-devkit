package com.revolsys.record.schema;

public enum LockMode {
  NONE(""), //
  FOR_UPDATE(" FOR UPDATE"), //
  FOR_NO_KEY_UPDATE(" FOR NO KEY UPDATE"), //
  FOR_UPDATE_NO_WAIT(" FOR UPDATE NOWAIT"), //
  LOCK_IN_SHARE_MODE(" LOCK IN SHARE MODE"), //
  LOCK_IN_SHARE_MODE_NOWAIT(" LOCK IN SHARE MODE NOWAIT");

  private String clause;

  private LockMode(final String clause) {
    this.clause = clause;
  }

  public StringBuilder append(final StringBuilder string) {
    string.append(this.clause);
    return string;
  }

  public String getClause() {
    return this.clause;
  }
}
