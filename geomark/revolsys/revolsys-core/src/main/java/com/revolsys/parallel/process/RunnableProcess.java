package com.revolsys.parallel.process;

public class RunnableProcess extends AbstractProcess {
  private final Runnable runnable;

  public RunnableProcess(final Runnable runnable) {
    this.runnable = runnable;
  }

  public RunnableProcess(final String processName, final Runnable runnable) {
    super(processName);
    this.runnable = runnable;
  }

  @Override
  public void run() {
    if (this.runnable != null) {
      this.runnable.run();
    }
  }
}
