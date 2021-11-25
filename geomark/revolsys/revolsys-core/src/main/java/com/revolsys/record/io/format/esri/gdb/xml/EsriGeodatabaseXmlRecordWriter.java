package com.revolsys.record.io.format.esri.gdb.xml;

import java.io.Writer;
import java.sql.Timestamp;
import java.util.UUID;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.date.Dates;
import org.jeometry.common.logging.Logs;
import org.jeometry.common.number.Doubles;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.PathUtil;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.type.EsriGeodatabaseXmlFieldType;
import com.revolsys.record.io.format.esri.gdb.xml.type.EsriGeodatabaseXmlFieldTypeRegistry;
import com.revolsys.record.io.format.xml.XmlConstants;
import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.record.io.format.xml.XsiConstants;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;

public class EsriGeodatabaseXmlRecordWriter extends AbstractRecordWriter
  implements EsriGeodatabaseXmlConstants {
  private int datasetId = 3;

  private String datasetType;

  private final EsriGeodatabaseXmlFieldTypeRegistry fieldTypes = EsriGeodatabaseXmlFieldTypeRegistry.INSTANCE;

  private String geometryType;

  private int objectId = 1;

  private boolean opened;

  private final XmlWriter out;

  public EsriGeodatabaseXmlRecordWriter(final RecordDefinitionProxy recordDefinition,
    final Writer out) {
    super(recordDefinition);
    this.out = new XmlWriter(out);
  }

  @Override
  public void close() {
    if (!this.opened) {
      writeHeader(null);
    }

    writeFooter();
    this.out.close();
  }

  @Override
  public void flush() {
    this.out.flush();
  }

  @Override
  public void write(final Record record) {
    if (!this.opened) {
      writeHeader(record.getGeometry());
      writeWorkspaceDataHeader();
    }
    this.out.startTag(RECORD);
    this.out.attribute(XsiConstants.TYPE, RECORD_TYPE);

    this.out.startTag(VALUES);
    this.out.attribute(XsiConstants.TYPE, VALUES_TYPE);

    for (final FieldDefinition field : getFieldDefinitions()) {
      final String fieldName = field.getName();
      final Object value;
      if (isWriteCodeValues()) {
        value = record.getValue(fieldName);
      } else {
        value = record.getCodeValue(fieldName);
      }
      final DataType type = field.getDataType();
      final EsriGeodatabaseXmlFieldType fieldType = this.fieldTypes.getFieldType(type);
      if (fieldType != null) {
        fieldType.writeValue(this.out, value);
      }
    }
    if (!hasField("OBJECTID")) {
      final EsriGeodatabaseXmlFieldType fieldType = this.fieldTypes.getFieldType(DataTypes.INT);
      fieldType.writeValue(this.out, this.objectId++);
    }

    this.out.endTag(VALUES);

    this.out.endTag(RECORD);
  }

  private void writeDataElement(final RecordDefinition recordDefinition, final Geometry geometry) {
    final String dataElementType;
    final FieldDefinition geometryField = recordDefinition.getGeometryField();
    boolean hasGeometry = false;
    DataType geometryDataType = null;
    if (geometryField != null) {
      geometryDataType = geometryField.getDataType();
      if (this.fieldTypes.getFieldType(geometryDataType) != null) {
        hasGeometry = true;

        if (geometryDataType.equals(GeometryDataTypes.POINT)) {
          this.geometryType = GEOMETRY_TYPE_POINT;
        } else if (geometryDataType.equals(GeometryDataTypes.MULTI_POINT)) {
          this.geometryType = GEOMETRY_TYPE_MULTI_POINT;
        } else if (geometryDataType.equals(GeometryDataTypes.LINE_STRING)) {
          this.geometryType = GEOMETRY_TYPE_POLYLINE;
        } else if (geometryDataType.equals(GeometryDataTypes.MULTI_LINE_STRING)) {
          this.geometryType = GEOMETRY_TYPE_POLYLINE;
        } else if (geometryDataType.equals(GeometryDataTypes.POLYGON)) {
          this.geometryType = GEOMETRY_TYPE_POLYGON;
        } else {
          if (geometry instanceof Point) {
            this.geometryType = GEOMETRY_TYPE_POINT;
          } else if (geometry instanceof Punctual) {
            this.geometryType = GEOMETRY_TYPE_MULTI_POINT;
          } else if (geometry instanceof Lineal) {
            this.geometryType = GEOMETRY_TYPE_POLYLINE;
          } else if (geometry instanceof Polygon) {
            this.geometryType = GEOMETRY_TYPE_POLYGON;
          } else {
            hasGeometry = false;
          }
        }
      }
    }

    if (hasGeometry) {
      dataElementType = DATA_ELEMENT_FEATURE_CLASS;
      this.datasetType = DATASET_TYPE_FEATURE_CLASS;
    } else {
      dataElementType = DATA_ELEMENT_TABLE;
      this.datasetType = DATASET_TYPE_TABLE;
    }

    this.out.startTag(DATA_ELEMENT);
    this.out.attribute(XsiConstants.TYPE, dataElementType);

    final String path = recordDefinition.getPath();
    final String localName = PathUtil.getName(path);
    this.out.element(CATALOG_PATH, "/FC=" + localName);
    this.out.element(NAME, localName);
    this.out.element(METADATA_RETRIEVED, true);

    this.out.startTag(METADATA);
    this.out.attribute(XsiConstants.TYPE, XML_PROPERTY_SET_TYPE);
    this.out.startTag(XML_DOC);
    this.out.text("<?xml version=\"1.0\"?>");
    this.out.text("<metadata xml:lang=\"en\">");
    this.out.text("<Esri>");
    this.out.text("<MetaID>{");
    this.out.text(UUID.randomUUID().toString().toUpperCase());
    this.out.text("}</MetaID>");
    this.out.text("<CreaDate>");
    final Timestamp date = new Timestamp(System.currentTimeMillis());
    this.out.text(Dates.format("yyyyMMdd", date));
    this.out.text("</CreaDate>");
    this.out.text("<CreaTime>");
    this.out.text(Dates.format("HHmmssSS", date));
    this.out.text("</CreaTime>");
    this.out.text("<SyncOnce>TRUE</SyncOnce>");
    this.out.text("</Esri>");
    this.out.text("</metadata>");
    this.out.endTag(XML_DOC);
    this.out.endTag(METADATA);

    this.out.element(DATASET_TYPE, this.datasetType);
    this.out.element(DSID, this.datasetId++);
    this.out.element(VERSIONED, false);
    this.out.element(CAN_VERSION, false);
    this.out.element(HAS_OID, true);
    this.out.element(OBJECT_ID_FIELD_NAME, "OBJECTID");
    writeFields(recordDefinition);

    this.out.element(CLSID, "{52353152-891A-11D0-BEC6-00805F7C4268}");
    this.out.emptyTag(EXTCLSID);
    this.out.startTag(RELATIONSHIP_CLASS_NAMES);
    this.out.attribute(XsiConstants.TYPE, NAMES_TYPE);
    this.out.endTag(RELATIONSHIP_CLASS_NAMES);
    this.out.element(ALIAS_NAME, localName);
    this.out.emptyTag(MODEL_NAME);
    this.out.element(HAS_GLOBAL_ID, false);
    this.out.emptyTag(GLOBAL_ID_FIELD_NAME);
    this.out.emptyTag(RASTER_FIELD_NAME);
    this.out.startTag(EXTENSION_PROPERTIES);
    this.out.attribute(XsiConstants.TYPE, PROPERTY_SET_TYPE);
    this.out.startTag(PROPERTY_ARRAY);
    this.out.attribute(XsiConstants.TYPE, PROPERTY_ARRAY_TYPE);
    this.out.endTag(PROPERTY_ARRAY);
    this.out.endTag(EXTENSION_PROPERTIES);
    this.out.startTag(CONTROLLER_MEMBERSHIPS);
    this.out.attribute(XsiConstants.TYPE, CONTROLLER_MEMBERSHIPS_TYPE);
    this.out.endTag(CONTROLLER_MEMBERSHIPS);
    if (hasGeometry) {
      this.out.element(FEATURE_TYPE, FEATURE_TYPE_SIMPLE);
      this.out.element(SHAPE_TYPE, this.geometryType);
      this.out.element(SHAPE_FIELD_NAME, geometryField.getName());
      final GeometryFactory geometryFactory = geometryField.getGeometryFactory();
      this.out.element(HAS_M, false);
      this.out.element(HAS_Z, geometryFactory.hasZ());
      this.out.element(HAS_SPATIAL_INDEX, false);
      this.out.emptyTag(AREA_FIELD_NAME);
      this.out.emptyTag(LENGTH_FIELD_NAME);

      writeExtent(geometryFactory);
      writeSpatialReference(geometryFactory);
    }

    this.out.endTag(DATA_ELEMENT);
  }

  public void writeExtent(final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      if (geometryFactory.isHasHorizontalCoordinateSystem()) {
        final BoundingBox boundingBox = geometryFactory.getAreaBoundingBox();
        this.out.startTag(EXTENT);
        this.out.attribute(XsiConstants.TYPE, ENVELOPE_N_TYPE);
        this.out.element(X_MIN, boundingBox.getMinX());
        this.out.element(Y_MIN, boundingBox.getMinY());
        this.out.element(X_MAX, boundingBox.getMaxX());
        this.out.element(Y_MAX, boundingBox.getMaxY());
        this.out.element(Z_MIN, boundingBox.getMin(2));
        this.out.element(Z_MAX, boundingBox.getMax(2));
        writeSpatialReference(geometryFactory);
        this.out.endTag(EXTENT);
      }
    }
  }

  private void writeField(final FieldDefinition field) {
    final String fieldName = field.getName();
    if (fieldName.equals("OBJECTID")) {
      writeOidField();
    } else {
      final DataType dataType = field.getDataType();
      final EsriGeodatabaseXmlFieldType fieldType = this.fieldTypes.getFieldType(dataType);
      if (fieldType == null) {
        Logs.error(this, "Data type not supported " + dataType);
      } else {
        this.out.startTag(FIELD);
        this.out.attribute(XsiConstants.TYPE, FIELD_TYPE);
        this.out.element(NAME, fieldName);
        this.out.element(TYPE, fieldType.getEsriFieldType());
        this.out.element(IS_NULLABLE, !field.isRequired());
        int length = fieldType.getFixedLength();
        if (length < 0) {
          length = field.getLength();
        }
        this.out.element(LENGTH, length);
        final int precision;
        if (fieldType.isUsePrecision()) {
          precision = field.getLength();
        } else {
          precision = 0;
        }
        this.out.element(PRECISION, precision);
        this.out.element(SCALE, field.getScale());

        final GeometryFactory geometryFactory = field.getGeometryFactory();
        if (geometryFactory != null) {
          this.out.startTag(GEOMETRY_DEF);
          this.out.attribute(XsiConstants.TYPE, GEOMETRY_DEF_TYPE);

          this.out.element(AVG_NUM_POINTS, 0);

          this.out.element(GEOMETRY_TYPE, this.geometryType);
          this.out.element(HAS_M, false);
          this.out.element(HAS_Z, geometryFactory.hasZ());

          writeSpatialReference(geometryFactory);

          this.out.endTag();
        }
        this.out.endTag(FIELD);
      }
    }
  }

  private void writeFields(final RecordDefinition recordDefinition) {
    this.out.startTag(FIELDS);
    this.out.attribute(XsiConstants.TYPE, FIELDS_TYPE);

    this.out.startTag(FIELD_ARRAY);
    this.out.attribute(XsiConstants.TYPE, FIELD_ARRAY_TYPE);

    for (final FieldDefinition attribute : recordDefinition.getFields()) {
      writeField(attribute);
    }
    if (recordDefinition.getField("OBJECTID") == null) {
      writeOidField();
    }
    this.out.endTag(FIELD_ARRAY);

    this.out.endTag(FIELDS);
  }

  public void writeFooter() {
    this.out.endDocument();
  }

  private void writeHeader(final Geometry geometry) {
    this.opened = true;
    this.out.startDocument("UTF-8", "1.0");
    this.out.startTag(WORKSPACE);
    this.out.setPrefix(XsiConstants.PREFIX, XsiConstants.NAMESPACE_URI);
    this.out.setPrefix(XmlConstants.XML_SCHEMA_NAMESPACE_PREFIX,
      XmlConstants.XML_SCHEMA_NAMESPACE_URI);

    this.out.startTag(WORKSPACE_DEFINITION);
    this.out.attribute(XsiConstants.TYPE, WORKSPACE_DEFINITION_TYPE);

    this.out.element(WORKSPACE_TYPE, "esriLocalDatabaseWorkspace");
    this.out.element(VERSION, "");

    this.out.startTag(DOMAINS);
    this.out.attribute(XsiConstants.TYPE, DOMAINS_TYPE);
    this.out.endTag(DOMAINS);

    this.out.startTag(DATASET_DEFINITIONS);
    this.out.attribute(XsiConstants.TYPE, DATASET_DEFINITIONS_TYPE);
    final RecordDefinition recordDefinition = getRecordDefinition();
    writeDataElement(recordDefinition, geometry);
    this.out.endTag(DATASET_DEFINITIONS);

    this.out.endTag(WORKSPACE_DEFINITION);
  }

  private void writeOidField() {
    this.out.startTag(FIELD);
    this.out.attribute(XsiConstants.TYPE, FIELD_TYPE);
    this.out.element(NAME, "OBJECTID");
    this.out.element(TYPE, FIELD_TYPE_OBJECT_ID);
    this.out.element(IS_NULLABLE, false);
    this.out.element(LENGTH, 4);
    this.out.element(PRECISION, 10);
    this.out.element(SCALE, 0);

    this.out.element(REQUIRED, true);
    this.out.element(EDIATBLE, false);
    this.out.endTag(FIELD);
  }

  public void writeSpatialReference(final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      if (geometryFactory.isHasHorizontalCoordinateSystem()) {
        final String wkt = geometryFactory.toWktCs();
        this.out.startTag(SPATIAL_REFERENCE);
        if (geometryFactory.isProjected()) {
          this.out.attribute(XsiConstants.TYPE, PROJECTED_COORDINATE_SYSTEM_TYPE);
        } else {
          this.out.attribute(XsiConstants.TYPE, GEOGRAPHIC_COORDINATE_SYSTEM_TYPE);
        }
        this.out.element(WKT, wkt);
        this.out.element(X_ORIGIN, 0);
        this.out.element(Y_ORIGIN, 0);
        final double scaleXy = geometryFactory.getScaleXY();
        this.out.element(XY_SCALE, (int)scaleXy);
        this.out.element(Z_ORIGIN, 0);
        final double scaleZ = geometryFactory.getScaleZ();
        this.out.element(Z_SCALE, (int)scaleZ);
        this.out.element(M_ORIGIN, 0);
        this.out.element(M_SCALE, 1);
        this.out.element(XY_TOLERANCE, Doubles.toString(1.0 / scaleXy * 2.0));
        this.out.element(Z_TOLERANCE, Doubles.toString(1.0 / scaleZ * 2.0));
        this.out.element(M_TOLERANCE, 1);
        this.out.element(HIGH_PRECISION, true);
        this.out.element(WKID, geometryFactory.getHorizontalCoordinateSystemId());
        this.out.endTag(SPATIAL_REFERENCE);
      }
    }
  }

  public void writeWorkspaceDataHeader() {
    this.out.startTag(WORKSPACE_DATA);
    this.out.attribute(XsiConstants.TYPE, WORKSPACE_DATA_TYPE);

    this.out.startTag(DATASET_DATA);
    this.out.attribute(XsiConstants.TYPE, DATASET_DATA_TABLE_DATA);

    final RecordDefinition recordDefinition = getRecordDefinition();
    this.out.element(DATASET_NAME, recordDefinition.getName());
    this.out.element(DATASET_TYPE, this.datasetType);

    this.out.startTag(DATA);
    this.out.attribute(XsiConstants.TYPE, DATA_RECORD_SET);

    writeFields(recordDefinition);

    this.out.startTag(RECORDS);
    this.out.attribute(XsiConstants.TYPE, RECORDS_TYPE);

  }

}
