package com.revolsys.transaction;

import org.jeometry.common.logging.Logs;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ClosedException;

public class SendToChannelAfterCommit<T> extends TransactionSynchronizationAdapter {
  public static <V> void send(final Channel<V> channel, final V value) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      final SendToChannelAfterCommit<V> synchronization = new SendToChannelAfterCommit<>(channel,
        value);
      TransactionSynchronizationManager.registerSynchronization(synchronization);
    } else {
      channel.write(value);
    }
  }

  private final Channel<T> channel;

  private final T object;

  public SendToChannelAfterCommit(final Channel<T> channel, final T object) {
    this.channel = channel;
    this.object = object;
  }

  @Override
  public void afterCommit() {
    try {
      this.channel.write(this.object);
    } catch (final ClosedException e) {
      Logs.error(this, "Channel closed " + this.channel, e);
    }
  }
}
