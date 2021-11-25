package com.revolsys.transaction;

import java.util.function.Consumer;
import java.util.function.Function;

import org.jeometry.common.exception.Exceptions;
import org.springframework.lang.Nullable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.revolsys.io.BaseCloseable;

public class Transaction implements BaseCloseable, TransactionDefinition {

  private static ThreadLocal<Transaction> currentTransaction = new ThreadLocal<>();

  public static void afterCommit(final Runnable runnable) {
    if (runnable != null) {
      if (TransactionSynchronizationManager.isSynchronizationActive()) {
        final RunnableAfterCommit synchronization = new RunnableAfterCommit(runnable);
        TransactionSynchronizationManager.registerSynchronization(synchronization);
      } else {
        runnable.run();
      }
    }
  }

  public static void assertInTransaction() {
    assert currentTransaction.get() != null : "Must be called in a transaction";
  }

  public static Transaction getCurrentTransaction() {
    return currentTransaction.get();
  }

  public static boolean isHasCurrentTransaction() {
    return getCurrentTransaction() != null;
  }

  public static void setCurrentRollbackOnly() {
    final Transaction transaction = currentTransaction.get();
    if (transaction != null) {
      transaction.setRollbackOnly();
    }
  }

  private Transaction previousTransaction;

  private PlatformTransactionManager transactionManager;

  private DefaultTransactionStatus transactionStatus;

  private Propagation propagation = Propagation.REQUIRES_NEW;

  private Isolation isolation = Isolation.DEFAULT;

  private int timeout = TIMEOUT_DEFAULT;

  private boolean readOnly;

  private boolean rollbackOnly = false;

  @Nullable
  private String name;

  public Transaction(final PlatformTransactionManager transactionManager,
    final TransactionOption... options) {
    for (final TransactionOption option : options) {
      option.initialize(this);
    }
    this.transactionManager = transactionManager;
    if (transactionManager == null) {
      this.transactionStatus = null;
    } else {
      this.transactionStatus = (DefaultTransactionStatus)transactionManager.getTransaction(this);
      if (this.rollbackOnly) {
        this.transactionStatus.setRollbackOnly();
      }
    }
    this.previousTransaction = getCurrentTransaction();
    currentTransaction.set(this);
  }

  @Override
  public void close() throws RuntimeException {
    commit();
    currentTransaction.set(this.previousTransaction);
    this.transactionManager = null;
    this.previousTransaction = null;
    this.transactionStatus = null;
  }

  protected void commit() {
    DefaultTransactionStatus transactionStatus = this.transactionStatus;
    if (this.transactionManager != null && transactionStatus != null) {
      if (!transactionStatus.isCompleted()) {
        if (transactionStatus.isRollbackOnly()) {
          rollback();
        } else {
          try {
            this.transactionManager.commit(transactionStatus);
          } catch (final Throwable e) {
            Exceptions.throwUncheckedException(e);
          }
        }
      }
    }
  }

  public void execute(final Consumer<Transaction> action) {
    try {
      action.accept(this);
    } catch (final Throwable e) {
      setRollbackOnly(e);
    }
  }

  public <V> V execute(final Function<Transaction, V> action) {
    try {
      return action.apply(this);
    } catch (final Throwable e) {
      throw setRollbackOnly(e);
    }
  }

  @Override
  public int getIsolationLevel() {
    return this.isolation.value();
  }

  @Override
  public int getPropagationBehavior() {
    return this.propagation.value();
  }

  @Override
  public int getTimeout() {
    return this.timeout;
  }

  public PlatformTransactionManager getTransactionManager() {
    return this.transactionManager;
  }

  public DefaultTransactionStatus getTransactionStatus() {
    return this.transactionStatus;
  }

  public boolean isCompleted() {
    if (this.transactionStatus == null) {
      return true;
    } else {
      return this.transactionStatus.isCompleted();
    }
  }

  public boolean isPropagation(final Propagation propagation) {
    return propagation == this.propagation;
  }

  @Override
  public boolean isReadOnly() {
    return this.readOnly;
  }

  public boolean isRollbackOnly() {
    if (this.transactionStatus == null) {
      return isReadOnly();
    } else {
      return this.transactionStatus.isRollbackOnly();
    }
  }

  protected void rollback() {
    if (this.transactionManager != null && this.transactionStatus != null) {
      this.transactionManager.rollback(this.transactionStatus);
    }
  }

  public void setIsolation(final Isolation isolation) {
    this.isolation = isolation;
  }

  void setPropagation(final Propagation propagation) {
    this.propagation = propagation;
  }

  void setReadOnly(final boolean readOnly) {
    this.readOnly = readOnly;
  }

  public Transaction setRollbackOnly() {
    this.rollbackOnly = true;
    if (this.transactionStatus != null) {
      this.transactionStatus.setRollbackOnly();
    }
    return this;
  }

  public RuntimeException setRollbackOnly(final Throwable e) {
    setRollbackOnly();
    return Exceptions.throwUncheckedException(e);
  }

  void setTimeout(final int timeout) {
    this.timeout = timeout;
  }
}
