package com.revolsys.csformat.geoid.ngabgh;

import java.nio.channels.ClosedByInterruptException;

import org.jeometry.common.exception.Exceptions;
import org.jeometry.coordinatesystem.model.systems.EpsgId;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelReader;
import com.revolsys.elevation.gridded.IntArrayScaleGriddedElevationModel;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.IoFactory;
import com.revolsys.io.channels.DataReader;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;

/*
 The total size of the file is 2,076,480 bytes. This file was created
using an INTEGER*2 data type format and is an unformatted direct access
file. The data on the file is arranged in records from north to south.
There are 721 records on the file starting with record 1 at 90 N. The
last record on the file is at latitude 90 S. For each record, there
are 1,440 15 arc-minute geoid heights arranged by longitude from west to
east starting at the Prime Meridian (0 E) and ending 15 arc-minutes west
of the Prime Meridian (359.75 E). On file, the geoid heights are in units
of centimeters. While retrieving the Integer*2 values on file, divide by
100 and this will produce a geoid height in meters.
 */
public class NgaBinaryGeoidModelReader extends BaseObjectWithProperties
  implements GriddedElevationModelReader {
  private final Resource resource;

  private final GeometryFactory geometryFactory = GeometryFactory.offsetScaled3d(EpsgId.WGS84, 0.0,
    0.0, 0.0, 0.0, 0.0, 100.0);

  private final BoundingBox boundingBox;

  private final double gridCellWidth = 0.25;

  private final int gridWidth = 1440;

  private final int gridHeight = 721;

  private final double gridCellHeight = 0.25;

  private boolean initialized = false;

  private DataReader reader;

  public NgaBinaryGeoidModelReader(final Resource resource, final MapEx properties) {
    this.resource = resource;
    this.boundingBox = this.geometryFactory.newBoundingBox(-180, -90.25, 180, 90);
    setProperties(properties);
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  @Override
  public double getGridCellHeight() {
    return this.gridCellHeight;
  }

  @Override
  public double getGridCellWidth() {
    return this.gridCellWidth;
  }

  private void open() {
    if (!this.initialized) {
      this.initialized = true;
      try {
        this.reader = IoFactory.newChannelReader(this.resource, 8192);
      } catch (final RuntimeException e) {
        if (!Exceptions.isException(e, ClosedByInterruptException.class)) {
          throw Exceptions.wrap("Unable to read : " + this.resource, e);
        }
      }

    }
  }

  @Override
  public GriddedElevationModel read() {
    open();
    final DataReader reader = this.reader;
    if (reader != null) {
      final IntArrayScaleGriddedElevationModel grid = new IntArrayScaleGriddedElevationModel(
        this.geometryFactory, -180, -90.25, this.gridWidth, this.gridHeight, this.gridCellWidth,
        this.gridCellHeight);

      try {
        // 90..-90.25
        for (int gridY = 720; gridY >= 0; gridY--) {
          // 0..179.75
          for (int gridX = 720; gridX < 1440; gridX++) {
            final int shiftCm = reader.getShort();
            final double shiftM = shiftCm / 100.0;
            grid.setValue(gridX, gridY, shiftM);
          }
          // -180..-0.25
          for (int gridX = 0; gridX < 720; gridX++) {
            final int shiftCm = reader.getShort();
            final double shiftM = shiftCm / 100.0;
            grid.setValue(gridX, gridY, shiftM);
          }
        }
        return grid;
      } catch (final RuntimeException e) {
        if (!Exceptions.isException(e, ClosedByInterruptException.class)) {
          throw Exceptions.wrap("Unable to read : " + this.resource, e);
        }
      }
    }
    return null;
  }
}
