package com.revolsys.record.io.format.esri.gdb.xml.model.enums;

public enum WorkspaceType {
  /** File-based workspaces. e.g. coverages, shapefiles, InMemory Workspaces. */
  esriFileSystemWorkspace,

  /**
   * Geodatabases that are local to your machine, e.g. A File Geodatabase or a
   * Personal geodatabase stored in an Access file.
   */
  esriLocalDatabaseWorkspace,

  /** Geodatabases that require a remote connection. e.g. ArcSDE, OLE DB. */
  esriRemoteDatabaseWorkspace;
}
