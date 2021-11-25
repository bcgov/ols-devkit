package com.revolsys.raster;

import org.jeometry.common.data.type.DataType;

public interface Band {

  byte getByte(double x, double y);

  byte getByte(int columnIndex, int rowIndex);

  DataType getDataType();

  double getDouble(double x, double y);

  double getDouble(int columnIndex, int rowIndex);

  float getFloat(double x, double y);

  float getFloat(int columnIndex, int rowIndex);

  int getHeight();

  int getInt(double x, double y);

  int getInt(int columnIndex, int rowIndex);

  long getLong(double x, double y);

  long getLong(int columnIndex, int rowIndex);

  Band getOverview(int index);

  int getOverviewCount();

  short getShort(double x, double y);

  short getShort(int columnIndex, int rowIndex);

  int getWidth();
}
