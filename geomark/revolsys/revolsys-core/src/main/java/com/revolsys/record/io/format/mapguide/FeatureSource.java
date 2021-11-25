package com.revolsys.record.io.format.mapguide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.io.PathName;

import com.revolsys.collection.Parent;
import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;
import com.revolsys.webservice.WebServiceResource;

public class FeatureSource extends ResourceDocument implements Parent<MapGuideFeatureLayer> {
  public static String getString(final MapEx map, final String name) {
    final Object value = getValue(map, name);
    return DataTypes.toString(value);
  }

  @SuppressWarnings("unchecked")
  private static <V> V getValue(final MapEx map, final String name) {
    Object value = map.getValue(name);
    if (value instanceof List<?>) {
      final List<?> list = (List<?>)value;
      value = list.get(0);
    }
    return (V)value;
  }

  private final Map<String, Integer> coordinateSystemIdBySrsName = Maps
    .<String, Integer> buildHash()//
    // .add("UTM83-09", EpsgCoordinateSystems.nad83UtmId(9)) //
    // .add("UTM83-10", EpsgCoordinateSystems.nad83UtmId(10)) //
    // .add("UTM83-11", EpsgCoordinateSystems.nad83UtmId(11)) //
    // .add("UTM83-12", 26912) //
    // .add("WORLD-MERCATOR", 3857) //
    .getMap();

  private List<MapGuideFeatureLayer> layers = new ArrayList<>();

  private Map<String, MapGuideFeatureLayer> layerByName = new HashMap<>();

