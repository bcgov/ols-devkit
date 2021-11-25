package com.revolsys.raster;
// Mesh and Warp Canvas

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.Point;
import com.revolsys.util.Cancellable;

public class ImageProjector {

  private static interface Grid {
    double getValue(int x, int y);
  }

  private class SourceXGrid implements Grid {
    private final double[] percents = new double[ImageProjector.this.gridWidth + 1];

    private SourceXGrid() {
      final double max = ImageProjector.this.gridWidth;
      for (int i = 0; i < this.percents.length; i++) {
        this.percents[i] = i / max;
      }
    }

    private double getValue(final int x) {
      return this.percents[x];
    }

    @Override
    public double getValue(final int x, final int y) {
      return this.percents[x];
    }

    @Override
    public String toString() {
      return Arrays.toString(this.percents);
    }
  }

  private class SourceYGrid implements Grid {
    private final double[] percents = new double[ImageProjector.this.gridHeight + 1];

    private SourceYGrid() {
      final double max = ImageProjector.this.gridHeight;
      for (int i = 0; i < this.percents.length; i++) {
        this.percents[i] = i / max;
      }
    }

    private double getValue(final int y) {
      return this.percents[y];
    }

    @Override
    public double getValue(final int x, final int y) {
      return this.percents[y];
    }

    @Override
    public String toString() {
      return Arrays.toString(this.percents);
    }
  }

  private class TargetGrid implements Grid {
    private final double[][] percentRows;

    private TargetGrid() {
      final double[][] grid = new double[ImageProjector.this.gridHeight + 1][];
      for (int i = 0; i < grid.length; i++) {
        grid[i] = new double[ImageProjector.this.gridWidth + 1];
      }
      this.percentRows = grid;
    }

    @Override
    public double getValue(final int x, final int y) {
      return this.percentRows[y][x];
    }

    private void setValue(final int x, final int y, final double value) {
      this.percentRows[y][x] = value;
    }

    @Override
    public String toString() {
      final StringBuilder string = new StringBuilder();
      for (final double[] percents : this.percentRows) {
        string.append(Arrays.toString(percents));
        string.append("\n");
      }
      return string.toString();
    }
  }

  private static void gauss(final double[][] matrix) {
    final double[] scalingFactor = new double[3];
    for (int i = 0; i < 3; i++) {
      scalingFactor[i] = maxAbs(matrix[i]);
    }

    int j = 2;
    for (int k = 0; k < 2; k++) {
      --j;
      double maxR = 0;
      for (int i = k; i < 3; i++) {
        final double r = Math.abs(matrix[i][k] / scalingFactor[i]);
        if (r > maxR) {
          maxR = r;
          j = i;
        }
      }
      final double[] rowK = matrix[k];
      for (int i = k + 1; i < 3; i++) {
        final double[] rowI = matrix[i];
        final double xmult = rowI[k] / rowK[k];
        rowI[k] = xmult;
        for (j = k + 1; j < 3; j++) {
          rowI[j] = rowI[j] - xmult * rowK[j];
        }
      }
    }
  }

  private static double maxAbs(final double[] values) {
    double max = 0;
    for (final double value : values) {
      final double absValue = Math.abs(value);
      if (absValue > max) {
        max = absValue;
      }
    }
    return max;
  }

  private static double[] solve(final double[][] matrix, final double[] b) {
    final double[] x = new double[3];
    for (int k = 0; k < 2; k++) {
      final int indexK = k;
      for (int i = k + 1; i < 3; i++) {
        final int indexI = i;
        b[indexI] -= matrix[indexI][k] * b[indexK];
      }
    }
    final int indexLast = 2;
    x[2] = b[indexLast] / matrix[indexLast][2];

    for (int i = 1; i >= 0; --i) {
      final int index = i;
      double sum = b[index];
      for (int j = i + 1; j < 3; j++) {
        sum = sum - matrix[index][j] * x[j];
      }
      x[i] = sum / matrix[index][i];

    }
    return x;
  }

  private final GeoreferencedImage sourceImage;

  private final BufferedImage sourceBufferdImage;

  private BufferedGeoreferencedImage targetImage;

  private final ImageProjectorTriangle targetTriangle = new ImageProjectorTriangle();

  private final CoordinatesOperationPoint point = new CoordinatesOperationPoint();

  private final CoordinatesOperation operation;

  private Graphics2D g2;

  private GeometryFactory targetGeometryFactory;

