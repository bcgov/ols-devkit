/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.geometry.util;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.util.AffineTransformation;

/**
 * Computes various kinds of common geometric shapes.
 * Provides various ways of specifying the location and extent
 * and rotations of the generated shapes,
 * as well as number of line segments used to form them.
 * <p>
 * <b>Example of usage:</b>
 * <pre>
 *  GeometricShapeFactory gsf = new GeometricShapeFactory();
 *  gsf.setSize(100);
 *  gsf.setNumPoints(100);
 *  gsf.setBase(new PointDoubleXY(100.0, 100.0));
 *  gsf.setRotation(0.5);
 *  Polygon rect = gsf.createRectangle();
 * </pre>
 *
 * @version 1.7
 */
public class GeometricShapeFactory {
  protected class Dimensions {
    public Point base;

    public Point centre;

    public double height;

    public double width;

    public Point getBase() {
      return this.base;
    }

    public Point getCentre() {
      if (this.centre == null) {
        this.centre = new PointDoubleXY(this.base.getX() + this.width / 2,
          this.base.getY() + this.height / 2);
      }
      return this.centre;
    }

    public BoundingBox getEnvelope() {
      if (this.base != null) {
        return BoundingBoxDoubleXY.newBoundingBoxDoubleXY(this.base.getX(), this.base.getY(),
          this.base.getX() + this.width, this.base.getY() + this.height);
      }
      if (this.centre != null) {
        return BoundingBoxDoubleXY.newBoundingBoxDoubleXY(this.centre.getX() - this.width / 2,
          this.centre.getY() - this.height / 2, this.centre.getX() + this.width / 2,
          this.centre.getY() + this.height / 2);
      }
      return BoundingBoxDoubleXY.newBoundingBoxDoubleXY(0, 0, this.width, this.height);
    }

    public double getHeight() {
      return this.height;
    }

    public double getMinSize() {
      return Math.min(this.width, this.height);
    }

    public double getWidth() {
      return this.width;
    }

    public void setBase(final Point base) {
      this.base = base;
    }

    public void setCentre(final Point centre) {
      this.centre = centre;
    }

    public void setEnvelope(final BoundingBox env) {
      this.width = env.getWidth();
      this.height = env.getHeight();
      this.base = new PointDoubleXY(env.getMinX(), env.getMinY());
      this.centre = env.getCentre().newPoint2D();
    }

    public void setHeight(final double height) {
      this.height = height;
    }

    public void setSize(final double size) {
      this.height = size;
      this.width = size;
    }

    public void setWidth(final double width) {
      this.width = width;
    }

  }

  protected Dimensions dim = new Dimensions();

  protected GeometryFactory geomFact;

  protected int vertexCount = 100;

  /**
   * Default is no rotation.
   */
  protected double rotationAngle = 0.0;

  /**
   * Construct a new shape factory which will create shapes using the default
   * {@link GeometryFactory}.
   */
  public GeometricShapeFactory() {
    this(GeometryFactory.DEFAULT_3D);
  }

  /**
   * Construct a new shape factory which will create shapes using the given
   * {@link GeometryFactory}.
   *
   * @param geomFact the factory to use
   */
  public GeometricShapeFactory(final GeometryFactory geomFact) {
    this.geomFact = geomFact;
  }

  protected Point coordTrans(final double x, final double y, final Point trans) {
    return newPoint(x + trans.getX(), y + trans.getY());
  }

  /**
   * Creates an elliptical arc, as a {@link LineString}.
   * The arc is always created in a counter-clockwise direction.
   * This can easily be reversed if required by using
   * {#link LineString.reverse()}
   *
   * @param startAng start angle in radians
   * @param angExtent size of angle in radians
   * @return an elliptical arc
   */
  public LineString newArc(final double startAng, final double angExtent) {
    final BoundingBox env = this.dim.getEnvelope();
    final double xRadius = env.getWidth() / 2.0;
    final double yRadius = env.getHeight() / 2.0;

    final double centreX = env.getMinX() + xRadius;
    final double centreY = env.getMinY() + yRadius;

    double angSize = angExtent;
    if (angSize <= 0.0 || angSize > 2 * Math.PI) {
      angSize = 2 * Math.PI;
    }
    final double angInc = angSize / (this.vertexCount - 1);

    final Point[] pts = new Point[this.vertexCount];
    int iPt = 0;
    for (int i = 0; i < this.vertexCount; i++) {
      final double ang = startAng + i * angInc;
      final double x = xRadius * Math.cos(ang) + centreX;
      final double y = yRadius * Math.sin(ang) + centreY;
      pts[iPt++] = newPoint(x, y);
    }
    final LineString line = this.geomFact.lineString(pts);
    return (LineString)rotate(line);
  }

