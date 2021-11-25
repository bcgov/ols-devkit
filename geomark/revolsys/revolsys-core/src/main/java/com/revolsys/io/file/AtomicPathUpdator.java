package com.revolsys.io.file;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.BaseCloseable;
import com.revolsys.util.Cancellable;

public class AtomicPathUpdator implements BaseCloseable {
  private static final CopyOption[] MOVE_OPTIONS = {
    StandardCopyOption.REPLACE_EXISTING
  };

  private final Path targetDirectory;

  private final String fileName;

  private Path tempDirectory;

  private Path path;

  private boolean cancelled = false;

  private final Cancellable cancellable;

  public AtomicPathUpdator(final Cancellable cancellable, final Path directory,
    final String fileName) {
    this(null, cancellable, directory, fileName);
  }

  public AtomicPathUpdator(final Path baseTempDirectory, final Cancellable cancellable,
    final Path directory, final String fileName) {
    this.cancellable = cancellable;
    try {
      this.targetDirectory = directory;
      this.fileName = fileName;
      if (baseTempDirectory == null) {
        this.tempDirectory = Files.createTempDirectory(fileName);
      } else {
        this.tempDirectory = Files.createTempDirectory(baseTempDirectory, fileName);

      }
      this.path = this.tempDirectory.resolve(fileName);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  public AtomicPathUpdator(final Path directory, final String fileName) {
    this(null, directory, fileName);
  }

  @Override
  public void close() {
    final Path tempDirectory = this.tempDirectory;
    try {
      if (!isCancelled()) {
        try {
          Files.list(tempDirectory).forEach(tempPath -> {
            final Path relativePath = tempDirectory.relativize(tempPath);
            final Path targetPath = this.targetDirectory.resolve(relativePath);
            final Path oldPath = Paths.addExtension(targetPath, "old");
            Paths.deleteDirectories(oldPath);
            move(targetPath, oldPath);
            move(tempPath, targetPath);
            Paths.deleteDirectories(oldPath);
          });
        } catch (final IOException e) {
          throw Exceptions.wrap(e);
        }
      }
    } finally {
      Paths.deleteDirectories(tempDirectory);
    }
  }

  public void deleteFiles() {
    final Path tempDirectory = this.tempDirectory;
    try {
      if (!isCancelled()) {
        try {
          Files.list(tempDirectory).forEach(tempPath -> {
            final Path relativePath = tempDirectory.relativize(tempPath);
            final Path targetPath = this.targetDirectory.resolve(relativePath);
            Paths.deleteDirectories(targetPath);
          });
        } catch (final IOException e) {
          throw Exceptions.wrap(e);
        }
      }
    } finally {
      Paths.deleteDirectories(tempDirectory);
    }
  }

  public Path getPath() {
    return this.path;
  }

  public Path getTargetDirectory() {
    return this.targetDirectory;
  }

  public Path getTargetPath() {
    return this.targetDirectory.resolve(this.fileName);
  }

  public boolean isCancelled() {
    return this.cancelled || this.cancellable != null && this.cancellable.isCancelled();
  }

  public boolean isTargetExists() {
    final Path targetFile = this.targetDirectory.resolve(this.fileName);
    return Files.exists(targetFile);
  }

  private void move(final Path sourcePath, final Path targetPath) {
    try {
      if (Files.exists(sourcePath)) {
        Files.move(sourcePath, targetPath, MOVE_OPTIONS);
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Error moving file: " + sourcePath + " to " + targetPath, e);
    }
  }

  public void setCancelled(final boolean cancelled) {
    this.cancelled = cancelled;
  }

  @Override
  public String toString() {
    return this.targetDirectory + "/" + this.fileName;
  }
}
