package org.jeometry.common.function;

@FunctionalInterface
public interface Function3<P1, P2, P3, R> {
  R apply(P1 parameter1, P2 parameter2, P3 parameter3);
}
