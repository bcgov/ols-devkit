package com.revolsys.parallel.process;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ChannelOutput;
import com.revolsys.parallel.channel.ClosedException;

public final class Delta<T> extends AbstractInProcess<T> {

  private boolean clone = true;

  private List<ChannelOutput<T>> out = new ArrayList<>();

  private boolean running;

  public Delta() {
  }

  public Delta(final InProcess<T>... processes) {
    if (processes != null) {
      for (final InProcess<T> process : processes) {
        final Channel<T> channel = process.getIn();
        addOut(channel);
      }
    }
  }

  public void addOut(final ChannelOutput<T> channel) {
    if (channel != null) {
      channel.writeConnect();
      this.out.add(channel);
    }
  }

  @SuppressWarnings("unchecked")
  private T clone(final T value) {
    if (this.clone && value instanceof Cloneable) {
      try {
        final Class<? extends Object> valueClass = value.getClass();
        final Method method = valueClass.getMethod("clone", new Class[0]);
        if (method != null) {
          return (T)method.invoke(value, new Object[0]);
        }
      } catch (final IllegalArgumentException e) {
        throw e;
      } catch (final InvocationTargetException e) {

        final Throwable cause = e.getCause();
        if (cause instanceof RuntimeException) {
          final RuntimeException re = (RuntimeException)cause;
          throw re;
        } else if (cause instanceof Error) {
          final Error ee = (Error)cause;
          throw ee;
        } else {
          throw new RuntimeException(cause.getMessage(), cause);
        }
      } catch (final RuntimeException e) {
        throw e;
      } catch (final Exception e) {
        throw new RuntimeException(e.getMessage(), e);
      }

    }
    return value;
  }

  @Override
  protected void destroy() {
    super.destroy();
    disconnectOut();
  }

  private void disconnectOut() {
    for (final ChannelOutput<T> channel : this.out) {
      if (channel != null) {
        channel.writeDisconnect();
      }
    }
  }

  public List<ChannelOutput<T>> getOut() {
    return this.out;
  }

  public boolean isClone() {
    return this.clone;
  }

  @Override
  protected void run(final Channel<T> in) {
    this.running = true;
    try {
      while (this.running) {
        final T record = in.read();
        if (record != null) {
          for (final Iterator<ChannelOutput<T>> iterator = this.out.iterator(); iterator
            .hasNext();) {
            final ChannelOutput<T> out = iterator.next();
            final T clonedObject = clone(record);
            try {
              out.write(clonedObject);
            } catch (final ClosedException e) {
              iterator.remove();
              if (this.out.isEmpty()) {
                this.running = false;
              }
            }
          }
        }
      }
    } finally {
      this.running = false;
    }

  }

  public void setClone(final boolean clone) {
    this.clone = clone;
  }

  public void setOut(final List<ChannelOutput<T>> out) {
    disconnectOut();
    this.out = new ArrayList<>();
    for (final ChannelOutput<T> channel : out) {
      addOut(channel);
    }
  }

}
