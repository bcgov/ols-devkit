package com.revolsys.elevation.cloud.las;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.jeometry.common.logging.Logs;
import org.jeometry.coordinatesystem.model.CoordinateOperationMethod;
import org.jeometry.coordinatesystem.model.CoordinateSystem;
import org.jeometry.coordinatesystem.model.GeographicCoordinateSystem;
import org.jeometry.coordinatesystem.model.ParameterName;
import org.jeometry.coordinatesystem.model.ParameterValue;
import org.jeometry.coordinatesystem.model.ProjectedCoordinateSystem;
import org.jeometry.coordinatesystem.model.systems.EpsgCoordinateSystems;
import org.jeometry.coordinatesystem.model.systems.EsriCoordinateSystems;
import org.jeometry.coordinatesystem.model.unit.LinearUnit;

import com.revolsys.collection.map.Maps;
import com.revolsys.elevation.cloud.las.pointformat.LasPointFormat;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.endian.EndianOutput;
import com.revolsys.io.endian.EndianOutputStream;
import com.revolsys.raster.io.format.tiff.TiffImageFactory;
import com.revolsys.raster.io.format.tiff.code.GeoTiffKey;
import com.revolsys.raster.io.format.tiff.code.GeoTiffKeyProjectionParameterName;
import com.revolsys.raster.io.format.tiff.code.GeoTiffKeys;
import com.revolsys.raster.io.format.tiff.code.TiffPrivateTag;
import com.revolsys.util.Pair;

public class LasProjection {
  private static final String LASF_PROJECTION = "LASF_Projection";

  private static final int ID_TIFF_GEO_KEY_DIRECTORY_TAG = TiffPrivateTag.GeoKeyDirectoryTag
    .getId();

  private static final int ID_TIFF_GEO_DOUBLE_PARAMS = TiffPrivateTag.GeoDoubleParamsTag.getId();

  private static final int ID_TIFF_GEO_ASCII_PARAMS = TiffPrivateTag.GeoAsciiParamsTag.getId();

  private static final Pair<String, Integer> KEY_TIFF_GEO_ASCII_PARAMS = new Pair<>(LASF_PROJECTION,
    ID_TIFF_GEO_ASCII_PARAMS);

  private static final Pair<String, Integer> KEY_TIFF_GEO_DOUBLE_PARAMS = new Pair<>(
    LASF_PROJECTION, ID_TIFF_GEO_DOUBLE_PARAMS);

  private static final Pair<String, Integer> KEY_WKT_COORDINATE_SYSTEM = new Pair<>(LASF_PROJECTION,
    2112);

  @SuppressWarnings("unused")
  private static Object convertGeoTiffProjection(final LasPointCloudHeader header,
    final byte[] bytes) {
    final double[] doubleParams = header.getLasPropertyValue(KEY_TIFF_GEO_DOUBLE_PARAMS,
      new double[0]);
    final byte[] asciiParamsBytes = header.getLasPropertyValue(KEY_TIFF_GEO_ASCII_PARAMS,
      new byte[0]);

    final Map<GeoTiffKey, Object> properties = new LinkedHashMap<>();
    final int[] geoKeys = LasVariableLengthRecordConverter.getUnsignedShortArray(bytes);

    int i = 0;
    final int keyDirectoryVersion = geoKeys[i++];
    final int keyRevision = geoKeys[i++];
    final int minorRevision = geoKeys[i++];
    final int keyCount = geoKeys[i++];
    for (int keyIndex = 0; keyIndex < keyCount; keyIndex++) {
      final GeoTiffKey keyId = GeoTiffKeys.getById(geoKeys[i++]);
      final int tagId = geoKeys[i++];
      final int count = geoKeys[i++];
      final int offset = geoKeys[i++];
      if (tagId == 0) {
        properties.put(keyId, offset);
      } else if (TiffPrivateTag.GeoDoubleParamsTag.equalsId(tagId)) {
        final double value = doubleParams[offset];
        properties.put(keyId, value);
      } else if (TiffPrivateTag.GeoAsciiParamsTag.equalsId(tagId)) {
        final String value = new String(asciiParamsBytes, offset, count, StandardCharsets.US_ASCII);
        properties.put(keyId, value);
      }
    }
    CoordinateSystem coordinateSystem = null;
    int coordinateSystemId = Maps.getInteger(properties, GeoTiffKeys.ProjectedCSTypeGeoKey, 0);
    if (coordinateSystemId == 0) {
      coordinateSystemId = Maps.getInteger(properties, GeoTiffKeys.GeographicTypeGeoKey, 0);
      if (coordinateSystemId != 0) {
        coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(coordinateSystemId);
      }
    } else if (coordinateSystemId <= 0 || coordinateSystemId == 32767) {
      final int geoSrid = Maps.getInteger(properties, GeoTiffKeys.GeographicTypeGeoKey, 0);
      if (geoSrid != 0) {
        if (geoSrid > 0 && geoSrid < 32767) {
          final GeographicCoordinateSystem geographicCoordinateSystem = EpsgCoordinateSystems
            .getCoordinateSystem(geoSrid);
          final String name = "unknown";
          final CoordinateOperationMethod coordinateOperationMethod = TiffImageFactory
            .getProjection(properties);

          final Map<ParameterName, ParameterValue> parameters = GeoTiffKeyProjectionParameterName
            .getProjectionParameters(properties);

          final LinearUnit linearUnit = TiffImageFactory.getLinearUnit(properties);
          final ProjectedCoordinateSystem projectedCoordinateSystem = new ProjectedCoordinateSystem(
            coordinateSystemId, name, geographicCoordinateSystem, coordinateOperationMethod,
            parameters, linearUnit);
          coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(projectedCoordinateSystem);
        }
      }
    } else {
      coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(coordinateSystemId);
    }
    header.setCoordinateSystemInternal(coordinateSystem);
    return coordinateSystem;
  }

