package com.revolsys.record.io.format.esri.gdb.xml.model;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.record.io.format.esri.gdb.xml.model.enums.WorkspaceType;

public class WorkspaceDefinition implements Cloneable {
  private List<DataElement> datasetDefinitions = new ArrayList<>();

  private List<Domain> domains = new ArrayList<>();

  private String metadata;

  private String version = "";

  private WorkspaceType workspaceType = WorkspaceType.esriLocalDatabaseWorkspace;

  public void addDatasetDefinition(final DataElement datasetDefinition) {
    this.datasetDefinitions.add(datasetDefinition);
  }

  public void addDomain(final Domain domain) {
    this.domains.add(domain);
  }

  @Override
  public WorkspaceDefinition clone() {
    try {
      final WorkspaceDefinition clone = (WorkspaceDefinition)super.clone();
      clone.domains = new ArrayList<>(this.domains.size());
      for (final Domain domain : this.domains) {
        clone.domains.add(domain.clone());
      }
      clone.datasetDefinitions = new ArrayList<>();
      for (final DataElement dataElement : this.datasetDefinitions) {
        clone.datasetDefinitions.add(dataElement.clone());
      }
      return clone;
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  public List<DataElement> getDatasetDefinitions() {
    return this.datasetDefinitions;
  }

  public List<Domain> getDomains() {
    return this.domains;
  }

  public String getMetadata() {
    return this.metadata;
  }

  public String getVersion() {
    return this.version;
  }

  public WorkspaceType getWorkspaceType() {
    return this.workspaceType;
  }

  public void setDatasetDefinitions(final List<DataElement> datasetDefinitions) {
    this.datasetDefinitions = datasetDefinitions;
  }

  public void setDomains(final List<Domain> domains) {
    this.domains = domains;
  }

  public void setMetadata(final String metadata) {
    this.metadata = metadata;
  }

  public void setVersion(final String version) {
    this.version = version;
  }

  public void setWorkspaceType(final WorkspaceType workspaceType) {
    this.workspaceType = workspaceType;
  }

}
