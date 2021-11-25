package com.revolsys.record.io.format.esri.rest.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jeometry.common.function.Function3;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.Parent;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.record.io.format.esri.rest.ArcGisResponse;
import com.revolsys.record.io.format.esri.rest.ArcGisRestService;
import com.revolsys.record.io.format.esri.rest.ArcGisRestServiceContainer;
import com.revolsys.record.io.format.esri.rest.CatalogElement;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;
import com.revolsys.webservice.WebServiceResource;

public abstract class ArcGisRestAbstractLayerService extends ArcGisRestService
  implements Parent<CatalogElement> {
  public static final Map<String, Function3<ArcGisRestAbstractLayerService, CatalogElement, MapEx, LayerDescription>> LAYER_FACTORY_BY_TYPE = Maps
    .<String, Function3<ArcGisRestAbstractLayerService, CatalogElement, MapEx, LayerDescription>> buildHash() //
    .add("Group Layer", GroupLayer::new)
    .add("Feature Layer", FeatureLayer::new)
    .add("Table", FeatureLayer::new)
    .add("Annotation Layer", AnnotationLayer::new)
    .getMap();

  private List<CatalogElement> children = Collections.emptyList();

  private Map<String, LayerDescription> rootLayersByName = Collections.emptyMap();

  public ArcGisRestAbstractLayerService(final ArcGisRestServiceContainer parent,
    final String type) {
    super(parent, type);
  }

  protected ArcGisRestAbstractLayerService(final String type) {
    super(type);
  }

  public LayerDescription addLayer(final CatalogElement parent,
    final Map<String, LayerDescription> layersByName, MapEx layerProperties) {
    final int id = layerProperties.getInteger("id");
    final Resource resource = getResource(Integer.toString(id), ArcGisResponse.FORMAT_PARAMETER);
    try {
      layerProperties = Json.toMap(resource);

      final String layerType = layerProperties.getString("type");
      final Function3<ArcGisRestAbstractLayerService, CatalogElement, MapEx, LayerDescription> factory = LAYER_FACTORY_BY_TYPE
        .get(layerType);
      LayerDescription layer;
      if (factory == null) {
        layer = new LayerDescription(this, parent, layerProperties);
      } else {
        layer = factory.apply(this, parent, layerProperties);
      }
      final String name = layer.getName();
      if (Property.hasValue(name)) {
        layersByName.put(name.toLowerCase(), layer);
        return layer;
      } else {
        return null;
      }
    } catch (final Throwable e) {
      e.printStackTrace();
      Logs.debug(this, "Unable to initialize layer: " + resource, e);
      return null;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C extends WebServiceResource> C getChild(final String name) {
    if (name == null) {
      return null;
    } else {
      refreshIfNeeded();
      return (C)this.rootLayersByName.get(name.toLowerCase());
    }
  }

  @Override
  public List<CatalogElement> getChildren() {
    refreshIfNeeded();
    return this.children;
  }

  @SuppressWarnings("unchecked")
  public <L extends LayerDescription> L getLayer(final PathName pathName) {
    refreshIfNeeded();
    final List<String> elements = pathName.getElements();
    if (!elements.isEmpty()) {
      LayerDescription layer = this.rootLayersByName.get(elements.get(0));
      for (int i = 1; layer != null && i < elements.size(); i++) {
        if (layer instanceof GroupLayer) {
          final GroupLayer layerGroup = (GroupLayer)layer;
          final String childLayerName = elements.get(i);
          layer = layerGroup.getLayer(childLayerName);
        } else {
          return null;
        }

      }
      return (L)layer;
    }
    return null;
  }

  protected void initChildren(final MapEx properties, final List<CatalogElement> children,
    final Map<String, LayerDescription> rootLayersByName) {
    final Map<String, GroupLayer> groups = new TreeMap<>();
    final Map<String, LayerDescription> layers = new TreeMap<>();
    final List<MapEx> layerDefinitions = properties.getValue("layers", Collections.emptyList());
    for (final MapEx layerProperties : layerDefinitions) {
      final Integer parentLayerId = layerProperties.getInteger("parentLayerId");
      if (parentLayerId == null || parentLayerId == -1) {
        final LayerDescription layer = addLayer(this, rootLayersByName, layerProperties);
        if (layer != null) {
          final String layerName = layer.getName();
          if (layer instanceof GroupLayer) {
            final GroupLayer group = (GroupLayer)layer;
            groups.put(layerName, group);
          } else {
            layers.put(layerName, layer);
          }
        }
      }
    }

    final List<MapEx> tableDefinitions = properties.getValue("tables", Collections.emptyList());
    for (final MapEx layerProperties : tableDefinitions) {
      final LayerDescription layer = addLayer(this, rootLayersByName, layerProperties);
      if (layer != null) {
        final String layerName = layer.getName();
        layers.put(layerName, layer);
      }
    }
    children.addAll(groups.values());
    children.addAll(layers.values());
  }

  @Override
  protected void initialize(final MapEx properties) {
    super.initialize(properties);
    final List<CatalogElement> children = new ArrayList<>();
    final Map<String, LayerDescription> rootLayersByName = new TreeMap<>();

    initChildren(properties, children, rootLayersByName);
    this.children = Collections.unmodifiableList(children);
    this.rootLayersByName = Collections.unmodifiableMap(rootLayersByName);
  }
}
