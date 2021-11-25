package com.revolsys.parallel.channel;

public class Timer implements SelectableInput {
  private final long time;

  public Timer(final long time) {
    this.time = time;
  }

  @Override
  public boolean disable() {
    return isTimeout();
  }

  @Override
  public boolean enable(final MultiInputSelector alt) {
    return isTimeout();
  }

  public long getWaitTime() {
    final long waitTime = this.time - System.currentTimeMillis();
    return waitTime;
  }

  @Override
  public boolean isClosed() {
    return false;
  }

  public boolean isTimeout() {
    final boolean timeout = System.currentTimeMillis() > this.time;
    return timeout;
  }
}
