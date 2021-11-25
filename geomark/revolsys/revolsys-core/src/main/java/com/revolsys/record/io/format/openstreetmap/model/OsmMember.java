package com.revolsys.record.io.format.openstreetmap.model;

public class OsmMember {
  private long ref;

  private String role;

  private String type;

  public long getRef() {
    return this.ref;
  }

  public String getRole() {
    return this.role;
  }

  public String getType() {
    return this.type;
  }

  public void setRef(final long ref) {
    this.ref = ref;
  }

  public void setRole(final String role) {
    this.role = role;
  }

  public void setType(final String type) {
    this.type = type;
  }

}
