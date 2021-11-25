package com.revolsys.geometry.operation;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.editor.LineStringEditor;
import com.revolsys.geometry.model.editor.MultiLineStringEditor;

public class RectangleIntersection {

  private double[] fromCoordinates = new double[4];

  private double[] toCoordinates = new double[4];

  private final double[] clippedFromCoordinates = new double[4];

  private final double[] clippedToCoordinates = new double[4];

  private int axisCount;

  private double minX;

  private double maxX;

  private double minY;

  private double maxY;

  private boolean fromClipped;

  public RectangleIntersection() {
  }

  public void appendClippedVertices(final LineStringEditor editor) {
    editor.appendVertex(false, this.clippedFromCoordinates);
    editor.appendVertex(false, this.clippedToCoordinates);
  }

  private void clampToX(final double[] coordinates, final double newX) {
    final int axisCount = this.axisCount;
    final double[] fromCoordinates = this.fromCoordinates;
    final double[] toCoordinates = this.toCoordinates;
    final double x1 = fromCoordinates[0];
    final double y1 = fromCoordinates[1];
    final double x2 = toCoordinates[0];
    final double y2 = toCoordinates[1];
    final double deltaX = x2 - x1;
    final double percent = (newX - x1) / deltaX;
    coordinates[0] = newX;
    coordinates[1] = y1 + (y2 - y1) * percent;
    for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
      final double c1 = fromCoordinates[axisIndex];
      final double c2 = toCoordinates[axisIndex];
      coordinates[axisIndex] = c1 + (c2 - c1) * percent;
    }
  }

  private void clampToY(final double[] coordinates, final double newY) {
    final int axisCount = this.axisCount;
    final double[] fromCoordinates = this.fromCoordinates;
    final double[] toCoordinates = this.toCoordinates;
    final double x1 = fromCoordinates[0];
    final double y1 = fromCoordinates[1];
    final double x2 = toCoordinates[0];
    final double y2 = toCoordinates[1];
    final double deltaY = y2 - y1;
    final double percent = (newY - y1) / deltaY;
    coordinates[0] = x1 + (x2 - x1) * percent;
    coordinates[1] = newY;
    for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
      final double c1 = fromCoordinates[axisIndex];
      final double c2 = toCoordinates[axisIndex];
      coordinates[axisIndex] = c1 + (c2 - c1) * percent;
    }
  }

  private boolean clipToMaxX(final double x, final double[] clippedCoordinates) {
    if (x > this.maxX) {
      clampToX(clippedCoordinates, this.maxX);
      return true;
    } else {
      return false;
    }
  }

  private boolean clipToMaxY(final double y, final double[] clippedCoordinates) {
    if (y > this.maxY) {
      clampToY(clippedCoordinates, this.maxY);
      return true;
    } else {
      return false;
    }
  }

  private boolean clipToMinX(final double x, final double[] clippedCoordinates) {
    if (x < this.minX) {
      clampToX(clippedCoordinates, this.minX);
      return true;
    } else {
      return false;
    }
  }

  private boolean clipToMinY(final double y, final double[] clippedCoordinates) {
    if (y < this.minY) {
      clampToY(clippedCoordinates, this.minY);
      return true;
    } else {
      return false;
    }
  }

  public boolean clipToRectangle() {
    final double[] from = this.clippedFromCoordinates;
    final double[] to = this.clippedToCoordinates;
    final boolean x1ClippedMinX = clipToMinX(from[0], from);
    final boolean x2ClippedMinX = clipToMinX(to[0], to);
    if (x1ClippedMinX) {
      this.fromClipped = true;
      if (x2ClippedMinX) {
        return false;
      }
    }
    final boolean x1ClippedMaxX = clipToMaxX(from[0], from);
    final boolean x2ClippedMaxX = clipToMaxX(to[0], to);
    if (x1ClippedMaxX) {
      this.fromClipped = true;
      if (x2ClippedMaxX) {
        return false;
      }
    }
    final boolean y1ClippedMinY = clipToMinY(from[1], from);
    final boolean y2ClippedMinY = clipToMinY(to[1], to);
    if (y1ClippedMinY) {
      this.fromClipped = true;
      if (y2ClippedMinY) {
        return false;
      }
    }
    final boolean y1ClippedMaxY = clipToMaxY(from[1], from);
    final boolean y2ClippedMaxY = clipToMaxY(to[1], to);
    if (y1ClippedMaxY) {
      this.fromClipped = true;
      if (y2ClippedMaxY) {
        return false;
      }
    }
    return true;
  }

  public Geometry intersectionLine(final LineString line, final BoundingBox rectangle) {
    if (line == null || rectangle.bboxCovers(line)) {
      return line;
    } else {
      final GeometryFactory geometryFactory = line.getGeometryFactory();
      if (line.bboxIntersects(rectangle)) {
        setRectangle(rectangle);
        this.axisCount = geometryFactory.getAxisCount();
        final MultiLineStringEditor lines = new MultiLineStringEditor(geometryFactory);
        LineStringEditor lineStringEditor = lines.appendEditor();
        final int vertexCount = line.getVertexCount();
        setNextLine(line);
        for (int vertexIndex = 1; vertexIndex < vertexCount; vertexIndex++) {
          setNextSegment(line, vertexIndex);
          if (clipToRectangle()) {
            if (isFromClipped() && !lineStringEditor.isEmpty()) {
              lineStringEditor = lines.appendEditor();
            }
            lineStringEditor.appendVertex(false, this.clippedFromCoordinates);
            lineStringEditor.appendVertex(false, this.clippedToCoordinates);
          }
        }
        final Geometry clip = lines.newGeometryAny();
        return clip;
      } else {
        return geometryFactory.lineString();
      }
    }
  }

  public boolean isFromClipped() {
    return this.fromClipped;
  }

  /**
   * Initialize the intersection for the start of the line.
   *
   * @param line The line.
   */
  public void setNextLine(final LineString line) {
    // toCoordinates is correct as it get's switched on the first call to
    // setNextSegment
    line.copyPoint(0, this.axisCount, this.toCoordinates);
  }

  /**
   * Set the from/to coordinates to the specified segment of the line.
   *
   * @param line The line.
   * @param vertexIndex The index of the vertex at the end of the segment.
   */
  public void setNextSegment(final LineString line, final int vertexIndex) {
    final double[] temp = this.fromCoordinates;
    this.fromCoordinates = this.toCoordinates;
    this.toCoordinates = temp;
    line.copyPoint(vertexIndex, this.axisCount, this.toCoordinates);
    System.arraycopy(this.fromCoordinates, 0, this.clippedFromCoordinates, 0, this.axisCount);
    System.arraycopy(this.toCoordinates, 0, this.clippedToCoordinates, 0, this.axisCount);
    this.fromClipped = false;
  }

  public void setRectangle(final BoundingBox boundingBox) {
    this.minX = boundingBox.getMinX();
    this.minY = boundingBox.getMinY();
    this.maxX = boundingBox.getMaxX();
    this.maxY = boundingBox.getMaxY();
  }

}
