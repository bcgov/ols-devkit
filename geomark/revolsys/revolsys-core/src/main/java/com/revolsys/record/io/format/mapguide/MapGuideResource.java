package com.revolsys.record.io.format.mapguide;

import java.sql.Timestamp;
import java.util.List;

import org.jeometry.common.io.PathName;

import com.revolsys.beans.Classes;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.UrlResource;
import com.revolsys.util.Property;
import com.revolsys.webservice.WebServiceResource;

public class MapGuideResource extends BaseObjectWithProperties implements WebServiceResource {
  private MapGuideWebService webService;

  private final Object resfreshSync = new Object();

  private boolean initialized = false;

  private String resourceId;

  private int depth;

  private String owner;

  private Timestamp createdDate;

  private Timestamp modifiedDate;

  private PathName path;

  private String name;

  private final String type;

  private WebServiceResource parent;

  public MapGuideResource() {
    this.type = Classes.className(this);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof MapGuideResource) {
      final MapGuideResource resource = (MapGuideResource)obj;
      if (resource.getPath().equals(this.path)) {
        return true;
      }
    }
    return super.equals(obj);
  }

  public Timestamp getCreatedDate() {
    return this.createdDate;
  }

  public int getDepth() {
    return this.depth;
  }

  @Override
  public String getIconName() {
    return "file";
  }

  public Timestamp getModifiedDate() {
    return this.modifiedDate;
  }

  @Override
  public String getName() {
    return this.name;
  }

  public String getOwner() {
    return this.owner;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends WebServiceResource> R getParent() {
    return (R)this.parent;
  }

  public PathName getPath() {
    return this.path;
  }

  public String getResourceId() {
    return this.resourceId;
  }

  @Override
  public UrlResource getServiceUrl() {
    return null;
  }

  public String getType() {
    return this.type;
  }

  @Override
  public MapGuideWebService getWebService() {
    return this.webService;
  }

  @Override
  public int hashCode() {
    return getPath().hashCode();
  }

  public final void refresh() {
    synchronized (this.resfreshSync) {
      this.initialized = true;
      refreshDo();
    }
  }

  protected void refreshDo() {
  }

  public final void refreshIfNeeded() {
    synchronized (this.resfreshSync) {
      if (!this.initialized) {
        refresh();
      }
    }
  }

  public void setCreatedDate(final Timestamp createdDate) {
    this.createdDate = createdDate;
  }

  public void setDepth(final int depth) {
    this.depth = depth;
  }

  public void setModifiedDate(final Timestamp modifiedDate) {
    this.modifiedDate = modifiedDate;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setOwner(final String owner) {
    this.owner = owner;
  }

  public void setParent(final WebServiceResource parent) {
    this.parent = parent;
  }

  @Override
  public void setProperty(String name, Object value) {
    name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
    if (value instanceof List) {
      final List<?> list = (List<?>)value;
      if (list.size() == 1) {
        final Class<?> type = Property.getType(this, name);
        if (!List.class.equals(type)) {
          value = list.get(0);
        }
      }
    }
    super.setProperty(name, value);
  }

  public void setResourceId(final String resourceId) {
    this.resourceId = resourceId;
    this.path = PathName.newPathName(resourceId.substring(resourceId.indexOf('/') + 1));
    this.name = this.path.getName().replace("." + this.type, "");
  }

  public void setWebService(final MapGuideWebService webService) {
    this.webService = webService;
  }

  @Override
  public String toString() {
    return getName();
  }
}
