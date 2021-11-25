package com.revolsys.record.io.format.saif.util;

import java.io.IOException;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.editor.LineStringEditor;
import com.revolsys.geometry.model.impl.LineStringDoubleGf;
import com.revolsys.record.io.format.saif.SaifConstants;
import com.revolsys.record.io.format.saif.geometry.ArcDirectedLineString;
import com.revolsys.record.io.format.saif.geometry.ArcLineString;

public class ArcDirectedConverter extends ArcConverter {

  public ArcDirectedConverter(final GeometryFactory geometryFactory) {
    super(geometryFactory, SaifConstants.ARC_DIRECTED);
  }

  @Override
  public LineString newLineString(final GeometryFactory geometryFactory,
    final LineStringEditor line) {
    final int axisCount = geometryFactory.getAxisCount();
    final int vertexCount = line.getVertexCount();
    final double[] coordinates = LineStringDoubleGf.getNewCoordinates(geometryFactory, line);
    return new ArcDirectedLineString(geometryFactory, axisCount, vertexCount, coordinates);
  }

  @Override
  protected void writeAttributes(final OsnSerializer serializer, final ArcLineString line)
    throws IOException {
    if (line instanceof ArcDirectedLineString) {
      final ArcDirectedLineString dirLine = (ArcDirectedLineString)line;
      final String flowDirection = dirLine.getFlowDirection();
      attributeEnum(serializer, "flowDirection", flowDirection);

    }
    super.writeAttributes(serializer, line);
  }
}
