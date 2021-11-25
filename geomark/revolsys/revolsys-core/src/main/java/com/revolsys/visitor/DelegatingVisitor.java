package com.revolsys.visitor;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DelegatingVisitor<T> extends AbstractVisitor<T> {
  private Consumer<T> action;

  public DelegatingVisitor() {
  }

  public DelegatingVisitor(final Comparator<T> comparator) {
    super(comparator);
  }

  public DelegatingVisitor(final Comparator<T> comparator, final Consumer<T> action) {
    super(comparator);
    this.action = action;
  }

  public DelegatingVisitor(final Consumer<T> action) {
    this.action = action;
  }

  public DelegatingVisitor(final Predicate<T> filter) {
    super(filter);
  }

  public DelegatingVisitor(final Predicate<T> filter, final Comparator<T> comparator) {
    super(filter, comparator);
  }

  public DelegatingVisitor(final Predicate<T> filter, final Comparator<T> comparator,
    final Consumer<T> action) {
    super(filter, comparator);
    this.action = action;
  }

  public DelegatingVisitor(final Predicate<T> filter, final Consumer<T> action) {
    super(filter);
    this.action = action;
  }

  @Override
  public void accept(final T item) {
    final Predicate<T> predicate = getPredicate();
    if (predicate.test(item)) {
      this.action.accept(item);
    }
  }

  public Consumer<T> getAction() {
    return this.action;
  }

  public void setAction(final Consumer<T> action) {
    this.action = action;
  }

  @Override
  public String toString() {
    return this.action.toString();
  }
}
