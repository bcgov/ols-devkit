package com.revolsys.elevation.gridded.usgsdem;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jeometry.common.exception.Exceptions;
import org.jeometry.coordinatesystem.model.CoordinateOperationMethod;
import org.jeometry.coordinatesystem.model.CoordinateSystem;
import org.jeometry.coordinatesystem.model.GeographicCoordinateSystem;
import org.jeometry.coordinatesystem.model.HorizontalCoordinateSystem;
import org.jeometry.coordinatesystem.model.ParameterName;
import org.jeometry.coordinatesystem.model.ParameterNames;
import org.jeometry.coordinatesystem.model.ParameterValue;
import org.jeometry.coordinatesystem.model.ParameterValueNumber;
import org.jeometry.coordinatesystem.model.ProjectedCoordinateSystem;
import org.jeometry.coordinatesystem.model.VerticalCoordinateSystem;
import org.jeometry.coordinatesystem.model.systems.EpsgCoordinateSystems;
import org.jeometry.coordinatesystem.model.systems.EpsgId;
import org.jeometry.coordinatesystem.model.unit.LinearUnit;
import org.jeometry.coordinatesystem.model.unit.Metre;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelReader;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.InputStreamResource;
import com.revolsys.spring.resource.NoSuchResourceException;
import com.revolsys.spring.resource.Resource;

