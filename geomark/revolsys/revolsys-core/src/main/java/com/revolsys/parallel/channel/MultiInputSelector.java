package com.revolsys.parallel.channel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.revolsys.parallel.ThreadInterruptedException;

public class MultiInputSelector {
  private int enabledChannels = 0;

  private int guardEnabledChannels = 0;

  private long maxWait;

  private final Object monitor = new Object();

  private boolean scheduled;

  void closeChannel() {
    synchronized (this.monitor) {
      this.enabledChannels--;
      if (this.enabledChannels <= 0) {
        this.monitor.notifyAll();
      }
    }
  }

  private int disableChannels(final List<? extends SelectableInput> channels) {
    int closedCount = 0;
    int selected = -1;
    for (int i = channels.size() - 1; i >= 0; i--) {
      final SelectableInput channel = channels.get(i);
      if (channel.disable()) {
        selected = i;
      } else if (channel.isClosed()) {
        closedCount++;
      }
    }
    if (closedCount == channels.size()) {
      throw new ClosedException();
    } else {
      return selected;
    }

  }

  private int disableChannels(final List<? extends SelectableInput> channels,
    final List<Boolean> guard) {
    int closedCount = 0;
    int selected = -1;
    for (int i = channels.size() - 1; i >= 0; i--) {
      final SelectableInput channel = channels.get(i);
      if (guard.get(i) && channel.disable()) {
        selected = i;
      } else if (channel == null || channel.isClosed()) {
        closedCount++;
      }
    }
    if (closedCount == channels.size()) {
      throw new ClosedException();
    } else {
      return selected;
    }
  }

  private boolean enableChannels(final List<? extends SelectableInput> channels) {
    this.enabledChannels = 0;
    this.scheduled = false;
    this.maxWait = Long.MAX_VALUE;
    int closedCount = 0;
    for (final SelectableInput channel : channels) {
      if (!channel.isClosed()) {
        if (channel.enable(this)) {
          this.enabledChannels++;
          return true;
        } else if (channel instanceof Timer) {
          final Timer timer = (Timer)channel;
          this.maxWait = Math.min(this.maxWait, timer.getWaitTime());
        }
      } else {
        closedCount++;
      }
    }
    return closedCount == channels.size();
  }

  private boolean enableChannels(final List<? extends SelectableInput> channels,
    final List<Boolean> guard) {
    this.enabledChannels = 0;
    this.scheduled = false;
    this.maxWait = Long.MAX_VALUE;
    int closedCount = 0;
    int activeChannelCount = 0;
    for (int i = 0; i < channels.size(); i++) {
      final SelectableInput channel = channels.get(i);
      if (guard.get(i)) {
        activeChannelCount++;
        if (!channel.isClosed()) {
          if (channel.enable(this)) {
            this.enabledChannels++;
            return true;
          } else if (channel instanceof Timer) {
            final Timer timer = (Timer)channel;
            this.maxWait = Math.min(this.maxWait, timer.getWaitTime());
          }
        } else {
          closedCount++;
        }
      }
    }
    this.guardEnabledChannels = activeChannelCount - closedCount;
    return closedCount == activeChannelCount;
  }

  void schedule() {
    synchronized (this.monitor) {
      this.scheduled = true;
      this.monitor.notifyAll();
    }
  }

  public synchronized int select(final List<? extends SelectableInput> channels) {
    return select(Long.MAX_VALUE, channels);
  }

  public synchronized int select(final List<? extends SelectableInput> channels,
    final boolean skip) {
    if (skip) {
      enableChannels(channels);
      return disableChannels(channels);
    } else {
      return select(channels);
    }
  }

  public synchronized int select(final List<? extends SelectableInput> channels,
    final List<Boolean> guard) {
    return select(channels, guard, Long.MAX_VALUE);
  }

  public synchronized int select(final List<? extends SelectableInput> channels,
    final List<Boolean> guard, final boolean skip) {
    if (skip) {
      enableChannels(channels, guard);
      return disableChannels(channels, guard);
    } else {
      return select(channels, guard);
    }
  }

  public synchronized int select(final List<? extends SelectableInput> channels,
    final List<Boolean> guard, final long msecs) {
    return select(channels, guard, msecs, 0);
  }

  public synchronized int select(final List<? extends SelectableInput> channels,
    final List<Boolean> guard, final long msecs, final int nsecs) {
    if (!enableChannels(channels, guard) && this.guardEnabledChannels > 0) {
      synchronized (this.monitor) {
        if (!this.scheduled) {
          try {
            try {
              this.monitor.wait(Math.min(msecs, this.maxWait), nsecs);
            } catch (final InterruptedException e) {
              throw new ThreadInterruptedException(e);
            }
          } catch (final ThreadInterruptedException e) {
            throw new ClosedException(e);
          }
        }
      }
    }
    return disableChannels(channels, guard);
  }

  public synchronized int select(final long msecs, final int nsecs,
    final List<? extends SelectableInput> channels) {
    if (!enableChannels(channels)) {
      if (msecs + nsecs >= 0) {
        synchronized (this.monitor) {
          try {
            if (!this.scheduled) {
              try {
                this.monitor.wait(Math.min(msecs, this.maxWait), nsecs);
              } catch (final InterruptedException e) {
                throw new ThreadInterruptedException(e);
              }
            }
          } catch (final ThreadInterruptedException e) {
            throw new ClosedException(e);
          }
        }
      }
    }
    return disableChannels(channels);
  }

  public synchronized int select(final long msecs, final int nsecs,
    final SelectableInput... channels) {
    return select(msecs, nsecs, Arrays.asList(channels));
  }

  public synchronized int select(final long msecs, final List<? extends SelectableInput> channels) {
    return select(msecs, 0, channels);
  }

  public synchronized int select(final long msecs, final SelectableInput... channels) {
    return select(msecs, 0, channels);
  }

  public synchronized int select(final SelectableInput... channels) {
    return select(Long.MAX_VALUE, channels);
  }

  public synchronized int select(final SelectableInput[] channels, final boolean skip) {
    return select(Arrays.asList(channels), skip);
  }

  public synchronized int select(final SelectableInput[] channels, final boolean[] guard) {
    return select(channels, guard, Long.MAX_VALUE);
  }

  public synchronized int select(final SelectableInput[] channels, final boolean[] guard,
    final boolean skip) {
    final List<Boolean> guardList = new ArrayList<>();
    for (final boolean enabled : guard) {
      guardList.add(enabled);
    }
    return select(Arrays.asList(channels), guardList, skip);
  }

  public synchronized int select(final SelectableInput[] channels, final boolean[] guard,
    final long msecs) {
    return select(channels, guard, msecs, 0);
  }

  public synchronized int select(final SelectableInput[] channels, final boolean[] guard,
    final long msecs, final int nsecs) {
    final List<Boolean> guardList = new ArrayList<>();
    for (final boolean enabled : guard) {
      guardList.add(enabled);
    }
    return select(Arrays.asList(channels), guardList, msecs, nsecs);
  }

  public synchronized <T extends SelectableInput> T selectChannelInput(final List<T> channels) {
    final int index = select(Long.MAX_VALUE, channels);
    if (index == -1) {
      return null;
    } else {
      return channels.get(index);
    }
  }

}
