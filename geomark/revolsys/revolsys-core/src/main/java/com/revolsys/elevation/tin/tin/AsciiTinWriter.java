package com.revolsys.elevation.tin.tin;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.elevation.tin.BaseCompactTriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkWriter;
import com.revolsys.geometry.model.Point;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;

public class AsciiTinWriter extends BaseObjectWithProperties
  implements TriangulatedIrregularNetworkWriter {

  private final PrintWriter out;

  private int tinIndex = 0;

  public AsciiTinWriter(final Resource resource) {
    this.out = resource.newPrintWriter();
    this.out.println("TIN");
  }

  @Override
  public void close() {
    this.out.close();
  }

  @Override
  public void flush() {
    this.out.flush();
  }

  @Override
  public void open() {
  }

  @Override
  public void write(final TriangulatedIrregularNetwork tin) {
    this.out.println("BEGT");

    this.out.print("TNAM tin-");
    this.out.println(++this.tinIndex);

    this.out.println("TCOL 255 255 255");

    final Map<Point, Integer> nodeMap = new HashMap<>();
    this.out.print("VERT ");
    final int vertexCount = tin.getVertexCount();
    this.out.println(vertexCount);
    final boolean isCompactTin = nodeMap instanceof BaseCompactTriangulatedIrregularNetwork;
    if (isCompactTin) {
      tin.forEachVertex((point) -> {
        this.out.print(point.getX());
        this.out.print(' ');
        this.out.print(point.getY());
        this.out.print(' ');
        this.out.println(point.getZ());
      });
    } else {
      tin.forEachVertex((point) -> {
        final int vertexIndex = nodeMap.size();
        nodeMap.put(point, vertexIndex);
        this.out.print(point.getX());
        this.out.print(' ');
        this.out.print(point.getY());
        this.out.print(' ');
        this.out.println(point.getZ());
      });
    }

    this.out.print("TRI ");
    final int triangleCount = tin.getTriangleCount();
    this.out.println(triangleCount);
    if (tin instanceof BaseCompactTriangulatedIrregularNetwork) {
      final BaseCompactTriangulatedIrregularNetwork compactTin = (BaseCompactTriangulatedIrregularNetwork)tin;
      for (int triangleIndex = 0; triangleIndex < triangleCount; triangleIndex++) {
        for (int j = 0; j < 3; j++) {
          if (j > 0) {
            this.out.print(' ');
          }
          final int index = compactTin.getTriangleVertexIndex(triangleIndex, j);
          this.out.print(index + 1);
        }
        this.out.println();
      }
    } else {
      tin.forEachTriangle((triangle) -> {
        for (int i = 0; i < 3; i++) {
          if (i > 0) {
            this.out.print(' ');
          }
          final Point point = triangle.getPoint(i);
          final Integer index = nodeMap.get(point);
          if (index == null) {
            throw new NullPointerException();
          }
          this.out.print(index + 1);
        }
        this.out.println();
      });
    }

    this.out.println("ENDT");
  }
}
