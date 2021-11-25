package com.revolsys.collection.range;

public abstract class AbstractNumberRange<V extends Number> extends AbstractRange<V> {

  /**
   * Construct a new expanded range if the this range and the other overlap or touch
   *
   * @param range
   * @return
   */
  public AbstractRange<?> expand(final AbstractNumberRange<?> range) {
    final long from = getFrom().longValue();
    final long to = getTo().longValue();
    final long rangeFrom = range.getFrom().longValue();
    final long rangeTo = range.getTo().longValue();

    if (from == rangeFrom) {
      if (to >= rangeTo) {
        return this;
      } else {
        return range;
      }
    } else if (to == rangeTo) {
      if (from < rangeFrom) {
        return this;
      } else {
        return range;
      }
    } else if (from < rangeFrom) {
      if (to > rangeTo) {
        return this;
      } else if (rangeFrom <= to + 1) {
        return newNumberRange(from, rangeTo);
      }
    } else if (from > rangeFrom) {
      if (to < rangeTo) {
        return range;
      } else if (from <= rangeTo + 1) {
        return newNumberRange(rangeFrom, to);
      }
    }
    return null;
  }

  @Override
  public AbstractRange<?> expand(final AbstractRange<?> range) {
    if (range instanceof AbstractNumberRange<?>) {
      final AbstractNumberRange<?> numberRange = (AbstractNumberRange<?>)range;
      return expand(numberRange);
    } else {
      return null;
    }
  }

  protected AbstractNumberRange<?> newNumberRange(final long from, final long to) {
    return new LongRange(from, to);
  }
}
