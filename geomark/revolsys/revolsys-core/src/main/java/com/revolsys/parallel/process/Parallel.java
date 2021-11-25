package com.revolsys.parallel.process;

import java.util.Collection;

public class Parallel extends AbstractMultipleProcess {
  public Parallel() {
  }

  public Parallel(final Collection<? extends Process> processes) {
    super(processes);
  }

  public Parallel(final Process... processes) {
    super(processes);
  }

  @Override
  public void run() {
    final ProcessNetwork network = new ProcessNetwork();
    network.setProcesses(getProcesses());
    network.startAndWait();
  }

  @Override
  public String toString() {
    return "Parallel:\n  " + super.toString();
  }
}
