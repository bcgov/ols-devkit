package com.revolsys.record.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.io.PathName;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.format.csv.GeometryFieldDefinition;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.record.schema.RecordStoreSchema;
import com.revolsys.util.Property;

public abstract class AbstractRecordReader extends AbstractIterator<Record>
  implements RecordReader {
  private String geometryColumnName;

  private GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;

  private DataType geometryType = GeometryDataTypes.GEOMETRY;

  private boolean hasPointFields;

  private String pointXFieldName;

  private String pointYFieldName;

  private RecordDefinition recordDefinition;

  private RecordFactory<? extends Record> recordFactory;

  public AbstractRecordReader(final RecordFactory<? extends Record> recordFactory) {
    this.recordFactory = recordFactory;
  }

  @Override
  protected void closeDo() {
    this.recordFactory = null;
    this.geometryFactory = null;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public String getPointXFieldName() {
    return this.pointXFieldName;
  }

  public String getPointYFieldName() {
    return this.pointYFieldName;
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    open();
    return this.recordDefinition;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends Record> RecordFactory<R> getRecordFactory() {
    return (RecordFactory<R>)this.recordFactory;
  }

  @Override
  protected void initDo() {
    this.pointXFieldName = getProperty("pointXFieldName");
    this.pointYFieldName = getProperty("pointYFieldName");
    this.geometryColumnName = getProperty("geometryColumnName");

    this.geometryFactory = GeometryFactory.get(getProperty("geometryFactory"));
    if (this.geometryFactory == null || this.geometryFactory == GeometryFactory.DEFAULT_3D) {
      final Integer geometrySrid = Property.getInteger(this, "geometrySrid");
      if (geometrySrid == null) {
        this.geometryFactory = loadGeometryFactory();
      } else {
        this.geometryFactory = GeometryFactory.floating3d(geometrySrid);
      }
    }
    if (this.geometryFactory == null) {
      this.geometryFactory = GeometryFactory.DEFAULT_3D;
    }
    final DataType geometryType = DataTypes.getDataType((String)getProperty("geometryType"));
    if (Geometry.class.isAssignableFrom(geometryType.getJavaClass())) {
      this.geometryType = geometryType;
    }

  }

  public boolean isHasPointFields() {
    return this.hasPointFields;
  }

  protected GeometryFactory loadGeometryFactory() {
    return GeometryFactory.DEFAULT_3D;
  }

  protected Record newRecord() {
    return this.recordFactory.newRecord(this.recordDefinition);
  }

  protected RecordDefinition newRecordDefinition(final String baseName,
    final List<String> fieldNames) throws IOException {
    String geometryColumnName = this.geometryColumnName;
    this.hasPointFields = Property.hasValue(this.pointXFieldName)
      && Property.hasValue(this.pointYFieldName);
    if (this.hasPointFields) {

      this.geometryType = GeometryDataTypes.POINT;
    } else {
      this.pointXFieldName = null;
      this.pointYFieldName = null;
    }
    final List<FieldDefinition> fields = new ArrayList<>();
    FieldDefinition geometryField = null;
    for (final String fieldName : fieldNames) {
      if (fieldName != null) {
        DataType type;
        int length = 0;
        boolean isGeometryField = false;
        if (geometryColumnName != null && fieldName.equalsIgnoreCase(geometryColumnName)) {
          type = this.geometryType;
          isGeometryField = true;
        } else if ("GEOMETRY".equalsIgnoreCase(fieldName)) {
          type = GeometryDataTypes.GEOMETRY;
          isGeometryField = true;
        } else if ("SHAPE".equalsIgnoreCase(fieldName)) {
          type = GeometryDataTypes.GEOMETRY;
          isGeometryField = true;
        } else if ("GEOMETRYCOLLECTION".equalsIgnoreCase(fieldName)
          || "GEOMETRY_COLLECTION".equalsIgnoreCase(fieldName)) {
          type = GeometryDataTypes.GEOMETRY_COLLECTION;
          isGeometryField = true;
        } else if ("POINT".equalsIgnoreCase(fieldName)) {
          type = GeometryDataTypes.POINT;
          isGeometryField = true;
        } else if ("MULTI_POINT".equalsIgnoreCase(fieldName)
          || "MULTIPOINT".equalsIgnoreCase(fieldName)) {
          type = GeometryDataTypes.MULTI_POINT;
          isGeometryField = true;
        } else if ("LINE_STRING".equalsIgnoreCase(fieldName)
          || "LINESTRING".equalsIgnoreCase(fieldName) || "LINE".equalsIgnoreCase(fieldName)) {
          type = GeometryDataTypes.LINE_STRING;
          isGeometryField = true;
        } else if ("MULTI_LINESTRING".equalsIgnoreCase(fieldName)
          || "MULTILINESTRING".equalsIgnoreCase(fieldName)
          || "MULTILINE".equalsIgnoreCase(fieldName) || "MULTI_LINE".equalsIgnoreCase(fieldName)) {
          type = GeometryDataTypes.MULTI_LINE_STRING;
          isGeometryField = true;
        } else if ("POLYGON".equalsIgnoreCase(fieldName)) {
          type = GeometryDataTypes.POLYGON;
          isGeometryField = true;
        } else if ("MULTI_POLYGON".equalsIgnoreCase(fieldName)
          || "MULTIPOLYGON".equalsIgnoreCase(fieldName)) {
          type = GeometryDataTypes.MULTI_POLYGON;
          isGeometryField = true;
        } else {
          type = DataTypes.STRING;
          length = 0;
        }
        final FieldDefinition field;
        if (isGeometryField) {
          field = new GeometryFieldDefinition(this.geometryFactory, fieldName, type, false);
          geometryField = field;
        } else {
          field = new FieldDefinition(fieldName, type, length, false);
        }
        fields.add(field);
      }
    }
    if (this.hasPointFields) {
      if (geometryField == null) {
        if (geometryColumnName == null) {
          geometryColumnName = this.geometryType.getName();
        }
        geometryField = new FieldDefinition(geometryColumnName, this.geometryType, true);
        fields.add(geometryField);
      }
    }
    if (geometryField != null) {
      geometryField.setGeometryFactory(this.geometryFactory);
    }
    final RecordStoreSchema schema = getProperty("schema");
    String typePath = getProperty("typePath");
    if (!Property.hasValue(typePath)) {
      typePath = "/" + baseName;
      String schemaPath = getProperty("schemaPath");
      if (Property.hasValue(schemaPath)) {
        if (!schemaPath.startsWith("/")) {
          schemaPath = "/" + schemaPath;
        }
        typePath = schemaPath + typePath;
      }
    }
    final PathName pathName = PathName.newPathName(typePath);
    this.recordDefinition = new RecordDefinitionImpl(schema, pathName, getProperties(), fields);
    this.recordDefinition.setGeometryFactory(this.geometryFactory);
    return this.recordDefinition;
  }

  /**
   * Parse a record containing an array of String values into a Record with
   * the strings converted to the objects based on the attribute data type.
   *
   * @param values The record.
   * @return The Record.
   */
  protected Record parseRecord(final List<String> values) {
    final Record record = this.recordFactory.newRecord(this.recordDefinition);
    final int valueCount = values.size();
    final int fieldCount = this.recordDefinition.getFieldCount();
    final int count = Math.min(valueCount, fieldCount);
    for (int i = 0; i < count; i++) {
      final String valueString = values.get(i);
      if (valueString != null) {
        record.setValue(i, valueString);
      }
    }
    if (this.hasPointFields) {
      final Double x = Maps.getDouble(record, this.pointXFieldName);
      final Double y = Maps.getDouble(record, this.pointYFieldName);
      if (x != null && y != null) {
        final GeometryFactory geometryFactory = getGeometryFactory();
        final Geometry geometry = geometryFactory.point(x, y);
        record.setGeometryValue(geometry);
      }
    }
    return record;
  }

  public void setGeometryColumnName(final String geometryColumnName) {
    this.geometryColumnName = geometryColumnName;
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public void setGeometryType(final DataType geometryType) {
    this.geometryType = geometryType;
  }

  public void setPointXFieldName(final String pointXFieldName) {
    this.pointXFieldName = pointXFieldName;
  }

  public void setPointYFieldName(final String pointYFieldName) {
    this.pointYFieldName = pointYFieldName;
  }

  protected void setRecordDefinition(final RecordDefinition recordDefinition) {
    this.recordDefinition = recordDefinition;
  }

}
