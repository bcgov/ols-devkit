package com.revolsys.parallel.channel;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

import com.revolsys.parallel.ThreadInterruptedException;

public class NamedChannelBundle<T> {

  /** Flag indicating if the channel has been closed. */
  private boolean closed = false;

  /** The monitor reads must synchronize on */
  private final Object monitor = new Object();

  /** The name of the channel. */
  private String name;

  /** Number of readers connected to the channel. */
  private int numReaders = 0;

  /** Number of writers connected to the channel. */
  private int numWriters = 0;

  private int readerNotifyCount = 0;

  /** The monitor reads must synchronize on */
  private final Object readMonitor = new Object();

  private AtomicLong sequence = new AtomicLong();

  private Map<String, Queue<Long>> sequenceQueueByName = new HashMap<>();

  /** The ChannelValueStore used to store the valueQueueByName for the Channel */
  protected Map<String, Queue<T>> valueQueueByName = new HashMap<>();

  /** Flag indicating if the channel is closed for writing. */
  private boolean writeClosed;

  /** The monitor writes must synchronize on */
  private final Object writeMonitor = new Object();

  public NamedChannelBundle() {
  }

  public NamedChannelBundle(final String name) {
    this.name = name;
  }

  public void close() {
    this.closed = true;
    synchronized (this.monitor) {
      this.valueQueueByName = null;
      this.sequence = null;
      this.sequenceQueueByName = null;
      this.monitor.notifyAll();
    }
  }

  public String getName() {
    return this.name;
  }

  private Queue<T> getNextValueQueue(Collection<String> names) {
    String selectedName = null;
    long lowestSequence = Long.MAX_VALUE;
    if (names == null) {
      names = this.sequenceQueueByName.keySet();
    }
    for (final String name : names) {
      final Queue<Long> sequenceQueue = this.sequenceQueueByName.get(name);
      if (sequenceQueue != null && !sequenceQueue.isEmpty()) {
        final long sequence = sequenceQueue.peek();
        if (sequence < lowestSequence) {
          lowestSequence = sequence;
          selectedName = name;
        }
      }
    }
    if (selectedName == null) {
      return null;
    } else {
      final Queue<Long> sequenceQueue = this.sequenceQueueByName.get(selectedName);
      sequenceQueue.remove();
      return getValueQueue(selectedName);
    }
  }

  private Queue<Long> getSequenceQueue(final String name) {
    Queue<Long> queue = this.sequenceQueueByName.get(name);
    if (queue == null) {
      queue = new LinkedList<>();
      this.sequenceQueueByName.put(name, queue);
    }
    return queue;
  }

  private Queue<T> getValueQueue(final String name) {
    Queue<T> queue = this.valueQueueByName.get(name);
    if (queue == null) {
      queue = new LinkedList<>();
      this.valueQueueByName.put(name, queue);
    }
    return queue;
  }

  public boolean isClosed() {
    if (!this.closed) {
      if (this.writeClosed) {
        boolean empty = true;
        synchronized (this.monitor) {
          for (final Queue<T> queue : this.valueQueueByName.values()) {
            if (!queue.isEmpty()) {
              empty = false;
            }
          }
          if (empty) {
            close();
          }
        }
      }
    }

    return this.closed;
  }

  public void notifyReaders() {
    synchronized (this.monitor) {
      this.readerNotifyCount++;
      this.monitor.notifyAll();
    }
  }

  /**
   * Reads an Object from the Channel. This method also ensures only one of the
   * readers can actually be reading at any time. All other readers are blocked
   * until it completes the read.
   *
   * @return The object returned from the Channel.
   */
  public T read() {
    return read(0, Collections.<String> emptyList());
  }

  public T read(final Collection<String> names) {
    return read(0, names);
  }

  /**
   * Reads an Object from the Channel. This method also ensures only one of the
   * readers can actually be reading at any time. All other readers are blocked
   * until it completes the read. If no data is available to be read after the
   * timeout the method will return null.
   *
   * @param timeout The maximum time to wait in milliseconds.
   * @return The object returned from the Channel.
   */
  public T read(final long timeout) {
    return read(timeout, Collections.<String> emptyList());
  }

