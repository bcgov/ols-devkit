package com.revolsys.gis.parallel;

import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.parallel.process.AbstractProcess;

public abstract class BoundingBoxTaskSplitter extends AbstractProcess {
  private Geometry boundary;

  private BoundingBox boundingBox;

  private boolean logScriptInfo;

  private int numX = 10;

  private int numY = 10;

  private Geometry preparedBoundary;

  public abstract void execute(BoundingBox cellBoundingBox);

  public Geometry getBoundary() {
    return this.boundary;
  }

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public int getNumX() {
    return this.numX;
  }

  public int getNumY() {
    return this.numY;
  }

  public boolean isLogScriptInfo() {
    return this.logScriptInfo;
  }

  protected void postRun() {
  }

  protected void preRun() {
    if (this.boundingBox != null) {
      if (this.boundary != null) {
        this.preparedBoundary = this.boundary.prepare();
      }
    }
  }

  @Override
  public void run() {
    preRun();
    try {
      if (this.boundingBox != null) {
        final GeometryFactory geometryFactory = this.boundingBox.getGeometryFactory();
        final double xInc = this.boundingBox.getWidth() / this.numX;
        final double yInc = this.boundingBox.getHeight() / this.numY;
        double y = this.boundingBox.getMinY();
        for (int j = 0; j < this.numX; j++) {
          double x = this.boundingBox.getMinX();
          for (int i = 0; i < this.numX; i++) {
            final BoundingBox cellBoundingBox = geometryFactory.newBoundingBox(x, y, x + xInc,
              y + yInc);
            if (this.preparedBoundary == null
              || this.preparedBoundary.bboxIntersects(cellBoundingBox.toPolygon(50))) {
              if (this.logScriptInfo) {
                Logs.info(this, "Processing bounding box " + cellBoundingBox.toPolygon(1));
              }
              execute(cellBoundingBox);
            }
            x += xInc;
          }
          y += yInc;
        }
      }
    } finally {
      postRun();
    }
  }

  public void setBoundary(final Geometry boundary) {
    this.boundary = boundary;
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  public void setLogScriptInfo(final boolean logScriptInfo) {
    this.logScriptInfo = logScriptInfo;
  }

  public void setNumX(final int numX) {
    this.numX = numX;
  }

  public void setNumY(final int numY) {
    this.numY = numY;
  }

}
