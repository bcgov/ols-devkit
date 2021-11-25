package com.revolsys.record.io.format.zip;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import com.revolsys.io.DelegatingReader;
import com.revolsys.io.FileUtil;
import com.revolsys.io.ZipUtil;
import com.revolsys.io.filter.ExtensionFilenameFilter;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;

public class ZipRecordReader extends DelegatingReader<Record> implements RecordReader {

  private File directory;

  private RecordReader reader;

  public ZipRecordReader(final Resource resource, final String fileExtension,
    final RecordFactory factory) {
    this(resource, null, fileExtension, factory);
  }

  public ZipRecordReader(final Resource resource, final String baseName, final String fileExtension,
    final RecordFactory factory) {
    try {
      String matchBaseName;
      if (baseName == null) {
        matchBaseName = resource.getBaseName();
      } else {
        matchBaseName = baseName;
      }
      final String zipEntryName = matchBaseName + "." + fileExtension;
      this.directory = ZipUtil.unzipFile(resource);
      if (!openFile(resource, factory, zipEntryName)) {
        FileFilter filter;
        if (baseName == null) {
          filter = new ExtensionFilenameFilter(fileExtension);
        } else {
          filter = FileUtil.filterFilename(zipEntryName);
        }
        List<File> files = FileUtil.listFilesTree(this.directory, filter);
        if (files.size() == 0 && baseName != null) {
          files = FileUtil.listFilesTree(this.directory,
            new ExtensionFilenameFilter(fileExtension));
        }
        if (files.size() == 0) {
          close();
          throw new IllegalArgumentException(
            "No " + fileExtension + " files exist in zip file " + resource);
        } else if (files.size() == 1) {
          final File file = files.get(0);
          openFile(resource, factory, file);
        } else {
          close();
          throw new IllegalArgumentException(
            "Multiple " + fileExtension + " files exist in zip file " + resource);
        }
      }
      if (this.reader == null) {
        close();
        throw new IllegalArgumentException(
          "No *." + fileExtension + " exists in zip file " + resource);
      } else {
        setReader(this.reader);
      }
    } catch (final IOException e) {
      throw new RuntimeException("Error reading resource " + resource, e);
    }
  }

  @Override
  protected void closeDo() {
    FileUtil.deleteDirectory(this.directory);
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.reader.getRecordDefinition();
  }

  protected boolean openFile(final Resource resource, final RecordFactory factory,
    final File file) {
    if (file.exists()) {
      this.reader = RecordReader.newRecordReader(file, factory);
      if (this.reader == null) {
        close();
        throw new IllegalArgumentException("Cannot create reader for file "
          + FileUtil.getRelativePath(this.directory, file) + " in zip file " + resource);
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  protected boolean openFile(final Resource resource, final RecordFactory factory,
    final String zipEntryName) {
    final File file = new File(this.directory, zipEntryName);
    return openFile(resource, factory, file);
  }
}