  public T read(final long timeout, final Collection<String> names) {
    synchronized (this.readMonitor) {
      synchronized (this.monitor) {
        final int readerNotifyCount = this.readerNotifyCount;
        try {
          long maxTime = 0;
          if (timeout > 0) {
            maxTime = System.currentTimeMillis() + timeout;
          }
          if (isClosed()) {
            throw new ClosedException();
          }
          Queue<T> queue = getNextValueQueue(names);
          if (timeout == 0) {
            while (queue == null && readerNotifyCount == this.readerNotifyCount) {
              try {
                this.monitor.wait();
              } catch (final InterruptedException e) {
                throw new ThreadInterruptedException(e);
              }
              if (isClosed()) {
                throw new ClosedException();
              }

              queue = getNextValueQueue(names);
            }
          } else if (timeout > 0) {
            long waitTime = maxTime - System.currentTimeMillis();
            while (queue == null && waitTime > 0 && readerNotifyCount == this.readerNotifyCount) {
              final long milliSeconds = waitTime;
              try {
                this.monitor.wait(milliSeconds);
              } catch (final InterruptedException e) {
                throw new ThreadInterruptedException(e);
              }
              if (isClosed()) {
                throw new ClosedException();
              }

              queue = getNextValueQueue(names);
              waitTime = maxTime - System.currentTimeMillis();
            }
          } else {
            queue = getNextValueQueue(names);
          }
          if (queue == null) {
            return null;
          } else {
            final T value = queue.remove();
            this.monitor.notifyAll();
            return value;
          }
        } catch (final ThreadInterruptedException e) {
          close();
          this.monitor.notifyAll();
          throw new ClosedException();
        }
      }
    }
  }

  public T read(final long timeout, final String... names) {
    return read(timeout, Arrays.asList(names));
  }

  public T read(final String... names) {
    return read(0, Arrays.asList(names));
  }

  public void readConnect() {
    synchronized (this.monitor) {
      if (isClosed()) {
        throw new IllegalStateException("Cannot connect to a closed channel");
      } else {
        this.numReaders++;
      }

    }
  }

  public void readDisconnect() {
    synchronized (this.monitor) {
      if (!this.closed) {
        this.numReaders--;
        if (this.numReaders <= 0) {
          close();
          this.monitor.notifyAll();
        }
      }

    }
  }

  public Collection<T> remove(final String name) {
    synchronized (this.monitor) {
      this.sequenceQueueByName.remove(name);
      final Queue<T> values = this.valueQueueByName.remove(name);
      this.monitor.notifyAll();
      return values;
    }
  }

  /**
   * Writes a named Object to the Channel. This method also ensures only one of
   * the writers can actually be writing at any time. All other writers are
   * blocked until it completes the write. The channel can never be full so it
   * does not block on write.
   *
   * @param value The object to write to the Channel.
   */
  public void write(final String name, final T value) {
    synchronized (this.writeMonitor) {
      synchronized (this.monitor) {
        if (this.closed) {
          this.monitor.notifyAll();
          throw new ClosedException();
        } else {
          final Long sequence = this.sequence.getAndIncrement();
          final Queue<Long> sequenceQueue = getSequenceQueue(name);
          sequenceQueue.add(sequence);

          final Queue<T> queue = getValueQueue(name);
          queue.add(value);

          this.monitor.notifyAll();
        }
      }
    }
  }

  public void writeConnect() {
    synchronized (this.monitor) {
      if (this.writeClosed) {
        throw new IllegalStateException("Cannot connect to a closed channel");
      } else {
        this.numWriters++;
      }

    }
  }

  public void writeDisconnect() {
    synchronized (this.monitor) {
      if (!this.writeClosed) {
        this.numWriters--;
        if (this.numWriters <= 0) {
          this.writeClosed = true;
          this.monitor.notifyAll();
        }
      }
    }
  }
}
