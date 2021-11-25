package com.revolsys.elevation.gridded.esriascii;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.number.Doubles;
import org.jeometry.common.number.Integers;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelWriter;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.spring.resource.Resource;

public class EsriAsciiGriddedElevationModelWriter extends AbstractWriter<GriddedElevationModel>
  implements GriddedElevationModelWriter {

  private Resource resource;

  private Writer writer;

  public EsriAsciiGriddedElevationModelWriter(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public void close() {
    super.close();
    flush();
    if (this.writer != null) {
      FileUtil.closeSilent(this.writer);
      this.writer = null;
    }
    this.resource = null;
  }

  @Override
  public void flush() {
    if (this.writer != null) {
      try {
        this.writer.flush();
      } catch (final IOException e) {
      }
    }
  }

  protected void open(final GriddedElevationModel model) {
    final GeometryFactory geometryFactory = model.getGeometryFactory();

    if (this.writer == null) {
      final String fileNameExtension = this.resource.getFileNameExtension();
      final OutputStream bufferedOut = this.resource.newBufferedOutputStream();
      if ("zip".equals(fileNameExtension)
        || EsriAsciiGriddedElevation.FILE_EXTENSION_ZIP.equals(fileNameExtension)) {
        try {
          final String fileName = this.resource.getBaseName();
          final ZipOutputStream zipOut = new ZipOutputStream(bufferedOut);
          final String prjString = geometryFactory.toWktCs();
          if (prjString.length() > 0) {
            final ZipEntry prjEntry = new ZipEntry(FileUtil.getBaseName(fileName) + ".prj");
            zipOut.putNextEntry(prjEntry);
            zipOut.write(prjString.getBytes(StandardCharsets.UTF_8));
          }
          final ZipEntry fileEntry = new ZipEntry(fileName);
          zipOut.putNextEntry(fileEntry);

          this.writer = new OutputStreamWriter(zipOut, StandardCharsets.UTF_8);
        } catch (final IOException e) {
          throw Exceptions.wrap("Error creating: " + this.resource, e);
        }
      } else if ("gz".equals(fileNameExtension)) {
        try {
          String fileName = this.resource.getBaseName();
          if (!fileName.endsWith("." + EsriAsciiGriddedElevation.FILE_EXTENSION)) {
            fileName += "." + EsriAsciiGriddedElevation.FILE_EXTENSION;
          }
          final GZIPOutputStream zipOut = new GZIPOutputStream(bufferedOut);
          this.writer = new OutputStreamWriter(zipOut, StandardCharsets.UTF_8);
        } catch (final IOException e) {
          throw Exceptions.wrap("Error creating: " + this.resource, e);
        }
      } else {
        geometryFactory.writePrjFile(this.resource);
        this.writer = this.resource.newWriter();
      }
    }
  }

  @Override
  public void write(final GriddedElevationModel model) {
    final String nodataValue = DataTypes.toString(model.getProperty("nodataValue", "-9999"));
    if (this.resource == null) {
      throw new IllegalStateException("Writer is closed");
    } else {
      open(model);
      try {
        final BoundingBox boundingBox = model.getBoundingBox();
        final int width = model.getGridWidth();
        final int height = model.getGridHeight();
        final double cellSize = model.getGridCellWidth();
        final double gridCellHeight = model.getGridCellHeight();
        if (cellSize != gridCellHeight) {
          throw new IllegalArgumentException("Can only write grids with square cells");
        }
        this.writer.write("NCOLS ");
        this.writer.write(Integers.toString(width));
        this.writer.write('\n');

        this.writer.write("NROWS ");
        this.writer.write(Integers.toString(height));
        this.writer.write('\n');

        this.writer.write("XLLCORNER ");
        this.writer.write(Doubles.toString(boundingBox.getMinX()));
        this.writer.write('\n');

        this.writer.write("YLLCORNER ");
        this.writer.write(Doubles.toString(boundingBox.getMinY()));
        this.writer.write('\n');

        this.writer.write("CELLSIZE ");
        this.writer.write(Doubles.toString(cellSize));
        this.writer.write('\n');

        this.writer.write("NODATA_VALUE ");
        this.writer.write(nodataValue);
        this.writer.write('\n');

        for (int gridY = height - 1; gridY >= 0; gridY--) {
          for (int gridX = 0; gridX < width; gridX++) {
            final double elevation = model.getValue(gridX, gridY);
            if (Double.isFinite(elevation)) {
              final String elevationString = Doubles.toString(elevation);
              this.writer.write(elevationString);
            } else {
              this.writer.write(nodataValue);
            }
            this.writer.write(' ');
          }
          this.writer.write('\n');
        }
        this.writer.write('\n');
      } catch (final Throwable e) {
        throw Exceptions.wrap("Unable to write to: " + this.resource, e);
      } finally {
        close();
      }
    }
  }
}
