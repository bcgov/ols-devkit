package com.revolsys.maven;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.util.Property;

public class MavenPom extends GroupArtifactVersion {
  public static String getGroupAndArtifactId(final String id) {
    final String[] parts = id.split(":");
    if (parts.length < 2) {
      return id;
    } else {
      return parts[0] + ":" + parts[1];
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> List<T> getList(final MapEx map, final String key) {
    final Object value = map.get(key);
    if (value instanceof List) {
      return (List<T>)value;
    } else if (value == null) {
      return Collections.emptyList();
    } else {
      return (List<T>)Arrays.asList(value);
    }
  }

  private final MavenRepository mavenRepository;

  private final MapEx pomProperties;

  private MapEx mergedPomProperties;

  private String packaging = "jar";

  private MavenPom parentPom;

  private final List<Dependency> dependencies = new ArrayList<>();

  private String mavenId;

  private String mavenDependencyId;

  public MavenPom(final MavenRepository mavenRepository, final MapEx pom) {
    this.mavenRepository = mavenRepository;
    final MapEx dependencyContainer = (MapEx)pom.remove("dependencies");
    final MapEx pomProperties = Maps.newLinkedHashEx((MapEx)pom.remove("properties"));
    setProperties(pom);
    final MapEx parent = getProperty("parent");
    if (parent != null) {
      final String parentGroupId = parent.getString("groupId");
      if (getGroupId() == null) {
        setGroupId(parentGroupId);
      }
      final String parentVersion = parent.getString("version");
      if (getVersion() == null) {
        setVersion(parentVersion);
      }
      pomProperties.put("project.parent.groupId", parentGroupId);
      pomProperties.put("project.parent.version", parentVersion);
    }
    updateGroupArtifactVersion();
    updateMavenId();
    this.pomProperties = pomProperties;
    if (dependencyContainer != null) {
      final List<MapEx> dependencies = getList(dependencyContainer, "dependency");
      if (dependencies != null) {
        for (final MapEx dependencyMap : dependencies) {
          final Dependency dependency = new Dependency(this, dependencyMap);
          this.dependencies.add(dependency);
        }
      }
    }
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public boolean addDependenciesFromTree(final Map<String, String> dependencies,
    final String dependencyPath, final Map<String, Map<String, Map>> dependencyTree,
    final int depth, final int searchDepth) {
    boolean hasChildren = false;
    final Set<Entry<String, Map<String, Map>>> entries = dependencyTree.entrySet();
    for (final Entry<String, Map<String, Map>> dependencyEntry : entries) {
      final String childDependencyId = dependencyEntry.getKey();
      final String childPath = dependencyPath + "/" + childDependencyId;
      final Map childTree = dependencyEntry.getValue();
      final String existingDependencyPath = dependencies.get(childDependencyId);
      if (childPath.equals(existingDependencyPath)) {
        if (depth < searchDepth) {
          if (addDependenciesFromTree(dependencies, childPath, childTree, depth + 1, searchDepth)) {
            hasChildren = true;
          }
        }
      } else if (!isDependencyIgnored(dependencies.keySet(), childDependencyId)) {
        if (depth == searchDepth) {
          dependencies.put(childDependencyId, childPath);
          if (!childTree.isEmpty()) {
            hasChildren = true;
          }
        } else if (addDependenciesFromTree(dependencies, childPath, childTree, depth + 1,
          searchDepth)) {
          hasChildren = true;
        }
      }
    }
    return hasChildren;
  }

  public List<Dependency> getDependencies() {
    return this.dependencies;
  }

  @SuppressWarnings("rawtypes")
  public Set<String> getDependencyIds(final Collection<String> exclusionIds) {
    final Map<String, String> versions = getDependencyVersions();
    final Map<String, Map<String, Map>> dependencyTree = getDependencyTree(versions, exclusionIds,
      true);

    return getDependencyIdsFromTree(dependencyTree);
  }

  @SuppressWarnings("rawtypes")
  private Set<String> getDependencyIdsFromTree(final Map<String, Map<String, Map>> dependencyTree) {
    final Map<String, String> dependencyPaths = new LinkedHashMap<>();
    int searchDepth = 0;
    while (addDependenciesFromTree(dependencyPaths, "", dependencyTree, 0, searchDepth)) {
      searchDepth++;
    }
    return dependencyPaths.keySet();
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  protected Map<String, Map<String, Map>> getDependencyTree(final Map<String, String> versions,
    final Collection<String> exclusionIds, final boolean includeOptional) {
    final Map<String, Map<String, Map>> dependencies = new LinkedHashMap<>();

    for (final Dependency dependency : this.dependencies) {
      final String groupId = dependency.getGroupId();
      final String artifactId = dependency.getArtifactId();
      final String dependencyKey = groupId + ":" + artifactId;
      String version = versions.get(dependencyKey);
      if (!Property.hasValue(version)) {
        version = dependency.getVersion();
      }
      final String scope = dependency.getScope();
      final boolean optional = dependency.isOptional();
      if (scope.equals("compile") && (includeOptional || !optional)) {
        if (!Property.hasValue(version)) {
          if (groupId.equals(getGroupId())) {
            version = getVersion();
          }
        }
        if (!exclusionIds.contains(dependencyKey) && !exclusionIds.contains(groupId + ":*")) {
          try {
            final MavenPom pom = this.mavenRepository.getPom(groupId, artifactId, version);
            if (pom == null) {
              Logs.error(this,
                "Maven pom not found for " + dependencyKey + ":" + version + " in pom " + this);
            } else {
              final String dependencyId = pom.getMavenId();
              final Set<String> mergedExclusionIds = new HashSet<>(exclusionIds);
              mergedExclusionIds.addAll(dependency.getExclusionIds());

              // Add child dependencies first so they don't override parent
              final Map<String, String> mergedVersions = new HashMap<>();
              mergedVersions.putAll(pom.getDependencyVersions());
              mergedVersions.putAll(versions);

              final Map childDependencyTree = pom.getDependencyTree(mergedVersions,
                mergedExclusionIds, false);
              dependencies.put(dependencyId, childDependencyTree);
            }
          } catch (final Exception e) {
            throw new IllegalArgumentException("Unable to download pom for " + dependencyKey + ":"
              + version + " in pom " + getMavenId(), e);
          }
        }
      }
    }
    return dependencies;
  }

  public Map<String, String> getDependencyVersions() {
    final Map<String, String> versions = new HashMap<>();
    final MavenPom parent = getParentPom();
    if (parent != null) {
      versions.putAll(parent.getDependencyVersions());
    }
    final MapEx dependencyManagement = getProperty("dependencyManagement");
    if (dependencyManagement != null) {
      final MapEx dependencyMap = dependencyManagement.getValue("dependencies");
      if (dependencyMap != null) {
        final List<MapEx> dependencyList = getList(dependencyMap, "dependency");
        if (dependencyList != null) {
          for (final MapEx dependency : dependencyList) {
            final String groupId = getMapValue(dependency, "groupId", null);
            final String artifactId = getMapValue(dependency, "artifactId", null);
            final String version = getMapValue(dependency, "version", null);
            versions.put(groupId + ":" + artifactId, version);
          }
        }
      }
    }
    return versions;
  }

  public Set<String> getExclusionIds(final Collection<String> dependencyIds) {
    final Set<String> exclusionIds = new LinkedHashSet<>();
    for (final String dependencyId : dependencyIds) {
      final int index1 = dependencyId.indexOf(':');
      if (index1 != -1) {
        final int index2 = dependencyId.indexOf(':', index1 + 1);
        if (index2 != -1) {
          final String exclusionId = dependencyId.substring(0, index2);
          exclusionIds.add(exclusionId);
        }
      }
    }
    return exclusionIds;
  }

  public String getMavenDependencyId() {
    return this.mavenDependencyId;
  }

  public String getMavenId() {
    return this.mavenId;
  }

  public MavenRepository getMavenRepository() {
    return this.mavenRepository;
  }

  public String getPackaging() {
    return this.packaging;
  }

  public MavenPom getParentPom() {
    if (this.parentPom == null) {
      final MapEx parent = getProperty("parent");
      if (parent == null) {
        return null;
      } else {
        final String groupId = parent.getString("groupId");
        final String artifactId = parent.getString("artifactId");
        final String version = parent.getString("version");
        this.parentPom = this.mavenRepository.getPom(groupId, artifactId, version);
        if (this.parentPom == null) {
          Logs.error(this, "Maven pom not found for parent " + groupId + ":" + artifactId + ":"
            + version + " in pom " + this);
        }
      }
    }
    return this.parentPom;
  }

  @Override
  public MapEx getPomProperties() {
    if (this.pomProperties == null) {
      return MapEx.EMPTY;
    } else {
      if (this.mergedPomProperties == null) {
        final MapEx properties = new LinkedHashMapEx();
        final MavenPom parentPom = getParentPom();
        if (parentPom != null) {
          final MapEx parentProperties = parentPom.getPomProperties();
          properties.putAll(parentProperties);
        }

        properties.putAll(this.pomProperties);
        properties.put("project.artifactId", getArtifactId());
        properties.put("project.version", getVersion());
        properties.put("project.groupId", getGroupId());
        this.mergedPomProperties = properties;
      }
      return this.mergedPomProperties;
    }
  }

  public boolean isDependencyIgnored(final Set<String> dependencies, final String dependencyId) {
    for (final String matchedDependencyId : dependencies) {
      boolean match = true;
      final String[] parts = dependencyId.split(":");
      final String[] matchParts = matchedDependencyId.split(":");
      if (matchParts.length == parts.length) {
        for (int i = 0; i < parts.length - 2; i++) {
          final String value1 = parts[i];
          final String value2 = matchParts[i];
          if (!value1.equals(value2)) {
            match = false;
          }
        }
        if (match) {
          return true;
        }
      }
    }
    return false;
  }

  public ClassLoader newClassLoader() {
    final String mavenId = getMavenId();
    return this.mavenRepository.newClassLoader(mavenId);
  }

  public ClassLoader newClassLoader(final Collection<String> exclusionIds) {
    final String mavenId = getMavenId();
    return this.mavenRepository.newClassLoader(mavenId, exclusionIds);
  }

  public void setPackaging(final String packaging) {
    this.packaging = packaging;
  }

  @Override
  public String toString() {
    return getMavenId();
  }

  protected void updateMavenId() {
    final String groupId = getGroupId();
    final String artifactId = getArtifactId();
    final String classifier = getClassifier();
    final String version = getVersion();
    final String scope = getScope();
    this.mavenId = MavenRepository.getMavenId(groupId, artifactId, this.packaging, classifier,
      version, scope);
  }
}
