package com.revolsys.record.io.format.mapguide;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.spring.resource.Resource;
import com.revolsys.spring.resource.UrlResource;
import com.revolsys.util.Property;
import com.revolsys.webservice.AbstractWebService;
import com.revolsys.webservice.WebServiceResource;

public class MapGuideWebService extends AbstractWebService<MapGuideResource> {
  public static final String J_TYPE = "mapGuideWebServer";

  private static final Map<String, Function<MapEx, ResourceDocument>> RESOURCE_DOCUMENT_FACTORIES = Maps
    .<String, Function<MapEx, ResourceDocument>> buildHash() //
    .add("ApplicationDefinition", ApplicationDefinition::new)
    .add("DrawingSource", DrawingSource::new)
    .add("FeatureSource", FeatureSource::new)
    .add("LayerDefinition", LayerDefinition::new)
    .add("LoadProcedure", LoadProcedure::new)
    .add("MapDefinition", MapDefinition::new)
    .add("SymbolDefinition", SymbolDefinition::new)
    .add("SymbolLibrary", SymbolLibrary::new)
    .add("WatermarkDefinition", WatermarkDefinition::new)
    .getMap();

  public static MapGuideWebService newMapGuideWebService(
    final Map<String, ? extends Object> properties) {
    final String serviceUrl = (String)properties.get("serviceUrl");
    if (Property.hasValue(serviceUrl)) {
      final MapGuideWebService service = new MapGuideWebService(serviceUrl);
      // service.setProperties(properties);
      return service;
    } else {
      throw new IllegalArgumentException("Missing serviceUrl");
    }
  }

  private final Object resfreshSync = new Object();

  private boolean initialized = false;

  private Folder root;

  public MapGuideWebService(final String serviceUrl) {
    super(serviceUrl);
  }

  @Override
  public <R extends WebServiceResource> R getChild(final String name) {
    refreshIfNeeded();
    if (this.root == null) {
      return null;
    } else {
      return this.root.getChild(name);
    }
  }

  @Override
  public List<MapGuideResource> getChildren() {
    refreshIfNeeded();
    if (this.root == null) {
      return Collections.emptyList();
    } else {
      return this.root.getChildren();
    }
  }

  @Override
  public String getIconName() {
    return "folder:world";
  }

  public InputStream getInputStream(final String operation, final String format,
    final Map<String, ? extends Object> parameters) {
    final Resource resource = getResource(operation, format, parameters);
    return resource.getInputStream();
  }

  public MapEx getJsonResponse(final String operation,
    final Map<String, ? extends Object> parameters) {
    final Resource resource = getResource(operation, "application/json", parameters);
    return Json.toMap(resource);
  }

  public Resource getResource(final String operation, final String format,
    final Map<String, ? extends Object> parameters) throws Error {
    final MapEx newParameters = new LinkedHashMapEx(parameters);
    newParameters.put("VERSION", "1.0.0");
    newParameters.put("OPERATION", operation);
    newParameters.put("format", format);

    final UrlResource serviceUrl = getServiceUrl();
    final UrlResource mapAgentUrl = serviceUrl.newChildResource("mapagent/mapagent.fcgi");
    final UrlResource resource = mapAgentUrl.newUrlResource(newParameters);
    return resource;
  }

  public Map<PathName, MapGuideResource> getResources(final String path) {
    final MapEx parameters = new LinkedHashMapEx();
    parameters.put("RESOURCEID", "Library:/" + path);
    parameters.put("COMPUTECHILDREN", "1");
    parameters.put("DEPTH", "-1");
    final MapEx response = getJsonResponse("ENUMERATERESOURCES", parameters);
    final MapEx resourceList = response.getValue("ResourceList");
    final List<MapEx> resourceFolders = resourceList.getValue("ResourceFolder");
    final Map<PathName, Folder> folderByPath = new HashMap<>();
    final Map<PathName, MapGuideResource> resourceByPath = new HashMap<>();
    for (final MapEx resourceDefinition : resourceFolders) {
      final Folder folder = new Folder(resourceDefinition);
      folder.setWebService(this);
      final PathName resourcePath = folder.getPath();
      folderByPath.put(resourcePath, folder);
      resourceByPath.put(resourcePath, folder);
      final PathName parentPath = resourcePath.getParent();
      if (parentPath != null) {
        final Folder parent = folderByPath.get(parentPath);
        parent.addResource(folder);
      }
    }
    final List<MapEx> resourceDocuments = resourceList.getValue("ResourceDocument");
    for (final MapEx resourceDefinition : resourceDocuments) {
      final List<String> resourceIdList = resourceDefinition.getValue("ResourceId");
      final String resourceId = resourceIdList.get(0);
      final String resourceType = resourceId.substring(resourceId.lastIndexOf(".") + 1);
      final Function<MapEx, ResourceDocument> factory = RESOURCE_DOCUMENT_FACTORIES
        .get(resourceType);
      if (factory != null) {
        final ResourceDocument resource = factory.apply(resourceDefinition);
        resource.setWebService(this);
        final PathName resourcePath = resource.getPath();
        resourceByPath.put(resourcePath, resource);
        final PathName parentPath = resourcePath.getParent();
        if (parentPath != null) {
          final Folder parent = folderByPath.get(parentPath);
          parent.addResource(resource);
        }
      } else {
        Logs.debug(this, "Unsupported resource type: " + resourceType);
      }
    }
    final Folder root = folderByPath.get(PathName.ROOT);
    if (root != null) {
      this.root = root;
    }
    return resourceByPath;
  }

  @Override
  public String getWebServiceTypeName() {
    return J_TYPE;
  }

  @Override
  public final void refresh() {
    synchronized (this.resfreshSync) {
      refreshDo();
    }
  }

  protected void refreshDo() {
    getResources("/");
  }

  public final void refreshIfNeeded() {
    synchronized (this.resfreshSync) {
      if (!this.initialized) {
        this.initialized = true;
        refresh();
      }
    }
  }

}
