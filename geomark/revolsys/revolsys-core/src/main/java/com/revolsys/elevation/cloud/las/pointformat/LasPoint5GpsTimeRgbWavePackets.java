package com.revolsys.elevation.cloud.las.pointformat;

import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.io.channels.DataReader;
import com.revolsys.record.io.format.json.JsonObject;

public class LasPoint5GpsTimeRgbWavePackets extends LasPoint3GpsTimeRgb
  implements LasPointWavePackets {
  private static final long serialVersionUID = 1L;

  private short wavePacketDescriptorIndex;

  private long byteOffsetToWaveformData;

  private long waveformPacketSizeInBytes;

  private float returnPointWaveformLocation;

  private float xT;

  private float yT;

  private float zT;

  public LasPoint5GpsTimeRgbWavePackets(final LasPointCloud pointCloud) {
    super(pointCloud);
  }

  @Override
  public long getByteOffsetToWaveformData() {
    return this.byteOffsetToWaveformData;
  }

  @Override
  public LasPointFormat getPointFormat() {
    return LasPointFormat.GpsTimeRgbWavePackets;
  }

  @Override
  public float getReturnPointWaveformLocation() {
    return this.returnPointWaveformLocation;
  }

  @Override
  public long getWaveformPacketSizeInBytes() {
    return this.waveformPacketSizeInBytes;
  }

  @Override
  public short getWavePacketDescriptorIndex() {
    return this.wavePacketDescriptorIndex;
  }

  @Override
  public float getXT() {
    return this.xT;
  }

  @Override
  public float getYT() {
    return this.yT;
  }

  @Override
  public float getZT() {
    return this.zT;
  }

  @Override
  public void read(final LasPointCloud pointCloud, final DataReader reader) {
    super.read(pointCloud, reader);
    this.wavePacketDescriptorIndex = reader.getUnsignedByte();
    this.byteOffsetToWaveformData = reader.getUnsignedLong();
    this.waveformPacketSizeInBytes = reader.getUnsignedInt();
    this.returnPointWaveformLocation = reader.getFloat();
    this.xT = reader.getFloat();
    this.yT = reader.getFloat();
    this.zT = reader.getFloat();
  }

  @Override
  public LasPoint5GpsTimeRgbWavePackets setByteOffsetToWaveformData(
    final long byteOffsetToWaveformData) {
    this.byteOffsetToWaveformData = byteOffsetToWaveformData;
    return this;
  }

  @Override
  public LasPoint5GpsTimeRgbWavePackets setReturnPointWaveformLocation(
    final float returnPointWaveformLocation) {
    this.returnPointWaveformLocation = returnPointWaveformLocation;
    return this;
  }

  @Override
  public LasPoint5GpsTimeRgbWavePackets setWaveformPacketSizeInBytes(
    final long waveformPacketSizeInBytes) {
    this.waveformPacketSizeInBytes = waveformPacketSizeInBytes;
    return this;
  }

  @Override
  public LasPoint5GpsTimeRgbWavePackets setWavePacketDescriptorIndex(
    final short wavePacketDescriptorIndex) {
    this.wavePacketDescriptorIndex = wavePacketDescriptorIndex;
    return this;
  }

  @Override
  public LasPoint5GpsTimeRgbWavePackets setXT(final float xt) {
    this.xT = xt;
    return this;
  }

  @Override
  public LasPoint5GpsTimeRgbWavePackets setYT(final float yt) {
    this.yT = yt;
    return this;
  }

  @Override
  public LasPoint5GpsTimeRgbWavePackets setZT(final float zt) {
    this.zT = zt;
    return this;
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    addToMap(map, "wavePacketDescriptorIndex", this.wavePacketDescriptorIndex);
    addToMap(map, "byteOffsetToWaveformData", this.byteOffsetToWaveformData);
    addToMap(map, "waveformPacketSizeInBytes", this.waveformPacketSizeInBytes);
    addToMap(map, "returnPointWaveformLocation", this.returnPointWaveformLocation);
    addToMap(map, "xT", this.xT);
    addToMap(map, "yT", this.yT);
    addToMap(map, "zT", this.zT);
    return map;
  }

  @Override
  public void writeLasPoint(final ChannelWriter out) {
    super.writeLasPoint(out);
    out.putUnsignedByte(this.wavePacketDescriptorIndex);
    out.putUnsignedLong(this.byteOffsetToWaveformData);
    out.putUnsignedInt(this.waveformPacketSizeInBytes);
    out.putFloat(this.returnPointWaveformLocation);
    out.putFloat(this.xT);
    out.putFloat(this.yT);
    out.putFloat(this.zT);
  }

}
