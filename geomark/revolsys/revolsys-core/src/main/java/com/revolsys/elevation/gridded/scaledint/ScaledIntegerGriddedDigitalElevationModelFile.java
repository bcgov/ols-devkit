package com.revolsys.elevation.gridded.scaledint;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Set;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.collection.set.Sets;
import com.revolsys.elevation.gridded.DirectFileElevationModel;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.grid.RectangularMapGrid;
import com.revolsys.io.Buffers;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.io.channels.DataReader;
import com.revolsys.io.file.Paths;

public class ScaledIntegerGriddedDigitalElevationModelFile extends DirectFileElevationModel {
  private static final int ELEVATION_BYTE_COUNT = 4;

  public static ScaledIntegerGriddedDigitalElevationModelFile newModel(final Path basePath,
    final GeometryFactory geometryFactory, final int minX, final int minY, final int gridWidth,
    final int gridHeight, final double gridCellSize) {

    final Path rowDirectory = basePath.resolve(Integer.toString(minX));
    final int coordinateSystemId = geometryFactory.getHorizontalCoordinateSystemId();
    final String fileName = RectangularMapGrid.getTileFileName("dem", coordinateSystemId,
      Integer.toString((int)gridCellSize * gridWidth), minX, minY, "demcs");
    final Path path = rowDirectory.resolve(fileName);
    return new ScaledIntegerGriddedDigitalElevationModelFile(path, geometryFactory, minX, minY,
      gridWidth, gridHeight, (int)gridCellSize);
  }

  private final Path path;

  private FileChannel channel;

  private DataReader reader;

  private final Set<OpenOption> openOptions;

  private final FileAttribute<?>[] fileAttributes = Paths.FILE_ATTRIBUTES_NONE;

  private final ByteBuffer buffer = ByteBuffer.allocateDirect(ELEVATION_BYTE_COUNT);

  private ByteBuffer rowBuffer;

  private double scaleZ;

  private boolean createMissing = false;

  private boolean useLocks = false;

  public ScaledIntegerGriddedDigitalElevationModelFile(final Path path) {
    super(ScaledIntegerGriddedDigitalElevation.HEADER_SIZE,
      ScaledIntegerGriddedDigitalElevation.RECORD_SIZE);
    this.path = path;
    this.openOptions = Paths.OPEN_OPTIONS_READ_SET;
    readHeader();
  }

  public ScaledIntegerGriddedDigitalElevationModelFile(final Path path,
    final GeometryFactory geometryFactory, final int minX, final int minY, final int gridWidth,
    final int gridHeight, final double gridCellSize) {
    super(geometryFactory, minX, minY, gridWidth, gridHeight, gridCellSize,
      ScaledIntegerGriddedDigitalElevation.HEADER_SIZE, 4);
    setModified(false);
    this.openOptions = Sets.newHash(StandardOpenOption.READ, StandardOpenOption.WRITE,
      StandardOpenOption.SYNC);
    this.path = path;

    this.scaleZ = geometryFactory.getScaleZ();
    if (this.scaleZ <= 0) {
      this.scaleZ = 1000;
    }
  }

  @Override
  public void close() {
    super.close();
    final FileChannel fileChannel = this.channel;
    this.channel = null;
    if (fileChannel != null) {
      try {
        fileChannel.close();
      } catch (final IOException e) {
      }
    }
  }

  protected void createNewFile() throws IOException {
    Paths.createParentDirectories(this.path);
    this.channel = FileChannel.open(this.path, Paths.OPEN_OPTIONS_READ_WRITE_SET,
      this.fileAttributes);
    try (
      final ChannelWriter writer = new ChannelWriter(this.channel)) {
      final int gridWidth = getGridWidth();
      final int gridHeight = getGridHeight();
      final double gridCellWidth = getGridCellWidth();
      final double gridCellHeight = getGridCellHeight();
      final BoundingBox boundingBox = getBoundingBox();
      final GeometryFactory geometryFactory = getGeometryFactory();
      ScaledIntegerGriddedDigitalElevationModelWriter.writeHeader(writer, boundingBox,
        geometryFactory, gridWidth, gridHeight, gridCellWidth, gridCellHeight);
      final int count = gridWidth * gridHeight;
      for (int i = 0; i < count; i++) {
        writer.putInt(Integer.MIN_VALUE);
      }
    }
  }

  private FileChannel getFileChannel() throws IOException {
    if (this.channel == null && isOpen()) {
      try {
        this.channel = FileChannel.open(this.path, this.openOptions, this.fileAttributes);
        this.reader = new ChannelReader(this.channel);
      } catch (final NoSuchFileException e) {
        if (this.createMissing) {
          createNewFile();
        } else {
          throw e;
        }

      }
      if (!isOpen()) {
        close();
        return null;
      }
    }
    return this.channel;
  }

  private DataReader getReader() throws IOException {
    getFileChannel();
    return this.reader;
  }

  public boolean isCreateMissing() {
    return this.createMissing;
  }

  public boolean isUseLocks() {
    return this.useLocks;
  }

