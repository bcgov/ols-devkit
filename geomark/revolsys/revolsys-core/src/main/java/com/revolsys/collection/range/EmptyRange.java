package com.revolsys.collection.range;

/**
 *
 * Ranges are immutable
 */
public class EmptyRange extends AbstractRange<Object> {
  public static final EmptyRange INSTANCE = new EmptyRange();

  public EmptyRange() {
  }

  @Override
  public AbstractRange<?> expand(final AbstractRange<?> range) {
    return range;
  }

  @Override
  public AbstractRange<?> expand(final Object value) {
    return Ranges.newRange(value);
  }

  @Override
  public String getFrom() {
    return null;
  }

  @Override
  public String getTo() {
    return null;
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public long size() {
    return 0;
  }
}
