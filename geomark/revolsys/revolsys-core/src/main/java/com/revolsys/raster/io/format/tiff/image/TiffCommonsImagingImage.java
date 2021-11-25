package com.revolsys.raster.io.format.tiff.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.common.ImageMetadata.ImageMetadataItem;
import org.apache.commons.imaging.common.bytesource.ByteSource;
import org.apache.commons.imaging.common.bytesource.ByteSourceFile;
import org.apache.commons.imaging.common.bytesource.ByteSourceInputStream;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata.TiffMetadataItem;
import org.apache.commons.imaging.formats.tiff.TiffImageParser;
import org.apache.commons.imaging.formats.tiff.constants.GeoTiffTagConstants;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.raster.AbstractGeoreferencedImage;
import com.revolsys.raster.io.format.tiff.TiffImageFactory;
import com.revolsys.raster.io.format.tiff.code.GeoTiffConstants;
import com.revolsys.raster.io.format.tiff.code.GeoTiffKey;
import com.revolsys.raster.io.format.tiff.code.GeoTiffKeys;
import com.revolsys.spring.resource.Resource;

public class TiffCommonsImagingImage extends AbstractGeoreferencedImage
  implements GeoTiffConstants {

  public static final int TAG_X_RESOLUTION = 282;

  public static final int TAG_Y_RESOLUTION = 283;

  public TiffCommonsImagingImage(final Resource imageResource) {
    super("tfw");
    setImageResource(imageResource);

    readImage();

    loadImageMetaData();
    postConstruct();
  }

  @Override
  public void cancelChanges() {
    if (getImageResource() != null) {
      loadImageMetaData();
      setHasChanges(false);
    }
  }

  private double getDouble(final TiffImageMetadata metaData, final int tag,
    final double defaultValue) throws ImageReadException {
    final TiffField field = getTiffField(metaData, tag);
    if (field == null) {
      return defaultValue;
    } else {
      return field.getDoubleValue();
    }
  }

  private Map<GeoTiffKey, Object> getGeoKeys(final TiffImageMetadata metaData)
    throws ImageReadException {
    final Map<GeoTiffKey, Object> geoKeys = new LinkedHashMap<>();

    final TiffField keysField = metaData
      .findField(GeoTiffTagConstants.EXIF_TAG_GEO_KEY_DIRECTORY_TAG);
    final TiffField asciiParamsField = metaData
      .findField(GeoTiffTagConstants.EXIF_TAG_GEO_ASCII_PARAMS_TAG);
    final TiffField doubleParamsField = metaData
      .findField(GeoTiffTagConstants.EXIF_TAG_GEO_DOUBLE_PARAMS_TAG);

    double[] doubleParams;
    if (doubleParamsField == null) {
      doubleParams = new double[0];
    } else {
      doubleParams = doubleParamsField.getDoubleArrayValue();
    }
    String asciiParams;
    if (asciiParamsField == null) {
      asciiParams = "";
    } else {
      asciiParams = asciiParamsField.getStringValue();
    }

    if (keysField != null) {
      final int[] keys = keysField.getIntArrayValue();
      for (int i = 4; i < keys.length; i += 4) {
        final GeoTiffKey keyId = GeoTiffKeys.getById(keys[i]);
        final int tiffTag = keys[i + 1];
        final int valueCount = keys[i + 2];
        final int valueOrOffset = keys[i + 3];

        Object value = null;
        switch (tiffTag) {
          case 34736: // DOUBLE
            value = doubleParams[valueOrOffset];
          break;
          case 34737: // ASCII
            value = asciiParams.substring(valueOrOffset, valueOrOffset + valueCount - 1);
          break;

          default:
            value = (short)valueOrOffset;
          break;
        }
        geoKeys.put(keyId, value);
      }

    }
    return geoKeys;
  }

  private TiffField getTiffField(final TiffImageMetadata metaData, final int tag) {
    for (final ImageMetadataItem item : metaData.getItems()) {
      if (item instanceof TiffMetadataItem) {
        final TiffMetadataItem tiffItem = (TiffMetadataItem)item;
        final TiffField field = tiffItem.getTiffField();
        if (field.getTag() == tag) {
          return field;
        }
      }
    }
    return null;
  }

  @Override
  public String getWorldFileExtension() {
    return "tfw";
  }

  @SuppressWarnings("unused")
  private boolean loadGeoTiffMetaData(final TiffImageMetadata metaData) throws ImageReadException {
    try {
      final int xResolution = (int)getDouble(metaData, TAG_X_RESOLUTION, 1);
      final int yResolution = (int)getDouble(metaData, TAG_Y_RESOLUTION, 1);
      setDpi(xResolution, yResolution);
    } catch (final Throwable e) {
      Logs.error(this, e);
    }
    final Map<GeoTiffKey, Object> geoKeys = getGeoKeys(metaData);
    final GeometryFactory geometryFactory = TiffImageFactory.getGeometryFactory(geoKeys);
    if (geometryFactory != null) {
      setGeometryFactory(geometryFactory);
    }

    final TiffField tiePoints = metaData.findField(GeoTiffTagConstants.EXIF_TAG_MODEL_TIEPOINT_TAG);
    if (tiePoints == null) {
      final TiffField geoTransform = metaData
        .findField(GeoTiffTagConstants.EXIF_TAG_MODEL_TRANSFORMATION_TAG);
      if (geoTransform == null) {
        return false;
      } else {
        final double[] geoTransformValues = geoTransform.getDoubleArrayValue();
        final double pixelWidth = geoTransformValues[0];
        final double yRotation = geoTransformValues[1];
        final double x1 = geoTransformValues[3];
        final double xRotation = geoTransformValues[4];
        final double pixelHeight = geoTransformValues[5];
        final double y1 = geoTransformValues[7];
        setResolutionX(pixelWidth);
        setResolutionY(pixelHeight);
        // TODO rotation
        setBoundingBox(x1, y1, pixelWidth, pixelHeight);
        return true;
      }
    } else {
      final TiffField pixelScale = metaData
        .findField(GeoTiffTagConstants.EXIF_TAG_MODEL_PIXEL_SCALE_TAG);
      if (pixelScale == null) {
        return false;
      } else {
        final double[] tiePointValues = tiePoints.getDoubleArrayValue();
        final double rasterXOffset = tiePointValues[0];
        final double rasterYOffset = tiePointValues[1];
        if (rasterXOffset != 0 && rasterYOffset != 0) {
          // These should be 0, not sure what to do if they are not
          throw new IllegalArgumentException(
            "Exepectig 0 for the raster x,y tie points in a GeoTIFF");
        }

        // Top left corner of image in model coordinates
        final double x1 = tiePointValues[3];
        final double y1 = tiePointValues[4];

        final double[] pixelScaleValues = pixelScale.getDoubleArrayValue();
        final double pixelWidth = pixelScaleValues[0];
        final double pixelHeight = pixelScaleValues[1];
        setResolutionX(pixelWidth);
        setResolutionY(pixelHeight);
        setBoundingBox(x1, y1, pixelWidth, -pixelHeight);
        return true;
      }
    }
  }

  @Override
  protected void loadMetaDataFromImage() {
    try {
      final ByteSource byteSource = newByteSource();
      final TiffImageParser imageParser = new TiffImageParser();
      final TiffImageMetadata metaData = (TiffImageMetadata)imageParser.getMetadata(byteSource);
      loadGeoTiffMetaData(metaData);
    } catch (ImageReadException | IOException e) {
      throw Exceptions.wrap("Unable to open:" + getImageResource(), e);
    }

  }

  private ByteSource newByteSource() {
    ByteSource byteSource;
    final Resource imageResource = getImageResource();
    if (imageResource.isFile()) {
      byteSource = new ByteSourceFile(imageResource.getFile());
    } else {
      final String filename = imageResource.getFilename();
      final InputStream in = imageResource.getInputStream();
      byteSource = new ByteSourceInputStream(in, filename);
    }
    return byteSource;
  }

  private void readImage() {
    final Map<String, Object> params = Collections.emptyMap();
    try {
      final ByteSource byteSource = newByteSource();
      final TiffImageParser imageParser = new TiffImageParser();
      final BufferedImage bufferedImage = imageParser.getBufferedImage(byteSource, params);
      setRenderedImage(bufferedImage);
    } catch (ImageReadException | IOException e) {
      throw Exceptions.wrap("Unable to open:" + getImageResource(), e);
    }
  }
}
