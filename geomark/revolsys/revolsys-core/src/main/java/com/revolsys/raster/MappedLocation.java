package com.revolsys.raster;

import java.awt.geom.AffineTransform;
import java.util.Map;

import com.revolsys.beans.AbstractPropertyChangeSupportProxy;
import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonObjectHash;

public class MappedLocation extends AbstractPropertyChangeSupportProxy
  implements GeometryFactoryProxy, MapSerializer {
  public static Point targetPointToPixel(final BoundingBox boundingBox, final Point point,
    final int imageWidth, final int imageHeight) {
    return toImagePoint(boundingBox, point, imageWidth, imageHeight);
  }

  public static Point toImagePoint(final BoundingBox boundingBox, Point modelPoint,
    final int imageWidth, final int imageHeight) {
    modelPoint = modelPoint.convertPoint2d(boundingBox.getGeometryFactory());
    final double modelX = modelPoint.getX();
    final double modelY = modelPoint.getY();
    final double modelDeltaX = modelX - boundingBox.getMinX();
    final double modelDeltaY = modelY - boundingBox.getMinY();

    final double modelWidth = boundingBox.getWidth();
    final double modelHeight = boundingBox.getHeight();

    final double xRatio = modelDeltaX / modelWidth;
    final double yRatio = modelDeltaY / modelHeight;

    final double imageX = imageWidth * xRatio;
    final double imageY = imageHeight * yRatio;
    return new PointDoubleXY(imageX, imageY);
  }

  public static double[] toModelCoordinates(final GeoreferencedImage image,
    final BoundingBox boundingBox, final boolean useTransform, final double... coordinates) {
    double[] targetCoordinates;
    if (useTransform) {
      targetCoordinates = new double[10];
      final AffineTransform transform = image.getAffineTransformation(boundingBox);
      transform.transform(coordinates, 0, targetCoordinates, 0, coordinates.length / 2);
    } else {
      targetCoordinates = coordinates.clone();
    }
    final int imageWidth = image.getImageWidth();
    final int imageHeight = image.getImageHeight();
    for (int vertexIndex = 0; vertexIndex < coordinates.length / 2; vertexIndex++) {
      final int vertexOffset = vertexIndex * 2;
      final double xPercent = targetCoordinates[vertexOffset] / imageWidth;
      final double yPercent = (imageHeight - targetCoordinates[vertexOffset + 1]) / imageHeight;

      final double modelWidth = boundingBox.getWidth();
      final double modelHeight = boundingBox.getHeight();

      final double modelX = boundingBox.getMinX() + modelWidth * xPercent;
      final double modelY = boundingBox.getMinY() + modelHeight * yPercent;
      targetCoordinates[vertexOffset] = modelX;
      targetCoordinates[vertexOffset + 1] = modelY;
    }
    return targetCoordinates;
  }

  private GeometryFactory geometryFactory = GeometryFactory.floating2d(0);

  private Point sourcePixel;

  private Point targetPoint;

  public MappedLocation(final int sourcePixelX, final int sourcePixelY,
    final GeometryFactory geometryFactory, final double x, final double y) {
    this(new PointDoubleXY(sourcePixelX, sourcePixelY), geometryFactory.point(x, y));
  }

  public MappedLocation(final int sourcePixelX, final int sourcePixelY, final Point targetPoint) {
    this(new PointDoubleXY(sourcePixelX, sourcePixelY), targetPoint);
  }

  public MappedLocation(final Map<String, Object> map) {
    final double sourceX = Maps.getDouble(map, "sourceX", 0.0);
    final double sourceY = Maps.getDouble(map, "sourceY", 0.0);
    this.sourcePixel = new PointDoubleXY(sourceX, sourceY);
    this.targetPoint = this.geometryFactory.geometry((String)map.get("target"));
  }

  public MappedLocation(final Point sourcePixel, final Point targetPoint) {
    this.sourcePixel = sourcePixel;
    this.targetPoint = targetPoint;
    this.geometryFactory = targetPoint.getGeometryFactory().convertAxisCount(2);
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public Point getSourcePixel() {
    return this.sourcePixel;
  }

  public double getSourcePixelX() {
    return this.sourcePixel.getX();
  }

  public double getSourcePixelY() {
    return this.sourcePixel.getY();
  }

  public Point getSourcePoint(final GeoreferencedImage image, final BoundingBox boundingBox,
    final boolean useTransform) {
    final Point sourcePixel = getSourcePixel();
    final double[] sourcePoint = toModelCoordinates(image, boundingBox, useTransform,
      sourcePixel.getX(), image.getImageHeight() - sourcePixel.getY());
    final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
    return geometryFactory.point(sourcePoint[0], sourcePoint[1]);
  }

  public LineString getSourceToTargetLine(final GeoreferencedImage image,
    final BoundingBox boundingBox, final boolean useTransform) {

    final Point sourcePixel = getSourcePixel();
    final double[] sourcePoint = toModelCoordinates(image, boundingBox, useTransform,
      sourcePixel.getX(), image.getImageHeight() - sourcePixel.getY());
    final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
    final double sourceX = sourcePoint[0];
    final double sourceY = sourcePoint[1];

    final Point targetPoint = getTargetPoint().convertGeometry(geometryFactory);
    final double targetX = targetPoint.getX();
    final double targetY = targetPoint.getY();
    return geometryFactory.lineString(2, sourceX, sourceY, targetX, targetY);
  }

  public Point getTargetPixel(final BoundingBox boundingBox, final int imageWidth,
    final int imageHeight) {
    final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
    final Point targetPointCoordinates = this.targetPoint.convertPoint2d(geometryFactory);
    return targetPointToPixel(boundingBox, targetPointCoordinates, imageWidth, imageHeight);
  }

  // public Point getSourcePoint(final WarpFilter filter,
  // final BoundingBox boundingBox) {
  // if (filter == null) {
  // return null;
  // } else {
  // final Point sourcePixel = getSourcePixel();
  // final Point sourcePoint = filter.sourcePixelToTargetPoint(boundingBox,
  // sourcePixel);
  // final GeometryFactory geometryFactory = filter.getGeometryFactory();
  // return geometryFactory.point(sourcePoint);
  // }
  // }

  public Point getTargetPoint() {
    return this.targetPoint;
  }

  public double getTargetPointX() {
    return this.targetPoint.getX();
  }

  public double getTargetPointY() {
    return this.targetPoint.getY();
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory.convertAxisCount(2);
    this.targetPoint = this.targetPoint.convertGeometry(this.geometryFactory);
  }

  public void setSourcePixel(final Point sourcePixel) {
    final Object oldValue = this.sourcePixel;
    this.sourcePixel = sourcePixel;
    firePropertyChange("sourcePixel", oldValue, sourcePixel);
  }

  public void setTargetPoint(final Point targetPoint) {
    final Object oldValue = this.targetPoint;
    this.targetPoint = targetPoint;
    firePropertyChange("targetPoint", oldValue, targetPoint);
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = new JsonObjectHash();
    map.put("sourceX", this.sourcePixel.getX());
    map.put("sourceY", this.sourcePixel.getY());
    map.put("target", this.targetPoint.toEwkt());
    return map;
  }

  @Override
  public String toString() {
    return this.sourcePixel + "->" + this.targetPoint;
  }
}
