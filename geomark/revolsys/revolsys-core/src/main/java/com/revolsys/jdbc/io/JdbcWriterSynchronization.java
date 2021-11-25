package com.revolsys.jdbc.io;

import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class JdbcWriterSynchronization extends TransactionSynchronizationAdapter {

  private boolean holderActive = true;

  private final Object key;

  private final JdbcWriterResourceHolder writerHolder;

  public JdbcWriterSynchronization(final AbstractJdbcRecordStore recordStore,
    final JdbcWriterResourceHolder writerHolder, final Object key) {
    this.writerHolder = writerHolder;
    this.key = key;
  }

  @Override
  public void afterCompletion(final int status) {
    if (this.holderActive) {
      TransactionSynchronizationManager.unbindResourceIfPossible(this.key);
      this.holderActive = false;
      this.writerHolder.close();
    }
    this.writerHolder.reset();
  }

  @Override
  public void beforeCompletion() {
    if (!this.writerHolder.isOpen()) {
      TransactionSynchronizationManager.unbindResource(this.key);
      this.holderActive = false;
      this.writerHolder.close();
    }
  }

  @Override
  public int getOrder() {
    return 999;
  }

  @Override
  public void resume() {
    if (this.holderActive) {
      TransactionSynchronizationManager.bindResource(this.key, this.writerHolder);
    }
  }

  @Override
  public void suspend() {
    if (this.holderActive) {
      TransactionSynchronizationManager.unbindResource(this.key);
      if (!this.writerHolder.isOpen()) {
        this.writerHolder.close();
      }
    }
  }
}
