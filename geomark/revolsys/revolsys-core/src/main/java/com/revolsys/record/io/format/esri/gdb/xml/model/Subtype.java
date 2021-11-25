package com.revolsys.record.io.format.esri.gdb.xml.model;

import java.util.List;

public class Subtype {
  private List<SubtypeFieldInfo> fieldInfos;

  private int subtypeCode;

  private String subtypePath;

  public List<SubtypeFieldInfo> getFieldInfos() {
    return this.fieldInfos;
  }

  public int getSubtypeCode() {
    return this.subtypeCode;
  }

  public String getSubtypePath() {
    return this.subtypePath;
  }

  public void setFieldInfos(final List<SubtypeFieldInfo> fieldInfos) {
    this.fieldInfos = fieldInfos;
  }

  public void setSubtypeCode(final int subtypeCode) {
    this.subtypeCode = subtypeCode;
  }

  public void setSubtypePath(final String subtypePath) {
    this.subtypePath = subtypePath;
  }

}
