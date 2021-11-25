package com.revolsys.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Pair;

public class ZipUtil {
  /**
   * Add the all the sub directories and files below the directory to the zip
   * output stream. The names of the files in the ZIP file will be relative to
   * the directory.
   *
   * @param zipOut The zip output stream to add the files to.
   * @param directory The directory containing the files.
   * @throws IOException
   * @throws IOException If an I/O error occurs.
   */
  public static void addDirectoryToZipFile(final ZipOutputStream zipOut, final File directory)
    throws IOException {
    addDirectoryToZipFile(zipOut, directory, directory);
  }

  /**
   * Add the all the sub directories and files below the directory to the zip
   * output stream. The names of the files in the ZIP file will be relative to
   * the baseDirectory.
   *
   * @param zipOut The zip output stream to add the files to.
   * @param baseDirectory The base directory files are relative to.
   * @param directory The directory containing the files.
   * @throws IOException If an I/O error occurs.
   */
  public static void addDirectoryToZipFile(final ZipOutputStream zipOut, final File baseDirectory,
    final File directory) throws IOException {
    final File[] files = directory.listFiles();
    if (files != null) {
      for (final File file : files) {
        if (file.isDirectory()) {
          addDirectoryToZipFile(zipOut, baseDirectory, file);
        } else {
          final String zipEntryName = FileUtil.getRelativePath(baseDirectory, file);
          zipOut.putNextEntry(new ZipEntry(zipEntryName));
          final InputStream in = new FileInputStream(file);
          FileUtil.copy(in, zipOut);
          in.close();
        }
      }
    }
  }

  /**
   * Add the list of file names to the zip output stream. The names of the files
   * in the ZIP file will be relative to the baseDirectory.
   *
   * @param zipOut The zip output stream to add the files to.
   * @param baseDirectory The base directory files are relative to.
   * @param fileNames The list of file names to add.
   * @throws IOException If an I/O error occurs.
   */
  public static void addFilesToZipFile(final ZipOutputStream zipOut, final File baseDirectory,
    final String[] fileNames) throws IOException {
    for (final String fileName : fileNames) {
      final File file = new File(baseDirectory, fileName);
      if (file.isDirectory()) {
        addDirectoryToZipFile(zipOut, baseDirectory, file);
      } else {
        zipOut.putNextEntry(new ZipEntry(fileName));
        final InputStream in = new FileInputStream(file);
        FileUtil.copy(in, zipOut);
        in.close();
      }
    }
  }

