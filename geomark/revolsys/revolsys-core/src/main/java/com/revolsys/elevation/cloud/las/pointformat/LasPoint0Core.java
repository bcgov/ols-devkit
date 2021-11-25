package com.revolsys.elevation.cloud.las.pointformat;

import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.io.channels.DataReader;
import com.revolsys.record.io.format.json.JsonObject;

public class LasPoint0Core extends BaseLasPoint {
  private static final long serialVersionUID = 1L;

  private byte scanAngleRank;

  private short userData;

  private byte scannerChannel;

  private byte returnByte = 0b001001;

  private byte classificationByte;

  protected LasPoint0Core(final LasPointCloud pointCloud) {
    super(pointCloud);
  }

  @Override
  public LasPoint0Core clone() {
    return (LasPoint0Core)super.clone();
  }

  @Override
  public short getClassification() {
    return (byte)(this.classificationByte & 0b11111);
  }

  @Override
  public byte getClassificationByte() {
    return this.classificationByte;
  }

  @Override
  public byte getNumberOfReturns() {
    return (byte)(this.returnByte >> 3 & 0b111);
  }

  @Override
  public LasPointFormat getPointFormat() {
    return LasPointFormat.Core;
  }

  @Override
  public byte getReturnByte() {
    return this.returnByte;
  }

  @Override
  public byte getReturnNumber() {
    return (byte)(this.returnByte & 0b111);
  }

  @Override
  public short getScanAngle() {
    return (short)(this.scanAngleRank / 0.006);
  }

  @Override
  public double getScanAngleDegrees() {
    return this.scanAngleRank;
  }

  @Override
  public byte getScanAngleRank() {
    return this.scanAngleRank;
  }

  @Override
  public byte getScannerChannel() {
    return this.scannerChannel;
  }

  @Override
  public short getUserData() {
    return this.userData;
  }

  @Override
  public boolean isEdgeOfFlightLine() {
    return (this.returnByte >> 7 & 0b1) == 1;
  }

  @Override
  public boolean isKeyPoint() {
    return (this.classificationByte >> 6 & 0b1) == 1;
  }

  @Override
  public boolean isScanDirectionFlag() {
    return (this.returnByte >> 6 & 0b1) == 1;
  }

  @Override
  public boolean isSynthetic() {
    return (this.classificationByte >> 5 & 0b1) == 1;
  }

  @Override
  public boolean isWithheld() {
    return (this.classificationByte >> 7 & 0b1) == 1;
  }

  @Override
  public void read(final LasPointCloud pointCloud, final DataReader reader) {
    final int xRecord = reader.getInt();
    final int yRecord = reader.getInt();
    final int zRecord = reader.getInt();
    setXYZ(xRecord, yRecord, zRecord);
    this.intensity = reader.getUnsignedShort();
    this.returnByte = reader.getByte();

    this.classificationByte = reader.getByte();
    this.scanAngleRank = reader.getByte();
    this.userData = reader.getUnsignedByte();
    this.pointSourceID = reader.getUnsignedShort();
  }

  @Override
  public LasPoint0Core setClassification(final short classification) {
    if (classification >= 0 && classification <= 31) {
      byte newClassificationByte = this.classificationByte;
      newClassificationByte &= 0b11100000;
      newClassificationByte |= classification & 0b11111;
      this.classificationByte = newClassificationByte;
    } else {
      throw new IllegalArgumentException(
        "classification must be in range 0..31: " + classification);
    }
    return this;
  }

  @Override
  public LasPoint0Core setClassificationByte(final byte classificationByte) {
    this.classificationByte = classificationByte;
    return this;
  }

  @Override
  public LasPoint0Core setEdgeOfFlightLine(final boolean edgeOfFlightLine) {
    if (edgeOfFlightLine) {
      this.returnByte |= 0b10000000;
    } else {
      this.returnByte &= ~0b10000000;
    }
    return this;
  }

  @Override
  public LasPoint0Core setKeyPoint(final boolean keyPoint) {
    if (keyPoint) {
      this.classificationByte |= 0b1000000;
    } else {
      this.classificationByte &= ~0b1000000;
    }
    return this;
  }

  @Override
  public LasPoint0Core setNumberOfReturns(final byte numberOfReturns) {
    if (numberOfReturns >= 0 && numberOfReturns <= 15) {
      this.returnByte &= 0b11000111;
      this.returnByte |= numberOfReturns << 3;
    } else {
      throw new IllegalArgumentException(
        "numberOfReturns must be in range 1..15: " + numberOfReturns);
    }
    return this;
  }

  @Override
  public LasPoint0Core setReturnByte(final byte returnByte) {
    this.returnByte = returnByte;
    return this;
  }

  @Override
  public LasPoint0Core setReturnNumber(final byte returnNumber) {
    if (returnNumber >= 0 && returnNumber <= 15) {
      this.returnByte &= 0b11111000;
      this.returnByte |= returnNumber;
    } else {
      throw new IllegalArgumentException("returnNumber must be in range 1..15: " + returnNumber);
    }
    return this;
  }

  @Override
  public LasPoint setScanAngle(final short scanAngle) {
    final double degrees = scanAngle * 0.006;
    return setScanAngleRank((byte)degrees);
  }

  @Override
  public LasPoint0Core setScanAngleRank(final byte scanAngleRank) {
    this.scanAngleRank = scanAngleRank;
    return this;
  }

  @Override
  public LasPoint0Core setScanDirectionFlag(final boolean scanDirectionFlag) {
    if (scanDirectionFlag) {
      this.returnByte |= 0b1000000;
    } else {
      this.returnByte &= ~0b1000000;
    }
    return this;
  }

  @Override
  public LasPoint0Core setScannerChannel(final byte scannerChannel) {
    this.scannerChannel = scannerChannel;
    return this;
  }

  @Override
  public LasPoint0Core setSynthetic(final boolean synthetic) {
    if (synthetic) {
      this.classificationByte |= 0b100000;
    } else {
      this.classificationByte &= ~0b100000;
    }
    return this;
  }

  @Override
  public LasPoint0Core setUserData(final short userData) {
    this.userData = userData;
    return this;
  }

  @Override
  public LasPoint0Core setWithheld(final boolean withheld) {
    if (withheld) {
      this.classificationByte |= 0b10000000;
    } else {
      this.classificationByte &= ~0b10000000;
    }
    return this;
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    addToMap(map, "intensity", this.intensity, 0);
    addToMap(map, "returnNumber", getReturnNumber(), 0);
    addToMap(map, "numberOfReturns", getNumberOfReturns(), 0);
    addToMap(map, "scanDirectionFlag", isScanDirectionFlag(), false);
    addToMap(map, "edgeOfFlightLine", isEdgeOfFlightLine(), false);
    addToMap(map, "classification", getClassification());
    addToMap(map, "synthetic", isSynthetic(), false);
    addToMap(map, "keyPoint", isKeyPoint(), false);
    addToMap(map, "withheld", isWithheld(), false);
    addToMap(map, "scanAngle", this.scanAngleRank, 0);
    addToMap(map, "userData", this.userData, 0);
    addToMap(map, "pointSourceID", this.pointSourceID, 0);
    addToMap(map, "scannerChannel", this.scannerChannel, 0);
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
    out.putByte(this.classificationByte);
    out.putByte(this.scanAngleRank);
    out.putUnsignedByte(this.userData);
    out.putUnsignedShort(this.pointSourceID);
  }
}
