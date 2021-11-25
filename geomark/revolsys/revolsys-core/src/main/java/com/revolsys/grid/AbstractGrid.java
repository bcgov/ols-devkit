package com.revolsys.grid;

import org.jeometry.common.awt.WebColors;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.util.RectangleUtil;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;

public abstract class AbstractGrid extends BaseObjectWithProperties implements Grid {
  protected double[] bounds = RectangleUtil.newBounds(3);

  protected int gridHeight;

  protected int gridWidth;

  private double minColourMultiple;

  private double colourGreyMultiple;

  protected double gridCellWidth;

  protected double gridCellHeight;

  private Resource resource;

  private GeometryFactory geometryFactory;

  private BoundingBox boundingBox;

  private double scaleX;

  private double scaleY;

  private boolean modified;

  public AbstractGrid() {
  }

  public AbstractGrid(final GeometryFactory geometryFactory, final BoundingBox boundingBox,
    final int gridWidth, final int gridHeight, final double gridCellSize) {
    this(geometryFactory, boundingBox, gridWidth, gridHeight, gridCellSize, gridCellSize);
  }

  public AbstractGrid(final GeometryFactory geometryFactory, final BoundingBox boundingBox,
    final int gridWidth, final int gridHeight, final double gridCellWidth,
    final double gridCellHeight) {
    this.gridWidth = gridWidth;
    this.gridHeight = gridHeight;
    this.gridCellWidth = gridCellWidth;
    this.gridCellHeight = gridCellHeight;
    setGeometryFactory(geometryFactory);
    final double minX = boundingBox.getMinX();
    final double minY = boundingBox.getMinY();
    final double minZ = boundingBox.getMinZ();
    final double maxX = boundingBox.getMaxX();
    final double maxY = boundingBox.getMaxY();
    final double maxZ = boundingBox.getMaxZ();
    setBounds(minX, minY, minZ, maxX, maxY, maxZ);
  }

  public AbstractGrid(final GeometryFactory geometryFactory, final double minX, final double minY,
    final int gridWidth, final int gridHeight, final double gridCellSize) {
    this(geometryFactory, minX, minY, gridWidth, gridHeight, gridCellSize, gridCellSize);
  }

  public AbstractGrid(final GeometryFactory geometryFactory, final double minX, final double minY,
    final int gridWidth, final int gridHeight, final double gridCellWidth,
    final double gridCellHeight) {
    this.gridWidth = gridWidth;
    this.gridHeight = gridHeight;
    this.gridCellWidth = gridCellWidth;
    this.gridCellHeight = gridCellHeight;
    setGeometryFactory(geometryFactory);
    this.bounds = new double[] {
      minX, minY, Double.NaN, minX + gridWidth * gridCellWidth, minY + gridHeight * gridCellHeight,
      Double.NaN
    };
    this.boundingBox = geometryFactory.newBoundingBox(3, this.bounds);
  }

  protected AbstractGrid(final GeometryFactory geometryFactory, final int gridWidth,
    final double gridCellSize) {
    this(geometryFactory, gridWidth, gridCellSize, gridCellSize);
  }

  protected AbstractGrid(final GeometryFactory geometryFactory, final int gridWidth,
    final double gridCellWidth, final double gridCellHeight) {
    this.gridWidth = gridWidth;
    this.gridHeight = 0;
    this.gridCellWidth = gridCellWidth;
    this.gridCellHeight = gridCellHeight;
    setGeometryFactory(geometryFactory);
  }

  @Override
  public void clear() {
    clearCachedObjects();
  }

  protected void clearCachedObjects() {
    this.modified = true;
  }

  protected void expandRange() {
    this.bounds[2] = Double.NaN;
    this.bounds[5] = Double.NaN;
    final int gridWidth = this.gridWidth;
    final int gridHeight = this.gridHeight;
    for (int gridY = 0; gridY < gridHeight; gridY++) {
      for (int gridX = 0; gridX < gridWidth; gridX++) {
        final double value = getValue(gridX, gridY);
        if (Double.isFinite(value)) {
          expandRange(value);
        }
      }
    }

  };

