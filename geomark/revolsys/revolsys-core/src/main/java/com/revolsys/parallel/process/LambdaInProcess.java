package com.revolsys.parallel.process;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.revolsys.parallel.channel.Channel;

public class LambdaInProcess<T> extends BaseInProcess<T> {

  private Consumer<Channel<T>> postRun = (c) -> {
  };

  private Consumer<Channel<T>> preRun = (c) -> {
  };

  private BiConsumer<Channel<T>, T> process = (c, o) -> {
  };

  public LambdaInProcess() {
  }

  public LambdaInProcess(final String processName) {
    super(processName);
  }

  @Override
  protected void postRun(final Channel<T> in) {
    this.postRun.accept(in);
  }

  @Override
  protected void preRun(final Channel<T> in) {
    this.preRun.accept(in);
  }

  @Override
  protected void process(final Channel<T> in, final T object) {
    this.process.accept(in, object);
  }

  public LambdaInProcess<T> setPostRun(final Consumer<Channel<T>> postRun) {
    this.postRun = postRun;
    return this;
  }

  public LambdaInProcess<T> setPostRun(final Runnable postRun) {
    this.postRun = (in) -> postRun.run();
    return this;
  }

  public LambdaInProcess<T> setPreRun(final Consumer<Channel<T>> preRun) {
    this.preRun = preRun;
    return this;
  }

  public LambdaInProcess<T> setPreRun(final Runnable preRun) {
    this.preRun = (in) -> preRun.run();
    return this;
  }

  public LambdaInProcess<T> setProcess(final BiConsumer<Channel<T>, T> process) {
    this.process = process;
    return this;
  }

  public LambdaInProcess<T> setProcess(final Consumer<T> process) {
    this.process = (in, object) -> process.accept(object);
    return this;
  }
}
