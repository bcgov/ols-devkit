package com.revolsys.visitor;

import java.util.Comparator;
import java.util.function.Predicate;

public class BaseVisitor<T> extends AbstractVisitor<T> {

  public BaseVisitor() {
  }

  public BaseVisitor(final Comparator<T> comparator) {
    super(comparator);
  }

  public BaseVisitor(final Predicate<T> filter) {
    super(filter);
  }

  public BaseVisitor(final Predicate<T> filter, final Comparator<T> comparator) {
    super(filter, comparator);
  }

  @Override
  public void accept(final T object) {
    final Predicate<T> predicate = getPredicate();
    if (predicate.test(object)) {
      acceptDo(object);
    }
  }

  protected void acceptDo(final T object) {
  }
}
