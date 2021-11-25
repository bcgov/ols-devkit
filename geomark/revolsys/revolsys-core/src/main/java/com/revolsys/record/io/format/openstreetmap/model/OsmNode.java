package com.revolsys.record.io.format.openstreetmap.model;

import java.util.Date;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jeometry.common.data.identifier.Identifier;

import com.revolsys.record.io.format.xml.StaxReader;

public class OsmNode extends OsmElement {

  public OsmNode() {
  }

  public OsmNode(final long id, final boolean visible, final int version, final long changeset,
    final Date timestamp, final String user, final int uid, final Map<String, String> tags,
    final double x, final double y) {
    super(id, visible, version, changeset, timestamp, user, uid, tags);
    setGeometryValue(OsmConstants.WGS84_2D.point(x, y));
  }

  public OsmNode(final OsmElement element) {
    super(element);
  }

  public OsmNode(final StaxReader in) {
    super(in);
    final double lon = in.getDoubleAttribute(null, "lon");
    final double lat = in.getDoubleAttribute(null, "lat");
    setGeometryValue(OsmConstants.WGS84_2D.point(lon, lat));
    while (in.skipToChildStartElements(NODE_XML_ELEMENTS)) {
      final QName name = in.getName();
      if (name.equals(TAG)) {
        parseTag(in);
      } else {
        in.skipSubTree();
      }
    }
  }

  @Override
  public Identifier getIdentifier() {
    final long id = getId();
    return new OsmNodeIdentifier(id);
  }

}
