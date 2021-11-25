package com.revolsys.parallel.process;

import com.revolsys.parallel.channel.Channel;

public class BaseInOutProcess<I, O> extends AbstractInOutProcess<I, O> {
  private boolean running = false;

  protected void postRun(final Channel<I> in, final Channel<O> out) {
  }

  protected void preRun(final Channel<I> in, final Channel<O> out) {
  }

  protected void process(final Channel<I> in, final Channel<O> out, final I object) {
  }

  @Override
  protected final void run(final Channel<I> in, final Channel<O> out) {
    if (in != null && out != null) {
      this.running = true;
      try {
        preRun(in, out);
        while (this.running) {
          final I object = in.read();
          if (object != null) {
            process(in, out, object);
          }
        }
      } finally {
        try {
          postRun(in, out);
        } finally {
          this.running = false;
        }
      }
    }
  }

}
