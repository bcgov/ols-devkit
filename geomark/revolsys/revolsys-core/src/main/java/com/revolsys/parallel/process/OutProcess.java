package com.revolsys.parallel.process;

import com.revolsys.parallel.channel.Channel;

public interface OutProcess<T> extends Process {

  Channel<T> getOut();

  OutProcess<T> setOut(Channel<T> out);

  default OutProcess<T> setOut(final InProcess<T> process) {
    if (process != null) {
      final Channel<T> channel = process.getIn();
      if (channel != null) {
        setOut(channel);
      }
    }
    return this;
  }
}
