package com.revolsys.record.io.format.esri.rest.map;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.ClockDirection;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.code.SingleValueCodeTable;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.GeometryType;
import com.revolsys.record.io.format.esri.rest.ArcGisResponse;
import com.revolsys.record.io.format.esri.rest.ArcGisRestCatalog;
import com.revolsys.record.io.format.esri.rest.CatalogElement;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.spring.resource.Resource;
import com.revolsys.spring.resource.UrlResource;
import com.revolsys.util.Property;
import com.revolsys.webservice.WebServiceFeatureLayer;

public class FeatureLayer extends LayerDescription implements WebServiceFeatureLayer {
  public static FeatureLayer getRecordLayerDescription(final String layerUrl) {
    return new FeatureLayer(layerUrl);
  }

  public static FeatureLayer getRecordLayerDescription(final String serverUrl,
    final PathName pathName) {
    final ArcGisRestCatalog catalog = new ArcGisRestCatalog(serverUrl);
    return catalog.getWebServiceResource(pathName, FeatureLayer.class);
  }

  public static FeatureLayer getRecordLayerDescription(final String serverUrl, final String path) {
    final PathName pathName = PathName.newPathName(path);
    return getRecordLayerDescription(serverUrl, pathName);
  }

  private RecordDefinition recordDefinition;

  private BoundingBox boundingBox;

  private boolean supportsPagination;

  protected FeatureLayer(final ArcGisRestAbstractLayerService service,
    final CatalogElement parent) {
    super(service, parent);
  }

  public FeatureLayer(final ArcGisRestAbstractLayerService service, final CatalogElement parent,
    final MapEx properties) {
    super(service, parent);
    initialize(properties);
  }

  public FeatureLayer(final String layerUrl) {
    setServiceUrl(new UrlResource(layerUrl));
  }

  private void addDefaultRecordQueryParameters(final Map<String, Object> parameters) {
    parameters.put("returnZ", "true");
    parameters.put("outFields", "*");
  }

  private void addField(final RecordDefinitionImpl recordDefinition, final String geometryType,
    final MapEx field) {
    final String fieldName = field.getString("name");
    final String fieldTitle = field.getString("string");
    final String fieldType = field.getString("type");
    final FieldType esriFieldType = FieldType.valueOf(fieldType);
    final DataType dataType;
    if (esriFieldType == FieldType.esriFieldTypeGeometry) {
      final DataType geometryDataType = getGeometryDataType(geometryType);

      if (geometryDataType == null) {
        throw new IllegalArgumentException("No geometryType specified for " + getServiceUrl());
      }
      dataType = geometryDataType;
    } else {
      dataType = esriFieldType.getDataType();
    }
    if (dataType == null) {
      throw new IllegalArgumentException(
        "Unsupported field=" + fieldName + " type=" + dataType + " for " + getServiceUrl());
    }
    final int length = field.getInteger("length", 0);
    final FieldDefinition fieldDefinition = recordDefinition.addField(fieldName, dataType, length,
      false);
    fieldDefinition.setTitle(fieldTitle);
    setCodeTable(fieldDefinition, field);
    if (esriFieldType == FieldType.esriFieldTypeOID) {
      recordDefinition.setIdFieldName(fieldName);
      fieldDefinition.setRequired(true);
    }
  }

  @Override
  public BoundingBox getBoundingBox() {
    refreshIfNeeded();
    return this.boundingBox;
  }

  private DataType getGeometryDataType(final String geometryType) {
    DataType geometryDataType = null;
    if (Property.hasValue(geometryType)) {
      final GeometryType esriGeometryType = GeometryType.valueOf(geometryType);
      geometryDataType = esriGeometryType.getDataType();
      if (geometryDataType == null) {
        throw new IllegalArgumentException(
          "Unsupported geometryType=" + geometryType + " for " + getServiceUrl());
      }
    }
    return geometryDataType;
  }

  @Override
  public String getIconName() {
    return WebServiceFeatureLayer.super.getIconName();
  }

  @Override
  public PathName getPathName() {
    return super.getPathName();
  }

  @Override
  public int getRecordCount(final BoundingBox boundingBox) {
    final Map<String, Object> parameters = newQueryParameters(boundingBox);
    if (parameters == null) {
      return 0;
    } else {
      return getRecordCount(parameters, boundingBox);
    }
  }

  protected int getRecordCount(final Map<String, Object> parameters, final Object errorText) {
    parameters.put("returnCountOnly", "true");
    final Resource resource = getResource("query", parameters);
    try {
      final MapEx response = Json.toMap(resource);
      return response.getInteger("count", 0);

    } catch (final Throwable e) {
      Logs.debug(this, "Unable to get count for: " + errorText + "\n" + resource.getUriString());
    }
    return 0;
  }

  @Override
  public int getRecordCount(final Query query) {
    final Map<String, Object> parameters = newQueryParameters(query);
    if (query != null) {
      // OFFSET & LIMIT
      final int offset = query.getOffset();
      parameters.put("resultOffset", offset);
      final int limit = query.getLimit();
      if (limit != Integer.MAX_VALUE) {
        parameters.put("resultRecordCount", limit);
      }
    }

    return getRecordCount(parameters, query);
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    refreshIfNeeded();
    return this.recordDefinition;
  }

  @Override
  public RecordReader getRecordReader(final Query query) {
    return newRecordReader(query, false);
  }