  private final AffineTransform transform = new AffineTransform();

  private final SourceXGrid sourceXGrid;

  private final SourceYGrid sourceYGrid;

  private final TargetGrid targetXGrid;

  private final TargetGrid targetYGrid;

  private final int gridWidth;

  private final int gridHeight;

  private final BoundingBox sourceBoundingBox;

  private final int step = 50;

  private final BoundingBox targetBoundingBox;

  private int targetImageWidth;

  private int targetImageHeight;

  private Cancellable cancellable = Cancellable.FALSE;

  public ImageProjector(final GeoreferencedImage sourceImage,
    final GeometryFactoryProxy targetGeometryFactory) {
    this.sourceImage = sourceImage;
    this.sourceBoundingBox = sourceImage.getBoundingBox();
    this.sourceBufferdImage = sourceImage.getBufferedImage();

    if (targetGeometryFactory == null) {
      this.targetGeometryFactory = GeometryFactory.DEFAULT_2D;
    } else {
      this.targetGeometryFactory = targetGeometryFactory.getGeometryFactory();
    }

    this.operation = this.sourceImage.getCoordinatesOperation(targetGeometryFactory);
    this.targetBoundingBox = this.sourceBoundingBox.bboxToCs(this.targetGeometryFactory);

    final int imageWidth = this.sourceImage.getImageWidth();
    final int imageHeight = this.sourceImage.getImageHeight();
    this.gridWidth = (int)Math.ceil(imageWidth / (double)this.step);
    this.gridHeight = (int)Math.ceil(imageHeight / (double)this.step);

    this.sourceXGrid = new SourceXGrid();
    this.sourceYGrid = new SourceYGrid();
    this.targetXGrid = new TargetGrid();
    this.targetYGrid = new TargetGrid();

    final CoordinatesOperationPoint point = this.point;
    final double sourceMinX = this.sourceBoundingBox.getMinX();
    final double sourceMaxY = this.sourceBoundingBox.getMaxY();
    final double sourceWidth = this.sourceBoundingBox.getWidth();
    final double sourceHeight = this.sourceBoundingBox.getHeight();

    final double targetMinX = this.targetBoundingBox.getMinX();
    final double targetMaxY = this.targetBoundingBox.getMaxY();
    final double targetWidth = this.targetBoundingBox.getWidth();
    final double targetHeight = this.targetBoundingBox.getHeight();

    final CoordinatesOperation operation = this.operation;
    for (int gridY = 0; gridY <= this.gridHeight; gridY++) {
      final double yPercent = this.sourceYGrid.getValue(gridY);
      final double sourceY = sourceMaxY - yPercent * sourceHeight;
      for (int gridX = 0; gridX <= this.gridWidth; gridX++) {
        final double xPercent = this.sourceXGrid.getValue(gridX);
        final double sourceX = sourceMinX + xPercent * sourceWidth;
        point.setPoint(sourceX, sourceY);
        operation.perform(point);
        final double targetPercentX = (point.x - targetMinX) / targetWidth;
        final double targetPercentY = (targetMaxY - point.y) / targetHeight;
        this.targetXGrid.setValue(gridX, gridY, targetPercentX);
        this.targetYGrid.setValue(gridX, gridY, targetPercentY);
      }
    }
  }

  public void drawImage(final Graphics2D graphics) {
    this.g2 = graphics;

    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
      RenderingHints.VALUE_INTERPOLATION_BICUBIC);

    final Shape clip = graphics.getClip();

    final int imageWidth = this.sourceImage.getImageWidth();
    final int imageHeight = this.sourceImage.getImageHeight();

