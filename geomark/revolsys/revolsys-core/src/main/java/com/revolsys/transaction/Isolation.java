package com.revolsys.transaction;

import org.springframework.transaction.TransactionDefinition;

public enum Isolation implements TransactionOption {
  /** */
  DEFAULT(TransactionDefinition.ISOLATION_DEFAULT),
  /** */
  READ_UNCOMMITTED(TransactionDefinition.ISOLATION_READ_UNCOMMITTED),
  /** */
  READ_COMMITTED(TransactionDefinition.ISOLATION_READ_COMMITTED),
  /** */
  REPEATABLE_READ(TransactionDefinition.ISOLATION_REPEATABLE_READ),
  /** */
  SERIALIZABLE(TransactionDefinition.ISOLATION_SERIALIZABLE);

  private final int value;

  Isolation(final int value) {
    this.value = value;
  }

  @Override
  public void initialize(final Transaction transaction) {
    transaction.setIsolation(this);
  }

  public int value() {
    return this.value;
  }
}
