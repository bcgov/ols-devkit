package com.revolsys.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.jeometry.common.exception.WrappedException;
import org.jeometry.common.logging.Logs;
import org.jeometry.coordinatesystem.util.Hex;

import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Pair;
import com.revolsys.util.Property;

public class MavenRepositoryCache extends MavenRepository {
  private List<MavenRepository> repositories = new ArrayList<>();

  public MavenRepositoryCache() {
  }

  public MavenRepositoryCache(final Resource root) {
    super(root);
  }

  public MavenRepositoryCache(final Resource root, final String... repositoryUrls) {
    super(root);
    for (String repositoryUrl : repositoryUrls) {
      if (!repositoryUrl.endsWith("/")) {
        repositoryUrl += '/';
      }
      final Resource resource = Resource.getResource(repositoryUrl);
      final MavenRepository repository = new MavenRepository(resource);
      this.repositories.add(repository);
    }
  }

  public MavenRepositoryCache(final String... repositoryUrls) {
    this(null, repositoryUrls);
  }

  private boolean copyRepositoryResource(final MavenRepository repository, final Resource resource,
    final String groupId, final String artifactId, final String version, final String type,
    final String classifier, final String specificVersion, final String algorithm,
    final String path) {
    final String sha1Digest = repository.getSha1(groupId, artifactId, version, type, classifier,
      specificVersion, algorithm);
    final Resource repositoryResource = repository.getRoot().newChildResource(path);
    try {
      if (Property.hasValue(sha1Digest)) {
        final InputStream in = repositoryResource.getInputStream();
        final DigestInputStream digestIn = new DigestInputStream(in,
          MessageDigest.getInstance("SHA-1"));
        resource.copyFrom(digestIn);
        final MessageDigest messageDigest = digestIn.getMessageDigest();
        final byte[] digest = messageDigest.digest();
        final String fileDigest = Hex.toHex(digest);
        if (!sha1Digest.equals(fileDigest)) {
          Logs.error(this, ".sha1 digest is different for: " + repositoryResource);
          resource.delete();
          return false;
        }
      } else {
        resource.copyFrom(repositoryResource);
      }
      Logs.info(this, "Download maven resource: " + repositoryResource);
      return true;
    } catch (Throwable e) {
      resource.delete();
      while (e instanceof WrappedException) {
        e = e.getCause();
      }
      if (e instanceof FileNotFoundException) {
        return false;
      } else if (e instanceof IOException) {
        final IOException ioException = (IOException)e;
        if (ioException.getMessage().contains(" 404 ")) {
          return false;
        }
      }
      Logs.error(this,
        "Unable to download MVN resource " + repositoryResource + "\n  " + e.getMessage(), e);
      return false;
    }
  }

  public List<MavenRepository> getRepositories() {
    return this.repositories;
  }

  @Override
  protected Pair<Long, String> getSnapshotVersion(final String groupId, final String artifactId,
    final String version, final String type, final String classifier, final String algorithm) {
    Pair<Long, String> timestampAndVersion = null;
    MavenRepository snapshotRepository = null;
    for (final MavenRepository repository : this.repositories) {
      final Pair<Long, String> repositorySnapshotVersion = repository.getSnapshotVersion(groupId,
        artifactId, version, type, classifier, algorithm);
      if (repositorySnapshotVersion != null) {
        boolean matched = false;
        if (timestampAndVersion == null) {
          matched = true;
        } else {
          final Long repositoryLastUpdateTime = repositorySnapshotVersion.getValue1();
          final Long lastUpdateTime = timestampAndVersion.getValue1();
          if (repositoryLastUpdateTime > lastUpdateTime) {
            matched = true;
          }
        }
        if (matched) {
          timestampAndVersion = repositorySnapshotVersion;
          snapshotRepository = repository;
        }
      }
    }

    if (timestampAndVersion != null) {
      final String specificVersion = timestampAndVersion.getValue2();
      final String path = getPath(groupId, artifactId, version, type, classifier, specificVersion,
        algorithm);
      final Resource rootResource = getRoot();
      final Resource artifactResource = rootResource.newChildResource(path);
      if (version.equals(specificVersion) || !artifactResource.exists()) {
        copyRepositoryResource(snapshotRepository, artifactResource, groupId, artifactId, version,
          type, classifier, specificVersion, algorithm, path);
      }
    }
    return timestampAndVersion;
  }

  @Override
  protected Resource handleMissingResource(final Resource resource, final String groupId,
    final String artifactId, final String version, final String type, final String classifier,
    final String specificVersion, final String algorithm) {
    final String path = getPath(groupId, artifactId, version, type, classifier, version, algorithm);
    for (final MavenRepository repository : this.repositories) {
      final boolean copied = copyRepositoryResource(repository, resource, groupId, artifactId,
        version, type, classifier, specificVersion, algorithm, path);
      if (copied) {
        return resource;
      }
    }
    return resource;
  }

  public void setRepositories(final List<MavenRepository> repositories) {
    this.repositories = repositories;
  }

  public void setRepositoryLocations(final List<Resource> repositoryLocations) {
    for (final Resource resource : repositoryLocations) {
      this.repositories.add(new MavenRepository(resource));
    }
  }

  @Override
  public void setRoot(final Resource root) {
    if (root != null) {
      try {
        final File file = root.getFile();
        if (!file.exists()) {
          if (!file.mkdirs()) {
            throw new IllegalArgumentException("Cannot create maven cache directory " + file);
          }
        } else if (!file.isDirectory()) {
          throw new IllegalArgumentException("Maven cache is not a directory directory " + file);
        }
        final PathResource fileResource = new PathResource(file);
        super.setRoot(fileResource);
      } catch (final Throwable e) {
        throw new IllegalArgumentException("Maven cache must resolve to a local directory " + root,
          e);
      }
    }
  }

  @Override
  public String toString() {
    final StringBuilder string = new StringBuilder(super.toString());
    for (final MavenRepository repository : this.repositories) {
      string.append("\n  ");
      string.append(repository.toString());
    }
    return string.toString();
  }

}
