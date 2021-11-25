package com.revolsys.csformat.geoid.usngsbin;

import java.nio.ByteOrder;
import java.nio.channels.ClosedByInterruptException;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.FloatArrayGriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelReader;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.IoFactory;
import com.revolsys.io.channels.DataReader;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;

public class UsNgsBinGeoidModelReader extends BaseObjectWithProperties
  implements GriddedElevationModelReader {
  private final Resource resource;

  private final GeometryFactory geometryFactory = GeometryFactory.nad83();

  private BoundingBox boundingBox;

  private double gridCellWidth;

  private int gridWidth;

  private int gridHeight;

  private double gridCellHeight;

  private boolean initialized;

  private DataReader reader;

  public UsNgsBinGeoidModelReader(final Resource resource, final MapEx properties) {
    this.resource = resource;
    setProperties(properties);
  }

  @Override
  public BoundingBox getBoundingBox() {
    open();
    return this.boundingBox;
  }

  @Override
  public double getGridCellHeight() {
    open();
    return this.gridCellHeight;
  }

  @Override
  public double getGridCellWidth() {
    open();
    return this.gridCellWidth;
  }

  private void open() {
    if (!this.initialized) {
      this.initialized = true;
      this.reader = IoFactory.newChannelReader(this.resource, 8192);
      if (this.reader != null) {
        readHeader();
      }
    }
  }

  @Override
  public GriddedElevationModel read() {
    open();
    final DataReader reader = this.reader;
    if (reader != null) {
      try {
        final int gridWidth = this.gridWidth;
        final int gridHeight = this.gridHeight;
        final int cellCount = gridWidth * gridHeight;
        final float[] cells = new float[cellCount];
        for (int i = 0; i < cellCount; i++) {
          final float value = reader.getFloat();
          cells[i] = value;
        }
        final FloatArrayGriddedElevationModel grid = new FloatArrayGriddedElevationModel(
          this.geometryFactory, this.boundingBox, gridWidth, gridHeight, this.gridCellWidth,
          this.gridCellHeight, cells);
        return grid;
      } catch (final RuntimeException e) {
        if (Exceptions.isException(e, ClosedByInterruptException.class)) {
          return null;
        } else {
          throw Exceptions.wrap("Unable to read : " + this.resource, e);
        }
      }
    }
    return null;
  }

  @SuppressWarnings("unused")
  private void readHeader() {
    final DataReader reader = this.reader;
    double minY = reader.getDouble();
    if (Math.abs(minY) < 1e-10) {
      minY = Double.longBitsToDouble(Long.reverseBytes(Double.doubleToRawLongBits(minY)));
      reader.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    }
    final double lonEast = reader.getDouble();
    final double minX = -(360 - lonEast);
    this.gridCellHeight = reader.getDouble();
    this.gridCellWidth = reader.getDouble();
    this.gridHeight = reader.getInt();
    this.gridWidth = reader.getInt();
    final int kind = reader.getInt();
    this.boundingBox = this.geometryFactory.newBoundingBox(2, minX, minY,
      minX + this.gridCellWidth * this.gridWidth, minY + this.gridCellHeight * this.gridHeight);
  }
}
