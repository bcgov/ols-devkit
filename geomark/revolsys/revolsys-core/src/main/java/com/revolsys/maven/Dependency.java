package com.revolsys.maven;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.revolsys.collection.map.MapEx;

public class Dependency extends GroupArtifactVersion {
  private final MavenPom parentPom;

  private boolean optional = false;

  public Dependency(final MavenPom parentPom, final MapEx dependency) {
    this.parentPom = parentPom;
    // final String version = getMapValue(dependency, "version", null);
    // setVersion(version);
    // dependency.remove("version");

    setProperties(dependency);
    updateGroupArtifactVersion();
  }

  public Set<String> getExclusionIds() {
    final Set<String> exclusionIds = new LinkedHashSet<>();
    final MapEx exclusionsMap = (MapEx)getProperty("exclusions");
    if (exclusionsMap != null) {
      final List<MapEx> exclusionsList = MavenPom.getList(exclusionsMap, "exclusion");
      if (exclusionsList != null) {
        for (final MapEx exclusion : exclusionsList) {
          final String groupId = this.parentPom.getMapValue(exclusion, "groupId", null);
          final String artifactId = this.parentPom.getMapValue(exclusion, "artifactId", null);
          final String exclusionId = groupId + ":" + artifactId;
          exclusionIds.add(exclusionId);
        }
      }
    }
    return exclusionIds;
  }

  public MavenPom getMavenPom() {
    final MavenRepository mavenRepository = this.parentPom.getMavenRepository();
    final String groupArtifactVersion = getGroupArtifactVersion();
    return mavenRepository.getPom(groupArtifactVersion);
  }

  public MavenPom getParentPom() {
    return this.parentPom;
  }

  @Override
  protected MapEx getPomProperties() {
    return this.parentPom.getPomProperties();
  }

  public boolean isOptional() {
    return this.optional;
  }

  public void setOptional(final boolean optional) {
    this.optional = optional;
  }
}
