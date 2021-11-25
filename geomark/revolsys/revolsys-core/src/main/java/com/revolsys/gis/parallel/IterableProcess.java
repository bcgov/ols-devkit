package com.revolsys.gis.parallel;

import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.AbstractOutProcess;

public class IterableProcess<T> extends AbstractOutProcess<T> {
  private Iterable<T> iterable;

  public IterableProcess() {

  }

  public IterableProcess(final Channel<T> out, final Iterable<T> iterable) {
    super(out);
    this.iterable = iterable;
  }

  public IterableProcess(final Iterable<T> iterable) {
    this.iterable = iterable;
  }

  public IterableProcess(final Iterable<T> iterable, final int bufferSize) {
    super(bufferSize);
    this.iterable = iterable;
  }

  /**
   * @return the iterable
   */
  public Iterable<T> getIterable() {
    return this.iterable;
  }

  @Override
  protected void run(final Channel<T> out) {
    for (final T object : this.iterable) {
      write(out, object);
    }
  }

  /**
   * @param iterable the iterable to set
   */
  public void setIterable(final Iterable<T> iterable) {
    this.iterable = iterable;
  }

  @Override
  public String toString() {
    return this.iterable.toString();
  }

  protected void write(final Channel<T> out, final T object) {
    out.write(object);
  }
}
