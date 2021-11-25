package com.revolsys.record.io.format.esri.gdb.xml.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.io.PathName;

import com.revolsys.collection.CollectionUtil;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.io.PathUtil;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.GeometryType;
import com.revolsys.record.io.format.esri.gdb.xml.type.EsriGeodatabaseXmlFieldType;
import com.revolsys.record.io.format.esri.gdb.xml.type.EsriGeodatabaseXmlFieldTypeRegistry;
import com.revolsys.record.property.AreaFieldName;
import com.revolsys.record.property.LengthFieldName;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.util.Booleans;
import com.revolsys.util.Property;

public class EsriXmlRecordDefinitionUtil implements EsriGeodatabaseXmlConstants {
  private static final String DE_TABLE_PROPERTY = EsriXmlRecordDefinitionUtil.class + ".DETable";

  public static final EsriGeodatabaseXmlFieldTypeRegistry FIELD_TYPES = EsriGeodatabaseXmlFieldTypeRegistry.INSTANCE;

  private static Field addField(final DETable table, final FieldDefinition fieldDefinition) {
    final String fieldName = fieldDefinition.getName();
    final DataType dataType = fieldDefinition.getDataType();
    EsriGeodatabaseXmlFieldType fieldType = FIELD_TYPES.getFieldType(dataType);
    if (fieldType == null) {
      fieldType = FIELD_TYPES.getFieldType(DataTypes.STRING);
    }
    final Field field = new Field();
    field.setName(fieldName);
    field.setType(fieldType.getEsriFieldType());
    field.setIsNullable(!fieldDefinition.isRequired());
    field.setRequired(fieldDefinition.isRequired());
    int length = fieldType.getFixedLength();
    if (length < 0) {
      length = fieldDefinition.getLength();
    }
    field.setLength(length);
    final int precision;
    if (fieldType.isUsePrecision()) {
      precision = fieldDefinition.getLength();
    } else {
      precision = 0;
    }
    field.setPrecision(precision);
    final int scale = fieldDefinition.getScale();
    field.setScale(scale);
    table.addField(field);
    return field;
  }

  private static void addField(final RecordDefinitionImpl recordDefinition, final DETable deTable,
    final String tableName, final Field field, final String fieldName) {
    final FieldType fieldType = field.getType();
    final int precision = field.getPrecision();
    final DataType dataType;
    if (fieldType == FieldType.esriFieldTypeGeometry && deTable instanceof DEFeatureClass) {
      final DEFeatureClass featureClass = (DEFeatureClass)deTable;
      final GeometryType shapeType = featureClass.getShapeType();
      switch (shapeType) {
        case esriGeometryPoint:
          dataType = GeometryDataTypes.POINT;
        break;
        case esriGeometryMultipoint:
          dataType = GeometryDataTypes.MULTI_POINT;
        break;
        case esriGeometryPolyline:
          dataType = GeometryDataTypes.MULTI_LINE_STRING;
        break;
        case esriGeometryPolygon:
          dataType = GeometryDataTypes.POLYGON;
        break;

        default:
          throw new RuntimeException(
            "Unknown geometry type" + shapeType + " for " + tableName + "." + fieldName);
      }

    } else if (precision > 0 && (fieldType.equals(FieldType.esriFieldTypeSingle)
      || fieldType.equals(FieldType.esriFieldTypeDouble))) {
      dataType = DataTypes.DECIMAL;
    } else {
      dataType = EsriGeodatabaseXmlFieldTypeRegistry.INSTANCE.getDataType(fieldType);
    }
    final int scale = field.getScale();
    int length = field.getLength();
    if (precision != 0) {
      length = precision;
    }
    final Boolean required = !field.isIsNullable() || Booleans.getBoolean(field.getRequired());
    final FieldDefinition attribute = new FieldDefinition(fieldName, dataType, length, scale,
      required);

    recordDefinition.addField(attribute);
    if (fieldName.equals(tableName + "_ID")) {
      recordDefinition.setIdFieldName(fieldName);
    }
  }