  /**
   * Creates an elliptical arc polygon.
   * The polygon is formed from the specified arc of an ellipse
   * and the two radii connecting the endpoints to the centre of the ellipse.
   *
   * @param startAng start angle in radians
   * @param angExtent size of angle in radians
   * @return an elliptical arc polygon
   */
  public Polygon newArcPolygon(final double startAng, final double angExtent) {
    final BoundingBox env = this.dim.getEnvelope();
    final double xRadius = env.getWidth() / 2.0;
    final double yRadius = env.getHeight() / 2.0;

    final double centreX = env.getMinX() + xRadius;
    final double centreY = env.getMinY() + yRadius;

    double angSize = angExtent;
    if (angSize <= 0.0 || angSize > 2 * Math.PI) {
      angSize = 2 * Math.PI;
    }
    final double angInc = angSize / (this.vertexCount - 1);
    // double check = angInc * vertexCount;
    // double checkEndAng = startAng + check;

    final Point[] pts = new Point[this.vertexCount + 2];

    int iPt = 0;
    pts[iPt++] = newPoint(centreX, centreY);
    for (int i = 0; i < this.vertexCount; i++) {
      final double ang = startAng + angInc * i;

      final double x = xRadius * Math.cos(ang) + centreX;
      final double y = yRadius * Math.sin(ang) + centreY;
      pts[iPt++] = newPoint(x, y);
    }
    pts[iPt++] = newPoint(centreX, centreY);
    final LinearRing ring = this.geomFact.linearRing(pts);
    final Polygon poly = this.geomFact.polygon(ring);
    return (Polygon)rotate(poly);
  }

  // * @deprecated use {@link createEllipse} instead
  /**
   * Creates a circular or elliptical {@link Polygon}.
   *
   * @return a circle or ellipse
   */
  public Polygon newCircle() {
    return newEllipse();
  }

  /**
   * Creates an elliptical {@link Polygon}.
   * If the supplied envelope is square the
   * result will be a circle.
   *
   * @return an ellipse or circle
   */
  public Polygon newEllipse() {

    final BoundingBox env = this.dim.getEnvelope();
    final double xRadius = env.getWidth() / 2.0;
    final double yRadius = env.getHeight() / 2.0;

    final double centreX = env.getMinX() + xRadius;
    final double centreY = env.getMinY() + yRadius;

    final double[] coordinates = new double[(this.vertexCount + 1) * 2];
    int coordinateIndex = 0;
    for (int i = 0; i < this.vertexCount; i++) {
      final double ang = i * (2 * Math.PI / this.vertexCount);
      final double x = xRadius * Math.cos(ang) + centreX;
      final double y = yRadius * Math.sin(ang) + centreY;
      coordinates[coordinateIndex++] = x;
      coordinates[coordinateIndex++] = y;
    }
    coordinates[coordinateIndex++] = 0;
    coordinates[coordinateIndex++] = 1;

    final Polygon poly = this.geomFact.polygon(2, coordinates);
    return (Polygon)rotate(poly);
  }

  protected Point newPoint(final double x, final double y) {
    return new PointDoubleXY(this.geomFact, x, y);
  }

  /**
   * Creates a rectangular {@link Polygon}.
   *
   * @return a rectangular Polygon
   *
   */
  public Polygon newRectangle() {
    int i;
    int ipt = 0;
    int nSide = this.vertexCount / 4;
    if (nSide < 1) {
      nSide = 1;
    }
    final double XsegLen = this.dim.getEnvelope().getWidth() / nSide;
    final double YsegLen = this.dim.getEnvelope().getHeight() / nSide;

    final Point[] pts = new Point[4 * nSide + 1];
    final BoundingBox env = this.dim.getEnvelope();

    // double maxx = env.getMinX() + nSide * XsegLen;
    // double maxy = env.getMinY() + nSide * XsegLen;

    for (i = 0; i < nSide; i++) {
      final double x = env.getMinX() + i * XsegLen;
      final double y = env.getMinY();
      pts[ipt++] = newPoint(x, y);
    }
    for (i = 0; i < nSide; i++) {
      final double x = env.getMaxX();
      final double y = env.getMinY() + i * YsegLen;
      pts[ipt++] = newPoint(x, y);
    }
    for (i = 0; i < nSide; i++) {
      final double x = env.getMaxX() - i * XsegLen;
      final double y = env.getMaxY();
      pts[ipt++] = newPoint(x, y);
    }
    for (i = 0; i < nSide; i++) {
      final double x = env.getMinX();
      final double y = env.getMaxY() - i * YsegLen;
      pts[ipt++] = newPoint(x, y);
    }
    pts[ipt++] = pts[0];

    final LinearRing ring = this.geomFact.linearRing(pts);
    final Polygon poly = this.geomFact.polygon(ring);
    return (Polygon)rotate(poly);
  }