    final double gridCellWidth = imageWidth / 10.0;
    final double gridCellHeight = imageHeight / 10.0;
    for (int gridY = 0; gridY < 10 && !isCancelled(); gridY++) {
      final double imageY = gridY * gridCellHeight;
      final double imageY2 = imageY + gridCellHeight;
      for (int gridX = 0; gridX < 10 && !isCancelled(); gridX++) {
        final double imageX = gridX * gridCellWidth;
        final double imageX2 = imageX + gridCellWidth;
        drawTriangle(imageX2, imageY, imageX, imageY2, imageX, imageY);
        drawTriangle(imageX2, imageY, imageX, imageY2, imageX2, imageY2);
      }
    }
    graphics.setClip(clip);
  }

  private void drawTriangle(final double x1, final double y1, final double x2, final double y2,
    final double x3, final double y3) {
    final GeoreferencedImage sourceImage = this.sourceImage;
    final ImageProjectorTriangle targetTriangle = this.targetTriangle;
    final CoordinatesOperationPoint point = this.point;
    final CoordinatesOperation operation = this.operation;
    final BufferedGeoreferencedImage targetImage = this.targetImage;
    targetTriangle.setCorner(0, sourceImage, x1, y1, point, operation, targetImage);
    targetTriangle.setCorner(1, sourceImage, x2, y2, point, operation, targetImage);
    targetTriangle.setCorner(2, sourceImage, x3, y3, point, operation, targetImage);

    final double[][] a = new double[][] {
      {
        x1, y1, 1
      }, {
        x2, y2, 1
      }, {
        x3, y3, 1
      }
    };

    gauss(a);

    final double[] bx = targetTriangle.xCoordinates.clone();
    final double[] x = solve(a, bx);

    final double[] by = targetTriangle.yCoordinates.clone();
    final double[] y = solve(a, by);

    this.transform.setTransform(x[0], y[0], x[1], y[1], x[2], y[2]);
    final Graphics2D g2 = this.g2;
    g2.setClip(targetTriangle);
    g2.drawImage(this.sourceBufferdImage, this.transform, null);
  }

  private double getResolution(final double originalDistance, final double distance1,
    final double distance2) {
    final double targetPixelSize1 = distance1 / originalDistance;
    final double targetPixelSize2 = distance2 / originalDistance;
    double targetPixelSize;
    if (targetPixelSize1 < targetPixelSize2) {
      targetPixelSize = targetPixelSize1;
    } else {
      targetPixelSize = targetPixelSize2;
    }
    return targetPixelSize;
  }

  public boolean isCancelled() {
    return this.cancellable.isCancelled();
  }

  public GeoreferencedImage newImage() {
    final BoundingBox sourceBoundingBox = this.sourceBoundingBox;
    final Point p1 = sourceBoundingBox.getCornerPoint(0).convertPoint2d(this.targetGeometryFactory);
    final Point p2 = sourceBoundingBox.getCornerPoint(1).convertPoint2d(this.targetGeometryFactory);
    final Point p3 = sourceBoundingBox.getCornerPoint(2).convertPoint2d(this.targetGeometryFactory);
    final Point p4 = sourceBoundingBox.getCornerPoint(3).convertPoint2d(this.targetGeometryFactory);

    final double sourceResolutionX = this.sourceImage.getResolutionX();
    final double sourceWidth = sourceBoundingBox.getWidth() / sourceResolutionX;
    final double width1 = p1.distancePoint(p2);
    final double width2 = p3.distancePoint(p4);
    final double targetResolutionX = getResolution(sourceWidth, width1, width2);

    final double sourceResolutionY = this.sourceImage.getResolutionY();
    final double sourceHeight = sourceBoundingBox.getHeight() / sourceResolutionY;
    final double height1 = p1.distancePoint(p4);
    final double height2 = p2.distancePoint(p3);
    final double targetResolutionY = getResolution(sourceHeight, height1, height2);

    final BoundingBox targetBoundingBox = this.targetBoundingBox;
    final double width = targetBoundingBox.getWidth();
    final double height = targetBoundingBox.getHeight();
    this.targetImageWidth = (int)(width / targetResolutionX);

    this.targetImageHeight = (int)(height / targetResolutionY);

    this.targetImage = BufferedGeoreferencedImage.newImage(targetBoundingBox, this.targetImageWidth,
      this.targetImageHeight);
    final Graphics2D graphics = this.targetImage.getBufferedImage().createGraphics();
    try {
      drawImage(graphics);
    } finally {
      graphics.dispose();
    }
    return this.targetImage;
  }

  public ImageProjector setCancellable(final Cancellable cancellable) {
    this.cancellable = cancellable;
    return this;
  }

  public void setCorner(final ImageProjectorTriangle targetTriangle, final int i,
    final GeoreferencedImage sourceImage, final double imageX, final double imageY,
    final CoordinatesOperationPoint point, final CoordinatesOperation operation,
    final BufferedGeoreferencedImage targetImage) {
    sourceImage.copyModelPoint(point, imageX, imageY);
    operation.perform(point);
    targetImage.toImagePoint(point);
    final double targetImageX = point.x;
    final double targetImageY = point.y;
    targetTriangle.setPoint(i, targetImageX, targetImageY);
  }

}
