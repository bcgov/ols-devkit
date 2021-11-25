package com.revolsys.geometry.model.editor;

import java.util.Arrays;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.segment.LineSegmentDoubleGF;

public class LineSegmentEditor extends LineSegmentDoubleGF {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final int axisCount;

  public LineSegmentEditor(final GeometryFactory geometryFactory) {
    super(geometryFactory);
    this.axisCount = geometryFactory.getAxisCount();
  }

  private void clampToX(final int coordinatesOffset, final double newX) {
    final int axisCount = this.axisCount;
    final double x1 = this.coordinates[0];
    final double y1 = this.coordinates[1];
    final double x2 = this.coordinates[axisCount];
    final double y2 = this.coordinates[axisCount + 1];
    final double percent = (newX - x1) / (x2 - x1);
    this.coordinates[coordinatesOffset] = newX;
    this.coordinates[coordinatesOffset + 1] = y1 + (y2 - y1) * percent;
    for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
      final double c1 = this.coordinates[axisIndex];
      final double c2 = this.coordinates[axisCount + axisIndex];
      this.coordinates[coordinatesOffset + axisIndex] = c1 + (c2 - c1) * percent;
    }
  }

  private void clampToY(final int coordinatesOffset, final double newY) {
    final int axisCount = this.axisCount;
    final double x1 = this.coordinates[0];
    final double y1 = this.coordinates[1];
    final double x2 = this.coordinates[axisCount];
    final double y2 = this.coordinates[axisCount + 1];
    final double percent = (newY - y1) / (y2 - y1);
    this.coordinates[coordinatesOffset] = x1 + (x2 - x1) * percent;
    this.coordinates[coordinatesOffset + 1] = newY;
    for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
      final double c1 = this.coordinates[axisIndex];
      final double c2 = this.coordinates[axisCount + axisIndex];
      this.coordinates[coordinatesOffset + axisIndex] = c1 + (c2 - c1) * percent;
    }
  }

  public boolean clipToBbox(final BoundingBox boundingBox) {
    final double minX = boundingBox.getMinX();
    final double minY = boundingBox.getMinY();
    final double maxX = boundingBox.getMaxX();
    final double maxY = boundingBox.getMaxY();
    final double[] coordinates = this.coordinates;
    final int axisCount = this.axisCount;
    final int x2Index = axisCount;
    final int y2Index = axisCount + 1;
    if (coordinates[0] < minX) {
      if (coordinates[x2Index] < minX) {
        Arrays.fill(coordinates, Double.NaN);
        return false;
      } else {
        clampToX(0, minX);
      }
    } else if (coordinates[x2Index] < minX) {
      clampToX(axisCount, minX);
    }
    if (coordinates[0] > maxX) {
      if (coordinates[x2Index] > maxX) {
        Arrays.fill(coordinates, Double.NaN);
        return false;
      } else {
        clampToX(0, maxX);
      }
    } else if (coordinates[x2Index] > maxX) {
      clampToX(axisCount, maxX);
    }
    if (coordinates[1] < minY) {
      if (coordinates[y2Index] < minY) {
        Arrays.fill(coordinates, Double.NaN);
        return false;
      } else {
        clampToY(0, minY);
      }
    } else if (coordinates[y2Index] < minY) {
      clampToY(axisCount, minY);
    }
    if (coordinates[1] > maxY) {
      if (coordinates[y2Index] > maxY) {
        Arrays.fill(coordinates, Double.NaN);
        return false;
      } else {
        clampToY(0, maxY);
      }
    } else if (coordinates[y2Index] > maxY) {
      clampToY(axisCount, maxY);
    }
    return true;
  }

  @Override
  public int getAxisCount() {
    return this.axisCount;
  }

  public LineSegmentEditor setFromVertex(final LineString line, final int vertexIndex) {
    final int axisCount = getAxisCount();
    int offset = 0;
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double coordinate = line.getCoordinate(vertexIndex, axisIndex);
      final double[] coordinates = this.coordinates;
      coordinates[offset++] = coordinate;
    }
    return this;
  }

  public LineSegmentEditor setToVertex(final LineString line, final int vertexIndex) {
    final int axisCount = getAxisCount();
    final double[] coordinates = this.coordinates;
    int offset = axisCount;
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double coordinate = line.getCoordinate(vertexIndex, axisIndex);
      coordinates[offset++] = coordinate;
    }
    return this;
  }
}
