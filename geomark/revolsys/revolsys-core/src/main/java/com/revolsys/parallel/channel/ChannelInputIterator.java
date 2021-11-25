package com.revolsys.parallel.channel;

import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;

public class ChannelInputIterator<T> extends AbstractIterator<T> {
  private final ChannelInput<T> in;

  public ChannelInputIterator(final ChannelInput<T> in) {
    this.in = in;
  }

  @Override
  protected T getNext() throws NoSuchElementException {
    try {
      final T object = this.in.read();
      return object;
    } catch (final ClosedException e) {
      throw new NoSuchElementException();
    }
  }
}
