package org.jeometry.common.function;

@FunctionalInterface
public interface BiFunctionDouble<R> {
  R accept(double parameter1, double parameter2);
}