  @Override
  protected synchronized double readElevation(final int offset) {
    try {
      final FileChannel fileChannel = getFileChannel();
      if (fileChannel == null) {
        return Double.NaN;
      } else {
        this.buffer.clear();
        while (this.buffer.hasRemaining()) {
          if (fileChannel.read(this.buffer, offset) == -1) {
            return Double.NaN;
          }
        }
        this.buffer.flip();
        final int elevationInt = this.buffer.getInt();
        if (elevationInt == Integer.MIN_VALUE) {
          return Double.NaN;
        } else {
          return elevationInt / this.scaleZ;
        }
      }
    } catch (final NoSuchFileException e) {
      return Double.NaN;
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to read: " + this.path, e);
    } finally {
      this.buffer.clear();
    }
  }

  private void readHeader() {
    try {
      final DataReader reader = getReader();

      final byte[] fileTypeBytes = new byte[6];
      this.reader.getBytes(fileTypeBytes);
      @SuppressWarnings("unused")
      final String fileType = new String(fileTypeBytes, StandardCharsets.UTF_8); // File
                                                                                 // type
      final short version = this.reader.getShort();
      final GeometryFactory geometryFactory = GeometryFactory.readOffsetScaled3d(this.reader);
      this.scaleZ = geometryFactory.getScaleZ();

      final double minX = reader.getDouble();
      final double minY = reader.getDouble();
      final double minZ = reader.getDouble();
      final double maxX = reader.getDouble();
      final double maxY = reader.getDouble();
      final double maxZ = reader.getDouble();
      int gridWidth;
      int gridHeight;
      final double gridCellWidth;
      final double gridCellHeight;
      if (version == 1) {
        gridCellWidth = reader.getInt();
        gridCellHeight = gridCellWidth;
        gridWidth = reader.getInt();
        gridHeight = reader.getInt();
      } else {
        gridWidth = reader.getInt();
        gridHeight = reader.getInt();
        gridCellWidth = reader.getDouble();
        gridCellHeight = reader.getDouble();
      }
      setGeometryFactory(geometryFactory);
      final BoundingBox boundingBox = geometryFactory.newBoundingBox(3, minX, minY, minZ, maxX,
        maxY, maxZ);
      setBoundingBox(boundingBox);
      setGridWidth(gridWidth);
      setGridHeight(gridHeight);
      setGridCellWidth(gridCellWidth);
      setGridCellHeight(gridCellHeight);
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to read: " + this.path, e);
    }
  }

  public void setCreateMissing(final boolean createMissing) {
    this.createMissing = createMissing;
  }

  public void setElevations(final double x, final double y, final double[] elevations) {
    final int gridX = getGridCellX(x);
    final int gridY = getGridCellY(y);
    setElevations(gridX, gridY, elevations);
  }

  public void setElevations(final int gridX, final int gridY, final double[] elevations) {
    try {
      final FileChannel fileChannel = getFileChannel();

      if (fileChannel != null) {
        ByteBuffer buffer = this.rowBuffer;
        final int gridWidth2 = getGridWidth();
        if (buffer == null) {
          buffer = ByteBuffer.allocateDirect(4 * gridWidth2);
          this.rowBuffer = buffer;
        }
        final double scale = this.scaleZ;
        for (final double elevation : elevations) {
          final int elevationInt;
          if (Double.isFinite(elevation)) {
            elevationInt = (int)Math.round(elevation * scale);
          } else {
            elevationInt = Integer.MIN_VALUE;
          }
          buffer.putInt(elevationInt);
        }
        final int offset = this.headerSize + (gridY * gridWidth2 + gridX) * ELEVATION_BYTE_COUNT;
        if (this.useLocks) {
          try (
            FileLock lock = fileChannel.lock(offset, elevations.length * ELEVATION_BYTE_COUNT,
              false)) {
            Buffers.writeAll(fileChannel, buffer, offset);
          }
        } else {
          Buffers.writeAll(fileChannel, buffer, offset);
        }
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to read: " + this.path, e);
    }
  }

  public void setUseLocks(final boolean useLocks) {
    this.useLocks = useLocks;
  }

  @Override
  public void setValueNull(final int gridX, final int gridY) {
    final int gridWidth = getGridWidth();
    final int offset = this.headerSize + (gridY * gridWidth + gridX) * this.elevationByteCount;
    writeElevation(offset, Integer.MIN_VALUE);
  }

  @Override
  protected synchronized void writeElevation(final int offset, final double elevation) {
    int elevationInt;
    if (Double.isFinite(elevation)) {
      final double scale = this.scaleZ;
      elevationInt = (int)Math.round(elevation * scale);
    } else {
      elevationInt = Integer.MIN_VALUE;
    }
    writeElevation(offset, elevationInt);
  }

  protected void writeElevation(int offset, final int elevationInt) {
    try {
      final FileChannel fileChannel = getFileChannel();
      if (fileChannel != null) {

        this.buffer.putInt(elevationInt);
        this.buffer.flip();
        while (this.buffer.hasRemaining()) {
          offset += fileChannel.write(this.buffer, offset);
        }
        this.buffer.clear();
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to read: " + this.path, e);
    }
  }
}
