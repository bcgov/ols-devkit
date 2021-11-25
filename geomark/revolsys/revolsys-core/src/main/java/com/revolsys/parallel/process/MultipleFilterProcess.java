package com.revolsys.parallel.process;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ClosedException;

public class MultipleFilterProcess<T> extends BaseInOutProcess<T, T> {
  /** The map of filters to channels. */
  private final Map<Predicate<T>, Channel<T>> predicates = new LinkedHashMap<>();

  /**
   * Add the filter with the channel to write the data object to if the filter
   * matches.
   *
   * @param filter The filter.
   * @param channel The channel.
   */
  private void addFiler(final Predicate<T> filter, final Channel<T> channel) {
    this.predicates.put(filter, channel);
    if (channel != null) {
      channel.writeConnect();
    }
  }

  @Override
  protected void destroy() {
    for (final Channel<T> channel : this.predicates.values()) {
      if (channel != null) {
        channel.writeDisconnect();
      }
    }
  }

  /**
   * @return the filters
   */
  public Map<Predicate<T>, Channel<T>> getPredicates() {
    return this.predicates;
  }

  @Override
  protected void preRun(final Channel<T> in, final Channel<T> out) {
  }

  @Override
  protected void process(final Channel<T> in, final Channel<T> out, final T object) {
    for (final Entry<Predicate<T>, Channel<T>> entry : this.predicates.entrySet()) {
      final Predicate<T> filter = entry.getKey();
      final Channel<T> filterOut = entry.getValue();
      if (processPredicate(object, filter, filterOut)) {
        return;
      }
    }
    if (out != null) {
      out.write(object);
    }
  }

  protected boolean processPredicate(final T object, final Predicate<T> filter,
    final Channel<T> filterOut) {
    if (filter.test(object)) {
      if (filterOut != null) {
        try {
          filterOut.write(object);
        } catch (final ClosedException e) {
        }
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * @param filters the filters to set
   */
  public void setPredicates(final Map<Predicate<T>, Channel<T>> filters) {
    for (final Entry<Predicate<T>, Channel<T>> filterEntry : filters.entrySet()) {
      final Predicate<T> filter = filterEntry.getKey();
      final Channel<T> channel = filterEntry.getValue();
      addFiler(filter, channel);
    }
  }

}
