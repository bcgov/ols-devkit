package com.revolsys.record.io.format.openstreetmap.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jeometry.common.data.identifier.Identifier;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.record.io.format.xml.StaxReader;

public class OsmWay extends OsmElement {

  public OsmWay() {
  }

  public OsmWay(final long id, final boolean visible, final int version, final long changeset,
    final Date timestamp, final String user, final int uid, final Map<String, String> tags,
    final Geometry geometry) {
    super(id, visible, version, changeset, timestamp, user, uid, tags);
    setGeometryValue(geometry);
  }

  public OsmWay(final OsmDocument document, final StaxReader in) {
    super(in);
    final List<Point> points = new ArrayList<>();
    while (in.skipToChildStartElements(WAY_XML_ELEMENTS)) {
      final QName name = in.getName();
      if (name.equals(TAG)) {
        parseTag(in);
      } else if (name.equals(ND)) {
        parseNodRef(document, points, in);
      } else {
        in.skipSubTree();
      }
    }
    setGeometry(points);
  }

  public OsmWay(final OsmElement element) {
    super(element);
  }

  public OsmWay(final StaxReader in) {
    super(in);
  }

  @Override
  public Identifier getIdentifier() {
    final long id = getId();
    return new OsmWayIdentifier(id);
  }

  public boolean isArea() {
    if ("yes".equals(getString("area"))) {
      return true;
    } else if (Arrays
      .asList("bare_rock", "fell", "glacier, landuse=grass", "grassland", "heath", "mud", "scree",
        "sand", "scrub", "tree", "wetland", "wood")
      .contains(getTag("natural"))) {
      return true;
    }
    return false;
  }

  private void parseNodRef(final OsmDocument document, final List<Point> points,
    final StaxReader in) {
    final long nodeId = in.getLongAttribute(null, "ref");
    final Point point = document.getNodePoint(nodeId);
    if (point != null && !point.isEmpty()) {
      points.add(point);
    }
    in.skipToEndElement(ND);
  }

  protected void setGeometry(final List<Point> points) {
    Geometry geometry;
    if (points.isEmpty()) {
      geometry = OsmConstants.WGS84_2D.point();
    } else if (points.size() == 1) {
      geometry = points.get(0);
    } else {
      final LineString line = OsmConstants.WGS84_2D.lineString(points);
      if (isArea() && line.isClosed()) {
        geometry = OsmConstants.WGS84_2D.polygon(line);
      } else {
        geometry = line;
      }
    }
    setGeometryValue(geometry);
  }

}
