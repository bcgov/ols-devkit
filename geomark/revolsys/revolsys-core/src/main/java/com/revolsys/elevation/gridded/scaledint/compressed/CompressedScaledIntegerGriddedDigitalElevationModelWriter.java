package com.revolsys.elevation.gridded.scaledint.compressed;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelWriter;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingInteger;
import com.revolsys.math.arithmeticcoding.ArithmeticEncoder;
import com.revolsys.spring.resource.Resource;

public class CompressedScaledIntegerGriddedDigitalElevationModelWriter
  extends AbstractWriter<GriddedElevationModel> implements GriddedElevationModelWriter {
  public static void writeHeader(final ChannelWriter writer, final BoundingBox boundingBox,
    final GeometryFactory geometryFactory, final int gridWidth, final int gridHeight,
    final double gridCellWidth, final double gridCellHeight) throws IOException {
    final int coordinateSystemId = geometryFactory.getHorizontalCoordinateSystemId();
    writer.putBytes(CompressedScaledIntegerGriddedDigitalElevation.FILE_FORMAT_BYTES);
    writer.putShort(CompressedScaledIntegerGriddedDigitalElevation.VERSION);
    writer.putShort((short)0); // Padding to make multiples of 8 bytes in header
    writer.putInt(coordinateSystemId);
    for (int axisIndex = 0; axisIndex < 3; axisIndex++) {
      final double offset = geometryFactory.getOffset(axisIndex);
      writer.putDouble(offset);
      final double scale = geometryFactory.getScale(axisIndex);
      if (scale <= 0) {
        writer.putDouble(1000);
      } else {
        writer.putDouble(scale);
      }
    }

    writer.putDouble(boundingBox.getMinX()); // minX
    writer.putDouble(boundingBox.getMinY()); // minY
    writer.putDouble(boundingBox.getMinZ()); // minZ
    writer.putDouble(boundingBox.getMaxX()); // maxX
    writer.putDouble(boundingBox.getMaxY()); // maxY
    writer.putDouble(boundingBox.getMaxZ()); // maxZ
    writer.putInt(gridWidth); // Grid Width
    writer.putInt(gridHeight); // Grid Height
    writer.putDouble(gridCellWidth); // Grid Cell Width
    writer.putDouble(gridCellHeight); // Grid Cell Height
  }

  private Resource resource;

  private ChannelWriter writer;

  private int gridWidth;

  private int gridHeight;

  private ByteBuffer byteBuffer;

  CompressedScaledIntegerGriddedDigitalElevationModelWriter(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public void close() {
    super.close();
    if (this.writer != null) {
      try {
        this.writer.close();
      } catch (final Throwable e) {
      } finally {
        this.writer = null;
      }
    }
    this.resource = null;
  }

  @Override
  public void open() {
    if (this.writer == null) {
      this.writer = this.resource.newChannelWriter(this.byteBuffer);
    }
  }

  public void setByteBuffer(final ByteBuffer buffer) {
    this.byteBuffer = buffer;
    if (buffer != null) {
      buffer.order(ByteOrder.BIG_ENDIAN);
    }
  }

  @Override
  public void write(final GriddedElevationModel elevationModel) {
    open();
    try {
      writeHeader(elevationModel);
      writeGrid(elevationModel);
    } catch (final IOException e) {
      Exceptions.throwUncheckedException(e);
    }
  }

  private void writeGrid(final GriddedElevationModel elevationModel) throws IOException {
    final ChannelWriter writer = this.writer;
    final int gridWidth = this.gridWidth;
    final int gridHeight = this.gridHeight;
    if (gridWidth > 0 && gridHeight > 0) {
      final GeometryFactory geometryFactory = elevationModel.getGeometryFactory();
      try (
        final ArithmeticEncoder encoder = new ArithmeticEncoder(writer)) {
        final ArithmeticCodingInteger compressor = encoder.newCodecInteger(32);

        final double minZ = elevationModel.getBoundingBox().getMinZ();
        final int minZInt = geometryFactory.toIntZ(minZ);
        final int nullInt = minZInt - 1;

        int previousZ = elevationModel.getValueInt(0, 0);
        if (previousZ == Integer.MIN_VALUE) {
          previousZ = nullInt;
        }
        writer.putInt(previousZ);
        boolean leftToRight = true;
        for (int gridY = 0; gridY < gridHeight; gridY++) {
          if (leftToRight) {
            int startX = 0;
            if (gridY == 0) {
              startX = 1;
            } else {
              startX = 0;
            }
            for (int gridX = startX; gridX < gridWidth; gridX++) {
              int zInt = elevationModel.getValueInt(gridX, gridY);
              if (zInt == Integer.MIN_VALUE) {
                zInt = nullInt;
              }
              compressor.compress(previousZ, zInt);
              previousZ = zInt;
            }
          } else {
            for (int gridX = gridWidth - 1; gridX >= 0; gridX--) {
              int zInt = elevationModel.getValueInt(gridX, gridY);
              if (zInt == Integer.MIN_VALUE) {
                zInt = nullInt;
              }
              compressor.compress(previousZ, zInt);
              previousZ = zInt;
            }
          }
          leftToRight = !leftToRight;
        }
      }
    }
  }

  private void writeHeader(final GriddedElevationModel elevationModel) throws IOException {
    final GeometryFactory geometryFactory = elevationModel.getGeometryFactory();
    elevationModel.updateValues();
    final BoundingBox boundingBox = elevationModel.getBoundingBox();
    this.gridWidth = elevationModel.getGridWidth();
    this.gridHeight = elevationModel.getGridHeight();
    final double gridCellWidth = elevationModel.getGridCellWidth();
    final double gridCellHeight = elevationModel.getGridCellHeight();

    writeHeader(this.writer, boundingBox, geometryFactory, this.gridWidth, this.gridHeight,
      gridCellWidth, gridCellHeight);
  }
}
