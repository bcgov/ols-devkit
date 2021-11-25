package com.revolsys.record.io.format.openstreetmap.model;

import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;

import org.jeometry.coordinatesystem.model.systems.EpsgId;

import com.revolsys.geometry.model.GeometryFactory;

public interface OsmConstants {
  QName BOUNDS = new QName("bounds");

  QName ND = new QName("nd");

  QName NODE = new QName("node");

  String OSM = "osm";

  QName RELATION = new QName("relation");

  QName TAG = new QName("tag");

  QName WAY = new QName("way");

  GeometryFactory WGS84_2D = GeometryFactory.floating2d(EpsgId.WGS84);

  List<QName> NODE_XML_ELEMENTS = Arrays.asList(TAG);

  List<QName> OSM_XML_ELEMENTS = Arrays.asList(BOUNDS, NODE, WAY, RELATION);

  List<QName> RELATION_XML_ELEMENTS = Arrays.asList(TAG);

  List<QName> WAY_XML_ELEMENTS = Arrays.asList(TAG, ND);
}
