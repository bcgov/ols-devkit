package com.revolsys.parallel.process;

import com.revolsys.parallel.channel.Channel;

/**
 * A BlackHole is a process which reads all objects from the input and discards
 * the value. This would be equivalent to piping to /dev/null on UNIX.
 *
 * @author Paul Austin
 * @param <T> The type of object
 */
public class BlackHole<T> extends BaseInProcess<T> {
  /**
   * Process the object.
   *
   * @param in The input channel.
   * @param object The object to process.
   */
  @Override
  protected void process(final Channel<T> in, final T object) {
  }
}
