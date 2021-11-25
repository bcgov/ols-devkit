package com.revolsys.predicate;

import java.util.function.Predicate;

public interface PredicateProxy<T> {
  Predicate<T> getPredicate();
}
