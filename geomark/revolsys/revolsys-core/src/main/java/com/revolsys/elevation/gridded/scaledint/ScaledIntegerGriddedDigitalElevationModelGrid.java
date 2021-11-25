package com.revolsys.elevation.gridded.scaledint;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.LinkedList;

import com.revolsys.collection.map.IntHashMap;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.grid.AbstractGrid;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.InProcess;
import com.revolsys.parallel.process.ProcessNetwork;

public class ScaledIntegerGriddedDigitalElevationModelGrid extends AbstractGrid
  implements GriddedElevationModel {

  private class ElevationFile {
    private final int tileX;

    private final int tileY;

    private FileChannel fileChannel;

    private final ByteBuffer bytes = ByteBuffer.allocateDirect(4);

    private final Path path;

    private boolean exists = true;

    public ElevationFile(final int tileX, final int tileY) {
      this.tileX = tileX;
      this.tileY = tileY;
      this.path = getPath(tileX, tileY);
    }

    private void close() {
      final FileChannel fileChannel = this.fileChannel;
      if (fileChannel != null) {
        this.fileChannel = null;
        try {
          fileChannel.close();
        } catch (final Exception e) {
        }
      }
    }

    @Override
    protected void finalize() throws Throwable {
      this.fileChannel.close();
    }

    private synchronized double getElevation(final long offset) throws IOException {
      checkNotClosed();
      final int elevationInt = getInt(offset);
      if (elevationInt == Integer.MIN_VALUE) {
        return Double.NaN;
      } else {
        return elevationInt / ScaledIntegerGriddedDigitalElevationModelGrid.this.scaleZ;
      }
    }

    private synchronized int getInt(final long offset) throws IOException {
      if (this.exists) {
        FileChannel fileChannel = this.fileChannel;
        if (fileChannel == null) {
          try {
            fileChannel = this.fileChannel = FileChannel.open(this.path, StandardOpenOption.READ);
            addOpenFile(this);
          } catch (final Exception e) {
            this.exists = false;
            return Integer.MIN_VALUE;
          }
        }
        final ByteBuffer bytes = this.bytes;
        bytes.rewind();
        fileChannel.read(bytes, offset);
        if (!ScaledIntegerGriddedDigitalElevationModelGrid.this.cacheFiles) {
          fileChannel.close();
        }
        return bytes.getInt(0);
      } else {
        return Integer.MIN_VALUE;
      }
    }

    private synchronized void remove(final Iterator<ElevationFile> iterator) {
      close();
      iterator.remove();
    }

    @Override
    public String toString() {
      return this.tileX + "," + this.tileY;
    }

  }

  private final double scaleZ;

  private final IntHashMap<IntHashMap<ElevationFile>> filesByXandY = new IntHashMap<>();

  private final int gridSizePixels;

  private final int gridCellSizeInt;

  private final int gridTileSize;

  private final int coordinateSystemId;

  private final Path tileBasePath;

  private final String filePrefix;

  private int maxOpenFiles = 10000;

  private final LinkedList<ElevationFile> openFiles = new LinkedList<>();

  private boolean cacheFiles = true;

  private boolean closed = false;

  private final InProcess<ElevationFile> addFileProcess = InProcess
    .<ElevationFile> lambda(this::addOpenFile)//
    .setInBufferSize(100);

  private final ProcessNetwork processes = new ProcessNetwork(this.addFileProcess);

  public ScaledIntegerGriddedDigitalElevationModelGrid(final Path basePath, final String filePrefix,
    final int coordinateSystemId, final int gridTileSize, final int gridCellSize,
    final double scaleZ) {
    setGridCellWidth(gridCellSize);
    setGridCellHeight(gridCellSize);
    this.coordinateSystemId = coordinateSystemId;
    this.gridTileSize = gridTileSize;
    this.gridCellSizeInt = gridCellSize;
    this.filePrefix = filePrefix;
    this.scaleZ = scaleZ;
    this.gridSizePixels = gridTileSize / gridCellSize;
    this.tileBasePath = basePath//
      .resolve(ScaledIntegerGriddedDigitalElevation.FILE_EXTENSION)//
      .resolve(Integer.toString(coordinateSystemId))//
      .resolve(Integer.toString(gridTileSize))//
    ;
    this.processes.start();
  }

  private void addOpenFile(final Channel<ElevationFile> channel, final ElevationFile file) {
    final LinkedList<ElevationFile> openFiles = this.openFiles;
    openFiles.add(file);
    if (openFiles.size() > this.maxOpenFiles) {
      for (final Iterator<ElevationFile> iterator = openFiles.iterator(); openFiles
        .size() > this.maxOpenFiles && iterator.hasNext();) {
        final ElevationFile closeFile = iterator.next();
        closeFile.remove(iterator);
      }
    }
  }

  private void addOpenFile(final ElevationFile elevationFile) {
    if (this.cacheFiles) {
      this.addFileProcess.write(elevationFile);
    }
  }

  private void checkNotClosed() {
    if (this.closed) {
      throw new IllegalStateException("closed");
    }
  }

  @Override
  public void close() {
    this.closed = true;
    this.addFileProcess.getIn().close();
    for (final ElevationFile elevationFile : this.openFiles) {
      elevationFile.close();
    }
    this.processes.stop();
  }

  private ElevationFile getElevationFile(final int tileX, final int tileY) throws IOException {
    checkNotClosed();
    IntHashMap<ElevationFile> filesByY;
    synchronized (this.filesByXandY) {
      filesByY = this.filesByXandY.get(tileX);
      if (filesByY == null) {
        filesByY = new IntHashMap<>();
        this.filesByXandY.put(tileX, filesByY);
      }
    }
    synchronized (filesByY) {
      ElevationFile file = filesByY.get(tileY);
      if (file == null) {
        file = new ElevationFile(tileX, tileY);
        filesByY.put(tileY, file);
      }
      return file;
    }
  }

  @Override
  public double getGridMinX() {
    return 0;
  }

  @Override
  public double getGridMinY() {
    return 0;
  }

  public int getMaxOpenFiles() {
    return this.maxOpenFiles;
  }

  private Path getPath(final int tileX, final int tileY) {
    final StringBuilder fileNameBuilder = new StringBuilder(this.filePrefix);
    fileNameBuilder.append('_');
    fileNameBuilder.append(this.coordinateSystemId);
    fileNameBuilder.append('_');
    fileNameBuilder.append(this.gridTileSize);
    fileNameBuilder.append('_');
    fileNameBuilder.append(tileX);
    fileNameBuilder.append('_');
    fileNameBuilder.append(tileY);
    fileNameBuilder.append('.');
    fileNameBuilder.append(ScaledIntegerGriddedDigitalElevation.FILE_EXTENSION);
    final String fileName = fileNameBuilder.toString();
    final Path path = this.tileBasePath//
      .resolve(Integer.toString(tileX))//
      .resolve(fileName);
    return path;
  }

  @Override
  public double getValue(final double x, final double y) {
    final int gridCellSize = this.gridCellSizeInt;
    final int gridX = (int)Math.floor(x / gridCellSize);
    final int gridY = (int)Math.floor(y / gridCellSize);

    return getValue(gridX, gridY);
  }

  @Override
  public double getValue(final int gridX, final int gridY) {
    final int gridTileSize = this.gridTileSize;
    final int tileX = Math.floorDiv(gridX, gridTileSize) * gridTileSize;
    final int tileY = Math.floorDiv(gridY, gridTileSize) * gridTileSize;

    try {
      final int gridCellX = gridX - tileX;
      final int gridCellY = gridY - tileY;
      final int elevationByteSize = 4;
      final int offset = ScaledIntegerGriddedDigitalElevation.HEADER_SIZE
        + (gridCellY * this.gridSizePixels + gridCellX) * elevationByteSize;
      final ElevationFile elevationFile = getElevationFile(tileX, tileY);
      return elevationFile.getElevation(offset);
    } catch (final NoSuchFileException e) {
      return Double.NaN;
    } catch (final IOException e) {
      return Double.NaN;
    }
  }

  @Override
  public double getValueFast(final int gridX, final int gridY) {
    final int gridTileSize = this.gridTileSize;
    final int tileX = Math.floorDiv(gridX, gridTileSize) * gridTileSize;
    final int tileY = Math.floorDiv(gridY, gridTileSize) * gridTileSize;

    try {
      final int gridCellX = gridX - tileX;
      final int gridCellY = gridY - tileY;
      final int elevationByteSize = 4;
      final int offset = ScaledIntegerGriddedDigitalElevation.HEADER_SIZE
        + (gridCellY * this.gridSizePixels + gridCellX) * elevationByteSize;

      final ElevationFile elevationFile = getElevationFile(tileX, tileY);
      return elevationFile.getElevation(offset);
    } catch (final NoSuchFileException e) {
      return Double.NaN;
    } catch (final IOException e) {
      return Double.NaN;
    }
  }

  public boolean isCacheFiles() {
    return this.cacheFiles;
  }

  @Override
  public GriddedElevationModel newGrid(final GeometryFactory geometryFactory, final double x,
    final double y, final int width, final int height, final double gridCellSize) {
    throw new UnsupportedOperationException("Tiled elevation models are too large to copy");
  }

  public ScaledIntegerGriddedDigitalElevationModelGrid setCacheFiles(final boolean cacheFiles) {
    this.cacheFiles = cacheFiles;
    return this;
  }

  public ScaledIntegerGriddedDigitalElevationModelGrid setMaxOpenFiles(final int maxOpenFiles) {
    this.maxOpenFiles = maxOpenFiles;
    return this;
  }

  @Override
  public void setValue(final int gridX, final int gridY, final double elevation) {
    throw new UnsupportedOperationException("Grid is read only");
  }
}
