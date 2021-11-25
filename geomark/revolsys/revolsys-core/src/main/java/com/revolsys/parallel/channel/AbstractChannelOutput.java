package com.revolsys.parallel.channel;

public abstract class AbstractChannelOutput<T> implements ChannelOutput<T> {
  /** Flag indicating if the channel has been closed. */
  private boolean closed = false;

  /** The monitor reads must synchronize on */
  private final Object monitor = new Object();

  /** The name of the channel. */
  private String name;

  /** Number of writers connected to the channel. */
  private int numWriters = 0;

  /** Flag indicating if the channel is closed for writing. */
  private boolean writeClosed;

  /** The monitor writes must synchronize on */
  private final Object writeMonitor = new Object();

  /**
   * Constructs a new Channel<T> with a ZeroBuffer ChannelValueStore.
   */
  public AbstractChannelOutput() {
  }

  public AbstractChannelOutput(final String name) {
    this.name = name;
  }

  public void close() {
    this.closed = true;
  }

  public String getName() {
    return this.name;
  }

  public boolean isClosed() {
    return this.closed;
  }

  @Override
  public String toString() {
    if (this.name == null) {
      return super.toString();
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
        writeDo(value);
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
        }
      }
    }
  }

  protected abstract void writeDo(T value);
}