  private static void addGeometryField(final GeometryType shapeType, final DETable table,
    final FieldDefinition attribute) {
    final Field field = addField(table, attribute);
    final DEFeatureClass featureClass = (DEFeatureClass)table;
    final SpatialReference spatialReference = featureClass.getSpatialReference();
    final GeometryDef geometryDef = new GeometryDef(shapeType, spatialReference);
    field.setGeometryDef(geometryDef);

    table.addIndex(field, false, "FDO_GEOMETRY");
  }

  private static void addObjectIdField(final DETable table) {
    final Field field = new Field();
    field.setName(table.getOIDFieldName());
    field.setType(FieldType.esriFieldTypeOID);
    field.setIsNullable(false);
    field.setLength(4);
    field.setRequired(true);
    field.setEditable(false);
    table.addField(field);

    table.addIndex(field, true, "FDO_OBJECTID");
  }

  public static DETable getDETable(final RecordDefinition recordDefinition,
    final SpatialReference spatialReference, final boolean createLengthField,
    final boolean createAreaField) {
    DETable table = recordDefinition.getProperty(DE_TABLE_PROPERTY);
    if (table == null) {
      table = newDETable(recordDefinition, spatialReference, createLengthField, createAreaField);
    }
    return table;
  }

  /**
   * Get a recordDefinition instance for the table definition excluding any ESRI
   * specific fields.
   *
   * @param schemaName
   * @param deTable
   * @return
   */
  public static RecordDefinition getRecordDefinition(final String schemaName,
    final DETable deTable) {
    return getRecordDefinition(schemaName, deTable, true);
  }

  public static RecordDefinition getRecordDefinition(final String schemaName, final DETable deTable,
    final boolean ignoreEsriFields) {
    final String tableName = deTable.getName();
    final PathName typePath = PathName.newPathName(PathUtil.toPath(schemaName, tableName));
    final RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl(typePath);
    final List<String> ignoreFieldNames = new ArrayList<>();
    if (ignoreEsriFields) {
      ignoreFieldNames.add(deTable.getOIDFieldName());

      if (deTable instanceof DEFeatureClass) {
        final DEFeatureClass featureClass = (DEFeatureClass)deTable;
        ignoreFieldNames.add(featureClass.getLengthFieldName());
        ignoreFieldNames.add(featureClass.getAreaFieldName());
      }
    }
    for (final Field field : deTable.getFields()) {
      final String fieldName = field.getName();
      if (!ignoreFieldNames.contains(fieldName)) {
        addField(recordDefinition, deTable, tableName, field, fieldName);
      }
    }
    for (final Index index : deTable.getIndexes()) {
      final String indexName = index.getName();
      if (indexName.endsWith("_PK")) {
        final List<Field> indexFields = index.getFields();
        final Field indexField = CollectionUtil.get(indexFields, 0);
        final String idName = indexField.getName();
        recordDefinition.setIdFieldName(idName);
      }
    }
    if (deTable instanceof DEFeatureClass) {
      final DEFeatureClass featureClass = (DEFeatureClass)deTable;
      final String shapeFieldName = featureClass.getShapeFieldName();
      recordDefinition.setGeometryFieldName(shapeFieldName);
      final SpatialReference spatialReference = featureClass.getSpatialReference();
      GeometryFactory geometryFactory = spatialReference.getGeometryFactory();
      int axisCount = 2;
      if (featureClass.isHasM()) {
        axisCount = 4;
      } else if (featureClass.isHasZ()) {
        axisCount = 3;
      }
      final double[] scales = geometryFactory.newScales(axisCount);
      geometryFactory = GeometryFactory.fixed(geometryFactory.getHorizontalCoordinateSystemId(),
        axisCount, scales);

      final FieldDefinition geometryField = recordDefinition.getGeometryField();
      geometryField.setGeometryFactory(geometryFactory);
    }

    return recordDefinition;
  }

  public static RecordDefinition getRecordDefinition(final String schemaName, final Domain domain,
    final boolean appendIdToName) {
    final String tableName;
    if (appendIdToName) {
      tableName = domain.getName() + "_ID";
    } else {
      tableName = domain.getName();
    }
    final PathName typePath = PathName.newPathName(PathUtil.toPath(schemaName, tableName));
    final RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl(typePath);
    final FieldType fieldType = domain.getFieldType();
    final DataType dataType = EsriGeodatabaseXmlFieldTypeRegistry.INSTANCE.getDataType(fieldType);
    int length = 0;
    for (final CodedValue codedValue : domain.getCodedValues()) {
      length = Math.max(length, codedValue.getCode().toString().length());
    }
    recordDefinition.addField(tableName, dataType, length, true);
    recordDefinition.addField("DESCRIPTION", DataTypes.STRING, 255, true);
    recordDefinition.setIdFieldIndex(0);
    return recordDefinition;
  }

