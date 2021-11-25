package com.revolsys.raster.io.format.tiff;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.formats.tiff.constants.GeoTiffTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffImageWriterLossy;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.coordinatesystem.model.Authority;
import org.jeometry.coordinatesystem.model.Ellipsoid;
import org.jeometry.coordinatesystem.model.GeographicCoordinateSystem;
import org.jeometry.coordinatesystem.model.ParameterName;
import org.jeometry.coordinatesystem.model.ParameterValue;
import org.jeometry.coordinatesystem.model.PrimeMeridian;
import org.jeometry.coordinatesystem.model.ProjectedCoordinateSystem;
import org.jeometry.coordinatesystem.model.datum.GeodeticDatum;
import org.jeometry.coordinatesystem.model.unit.AngularUnit;
import org.jeometry.coordinatesystem.model.unit.LinearUnit;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.AbstractWriter;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageWriter;
import com.revolsys.raster.io.format.tiff.code.GeoTiffConstants;
import com.revolsys.raster.io.format.tiff.code.GeoTiffCoordinateTransformationCode;
import com.revolsys.raster.io.format.tiff.code.GeoTiffKey;
import com.revolsys.raster.io.format.tiff.code.GeoTiffKeyProjectionParameterName;
import com.revolsys.raster.io.format.tiff.code.GeoTiffKeys;
import com.revolsys.spring.resource.Resource;

