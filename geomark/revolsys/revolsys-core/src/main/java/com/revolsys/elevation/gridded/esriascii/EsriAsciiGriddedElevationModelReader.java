package com.revolsys.elevation.gridded.esriascii;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.number.Doubles;
import org.jeometry.common.number.Numbers;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.elevation.gridded.DoubleArrayGriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelReader;
import com.revolsys.geometry.io.PointReader;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Readers;
import com.revolsys.spring.resource.Resource;

public class EsriAsciiGriddedElevationModelReader extends AbstractIterator<Point>
  implements GriddedElevationModelReader, PointReader {

  private GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;

  private final Resource resource;

  private double noDataValue = 0;

  private double x;

  private double y;

  private int width = -1;

  private int height = -1;

  private double elevation = Double.NaN;

  private double gridCellSize = 0;

  private BufferedReader reader;

  private int gridX = 0;

  private int gridY = 0;

  public EsriAsciiGriddedElevationModelReader(final Resource resource, final MapEx properties) {
    this.resource = resource;
    setProperties(properties);
    if (this.geometryFactory == GeometryFactory.DEFAULT_3D) {
      this.geometryFactory = GeometryFactory.floating3d(resource, GeometryFactory.DEFAULT_3D);
    }
  }

  @Override
  protected void closeDo() {
    final BufferedReader reader = this.reader;
    if (reader != null) {
      try {
        reader.close();
      } catch (final IOException e) {
      } finally {
        this.reader = null;
      }
    }
  }

  @Override
  public BoundingBox getBoundingBox() {
    init();
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.newBoundingBox(this.x, this.y, this.x + this.width * this.gridCellSize,
      this.y + this.height * this.gridCellSize);
  }

  protected BufferedReader getBufferedReader() {

    final String fileExtension = this.resource.getFileNameExtension();
    try {
      if (fileExtension.equals("zip")) {
        final ZipInputStream in = this.resource.newBufferedInputStream(ZipInputStream::new);
        final String fileName = this.resource.getBaseName();
        final String baseName = FileUtil.getBaseName(fileName);
        final String projName = baseName + ".prj";
        for (ZipEntry zipEntry = in.getNextEntry(); zipEntry != null; zipEntry = in
          .getNextEntry()) {
          final String name = zipEntry.getName();
          if (name.equals(projName)) {
            if (this.geometryFactory != GeometryFactory.DEFAULT_3D) {
              final String wkt = FileUtil
                .getString(new InputStreamReader(in, StandardCharsets.UTF_8), false);
              final GeometryFactory geometryFactory = GeometryFactory.floating3d(wkt);
              if (geometryFactory.isHasHorizontalCoordinateSystem()) {
                this.geometryFactory = geometryFactory;
              }
            }
          } else if (name.equals(fileName)) {
            this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            return this.reader;
          }
        }
        throw new IllegalArgumentException("Cannot find " + fileName + " in " + this.resource);
      } else if (fileExtension.equals("gz")) {
        final InputStream in = this.resource.newBufferedInputStream();
        final GZIPInputStream gzIn = new GZIPInputStream(in);
        this.reader = new BufferedReader(new InputStreamReader(gzIn, StandardCharsets.UTF_8));
        return this.reader;
      } else {
        this.reader = this.resource.newBufferedReader();
        return this.reader;
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to open: " + this.resource, e);
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public double getGridCellHeight() {
    return this.gridCellSize;
  }

  @Override
  public double getGridCellWidth() {
    init();
    return this.gridCellSize;
  }

  public int getHeight() {
    return this.height;
  }

  public double getMinX() {
    return this.x;
  }

  public double getMinY() {
    return this.y;
  }

  @Override
  protected Point getNext() throws NoSuchElementException {
    try {
      while (this.gridY >= 0) {
        while (this.gridX < this.width) {
          if (this.elevation != this.noDataValue) {
            final Point point = this.geometryFactory.point(this.x + this.gridX * this.gridCellSize,
              this.y + this.gridY * this.gridCellSize, this.elevation);
            this.elevation = Readers.readDouble(this.reader);
            this.gridX++;
            return point;
          } else {
            this.elevation = Readers.readDouble(this.reader);
            this.gridX++;
          }
        }
        this.gridX = 0;
        this.gridY--;
      }
    } catch (final Exception e) {
      throw Exceptions.wrap("Error reading: " + this.resource, e);
    }
    throw new NoSuchElementException();
  }

  public double getNoDataValue() {
    return this.noDataValue;
  }

  public int getWidth() {
    return this.width;
  }

  @Override
  protected void initDo() {
    readHeader();
  }

  @Override
  public GriddedElevationModel read() {
    try {
      init();
      final DoubleArrayGriddedElevationModel elevationModel = new DoubleArrayGriddedElevationModel(
        this.geometryFactory, this.x, this.y, this.width, this.height, this.gridCellSize);
      elevationModel.setResource(this.resource);
      if (Maps.getBool(getProperties(), EsriAsciiGriddedElevation.PROPERTY_READ_DATA, true)) {
        for (int gridY = this.height - 1; gridY >= 0; gridY--) {
          for (int gridX = 0; gridX < this.width; gridX++) {
            if (this.elevation != this.noDataValue) {
              elevationModel.setValue(gridX, gridY, this.elevation);
            }
            this.elevation = Readers.readDouble(this.reader);
          }
        }
      }
      elevationModel.updateValues();
      return elevationModel;
    } catch (final Exception e) {
      throw Exceptions.wrap("Error reading: " + this.resource, e);
    }
  }

  private BufferedReader readHeader() {
    try {
      final BufferedReader reader = getBufferedReader();
      double xCentre = Double.NaN;
      double yCentre = Double.NaN;
      double xCorner = Double.NaN;
      double yCorner = Double.NaN;
      while (Double.isNaN(this.elevation)) {
        String keyword = Readers.readKeyword(reader);
        final Object value = keyword;
        if (Numbers.isNumber(value)) {
          this.elevation = Doubles.toValid(keyword);
        } else {
          keyword = keyword.toLowerCase();
          if ("ncols".equals(keyword)) {
            this.width = Readers.readInteger(reader);
            if (this.width <= 0) {
              throw new IllegalArgumentException("ncols must be > 0\n" + this.resource);
            }
          } else if ("nrows".equals(keyword)) {
            this.height = Readers.readInteger(reader);
            if (this.height <= 0) {
              throw new IllegalArgumentException("nrows must be > 0\n" + this.resource);
            }
          } else if ("cellsize".equals(keyword)) {
            this.gridCellSize = Readers.readDouble(reader);
            if (this.gridCellSize <= 0) {
              throw new IllegalArgumentException("cellsize must be > 0\n" + this.resource);
            }
          } else if ("xllcenter".equals(keyword)) {
            xCentre = Readers.readDouble(reader);
          } else if ("yllcenter".equals(keyword)) {
            yCentre = Readers.readDouble(reader);
          } else if ("xllcorner".equals(keyword)) {
            xCorner = Readers.readDouble(reader);
          } else if ("yllcorner".equals(keyword)) {
            yCorner = Readers.readDouble(reader);
          } else if ("nodata_value".equals(keyword)) {
            this.noDataValue = Readers.readDouble(reader);
          } else {
            // Skip unknown value
            Readers.readKeyword(reader);
          }
        }
      }
      if (this.width == 0) {
        throw new IllegalArgumentException("ncols not specified\n" + this.resource);
      } else if (this.height == 0) {
        throw new IllegalArgumentException("nrows not specified\n" + this.resource);
      } else if (this.gridCellSize == 0) {
        throw new IllegalArgumentException("cellsize not specified\n" + this.resource);
      } else if (Double.isNaN(xCentre)) {
        if (Double.isNaN(xCorner)) {
          throw new IllegalArgumentException(
            "xllcenter, yllcenter or xllcorner, yllcorner missing\n" + this.resource);
        } else {
          if (Double.isNaN(yCorner)) {
            throw new IllegalArgumentException(
              "xllcorner set must missing yllcorner\n" + this.resource);
          } else {
            this.x = xCorner;
            this.y = yCorner;
          }
        }
      } else {
        if (Double.isNaN(yCentre)) {
          throw new IllegalArgumentException(
            "xllcenter set must missing yllcenter\n" + this.resource);
        } else {
          this.x = xCentre - this.gridCellSize / 2.0;
          this.y = yCentre - this.gridCellSize / 2.0;
        }
      }
      this.gridY = this.height - 1;
      return reader;
    } catch (final Exception e) {
      throw Exceptions.wrap("Error reading: " + this.resource, e);
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