  public static Pair<Resource, GeometryFactory> getZipResourceAndGeometryFactory(
    final Resource resource, final String fileType, final GeometryFactory defaultGeometryFactory) {
    String baseName = resource.getBaseName();
    if (baseName.endsWith(fileType)) {
      baseName = baseName.replace(fileType, "");
    }
    final String fileName = baseName + fileType;

    final ZipFile zipFile = resource.getOrDownloadFile(file -> {
      try {
        return new ZipFile(file);
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    });
    ZipEntry zipEntry = zipFile.getEntry(fileName);
    if (zipEntry == null) {
      for (final Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries
        .hasMoreElements();) {
        final ZipEntry fileEntry = entries.nextElement();
        final String entryName = fileEntry.getName();
        if (entryName.endsWith(fileType)) {
          if (zipEntry == null) {
            zipEntry = fileEntry;
          } else {
            throw new IllegalArgumentException(
              resource + "contains multiple " + fileType + " files");
          }
        }
      }

    }
    if (zipEntry == null) {
      throw new IllegalArgumentException(resource + " does not contain a " + fileType + " file");
    } else {
      final Resource zipEntryResource = Resource.newResource(zipFile, zipEntry);
      final GeometryFactory geometryFactory = GeometryFactory.floating3d(zipFile, zipEntry,
        defaultGeometryFactory);
      return new Pair<>(zipEntryResource, geometryFactory);
    }
  }

  public static List<String> unzipFile(final File file, final File outputDirectory) {
    final List<String> entryNames = new ArrayList<>();
    try (
      final ZipFile zipFile = new ZipFile(file)) {
      for (final Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries
        .hasMoreElements();) {
        final ZipEntry entry = entries.nextElement();
        if (!entry.isDirectory()) {
          final String entryName = entry.getName();
          final File outputFile = new File(outputDirectory, entryName);
          outputFile.getParentFile().mkdirs();
          try (
            InputStream entryIn = zipFile.getInputStream(entry)) {
            FileUtil.copy(entryIn, outputFile, entry.getSize());
          }
          entryNames.add(entryName);
        }
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Error extracting: " + file, e);
    }
    return entryNames;
  }

  public static List<String> unzipFile(final Path zip, final Path outputDirectory) {
    return unzipFile(zip.toFile(), outputDirectory.toFile());
  }

  public static File unzipFile(final Resource resource) throws IOException {
    String filename = resource.getFilename();
    while (filename.length() < 3) {
      filename += "x";
    }
    final File directory = FileUtil.newTempDirectory(filename, ".zip");
    unzipFile(resource, directory);
    return directory;
  }

  public static boolean unzipFile(final Resource resource, final File directory)
    throws IOException {
    final InputStream in = resource.getInputStream();
    final ZipInputStream zipIn = new ZipInputStream(in);
    try {
      ZipEntry entry;
      while ((entry = zipIn.getNextEntry()) != null) {
        final String entryName = entry.getName();
        final File outputFile = new File(directory, entryName);
        outputFile.getParentFile().mkdirs();
        if (entry.isDirectory()) {
          outputFile.mkdir();
        } else {
          FileUtil.copy(zipIn, outputFile);
        }
        zipIn.closeEntry();
      }
      FileUtil.closeSilent(zipIn);
      return true;
    } catch (final IOException e) {
      FileUtil.closeSilent(zipIn);
      FileUtil.deleteDirectory(directory);
      throw e;
    }
  }

  public static boolean unzipFile(final Resource resource, final Path directory)
    throws IOException {
    final File file = directory.toFile();
    return unzipFile(resource, file);
  }

  public static void unzipSingleFile(final Path zipPath, final Path targetFile,
    final String fileExtension) {
    boolean hasMatch = false;
    try (
      final ZipFile zipFile = new ZipFile(zipPath.toFile())) {
      for (final Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries
        .hasMoreElements();) {
        final ZipEntry entry = entries.nextElement();
        if (!entry.isDirectory()) {
          final String name = entry.getName();
          if (name.endsWith("." + fileExtension)) {
            if (hasMatch) {
              throw new IllegalArgumentException(
                "Zip file cannot have more than one *." + fileExtension + " files: " + zipPath);
            } else {
              hasMatch = true;
              try (
                InputStream in = zipFile.getInputStream(entry)) {
                final Resource targetResource = Resource.getResource(targetFile);
                final Resource tempResource = targetResource.newResourceAddExtension("copy");
                tempResource.copyFrom(in);
                Files.move(tempResource.getPath(), targetFile, StandardCopyOption.ATOMIC_MOVE);
              } catch (final Exception e) {
                throw Exceptions.wrap("Error copying " + zipPath + "!" + name + " to " + targetFile,
                  e);
              }
            }
          }
        }
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Error extracting: " + zipPath, e);
    }
  }

  public static InputStream wrapStream(final ZipInputStream in) {
    return new FilterInputStream(in) {
      @Override
      public void close() throws IOException {
      }
    };
  }

  public static void zipDirectory(final File zipFile, final File directory) {
    try {
      final OutputStream outputStream = new FileOutputStream(zipFile);
      zipDirectory(directory, outputStream);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to create zip file:" + zipFile, e);
    }
  }

  public static void zipDirectory(final File directory, final OutputStream outputStream)
    throws IOException {
    final ZipOutputStream zipOut = new ZipOutputStream(outputStream);
    addDirectoryToZipFile(zipOut, directory, directory);
    zipOut.close();
  }
}
