package com.revolsys.elevation.tin.tin;

import java.io.BufferedReader;
import java.io.IOException;

import org.jeometry.common.number.Doubles;
import org.jeometry.common.number.Integers;

import com.revolsys.elevation.tin.CompactTriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.FileUtil;
import com.revolsys.spring.resource.Resource;

public class AsciiTinReader implements BaseCloseable {
  private final GeometryFactory geometryFactory;

  private final BufferedReader in;

  public AsciiTinReader(final GeometryFactory geometryFactory, final Resource resource) {
    this.geometryFactory = geometryFactory;
    this.in = resource.newBufferedReader();
    final String line = readLine();
    if (!"TIN".equals(line)) {
      throw new IllegalArgumentException("File does not contain a tin");
    }
  }

  @Override
  public void close() {
    FileUtil.closeSilent(this.in);
  }

  public TriangulatedIrregularNetwork read() {
    String line = readLine();
    if (!"BEGT".equals(line)) {
      throw new IllegalArgumentException("Expecting BEGT not " + line);
    }
    line = readLine();
    if (line.startsWith("TNAM")) {
      line = readLine();
    }
    if (line.startsWith("TCOL")) {
      line = readLine();
    }
    if (!line.startsWith("VERT ")) {
      throw new IllegalArgumentException("Expecting VERT not " + line);
    }

    final int vertexCount = Integer.parseInt(line.substring(5));
    final double[] vertexXCoordinates = new double[vertexCount];
    final double[] vertexYCoordinates = new double[vertexCount];
    final double[] vertexZCoordinates = new double[vertexCount];
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      line = readLine();
      final double[] coordinates = Doubles.toDoubleArraySplit(line, " ");
      vertexXCoordinates[vertexIndex] = coordinates[0];
      vertexYCoordinates[vertexIndex] = coordinates[1];
      vertexZCoordinates[vertexIndex] = coordinates[2];
    }
    line = readLine();

    int triangleCount = 0;
    int[] triangleVertex0Indices = null;
    int[] triangleVertex1Indices = null;
    int[] triangleVertex2Indices = null;
    if (line.startsWith("ENDT")) {
    } else {
      if (!line.startsWith("TRI ")) {
        throw new IllegalArgumentException("Expecting TRI not " + line);
      }

      triangleCount = Integer.parseInt(line.substring(4));
      triangleVertex0Indices = new int[triangleCount];
      triangleVertex1Indices = new int[triangleCount];
      triangleVertex2Indices = new int[triangleCount];
      for (int triangleIndex = 0; triangleIndex < triangleCount; triangleIndex++) {
        line = readLine();
        final int[] indexes = Integers.toIntArraySplit(line, " ");
        triangleVertex0Indices[triangleIndex] = indexes[0] - 1;
        triangleVertex1Indices[triangleIndex] = indexes[1] - 1;
        triangleVertex2Indices[triangleIndex] = indexes[2] - 1;
      }
    }
    if (triangleVertex0Indices == null) {
      throw new IllegalArgumentException("Not implemented");
    } else {
      return new CompactTriangulatedIrregularNetwork(this.geometryFactory, vertexCount,
        vertexXCoordinates, vertexYCoordinates, vertexZCoordinates, triangleCount,
        triangleVertex0Indices, triangleVertex1Indices, triangleVertex2Indices);
    }
  }

  private String readLine() {
    try {
      return this.in.readLine();
    } catch (final IOException e) {
      throw new RuntimeException("Unable to read line", e);
    }
  }
}
