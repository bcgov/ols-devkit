package com.revolsys.record.io.format.esri.gdb.xml.model;

import java.util.ArrayList;
import java.util.List;

public class Workspace implements Cloneable {
  private List<AnyDatasetData> workspaceData = new ArrayList<>();

  private WorkspaceDefinition workspaceDefinition = new WorkspaceDefinition();

  public Workspace() {
  }

  @Override
  public Workspace clone() {
    try {
      final Workspace clone = (Workspace)super.clone();
      this.workspaceDefinition = this.workspaceDefinition.clone();
      clone.workspaceData = new ArrayList<>(this.workspaceData.size());
      for (final AnyDatasetData data : this.workspaceData) {
        clone.workspaceData.add(data.clone());
      }
      return clone;
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  public List<AnyDatasetData> getWorkspaceData() {
    return this.workspaceData;
  }

  public WorkspaceDefinition getWorkspaceDefinition() {
    return this.workspaceDefinition;
  }

  public void setWorkspaceData(final List<AnyDatasetData> workspaceData) {
    this.workspaceData = workspaceData;
  }

  public void setWorkspaceDefinition(final WorkspaceDefinition workspaceDefinition) {
    this.workspaceDefinition = workspaceDefinition;
  }

}
