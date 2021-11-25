package org.jeometry.common.function;

@FunctionalInterface
public interface Function3Double<R> {
  R accept(double parameter1, double parameter2, double parameter3);
}
