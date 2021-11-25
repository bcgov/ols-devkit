package com.revolsys.parallel.channel;

import java.util.Iterator;

import com.revolsys.parallel.ThreadInterruptedException;
import com.revolsys.parallel.channel.store.ZeroBuffer;

public class Channel<T> implements SelectableChannelInput<T>, ChannelOutput<T> {
  /** The Alternative class which will control the selection */
  protected MultiInputSelector alt;

  /** Flag indicating if the channel has been closed. */
  private boolean closed = false;

  /** The ChannelValueStore used to store the data for the Channel */
  protected ChannelValueStore<T> data;

  /** The monitor reads must synchronize on */
  protected Object monitor = new Object();

  /** The name of the channel. */
  private String name;

  /** Number of readers connected to the channel. */
  private int numReaders = 0;

  /** Number of writers connected to the channel. */
  private int numWriters = 0;

  /** The monitor reads must synchronize on */
  protected Object readMonitor = new Object();

  /** Flag indicating if the channel is closed for writing. */
  private boolean writeClosed;

  /** The monitor writes must synchronize on */
  protected Object writeMonitor = new Object();

  /**
   * Constructs a new Channel<T> with a ZeroBuffer ChannelValueStore.
   */
  public Channel() {
    this(new ZeroBuffer<T>());
  }

  /**
   * Constructs a new Channel<T> with the specified ChannelValueStore.
   *
   * @param data The ChannelValueStore used to store the data for the Channel
   */
  public Channel(final ChannelValueStore<T> data) {
    this.data = data;
  }

  public Channel(final String name) {
    this();
    this.name = name;
  }

  public Channel(final String name, final ChannelValueStore<T> data) {
    this.name = name;
    this.data = data;
  }

  public void close() {
    this.closed = true;
  }

  @Override
  public boolean disable() {
    this.alt = null;
    return this.data.getState() != ChannelValueStore.EMPTY;
  }

  @Override
  public boolean enable(final MultiInputSelector alt) {
    synchronized (this.monitor) {
      if (this.data.getState() == ChannelValueStore.EMPTY) {
        this.alt = alt;
        return false;
      } else {
        return true;
      }
    }
  }

  public String getName() {
    return this.name;
  }

  @Override
  public boolean isClosed() {
    if (!this.closed) {
      if (this.writeClosed) {
        if (this.data.getState() == ChannelValueStore.EMPTY) {
          close();
        }
      }
    }

    return this.closed;
  }

  @Override
  public Iterator<T> iterator() {
    return new ChannelInputIterator<>(this);
  }

  /**
   * Reads an Object from the Channel. This method also ensures only one of the
   * readers can actually be reading at any time. All other readers are blocked
   * until it completes the read.
   *
   * @return The object returned from the Channel.
   */
  @Override
  public T read() {
    return read(0);
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
  @Override
  public T read(final long timeout) {
    synchronized (this.readMonitor) {
      synchronized (this.monitor) {
        if (isClosed()) {
          throw new ClosedException();
        }
        if (this.data.getState() == ChannelValueStore.EMPTY) {
          try {
            try {
              this.monitor.wait(timeout);
            } catch (final InterruptedException e) {
              throw new ThreadInterruptedException(e);
            }
            if (isClosed()) {
              throw new ClosedException();
            }
          } catch (final ThreadInterruptedException e) {
            close();
            this.monitor.notifyAll();
            throw new ClosedException();
          }
        }
        if (this.data.getState() == ChannelValueStore.EMPTY) {
          return null;
        } else {
          final T value = this.data.get();
          this.monitor.notifyAll();
          return value;
        }
      }
    }
  }

  @Override
  public void readConnect() {
    synchronized (this.monitor) {
      if (isClosed()) {
        throw new IllegalStateException("Cannot connect to a closed channel");
      } else {
        this.numReaders++;
      }

    }
  }

  @Override
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

  @Override
  public String toString() {
    if (this.name == null) {
      return this.data.toString();
    } else {
      return this.name;
    }
  }

  /**
   * Writes an Object to the Channel. This method also ensures only one of the
   * writers can actually be writing at any time. All other writers are blocked
   * until it completes the write.
   *
   * @param value The object to write to the Channel.
   */
  @Override
  public void write(final T value) {
    synchronized (this.writeMonitor) {
      synchronized (this.monitor) {
        if (this.closed) {
          throw new ClosedException();
        }
        final MultiInputSelector tempAlt = this.alt;
        this.data.put(value);
        if (tempAlt != null) {
          tempAlt.schedule();
        } else {
          this.monitor.notifyAll();
        }
        if (this.data.getState() == ChannelValueStore.FULL) {
          try {
            try {
              this.monitor.wait();
            } catch (final InterruptedException e) {
              throw new ThreadInterruptedException(e);
            }
            if (this.closed) {
              throw new ClosedException();
            }
          } catch (final ThreadInterruptedException e) {
            close();
            this.monitor.notifyAll();
            throw new ClosedException(e);
          }
        }
      }
    }
  }

  @Override
  public void writeConnect() {
    synchronized (this.monitor) {
      if (this.writeClosed) {
        throw new IllegalStateException("Cannot connect to a closed channel");
      } else {
        this.numWriters++;
      }

    }
  }

  @Override
  public void writeDisconnect() {
    synchronized (this.monitor) {
      if (!this.writeClosed) {
        this.numWriters--;
        if (this.numWriters <= 0) {
          this.writeClosed = true;
          final MultiInputSelector tempAlt = this.alt;
          if (tempAlt != null) {
            tempAlt.closeChannel();
          } else {
            this.monitor.notifyAll();
          }
        }
      }

    }
  }
}
