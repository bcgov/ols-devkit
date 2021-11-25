package com.revolsys.maven;

import com.revolsys.collection.CollectionUtil;
import com.revolsys.collection.map.MapEx;
import com.revolsys.properties.BaseObjectWithProperties;

public abstract class GroupArtifactVersion extends BaseObjectWithProperties {
  private String classifier;

  private String scope = "compile";

  private String artifactId;

  private String version;

  private String groupArtifactVersion;

  private String groupId;

  private String type;

  public String getArtifactId() {
    return this.artifactId;
  }

  public String getClassifier() {
    return this.classifier;
  }

  public String getGroupArtifactVersion() {
    return this.groupArtifactVersion;
  }

  public String getGroupId() {
    return this.groupId;
  }

  public String getMapValue(final MapEx map, final String key, final String defaultValue) {
    if (map == null) {
      return null;
    } else {
      final String value = map.getString(key, defaultValue);
      return replaceProperties(value);
    }
  }

  protected abstract MapEx getPomProperties();

  public String getScope() {
    return this.scope;
  }

  public String getType() {
    return this.type;
  }

  public String getVersion() {
    return this.version;
  }

  protected String replaceProperties(String value) {
    final MapEx pomProperties = getPomProperties();
    value = CollectionUtil.replaceProperties(value, pomProperties);
    return value;
  }

  public void setArtifactId(final String artifactId) {
    this.artifactId = replaceProperties(artifactId);
  }

  public void setClassifier(final String classifier) {
    this.classifier = classifier;
  }

  public void setGroupId(final String groupId) {
    this.groupId = replaceProperties(groupId);
  }

  public void setScope(final String scope) {
    this.scope = scope;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public void setVersion(final String version) {
    this.version = replaceProperties(version);
  }

  @Override
  public String toString() {
    return this.groupArtifactVersion;
  }

  protected void updateGroupArtifactVersion() {
    this.groupArtifactVersion = getGroupId() + ":" + getArtifactId() + ":" + getVersion();
  }
}
