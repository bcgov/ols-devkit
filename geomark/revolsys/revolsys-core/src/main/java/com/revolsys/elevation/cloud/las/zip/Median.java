package com.revolsys.elevation.cloud.las.zip;

public class Median {

  private int value1;

  private int value2;

  private int value3;

  private int index;

  public void addValue(final int value) {
    switch (this.index) {
      case 0:
        this.value1 = value;
      break;
      case 1:
        this.value2 = value;
      break;
      default:
        this.value3 = value;
      break;
    }
    this.index = (this.index + 1) % 3;
  }

  public int median() {
    if (this.value1 < this.value2) {
      if (this.value2 < this.value3) {
        return this.value2;
      } else if (this.value1 < this.value3) {
        return this.value3;
      } else {
        return this.value1;
      }
    } else {
      if (this.value1 < this.value3) {
        return this.value1;
      } else if (this.value2 < this.value3) {
        return this.value3;
      } else {
        return this.value2;
      }
    }
  }

  public void reset() {
    this.value1 = 0;
    this.value2 = 0;
    this.value3 = 0;
    this.index = 0;
  }
}