  public FeatureSource(final MapEx properties) {
    setProperties(properties);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends WebServiceResource> R getChild(final String name) {
    refreshIfNeeded();
    if (name == null) {
      return null;
    } else {
      return (R)this.layerByName.get(name.toLowerCase());
    }
  }

  @Override
  public List<MapGuideFeatureLayer> getChildren() {
    refreshIfNeeded();
    return this.layers;
  }

  @Override
  public String getIconName() {
    return "folder:table";
  }

  private MapGuideFeatureLayer newLayer(final String name, final MapEx element, final MapEx complexType) {
    if (!"true".equals(getString(complexType, "@abstract"))) {
      final PathName pathName = getPathName();
      final PathName layerPathName = pathName.newChild(name);
      final RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl(layerPathName);
      GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;
      final MapEx complexContent = getValue(complexType, "xs:complexContent");
      final MapEx extension = getValue(complexContent, "xs:extension");
      if ("gml:AbstractFeatureType".equals(getString(extension, "@base"))) {
        final MapEx sequence = getValue(extension, "xs:sequence");
        final List<MapEx> elements = sequence.getValue("xs:element");
        for (final MapEx fieldElement : elements) {
          final String fieldName = getString(fieldElement, "@name");
          final boolean required = !"0".equals(getString(fieldElement, "@minOccurs"));
          DataType dataType = DataTypes.STRING;
          final MapEx simpleFieldType = getValue(fieldElement, "xs:simpleType");
          if (simpleFieldType == null) {
            final String fieldType = getString(fieldElement, "@type");
            if ("gml:AbstractGeometryType".equals(fieldType)) {
              final String geometryTypes = getString(fieldElement, "@fdo:geometryTypes");
              for (final String geometryType : geometryTypes.split(" ")) {
                final DataType geometryDataType = DataTypes.getDataType(geometryType);
                if (geometryDataType != DataTypes.OBJECT) {
                  if (dataType == DataTypes.STRING) {
                    dataType = geometryDataType;
                  } else if (dataType == GeometryDataTypes.GEOMETRY) {
                  } else if (geometryDataType == GeometryDataTypes.GEOMETRY) {
                    dataType = GeometryDataTypes.GEOMETRY;
                  } else if (geometryDataType == GeometryDataTypes.GEOMETRY_COLLECTION) {
                    dataType = GeometryDataTypes.GEOMETRY;
                  } else if (dataType.equals(GeometryDataTypes.POINT)) {
                    if (geometryDataType.equals(GeometryDataTypes.POINT)) {
                    } else if (geometryDataType.equals(GeometryDataTypes.MULTI_POINT)) {
                      dataType = GeometryDataTypes.MULTI_POINT;
                    } else {
                      dataType = GeometryDataTypes.GEOMETRY;
                    }
                  } else if (dataType.equals(GeometryDataTypes.MULTI_POINT)) {
                    if (geometryDataType.equals(GeometryDataTypes.POINT)) {
                    } else if (geometryDataType.equals(GeometryDataTypes.MULTI_POINT)) {
                    } else {
                      dataType = GeometryDataTypes.GEOMETRY;
                    }
                  } else if (dataType.equals(GeometryDataTypes.LINE_STRING)) {
                    if (geometryDataType.equals(GeometryDataTypes.LINE_STRING)) {
                    } else if (geometryDataType.equals(GeometryDataTypes.MULTI_LINE_STRING)) {
                      dataType = GeometryDataTypes.MULTI_LINE_STRING;
                    } else {
                      dataType = GeometryDataTypes.GEOMETRY;
                    }
                  } else if (dataType.equals(GeometryDataTypes.MULTI_LINE_STRING)) {
                    if (geometryDataType.equals(GeometryDataTypes.LINE_STRING)) {
                    } else if (geometryDataType.equals(GeometryDataTypes.MULTI_LINE_STRING)) {
                    } else {
                      dataType = GeometryDataTypes.GEOMETRY;
                    }
                  } else if (dataType.equals(GeometryDataTypes.POLYGON)) {
                    if (geometryDataType.equals(GeometryDataTypes.POLYGON)) {
                    } else if (geometryDataType.equals(GeometryDataTypes.MULTI_POLYGON)) {
                      dataType = GeometryDataTypes.MULTI_POLYGON;
                    } else {
                      dataType = GeometryDataTypes.GEOMETRY;
                    }
                  } else if (dataType.equals(GeometryDataTypes.MULTI_POLYGON)) {
                    if (geometryDataType.equals(GeometryDataTypes.POLYGON)) {
                    } else if (geometryDataType.equals(GeometryDataTypes.MULTI_POLYGON)) {
                    } else {
                      dataType = GeometryDataTypes.GEOMETRY;
                    }
                  }
                }

              }
              if (dataType == DataTypes.STRING) {
                dataType = GeometryDataTypes.GEOMETRY;
              }
              int axisCount = 2;
              if ("true".equals(getString(fieldElement, "@fdo:hasMeasure"))) {
                axisCount = 4;
              } else if ("true".equals(getString(fieldElement, "@fdo:hasElevation"))) {
                axisCount = 3;
              }
              final String srsName = getString(fieldElement, "@fdo:srsName");
              if (Property.hasValue(srsName)) {
                final Integer coordinateSystemId = Maps.getInteger(this.coordinateSystemIdBySrsName,
                  srsName);
                if (coordinateSystemId == null) {
                  try {
                    final Map<String, Object> csParameters = Collections.singletonMap("CSCODE",
                      srsName);
                    final MapGuideWebService webService = getWebService();
                    final Resource wktResource = webService
                      .getResource("CS.CONVERTCOORDINATESYSTEMCODETOWKT", null, csParameters);
                    final String wkt = wktResource.contentsAsString();
                    geometryFactory = GeometryFactory.floating(wkt, axisCount);
                  } catch (final Throwable e) {

                  }
                } else {
                  geometryFactory = GeometryFactory.floating(coordinateSystemId, axisCount);
                }
              }
            }

          } else {
            final MapEx restriction = getValue(simpleFieldType, "xs:restriction");
            if (restriction != null) {
              final String fieldBase = getString(restriction, "@base");
              dataType = DataTypes.getDataType(fieldBase.replace("xs:", ""));
            }
          }
          if (dataType != null) {
            recordDefinition.addField(fieldName, dataType, required);
          }
        }
        recordDefinition.setGeometryFactory(geometryFactory);
        final MapGuideFeatureLayer layer = new MapGuideFeatureLayer(this, recordDefinition);
        return layer;
      }
    }
    return null;

  }

  @Override
  protected void refreshDo() {
    final MapEx properties = new LinkedHashMapEx();
    final String resourceId = getResourceId();
    properties.put("RESOURCEID", resourceId);
    final MapGuideWebService webService = getWebService();
    final MapEx schemaResource = webService.getJsonResponse("DESCRIBEFEATURESCHEMA", properties);
    final MapEx schema = schemaResource.getValue("xs:schema");
    if (schema != null) {
      final Map<String, String> prefixByUri = new HashMap<>();
      for (final String name : schema.keySet()) {
        if (name.startsWith("@xmlns:")) {
          final String namespaceUri = getString(schema, name);
          final String prefix = name.substring(7);
          prefixByUri.put(namespaceUri, prefix);
        }
      }
      final String targetNamespace = getString(schema, "@targetNamespace");
      final String prefix = prefixByUri.get(targetNamespace);
      final Map<String, MapEx> complexTypeDefinitions = new HashMap<>();
      for (final MapEx complexType : schema.getValue("xs:complexType",
        Collections.<MapEx> emptyList())) {
        String name = getString(complexType, "@name");
        if (prefix != null) {
          name = prefix + ":" + name;
        }
        complexTypeDefinitions.put(name, complexType);
      }
      final List<MapGuideFeatureLayer> layers = new ArrayList<>();
      final Map<String, MapGuideFeatureLayer> layerByName = new HashMap<>();
      for (final MapEx element : schema.getValue("xs:element", Collections.<MapEx> emptyList())) {
        final String name = getString(element, "@name");
        final String type = getString(element, "@type");
        final MapEx complexType = complexTypeDefinitions.get(type);
        final MapGuideFeatureLayer layer = newLayer(name, element, complexType);
        if (layer != null) {
          layers.add(layer);
          layerByName.put(name.toLowerCase(), layer);
        }
        this.layers = layers;
        this.layerByName = layerByName;
      }
    }
  }
}