  protected void expandRange(final double value) {
    if (Double.isFinite(value)) {
      final double minZ = this.bounds[2];
      if (value < minZ || !Double.isFinite(minZ)) {
        this.bounds[2] = value;
      }
      final double maxZ = this.bounds[5];
      if (value > maxZ || !Double.isFinite(maxZ)) {
        this.bounds[5] = value;
      }
    }
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  @Override
  public int getColour(final int gridX, final int gridY) {
    final int colour;
    final double elevation = getValue(gridX, gridY);
    if (Double.isNaN(elevation)) {
      colour = NULL_COLOUR;
    } else {
      final double elevationMultiple = elevation * this.colourGreyMultiple;
      final double elevationPercent = elevationMultiple - this.minColourMultiple;
      final int grey = (int)Math.round(elevationPercent * 255);
      colour = WebColors.colorToRGB(255, grey, grey, grey);
    }
    return colour;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public double getGridCellHeight() {
    return this.gridCellHeight;
  }

  @Override
  public double getGridCellWidth() {
    return this.gridCellWidth;
  }

  @Override
  public int getGridCellX(final double x) {
    final double minX = this.bounds[0];
    final double deltaX = x - minX;
    final double cellDiv = deltaX / this.gridCellWidth;
    final int gridX = (int)Math.floor(cellDiv);
    return gridX;
  }

  @Override
  public int getGridCellY(final double y) {
    final double minY = this.bounds[1];
    final double deltaY = y - minY;
    final double cellDiv = deltaY / this.gridCellHeight;
    final int gridY = (int)Math.floor(cellDiv);
    return gridY;
  }

  @Override
  public int getGridHeight() {
    return this.gridHeight;
  }

  @Override
  public double getGridMinX() {
    return this.bounds[0];
  }

  @Override
  public double getGridMinY() {
    return this.bounds[1];
  }

  @Override
  public int getGridWidth() {
    return this.gridWidth;
  }

  @Override
  public double getMaxValue() {
    return this.bounds[5];
  }

  @Override
  public double getMinValue() {
    return this.bounds[2];
  }

  @Override
  public Resource getResource() {
    return this.resource;
  }

  @Override
  public double getScaleX() {
    return this.scaleX;
  }

  @Override
  public double getScaleY() {
    return this.scaleY;
  }

  @Override
  public boolean isEmpty() {
    getBoundingBox();
    return Double.isNaN(this.bounds[2]);
  }

  public boolean isModified() {
    return this.modified;
  }

  @Override
  public void setBoundingBox(final BoundingBox boundingBox) {
    this.bounds = boundingBox.getMinMaxValues(3);
  }

  protected void setBounds(final double minX, final double minY, final double minZ,
    final double maxX, final double maxY, final double maxZ) {
    this.bounds = new double[] {
      minX, minY, minZ, maxX, maxY, maxZ
    };
    if (Double.isFinite(minZ)) {
      this.colourGreyMultiple = 1.0 / (maxZ - minZ);
      this.minColourMultiple = minZ * this.colourGreyMultiple;
      this.modified = false;
    }
    this.boundingBox = this.geometryFactory.newBoundingBox(3, this.bounds);
  }

  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    this.scaleX = geometryFactory.getScaleX();
    this.scaleY = geometryFactory.getScaleY();
  }

  public void setGridCellHeight(final double gridCellHeight) {
    this.gridCellHeight = gridCellHeight;
  }

  public void setGridCellWidth(final double gridCellWidth) {
    this.gridCellWidth = gridCellWidth;
  }

  public void setGridHeight(final int gridHeight) {
    this.gridHeight = gridHeight;
  }

  public void setGridWidth(final int gridWidth) {
    this.gridWidth = gridWidth;
  }

  protected void setModified(final boolean modified) {
    this.modified = modified;
  }

  public void setResource(final Resource resource) {
    this.resource = resource;
  }

  protected void setValueRange(final double minValue, final double maxValue) {
    final double oldMinZ = this.bounds[2];
    if (minValue < oldMinZ || !Double.isFinite(oldMinZ)) {
      this.bounds[2] = minValue;
    }
    final double oldMaxZ = this.bounds[5];
    if (maxValue < oldMaxZ || !Double.isFinite(oldMaxZ)) {
      this.bounds[5] = maxValue;
    }
  }

  @Override
  public void setValuesForTriangle(final double x1, final double y1, final double z1,
    final double x2, final double y2, final double z2, final double x3, final double y3,
    final double z3) {
    if (Double.isFinite(z1) && Double.isFinite(z2) && Double.isFinite(z3)) {
      double minX = x1;
      double maxX = x1;
      if (x2 < minX) {
        minX = x2;
      } else if (x2 > maxX) {
        maxX = x2;
      }
      if (x3 < minX) {
        minX = x3;
      } else if (x3 > maxX) {
        maxX = x3;
      }

      double minY = y1;
      double maxY = y1;
      if (y2 < minY) {
        minY = y2;
      } else if (y2 > maxY) {
        maxY = y2;
      }
      if (y3 < minY) {
        minY = y3;
      } else if (y3 > maxY) {
        maxY = y3;
      }
      final double gridCellWidth = this.gridCellWidth;
      final double gridCellHeight = this.gridCellHeight;
      final double[] bounds = this.bounds;
      final double gridMinX = bounds[0];
      final double gridMaxX = bounds[3];
      final double startX;
      if (maxX <= gridMinX) {
        return;
      } else if (minX >= gridMaxX) {
        return;
      } else if (minX < gridMinX) {
        startX = gridMinX;
      } else {
        startX = Math.ceil(minX / gridCellWidth) * gridCellWidth;
      }
      if (maxX > gridMaxX) {
        maxX = gridMaxX;
      }
      final double gridMinY = bounds[1];
      final double gridMaxY = bounds[4];
      final double startY;
      if (maxY <= gridMinY) {
        return;
      } else if (minY >= gridMaxY) {
        return;
      } else if (minY < gridMinY) {
        startY = gridMinY;
      } else {
        startY = Math.ceil(minY / gridCellHeight) * gridCellHeight;
      }
      if (maxY > gridMaxY) {
        maxY = gridMaxY;
      }
      final double x1x3 = x1 - x3;
      final double x3x2 = x3 - x2;
      final double y1y3 = y1 - y3;
      final double y2y3 = y2 - y3;
      final double y3y1 = y3 - y1;
      final double det = y2y3 * x1x3 + x3x2 * y1y3;

      for (double y = startY; y < maxY; y += gridCellHeight) {
        final double yy3 = y - y3;
        for (double x = startX; x < maxX; x += gridCellWidth) {
          final double xx3 = x - x3;
          final double lambda1 = (y2y3 * xx3 + x3x2 * yy3) / det;
          if (0 <= lambda1 && lambda1 <= 1) {
            final double lambda2 = (y3y1 * xx3 + x1x3 * yy3) / det;
            if (0 <= lambda2 && lambda2 <= 1) {
              final double lambda3 = 1.0 - lambda1 - lambda2;
              if (0 < lambda3 && lambda3 < 1) {
                final double elevation = lambda1 * z1 + lambda2 * z2 + lambda3 * z3;
                if (Double.isFinite(elevation)) {
                  setValue(x, y, elevation);
                }
              }
            }
          }
        }
      }
    }
  }

  @Override
  public String toString() {
    return getBoundingBox() + " " + this.gridWidth + "x" + this.gridHeight + " c="
      + this.gridCellWidth + "x" + this.gridCellHeight;
  }

  @Override
  public void updateValues() {
    expandRange();
    this.boundingBox = this.geometryFactory.newBoundingBox(3, this.bounds);
  }

}