  public static List<Record> getValues(final RecordDefinition recordDefinition,
    final Domain domain) {
    final List<Record> values = new ArrayList<>();
    for (final CodedValue codedValue : domain.getCodedValues()) {
      final Record value = new ArrayRecord(recordDefinition);
      value.setIdentifier(Identifier.newIdentifier(codedValue.getCode()));
      value.setValue("DESCRIPTION", codedValue.getName());
      values.add(value);
    }
    return values;
  }

  public static DEFeatureDataset newDEFeatureDataset(final String schemaName,
    final SpatialReference spatialReference) {
    final DEFeatureDataset dataset = new DEFeatureDataset();
    String name;
    final int slashIndex = schemaName.lastIndexOf('\\');
    if (slashIndex == -1) {
      name = schemaName;
    } else {
      name = schemaName.substring(slashIndex + 1);
    }
    if (schemaName.startsWith("\\")) {
      dataset.setCatalogPath(schemaName);
    } else {
      dataset.setCatalogPath("\\" + schemaName);
    }

    dataset.setName(name);

    final EnvelopeN envelope = new EnvelopeN(spatialReference);
    dataset.setExtent(envelope);
    dataset.setSpatialReference(spatialReference);
    return dataset;
  }

  public static List<DEFeatureDataset> newDEFeatureDatasets(final DETable table) {
    final String parentPath = table.getParentCatalogPath();
    if (parentPath.equals("\\")) {
      return Collections.emptyList();
    } else if (table instanceof DEFeatureClass) {
      final DEFeatureClass featureClass = (DEFeatureClass)table;
      final String schemaName = parentPath.substring(1);
      final SpatialReference spatialReference = featureClass.getSpatialReference();
      return newDEFeatureDatasets(schemaName, spatialReference);
    } else {
      throw new IllegalArgumentException("Expected a " + DEFeatureClass.class.getName());
    }
  }

  public static List<DEFeatureDataset> newDEFeatureDatasets(final String schemaName,
    final SpatialReference spatialReference) {
    final List<DEFeatureDataset> datasets = new ArrayList<>();
    String path = "";
    for (final String name : schemaName.split("\\\\")) {
      path += name;
      final DEFeatureDataset dataset = newDEFeatureDataset(path, spatialReference);
      datasets.add(dataset);
      path += "\\";
    }
    return datasets;
  }

