package com.revolsys.raster;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

class ImageProjectorTriangle implements Shape {

  protected final double[] xCoordinates;

  protected final double[] yCoordinates;

  public ImageProjectorTriangle() {
    this.xCoordinates = new double[3];
    this.yCoordinates = new double[3];
  }

  public ImageProjectorTriangle(final double x1, final double y1, final double x2, final double y2,
    final double x3, final double y3) {
    this.xCoordinates = new double[] {
      x1, x2, x3
    };
    this.yCoordinates = new double[] {
      y1, y2, y3
    };
  }

  public ImageProjectorTriangle(final double[] coords) {
    this.xCoordinates = new double[3];
    this.yCoordinates = new double[3];

    if (coords.length < 6) {
      throw new IllegalArgumentException("Must contain 6 points " + Arrays.toString(coords));
    }

    int j = 0;
    for (int i = 0; i < 3; i++) {
      this.xCoordinates[i] = coords[j++];
      this.yCoordinates[i] = coords[j++];
    }
  }

  public Rectangle2D.Double calculateBounds(final Rectangle2D.Double bounds) {
    bounds.setRect(this.xCoordinates[0], this.yCoordinates[0], 0, 0);
    for (int i = 1; i < 3; i++) {
      final double x = this.xCoordinates[i];
      final double y = this.yCoordinates[i];
      bounds.add(x, y);
    }
    return bounds;
  }

  @Override
  public boolean contains(final double x, final double y) {
    final double x1 = this.xCoordinates[0];
    final double y1 = this.yCoordinates[0];
    final double x2 = this.xCoordinates[1];
    final double y2 = this.yCoordinates[1];
    final double x3 = this.xCoordinates[2];
    final double y3 = this.yCoordinates[2];
    return com.revolsys.geometry.model.Triangle.containsPoint(x1, y1, x2, y2, x3, y3, x, y);
  }

  @Override
  public boolean contains(final double x, final double y, final double w, final double h) {
    final double x2 = x + w;
    final double y2 = y + h;
    if (contains(x, y)) {
      if (contains(x2, y)) {
        if (contains(x, y2)) {
          if (contains(x2, y2)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public boolean contains(final Point2D point) {
    final double x = point.getX();
    final double y = point.getY();
    return contains(x, y);
  }

  @Override
  public boolean contains(final Rectangle2D r) {
    final double x = r.getX();
    final double y = r.getY();
    final double w = r.getWidth();
    final double h = r.getHeight();
    return contains(x, y, w, h);
  }

  @Override
  public Rectangle getBounds() {
    return getBounds2D().getBounds();
  }

  @Override
  public Rectangle2D getBounds2D() {
    return calculateBounds(new Rectangle2D.Double());
  }

  @Override
  public PathIterator getPathIterator(final AffineTransform at) {
    if (at == null || at.isIdentity()) {
      return new PathIterator() {

        private boolean done;

        protected int vertexIndex = 0;

        protected int awtType = SEG_MOVETO;

        @Override
        public int currentSegment(final double[] coordinates) {
          final int vertexIndex = this.vertexIndex % 3;
          coordinates[0] = ImageProjectorTriangle.this.xCoordinates[vertexIndex];
          coordinates[1] = ImageProjectorTriangle.this.yCoordinates[vertexIndex];
          return this.awtType;
        }

        @Override
        public int currentSegment(final float[] coordinates) {
          final int vertexIndex = this.vertexIndex % 3;
          coordinates[0] = (float)ImageProjectorTriangle.this.xCoordinates[vertexIndex];
          coordinates[1] = (float)ImageProjectorTriangle.this.yCoordinates[vertexIndex];
          return this.awtType;
        }

        @Override
        public int getWindingRule() {
          return WIND_EVEN_ODD;
        }

        @Override
        public boolean isDone() {
          return this.done;
        }

        @Override
        public void next() {
          this.vertexIndex++;
          if (this.vertexIndex < 3) {
            this.awtType = SEG_LINETO;
          } else if (this.vertexIndex == 3) {
            this.awtType = SEG_CLOSE;
          } else {
            this.done = true;
          }
        }

      };

    } else {
      return new PathIterator() {

        private boolean done;

        protected int vertexIndex = 0;

        protected int awtType = SEG_MOVETO;

        @Override
        public int currentSegment(final double[] coordinates) {
          final int vertexIndex = this.vertexIndex % 3;
          coordinates[0] = ImageProjectorTriangle.this.xCoordinates[vertexIndex];
          coordinates[1] = ImageProjectorTriangle.this.yCoordinates[vertexIndex];
          at.transform(coordinates, 0, coordinates, 0, 1);
          return this.awtType;
        }

        @Override
        public int currentSegment(final float[] coordinates) {
          final int vertexIndex = this.vertexIndex % 3;
          coordinates[0] = (float)ImageProjectorTriangle.this.xCoordinates[vertexIndex % 3];
          coordinates[1] = (float)ImageProjectorTriangle.this.xCoordinates[vertexIndex % 3];
          at.transform(coordinates, 0, coordinates, 0, 1);
          return this.awtType;
        }

        @Override
        public int getWindingRule() {
          return WIND_EVEN_ODD;
        }

        @Override
        public boolean isDone() {
          return this.done;
        }

        @Override
        public void next() {
          this.vertexIndex++;
          if (this.vertexIndex < 3) {
            this.awtType = SEG_LINETO;
          } else if (this.vertexIndex == 3) {
            this.awtType = SEG_CLOSE;
          } else {
            this.done = true;
          }
        }

      };

    }
  }

  @Override
  public PathIterator getPathIterator(final AffineTransform at, final double flatness) {
    final PathIterator pathIterator = getPathIterator(at);
    return new FlatteningPathIterator(pathIterator, flatness);
  }

  public double getX(final int index) {
    return this.xCoordinates[index];
  }

  public double getY(final int index) {
    return this.yCoordinates[index];
  }

  @Override
  public boolean intersects(final double x, final double y, final double width,
    final double height) {
    return false;
    // return this.geometry.bboxIntersects(x, y, x + width, y + height);
  }

  @Override
  public boolean intersects(final Rectangle2D r) {
    final double x = r.getX();
    final double y = r.getY();
    final double width = r.getWidth();
    final double height = r.getHeight();
    return intersects(x, y, width, height);
  }

  public void setCorner(final int i, final GeoreferencedImage sourceImage, final double imageX,
    final double imageY, final CoordinatesOperationPoint point,
    final CoordinatesOperation operation, final BufferedGeoreferencedImage targetImage) {
    sourceImage.copyModelPoint(point, imageX, imageY);
    operation.perform(point);
    targetImage.toImagePoint(point);
    final double targetImageX = point.x;
    final double targetImageY = point.y;
    setPoint(i, targetImageX, targetImageY);
  }

  public void setCorners(final double x1, final double y1, final double x2, final double y2,
    final double x3, final double y3) {
    this.xCoordinates[0] = x1;
    this.yCoordinates[0] = y1;
    this.xCoordinates[1] = x2;
    this.yCoordinates[1] = y2;
    this.xCoordinates[2] = x3;
    this.yCoordinates[2] = y3;
  }

  public void setPoint(final int index, final double x, final double y) {
    this.xCoordinates[index] = x;
    this.yCoordinates[index] = y;
  }

  public void setX(final int index, final double x) {
    this.xCoordinates[index] = x;
  }

  public void setY(final int index, final double y) {
    this.yCoordinates[index] = y;
  }

}
