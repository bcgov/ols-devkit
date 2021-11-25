package com.revolsys.parallel.process;

import java.util.function.BiConsumer;

import com.revolsys.parallel.channel.Channel;

public interface InProcess<T> extends Process {

  static <V> LambdaInProcess<V> lambda(final BiConsumer<Channel<V>, V> process) {
    return new LambdaInProcess<V>().setProcess(process);
  }

  Channel<T> getIn();

  InProcess<T> setIn(Channel<T> in);

  default InProcess<T> setIn(final OutProcess<T> process) {
    if (process != null) {
      final Channel<T> in = process.getOut();
      if (in != null) {
        setIn(in);
      }
    }
    return this;
  }

  default void write(final T value) {
    final Channel<T> in = getIn();
    in.write(value);
  }
}
