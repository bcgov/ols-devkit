package org.jeometry.common.math;

import java.util.Random;

public class Randoms {

  public static final Random RANDOM = new Random();

  public static double randomGaussian(final double mean, final double variance) {
    return mean + RANDOM.nextGaussian() * variance;
  }

  public static double randomRange(final double min, final double max) {
    return min + RANDOM.nextDouble() * (max - min);
  }

}
