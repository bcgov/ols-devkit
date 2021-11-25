package com.revolsys.elevation.cloud.las.pointformat;

import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.io.channels.DataReader;
import com.revolsys.record.io.format.json.JsonObject;

public class LasPoint8GpsTimeRgbNir extends LasPoint7GpsTimeRgb implements LasPointNir {
  private static final long serialVersionUID = 1L;

  private int nir;

  public LasPoint8GpsTimeRgbNir(final LasPointCloud pointCloud) {
    super(pointCloud);
  }

  @Override
  public int getNir() {
    return this.nir;
  }

  @Override
  public LasPointFormat getPointFormat() {
    return LasPointFormat.ExtendedGpsTimeRgbNir;
  }

  @Override
  public void read(final LasPointCloud pointCloud, final DataReader reader) {
    super.read(pointCloud, reader);
    this.nir = reader.getUnsignedShort();
  }

  @Override
  public LasPoint8GpsTimeRgbNir setNir(final int nir) {
    this.nir = nir;
    return this;
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    addToMap(map, "nir", this.nir);
    return map;
  }

  @Override
  public void writeLasPoint(final ChannelWriter out) {
    super.writeLasPoint(out);
    out.putUnsignedShort(this.nir);
  }

}
