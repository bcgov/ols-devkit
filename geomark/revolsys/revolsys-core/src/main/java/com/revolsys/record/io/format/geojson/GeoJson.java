package com.revolsys.record.io.format.geojson;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.jeometry.coordinatesystem.model.CoordinateSystem;
import org.jeometry.coordinatesystem.model.systems.EpsgCoordinateSystems;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.io.FileUtil;
import com.revolsys.record.io.GeometryRecordReaderFactory;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.spring.resource.Resource;

public class GeoJson extends GeometryRecordReaderFactory implements RecordWriterFactory {

  public static final String COORDINATES = "coordinates";

  public static final String CRS = "crs";

  public static final String EPSG = "EPSG:";

  public static final String FEATURE = "Feature";

  public static final String FEATURE_COLLECTION = "FeatureCollection";

  public static final String FEATURES = "features";

  public static final String GEOMETRIES = "geometries";

  public static final String GEOMETRY = "geometry";

  public static final String GEOMETRY_COLLECTION = "GeometryCollection";

  public static final String LINE_STRING = "LineString";

  public static final String MULTI_LINE_STRING = "MultiLineString";

  public static final String MULTI_POINT = "MultiPoint";

  public static final String MULTI_POLYGON = "MultiPolygon";

  public static final String NAME = "name";

  public static final String POINT = "Point";

  public static final String POLYGON = "Polygon";

  public static final String PROPERTIES = "properties";

  public static final String TYPE = "type";

  public static final String URN_OGC_DEF_CRS_EPSG = "urn:ogc:def:crs:EPSG::";

  public static final CoordinateSystem COORDINATE_SYSTEM = EpsgCoordinateSystems.wgs84();

  public static final Set<String> GEOMETRY_TYPE_NAMES = new LinkedHashSet<>(Arrays.asList(POINT,
    LINE_STRING, POLYGON, MULTI_POINT, MULTI_LINE_STRING, MULTI_POLYGON, GEOMETRY_COLLECTION));

  public static final Set<String> OBJECT_TYPE_NAMES = new TreeSet<>(
    Arrays.asList(FEATURE, FEATURE_COLLECTION, POINT, LINE_STRING, POLYGON, MULTI_POINT,
      MULTI_LINE_STRING, MULTI_POLYGON, GEOMETRY_COLLECTION));

  public GeoJson() {
    super("GeoJSON");
    addMediaTypeAndFileExtension("application/vnd.geo+json", "geojson");
    addMediaType("application/x-geo+json");
  }

  @Override
  public boolean isCoordinateSystemSupported(final CoordinateSystem coordinateSystem) {
    return COORDINATE_SYSTEM.equals(coordinateSystem);
  }

  @Override
  public boolean isReadFromZipFileSupported() {
    return true;
  }

  @Override
  public GeometryReader newGeometryReader(final Resource resource, final MapEx properties) {
    return new GeoJsonGeometryReader(resource, properties);
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinitionProxy recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = FileUtil.newUtf8Writer(outputStream);
    return new GeoJsonRecordWriter(writer, recordDefinition);
  }
}
