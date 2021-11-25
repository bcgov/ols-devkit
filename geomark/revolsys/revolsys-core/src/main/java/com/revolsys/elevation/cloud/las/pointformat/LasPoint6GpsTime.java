package com.revolsys.elevation.cloud.las.pointformat;

import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.io.channels.DataReader;
import com.revolsys.record.io.format.json.JsonObject;

public class LasPoint6GpsTime extends BaseLasPoint implements LasPointExtended {
  private static final long serialVersionUID = 1L;

  private short classification;

  private short scanAngle;

  private short userData;

  private byte scannerChannel;

  private double gpsTime;

  private byte returnByte = 0b00010001;

  private byte classificationFlags;

  public LasPoint6GpsTime(final LasPointCloud pointCloud) {
    super(pointCloud);
    this.gpsTime = pointCloud.getFileGpsTime();
  }

  @Override
  public LasPoint6GpsTime clone() {
    return (LasPoint6GpsTime)super.clone();
  }

  @Override
  public short getClassification() {
    return this.classification;
  }

  @Override
  public byte getClassificationByte() {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public byte getClassificationFlags() {
    return this.classificationFlags;
  }

  @Override
  public double getGpsTime() {
    return this.gpsTime;
  }

  @Override
  public byte getNumberOfReturns() {
    return (byte)(this.returnByte >> 4 & 0b1111);
  }

  @Override
  public LasPointFormat getPointFormat() {
    return LasPointFormat.ExtendedGpsTime;
  }

  @Override
  public byte getReturnByte() {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public byte getReturnNumber() {
    return (byte)(this.returnByte & 0b1111);
  }

  @Override
  public short getScanAngle() {
    return this.scanAngle;
  }

  @Override
  public double getScanAngleDegrees() {
    return this.scanAngle * 0.006;
  }

  @Override
  public byte getScanAngleRank() {
    return (byte)getScanAngleDegrees();
  }

  @Override
  public byte getScannerChannel() {
    return (byte)(this.classificationFlags >> 4 & 0b11);
  }

  @Override
  public short getUserData() {
    return this.userData;
  }

  @Override
  public boolean isEdgeOfFlightLine() {
    return (this.classificationFlags & 0b10000000) != 0;
  }

  @Override
  public boolean isKeyPoint() {
    return (this.classificationFlags & 0b10) != 0;
  }

  @Override
  public boolean isOverlap() {
    return (this.classificationFlags & 0b1000) != 0;
  }

  @Override
  public boolean isScanDirectionFlag() {
    return (this.classificationFlags & 0b1000000) != 0;
  }

  @Override
  public boolean isSynthetic() {
    return (this.classificationFlags & 0b1) != 0;
  }

  @Override
  public boolean isWithheld() {
    return (this.classificationFlags & 0b100) != 0;
  }

  @Override
  public void read(final LasPointCloud pointCloud, final DataReader reader) {
    final int xRecord = reader.getInt();
    final int yRecord = reader.getInt();
    final int zRecord = reader.getInt();
    setXYZ(xRecord, yRecord, zRecord);
    this.intensity = reader.getUnsignedShort();
    this.returnByte = reader.getByte();
    this.classificationFlags = reader.getByte();
    this.classification = reader.getUnsignedByte();
    this.userData = reader.getUnsignedByte();
    this.scanAngle = reader.getShort();
    this.pointSourceID = reader.getUnsignedShort();
    this.gpsTime = reader.getDouble();
  }

  @Override
  public LasPoint6GpsTime setClassification(final short classification) {
    if (classification < 0 && classification > 256) {
      throw new IllegalArgumentException("Invalid LAS classificaion " + classification
        + " not in 0..255 for record format " + getPointFormatId());
    }
    this.classification = classification;
    return this;
  }

  @Override
  public LasPoint6GpsTime setClassificationByte(final byte classificationByte) {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public LasPoint6GpsTime setClassificationFlags(final byte classificationFlags) {
    this.classificationFlags = classificationFlags;
    return this;
  }

  @Override
  public LasPoint6GpsTime setEdgeOfFlightLine(final boolean edgeOfFlightLine) {
    if (edgeOfFlightLine) {
      this.classificationFlags |= 0b10000000;
    } else {
      this.classificationFlags &= ~0b10000000;
    }
    return this;
  }

  @Override
  public LasPoint6GpsTime setGpsTime(final double gpsTime) {
    this.gpsTime = gpsTime;
    return this;
  }

  @Override
  public LasPoint6GpsTime setKeyPoint(final boolean keyPoint) {
    if (keyPoint) {
      this.classificationFlags |= 0b10;
    } else {
      this.classificationFlags &= ~0b10;
    }
    return this;
  }

  @Override
  public LasPoint6GpsTime setNumberOfReturns(final byte numberOfReturns) {
    if (numberOfReturns >= 0 && numberOfReturns <= 15) {
      this.returnByte &= 0b1111;
      this.returnByte |= numberOfReturns << 4;
    } else {
      throw new IllegalArgumentException(
        "numberOfReturns must be in range 1..15: " + numberOfReturns);
    }
    return this;
  }

  @Override
  public void setOverlap(final boolean overlap) {
    if (overlap) {
      this.classificationFlags |= 0b1000;
    } else {
      this.classificationFlags &= ~0b1000;
    }
  }

  @Override
  public LasPoint6GpsTime setReturnByte(final byte returnByte) {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public LasPoint6GpsTime setReturnNumber(final byte returnNumber) {
    if (returnNumber >= 0 && returnNumber <= 15) {
      this.returnByte &= 0b11110000;
      this.returnByte |= returnNumber;
    } else {
      throw new IllegalArgumentException("returnNumber must be in range 1..15: " + returnNumber);
    }
    return this;
  }

  @Override
  public LasPoint6GpsTime setScanAngle(final short scanAngle) {
    this.scanAngle = scanAngle;
    return this;
  }

  @Override
  public LasPoint6GpsTime setScanAngleRank(final byte scanAngleRank) {
    this.scanAngle = (short)(scanAngleRank / 0.006);
    return this;
  }

  @Override
  public LasPoint6GpsTime setScanDirectionFlag(final boolean scanDirectionFlag) {
    if (scanDirectionFlag) {
      this.classificationFlags |= 0b1000000;
    } else {
      this.classificationFlags &= ~0b1000000;
    }
    return this;
  }

  @Override
  public LasPoint6GpsTime setScannerChannel(final byte scannerChannel) {
    this.classificationFlags &= 0b11001111;
    this.scannerChannel |= scannerChannel << 4;
    return this;
  }

  @Override
  public LasPoint6GpsTime setSynthetic(final boolean synthetic) {
    if (synthetic) {
      this.classificationFlags |= 0b1;
    } else {
      this.classificationFlags &= ~0b1;
    }
    return this;
  }

  @Override
  public LasPoint6GpsTime setUserData(final short userData) {
    this.userData = userData;
    return this;
  }

  @Override
  public LasPoint6GpsTime setWithheld(final boolean withheld) {
    if (withheld) {
      this.classificationFlags |= 0b100;
    } else {
      this.classificationFlags &= ~0b100;
    }
    return this;
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    addToMap(map, "intensity", this.intensity);
    addToMap(map, "returnNumber", getReturnNumber());
    addToMap(map, "numberOfReturns", getNumberOfReturns());
    addToMap(map, "scanDirectionFlag", isScanDirectionFlag());
    addToMap(map, "edgeOfFlightLine", isEdgeOfFlightLine());
    addToMap(map, "classification", this.classification);
    addToMap(map, "synthetic", isSynthetic());
    addToMap(map, "keyPoint", isKeyPoint());
    addToMap(map, "withheld", isWithheld());
    addToMap(map, "scanAngle", getScanAngleDegrees());
    addToMap(map, "userData", this.userData);
    addToMap(map, "pointSourceID", this.pointSourceID);
    addToMap(map, "overlap", isOverlap());
    addToMap(map, "scannerChannel", this.scannerChannel);
    addToMap(map, "gpsTime", this.gpsTime);
    return map;
  }

  @Override
  public void writeLasPoint(final ChannelWriter out) {
    final int xRecord = getXInt();
    final int yRecord = getYInt();
    final int zRecord = getZInt();

    out.putInt(xRecord);
    out.putInt(yRecord);
    out.putInt(zRecord);

    out.putUnsignedShort(this.intensity);
    out.putByte(this.returnByte);

    out.putByte(this.classificationFlags);
    out.putUnsignedByte(this.classification);
    out.putUnsignedByte(this.userData);
    out.putShort(this.scanAngle);
    out.putUnsignedShort(this.pointSourceID);
    out.putDouble(this.gpsTime);
  }

}