public class TiffGeoreferencedImageWriter extends AbstractWriter<GeoreferencedImage>
  implements GeoreferencedImageWriter, GeoTiffConstants {

  private static final int CUSTOM = 32767;

  private final Resource resource;

  public TiffGeoreferencedImageWriter(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public void write(final GeoreferencedImage image) {
    final BufferedImage bufferedImage = image.getBufferedImage();
    if (bufferedImage != null) {
      try (
        OutputStream out = this.resource.newBufferedOutputStream()) {
        final MapEx params = getProperties();

        final TiffImageWriterLossy writer = new TiffImageWriterLossy() {

          private final List<Short> geoKeys = new ArrayList<>();

          private final List<Double> geoDoubleParams = new ArrayList<>();

          private final StringBuilder geoAsciiParams = new StringBuilder();

          private void addGeographicCoordinateSystem(
            final GeographicCoordinateSystem coordinateSystem) {
            if (!addGeoKeyAuthority(GeoTiffKeys.GeographicTypeGeoKey,
              coordinateSystem.getAuthority())) {
              final AngularUnit angularUnit = coordinateSystem.getAngularUnit();
              final AngularUnit unit = angularUnit;
              final Authority authority = unit.getAuthority();
              if (!addGeoKeyAuthority(GeoTiffKeys.GeogAngularUnitsGeoKey, authority)) {
                addGeoKeyDouble(GeoTiffKeys.GeogAngularUnitSizeGeoKey, unit.toDegrees(1));
              }
              final GeodeticDatum datum = coordinateSystem.getGeodeticDatum();
              if (!addGeoKeyAuthority(GeoTiffKeys.GeogGeodeticDatumGeoKey, datum.getAuthority())) {
                final Ellipsoid ellipsoid = datum.getEllipsoid();
                if (!addGeoKeyAuthority(GeoTiffKeys.GeogEllipsoidGeoKey,
                  ellipsoid.getAuthority())) {
                  addGeoKeyDouble(GeoTiffKeys.GeogSemiMajorAxisGeoKey,
                    ellipsoid.getSemiMajorAxis());
                  addGeoKeyDouble(GeoTiffKeys.GeogSemiMinorAxisGeoKey,
                    ellipsoid.getSemiMinorAxis());
                }
              }
              final PrimeMeridian primeMeridian = coordinateSystem.getPrimeMeridian();
              if (!addGeoKeyAuthority(GeoTiffKeys.GeogPrimeMeridianGeoKey,
                primeMeridian.getAuthority())) {
                addGeoKeyShort(GeoTiffKeys.GeogPrimeMeridianGeoKey, 32767);
                addGeoKeyDouble(GeoTiffKeys.GeogPrimeMeridianLongGeoKey,
                  primeMeridian.getLongitude());
              }
            }
          }

          private void addGeographicCoordinateSystem(final GeometryFactory geometryFactory) {
            addGeoKeyShort(GeoTiffKeys.GTModelTypeGeoKey, ModelTypeGeographic);

            final GeographicCoordinateSystem coordinateSystem = geometryFactory
              .getHorizontalCoordinateSystem();
            final String coordinateSystemName = coordinateSystem.getCoordinateSystemName();
            addGeoKeyString(GeoTiffKeys.GTCitationGeoKey, coordinateSystemName);
            addGeoKeyString(GeoTiffKeys.GeogCitationGeoKey, coordinateSystemName);

            addGeographicCoordinateSystem(coordinateSystem);
          }

          private void addGeoKey(final GeoTiffKey keyId, final int tiffTag, final int valueCount,
            final int valueOrOffset) {
            this.geoKeys.add((short)keyId.getId());
            this.geoKeys.add((short)tiffTag);
            this.geoKeys.add((short)valueCount);
            this.geoKeys.add((short)valueOrOffset);
          }

          private boolean addGeoKeyAuthority(final GeoTiffKeys keyId, final Authority authority) {
            final int id = authority.getId();
            if (id > 0 && id <= 65535 && id != CUSTOM) {
              addGeoKeyShort(keyId, id);
              return true;
            } else {
              addGeoKeyShort(keyId, CUSTOM);
              return false;
            }
          }

          private void addGeoKeyDouble(final GeoTiffKey keyId, final double value) {
            addGeoKey(keyId, 34736, 1, this.geoDoubleParams.size());
            this.geoDoubleParams.add(value);
          }

          private void addGeoKeyShort(final GeoTiffKey keyId, final int value) {
            addGeoKey(keyId, 0, 1, value);
          }

          private void addGeoKeyString(final GeoTiffKey keyId, final String value) {
            if (value != null && value.length() > 0) {
              final int offset = this.geoAsciiParams.length();
              final int stringLength = value.length() + 1;
              addGeoKey(keyId, 34737, stringLength, offset);
              this.geoAsciiParams.append(value);
              this.geoAsciiParams.append('|');
            }
          }

          private void addProjectedCoordinateSystem(final GeometryFactory geometryFactory) {
            final ProjectedCoordinateSystem projectedCoordinateSystem = geometryFactory
              .getCoordinateSystem();
            final GeographicCoordinateSystem geographicCoordinateSystem = projectedCoordinateSystem
              .getGeographicCoordinateSystem();
            final int coordinateSystemId = geometryFactory.getHorizontalCoordinateSystemId();
            final String coordinateSystemName = geometryFactory.getCoordinateSystemName();

            final String geographicCSName = geographicCoordinateSystem.getCoordinateSystemName();

            final LinearUnit linearUnit = projectedCoordinateSystem.getLinearUnit();

            addGeoKeyShort(GeoTiffKeys.GTModelTypeGeoKey, ModelTypeProjected);
            addGeoKeyString(GeoTiffKeys.GTCitationGeoKey, coordinateSystemName);
            addGeoKeyString(GeoTiffKeys.PCSCitationGeoKey, coordinateSystemName);
            addGeoKeyString(GeoTiffKeys.GeogCitationGeoKey, geographicCSName);

            if (!addGeoKeyAuthority(GeoTiffKeys.ProjectedCSTypeGeoKey,
              projectedCoordinateSystem.getAuthority())) {
              addGeographicCoordinateSystem(geographicCoordinateSystem);

              addGeoKeyShort(GeoTiffKeys.ProjectedCSTypeGeoKey, coordinateSystemId);

              final int projectionCode = GeoTiffCoordinateTransformationCode
                .getCode(projectedCoordinateSystem);
              addGeoKeyShort(GeoTiffKeys.ProjCoordTransGeoKey, projectionCode);

              final Authority authority = linearUnit.getAuthority();
              if (!addGeoKeyAuthority(GeoTiffKeys.ProjLinearUnitsGeoKey, authority)) {
                addGeoKeyDouble(GeoTiffKeys.ProjLinearUnitSizeGeoKey, linearUnit.toMetres(1));
              }
              for (final Entry<ParameterName, ParameterValue> entry : projectedCoordinateSystem
                .getParameterValues()
                .entrySet()) {
                final ParameterName parameterName = entry.getKey();
                final ParameterValue value = entry.getValue();

                final GeoTiffKey key = GeoTiffKeyProjectionParameterName.getById(parameterName);
                final double valueDouble = ((Number)value.getValue()).doubleValue();
                addGeoKeyDouble(key, valueDouble);
              }
            }
          }

          private void addTransformation(final TiffOutputDirectory rootDirectory)
            throws ImageWriteException {
            final BoundingBox boundingBox = image.getBoundingBox();
            final double minX = boundingBox.getMinX();
            final double maxY = boundingBox.getMaxY();
            final double[] tiePoints = {
              0, 0, 0, //
              minX, maxY, 0//
            };
            rootDirectory.add(GeoTiffTagConstants.EXIF_TAG_MODEL_TIEPOINT_TAG, tiePoints);
            final double resolutionX = image.getResolutionX();
            final double resolutionY = image.getResolutionY();
            final double[] pixelScale = {
              resolutionX, resolutionY, 0
            };
            rootDirectory.add(GeoTiffTagConstants.EXIF_TAG_MODEL_PIXEL_SCALE_TAG, pixelScale);
          }

          @Override
          public void write(final OutputStream os, final TiffOutputSet outputSet)
            throws IOException, ImageWriteException {
            final TiffOutputDirectory rootDirectory = outputSet.getRootDirectory();
            addTransformation(rootDirectory);
            final GeometryFactory geometryFactory = image.getGeometryFactory();
            if (geometryFactory.isProjected()) {
              addProjectedCoordinateSystem(geometryFactory);
            } else if (geometryFactory.isGeographic()) {
              addGeographicCoordinateSystem(geometryFactory);
            }
            addGeoKeyShort(GeoTiffKeys.GTRasterTypeGeoKey, RasterPixelIsArea);
            if (!this.geoKeys.isEmpty()) {
              final short[] geoKeysArray = new short[this.geoKeys.size() + 4];
              geoKeysArray[0] = 1;
              geoKeysArray[1] = 1;
              geoKeysArray[2] = 1;
              geoKeysArray[3] = (short)(this.geoKeys.size() / 4);
              for (int i = 0; i < this.geoKeys.size(); i++) {
                geoKeysArray[i + 4] = this.geoKeys.get(i);
              }
              rootDirectory.add(GeoTiffTagConstants.EXIF_TAG_GEO_KEY_DIRECTORY_TAG, geoKeysArray);
              if (this.geoAsciiParams.length() > 0) {
                rootDirectory.add(GeoTiffTagConstants.EXIF_TAG_GEO_ASCII_PARAMS_TAG,
                  this.geoAsciiParams.toString());
              }

              if (!this.geoDoubleParams.isEmpty()) {
                final double[] geoDoubleParamsArray = new double[this.geoDoubleParams.size()];
                for (int i = 0; i < geoDoubleParamsArray.length; i++) {
                  geoDoubleParamsArray[i] = this.geoDoubleParams.get(i);
                }
                rootDirectory.add(GeoTiffTagConstants.EXIF_TAG_GEO_DOUBLE_PARAMS_TAG,
                  geoDoubleParamsArray);
              }
            }
            super.write(os, outputSet);
          }
        };
        writer.writeImage(bufferedImage, out, params);
      } catch (final ImageWriteException | IOException e) {
        throw Exceptions.wrap("Unable to write: " + this.resource, e);
      }
    }
  }
}
