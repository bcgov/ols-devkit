package com.revolsys.transaction;

import org.springframework.transaction.support.TransactionSynchronizationAdapter;

public class RunnableAfterCommit extends TransactionSynchronizationAdapter {
  private final Runnable runnable;

  public RunnableAfterCommit(final Runnable runnable) {
    this.runnable = runnable;
  }

  @Override
  public void afterCommit() {
    this.runnable.run();
  }
}
