package com.revolsys.parallel.process;

import java.util.function.BiConsumer;

import com.revolsys.parallel.channel.Channel;

public class ConsumerInOutProcess<I, O> extends AbstractInOutProcess<I, O> {

  private final BiConsumer<Channel<I>, Channel<O>> action;

  public ConsumerInOutProcess(final BiConsumer<Channel<I>, Channel<O>> action) {
    this.action = action;
  }

  @Override
  protected void run(final Channel<I> in, final Channel<O> out) {
    this.action.accept(in, out);
  }
}
