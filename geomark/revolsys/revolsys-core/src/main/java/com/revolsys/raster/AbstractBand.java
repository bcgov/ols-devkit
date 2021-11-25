package com.revolsys.raster;

import org.jeometry.common.data.type.DataType;

public abstract class AbstractBand implements Band {
  private DataType dataType;

  private int height;

  private int width;

  @Override
  public byte getByte(final double x, final double y) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public byte getByte(final int columnIndex, final int rowIndex) {
    return (byte)getLong(columnIndex, rowIndex);
  }

  @Override
  public DataType getDataType() {
    return this.dataType;
  }

  @Override
  public double getDouble(final double x, final double y) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public double getDouble(final int columnIndex, final int rowIndex) {
    final long value = getLong(columnIndex, rowIndex);
    return Double.longBitsToDouble(value);
  }

  @Override
  public float getFloat(final double x, final double y) {
    return 0;
  }

  @Override
  public float getFloat(final int columnIndex, final int rowIndex) {
    final int value = getInt(columnIndex, rowIndex);
    return Float.intBitsToFloat(value);
  }

  @Override
  public int getHeight() {
    return this.height;
  }

  @Override
  public int getInt(final double x, final double y) {
    return 0;
  }

  @Override
  public int getInt(final int columnIndex, final int rowIndex) {
    return (int)getLong(columnIndex, rowIndex);
  }

  @Override
  public long getLong(final double x, final double y) {
    return 0;
  }

  @Override
  public long getLong(final int columnIndex, final int rowIndex) {
    return 0;
  }

  @Override
  public Band getOverview(final int index) {
    return null;
  }

  @Override
  public int getOverviewCount() {
    return 0;
  }

  @Override
  public short getShort(final double x, final double y) {
    return 0;
  }

  @Override
  public short getShort(final int columnIndex, final int rowIndex) {
    return (short)getLong(columnIndex, rowIndex);
  }

  @Override
  public int getWidth() {
    return this.width;
  }
}
