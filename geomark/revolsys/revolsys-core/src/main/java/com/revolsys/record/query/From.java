package com.revolsys.record.query;

public interface From {
  void appendFrom(final Appendable string);

  default void appendFromWithAlias(final Appendable string) {
    appendFrom(string);
  }
}
