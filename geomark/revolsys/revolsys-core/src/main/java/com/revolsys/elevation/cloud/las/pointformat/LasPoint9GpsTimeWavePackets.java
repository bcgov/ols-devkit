package com.revolsys.elevation.cloud.las.pointformat;

import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.io.channels.DataReader;
import com.revolsys.record.io.format.json.JsonObject;

public class LasPoint9GpsTimeWavePackets extends LasPoint6GpsTime implements LasPointWavePackets {
  private static final long serialVersionUID = 1L;

  private short wavePacketDescriptorIndex;

  private long byteOffsetToWaveformData;

  private long waveformPacketSizeInBytes;

  private float returnPointWaveformLocation;

  private float xT;

  private float yT;

  private float zT;

  public LasPoint9GpsTimeWavePackets(final LasPointCloud pointCloud) {
    super(pointCloud);
  }

  @Override
  public long getByteOffsetToWaveformData() {
    return this.byteOffsetToWaveformData;
  }

  @Override
  public LasPointFormat getPointFormat() {
    return LasPointFormat.ExtendedGpsTimeWavePackets;
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