  private static Object convertWktProjection(final LasPointCloudHeader header, final byte[] bytes) {
    final String wkt = new String(bytes, StandardCharsets.UTF_8);
    try {
      final CoordinateSystem coordinateSystem = EsriCoordinateSystems.readCoordinateSystem(wkt);
      if (coordinateSystem == null) {
        Logs.error(LasProjection.class, "Unsupported coordinate system\n" + wkt);
      } else {
        header.setCoordinateSystemInternal(coordinateSystem);
      }
      return coordinateSystem;
    } catch (final Exception e) {
      Logs.error(LasProjection.class, "Invalid coordinate system\n" + wkt);
      return null;
    }
  }

  private static byte[] coordinateSystemToWktBytes(final LasPointCloud pointCloud,
    final LasVariableLengthRecord variable) {
    final Object value = variable.getValue();
    if (value instanceof CoordinateSystem) {
      final CoordinateSystem coordinateSystem = (CoordinateSystem)value;
      final String wkt = coordinateSystem.toEpsgWkt();
      final byte[] stringBytes = wkt.getBytes(StandardCharsets.UTF_8);
      final byte[] bytes = new byte[stringBytes.length + 1];
      System.arraycopy(stringBytes, 0, bytes, 0, stringBytes.length);
      return bytes;
    } else {
      throw new IllegalArgumentException(
        "Not a CoordinateSystem\n" + value.getClass() + "\n" + value);
    }
  }

  public static void init() {
    LasVariableLengthRecordConverter.bytes(KEY_TIFF_GEO_ASCII_PARAMS);
    LasVariableLengthRecordConverter.doubleArray(KEY_TIFF_GEO_DOUBLE_PARAMS);

    new LasVariableLengthRecordConverterFunction(LASF_PROJECTION, ID_TIFF_GEO_KEY_DIRECTORY_TAG,
      LasProjection::convertGeoTiffProjection);

    new LasVariableLengthRecordConverterFunction(KEY_WKT_COORDINATE_SYSTEM,
      LasProjection::convertWktProjection, LasProjection::coordinateSystemToWktBytes);
  }

  protected static void setCoordinateSystem(final LasPointCloudHeader header,
    final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      header.removeLasProperties(LASF_PROJECTION);
      final LasPointFormat pointFormat = header.getPointFormat();
      if (pointFormat.getId() <= 5) {
        final int coordinateSystemId = geometryFactory.getHorizontalCoordinateSystemId();
        GeoTiffKey keyId;
        if (geometryFactory.isProjected()) {
          keyId = GeoTiffKeys.ProjectedCSTypeGeoKey;
        } else {
          keyId = GeoTiffKeys.GeographicTypeGeoKey;
        }

        final ByteArrayOutputStream byteOut = new ByteArrayOutputStream(1024);
        try (
          final EndianOutput out = new EndianOutputStream(byteOut)) {
          out.writeLEUnsignedShort(1);
          out.writeLEUnsignedShort(1);
          out.writeLEUnsignedShort(0);
          out.writeLEUnsignedShort(1);
          {
            out.writeLEUnsignedShort(keyId.getId());
            out.writeLEUnsignedShort(0);
            out.writeLEUnsignedShort(1);
            out.writeLEUnsignedShort(coordinateSystemId);
          }
        }
        final byte[] bytes = byteOut.toByteArray();
        final LasVariableLengthRecord property = new LasVariableLengthRecord(header,
          LASF_PROJECTION, ID_TIFF_GEO_KEY_DIRECTORY_TAG, "TIFF GeoKeyDirectoryTag", bytes,
          geometryFactory);
        header.addProperty(property);
      } else {
        header.addLasProperty(KEY_WKT_COORDINATE_SYSTEM, "WKT", geometryFactory);
      }
    }
  }

}
