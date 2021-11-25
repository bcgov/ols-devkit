package com.revolsys.geometry.model;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.function.Consumer3;
import org.jeometry.common.logging.Logs;
import org.jeometry.common.number.Doubles;
import org.jeometry.coordinatesystem.model.CoordinateSystem;

import com.revolsys.geometry.model.editor.BoundingBoxEditor;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.geometry.model.impl.LineStringDouble;
import com.revolsys.geometry.model.impl.PointDoubleGf;
import com.revolsys.geometry.model.impl.RectangleXY;
import com.revolsys.geometry.util.OutCode;
import com.revolsys.geometry.util.Points;
import com.revolsys.record.io.format.wkt.WktParser;
import com.revolsys.util.Emptyable;
import com.revolsys.util.Property;
import com.revolsys.util.QuantityType;

import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.Units;

public interface BoundingBox
  extends BoundingBoxProxy, Emptyable, GeometryFactoryProxy, Cloneable, Serializable {
  static BoundingBoxEditor bboxEditor(final BoundingBoxProxy... boundingBoxes) {
    return new BoundingBoxEditor() //
      .addAllBbox(boundingBoxes);
  }

  static BoundingBoxEditor bboxEditor(final GeometryFactoryProxy geometryFactory,
    final BoundingBoxProxy... boundingBoxes) {
    return new BoundingBoxEditor(geometryFactory) //
      .addAllBbox(boundingBoxes);
  }

  static BoundingBoxEditor bboxEditor(final GeometryFactoryProxy geometryFactory,
    final Iterable<? extends BoundingBoxProxy> boundingBoxes) {
    return new BoundingBoxEditor(geometryFactory) //
      .addAllBbox(boundingBoxes);
  }

  static BoundingBoxEditor bboxEditor(final Iterable<? extends BoundingBoxProxy> boundingBoxes) {
    return new BoundingBoxEditor() //
      .addAllBbox(boundingBoxes);
  }

  static BoundingBox bboxGet(final Object value) {
    if (value == null) {
      return empty();
    } else if (value instanceof BoundingBoxProxy) {
      return ((BoundingBoxProxy)value).getBoundingBox();
    } else {
      final String string = DataTypes.toString(value);
      return BoundingBox.bboxNew(string);
    }
  }

  static BoundingBox bboxNew(final BoundingBoxProxy... boundingBoxes) {
    return bboxEditor(boundingBoxes) //
      .newBoundingBox();
  }

  static BoundingBox bboxNew(final GeometryFactoryProxy geometryFactory,
    final BoundingBoxProxy... boundingBoxes) {
    return new BoundingBoxEditor(geometryFactory) //
      .addAllBbox(boundingBoxes) //
      .newBoundingBox();
  }

  static BoundingBox bboxNew(final GeometryFactoryProxy geometryFactory,
    final Iterable<? extends BoundingBoxProxy> boundingBoxes) {
    return new BoundingBoxEditor(geometryFactory) //
      .addAllBbox(boundingBoxes) //
      .newBoundingBox();
  }

  static BoundingBox bboxNew(final Iterable<? extends BoundingBoxProxy> boundingBoxes) {
    return new BoundingBoxEditor() //
      .addAllBbox(boundingBoxes) //
      .newBoundingBox();
  }

  static BoundingBox bboxNew(final String wkt) {
    if (Property.hasValue(wkt)) {
      try {
        int coordinateSystemId = 0;
        final PushbackReader reader = new PushbackReader(new StringReader(wkt), 20);
        WktParser.skipWhitespace(reader);
        if (WktParser.hasText(reader, "SRID=")) {
          final Integer srid = WktParser.parseInteger(reader);
          if (srid != null) {
            coordinateSystemId = srid;
          }
          WktParser.hasChar(reader, ';');
        }
        if (WktParser.hasText(reader, "BBOX")) {
          int axisCount = 2;
          if (WktParser.hasSpace(reader)) {
            if (WktParser.hasChar(reader, 'Z')) {
              axisCount = 3;
            } else if (WktParser.hasChar(reader, 'M')) {
              axisCount = 4;
            } else {
              reader.unread(' ');
            }
          }
          final GeometryFactory geometryFactory = GeometryFactory.floating(coordinateSystemId,
            axisCount);

          if (WktParser.hasText(reader, "(")) {
            final double[] bounds = new double[axisCount * 2];
            for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
              if (axisIndex > 0) {
                WktParser.skipWhitespace(reader);
                WktParser.hasChar(reader, ','); // Remove later old format BBOX
              }
              bounds[axisIndex] = WktParser.parseDouble(reader);
            }
            if (WktParser.hasSpace(reader) || WktParser.hasChar(reader, ',')) {
              for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
                if (axisIndex > 0) {
                  WktParser.skipWhitespace(reader);
                  WktParser.hasChar(reader, ','); // Remove later old format
                                                  // BBOX
                }
                bounds[axisCount + axisIndex] = WktParser.parseDouble(reader);
              }
              if (WktParser.hasChar(reader, ')')) {
                return geometryFactory.newBoundingBox(axisCount, bounds);
              }
            }
          } else if (WktParser.hasText(reader, " EMPTY")) {
            return geometryFactory.bboxEmpty();
          }
        }
      } catch (final IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid BBOX " + wkt, e);
      } catch (final IOException e) {
        throw Exceptions.wrap("Error reading WKT:" + wkt, e);
      }
      throw new IllegalArgumentException("Invalid BBOX " + wkt);
    } else {
      return empty();
    }
  }

  static BoundingBox bboxNewDelta(final double x, final double y, final double delta) {
    return new BoundingBoxDoubleXY(x - delta, y - delta, x + delta, y + delta);
  }

  static String bboxToWkt(final double minX, final double minY, final double maxX,
    final double maxY) {
    if (minX > maxX) {
      return "BBOX EMPTY";
    } else {
      final StringBuilder s = new StringBuilder();
      s.append("BBOX(");
      s.append(Doubles.toString(minX));
      s.append(' ');
      s.append(Doubles.toString(minY));
      s.append(',');
      s.append(Doubles.toString(maxX));
      s.append(' ');
      s.append(Doubles.toString(maxY));
      s.append(')');
      return s.toString();
    }
  }

  static BoundingBox empty() {
    return GeometryFactory.DEFAULT_3D.bboxEmpty();
  }

  static int hashCode(final BoundingBox boundingBox) {
    if (boundingBox.isEmpty()) {
      return 0;
    } else {
      final double minX = boundingBox.getMinX();
      final double minY = boundingBox.getMinY();
      final double maxX = boundingBox.getMaxX();
      final double maxY = boundingBox.getMaxY();
      long bits = 17;
      bits ^= java.lang.Double.doubleToLongBits(minX) * 37;
      bits ^= java.lang.Double.doubleToLongBits(minY) * 37;
      if (minX != maxX) {
        bits ^= java.lang.Double.doubleToLongBits(maxX) * 37;
      }
      if (minY != maxY) {
        bits ^= java.lang.Double.doubleToLongBits(maxY) * 37;
      }
      return (int)bits ^ (int)(bits >> 32);
    }
  }

  static boolean isEmpty(final double minX, final double maxX) {
    if (Double.isNaN(minX)) {
      return true;
    } else if (Double.isNaN(maxX)) {
      return true;
    } else {
      return maxX < minX;
    }
  }

  static <V> List<V> newArray(final BiConsumer<BoundingBoxProxy, Consumer<V>> forEachFunction,
    final BoundingBoxProxy boundingBoxProxy) {
    final List<V> values = new ArrayList<>();
    if (boundingBoxProxy != null) {
      final BoundingBox boundingBox = boundingBoxProxy.getBoundingBox();
      forEachFunction.accept(boundingBox, values::add);
    }
    return values;
  }

  static <V> List<V> newArray(
    final Consumer3<BoundingBoxProxy, Predicate<? super V>, Consumer<V>> forEachFunction,
    final BoundingBoxProxy boundingBoxProxy, final Predicate<? super V> filter) {
    final List<V> values = new ArrayList<>();
    if (boundingBoxProxy != null) {
      final BoundingBox boundingBox = boundingBoxProxy.getBoundingBox();
      forEachFunction.accept(boundingBox, filter, values::add);
    }
    return values;
  }

  static <V> List<V> newArraySorted(final BiConsumer<BoundingBoxProxy, Consumer<V>> forEachFunction,
    final BoundingBoxProxy boundingBoxProxy) {
    return newArraySorted(forEachFunction, boundingBoxProxy, null);
  }

  static <V> List<V> newArraySorted(final BiConsumer<BoundingBoxProxy, Consumer<V>> forEachFunction,
    final BoundingBoxProxy boundingBoxProxy, final Comparator<V> comparator) {
    final List<V> values = newArray(forEachFunction, boundingBoxProxy);
    values.sort(comparator);
    return values;
  }

  static <V> List<V> newArraySorted(
    final Consumer3<BoundingBoxProxy, Predicate<? super V>, Consumer<V>> forEachFunction,
    final BoundingBoxProxy boundingBoxProxy, final Predicate<? super V> filter) {
    return newArraySorted(forEachFunction, boundingBoxProxy, filter, null);
  }

  static <V> List<V> newArraySorted(
    final Consumer3<BoundingBoxProxy, Predicate<? super V>, Consumer<V>> forEachFunction,
    final BoundingBoxProxy boundingBoxProxy, final Predicate<? super V> filter,
    final Comparator<V> comparator) {
    final List<V> values = newArray(forEachFunction, boundingBoxProxy, filter);
    values.sort(comparator);
    return values;
  }

  static String toString(final BoundingBox boundingBox) {
    final StringBuilder s = new StringBuilder();
    final int srid = boundingBox.getHorizontalCoordinateSystemId();
    if (srid > 0) {
      s.append("SRID=");
      s.append(srid);
      s.append(";");
    }
    if (boundingBox.isEmpty()) {
      s.append("BBOX EMPTY");
    } else {
      s.append("BBOX");
      final int axisCount = boundingBox.getAxisCount();
      if (axisCount == 3) {
        s.append(" Z");
      } else if (axisCount == 4) {
        s.append(" ZM");
      } else if (axisCount != 2) {
        s.append(" ");
        s.append(axisCount);
      }
      s.append("(");
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        if (axisIndex > 0) {
          s.append(' ');
        }
        s.append(Doubles.toString(boundingBox.getMin(axisIndex)));
      }
      s.append(',');
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        if (axisIndex > 0) {
          s.append(' ');
        }
        s.append(Doubles.toString(boundingBox.getMax(axisIndex)));
      }
      s.append(')');
    }
    return s.toString();
  }

  default boolean bboxCoveredBy(final double minX, final double minY, final double maxX,
    final double maxY) {
    final double minX2 = getMinX();
    final double minY2 = getMinY();
    final double maxX2 = getMaxX();
    final double maxY2 = getMaxY();
    return minX <= minX2 && maxX2 <= maxX && minY <= minY2 && maxY2 <= maxY;
  }

  /**
   * Tests if the given point lies in or on the envelope.
   *
   *@param  x  the x-coordinate of the point which this <code>BoundingBox</code> is
   *      being checked for containing
   *@param  y  the y-coordinate of the point which this <code>BoundingBox</code> is
   *      being checked for containing
   *@return    <code>true</code> if <code>(x, y)</code> lies in the interior or
   *      on the boundary of this <code>BoundingBox</code>.
   */
  default boolean bboxCovers(final double x, final double y) {
    if (isEmpty()) {
      return false;
    } else {
      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();

      return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }
  }

  /**
   * Tests if the <code>BoundingBox other</code>
   * lies wholely inside this <code>BoundingBox</code> (inclusive of the boundary).
   *
   *@param  other the <code>BoundingBox</code> to check
   *@return true if this <code>BoundingBox</code> covers the <code>other</code>
   */
  default boolean bboxCovers(final double minX, final double minY, final double maxX,
    final double maxY) {
    final double minX2 = getMinX();
    final double minY2 = getMinY();
    final double maxX2 = getMaxX();
    final double maxY2 = getMaxY();
    return minX2 <= minX && maxX <= maxX2 && minY2 <= minY && maxY <= maxY2;
  }

  @Override
  default boolean bboxCovers(final Point point) {
    if (point == null || point.isEmpty()) {
      return false;
    } else if (isSameCoordinateSystem(point)) {
      final double x = point.getX();
      final double y = point.getY();
      return bboxCovers(x, y);
    } else {
      return BoundingBoxProxy.super.bboxCovers(point);
    }
  }

  default double bboxDistance(final double x, final double y) {
    if (bboxIntersects(x, y)) {
      return 0;
    } else {

      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();

      double dx = 0.0;
      if (maxX < x) {
        dx = x - maxX;
      } else if (minX > x) {
        dx = minX - x;
      }

      double dy = 0.0;
      if (maxY < y) {
        dy = y - maxY;
      } else if (minY > y) {
        dy = minY - y;
      }

      // if either is zero, the envelopes overlap either vertically or
      // horizontally
      if (dx == 0.0) {
        return dy;
      }
      if (dy == 0.0) {
        return dx;
      }
      return Math.sqrt(dx * dx + dy * dy);
    }
  }

  /**
   * Computes the distance between this and another
   * <code>BoundingBox</code>.
   * The distance between overlapping Envelopes is 0.  Otherwise, the
   * distance is the Euclidean distance between the closest points.
   */
  @Override
  default double bboxDistance(final double minX, final double minY, final double maxX,
    final double maxY) {
    final double minXThis = getMinX();
    final double minYThis = getMinY();
    final double maxXThis = getMaxX();
    final double maxYThis = getMaxY();

    if (isEmpty(minXThis, maxXThis) || isEmpty(minX, maxX)) {
      // Empty
      return Double.MAX_VALUE;
    } else if (!(minX > maxXThis || maxX < minXThis || minY > maxYThis || maxY < minYThis)) {
      // Intersects
      return 0;
    } else {
      double dx;
      if (maxXThis < minX) {
        dx = minX - maxXThis;
      } else {
        if (minXThis > maxX) {
          dx = minXThis - maxX;
        } else {
          dx = 0;
        }
      }

      double dy;
      if (maxYThis < minY) {
        dy = minY - maxYThis;
      } else if (minYThis > maxY) {
        dy = minYThis - maxY;
      } else {
        dy = 0;
      }

      if (dx == 0.0) {
        return dy;
      } else if (dy == 0.0) {
        return dx;
      } else {
        return Math.sqrt(dx * dx + dy * dy);
      }
    }
  }

  default boolean bboxEquals(final BoundingBox boundingBox) {
    if (boundingBox == null || boundingBox.isEmpty()) {
      return false;
    } else if (isSameCoordinateSystem(boundingBox)) {
      if (getMaxX() == boundingBox.getMaxX()) {
        if (getMaxY() == boundingBox.getMaxY()) {
          if (getMinX() == boundingBox.getMinX()) {
            if (getMinY() == boundingBox.getMinY()) {
              return true;
            }
          }
        }
      }
      return false;
    } else {
      final BiFunction<BoundingBox, BoundingBox, Boolean> action = BoundingBox::equals;
      return bboxWith(boundingBox, action, false);
    }
  }

  @Override
  default boolean bboxEquals(final BoundingBoxProxy boundingBox) {
    if (boundingBox == null) {
      return false;
    } else {
      final BoundingBox bbox = boundingBox.getBoundingBox();
      return bboxEquals(bbox);
    }
  }

  /**
   * Computes the intersection of two {@link BoundingBox}s.
   *
   * @param env the envelope to intersect with
   * @return a new BoundingBox representing the intersection of the envelopes (this will be
   * the null envelope if either argument is null, or they do not intersect
   */
  @Override
  default BoundingBox bboxIntersection(final BoundingBoxProxy boundingBox) {
    final BoundingBoxFunction<BoundingBox> action = BoundingBox::bboxIntersection;
    final GeometryFactory geometryFactory = getGeometryFactory();
    final BoundingBox empty = geometryFactory.bboxEmpty();
    return bboxWith(boundingBox, action, empty);
  }

  /**
   * Computes the intersection of this and another bounding box.
   *
   * @return The intersection.
   */
  default BoundingBox bboxIntersection(final double minX, final double minY, final double maxX,
    final double maxY) {
    if (isEmpty()) {
      return this;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      if (bboxIntersects(minX, minY, maxX, maxY)) {
        final double intMinX = Math.max(getMinX(), minX);
        final double intMinY = Math.max(getMinY(), minY);
        final double intMaxX = Math.min(getMaxX(), maxX);
        final double intMaxY = Math.min(getMaxY(), maxY);
        return geometryFactory.newBoundingBox(intMinX, intMinY, intMaxX, intMaxY);
      } else {
        return geometryFactory.bboxEmpty();
      }
    }
  }

  default boolean bboxIntersects(final BoundingBox boundingBox) {
    if (boundingBox == null || boundingBox.isEmpty()) {
      return false;
    } else if (isSameCoordinateSystem(boundingBox)) {
      final double minX = boundingBox.getMinX();
      final double minY = boundingBox.getMinY();
      final double maxX = boundingBox.getMaxX();
      final double maxY = boundingBox.getMaxY();
      return bboxIntersects(minX, minY, maxX, maxY);
    } else {
      final BoundingBoxFunction<Boolean> action = BoundingBox::bboxIntersects;
      return bboxWith(boundingBox, action, false);
    }
  }

  @Override
  default boolean bboxIntersects(final BoundingBoxProxy boundingBox) {
    if (boundingBox == null) {
      return false;
    } else {
      final BoundingBox bbox = boundingBox.getBoundingBox();
      return bboxIntersects(bbox);
    }
  }

  /**
   *  Check if the point <code>(x, y)</code>
   *  overlaps (lies inside) the region of this <code>BoundingBox</code>.
   *
   *@param  x  the x-ordinate of the point
   *@param  y  the y-ordinate of the point
   *@return        <code>true</code> if the point overlaps this <code>BoundingBox</code>
   */
  default boolean bboxIntersects(final double x, final double y) {
    if (isEmpty()) {
      return false;
    } else {
      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();
      return !(x > maxX || x < minX || y > maxY || y < minY);
    }
  }

  @Override
  default boolean bboxIntersects(double x1, double y1, double x2, double y2) {
    final double minX = getMinX();
    final double minY = getMinY();
    final double maxX = getMaxX();
    final double maxY = getMaxY();
    if (x1 > x2) {
      final double t = x1;
      x1 = x2;
      x2 = t;
    }
    if (y1 > y2) {
      final double t = y1;
      y1 = y2;
      y2 = t;
    }
    return !(x1 > maxX || x2 < minX || y1 > maxY || y2 < minY);
  }

  @Override
  default BoundingBox bboxToCs(final GeometryFactoryProxy geometryFactory) {
    if (geometryFactory == null || isEmpty()) {
      return this;
    } else if (isHasHorizontalCoordinateSystem()) {
      if (!isProjectionRequired(geometryFactory)) {
        return this;
      }
    } else if (!geometryFactory.isHasHorizontalCoordinateSystem()) {
      return this;
    }
    return bboxEditor() //
      .setGeometryFactory(geometryFactory) //
      .newBoundingBox();
  }

  default String bboxToEWkt() {
    final StringBuilder s = new StringBuilder();
    final int srid = getHorizontalCoordinateSystemId();
    if (srid > 0) {
      s.append("SRID=");
      s.append(srid);
      s.append(";");
    }
    if (isEmpty()) {
      s.append("BBOX EMPTY");
    } else {
      s.append("BBOX");
      final int axisCount = getAxisCount();
      if (axisCount == 3) {
        s.append(" Z");
      } else if (axisCount == 4) {
        s.append(" ZM");
      } else if (axisCount != 2) {
        s.append(" ");
        s.append(axisCount);
      }
      s.append("(");
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        if (axisIndex > 0) {
          s.append(' ');
        }
        s.append(Doubles.toString(getMin(axisIndex)));
      }
      s.append(',');
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        if (axisIndex > 0) {
          s.append(' ');
        }
        s.append(Doubles.toString(getMax(axisIndex)));
      }
      s.append(')');
    }
    return s.toString();
  }

  default String bboxToWkt() {
    if (isEmpty()) {
      return "BBOX EMPTY";
    } else {
      final StringBuilder s = new StringBuilder();
      s.append("BBOX(");
      s.append(Doubles.toString(getMinX()));
      s.append(' ');
      s.append(Doubles.toString(getMinY()));
      s.append(',');
      s.append(Doubles.toString(getMaxX()));
      s.append(' ');
      s.append(Doubles.toString(getMaxY()));
      s.append(')');
      return s.toString();
    }
  }

  @Override
  default <R> R bboxWith(final BoundingBoxProxy boundingBox,
    final BiFunction<BoundingBox, BoundingBox, R> action, final R emptyValue) {
    BoundingBox boundingBox2;
    if (boundingBox == null) {
      boundingBox2 = BoundingBox.empty();
    } else {
      boundingBox2 = boundingBox.getBoundingBox();
    }
    return action.apply(this, boundingBox2);

  }

  @Override
  default <R> R bboxWith(final BoundingBoxProxy boundingBox, final BoundingBoxFunction<R> action,
    final R emptyResult) {
    if (boundingBox != null && !isEmpty()) {
      BoundingBox boundingBox2 = boundingBox.getBoundingBox();
      if (isProjectionRequired(boundingBox2)) {
        // TODO just convert points
        boundingBox2 = boundingBox2.bboxToCs(getGeometryFactory());
      }
      if (!boundingBox2.isEmpty()) {
        final double minX = boundingBox2.getMinX();
        final double minY = boundingBox2.getMinY();
        final double maxX = boundingBox2.getMaxX();
        final double maxY = boundingBox2.getMaxY();
        return action.accept(this, minX, minY, maxX, maxY);
      }

    }
    return emptyResult;
  }

  @Override
  default <R> R bboxWith(Point point, final BoundingBoxPointFunction<R> action,
    final R emptyResult) {
    if (point != null && !isEmpty()) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      point = point.as2d(geometryFactory);
      if (!point.isEmpty()) {
        final double x = point.getX();
        final double y = point.getY();
        return action.accept(this, x, y);
      }

    }
    return emptyResult;
  }

  default BoundingBox clone() {
    return this;
  }

  default boolean containsSFS(final Geometry geometry) {
    if (bboxCovers(geometry)) {
      if (geometry.isContainedInBoundary(this)) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  default double distanceFromCenter(final BoundingBox boundingBox) {
    final double x = boundingBox.getCentreX();
    final double y = boundingBox.getCentreY();
    return distanceFromCenter(x, y);
  }

  default double distanceFromCenter(final double x, final double y) {
    final double x1 = getCentreX();
    final double y1 = getCentreY();
    return Points.distance(x1, y1, x, y);
  }

  default double edgeDeltas() {
    double distance = 0;
    for (int axiIndex = 0; axiIndex < getAxisCount(); axiIndex++) {
      distance += getMax(axiIndex) - getMin(axiIndex);
    }

    return distance;
  }

  default boolean equals(final BoundingBox boundingBox) {
    if (isEmpty()) {
      return boundingBox.isEmpty();
    } else if (boundingBox.isEmpty()) {
      return false;
    } else {
      final int csId1 = getHorizontalCoordinateSystemId();
      final int csId2 = boundingBox.getHorizontalCoordinateSystemId();
      if (csId1 == csId2 || csId1 < 1 || csId2 < 1) {
        if (getMaxX() == boundingBox.getMaxX()) {
          if (getMaxY() == boundingBox.getMaxY()) {
            if (getMinX() == boundingBox.getMinX()) {
              if (getMinY() == boundingBox.getMinY()) {
                return true;
              }
            }
          }
        }

      }
    }
    return false;
  }

  /**
   * Gets the area of this envelope.
   *
   * @return the area of the envelope
   * @return 0.0 if the envelope is null
   */
  default double getArea() {
    if (getAxisCount() < 2 || isEmpty()) {
      return 0;
    } else {
      final double width = getWidth();
      final double height = getHeight();
      return width * height;
    }
  }

  /**
   * Get the aspect ratio x:y.
   *
   * @return The aspect ratio.
   */
  default double getAspectRatio() {
    final double width = getWidth();
    final double height = getHeight();
    final double aspectRatio = width / height;
    return aspectRatio;
  }

  @Override
  default int getAxisCount() {
    return 2;
  }

  default Point getBottomLeftPoint() {
    return getGeometryFactory().point(getMinX(), getMinY());
  }

  default Point getBottomRightPoint() {
    return getGeometryFactory().point(getMaxX(), getMinY());
  }

  @Override
  default BoundingBox getBoundingBox() {
    return this;
  }

  default Point getCentre() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (isEmpty()) {
      return geometryFactory.point();
    } else {
      final double centreX = getCentreX();
      final double centreY = getCentreY();
      return geometryFactory.point(centreX, centreY);
    }
  }

  default double getCentreX() {
    return (getMinX() + getMaxX()) / 2;
  }

  default double getCentreY() {
    return (getMinY() + getMaxY()) / 2;
  }

  /**
   * maxX,minY
   * minX,minY
   * minX,maxY
   * maxX,maxY
   */
  default Point getCornerPoint(int index) {
    if (isEmpty()) {
      return null;
    } else {
      final double minX = getMinX();
      final double maxX = getMaxX();
      final double minY = getMinY();
      final double maxY = getMaxY();
      index = index % 4;
      switch (index) {
        case 0:
          return new PointDoubleGf(getGeometryFactory(), maxX, minY);
        case 1:
          return new PointDoubleGf(getGeometryFactory(), minX, minY);
        case 2:
          return new PointDoubleGf(getGeometryFactory(), minX, maxY);
        default:
          return new PointDoubleGf(getGeometryFactory(), maxX, maxY);
      }
    }
  }

  default LineString getCornerPoints() {
    final double minX = getMinX();
    final double maxX = getMaxX();
    final double minY = getMinY();
    final double maxY = getMaxY();
    return new LineStringDouble(2, maxX, minY, minX, minY, minX, maxY, maxX, maxY);
  }

  /**
   *  Returns the difference between the maximum and minimum y values.
   *
   *@return    max y - min y, or 0 if this is a null <code>BoundingBox</code>
   */
  default double getHeight() {
    if (getAxisCount() < 2 || isEmpty()) {
      return 0;
    } else {
      return getMaxY() - getMinY();
    }
  }

  default Quantity<Length> getHeightLength() {
    final double height = getHeight();
    final GeometryFactory geometryFactory = getGeometryFactory();
    final Unit<Length> unit = geometryFactory.getHorizontalLengthUnit();
    return Quantities.getQuantity(height, unit);
  }

  default double getMax(final int i) {
    return Double.NEGATIVE_INFINITY;
  }

  default <Q extends Quantity<Q>> Quantity<Q> getMaximum(final int axisIndex) {
    final Unit<Q> unit = getUnit();
    final double max = this.getMax(axisIndex);
    return Quantities.getQuantity(max, unit);
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  default <Q extends Quantity<Q>> double getMaximum(final int axisIndex, final Unit convertUnit) {
    final Quantity<Q> max = getMaximum(axisIndex);
    return QuantityType.doubleValue(max, convertUnit);
  }

  /**
   *  Returns the <code>BoundingBox</code>s maximum x-value. min x > max x
   *  indicates that this is a null <code>BoundingBox</code>.
   *
   *@return    the maximum x-coordinate
   */
  default double getMaxX() {
    return getMax(0);
  }

  /**
   *  Returns the <code>BoundingBox</code>s maximum y-value. min y > max y
   *  indicates that this is a null <code>BoundingBox</code>.
   *
   *@return    the maximum y-coordinate
   */
  default double getMaxY() {
    return getMax(1);
  }

  default double getMaxZ() {
    return getMax(2);
  }

  default double getMin(final int i) {
    return Double.POSITIVE_INFINITY;
  }

  default <Q extends Quantity<Q>> Quantity<Q> getMinimum(final int axisIndex) {
    final Unit<Q> unit = getUnit();
    final double min = this.getMin(axisIndex);
    return Quantities.getQuantity(min, unit);
  }

  default <Q extends Quantity<Q>> double getMinimum(final int axisIndex,
    final Unit<Q> convertUnit) {
    final Quantity<Q> min = getMinimum(axisIndex);
    return QuantityType.doubleValue(min, convertUnit);
  }

  default double[] getMinMaxValues() {
    if (isEmpty()) {
      return null;
    } else {
      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();
      return new double[] {
        minX, minY, maxX, maxY
      };
    }
  }

  default double[] getMinMaxValues(final int axisCount) {
    if (isEmpty()) {
      return null;
    } else {
      final double[] bounds = new double[2 * axisCount];
      for (int i = 0; i < axisCount; i++) {
        bounds[i] = getMin(i);
        bounds[i + axisCount] = getMax(i);
      }
      return bounds;
    }
  }

  /**
   *  Returns the <code>BoundingBox</code>s minimum x-value. min x > max x
   *  indicates that this is a null <code>BoundingBox</code>.
   *
   *@return    the minimum x-coordinate
   */
  default double getMinX() {
    return getMin(0);
  }

  /**
   *  Returns the <code>BoundingBox</code>s minimum y-value. min y > max y
   *  indicates that this is a null <code>BoundingBox</code>.
   *
   *@return    the minimum y-coordinate
   */
  default double getMinY() {
    return getMin(1);
  }

  default double getMinZ() {
    return getMin(2);
  }

  default int getOutcode(final double x, final double y) {
    final double minX = getMinX();
    final double minY = getMinY();
    final double maxX = getMaxX();
    final double maxY = getMaxY();
    return OutCode.getOutcode(minX, minY, maxX, maxY, x, y);
  }

  default Point getRandomPointWithin() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final double x = getMinX() + getWidth() * Math.random();
    final double y = getMinY() + getHeight() * Math.random();
    return geometryFactory.point(x, y);
  }

  default Point getTopLeftPoint() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.point(getMinX(), getMaxY());
  }

  default Point getTopRightPoint() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.point(getMaxX(), getMaxY());
  }

  @SuppressWarnings("unchecked")
  default <Q extends Quantity<Q>> Unit<Q> getUnit() {
    final CoordinateSystem coordinateSystem = getHorizontalCoordinateSystem();
    if (coordinateSystem == null) {
      return (Unit<Q>)Units.METRE;
    } else {
      return coordinateSystem.<Q> getUnit();
    }
  }

  /**
   *  Returns the difference between the maximum and minimum x values.
   *
   *@return    max x - min x, or 0 if this is a null <code>BoundingBox</code>
   */
  default double getWidth() {
    if (getAxisCount() < 2 || isEmpty()) {
      return 0;
    } else {
      final double minX = getMinX();
      final double maxX = getMaxX();

      return maxX - minX;
    }
  }

  default Quantity<Length> getWidthLength() {
    final double width = getWidth();
    final GeometryFactory geometryFactory = getGeometryFactory();
    final Unit<Length> unit = geometryFactory.getHorizontalLengthUnit();
    return Quantities.getQuantity(width, unit);
  }

  default boolean intersectsLine(double x1, double y1, final double x2, final double y2) {
    final int out2 = getOutcode(x2, y2);
    if (out2 == 0) {
      return true;
    }
    int out1 = getOutcode(x1, y1);
    while (out1 != 0) {
      if ((out1 & out2) != 0) {
        return false;
      } else if ((out1 & (OutCode.OUT_LEFT | OutCode.OUT_RIGHT)) != 0) {
        double x;
        if ((out1 & OutCode.OUT_RIGHT) != 0) {
          x = getMaxX();
        } else {
          x = getMinX();
        }
        y1 = y1 + (x - x1) * (y2 - y1) / (x2 - x1);
        x1 = x;
      } else {
        double y;
        if ((out1 & OutCode.OUT_TOP) != 0) {
          y = getMaxY();
        } else {
          y = getMinY();
        }
        x1 = x1 + (y - y1) * (x2 - x1) / (y2 - y1);
        y1 = y;
      }
      out1 = getOutcode(x1, y1);
    }
    return true;
  }

  @Override
  default boolean isBboxEmpty() {
    return isEmpty();
  }

  default OutCode outcode(final double x, final double y) {
    final double minX = getMinX();
    final double minY = getMinY();
    final double maxX = getMaxX();
    final double maxY = getMaxY();
    if (x < minX) {
      if (y < minY) {
        return OutCode.LEFT_BOTTOM;
      } else if (y > maxY) {
        return OutCode.LEFT_TOP;
      } else {
        return OutCode.LEFT;
      }
    } else if (x > maxX) {
      if (y < minY) {
        return OutCode.RIGHT_BOTTOM;
      } else if (y > maxY) {
        return OutCode.RIGHT_TOP;
      } else {
        return OutCode.RIGHT;
      }

    } else {
      if (y < minY) {
        return OutCode.BOTTOM;
      } else if (y > maxY) {
        return OutCode.TOP;
      } else {
        return OutCode.INSIDE;
      }

    }
  }

  default double overlappingArea(final BoundingBox bb) {

    double area = 1.0;
    for (int axis = 0; axis < 2; axis++) {
      final double min1 = getMin(axis);
      final double max1 = getMax(axis);
      final double min2 = bb.getMin(axis);
      final double max2 = bb.getMax(axis);

      // left edge outside left edge
      if (min1 < min2) {
        if (min2 < max1) { // and right edge inside left edge

          if (max2 < max1) {// right edge outside right edge
            area *= max2 - min2;
          } else {
            area *= max1 - min2;
          }
        } else {
          return 0.0;
        }
      }

      else if (min1 < max2) { // right edge inside left edge

        if (max1 < max2) { // right edge outside right edge
          area *= max1 - min1;
        } else {
          area *= max2 - min1;
        }
      } else {
        return 0.0;
      }
    }

    return area;
  }

  /**
   * Creates a {@link Geometry} with the same extent as the given envelope.
   * The Geometry returned is guaranteed to be valid.
   * To provide this behaviour, the following cases occur:
   * <p>
   * If the <code>BoundingBox</code> is:
   * <ul>
   * <li>null : returns an empty {@link Point}
   * <li>a point : returns a non-empty {@link Point}
   * <li>a line : returns a two-point {@link LineString}
   * <li>a rectangle : returns a {@link Polygon}> whose points are (minx, miny),
   *  (minx, maxy), (maxx, maxy), (maxx, miny), (minx, miny).
   * </ul>
   *
   *@param  envelope the <code>BoundingBox</code> to convert
   *@return an empty <code>Point</code> (for null <code>BoundingBox</code>s),
   *  a <code>Point</code> (when min x = max x and min y = max y) or a
   *      <code>Polygon</code> (in all other cases)
   */

  default Geometry toGeometry() {
    GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      geometryFactory = GeometryFactory.floating2d(0);
    }
    if (isEmpty()) {
      return geometryFactory.point();
    } else {
      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();
      final double width = getWidth();
      final double height = getHeight();
      if (width == 0 && height == 0) {
        return geometryFactory.point(minX, minY);
      } else if (width == 0 || height == 0) {
        return geometryFactory.lineString(2, minX, minY, maxX, maxY);
      } else {
        return toRectangle();
      }
    }
  }

  default LinearRing toLinearRing() {
    final double minX = getMinX();
    final double minY = getMinY();
    final double maxX = getMaxX();
    final double maxY = getMaxY();
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.linearRing(2, //
      minX, minY, //
      maxX, minY, //
      maxX, maxY, //
      minX, maxY, //
      minX, minY //
    );
  }

  default LinearRing toLinearRing(GeometryFactory geometryFactory, int numX, int numY) {
    if (isEmpty()) {
      return geometryFactory.linearRing();
    } else {
      final GeometryFactory factory = getGeometryFactory();
      if (geometryFactory == null) {
        if (factory == null) {
          geometryFactory = GeometryFactory.floating2d(0);
        } else {
          geometryFactory = factory;
        }
      }
      if (numX == 0 && numY == 0) {
        return toLinearRing();
      } else {
        try {
          double minStep = 0.00001;
          if (factory.isProjected()) {
            minStep = 1;
          } else {
            minStep = 0.00001;
          }

          double xStep;
          final double width = getWidth();
          if (!Double.isFinite(width)) {
            return geometryFactory.linearRing();
          } else if (numX <= 1) {
            numX = 1;
            xStep = width;
          } else {
            xStep = width / numX;
            if (xStep < minStep) {
              xStep = minStep;
            }
            numX = Math.max(1, (int)Math.ceil(width / xStep));
          }

          double yStep;
          if (numY <= 1) {
            numY = 1;
            yStep = getHeight();
          } else {
            yStep = getHeight() / numY;
            if (yStep < minStep) {
              yStep = minStep;
            }
            numY = Math.max(1, (int)Math.ceil(getHeight() / yStep));
          }

          final double minX = getMinX();
          final double maxX = getMaxX();
          final double minY = getMinY();
          final double maxY = getMaxY();
          final int coordinateCount = 1 + 2 * (numX + numY);
          final double[] coordinates = new double[coordinateCount * 2];
          int i = 0;

          coordinates[i++] = minX;
          coordinates[i++] = minY;
          for (int j = 1; j < numX; j++) {
            final double x = minX + j * xStep;
            coordinates[i++] = x;
            coordinates[i++] = minY;
          }
          coordinates[i++] = maxX;
          coordinates[i++] = minY;

          for (int j = 1; j < numY; j++) {
            final double y = minY + j * yStep;
            coordinates[i++] = maxX;
            coordinates[i++] = y;
          }
          coordinates[i++] = maxX;
          coordinates[i++] = maxY;

          for (int j = numX - 1; j > 0; j--) {
            final double x = minX + j * xStep;
            coordinates[i++] = x;
            coordinates[i++] = maxY;
          }

          coordinates[i++] = minX;
          coordinates[i++] = maxY;

          for (int j = numY - 1; j > 0; j--) {
            final double y = minY + j * yStep;
            coordinates[i++] = minX;
            coordinates[i++] = y;
          }
          coordinates[i++] = minX;
          coordinates[i++] = minY;

          final LinearRing ring = factory.linearRing(2, coordinates);
          if (geometryFactory == null) {
            return ring;
          } else {
            return ring.as2d(geometryFactory);
          }
        } catch (final IllegalArgumentException e) {
          Logs.error(this, "Unable to convert to linearRing: " + this, e);
          return geometryFactory.linearRing();
        }
      }
    }
  }

  default Polygon toPolygon(final GeometryFactory factory) {
    return toPolygon(factory, 100, 100);
  }

  default Polygon toPolygon(final GeometryFactory factory, final int numSegments) {
    return toPolygon(factory, numSegments, numSegments);
  }

  default Polygon toPolygon(GeometryFactory geometryFactory, int numX, int numY) {
    if (isEmpty()) {
      return geometryFactory.polygon();
    } else {
      final GeometryFactory factory = getGeometryFactory();
      if (geometryFactory == null) {
        if (factory == null) {
          geometryFactory = GeometryFactory.floating2d(0);
        } else {
          geometryFactory = factory;
        }
      }
      if (numX == 0 && numY == 0) {
        return toRectangle();
      } else {
        try {
          double minStep = 0.00001;
          if (factory.isProjected()) {
            minStep = 1;
          } else {
            minStep = 0.00001;
          }

          double xStep;
          final double width = getWidth();
          if (!Double.isFinite(width)) {
            return geometryFactory.polygon();
          } else if (numX <= 1) {
            numX = 1;
            xStep = width;
          } else {
            xStep = width / numX;
            if (xStep < minStep) {
              xStep = minStep;
            }
            numX = Math.max(1, (int)Math.ceil(width / xStep));
          }

          double yStep;
          if (numY <= 1) {
            numY = 1;
            yStep = getHeight();
          } else {
            yStep = getHeight() / numY;
            if (yStep < minStep) {
              yStep = minStep;
            }
            numY = Math.max(1, (int)Math.ceil(getHeight() / yStep));
          }

          final double minX = getMinX();
          final double maxX = getMaxX();
          final double minY = getMinY();
          final double maxY = getMaxY();
          final int coordinateCount = 1 + 2 * (numX + numY);
          final double[] coordinates = new double[coordinateCount * 2];
          int i = 0;

          coordinates[i++] = minX;
          coordinates[i++] = minY;
          for (int j = 1; j < numX; j++) {
            final double x = minX + j * xStep;
            coordinates[i++] = x;
            coordinates[i++] = minY;
          }
          coordinates[i++] = maxX;
          coordinates[i++] = minY;

          for (int j = 1; j < numY; j++) {
            final double y = minY + j * yStep;
            coordinates[i++] = maxX;
            coordinates[i++] = y;
          }
          coordinates[i++] = maxX;
          coordinates[i++] = maxY;

          for (int j = numX - 1; j > 0; j--) {
            final double x = minX + j * xStep;
            coordinates[i++] = x;
            coordinates[i++] = maxY;
          }

          coordinates[i++] = minX;
          coordinates[i++] = maxY;

          for (int j = numY - 1; j > 0; j--) {
            final double y = minY + j * yStep;
            coordinates[i++] = minX;
            coordinates[i++] = y;
          }
          coordinates[i++] = minX;
          coordinates[i++] = minY;

          final LinearRing ring = factory.linearRing(2, coordinates);
          final Polygon polygon = factory.polygon(ring);
          if (geometryFactory == null) {
            return polygon;
          } else {
            return (Polygon)polygon.convertGeometry(geometryFactory);
          }
        } catch (final IllegalArgumentException e) {
          Logs.error(this, "Unable to convert to polygon: " + this, e);
          return geometryFactory.polygon();
        }
      }
    }
  }

  default Polygon toPolygon(final int numSegments) {
    return toPolygon(numSegments, numSegments);
  }

  default Polygon toPolygon(final int numX, final int numY) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return toPolygon(geometryFactory, numX, numY);
  }

  default RectangleXY toRectangle() {
    final double minX = getMinX();
    final double minY = getMinY();
    final double maxX = getMaxX();
    final double maxY = getMaxY();
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.newRectangleCorners(minX, minY, maxX, maxY);
  }
}
