package com.revolsys.parallel.process;

import java.util.function.Consumer;

import com.revolsys.parallel.channel.Channel;

public class ConsumerOutProcess<O> extends AbstractOutProcess<O> {

  private final Consumer<Channel<O>> action;

  public ConsumerOutProcess(final Consumer<Channel<O>> action) {
    this.action = action;
  }

  @Override
  protected void run(final Channel<O> out) {
    this.action.accept(out);
  }

}
