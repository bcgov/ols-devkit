package com.revolsys.parallel.process;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.store.Buffer;

public class ProcessQueue {
  private final int maxWorkerIdleTime;

  private final int maxWorkers;

  private final Buffer<Process> processBuffer = new Buffer<>(200);

  private final Channel<Process> processChannel = new Channel<>(this.processBuffer);

  private final Set<ProcessQueueWorker> workers = Collections
    .synchronizedSet(new HashSet<ProcessQueueWorker>());

  public ProcessQueue(final int maxWorkers, final int maxWorkerIdleTime) {
    this.maxWorkers = maxWorkers;
    this.maxWorkerIdleTime = maxWorkerIdleTime;
  }

  void addWorker(final ProcessQueueWorker worker) {
    synchronized (this.workers) {
      this.workers.add(worker);
    }
  }

  public synchronized void cancelProcess(final Process process) {

    if (process != null && !this.processBuffer.remove(process)) {
      synchronized (this.workers) {
        for (final ProcessQueueWorker worker : this.workers) {
          if (worker.getProcess() == process) {
            worker.interrupt();
          }
        }
      }
    }
  }

  public void clear() {
    this.processBuffer.clear();
  }

  public int getMaxWorkerIdleTime() {
    return this.maxWorkerIdleTime;
  }

  Channel<Process> getProcessChannel() {
    return this.processChannel;
  }

  void removeWorker(final ProcessQueueWorker worker) {
    synchronized (this.workers) {
      this.workers.remove(worker);
    }
  }

  public synchronized void runProcess(final Process process) {
    this.processChannel.write(process);
    if (this.workers.size() < this.maxWorkers && this.processBuffer.size() > this.workers.size()) {
      final ProcessQueueWorker worker = new ProcessQueueWorker(this);
      worker.start();
    }
  }

  public void stop() {
    clear();
    this.processChannel.close();
    synchronized (this.workers) {
      for (final ProcessQueueWorker worker : this.workers) {
        worker.interrupt();
      }
    }
    this.workers.clear();
  }

}
