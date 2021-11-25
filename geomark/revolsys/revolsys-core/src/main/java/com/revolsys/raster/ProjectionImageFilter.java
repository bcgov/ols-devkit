package com.revolsys.raster;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.Point;
import com.revolsys.grid.Grid;

public class ProjectionImageFilter extends WholeImageFilter {
  private static final long serialVersionUID = 1L;

  private final BoundingBox destBoundingBox;

  private final int destHeight;

  private final double destPixelSize;

  private final int destWidth;

  private final BoundingBox sourceBoundingBox;

  public ProjectionImageFilter(final BoundingBox sourceBoundingBox,
    final GeometryFactoryProxy destGeometryFactory, final double sourcePixelSize) {
    this.sourceBoundingBox = sourceBoundingBox;
    this.destBoundingBox = sourceBoundingBox.bboxToCs(destGeometryFactory);
    final Point p1 = sourceBoundingBox.getCornerPoint(0).convertPoint2d(destGeometryFactory);
    final Point p2 = sourceBoundingBox.getCornerPoint(1).convertPoint2d(destGeometryFactory);
    final Point p3 = sourceBoundingBox.getCornerPoint(2).convertPoint2d(destGeometryFactory);
    final Point p4 = sourceBoundingBox.getCornerPoint(3).convertPoint2d(destGeometryFactory);

    final double sourceWidth = sourceBoundingBox.getWidth() / sourcePixelSize;
    final double distance1 = p1.distancePoint(p2);
    final double distance2 = p3.distancePoint(p4);
    final double destPixelSize1 = distance1 / sourceWidth;
    final double destPixelSize2 = distance2 / sourceWidth;
    if (destPixelSize1 < destPixelSize2) {
      this.destPixelSize = destPixelSize1 * 10;
    } else {
      this.destPixelSize = destPixelSize2 * 10;
    }
    final double width = this.destBoundingBox.getWidth();
    this.destWidth = (int)(width / this.destPixelSize);

    final double height = this.destBoundingBox.getHeight();
    this.destHeight = (int)(height / this.destPixelSize);
  }

  public int cubic(final int[] cubicParams, final int[] cubicParamsByte, final double percent) {
    int cubicRowResult = 0;
    for (int shift = 0; shift < 32; shift += 8) {
      for (int cubicCol = 0; cubicCol < 4; cubicCol++) {
        cubicParamsByte[cubicCol] = cubicParams[cubicCol] >> shift & 0xFF;
      }
      final int cubic = (int)Math.round(Grid.cubicInterpolate(cubicParamsByte[0],
        cubicParamsByte[1], cubicParamsByte[2], cubicParamsByte[3], percent));
      cubicRowResult |= cubic << shift;
    }
    return cubicRowResult;
  }

  public BufferedImage filter(final BufferedImage source) {
    if (this.destWidth < 1 || this.destHeight < 1) {
      return source;
    } else {
      final BufferedImage dest = new BufferedImage(this.destWidth, this.destHeight,
        BufferedImage.TYPE_INT_ARGB);
      return super.filter(source, dest);
    }
  }

  @Override
  protected int[] filterPixels(final int imageWidth, final int imageHeight, final int[] inPixels,
    final Rectangle transformedSpace) {
    final int inLength = inPixels.length;
    final boolean[] cubicHasRow = new boolean[4];
    final int[] cubicParams = new int[4];
    final int[] cubicParamsByte = new int[4];
    final int[] cubicResult = new int[4];
    final int[] outPixels = new int[transformedSpace.width * transformedSpace.height];

    final double minX = this.sourceBoundingBox.getMinX();
    final double minY = this.sourceBoundingBox.getMinY();
    final double width = this.sourceBoundingBox.getWidth();
    final double height = this.sourceBoundingBox.getHeight();
    final double pixelWidth = width / imageWidth;
    final double pixelHeight = height / imageHeight;

    final double newMinX = this.destBoundingBox.getMinX();
    final double newMaxY = this.destBoundingBox.getMaxY();

    final int newImageWidth = transformedSpace.width;
    final int newImageHeight = transformedSpace.height;
    final GeometryFactory sourceGeometryFactory = this.sourceBoundingBox.getGeometryFactory();
    final GeometryFactory destGeometryFactory = this.destBoundingBox.getGeometryFactory();

    final CoordinatesOperation operation = destGeometryFactory
      .getCoordinatesOperation(sourceGeometryFactory);
    if (operation == null) {
      return inPixels;
    }
    final CoordinatesOperationPoint point = new CoordinatesOperationPoint();
    for (int i = 0; i < newImageWidth; i++) {
      final double newImageX = newMinX + i * this.destPixelSize;
      for (int j = 0; j < newImageHeight; j++) {
        final double newImageY = newMaxY - j * this.destPixelSize;
        point.setPoint(newImageX, newImageY);
        operation.perform(point);
        final double imageX = point.x;
        final double imageY = point.y;
        final double xGrid = (imageX - minX) / pixelWidth;
        final int gridX = (int)Math.floor(xGrid);
        final double xPercent = xGrid - gridX;

        final double yGrid = (imageY - minY) / pixelHeight;
        int gridY = (int)Math.floor(yGrid);

        final double yPercent = yGrid - gridY;
        gridY = imageHeight - gridY;

        if (gridX > -1 && gridX < imageWidth) {
          if (gridY > -1 && gridY < imageHeight) {
            final int rgb = inPixels[gridY * imageWidth + gridX];
            // final int rgb = getValueBiCubic(imageWidth, inPixels, inLength,
            // cubicHasRow,
            // cubicParams, cubicParamsByte, cubicResult, gridX, gridY,
            // xPercent, yPercent);
            if (rgb != -1) {
              outPixels[j * newImageWidth + i] = rgb;
              // // TODO better interpolation
            }
          }
        }
      }
    }
    return outPixels;
  }

  public BoundingBox getDestBoundingBox() {
    return this.destBoundingBox;
  }

  public int getDestHeight() {
    return this.destHeight;
  }

  public int getDestWidth() {
    return this.destWidth;
  }

  @Override
  protected void transformSpace(final Rectangle rect) {
    super.transformSpace(rect);
    rect.width = this.destWidth;
    rect.height = this.destHeight;
  }

}
