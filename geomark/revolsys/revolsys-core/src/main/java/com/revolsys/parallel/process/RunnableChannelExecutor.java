package com.revolsys.parallel.process;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.jeometry.common.logging.Logs;
import org.springframework.beans.factory.BeanNameAware;

import com.revolsys.parallel.NamedThreadFactory;
import com.revolsys.parallel.ThreadInterruptedException;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ClosedException;
import com.revolsys.parallel.channel.MultiInputSelector;

public class RunnableChannelExecutor extends ThreadPoolExecutor implements Process, BeanNameAware {
  private String beanName;

  private List<Channel<Runnable>> channels = new ArrayList<>();

  private final Object monitor = new Object();

  private ProcessNetwork processNetwork;

  private final AtomicInteger taskCount = new AtomicInteger();

  public RunnableChannelExecutor() {
    super(0, 100, 60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new NamedThreadFactory());
  }

  @Override
  protected void afterExecute(final Runnable r, final Throwable t) {
    this.taskCount.decrementAndGet();
    synchronized (this.monitor) {
      this.monitor.notifyAll();
    }
  }

  public void closeChannels() {
    synchronized (this.monitor) {
      final List<Channel<Runnable>> channels = this.channels;
      if (channels != null) {
        for (final Channel<Runnable> channel : channels) {
          channel.readDisconnect();
        }
      }
      this.channels = null;
    }
  }

  @Override
  public void execute(final Runnable command) {
    if (command != null) {
      while (!isShutdown()) {
        if (this.taskCount.get() >= getMaximumPoolSize()) {
          synchronized (this.monitor) {
            try {
              this.monitor.wait();
            } catch (final InterruptedException e) {
              throw new ThreadInterruptedException(e);
            }
          }
        }
        this.taskCount.incrementAndGet();
        try {
          super.execute(command);
          return;
        } catch (final RejectedExecutionException e) {
          this.taskCount.decrementAndGet();
        } catch (final RuntimeException e) {
          this.taskCount.decrementAndGet();
          throw e;
        } catch (final Error e) {
          this.taskCount.decrementAndGet();
          throw e;
        }
      }
    }
  }

  @Override
  public String getBeanName() {
    return this.beanName;
  }

  public List<Channel<Runnable>> getChannels() {
    return this.channels;
  }

  /**
   * @return the processNetwork
   */
  @Override
  public ProcessNetwork getProcessNetwork() {
    return this.processNetwork;
  }

  @PostConstruct
  public void init() {
    for (final Channel<Runnable> channel : this.channels) {
      channel.readConnect();
    }
  }

  public void postRun() {
    closeChannels();
  }

  protected void preRun() {

  }

  @Override
  public void run() {
    preRun();
    try {
      final MultiInputSelector selector = new MultiInputSelector();

      while (!isShutdown()) {
        final List<Channel<Runnable>> channels = this.channels;
        try {
          if (!isShutdown()) {
            final Channel<Runnable> channel = selector.selectChannelInput(channels);
            if (channel != null) {
              final Runnable runnable = channel.read();
              execute(runnable);
            }
          }
        } catch (final ClosedException e) {
          final Throwable cause = e.getCause();
          if (cause instanceof ThreadInterruptedException) {
            throw (ThreadInterruptedException)cause;
          }
          synchronized (this.monitor) {
            for (final Iterator<Channel<Runnable>> iterator = channels.iterator(); iterator
              .hasNext();) {
              final Channel<Runnable> channel = iterator.next();
              if (channel.isClosed()) {
                iterator.remove();
              }
            }
            if (channels.isEmpty()) {
              return;
            }
          }
        }
      }
    } catch (final ThreadInterruptedException e) {
      throw e;
    } catch (final Throwable t) {
      if (!isShutdown()) {
        Logs.error(this, "Unexexpected error ", t);
      }
    } finally {
      postRun();
    }
  }

  @Override
  public void setBeanName(final String beanName) {
    this.beanName = beanName;
    final ThreadFactory threadFactory = getThreadFactory();
    if (threadFactory instanceof NamedThreadFactory) {
      final NamedThreadFactory namedThreadFactory = (NamedThreadFactory)threadFactory;
      namedThreadFactory.setNamePrefix(beanName + "-pool");
    }
  }

  public void setChannels(final List<Channel<Runnable>> channels) {
    this.channels = channels;
  }

  /**
   * @param processNetwork the processNetwork to set
   */
  @Override
  public void setProcessNetwork(final ProcessNetwork processNetwork) {
    this.processNetwork = processNetwork;
    if (processNetwork != null) {
      processNetwork.addProcess(this);
      final ThreadFactory threadFactory = getThreadFactory();
      if (threadFactory instanceof NamedThreadFactory) {
        final NamedThreadFactory namedThreadFactory = (NamedThreadFactory)threadFactory;
        namedThreadFactory.setParentGroup(processNetwork.getThreadGroup());
      }
    }
  }

  @PreDestroy
  public void stop() {
    shutdownNow();
    closeChannels();
    this.processNetwork = null;
    synchronized (this.monitor) {
      this.monitor.notifyAll();
    }
  }

  @Override
  public String toString() {
    return this.beanName;
  }
}