  public static DETable newDETable(final RecordDefinition recordDefinition,
    final SpatialReference spatialReference, final boolean createLengthField,
    final boolean createAreaField) {
    final String typePath = recordDefinition.getPath();
    String schemaPath = PathUtil.getPath(typePath).replaceAll("/", "\\\\");

    final FieldDefinition geometryField = recordDefinition.getGeometryField();
    boolean hasGeometry = false;
    DataType geometryDataType = null;
    GeometryType shapeType = null;
    if (geometryField != null) {
      if (spatialReference == null) {
        throw new IllegalArgumentException(
          "A Geometry Factory with a coordinate system must be specified.");
      }
      geometryDataType = geometryField.getDataType();
      if (FIELD_TYPES.getFieldType(geometryDataType) != null) {
        hasGeometry = true;
        // TODO Z,m
        if (geometryDataType.equals(GeometryDataTypes.POINT)) {
          shapeType = GeometryType.esriGeometryPoint;
        } else if (geometryDataType.equals(GeometryDataTypes.MULTI_POINT)) {
          shapeType = GeometryType.esriGeometryMultipoint;
        } else if (geometryDataType.equals(GeometryDataTypes.LINE_STRING)) {
          shapeType = GeometryType.esriGeometryPolyline;
        } else if (geometryDataType.equals(GeometryDataTypes.LINEAR_RING)) {
          shapeType = GeometryType.esriGeometryPolyline;
        } else if (geometryDataType.equals(GeometryDataTypes.MULTI_LINE_STRING)) {
          shapeType = GeometryType.esriGeometryPolyline;
        } else if (geometryDataType.equals(GeometryDataTypes.POLYGON)) {
          shapeType = GeometryType.esriGeometryPolygon;
        } else if (geometryDataType.equals(GeometryDataTypes.MULTI_POLYGON)) {
          shapeType = GeometryType.esriGeometryPolygon;
        } else {
          throw new IllegalArgumentException("Unable to detect geometry type");
        }
      }
    }

    final List<FieldDefinition> fieldDefinitions = new ArrayList<>(recordDefinition.getFields());

    final String path = recordDefinition.getPath();
    final String name = PathUtil.getName(path);
    DETable table;
    if (hasGeometry) {
      final DEFeatureClass featureClass = new DEFeatureClass();
      table = featureClass;
      featureClass.setShapeType(shapeType);
      final String geometryFieldName = geometryField.getName();
      featureClass.setShapeFieldName(geometryFieldName);
      final GeometryFactory geometryFactory = spatialReference.getGeometryFactory();
      featureClass.setSpatialReference(spatialReference);
      featureClass.setHasM(geometryFactory.hasM());
      featureClass.setHasZ(geometryFactory.hasZ());
      final EnvelopeN envelope = new EnvelopeN(spatialReference);
      featureClass.setExtent(envelope);

      final Class<?> geometryClass = geometryDataType.getJavaClass();
      if (!Punctual.class.isAssignableFrom(geometryClass)) {
        final LengthFieldName lengthFieldNameProperty = LengthFieldName
          .getProperty(recordDefinition);
        String lengthFieldName = lengthFieldNameProperty.getFieldName();
        if (createLengthField) {
          if (!Property.hasValue(lengthFieldName)) {
            lengthFieldName = geometryFieldName + "_Length";
            lengthFieldNameProperty.setFieldName(lengthFieldName);
          }
          if (!recordDefinition.hasField(lengthFieldName)) {
            fieldDefinitions.add(new FieldDefinition(lengthFieldName, DataTypes.DOUBLE, true));
          }
        }
        featureClass.setLengthFieldName(lengthFieldName);

        if (!Lineal.class.isAssignableFrom(geometryClass)) {
          final AreaFieldName areaFieldNameProperty = AreaFieldName.getProperty(recordDefinition);
          String areaFieldName = areaFieldNameProperty.getFieldName();
          if (createAreaField) {
            if (!Property.hasValue(areaFieldName)) {
              areaFieldName = geometryFieldName + "_Area";
              areaFieldNameProperty.setFieldName(areaFieldName);
            }
            if (!recordDefinition.hasField(areaFieldName)) {
              fieldDefinitions.add(new FieldDefinition(areaFieldName, DataTypes.DOUBLE, true));
            }
          }
          featureClass.setAreaFieldName(areaFieldName);
        }
      }
    } else {
      table = new DETable();
      schemaPath = "\\";
    }

    String oidFieldName = recordDefinition
      .getProperty(EsriGeodatabaseXmlConstants.ESRI_OBJECT_ID_FIELD_NAME);
    if (!Property.hasValue(oidFieldName)) {
      oidFieldName = "OBJECTID";
    }
    final String catalogPath;
    if (schemaPath.equals("\\")) {
      catalogPath = "\\" + name;
    } else {
      catalogPath = schemaPath + "\\" + name;
    }
    table.setCatalogPath(catalogPath);
    table.setName(name);
    table.setHasOID(true);
    table.setOIDFieldName(oidFieldName);

    addObjectIdField(table);
    final FieldDefinition idField = recordDefinition.getIdField();
    for (final FieldDefinition fieldDefinition : fieldDefinitions) {
      if (fieldDefinition == geometryField) {
        addGeometryField(shapeType, table, fieldDefinition);
      } else {
        final String fieldName = fieldDefinition.getName();
        if (!fieldName.equals(oidFieldName)) {
          final Field field = addField(table, fieldDefinition);
          if (idField == fieldDefinition) {
            table.addIndex(field, true, fieldName + "_PK");
          }
        }
      }
    }
    table.setAliasName(name);
    return table;
  }
}