  @Override
  public <V extends Record> RecordReader getRecordReader(final RecordFactory<V> recordFactory,
    final BoundingBox boundingBox) {
    final Map<String, Object> parameters = newQueryParameters(boundingBox);
    final ArcGisRestServerFeatureReader reader = new ArcGisRestServerFeatureReader(this, parameters,
      0, Integer.MAX_VALUE, recordFactory, !isSupportsPagination());
    return reader;
  }

  @Override
  protected void initialize(final MapEx properties) {
    super.initialize(properties);
    this.boundingBox = ArcGisResponse.newBoundingBox(properties, "extent");
    final PathName pathName = getPathName();
    final List<MapEx> fields = properties.getValue("fields");
    if (fields != null) {
      final RecordDefinitionImpl newRecordDefinition = new RecordDefinitionImpl(pathName);
      newRecordDefinition.setPolygonRingDirection(ClockDirection.CLOCKWISE);
      final String description = properties.getString("description");
      newRecordDefinition.setDescription(description);

      final String geometryType = properties.getString("geometryType");

      for (final MapEx field : fields) {
        addField(newRecordDefinition, geometryType, field);
      }
      if (Property.hasValue(geometryType)) {
        if (!newRecordDefinition.hasGeometryField()) {
          final DataType geometryDataType = getGeometryDataType(geometryType);
          if (geometryDataType == null) {
            throw new IllegalArgumentException("No geometryType specified for " + getServiceUrl());
          } else {
            newRecordDefinition.addField("GEOMETRY", geometryDataType);
          }
        }
      }

      if (this.boundingBox != null) {
        final GeometryFactory geometryFactory = this.boundingBox.getGeometryFactory();
        newRecordDefinition.setGeometryFactory(geometryFactory);
      }
      final FieldDefinition objectIdField = newRecordDefinition.getField("OBJECTID");
      if (newRecordDefinition.getIdField() == null && objectIdField != null) {
        final int fieldIndex = objectIdField.getIndex();
        newRecordDefinition.setIdFieldIndex(fieldIndex);
        objectIdField.setRequired(true);
      }
      this.recordDefinition = newRecordDefinition;
    }
  }

  public boolean isSupportsPagination() {
    return this.supportsPagination;
  }

  @Override
  public ArcGisRestFeatureLayerQuery newQuery() {
    return new ArcGisRestFeatureLayerQuery(this);
  }

  public Map<String, Object> newQueryParameters(BoundingBox boundingBox) {
    refreshIfNeeded();
    boundingBox = convertBoundingBox(boundingBox);
    if (Property.hasValue(boundingBox)) {
      final Map<String, Object> parameters = new LinkedHashMap<>();
      parameters.put("f", "json");
      parameters.put("geometryType", "esriGeometryEnvelope");
      final double minX = boundingBox.getMinX();
      final double minY = boundingBox.getMinY();
      final double maxX = boundingBox.getMaxX();
      final double maxY = boundingBox.getMaxY();
      final String boundingBoxText = minX + "," + minY + "," + maxX + "," + maxY;
      parameters.put("geometry", boundingBoxText);
      addDefaultRecordQueryParameters(parameters);
      return parameters;
    } else {
      return null;
    }
  }

  public Map<String, Object> newQueryParameters(final Query query) {
    final Map<String, Object> parameters = new LinkedHashMap<>();
    parameters.put("f", "json");
    parameters.put("returnGeometry", "true");
    parameters.put("where", this.recordDefinition.getIdFieldName() + " > 0");
    if (query != null) {
      // WHERE
      final Condition whereCondition = query.getWhereCondition();
      if (whereCondition != Condition.ALL) {
        final String where = whereCondition.toString();
        parameters.put("where", where);
      }

      // ORDER BY
      final Map<QueryValue, Boolean> orderBy = query.getOrderBy();
      if (Property.hasValue(orderBy)) {
        final String orderByFields = JdbcUtils
          .appendOrderByFields(query, new StringBuilder(), this.recordDefinition, orderBy)
          .toString();
        parameters.put("orderByFields", orderByFields);
      }
    }
    return parameters;
  }

  public RecordReader newRecordReader(final Query query, final boolean pageByObjectId) {
    final RecordFactory<?> recordFactory = query.getRecordFactory();
    refreshIfNeeded();
    final Map<String, Object> parameters = newQueryParameters(query);
    addDefaultRecordQueryParameters(parameters);
    int offset = 0;
    int limit = Integer.MAX_VALUE;
    if (query != null) {
      offset = query.getOffset();
      limit = query.getLimit();
    }
    return new ArcGisRestServerFeatureReader(this, parameters, offset, limit, recordFactory,
      pageByObjectId);
  }

  public void setAdvancedQueryCapabilities(final MapEx advancedQueryCapabilities) {
    setProperties(advancedQueryCapabilities);
  }

  @SuppressWarnings("unchecked")
  private void setCodeTable(final FieldDefinition fieldDefinition, final MapEx field) {
    final MapEx domain = (MapEx)field.get("domain");
    if (domain != null) {
      final String domainType = domain.getString("type");
      final String domainName = domain.getString("name");
      final List<MapEx> codedValues = (List<MapEx>)domain.get("codedValues");

      if ("codedValue".equals(domainType) && Property.hasValuesAll(domainName, codedValues)) {
        final SingleValueCodeTable codeTable = new SingleValueCodeTable(domainName);
        for (final MapEx codedValue : codedValues) {
          final String code = codedValue.getString("code");
          final String description = codedValue.getString("name");
          codeTable.addValue(code, description);
        }
        fieldDefinition.setCodeTable(codeTable);
      }
    }
  }

  public void setSupportsPagination(final boolean supportsPagination) {
    this.supportsPagination = supportsPagination;
  }
}
