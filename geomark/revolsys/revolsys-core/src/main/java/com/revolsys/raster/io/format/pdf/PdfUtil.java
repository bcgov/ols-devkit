package com.revolsys.raster.io.format.pdf;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSObject;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.editor.BoundingBoxEditor;
import com.revolsys.util.Property;

public class PdfUtil {

  public static void addFloat(final COSArray array, final double number) {
    try {
      array.add(new COSFloat(String.valueOf(number)));
    } catch (final IOException e) {
    }
  }

  public static void addInt(final COSArray array, final long number) {
    array.add(COSInteger.get(number));
  }

  public static COSArray findArray(final COSDictionary dictionary, final COSName key) {
    final COSBase item = dictionary.getDictionaryObject(key);
    if (item == null) {
      return null;
    } else if (item instanceof COSArray) {
      return (COSArray)item;
    } else {
      throw new IllegalArgumentException("Expecting COSArray not " + item.getClass());
    }
  }

  public static COSArray findArray(final COSDictionary dictionary, final String key) {
    final COSBase item = dictionary.getDictionaryObject(key);
    if (item == null) {
      return null;
    } else if (item instanceof COSArray) {
      return (COSArray)item;
    } else {
      throw new IllegalArgumentException("Expecting COSArray not " + item.getClass());
    }
  }

  public static Rectangle2D findRectangle(final COSDictionary dictionary, final COSName key) {
    final COSArray bbox = PdfUtil.findArray(dictionary, key);
    if (bbox == null) {
      return null;
    } else {
      final float x1 = getFloat(bbox, 0);
      final float y1 = getFloat(bbox, 1);
      final float x2 = getFloat(bbox, 2);
      final float y2 = getFloat(bbox, 3);
      final float x = Math.min(x1, x2);
      final float y = Math.min(y1, y2);
      final float width = Math.abs(x1 - x2);
      final float height = Math.abs(y1 - y2);
      return new Rectangle2D.Float(x, y, width, height);
    }
  }

  public static COSArray floatArray(final double... numbers) {
    final COSArray array = new COSArray();
    for (final double number : numbers) {
      addFloat(array, number);
    }
    return array;
  }

  public static COSArray getArray(final COSDictionary dictionary, final String key) {
    final COSBase item = dictionary.getDictionaryObject(key);
    if (item == null) {
      final COSArray array = new COSArray();
      dictionary.setItem(key, array);
      return array;
    } else if (item instanceof COSArray) {
      return (COSArray)item;
    } else {
      throw new IllegalArgumentException("Expecting COSArray not " + item.getClass());
    }
  }

  public static COSDictionary getDictionary(final COSDictionary dictionary, final String key) {
    final COSBase item = dictionary.getDictionaryObject(key);
    if (item == null) {
      final COSDictionary childDictionary = new COSDictionary();
      dictionary.setItem(key, childDictionary);
      return childDictionary;
    } else if (item instanceof COSDictionary) {
      return (COSDictionary)item;
    } else {
      throw new IllegalArgumentException("Expecting COSDictionary not " + item.getClass());
    }
  }

  public static float getFloat(final COSArray array, final int index) {
    final COSBase object = array.getObject(index);
    if (object instanceof COSNumber) {
      final COSNumber number = (COSNumber)object;
      return number.floatValue();
    } else {
      return 0;
    }
  }

  public static COSDictionary getPageViewport(final COSDictionary page) {
    final COSArray viewports = PdfUtil.findArray(page, "VP");
    if (viewports != null) {
      for (COSBase item : viewports) {
        if (item instanceof COSObject) {
          final COSObject object = (COSObject)item;
          item = object.getObject();
        }
        if (item instanceof COSDictionary) {
          final COSDictionary viewport = (COSDictionary)item;
          if (hasNameValue(viewport, "Type", "Viewport")) {
            return viewport;
          }
        }
      }
    }
    return null;
  }

  public static List<Point2D> getPoints(final COSDictionary dictionary, final String key) {
    final COSArray array = PdfUtil.findArray(dictionary, key);
    final List<Point2D> points = new ArrayList<>();
    if (array != null) {
      for (int i = 0; i < array.size(); i++) {
        final float x = PdfUtil.getFloat(array, i++);
        final float y = PdfUtil.getFloat(array, i);
        final Point2D point = new Point2D.Double(x, y);
        points.add(point);
      }
    }
    return points;
  }

  public static BoundingBox getViewportBoundingBox(final COSDictionary viewport) {
    if (hasNameValue(viewport, "Type", "Viewport")) {
      final COSDictionary measure = PdfUtil.getDictionary(viewport, "Measure");
      if (PdfUtil.hasNameValue(measure, "Type", "Measure")) {
        if (PdfUtil.hasNameValue(measure, "Subtype", "GEO")) {
          final COSDictionary gcs = PdfUtil.getDictionary(measure, "GCS");
          if (gcs != null) {
            GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;
            final int srid = gcs.getInt("EPSG");
            if (srid == -1) {
              final String wkt = gcs.getString("WKT");
              if (Property.hasValue(wkt)) {
                geometryFactory = GeometryFactory.floating3d(wkt);
              }
            } else {
              geometryFactory = GeometryFactory.floating3d(srid);
            }
            final GeometryFactory geoGeometryFactory = geometryFactory
              .getGeographicGeometryFactory();

            final BoundingBoxEditor boundingBox = geometryFactory.bboxEditor();
            final COSArray geoPoints = PdfUtil.findArray(measure, "GPTS");

            if (geometryFactory.isProjected()) {
              final CoordinatesOperation projection = geometryFactory
                .getCoordinatesOperation(geoGeometryFactory);
              final CoordinatesOperationPoint point = new CoordinatesOperationPoint();
              for (int i = 0; i < geoPoints.size(); i++) {
                final float lon = PdfUtil.getFloat(geoPoints, i);
                final float lat = PdfUtil.getFloat(geoPoints, i++);
                point.setPoint(lon, lat);
                projection.perform(point);
                final double x = point.x;
                final double y = point.y;
                boundingBox.addPoint(x, y);
              }
            } else {
              for (int i = 0; i < geoPoints.size(); i++) {
                final float lon = PdfUtil.getFloat(geoPoints, i);
                final float lat = PdfUtil.getFloat(geoPoints, i++);
                boundingBox.addPoint(lon, lat);
              }
            }
            return boundingBox.newBoundingBox();
          }
        }
      }
    }
    return BoundingBox.empty();
  }

  public static boolean hasNameValue(final COSDictionary dictionary, final String key,
    final String value) {
    final String name = dictionary.getNameAsString(key);
    return value.equals(name);
  }

  public static COSArray intArray(final long... numbers) {
    final COSArray array = new COSArray();
    for (final long number : numbers) {
      addInt(array, number);
    }
    return array;
  }
}
