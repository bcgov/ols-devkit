package com.revolsys.elevation.gridded.scaledint;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelReader;
import com.revolsys.elevation.gridded.IntArrayScaleGriddedElevationModel;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.IoFactory;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;

public class ScaledIntegerGriddedDigitalElevationModelReader extends BaseObjectWithProperties
  implements GriddedElevationModelReader {
  private boolean initialized;

  private Resource resource;

  private ByteBuffer byteBuffer;

  private ChannelReader reader;

  private GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;

  private BoundingBox boundingBox;

  private double gridCellWidth;

  private double gridCellHeight;

  private int gridWidth;

  private int gridHeight;

  private boolean memoryMapped = false;

  private boolean exists;

  ScaledIntegerGriddedDigitalElevationModelReader(final Resource resource,
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
    final ChannelReader reader = this.reader;
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

  public boolean isMemoryMapped() {
    return this.memoryMapped;
  }

  @Override
  public GriddedElevationModel read() {
    init();
    if (this.exists) {
      try {
        final ChannelReader in = this.reader;
        final int cellCount = this.gridWidth * this.gridHeight;
        final int[] elevations = new int[cellCount];
        final ReadableByteChannel channel = in.getChannel();
        if (isMemoryMapped() && channel instanceof FileChannel) {
          final FileChannel fileChannel = (FileChannel)channel;
          final MappedByteBuffer mappedBytes = fileChannel.map(MapMode.READ_ONLY,
            ScaledIntegerGriddedDigitalElevation.HEADER_SIZE,
            cellCount * ScaledIntegerGriddedDigitalElevation.RECORD_SIZE);
          final IntBuffer intBuffer = mappedBytes.asIntBuffer();
          for (int index = 0; index < cellCount; index++) {
            elevations[index] = intBuffer.get();
          }
        } else {

          for (int index = 0; index < cellCount; index++) {
            final int elevation = in.getInt();
            elevations[index] = elevation;
          }
        }
        final IntArrayScaleGriddedElevationModel elevationModel = new IntArrayScaleGriddedElevationModel(
          this.geometryFactory, this.boundingBox, this.gridWidth, this.gridHeight,
          this.gridCellWidth, this.gridCellHeight, elevations);
        elevationModel.setResource(this.resource);
        return elevationModel;
      } catch (final ClosedByInterruptException e) {
        return null;
      } catch (final IOException | RuntimeException e) {
        if (Exceptions.isException(e, ClosedByInterruptException.class)) {
          return null;
        } else {
          throw Exceptions.wrap("Unable to read DEM: " + this.resource, e);
        }
      }
    } else {
      return null;
    }
  }

  private void readHeader() {
    final byte[] fileTypeBytes = new byte[6]; // 0 offset
    this.reader.getBytes(fileTypeBytes);
    @SuppressWarnings("unused")
    final String fileType = new String(fileTypeBytes, StandardCharsets.UTF_8);
    @SuppressWarnings("unused")
    final short version = this.reader.getShort(); // 6 offset
    final GeometryFactory geometryFactory = GeometryFactory.readOffsetScaled3d(this.reader); // 8
                                                                                             // offset
    this.geometryFactory = geometryFactory;
    final double minX = this.reader.getDouble(); // 60 offset
    final double minY = this.reader.getDouble(); // 68 offset
    final double minZ = this.reader.getDouble(); // 76 offset
    final double maxX = this.reader.getDouble(); // 84 offset
    final double maxY = this.reader.getDouble(); // 92 offset
    final double maxZ = this.reader.getDouble(); // 100 offset
    this.gridWidth = this.reader.getInt(); // 104 offset
    this.gridHeight = this.reader.getInt(); // 108 offset
    this.gridCellWidth = this.reader.getDouble(); // 116 offset
    this.gridCellHeight = this.reader.getDouble(); // 124 offset
    // 132 offset
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

  public void setMemoryMapped(final boolean memoryMapped) {
    this.memoryMapped = memoryMapped;
  }
}
