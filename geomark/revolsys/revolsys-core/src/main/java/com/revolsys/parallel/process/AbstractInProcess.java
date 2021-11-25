package com.revolsys.parallel.process;

import org.jeometry.common.logging.Logs;

import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ClosedException;
import com.revolsys.parallel.channel.store.Buffer;

public abstract class AbstractInProcess<T> extends AbstractProcess implements InProcess<T> {

  private Channel<T> in;

  private int inBufferSize = 0;

  public AbstractInProcess() {
  }

  public AbstractInProcess(final Channel<T> in) {
    this.in = in;
  }

  public AbstractInProcess(final int bufferSize) {
    this.inBufferSize = bufferSize;
  }

  public AbstractInProcess(final String processName) {
    super(processName);
  }

  protected void destroy() {
  }

  /**
   * @return the in
   */
  @Override
  public Channel<T> getIn() {
    if (this.in == null) {
      final String channelName = getBeanName() + ".in";
      if (this.inBufferSize == 0) {
        setIn(new Channel<T>(channelName));
      } else {
        setIn(new Channel<>(channelName, new Buffer<T>(this.inBufferSize)));
      }
    }
    return this.in;
  }

  /**
   * @return the bufferSize
   */
  public int getInBufferSize() {
    return this.inBufferSize;
  }

  protected void init() {
  }

  @Override
  public void run() {
    try {
      Logs.debug(this, "Start");
      init();
      run(getIn());
    } catch (final ThreadDeath e) {
      Logs.debug(this, "Shutdown");
    } catch (final ClosedException e) {
      Logs.debug(this, "Shutdown");
    } catch (final Throwable e) {
      Logs.error(this, e.getMessage(), e);
      getProcessNetwork().stop();
    } finally {
      if (this.in != null) {
        this.in.readDisconnect();
      }
      destroy();
    }
  }

  protected abstract void run(Channel<T> in);

  /**
   * @param in the in to set
   */
  @Override
  public AbstractInProcess<T> setIn(final Channel<T> in) {
    this.in = in;
    in.readConnect();
    return this;
  }

  /**
   * @param bufferSize the bufferSize to set
   */
  public AbstractInProcess<T> setInBufferSize(final int inBufferSize) {
    this.inBufferSize = inBufferSize;
    return this;
  }
}
