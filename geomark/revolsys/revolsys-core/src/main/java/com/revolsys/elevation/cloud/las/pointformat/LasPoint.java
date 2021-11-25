package com.revolsys.elevation.cloud.las.pointformat;

import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.io.channels.DataReader;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonObjectHash;

public interface LasPoint extends Point, MapSerializer {

  @Override
  LasPoint clone();

  default int getBlue() {
    return 0;
  }

  short getClassification();

  byte getClassificationByte();

  default byte getClassificationFlags() {
    return 0;
  }

  default double getGpsTime() {
    return 315964800;
  }

  default long getGpsTimeLong() {
    final double gpsTime = getGpsTime();
    return Double.doubleToLongBits(gpsTime);
  }

  default int getGreen() {
    return 0;
  }

  int getIntensity();

  default int getNir() {
    return 0;
  }

  byte getNumberOfReturns();

  LasPointFormat getPointFormat();

  default int getPointFormatId() {
    return getPointFormat().getId();
  }

  int getPointSourceID();

  default int getRed() {
    return 0;
  }

  byte getReturnByte();

  byte getReturnNumber();

  short getScanAngle();

  double getScanAngleDegrees();

  byte getScanAngleRank();

  byte getScannerChannel();

  short getUserData();

  int getXInt();

  int getYInt();

  int getZInt();

  boolean isEdgeOfFlightLine();

  boolean isKeyPoint();

  default boolean isPointFormat(final LasPointFormat pointFormat) {
    return pointFormat.equals(getPointFormat());
  }

  boolean isScanDirectionFlag();

  boolean isSynthetic();

  boolean isWithheld();

  void read(LasPointCloud pointCloud, DataReader reader);

  default LasPoint setBlue(final int blue) {
    return this;
  }

  default LasPoint setByteOffsetToWaveformData(final long byteOffsetToWaveformData) {
    throw new UnsupportedOperationException();
  }

  LasPoint setClassification(short classification);

  LasPoint setClassificationByte(byte classificationByte);

  default LasPoint setClassificationFlags(final byte classificationFlags) {
    return this;
  }

  LasPoint setEdgeOfFlightLine(boolean edgeOfFlightLine);

  default LasPoint setGpsTime(final double gpsTime) {
    return this;
  }

  default LasPoint setGpsTimeLong(final long gpsTime) {
    final double time = Double.longBitsToDouble(gpsTime);
    setGpsTime(time);
    return this;
  }

  default LasPoint setGreen(final int green) {
    return this;
  }

  LasPoint setIntensity(int intensity);

  LasPoint setKeyPoint(boolean keyPoint);

  default LasPoint setNir(final int nir) {
    return this;
  }

  LasPoint setNumberOfReturns(byte numberOfReturns);

  LasPoint setPointSourceID(int pointSourceID);

  default LasPoint setRed(final int red) {
    return this;
  }

  LasPoint setReturnByte(byte returnByte);

  LasPoint setReturnNumber(byte returnNumber);

  default LasPoint setReturnPointWaveformLocation(final float returnPointWaveformLocation) {
    throw new UnsupportedOperationException();
  }

  LasPoint setScanAngle(short scanAngle);

  LasPoint setScanAngleRank(byte scanAngleRank);

  LasPoint setScanDirectionFlag(boolean scanDirectionFlag);

  LasPoint setScannerChannel(byte scannerChannel);

  LasPoint setSynthetic(boolean synthetic);

  LasPoint setUserData(short userData);

  default LasPoint setWaveformPacketSizeInBytes(final long waveformPacketSizeInBytes) {
    throw new UnsupportedOperationException();
  }

  default LasPoint setWavePacketDescriptorIndex(final short wavePacketDescriptorIndex) {
    throw new UnsupportedOperationException();
  }

  LasPoint setWithheld(boolean withheld);

  default LasPoint setXInt(final int x) {
    throw new UnsupportedOperationException();
  }

  default LasPoint setXT(final float xT) {
    throw new UnsupportedOperationException();
  }

  void setXYZ(int x, int y, int z);

  default LasPoint setYInt(final int y) {
    throw new UnsupportedOperationException();
  }

  default LasPoint setYT(final float yT) {
    throw new UnsupportedOperationException();
  }

  default LasPoint setZInt(final int z) {
    throw new UnsupportedOperationException();
  }

  default LasPoint setZT(final float zT) {
    throw new UnsupportedOperationException();
  }

  @Override
  default JsonObject toMap() {
    final JsonObject map = new JsonObjectHash();
    return map;
  }

  void writeLasPoint(ChannelWriter out);
}
