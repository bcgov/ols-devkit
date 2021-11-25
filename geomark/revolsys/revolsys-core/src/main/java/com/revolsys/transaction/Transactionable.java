package com.revolsys.transaction;

import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.transaction.PlatformTransactionManager;

public interface Transactionable {
  PlatformTransactionManager getTransactionManager();

  /**
   * Construct a new {@link Transaction} with {@link TransactionOptions#DEFAULT}.
   * @return The transaction.
   */
  default Transaction newTransaction() {
    return newTransaction(TransactionOptions.DEFAULT);
  }

  /**
   * Construct a new {@link Transaction} with the specified {@link TransactionOption}.
   *
   * Default values are
   *
   * <dl>
   * <dt>{@link Propagation}</dt>
   * <dd>{@link Propagation#REQUIRES_NEW}</dd>
   * <dt>{@link Isolation}</dt>
   * <dd>{@link Isolation#DEFAULT}</dd>
   * </dl>
   *
   * @param options The transaction options.
   * @return The transaction.
   * @see TransactionOption
   * @see TransactionOptions
   * @see Propagation
   * @see Isolation
   */
  default Transaction newTransaction(final TransactionOption... options) {
    final PlatformTransactionManager transactionManager = getTransactionManager();
    return new Transaction(transactionManager, options);
  }

  default void transactionExecute(final Consumer<Transaction> action,
    final TransactionOption... options) {
    try (
      Transaction transaction = newTransaction(options)) {
      transaction.execute(action);
    }
  }

  default <V> V transactionExecute(final Function<Transaction, V> action,
    final TransactionOption... options) {
    try (
      Transaction transaction = newTransaction(options)) {
      return transaction.execute(action);
    }
  }
}
