package com.revolsys.csformat.gridshift.nadcon5;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.Buffers;

public class Nadcon5FileGrid {

  private static final String FILE_PREFIX = "nadcon5";

  public static String getDatumKey(final String datumName) {
    return datumName.replace("(", "_").replace(")", "").replace("NSRS", "").toLowerCase();
  }

  public static double quadratic(final double n, final double n2, final double n3,
    final double n4) {
    final double n5 = n3 - n2;
    return n2 + n * n5 + 0.5 * n * (n - 1.0) * (n4 - n3 - n5);
  }

  private final String sourceDatumName;

  private final String targetDatumName;

  private final String sourceDatumKey;

  private final String targetDatumKey;

  private final String gridParameter;

  private final String gridType;

  private final double gridTolerance = 5.0E-6;

  private final int HEADER_LENGTH = 52;

  private double minlat;

  private double minlon;

  private double maxlat;

  private double maxlon;

  private double dlat;

  private double dlon;

  private int gridWidth;

  private int gridHeight;

  private int ikind;

  private int cellSize;

  private int drLen;

  private final Nadcon5Region region;

  private FileChannel channel;

  private boolean open = false;

  public Nadcon5FileGrid(final Nadcon5Region region, final String sourceDatumName,
    final String targetDatumName, final String gridParameter, final String gridType) {
    this.region = region;
    if (this.region == null) {
      throw new IllegalArgumentException(
        "Region " + region + " not in " + Nadcon5Region.REGION_NAMES);
    }
    this.sourceDatumName = sourceDatumName;
    this.targetDatumName = targetDatumName;
    this.sourceDatumKey = getDatumKey(sourceDatumName);
    this.targetDatumKey = getDatumKey(targetDatumName);
    this.gridParameter = gridParameter;
    this.gridType = gridType;
  }

  public synchronized void close() {
    this.open = false;
    if (this.channel != null) {
      try {
        this.channel.close();
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
      this.channel = null;
    }
  }

  private FileChannel getChannel() {
    if (this.open) {
      return this.channel;
    } else {
      return open();
    }
  }

  private int getGridColumn(double lon) {
    if (lon < this.minlon || lon > this.maxlon) {
      if (lon < this.minlon - this.gridTolerance) {
        return -1;
      }
      lon = this.minlon;
    }
    if (lon > this.maxlon) {
      if (lon > this.maxlon + this.gridTolerance) {
        return -1;
      }
      lon = this.maxlon;
    }
    final int n2 = (int)((lon - this.minlon) / (this.dlon / 2.0)) + 1;
    final int n3 = n2 % 2 != 0 ? (n2 + 1) / 2 - 1 : n2 / 2;
    final int n4 = n3 < 1 ? 1 : n3;
    return (n4 > this.gridWidth - 2 ? this.gridWidth - 2 : n4) - 1;
  }

  private final String getGridFile() {
    final String regionName = this.region.getName();
    String regionKey = regionName.toLowerCase();
    if (regionKey.equals("stgeorge") || regionKey.equals("stpaul")
      || regionKey.equals("stlawrence")) {
      if (getYear(this.sourceDatumKey) > 1986 || getYear(this.targetDatumKey) > 1986) {
        regionKey = "alaska";
      }
    }
    final String date = this.region.getDateString();
    return FILE_PREFIX + "." + this.sourceDatumKey + "." + this.targetDatumKey + "." + regionKey
      + "." + this.gridParameter + "." + this.gridType + "." + date + ".b";
  }

  private int getGridRow(double lat) {
    if (lat < this.minlat) {
      if (lat < this.minlat - this.gridTolerance) {
        return -1;
      }
      lat = this.minlat;
    }
    if (lat > this.maxlat) {
      if (lat > this.maxlat + this.gridTolerance) {
        return -1;
      }
      lat = this.maxlat;
    }
    final int n2 = (int)((lat - this.minlat) / (this.dlat / 2.0)) + 1;
    final int n3 = n2 % 2 != 0 ? (n2 + 1) / 2 - 1 : n2 / 2;
    final int n4 = n3 < 1 ? 1 : n3;
    return (n4 > this.gridHeight - 2 ? this.gridHeight - 2 : n4) - 1;
  }

  public String getNadconVersion() {
    return "5.0";
  }

  public double getValueBiquadratic(final double lon, final double lat) {
    try {
      final FileChannel channel = getChannel();
      if (channel == null) {
        return Double.NaN;
      } else {
        final int gridY = getGridRow(lat);
        final int gridX = getGridColumn(lon);
        if (gridY == -1 || gridX == -1) {
          return Double.NaN;
        } else {
          final double x = (lon - this.minlon - this.dlon * gridX) / this.dlon;
          final double y = (lat - this.minlat - this.dlat * gridY) / this.dlat;
          final double[] cells = new double[9];
          final int bufferSize = 3 * this.cellSize;
          final ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
          int cellIndex = 0;
          for (int blockY = 0; blockY < 3; ++blockY) {
            final int offset = this.HEADER_LENGTH + (gridY + blockY) * this.drLen
              + (gridX + 1) * this.cellSize;
            channel.position(offset);
            buffer.clear();
            int readCount = channel.read(buffer, offset);
            while (readCount < bufferSize) {
              final int read = channel.read(buffer, offset + readCount);
              if (read == -1) {
                return Double.NaN;
              } else {
                readCount += read;
              }
            }

            buffer.flip();
            for (int blockX = 0; blockX < 3; ++blockX) {
              double value;
              if (this.cellSize == 4) {
                value = buffer.getFloat();
              } else {
                value = buffer.getShort();
              }
              cells[cellIndex++] = value;
            }
          }
          return quadratic(y, //
            quadratic(x, cells[0], cells[1], cells[2]), //
            quadratic(x, cells[3], cells[4], cells[5]), //
            quadratic(x, cells[6], cells[7], cells[8]) //
          );
        }
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private int getYear(final String datumName) {
    return Integer.parseInt(datumName.substring(datumName.length() - 4, datumName.length()));
  }

  public boolean isOpen() {
    return this.open && this.channel != null;
  }

  private synchronized FileChannel open() {
    if (!this.open) {
      this.open = true;
      final String fileName = "/Volumes/RS_8TB/Data/BCDEM/benchmarks/noaa/nadcon5/"
        + this.getGridFile();
      final File file = new File(fileName);
      if (file.exists()) {
        try {
          final FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.READ);
          final ByteBuffer buffer = ByteBuffer.allocate(this.HEADER_LENGTH);
          Buffers.readAll(channel, buffer);
          @SuppressWarnings("unused")
          final int u = buffer.getInt();
          this.minlat = buffer.getDouble();
          this.minlon = buffer.getDouble();
          this.dlat = buffer.getDouble();
          this.dlon = buffer.getDouble();
          this.gridHeight = buffer.getInt();
          this.gridWidth = buffer.getInt();
          this.ikind = buffer.getInt();
          this.maxlat = this.minlat + (this.gridHeight - 1) * this.dlat;
          this.maxlon = this.minlon + (this.gridWidth - 1) * this.dlon;
          this.cellSize = this.ikind == 0 || this.ikind == 1 ? 4 : 2;
          this.drLen = (this.gridWidth + 2) * this.cellSize;
          this.channel = channel;
          return channel;
        } catch (final IOException e) {
          throw Exceptions.wrap("Cannot open file:" + fileName, e);
        }
      } else {
        return this.channel;
      }
    }
    return this.channel;
  }

  @Override
  public String toString() {
    return this.sourceDatumName + " -> " + this.targetDatumName;
  }
}