public class UsgsGriddedElevationReader extends BaseObjectWithProperties
  implements GriddedElevationModelReader {

  private BoundingBox boundingBox = BoundingBox.empty();

  private final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

  private final byte[] bytes = new byte[1024];

  private ReadableByteChannel channel;

  private final double[] cornersX = new double[4];

  private final double[] cornersY = new double[4];

  private GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;

  private int gridHeight;

  private int gridWidth;

  private boolean initialized;

  private double resolutionX;

  private double resolutionY;

  private double resolutionZ;

  private final Resource resource;

  public UsgsGriddedElevationReader(final Resource resource, final MapEx properties) {
    setProperties(properties);
    this.resource = resource;
    if (this.geometryFactory == GeometryFactory.DEFAULT_3D) {
      this.geometryFactory = GeometryFactory.floating3d(resource, GeometryFactory.DEFAULT_3D);
    }
  }

  @Override
  public void close() {
    try {
      this.channel.close();
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private ParameterValue fromDms(final double value) {
    final double degrees = Math.floor(value / 1000000);
    final double minutes = Math.floor(Math.abs(value) % 100000 / 1000);
    final double seconds = Math.abs(value) % 100;
    final double decimal = degrees + minutes / 60 + seconds / 3600;
    return new ParameterValueNumber(decimal);
  }

  @Override
  public BoundingBox getBoundingBox() {
    init();
    return this.boundingBox;
  }

  private byte getByte1() {
    final String string = getString(1);
    if (string.isEmpty()) {
      return 0;
    } else {
      return Byte.valueOf(string);
    }
  }

  protected ReadableByteChannel getChannel() {

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
            return new InputStreamResource(in).newReadableByteChannel();
          }
        }
        throw new IllegalArgumentException("Cannot find " + fileName + " in " + this.resource);
      } else if (fileExtension.equals("gz")) {
        final InputStream in = this.resource.newBufferedInputStream();
        final GZIPInputStream gzIn = new GZIPInputStream(in);
        return new InputStreamResource(gzIn).newReadableByteChannel();
      } else {
        return this.resource.newReadableByteChannel();
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to open: " + this.resource, e);
    }
  }

  private double getDms() {
    final int degrees = getInteger(4);
    final double minutes = getInteger(2);
    final double seconds = getDoubleSci(7);
    final double fractional = minutes / 60 + seconds / 3600;
    if (degrees < 0) {
      return degrees - fractional;
    } else {
      return degrees + fractional;
    }
  }

  private double getDoubleSci(final int length) {
    String string = getString(length);
    if (string.isEmpty()) {
      return 0;
    } else {
      string = string.replace('D', 'E');
      return Double.valueOf(string);
    }
  }

  private int getGeographicCoordinateSystemId(final int horizontalDatum) {
    int geographicCoordinateSystemId;
    switch (horizontalDatum) {
      case 1: // NAD 27
        geographicCoordinateSystemId = EpsgId.NAD27;
      break;
      case 2: // WGS 72
        geographicCoordinateSystemId = 4322;
      break;
      case 3: // WGS 84
        geographicCoordinateSystemId = EpsgId.WGS84;
      break;
      case 4: // NAD 83
        geographicCoordinateSystemId = EpsgId.NAD83;
      break;

      default:
        throw new IllegalArgumentException("horizontalDatum=" + horizontalDatum
          + " not currently supported for USGS DEM: " + this.resource);
    }
    return geographicCoordinateSystemId;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public double getGridCellHeight() {
    init();
    return this.resolutionY;
  }

  @Override
  public double getGridCellWidth() {
    init();
    return this.resolutionX;
  }

  private int getInteger(final int length) {
    final String string = getString(length);
    if (string.isEmpty()) {
      return 0;
    } else {
      return Integer.parseInt(string);
    }
  }

  private LinearUnit getLinearUnit() {
    final int planimetricUom = getInteger(6);
    LinearUnit linearUnit = null;
    if (planimetricUom == 1) {
      linearUnit = EpsgCoordinateSystems.getLinearUnit("foot");
    } else if (planimetricUom == 2) {
      linearUnit = EpsgCoordinateSystems.getLinearUnit("metre");
    }
    return linearUnit;
  }

  private short getShort5() {
    final String string = getString(5);
    if (string.isEmpty()) {
      return 0;
    } else {
      return Short.parseShort(string);
    }
  }

  private String getString(final int length) {
    this.buffer.get(this.bytes, 0, length);
    int offset = 0;
    while (offset < length && this.bytes[offset] == ' ') {
      offset++;
    }
    int size = length;
    while (size > offset + 1 && this.bytes[size - 1] == ' ') {
      size--;
    }
    final String s = new String(this.bytes, offset, size - offset, StandardCharsets.US_ASCII);
    return s;
  }

  private VerticalCoordinateSystem getVerticalCoordinateSystem(final LinearUnit verticalUom) {
    final boolean metres = verticalUom instanceof Metre;
    final int verticalDatum = getInteger(2);
    switch (verticalDatum) {
      case 0:
        return null;
      case 1: // Mean Sea Level
        if (metres) {
          return EpsgCoordinateSystems.getCoordinateSystem(EpsgId.MSL_HEIGHT_METRE);
        } else {
          return EpsgCoordinateSystems.getCoordinateSystem(EpsgId.MSL_HEIGHT_FOOT);
        }
      case 2: // NGVD_1929
        if (metres) {
          return EpsgCoordinateSystems.getCoordinateSystem(EpsgId.NGVD29_HEIGHT_METRE);
        } else {
          return EpsgCoordinateSystems.getCoordinateSystem(EpsgId.NGVD29_HEIGHT_FOOT_US);
        }
      case 3: // NAVD_1988
        if (metres) {
          return EpsgCoordinateSystems.getCoordinateSystem(EpsgId.NAVD88_HEIGHT_METRE);
        } else {
          return EpsgCoordinateSystems.getCoordinateSystem(EpsgId.NAVD88_HEIGHT_FOOT);
        }

      default:
        throw new IllegalArgumentException("verticalDatum=" + verticalDatum
          + " not currently supported for USGS DEM: " + this.resource);
    }
  }

  private void init() {
    if (!this.initialized) {
      this.initialized = true;
      this.channel = getChannel();
      if (this.channel == null) {
        throw new NoSuchResourceException(this.resource);
      }
      readHeader();
    }
  }

  @Override
  public final GriddedElevationModel read() {
    init();
    final double resolutionY = this.resolutionY;
    final double resolutionZ = this.resolutionZ;
    final double scaleY = 1 / resolutionY;
    final double scaleZ = 1 / resolutionZ;
    try {
      final UsgsGriddedElevationModel elevationModel = new UsgsGriddedElevationModel(
        this.geometryFactory, this.boundingBox, this.gridWidth, this.gridHeight, this.resolutionX,
        resolutionY);

      final double minY = this.boundingBox.getMinY();
      int gridHeight = 0;
      int columnCount = 0;
      final double yShift = resolutionY / 2.0;
      while (columnCount < this.gridWidth && readBuffer()) {
        final int rowIndex = getInteger(6) - 1;
        final int columnIndex = getInteger(6) - 1;

        final int rowCount = getInteger(6);

        final int colCount = getInteger(6);
        columnCount += colCount;

        final double x = getDoubleSci(24);
        final double y = getDoubleSci(24) + yShift;

        final int gridYMax = rowIndex + rowCount;
        if (gridYMax > gridHeight) {
          gridHeight = gridYMax;
        }

        final double zOffset = getDoubleSci(24) * scaleZ;
        final double minZ = getDoubleSci(24);
        final double maxZ = getDoubleSci(24);

        for (int i = 0; i < colCount; i++) {
          final int[] elevations = new int[rowCount];
          final int gridX = columnIndex + i;
          for (int j = 0; j < rowCount; j++) {
            if (j > 145) {
              final int offset = (j - 146) % 170;
              if (offset == 0) {
                readBuffer();
              }
            }
            final int value = getInteger(6);
            if (value > -32767) {
              elevations[j] = (int)Math.round(zOffset + value);
            } else {
              elevations[j] = Integer.MIN_VALUE;
            }
          }
          final double deltaY = y - minY;
          final int gridY = (int)Math.floor(deltaY * scaleY) - 1;
          elevationModel.setColumn(gridX, gridY, elevations);
        }
      }

      elevationModel.setResource(this.resource);

      return elevationModel;
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  public boolean readBuffer() throws IOException {
    this.buffer.clear();
    int totalReadCount = 0;
    while (totalReadCount < 1024) {
      final int readCount = this.channel.read(this.buffer);
      if (readCount == -1) {
        if (totalReadCount == 0) {
          return false;
        } else {
          final int position = this.buffer.position();
          this.buffer.flip();
          return position == 1024;
        }
      } else {
        totalReadCount += readCount;
      }
    }
    this.buffer.flip();
    return true;
  }

  @SuppressWarnings("unused")
  private void readHeader() {
    try {
      if (readBuffer()) {
        final String fileName = getString(40);
        final String descriptor = getString(40);
        skip(29); // Blank 81 - 109
        final double maxLon = getDms();
        final double minLat = getDms();
        final String processCode = getString(1);
        skip(1);// Blank 137
        final String sectionIndicator = getString(3);
        final String originCode = getString(4);
        final int demLevelCode = getInteger(6);
        final int elevationPattern = getInteger(6);
        final int planimetricReferenceSystem = getInteger(6);
        final int zone = getInteger(6);
        final double[] projectionParameters = new double[15];
        for (int i = 0; i < projectionParameters.length; i++) {
          projectionParameters[i] = getDoubleSci(24);
        }
        final LinearUnit horizontalUom = getLinearUnit();
        final LinearUnit verticalUom = getLinearUnit();
        final int cornerCount = getInteger(6);
        if (cornerCount != 4) {
          throw new IllegalArgumentException("Only for corners are supported");
        }
        for (int i = 0; i < 4; i++) {
          this.cornersX[i] = getDoubleSci(24);
          this.cornersY[i] = getDoubleSci(24);
        }
        double minX = Math.min(this.cornersX[0], this.cornersX[1]);
        double maxX = Math.max(this.cornersX[2], this.cornersX[3]);
        double minY = Math.min(this.cornersY[0], this.cornersY[3]);
        double maxY = Math.max(this.cornersY[1], this.cornersY[2]);

        final double minZ = getDoubleSci(24);
        final double maxZ = getDoubleSci(24);
        final double angle = getDoubleSci(24);
        if (angle != 0) {
          throw new IllegalArgumentException(
            "Angle=" + angle + " not currently supported for USGS DEM: " + this.resource);
        }
        final int verticalAccuracy = getInteger(6);
        this.resolutionX = getDoubleSci(12);
        this.resolutionY = getDoubleSci(12);
        this.resolutionZ = getDoubleSci(12);
        final int rasterRowCount = getInteger(6);
        this.gridWidth = getInteger(6);
        final short largestContourInterval = getShort5();
        final byte largestContourIntervalUnits = getByte1();
        final short smallestContourInterval = getShort5();
        final byte smallest = getByte1();
        final int sourceYear = getInteger(4);
        final int revisionYear = getInteger(4);
        final String inspectionFlag = getString(1);
        final String dataValidationFlag = getString(1);
        final int suspectAndVoidAreaFlag = getInteger(2);
        final VerticalCoordinateSystem verticalCoordinateSystem = getVerticalCoordinateSystem(
          verticalUom);
        final int horizontalDatum = getInteger(2);
        final int geographicCoordinateSystemId = getGeographicCoordinateSystemId(horizontalDatum);
        final int dataEdition = getInteger(4);
        final int percentVoid = getInteger(4);
        final int edgeMatchWest = getInteger(2);
        final int edgeMatchNorth = getInteger(2);
        final int edgeMatchEast = getInteger(2);
        final int edgeMatchSouth = getInteger(2);
        final Double verticalDatumShift = getDoubleSci(7);

        final double scaleZ = 1.0 / this.resolutionZ;
        int horizontalCoordinateSystemId = 0;
        HorizontalCoordinateSystem horizontalCoordinateSystem = null;
        if (0 == planimetricReferenceSystem) {
          horizontalCoordinateSystemId = geographicCoordinateSystemId;
        } else if (1 == planimetricReferenceSystem) {
          // UTM Zones
          switch (horizontalDatum) {
            case 1: // NAD27
              horizontalCoordinateSystemId = EpsgId.nad27Utm(zone);
            break;
            case 2: // WGS 72
              horizontalCoordinateSystemId = EpsgId.wgs72Utm(zone);
            break;
            case 3: // WGS 84
              horizontalCoordinateSystemId = EpsgId.wgs84Utm(zone);
            break;
            case 4: // NAD 83
              horizontalCoordinateSystemId = EpsgId.nad83Utm(zone);
            break;

            default:
              throw new IllegalArgumentException("UTM horizontalDatum=" + horizontalDatum
                + " not currently supported for USGS DEM: " + this.resource);
          }
          this.gridHeight = (int)((maxY - minY) / this.resolutionY);
        } else if (2 == planimetricReferenceSystem) {
          throw new IllegalArgumentException(
            "planimetricReferenceSystem=" + planimetricReferenceSystem
              + " not currently supported for USGS DEM: " + this.resource);
        } else if (3 == planimetricReferenceSystem) {
          final Map<ParameterName, ParameterValue> parameters = new LinkedHashMap<>();
          parameters.put(ParameterNames.CENTRAL_MERIDIAN, fromDms(projectionParameters[4]));
          parameters.put(ParameterNames.STANDARD_PARALLEL_1, fromDms(projectionParameters[2]));
          parameters.put(ParameterNames.STANDARD_PARALLEL_2, fromDms(projectionParameters[3]));
          parameters.put(ParameterNames.LATITUDE_OF_ORIGIN, fromDms(projectionParameters[5]));
          parameters.put(ParameterNames.FALSE_EASTING,
            new ParameterValueNumber(projectionParameters[6]));
          parameters.put(ParameterNames.FALSE_NORTHING,
            new ParameterValueNumber(projectionParameters[7]));

          final CoordinateOperationMethod coordinateOperationMethod = new CoordinateOperationMethod(
            "Albers_Equal_Area");
          final GeographicCoordinateSystem geographicCoordinateSystem = EpsgCoordinateSystems
            .getCoordinateSystem(geographicCoordinateSystemId);
          final ProjectedCoordinateSystem projectedCoordinateSystem = new ProjectedCoordinateSystem(
            -1, "", geographicCoordinateSystem, coordinateOperationMethod, parameters,
            horizontalUom);
          final ProjectedCoordinateSystem projectedCoordinateSystem2 = EpsgCoordinateSystems
            .getCoordinateSystem(projectedCoordinateSystem);
          if (projectedCoordinateSystem2 == projectedCoordinateSystem
            || projectedCoordinateSystem2 == null) {
            horizontalCoordinateSystem = projectedCoordinateSystem;
          } else {
            horizontalCoordinateSystemId = projectedCoordinateSystem2
              .getHorizontalCoordinateSystemId();
          }
          this.gridHeight = (int)((maxY - minY) / this.resolutionY);

        } else {
          throw new IllegalArgumentException(
            "planimetricReferenceSystem=" + planimetricReferenceSystem
              + " not currently supported for USGS DEM: " + this.resource);
        }
        if (horizontalCoordinateSystemId > 0) {
          horizontalCoordinateSystem = EpsgCoordinateSystems
            .getCoordinateSystem(horizontalCoordinateSystemId);
        }
        if (this.geometryFactory == null
          || !this.geometryFactory.isHasHorizontalCoordinateSystem()) {
          if (horizontalCoordinateSystem == null) {
            throw new IllegalArgumentException("No coordinate system found: " + this.resource);
          } else {
            CoordinateSystem coordinateSystem;
            if (verticalCoordinateSystem == null) {
              coordinateSystem = horizontalCoordinateSystem;
            } else {
              final int verticalCoordinateSystemId = verticalCoordinateSystem
                .getCoordinateSystemId();
              if (horizontalCoordinateSystemId > 0 && verticalCoordinateSystemId > 0) {
                coordinateSystem = EpsgCoordinateSystems.getCompound(horizontalCoordinateSystemId,
                  verticalCoordinateSystemId);
              } else {
                coordinateSystem = horizontalCoordinateSystem.getCompound(verticalCoordinateSystem);
              }
            }
            this.geometryFactory = GeometryFactory.fixed(coordinateSystem, 3, 0.0, 0.0, scaleZ);
          }
        } else {
          final double[] scales = this.geometryFactory.newScales(3);
          scales[2] = scaleZ;
          this.geometryFactory = this.geometryFactory.convertScales(scales);
        }
        if (horizontalDatum == 3 || horizontalDatum == 4) {
        } else {
          minX = Math.floor(minX / this.resolutionX) * this.resolutionX + this.resolutionX / 2;
          maxX = Math.ceil(maxX / this.resolutionX) * this.resolutionX - this.resolutionX / 2;
          minY = Math.floor(minY / this.resolutionY) * this.resolutionY - this.resolutionY / 2;
          maxY = Math.ceil(maxY / this.resolutionY) * this.resolutionY + this.resolutionY / 2;
          this.gridHeight = (int)((maxY - minY) / this.resolutionY);
        }
        this.boundingBox = this.geometryFactory.newBoundingBox(3, minX, minY, minZ, maxX, maxY,
          maxZ);
      }
    } catch (final Exception e) {
      try {
        close();
      } catch (final Exception e1) {
      }
      throw Exceptions.wrap(e);
    }
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      this.geometryFactory = GeometryFactory.DEFAULT_3D;
    } else {
      this.geometryFactory = geometryFactory;
    }
  }

  private void skip(final int count) {
    this.buffer.position(this.buffer.position() + count);
  }

  @Override
  public String toString() {
    return this.resource.toString();
  }

}
