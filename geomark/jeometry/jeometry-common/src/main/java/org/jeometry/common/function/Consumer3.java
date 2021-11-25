package org.jeometry.common.function;

@FunctionalInterface
public interface Consumer3<P1, P2, P3> {
  void accept(P1 parameter1, P2 parameter2, P3 parameter3);
}
