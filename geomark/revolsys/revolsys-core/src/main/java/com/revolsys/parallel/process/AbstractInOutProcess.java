package com.revolsys.parallel.process;

import org.jeometry.common.logging.Logs;

import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ChannelValueStore;
import com.revolsys.parallel.channel.ClosedException;
import com.revolsys.parallel.channel.store.Buffer;
import com.revolsys.parallel.channel.store.ZeroBuffer;

public abstract class AbstractInOutProcess<I, O> extends AbstractProcess
  implements InOutProcess<I, O> {

  private Channel<I> in;

  private int inBufferSize = 0;

  private Channel<O> out;

  private int outBufferSize = 0;

  private boolean initialized;

  public AbstractInOutProcess() {
  }

  public AbstractInOutProcess(final Channel<I> in, final Channel<O> out) {
    this.in = in;
    this.out = out;
  }

  protected void destroy() {
  }

  /**
   * @return the in
   */
  @Override
  public Channel<I> getIn() {
    if (this.in == null) {
      final String channelName = getBeanName() + ".in";
      final ChannelValueStore<I> buffer = newInValueStore();
      final Channel<I> channel = new Channel<>(channelName, buffer);
      setIn(channel);
    }
    return this.in;
  }

  public int getInBufferSize() {
    return this.inBufferSize;
  }

  /**
   * @return the out
   */
  @Override
  public Channel<O> getOut() {
    if (this.out == null) {
      final String channelName = getBeanName() + ".out";
      final ChannelValueStore<O> buffer = newOutValueStore();
      final Channel<O> channel = new Channel<>(channelName, buffer);
      setOut(channel);
    }
    return this.out;
  }

  public int getOutBufferSize() {
    return this.outBufferSize;
  }

  @Override
  public final synchronized void initialize() {
    if (!this.initialized) {
      this.initialized = true;
      initializeDo();
    }
  }

  protected void initializeDo() {
  }

  protected ChannelValueStore<I> newInValueStore() {
    if (this.inBufferSize == 0) {
      return new ZeroBuffer<>();
    } else if (this.inBufferSize < 0) {
      return new Buffer<>();
    } else {
      return new Buffer<>(this.inBufferSize);
    }
  }

  protected ChannelValueStore<O> newOutValueStore() {
    if (this.outBufferSize == 0) {
      return new ZeroBuffer<>();
    } else if (this.outBufferSize < 0) {
      return new Buffer<>();
    } else {
      return new Buffer<>(this.outBufferSize);
    }
  }

  @Override
  public final void run() {
    boolean hasError = false;
    try {
      Logs.debug(this, "Start");
      initialize();
      run(this.in, this.out);
    } catch (final ClosedException e) {
      Logs.debug(this, "Shutdown");
    } catch (final ThreadDeath e) {
      Logs.debug(this, "Shutdown");
    } catch (final Throwable e) {
      Logs.error(this, e.getMessage(), e);
      hasError = true;
    } finally {
      if (this.in != null) {
        this.in.readDisconnect();
      }
      if (this.out != null) {
        this.out.writeDisconnect();
      }
      destroy();
    }
    if (hasError) {
      getProcessNetwork().stop();
    }
  }

  protected abstract void run(Channel<I> in, Channel<O> out);

  /**
   * @param in the in to set
   */
  @Override
  public AbstractInOutProcess<I, O> setIn(final Channel<I> in) {
    this.in = in;
    in.readConnect();
    return this;
  }

  @Override
  public AbstractInOutProcess<I, O> setIn(final OutProcess<I> process) {
    if (process != null) {
      final Channel<I> in = process.getOut();
      if (in != null) {
        setIn(in);
      }
    }
    return this;
  }

  public AbstractInOutProcess<I, O> setInBufferSize(final int inBufferSize) {
    this.inBufferSize = inBufferSize;
    return this;
  }

  /**
   * @param out the out to set
   */
  @Override
  public AbstractInOutProcess<I, O> setOut(final Channel<O> out) {
    this.out = out;
    out.writeConnect();
    return this;
  }

  @Override
  public AbstractInOutProcess<I, O> setOut(final InProcess<O> process) {
    if (process != null) {
      final Channel<O> channel = process.getIn();
      if (channel != null) {
        setOut(channel);
      }
    }
    return this;
  }

  public AbstractInOutProcess<I, O> setOutBufferSize(final int outBufferSize) {
    this.outBufferSize = outBufferSize;
    return this;
  }

}
