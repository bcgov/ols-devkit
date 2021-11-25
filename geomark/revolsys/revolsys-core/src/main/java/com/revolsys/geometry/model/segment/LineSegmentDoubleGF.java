package com.revolsys.geometry.model.segment;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleGf;

public class LineSegmentDoubleGF extends LineSegmentDouble {
  private static final long serialVersionUID = 3905321662159212931L;

  private GeometryFactory geometryFactory;

  public LineSegmentDoubleGF() {
  }

  public LineSegmentDoubleGF(final GeometryFactory geometryFactory) {
    super(geometryFactory.getAxisCount());
    setGeometryFactory(geometryFactory);
  }

  public LineSegmentDoubleGF(final GeometryFactory geometryFactory, final int axisCount,
    final double... coordinates) {
    super(geometryFactory, axisCount, coordinates);
    setGeometryFactory(geometryFactory);
  }

  public LineSegmentDoubleGF(final GeometryFactory geometryFactory, final LineString line) {
    super(geometryFactory, line);
    setGeometryFactory(geometryFactory);
  }

  public LineSegmentDoubleGF(final GeometryFactory geometryFactory, final Point point1,
    final Point point2) {
    super(geometryFactory, point1, point2);
    setGeometryFactory(geometryFactory);
  }

  public LineSegmentDoubleGF(final int axisCount, final double... coordinates) {
    this(null, axisCount, coordinates);
  }

  public LineSegmentDoubleGF(final LineSegment line) {
    this(null, line);
    setGeometryFactory(null);
  }

  public LineSegmentDoubleGF(final LineString line) {
    this(null, line);
  }

  public LineSegmentDoubleGF(final Point coordinates1, final Point coordinates2) {
    this(coordinates1.getGeometryFactory(), coordinates1, coordinates2);
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public LineSegment newLineSegment(final int axisCount, final double... coordinates) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return new LineSegmentDoubleGF(geometryFactory, axisCount, coordinates);
  }

  @Override
  public Point newPoint(final double... coordinates) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return new PointDoubleGf(geometryFactory, coordinates);
  }

  private void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      this.geometryFactory = GeometryFactory.DEFAULT_3D;
    } else {
      this.geometryFactory = geometryFactory;
    }
  }

}
