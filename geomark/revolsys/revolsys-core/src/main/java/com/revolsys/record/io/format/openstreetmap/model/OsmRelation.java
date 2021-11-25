package com.revolsys.record.io.format.openstreetmap.model;

import javax.xml.namespace.QName;

import org.jeometry.common.data.identifier.Identifier;

import com.revolsys.record.io.format.xml.StaxReader;

public class OsmRelation extends OsmElement {

  public OsmRelation() {
  }

  public OsmRelation(final OsmElement element) {
    super(element);
  }

  public OsmRelation(final StaxReader in) {
    super(in);
    while (in.skipToChildStartElements(RELATION_XML_ELEMENTS)) {
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
    return new OsmRelationIdentifier(id);
  }

}
