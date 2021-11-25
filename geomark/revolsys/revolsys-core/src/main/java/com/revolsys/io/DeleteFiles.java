package com.revolsys.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public class DeleteFiles {

  private static final ResourcePatternResolver RESOLVER = new PathMatchingResourcePatternResolver();

  private boolean deleteDirectories = true;

  private List<String> filePatterns = new ArrayList<>();

  @PostConstruct
  public void deleteFiles() {
    for (String filePattern : this.filePatterns) {
      if (!filePattern.startsWith("file:")) {
        filePattern = "file:" + filePattern;
      }
      try {
        for (final Resource resource : RESOLVER.getResources(filePattern)) {
          final File file = resource.getFile();
          if (file.isDirectory()) {
            if (this.deleteDirectories) {
              if (!FileUtil.deleteDirectory(file)) {
                throw new RuntimeException("Unable to delete directory: " + file);
              }
            }
          } else if (file.exists()) {
            if (!file.delete()) {
              throw new RuntimeException("Unable to delete file: " + file);
            }
          }
        }
      } catch (final Throwable e) {
        throw new RuntimeException("Cannot delete files: " + filePattern, e);
      }
    }
  }

  public List<String> getFilePatterns() {
    return this.filePatterns;
  }

  public boolean isDeleteDirectories() {
    return this.deleteDirectories;
  }

  public void setDeleteDirectories(final boolean deleteDirectories) {
    this.deleteDirectories = deleteDirectories;
  }

  public void setFilePattern(final String filePattern) {
    this.filePatterns.add(filePattern);
  }

  public void setFilePatterns(final List<String> filePatterns) {
    this.filePatterns = new ArrayList<>(filePatterns);
  }
}
