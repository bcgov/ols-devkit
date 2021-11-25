package com.revolsys.elevation.gridded.scaledint;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelWriter;
import com.revolsys.elevation.gridded.IntArrayScaleGriddedElevationModel;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.spring.resource.Resource;

public class ScaledIntegerGriddedDigitalElevationModelWriter
  extends AbstractWriter<GriddedElevationModel> implements GriddedElevationModelWriter {
  public static void writeHeader(final ChannelWriter writer, final BoundingBox boundingBox,
    final GeometryFactory geometryFactory, final int gridWidth, final int gridHeight,
    final double gridCellWidth, final double gridCellHeight) throws IOException {
    final int coordinateSystemId = geometryFactory.getHorizontalCoordinateSystemId();
    writer.putBytes(ScaledIntegerGriddedDigitalElevation.FILE_FORMAT_BYTES);
    writer.putShort(ScaledIntegerGriddedDigitalElevation.VERSION);
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

  ScaledIntegerGriddedDigitalElevationModelWriter(final Resource resource) {
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
      final String fileNameExtension = this.resource.getFileNameExtension();
      if ("zip".equals(fileNameExtension)
        || ScaledIntegerGriddedDigitalElevation.FILE_EXTENSION_ZIP.equals(fileNameExtension)) {
        try {
          final OutputStream bufferedOut = this.resource.newBufferedOutputStream();
          final String fileName = this.resource.getBaseName();
          final ZipOutputStream zipOut = new ZipOutputStream(bufferedOut);
          final ZipEntry zipEntry = new ZipEntry(fileName);
          zipOut.putNextEntry(zipEntry);
          final WritableByteChannel channel = Channels.newChannel(zipOut);
          this.writer = new ChannelWriter(channel, true, this.byteBuffer);
        } catch (final IOException e) {
          throw Exceptions.wrap("Error creating: " + this.resource, e);
        }
      } else if ("gz".equals(fileNameExtension)) {
        try {
          String fileName = this.resource.getBaseName();
          if (!fileName.endsWith("." + ScaledIntegerGriddedDigitalElevation.FILE_EXTENSION)) {
            fileName += "." + ScaledIntegerGriddedDigitalElevation.FILE_EXTENSION;
          }
          final OutputStream bufferedOut = this.resource.newBufferedOutputStream();
          final GZIPOutputStream zipOut = new GZIPOutputStream(bufferedOut);
          final WritableByteChannel channel = Channels.newChannel(zipOut);
          this.writer = new ChannelWriter(channel, true, this.byteBuffer);
        } catch (final IOException e) {
          throw Exceptions.wrap("Error creating: " + this.resource, e);
        }
      } else {
        this.writer = this.resource.newChannelWriter(this.byteBuffer);
      }
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
      if (elevationModel instanceof IntArrayScaleGriddedElevationModel) {
        final IntArrayScaleGriddedElevationModel scaleModel = (IntArrayScaleGriddedElevationModel)elevationModel;
        scaleModel.writeIntArray(this.writer);
      } else {
        writeGrid(elevationModel);
      }
    } catch (final IOException e) {
      Exceptions.throwUncheckedException(e);
    }
  }

  private void writeGrid(final GriddedElevationModel elevationModel) throws IOException {
    final ChannelWriter out = this.writer;
    final int gridWidth = this.gridWidth;
    final int gridHeight = this.gridHeight;
    final GeometryFactory geometryFactory = elevationModel.getGeometryFactory();
    for (int gridY = 0; gridY < gridHeight; gridY++) {
      for (int gridX = 0; gridX < gridWidth; gridX++) {
        final double elevation = elevationModel.getValue(gridX, gridY);
        final int zInt = geometryFactory.toIntZ(elevation);
        out.putInt(zInt);
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