  /**
   * Creates a squircular {@link Polygon}.
   *
   * @return a squircle
   */
  public Polygon newSquircle()
  /**
   * Creates a squircular {@link Polygon}.
   *
   * @return a squircle
   */
  {
    return newSupercircle(4);
  }

  /**
   * Creates a supercircular {@link Polygon}
   * of a given positive power.
   *
   * @return a supercircle
   */
  public Polygon newSupercircle(final double power) {
    final double recipPow = 1.0 / power;

    final double radius = this.dim.getMinSize() / 2;
    final Point centre = this.dim.getCentre();

    final double r4 = Math.pow(radius, power);
    final double y0 = radius;

    final double xyInt = Math.pow(r4 / 2, recipPow);

    final int nSegsInOct = this.vertexCount / 8;
    final int totPts = nSegsInOct * 8 + 1;
    final Point[] pts = new Point[totPts];
    final double xInc = xyInt / nSegsInOct;

    for (int i = 0; i <= nSegsInOct; i++) {
      double x = 0.0;
      double y = y0;
      if (i != 0) {
        x = xInc * i;
        final double x4 = Math.pow(x, power);
        y = Math.pow(r4 - x4, recipPow);
      }
      pts[i] = coordTrans(x, y, centre);
      pts[2 * nSegsInOct - i] = coordTrans(y, x, centre);

      pts[2 * nSegsInOct + i] = coordTrans(y, -x, centre);
      pts[4 * nSegsInOct - i] = coordTrans(x, -y, centre);

      pts[4 * nSegsInOct + i] = coordTrans(-x, -y, centre);
      pts[6 * nSegsInOct - i] = coordTrans(-y, -x, centre);

      pts[6 * nSegsInOct + i] = coordTrans(-y, x, centre);
      pts[8 * nSegsInOct - i] = coordTrans(-x, y, centre);
    }
    pts[pts.length - 1] = pts[0];

    final LinearRing ring = this.geomFact.linearRing(pts);
    final Polygon poly = this.geomFact.polygon(ring);
    return (Polygon)rotate(poly);
  }

  protected Geometry rotate(final Geometry geom) {
    if (this.rotationAngle != 0.0) {
      final AffineTransformation trans = AffineTransformation.rotationInstance(this.rotationAngle,
        this.dim.getCentre().getX(), this.dim.getCentre().getY());
      trans.transform(geom);
    }
    return geom;
  }

  /**
   * Sets the location of the shape by specifying the base coordinate
   * (which in most cases is the
   * lower left point of the envelope containing the shape).
   *
   * @param base the base coordinate of the shape
   */
  public void setBase(final Point base) {
    this.dim.setBase(base);
  }

  /**
   * Sets the location of the shape by specifying the centre of
   * the shape's bounding box
   *
   * @param centre the centre coordinate of the shape
   */
  public void setCentre(final Point centre) {
    this.dim.setCentre(centre);
  }

  public void setEnvelope(final BoundingBox env) {
    this.dim.setEnvelope(env);
  }

  /**
   * Sets the height of the shape.
   *
   * @param height the height of the shape
   */
  public void setHeight(final double height) {
    this.dim.setHeight(height);
  }

  /**
   * Sets the total number of points in the created {@link Geometry}.
   * The created geometry will have no more than this number of points,
   * unless more are needed to Construct a new valid geometry.
   */
  public void setNumPoints(final int nPts) {
    this.vertexCount = nPts;
  }

  /**
   * Sets the rotation angle to use for the shape.
   * The rotation is applied relative to the centre of the shape.
   *
   * @param radians the rotation angle in radians.
   */
  public void setRotation(final double radians) {
    this.rotationAngle = radians;
  }

  /**
   * Sets the size of the extent of the shape in both x and y directions.
   *
   * @param size the size of the shape's extent
   */
  public void setSize(final double size) {
    this.dim.setSize(size);
  }

  /**
   * Sets the width of the shape.
   *
   * @param width the width of the shape
   */
  public void setWidth(final double width) {
    this.dim.setWidth(width);
  }
}
