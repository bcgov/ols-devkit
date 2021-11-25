package com.revolsys.parallel.process;

import java.util.Arrays;
import java.util.Collection;

public class Sequential extends AbstractMultipleProcess {
  public Sequential() {
  }

  public Sequential(final Collection<? extends Process> processes) {
    super(processes);
  }

  public Sequential(final Process... processes) {
    this(Arrays.asList(processes));
  }

  @Override
  public void run() {
    for (final Process process : getProcesses()) {
      process.run();
    }
  }

  @Override
  public String toString() {
    return "Sequential:\n  " + super.toString();
  }
}
