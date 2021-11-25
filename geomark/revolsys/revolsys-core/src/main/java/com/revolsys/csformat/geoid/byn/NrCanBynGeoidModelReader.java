package com.revolsys.csformat.geoid.byn;

import java.nio.ByteOrder;
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

public class NrCanBynGeoidModelReader extends BaseObjectWithProperties
  implements GriddedElevationModelReader {
  private final Resource resource;

  private GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;

  private BoundingBox boundingBox;

  private double gridCellWidth;

  private int gridWidth;

  private int gridHeight;

  private double gridCellHeight;

  private boolean initialized = false;

  private DataReader reader;

  public NrCanBynGeoidModelReader(final Resource resource, final MapEx properties) {
    this.resource = resource;
    setProperties(properties);
    if (this.geometryFactory == GeometryFactory.DEFAULT_3D) {
      this.geometryFactory = GeometryFactory.floating3d(resource, GeometryFactory.DEFAULT_3D);
    }
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
      try {
        this.reader = IoFactory.newChannelReader(this.resource, 8192);
      } catch (final RuntimeException e) {
        if (!Exceptions.isException(e, ClosedByInterruptException.class)) {
          throw Exceptions.wrap("Unable to read : " + this.resource, e);
        }
      }
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
        final int[] cells = new int[cellCount];
        for (int gridY = gridHeight - 1; gridY >= 0; gridY--) {
          int index = gridY * gridWidth;
          for (int gridX = 0; gridX < gridWidth; gridX++) {
            final int value = reader.getInt();
            cells[index++] = value;
          }
        }
        final IntArrayScaleGriddedElevationModel grid = new IntArrayScaleGriddedElevationModel(
          this.geometryFactory, this.boundingBox, gridWidth, gridHeight, this.gridCellWidth,
          this.gridCellHeight, cells);
        return grid;
      } catch (final RuntimeException e) {
        if (!Exceptions.isException(e, ClosedByInterruptException.class)) {
          throw Exceptions.wrap("Unable to read : " + this.resource, e);
        }
      }
    }
    return null;
  }

  @SuppressWarnings("unused")
  private void readHeader() {
    final DataReader reader = this.reader;
    reader.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    final int minYSeconds = reader.getInt();
    final int maxYSeconds = reader.getInt();
    final int minXSeconds = reader.getInt();
    final int maxXSeconds = reader.getInt();

    final int cellHeight = reader.getShort();
    final int cellWidth = reader.getShort();

    final short gridType = reader.getShort();
    final short dataType = reader.getShort();

    final double scaleZ = reader.getDouble();

    final long zByteCount = reader.getShort();

    reader.getBytes(6);

    final short dataDescription = reader.getShort();
    final short subType = reader.getShort();
    final short datum = reader.getShort();
    final short ellipsoid = reader.getShort();
    if (ellipsoid == 0) {

    } else if (ellipsoid == 1) {
    } else {
      throw new IllegalArgumentException("Ellipsoid must be in range 0..1 not " + ellipsoid);
    }
    if (datum == 0) {
      this.geometryFactory = GeometryFactory.fixed3d(EpsgId.WGS84, 3600.0, 3600.0, scaleZ);
    } else if (datum == 1) {
      this.geometryFactory = GeometryFactory.fixed3d(EpsgId.NAD83, 3600.0, 3600.0, scaleZ);
    } else {
      throw new IllegalArgumentException("Ellipsoid must be in range 0..3 not " + datum);
    }
    final short byteOrder = reader.getShort();
    final short scaleFlag = reader.getShort();

    final double geopotentialWo = reader.getDouble();
    final double gm = reader.getDouble();
    final short tideSystem = reader.getShort();
    final short refRealization = reader.getShort();
    final double epoch = reader.getFloat();
    final short recordType = reader.getShort();
    reader.getShort(); // padding

    final double minY = minYSeconds / 3600.0;
    final double maxY = maxYSeconds / 3600.0;
    final double minX = minXSeconds / 3600.0;
    final double maxX = maxXSeconds / 3600.0;

    this.gridCellWidth = cellWidth / 3600.0;
    this.gridCellHeight = cellHeight / 3600.0;
    this.gridWidth = (maxXSeconds - minXSeconds) / cellWidth + 1;
    this.gridHeight = (maxYSeconds - minYSeconds) / cellHeight + 1;

    this.boundingBox = this.geometryFactory.newBoundingBox(2, minX, minY, maxX, maxY);
    if (byteOrder == 0) {
      reader.setByteOrder(ByteOrder.BIG_ENDIAN);
    }
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      this.geometryFactory = GeometryFactory.DEFAULT_3D;
    } else {
      this.geometryFactory = geometryFactory;
    }
  }
}
