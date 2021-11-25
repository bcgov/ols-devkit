package com.revolsys.visitor;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.comparator.ComparatorProxy;
import com.revolsys.predicate.PredicateProxy;
import com.revolsys.predicate.Predicates;

public abstract class AbstractVisitor<T>
  implements Consumer<T>, PredicateProxy<T>, ComparatorProxy<T> {
  private Comparator<T> comparator;

  private Predicate<T> predicate = Predicates.all();

  public AbstractVisitor() {
  }

  public AbstractVisitor(final Comparator<T> comparator) {
    this.comparator = comparator;
  }

  public AbstractVisitor(final Predicate<T> predicate) {
    setPredicate(predicate);
  }

  public AbstractVisitor(final Predicate<T> predicate, final Comparator<T> comparator) {
    this.comparator = comparator;
    setPredicate(predicate);
  }

  @Override
  public Comparator<T> getComparator() {
    return this.comparator;
  }

  @Override
  public Predicate<T> getPredicate() {
    return this.predicate;
  }

  public void setComparator(final Comparator<T> comparator) {
    this.comparator = comparator;
  }

  public void setPredicate(final Predicate<T> predicate) {
    if (predicate == null) {
      this.predicate = Predicates.all();
    } else {
      this.predicate = predicate;
    }
  }
}
