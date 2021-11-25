package com.revolsys.record.io.format.gml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.io.FileUtil;
import com.revolsys.record.io.GeometryRecordReaderFactory;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.spring.resource.Resource;

public class Gml extends GeometryRecordReaderFactory implements RecordWriterFactory {
  public static final String _NS_PREFIX = "gml";

  public static final String _NS_URI = "http://www.opengis.net/gml";

  public static final QName BOUNDED_BY = new QName(_NS_URI, "boundedBy", _NS_PREFIX);

  public static final QName BOX = new QName(_NS_URI, "Box", _NS_PREFIX);

  public static final QName COORDINATES = new QName(_NS_URI, "coordinates", _NS_PREFIX);

  public static final QName DIMENSION = new QName("dimension");

  public static final QName ENVELOPE = new QName(_NS_URI, "Envelope", _NS_PREFIX);

  public static final QName FEATURE_COLLECTION = new QName(_NS_URI, "FeatureCollection",
    _NS_PREFIX);

  public static final QName FEATURE_MEMBER = new QName(_NS_URI, "featureMember", _NS_PREFIX);

  public static final QName GEOMETRY_MEMBER = new QName(_NS_URI, "geometryMember", _NS_PREFIX);

  public static final QName INNER_BOUNDARY_IS = new QName(_NS_URI, "innerBoundaryIs", _NS_PREFIX);

  public static final QName LINE_STRING = new QName(_NS_URI, "LineString", _NS_PREFIX);

  public static final QName LINE_STRING_MEMBER = new QName(_NS_URI, "lineStringMember", _NS_PREFIX);

  public static final QName LINEAR_RING = new QName(_NS_URI, "LinearRing", _NS_PREFIX);

  public static final QName LOWER_CORNER = new QName(_NS_URI, "lowerCorner", _NS_PREFIX);

  public static final QName MULTI_GEOMETRY = new QName(_NS_URI, "MultiGeometry", _NS_PREFIX);

  public static final QName MULTI_LINE_STRING = new QName(_NS_URI, "MultiLineString", _NS_PREFIX);

  public static final QName MULTI_POINT = new QName(_NS_URI, "MultiPoint", _NS_PREFIX);

  public static final QName MULTI_POLYGON = new QName(_NS_URI, "MultiPolygon", _NS_PREFIX);

  public static final QName OUTER_BOUNDARY_IS = new QName(_NS_URI, "outerBoundaryIs", _NS_PREFIX);

  public static final QName POINT = new QName(_NS_URI, "Point", _NS_PREFIX);

  public static final QName POINT_MEMBER = new QName(_NS_URI, "pointMember", _NS_PREFIX);

  public static final QName POLYGON = new QName(_NS_URI, "Polygon", _NS_PREFIX);

  public static final QName POLYGON_MEMBER = new QName(_NS_URI, "polygonMember", _NS_PREFIX);

  public static final QName POS = new QName(_NS_URI, "pos", _NS_PREFIX);

  public static final QName POS_LIST = new QName(_NS_URI, "posList", _NS_PREFIX);

  public static final QName SRS_NAME = new QName("srsName");

  public static final QName UPPER_CORNER = new QName(_NS_URI, "upperCorner", _NS_PREFIX);

  public static final String VERSION_PROPERTY = "java:" + Gml.class.getName() + ".version";

  public static final Set<QName> ENVELOPE_AND_GEOMETRY_TYPE_NAMES = new LinkedHashSet<>(
    Arrays.asList(ENVELOPE, POINT, LINE_STRING, POLYGON, MULTI_POINT, MULTI_LINE_STRING,
      MULTI_POLYGON, MULTI_GEOMETRY));

  public static final Set<QName> GEOMETRY_TYPE_NAMES = new LinkedHashSet<>(Arrays.asList(POINT,
    LINE_STRING, POLYGON, MULTI_POINT, MULTI_LINE_STRING, MULTI_POLYGON, MULTI_GEOMETRY));

  public Gml() {
    super("Geography Markup Language");
    addMediaTypeAndFileExtension("application/gml+xml", "gml");
  }

  @Override
  public boolean isReadFromZipFileSupported() {
    return true;
  }

  @Override
  public GeometryReader newGeometryReader(final Resource resource, final MapEx properties) {
    return new GmlGeometryReader(resource, properties);
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinitionProxy recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = FileUtil.newUtf8Writer(outputStream);
    return new GmlRecordWriter(recordDefinition, writer);
  }
}
