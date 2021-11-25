package com.revolsys.transaction;

import java.util.function.Consumer;

public enum TransactionOptions implements TransactionOption {

  ROLLBACK_ONLY(TransactionOptions::rollbackOnly), //
  READ_ONLY(TransactionOptions::readOnly);

  public static TransactionOption[] NONE = {};

  public static TransactionOption[] DEFAULT = {
    Propagation.REQUIRES_NEW, Isolation.DEFAULT
  };

  public static TransactionOption[] REQUIRED_READONLY = {
    Propagation.REQUIRED, READ_ONLY, ROLLBACK_ONLY,
  };

  public static TransactionOption[] REQUIRES_NEW_READONLY = {
    Propagation.REQUIRES_NEW, READ_ONLY, ROLLBACK_ONLY,
  };

  public static TransactionOption[] REQUIRED = {
    Propagation.REQUIRED,
  };

  private static void readOnly(final Transaction transaction) {
    transaction.setReadOnly(true);
  }

  private static void rollbackOnly(final Transaction transaction) {
    transaction.setRollbackOnly();
  }

  private Consumer<Transaction> initializer;

  private TransactionOptions(final Consumer<Transaction> initializer) {
    this.initializer = initializer;
  }

  @Override
  public void initialize(final Transaction transaction) {
    this.initializer.accept(transaction);
  }
}
