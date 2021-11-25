package com.revolsys.parallel.channel;

public interface ChannelOutput<T> {
  /**
   * Writes an Object to the Channel. This method also ensures only one of the
   * writers can actually be writing at any time. All other writers are blocked
   * until it completes the write.
   *
   * @param value The object to write to the Channel.
   */
  void write(final T value);

  void writeConnect();

  void writeDisconnect();
}
