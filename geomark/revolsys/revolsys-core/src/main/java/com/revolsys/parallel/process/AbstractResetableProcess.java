package com.revolsys.parallel.process;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;

import com.revolsys.parallel.ThreadInterruptedException;

public abstract class AbstractResetableProcess extends AbstractProcess {
  private final Set<UUID> executions = new LinkedHashSet<>();

  private boolean pause = false;

  private boolean reset = false;

  private boolean running = false;

  private String status = "initialized";

  private boolean waitForExecutionToFinish = false;

  private long waitTime = 1000;

  public AbstractResetableProcess() {
  }

  public AbstractResetableProcess(final long waitTime) {
    this.waitTime = waitTime;
  }

  protected abstract boolean execute();

  protected void finishExecution(final UUID id) {
    synchronized (this.executions) {
      this.executions.remove(id);
      this.executions.notifyAll();
    }
  }

  @ManagedAttribute
  public int getExecutionCount() {
    return this.executions.size();
  }

  @ManagedAttribute
  public String getStatus() {
    return this.status;
  }

  public long getWaitTime() {
    return this.waitTime;
  }

  /**
   * The hard reset causes the scheduler loop to restart ignoring all current
   * executing requests. Upon reset the counts of executing requests and the
   * status of all jobs will be updated to ensure consistency.
   */
  @ManagedOperation
  public void hardReset() {
    this.waitForExecutionToFinish = false;
    this.pause = false;
    this.reset = true;
  }

  /**
   * The pause causes the scheduler to sleep until a soft or hard reset is
   * initiated.
   */
  @ManagedOperation
  public void pause() {
    this.pause = true;
  }

  protected void postRun() {
  }

  protected void preRun() {
  }

  protected void reset() {
  }

  @Override
  public void run() {
    preRun();
    this.running = true;
    try {
      while (this.running) {
        this.status = "resetting";
        this.executions.clear();
        reset();
        this.reset = false;
        while (this.running && !this.reset) {
          this.status = "starting execution";
          if (this.pause || !execute()) {
            if (this.pause) {
              this.status = "paused";
            } else {
              this.status = "waiting";
            }
            final long milliSeconds = this.waitTime;
            synchronized (this) {
              try {
                this.wait(milliSeconds);
              } catch (final InterruptedException e) {
                throw new ThreadInterruptedException(e);
              }
            }
          }
        }

        synchronized (this.executions) {
          while (this.waitForExecutionToFinish && !this.executions.isEmpty()) {
            waitOnExecutions();
          }
        }
      }
    } catch (final ThreadInterruptedException e) {
    } finally {
      try {
        postRun();
      } finally {
        this.running = false;
        this.status = "terminated";
      }
    }
  }

  protected void setStatus(final String status) {
    this.status = status;
  }

  public void setWaitTime(final long waitTime) {
    this.waitTime = waitTime;
  }

  /**
   * The soft reset causes the scheduler loop to restart after all current
   * executing requests have completed. Upon reset the counts of executing
   * requests and the status of all jobs will be updated to ensure consistency.
   */
  @ManagedOperation
  public void softReset() {
    this.waitForExecutionToFinish = true;
    this.pause = false;
    this.reset = true;
  }

  protected UUID startExecution() {
    synchronized (this.executions) {
      final UUID id = UUID.randomUUID();
      this.executions.add(id);
      this.executions.notifyAll();
      return id;
    }
  }

  protected void waitOnExecutions() {
    this.status = "waiting on executions";
    final long milliSeconds = this.waitTime;
    synchronized (this.executions) {
      try {
        this.executions.wait(milliSeconds);
      } catch (final InterruptedException e) {
        throw new ThreadInterruptedException(e);
      }
    }
  }

}
