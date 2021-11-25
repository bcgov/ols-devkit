package org.jeometry.common.function;

@FunctionalInterface
public interface Function4<P1, P2, P3, P4, R> {
  R apply(P1 parameter1, P2 parameter2, P3 parameter3, P4 parameter4);
}
