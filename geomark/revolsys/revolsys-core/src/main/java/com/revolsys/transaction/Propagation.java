package com.revolsys.transaction;

import org.springframework.transaction.TransactionDefinition;

public enum Propagation implements TransactionOption {
  /** */
  MANDATORY(TransactionDefinition.PROPAGATION_MANDATORY),
  /** */
  NESTED(TransactionDefinition.PROPAGATION_NESTED),
  /** */
  NEVER(TransactionDefinition.PROPAGATION_NEVER),
  /** */
  NOT_SUPPORTED(TransactionDefinition.PROPAGATION_NOT_SUPPORTED),
  /** */
  REQUIRED(TransactionDefinition.PROPAGATION_REQUIRED),
  /** */
  REQUIRES_NEW(TransactionDefinition.PROPAGATION_REQUIRES_NEW),
  /** */
  SUPPORTS(TransactionDefinition.PROPAGATION_SUPPORTS);

  private final int value;

  Propagation(final int value) {
    this.value = value;
  }

  @Override
  public void initialize(final Transaction transaction) {
    transaction.setPropagation(this);
  }

  public int value() {
    return this.value;
  }
}
