package com.revolsys.record.io.format.kml;

import javax.xml.namespace.QName;

import org.jeometry.coordinatesystem.model.systems.EpsgId;

public interface Kml22Constants {
  String KML_NS_URI = "http://www.opengis.net/kml/2.2";

  QName ALTITUDE = new QName(KML_NS_URI, "altitude");

  QName ALTITUDE_MODE = new QName(KML_NS_URI, "altitudeMode");

  QName COLOR = new QName(KML_NS_URI, "color");

  QName COLOR_MODE = new QName(KML_NS_URI, "colorMode");

  int COORDINATE_SYSTEM_ID = EpsgId.WGS84;

  QName COORDINATES = new QName(KML_NS_URI, "coordinates");

  QName DATA = new QName(KML_NS_URI, "Data");

  QName DESCRIPTION = new QName(KML_NS_URI, "description");

  QName DOCUMENT = new QName(KML_NS_URI, "Document");

  String DOCUMENT_DESCRIPTION_PROPERTY = "kmlDocumentDescription";

  String DOCUMENT_NAME_PROPERTY = "kmlDocumentName";

  double EARTH_RADIUS = 6378137;

  QName EAST = new QName(KML_NS_URI, "east");

  QName EXTENDED_DATA = new QName(KML_NS_URI, "ExtendedData");

  QName FILL = new QName(KML_NS_URI, "fill");

  QName FOLDER = new QName(KML_NS_URI, "Folder");

  QName GROUND_OVERLAY = new QName(KML_NS_URI, "GroundOverlay");

  QName HEADING = new QName(KML_NS_URI, "heading");

  QName HREF = new QName(KML_NS_URI, "href");

  QName ICON = new QName(KML_NS_URI, "Icon");

  QName ICON_STYLE = new QName(KML_NS_URI, "IconStyle");

  QName INNER_BOUNDARY_IS = new QName(KML_NS_URI, "innerBoundaryIs");

  QName KML = new QName(KML_NS_URI, "kml");

  String KML_FILE_EXTENSION = "kml";

  String KML_FORMAT_DESCRIPTION = "KML - Google Earth";

  String KML_MEDIA_TYPE = "application/vnd.google-earth.kml+xml";

  String KMZ_FILE_EXTENSION = "kmz";

  String KMZ_FORMAT_DESCRIPTION = "KMZ - Google Earth";

  String KMZ_MEDIA_TYPE = "application/vnd.google-earth.kmz";

  QName LABEL_STYLE = new QName(KML_NS_URI, "LabelStyle");

  QName LAT_LON_ALT_BOX = new QName(KML_NS_URI, "LatLonAltBox");

  QName LAT_LON_BOX = new QName(KML_NS_URI, "LatLonBox");

  QName LATITUDE = new QName(KML_NS_URI, "latitude");

  QName LINE_STRING = new QName(KML_NS_URI, "LineString");

  QName LINEAR_RING = new QName(KML_NS_URI, "LinearRing");

  QName LINK = new QName(KML_NS_URI, "Link");

  QName LOD = new QName(KML_NS_URI, "Lod");

  QName LONGITUDE = new QName(KML_NS_URI, "longitude");

  QName LOOK_AT = new QName(KML_NS_URI, "LookAt");

  String LOOK_AT_MAX_RANGE_PROPERTY = "kmlLookAtMaxRange";

  String LOOK_AT_MIN_RANGE_PROPERTY = "kmlLookAtMinRange";

  String LOOK_AT_POINT_PROPERTY = "kmlLookAtPoint";

  String LOOK_AT_RANGE_PROPERTY = "kmlLookAtRange";

  QName MAX_LINES = new QName("maxLines");

  QName MAX_LOD_PIXELS = new QName(KML_NS_URI, "maxLodPixels");

  QName MIN_LOD_PIXELS = new QName(KML_NS_URI, "minLodPixels");

  QName MULTI_GEOMETRY = new QName(KML_NS_URI, "MultiGeometry");

  QName NAME = new QName(KML_NS_URI, "name");

  QName NETWORK_LINK = new QName(KML_NS_URI, "NetworkLink");

  QName NORTH = new QName(KML_NS_URI, "north");

  QName OPEN = new QName(KML_NS_URI, "open");

  QName OUTER_BOUNDARY_IS = new QName(KML_NS_URI, "outerBoundaryIs");

  QName PLACEMARK = new QName(KML_NS_URI, "Placemark");

  String PLACEMARK_DESCRIPTION_PROPERTY = "kmlPlacemarkDescription";

  String PLACEMARK_NAME_ATTRIBUTE_PROPERTY = "kmlPlaceMarkNameAttribute";

  QName POINT = new QName(KML_NS_URI, "Point");

  QName POLY_STYLE = new QName(KML_NS_URI, "PolyStyle");

  QName POLYGON = new QName(KML_NS_URI, "Polygon");

  QName RANGE = new QName(KML_NS_URI, "range");

  QName REGION = new QName(KML_NS_URI, "Region");

  QName SCALE = new QName(KML_NS_URI, "scale");

  QName SNIPPET = new QName(KML_NS_URI, "Snippet");

  String SNIPPET_PROPERTY = "kmlSnippet";

  QName SOUTH = new QName(KML_NS_URI, "south");

  QName STYLE = new QName(KML_NS_URI, "Style");

  String STYLE_PROPERTY = "kmlStyle";

  QName STYLE_URL = new QName(KML_NS_URI, "styleUrl");

  String STYLE_URL_PROPERTY = "kmlStyleUrl";

  QName TILT = new QName(KML_NS_URI, "tilt");

  QName VALUE = new QName(KML_NS_URI, "value");

  QName VIEW_REFRESH_MODE = new QName(KML_NS_URI, "viewRefreshMode");

  QName VISIBLITY = new QName(KML_NS_URI, "visibility");

  QName WEST = new QName(KML_NS_URI, "west");
}
