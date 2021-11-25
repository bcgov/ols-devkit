package com.revolsys.elevation.gridded.scaledint.compressed;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelReader;
import com.revolsys.elevation.gridded.IntArrayScaleGriddedElevationModel;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.IoFactory;
import com.revolsys.io.channels.DataReader;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingInteger;
import com.revolsys.math.arithmeticcoding.ArithmeticDecoder;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;

public class CompressedScaledIntegerGriddedDigitalElevationModelReader
  extends BaseObjectWithProperties implements GriddedElevationModelReader {
  private boolean initialized;

  private Resource resource;

  private ByteBuffer byteBuffer;

  private DataReader reader;

  private GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;

  private BoundingBox boundingBox;

  private double gridCellWidth;

  private double gridCellHeight;

  private int gridWidth;

  private int gridHeight;

  private boolean exists;

  CompressedScaledIntegerGriddedDigitalElevationModelReader(final Resource resource,
    final Map<String, ? extends Object> properties) {
    this.resource = resource;
    setProperties(properties);
    if (this.geometryFactory == GeometryFactory.DEFAULT_3D) {
      this.geometryFactory = GeometryFactory.floating3d(resource, GeometryFactory.DEFAULT_3D);
    }
  }

  @Override
  public void close() {
    super.close();
    final DataReader reader = this.reader;
    this.reader = null;
    if (reader != null) {
      reader.close();
    }
    this.resource = null;
  }

  @Override
  public BoundingBox getBoundingBox() {
    init();
    return this.boundingBox;
  }

  public ByteBuffer getByteBuffer() {
    return this.byteBuffer;
  }

  @Override
  public double getGridCellHeight() {
    init();
    return this.gridCellHeight;
  }

  @Override
  public double getGridCellWidth() {
    init();
    return this.gridCellWidth;
  }

  private void init() {
    if (!this.initialized) {
      this.initialized = true;
      if (this.byteBuffer == null) {
        this.reader = IoFactory.newChannelReader(this.resource, 8192);
      } else {
        this.reader = IoFactory.newChannelReader(this.resource, this.byteBuffer);
      }
      if (this.reader == null) {
        this.exists = false;
      } else {
        this.exists = true;
        try {
          readHeader();
        } catch (final Exception e) {
          throw Exceptions.wrap("Unable to read DEM: " + this.resource, e);
        }
      }
    }
  }

  @Override
  public GriddedElevationModel read() {
    init();
    if (this.exists) {
      try {
        final DataReader reader = this.reader;
        final double minZ = this.boundingBox.getMinZ();
        final int minZInt = this.geometryFactory.toIntZ(minZ);
        final int nullInt = minZInt - 1;

        final int cellCount = this.gridWidth * this.gridHeight;
        final int[] elevations = new int[cellCount];
        int previousZ = reader.getInt();
        if (previousZ == nullInt) {
          elevations[0] = Integer.MIN_VALUE;
        } else {
          elevations[0] = previousZ;
        }
        final ArithmeticDecoder decoder = new ArithmeticDecoder();
        decoder.init(reader);
        final ArithmeticCodingInteger decompressor = decoder.newCodecInteger(32);
        decompressor.init();
        boolean leftToRight = true;
        int rowIndex = 0;
        for (int gridY = 0; gridY < this.gridHeight; gridY++) {
          if (leftToRight) {
            int startX = 0;
            if (gridY == 0) {
              startX = 1;
            } else {
              startX = 0;
            }
            for (int gridX = startX; gridX < this.gridWidth; gridX++) {
              final int zDiff = decompressor.decompress(0);
              final int zInt = previousZ + zDiff;
              if (zInt == nullInt) {
                elevations[rowIndex + gridX] = Integer.MIN_VALUE;
              } else {
                elevations[rowIndex + gridX] = zInt;
              }
              previousZ = zInt;
            }
          } else {
            for (int gridX = this.gridWidth - 1; gridX >= 0; gridX--) {
              final int zDiff = decompressor.decompress(0);
              final int zInt = previousZ + zDiff;
              if (zInt == nullInt) {
                elevations[rowIndex + gridX] = Integer.MIN_VALUE;
              } else {
                elevations[rowIndex + gridX] = zInt;
              }
              previousZ = zInt;
            }
          }
          leftToRight = !leftToRight;
          rowIndex += this.gridWidth;
        }
        final IntArrayScaleGriddedElevationModel elevationModel = new IntArrayScaleGriddedElevationModel(
          this.geometryFactory, this.boundingBox, this.gridWidth, this.gridHeight,
          this.gridCellWidth, this.gridCellHeight, elevations);
        elevationModel.setResource(this.resource);
        return elevationModel;
      } catch (final RuntimeException e) {
        if (Exceptions.isException(e, ClosedByInterruptException.class)) {
          return null;
        } else {
          throw Exceptions.wrap("Unable to read DEM: " + this.resource, e);
        }
      }
    } else

    {
      return null;
    }
  }

  private void readHeader() {
    final byte[] fileTypeBytes = new byte[8]; // 0 offset
    this.reader.getBytes(fileTypeBytes);
    @SuppressWarnings("unused")
    final String fileType = new String(fileTypeBytes, 0, 7, StandardCharsets.UTF_8);
    @SuppressWarnings("unused")
    final short version = this.reader.getShort(); // 8 offset
    @SuppressWarnings("unused")
    final short blank = this.reader.getShort();
    final GeometryFactory geometryFactory = GeometryFactory.readOffsetScaled3d(this.reader); // 12
    // offset
    this.geometryFactory = geometryFactory;
    final double minX = this.reader.getDouble(); // 64 offset
    final double minY = this.reader.getDouble(); // 72 offset
    final double minZ = this.reader.getDouble(); // 80 offset
    final double maxX = this.reader.getDouble(); // 88 offset
    final double maxY = this.reader.getDouble(); // 96 offset
    final double maxZ = this.reader.getDouble(); // 104 offset
    this.gridWidth = this.reader.getInt(); // 112 offset
    this.gridHeight = this.reader.getInt(); // 116 offset
    this.gridCellWidth = this.reader.getDouble(); // 120 offset
    this.gridCellHeight = this.reader.getDouble(); // 128 offset
    // 136 offset
    this.boundingBox = geometryFactory.newBoundingBox(3, minX, minY, minZ, maxX, maxY, maxZ);
  }

  public void setByteBuffer(final ByteBuffer byteBuffer) {
    this.byteBuffer = byteBuffer;
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      this.geometryFactory = GeometryFactory.DEFAULT_3D;
    } else {
      this.geometryFactory = geometryFactory;
    }
  }
}
