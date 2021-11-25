package ca.bc.gov.geomark.web.domain;

import java.util.List;

import javax.xml.namespace.QName;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.io.PathName;

import com.revolsys.collection.map.IntHashMap;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.record.property.RecordProperties;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;

public class GeomarkRecordDefinitions {

  public static final GeomarkRecordDefinitions BOUNDING_BOX = new GeomarkRecordDefinitions(
    "GeomarkBoundingBox", false, false);

  public static final GeomarkRecordDefinitions PART = new GeomarkRecordDefinitions("GeomarkPart",
    true, true);

  public static final GeomarkRecordDefinitions POINT = new GeomarkRecordDefinitions("GeomarkPoint",
    false, false);

  public static final GeomarkRecordDefinitions GEOMARK = new GeomarkRecordDefinitions("Geomark",
    true, false);

  public static final List<String> FIELD_NAMES = PART.getRecordDefinition(3005).getFieldNames();

  private final String name;

  private final boolean full;

  private final boolean hasParts;

  private final IntHashMap<RecordDefinition> recordDefinitionByCoordinateSystem = new IntHashMap<>();

  private GeomarkRecordDefinitions(final String name, final boolean full, final boolean hasParts) {
    this.name = name;
    this.full = full;
    this.hasParts = hasParts;
  }

  public String getName() {
    return this.name;
  }

  public RecordDefinition getRecordDefinition(final int coordinateSystemId) {
    RecordDefinition recordDefinition = this.recordDefinitionByCoordinateSystem
      .get(coordinateSystemId);
    if (recordDefinition == null) {
      recordDefinition = newRecordDefinition(coordinateSystemId);
      this.recordDefinitionByCoordinateSystem.put(coordinateSystemId, recordDefinition);
    }
    return recordDefinition;
  }

  private RecordDefinitionImpl newRecordDefinition(final int coordinateSystemId) {
    final RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl(
      PathName.newPathName("/" + this.name));
    recordDefinition.setProperty(RecordProperties.QUALIFIED_NAME,
      new QName("http://gov.bc.ca/geomark", this.name, "geomark"));

    recordDefinition.addField(new FieldDefinition("id", DataTypes.STRING, 50, true,
      "The unique identifier of the geomark"));
    recordDefinition.addField(new FieldDefinition("url", DataTypes.ANY_URI, 254, true,
      "The URL to the HTML page describing the geomark and supported output formats"));
    if (this.full) {
      recordDefinition.addField(new FieldDefinition("geometryType", DataTypes.STRING, 20, false,
        "The type of geometry contained in the geomark"));
      if (this.hasParts) {
        recordDefinition.addField(
          new FieldDefinition("partIndex", DataTypes.LONG, false, "The index of the geomark part"));
      }
      recordDefinition.addField(new FieldDefinition("numPolygons", DataTypes.INT, true,
        "The total number of geometry parts in the geomark"));
      recordDefinition.addField(new FieldDefinition("numParts", DataTypes.INT, true,
        "The total number of geometry parts in the geomark"));
      recordDefinition.addField(new FieldDefinition("createDate", DataTypes.SQL_DATE, true,
        "The date the geomark was created"));
      recordDefinition.addField(new FieldDefinition("expiryDate", DataTypes.SQL_DATE, false,
        "The date the geomark will expire and be deleted, or null if this geomark"
          + " is registered with a BC Government application and"
          + " will be kept as long as it remains registered."));
      recordDefinition.addField(new FieldDefinition("minX", DataTypes.DOUBLE, true,
        "The minimum x (east) of the geomark's bounding box"));
      recordDefinition.addField(new FieldDefinition("minY", DataTypes.DOUBLE, true,
        "The minimum y (south) of the geomark's bounding box"));
      recordDefinition.addField(new FieldDefinition("maxX", DataTypes.DOUBLE, true,
        "The maximum x (north) of the geomark's bounding box"));
      recordDefinition.addField(new FieldDefinition("maxY", DataTypes.DOUBLE, true,
        "The minimum y (north) of the geomark's bounding box"));
      recordDefinition.addField(new FieldDefinition("centroidX", DataTypes.DOUBLE, true,
        "The x coordinate of the centroid of the geomark's geometries"));
      recordDefinition.addField(new FieldDefinition("centroidY", DataTypes.DOUBLE, true,
        "The y coordinate of the centroid of the geomark's geometries"));
      recordDefinition.addField(new FieldDefinition("numVertices", DataTypes.INT, true,
        "The total number of vertices from the geomark's geometries (including holes)"));
      recordDefinition.addField(new FieldDefinition("length", DataTypes.DOUBLE, true,
        "The total length in metres of the geomark's geometries"));
      recordDefinition.addField(new FieldDefinition("area", DataTypes.LONG, true,
        "The total area in square metres of the geomark's geometries"));
      recordDefinition.addField(new FieldDefinition("isValid", DataTypes.BOOLEAN, true,
        "Flag indicating if the geometry is valid acording to the OGC simple feature specifications."));
      recordDefinition.addField(new FieldDefinition("validationError", DataTypes.BOOLEAN, false,
        "Description of the validation error if the geometry is valid acording to the OGC simple feature specifications."));
      recordDefinition.addField(new FieldDefinition("isSimple", DataTypes.BOOLEAN, true,
        "Flag indicating if the geometry is simple acording to the OGC simple feature specifications."));
      recordDefinition.addField(new FieldDefinition("isRobust", DataTypes.BOOLEAN, true,
        "Flag indicating if coordinates of the geometry are less than the tolerance away from the edges of the geometry."));
      recordDefinition.addField(new FieldDefinition("minimumClearance", DataTypes.DOUBLE, true,
        "The Minimum Clearance is a measure of what magnitude of perturbation of the vertices of a geometry can be tolerated before the geometry becomes topologically invalid."));
    }
    recordDefinition
      .addField(new FieldDefinition("geometry", GeometryDataTypes.GEOMETRY, true, "The geometry"));
    recordDefinition.setIdFieldIndex(0);
    recordDefinition.setGeometryFactory(
      GeomarkConfig.getConfig().getGeometryFactory().convertSrid(coordinateSystemId));
    return recordDefinition;
  }

}
