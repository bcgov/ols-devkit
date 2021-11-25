package org.jeometry.coordinatesystem.model;

public interface Authority {

  String getCode();

  default int getId() {
    return Integer.parseInt(getCode());
  }

  String getName();
}
