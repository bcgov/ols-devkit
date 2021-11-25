package com.revolsys.parallel.process;

import java.util.function.Predicate;

import com.revolsys.parallel.channel.Channel;

public class FilterProcess<T> extends BaseInOutProcess<T, T> {
  private Predicate<T> filter;

  private boolean invert = false;

  public Predicate<T> getFilter() {
    return this.filter;
  }

  public boolean isInvert() {
    return this.invert;
  }

  protected void postAccept(final T object) {
  }

  protected void postReject(final T object) {
  }

  @Override
  protected void process(final Channel<T> in, final Channel<T> out, final T object) {
    boolean test = this.filter.test(object);
    if (this.invert) {
      test = !test;
    }
    if (test) {
      out.write(object);
      postAccept(object);
    } else {
      postReject(object);
    }
  }

  public void setFilter(final Predicate<T> filter) {
    this.filter = filter;
  }

  public void setInvert(final boolean invert) {
    this.invert = invert;
  }

}
