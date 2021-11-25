package com.revolsys.record.io.format.mapguide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.Parent;
import com.revolsys.webservice.WebServiceResource;

public class Folder extends MapGuideResource implements Parent<MapGuideResource> {
  private int numberOfFolders;

  private int numberOfDocuments;

  private List<MapGuideResource> resources = new ArrayList<>();

  private final Map<String, MapGuideResource> resourceByName = new HashMap<>();

  public Folder(final Map<String, ? extends Object> properties) {
    setProperties(properties);
  }

  public void addResource(final MapGuideResource resource) {
    this.resources.add(resource);
    final String name = resource.getName().toLowerCase();
    this.resourceByName.put(name, resource);
    resource.setParent(this);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends WebServiceResource> R getChild(final String name) {
    if (name == null) {
      return null;
    } else {
      return (R)this.resourceByName.get(name.toLowerCase());
    }
  }

  @Override
  public List<MapGuideResource> getChildren() {
    return getResources();
  }

  @Override
  public String getIconName() {
    return "folder:world";
  }

  public int getNumberOfDocuments() {
    return this.numberOfDocuments;
  }

  public int getNumberOfFolders() {
    return this.numberOfFolders;
  }

  public List<MapGuideResource> getResources() {
    return this.resources;
  }

  public void setNumberOfDocuments(final int numberOfDocuments) {
    this.numberOfDocuments = numberOfDocuments;
  }

  public void setNumberOfFolders(final int numberOfFolders) {
    this.numberOfFolders = numberOfFolders;
  }

  public void setResources(final List<MapGuideResource> resources) {
    this.resources = resources;
  }
}
